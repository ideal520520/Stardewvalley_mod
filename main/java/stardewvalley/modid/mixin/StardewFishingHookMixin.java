package stardewvalley.modid.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.fishing.FishingDataStorage;
import stardewvalley.modid.fishing.FishingHookLogic;
import stardewvalley.modid.fishing.FishingModeManager;
import stardewvalley.modid.gui.ModPayloads;
import stardewvalley.modid.item.ModFishingRodItem;
import stardewvalley.modid.item.RodComponent;
import stardewvalley.modid.season.FishingLootManager;
import java.util.ArrayList;
import java.util.List;

@Mixin(FishingBobberEntity.class)
public abstract class StardewFishingHookMixin {

    @Shadow
    private boolean caughtFish;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        FishingBobberEntity self = (FishingBobberEntity) (Object) this;

        Entity ownerEntity = self.getOwner();
        if (!(ownerEntity instanceof PlayerEntity player)) return;
        if (player.getEntityWorld().isClient()) return;
        if (!caughtFish) return;

        if (FishingModeManager.getMode() != 2) return;

        boolean holdingSvRod = FishingLootManager.isSvRod(player.getMainHandStack())
            || FishingLootManager.isSvRod(player.getOffHandStack());
        if (!holdingSvRod) return;

        ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        BlockPos pos = self.getBlockPos();

        // 读取鱼竿上的鱼饵/渔具
        ItemStack rod = FishingLootManager.isSvRod(stack) ? stack : ItemStack.EMPTY;
        if (rod.isEmpty()) {
            if (FishingLootManager.isSvRod(player.getMainHandStack())) {
                rod = player.getMainHandStack();
            } else if (FishingLootManager.isSvRod(player.getOffHandStack())) {
                rod = player.getOffHandStack();
            }
        }

        RodComponent comp = rod.get(RodComponent.TYPE);
        ItemStack bait = comp != null ? comp.getBait() : ItemStack.EMPTY;
        String baitPath = bait.isEmpty() ? "" : Registries.ITEM.getId(bait.getItem()).getPath();

        // 检查是否有珍稀诱钩
        boolean curiosityLure = false;
        if (comp != null) {
            for (int i = 0; i < comp.getTackleCount(); i++) {
                ItemStack tackle = comp.getTackle(i);
                if (!tackle.isEmpty() && "fishtool_curiosity_lure".equals(Registries.ITEM.getId(tackle.getItem()).getPath())) {
                    curiosityLure = true;
                    break;
                }
            }
        }

        List<ItemStack> loot = FishingLootManager.generateCustomLoot(player, pos, curiosityLure, "bait_magic_bait".equals(baitPath));

        // 钓鱼消耗
        ModFishingRodItem.consumeAndDamage(stack);
        player.getInventory().markDirty();
        ItemStack caughtItem = loot.get(0);
        String itemPath = Registries.ITEM.getId(caughtItem.getItem()).getPath();
        boolean isRealFish = FishingLootManager.isRealFishItem(itemPath);

        if (isRealFish) {
            // 挑战鱼饵：33%概率替换为鱼王
            boolean isChallengeBait = "bait_challenge_bait".equals(baitPath);
            ItemStack legendaryFish = FishingLootManager.tryReplaceWithLegendaryFish(serverWorld, serverPlayer, pos, isChallengeBait);
            if (legendaryFish != null) {
                caughtItem = legendaryFish;
                itemPath = Registries.ITEM.getId(caughtItem.getItem()).getPath();
            }

            // 万能鱼饵：0.25 + 当日运气/2.0 概率一次钓到两个
            if ("bait_wild_bait".equals(baitPath)) {
                float luckMult = StardewValley.getFinalLuckMultiplier(serverWorld, serverPlayer);
                if (serverWorld.random.nextFloat() < 0.25f + (luckMult - 1.0f) / 2.0f) {
                    // 将第二个鱼也存入数据存储（修改 caughtItem 数量）
                    caughtItem = caughtItem.copy();
                    caughtItem.setCount(2);
                }
            }

            // 存储水深和钓鱼等级，用于小游戏成功后判定品质
            int wDepth = FishingLootManager.calculateWaterDepth(serverWorld, pos);
            stardewvalley.modid.season.FishingLevelManager flm = stardewvalley.modid.season.FishingLevelManager.get(serverWorld);
            int fishLevel = flm.getLevel(serverPlayer.getUuid());
            FishingDataStorage.storeFishingData(serverPlayer, wDepth, fishLevel);
            FishingDataStorage.storeData(serverPlayer, self, caughtItem);
            FishingHookLogic.startMinigame(serverPlayer, caughtItem);
            serverWorld.playSound(null, pos, SoundEvents.ENTITY_FISHING_BOBBER_SPLASH,
                SoundCategory.NEUTRAL, 0.5F, 0.4F);
            cir.setReturnValue(0);
        } else {
            // 非鱼（垃圾/藻类）也应用万能鱼饵效果
            if ("bait_wild_bait".equals(baitPath)) {
                float luckMult = StardewValley.getFinalLuckMultiplier(serverWorld, serverPlayer);
                if (serverWorld.random.nextFloat() < 0.25f + (luckMult - 1.0f) / 2.0f) {
                    player.getInventory().offerOrDrop(caughtItem.copy());
                }
            }

            player.getInventory().offerOrDrop(caughtItem);
            FishingLootManager.addFishingXp(serverWorld, player, 1);
            serverWorld.playSound(null, pos, SoundEvents.ENTITY_FISHING_BOBBER_SPLASH,
                SoundCategory.NEUTRAL, 0.5F, 0.4F);
            self.discard();
            cir.setReturnValue(0);
        }
    }
}
