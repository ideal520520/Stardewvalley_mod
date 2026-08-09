package stardewvalley.modid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.fishing.FishingDataStorage;
import stardewvalley.modid.fishing.FishingModeManager;
import stardewvalley.modid.fishing.S2CFishBitePayload;
import stardewvalley.modid.item.ModFishingRodItem;
import stardewvalley.modid.item.RodComponent;
import stardewvalley.modid.season.FishingLevelManager;
import stardewvalley.modid.season.FishingLootManager;

import java.util.List;

@Mixin(FishingBobberEntity.class)
public abstract class FishingHookTimeMixin {

    @Shadow private boolean caughtFish;

    @Unique
    private int stardewWaitTimer = -1;

    @Unique
    private int escapeTicks = 0;

    @Unique
    private static float getBaitWaitMultiplier(RodComponent comp) {
        if (comp == null) return 1f;
        ItemStack bait = comp.getBait();
        if (bait.isEmpty()) return 1f;
        String path = Registries.ITEM.getId(bait.getItem()).getPath();
        // 针对性鱼饵（bait_{fish}_bait）也提供50%加速
        if (path.startsWith("bait_") && path.endsWith("_bait")) {
            return 0.5f;
        }
        return switch (path) {
            case "bait_bait", "bait_magnet" -> 0.5f;
            case "bait_wild_bait", "bait_challenge_bait" -> 0.375f;
            case "bait_deluxe_bait" -> 0.33f;
            default -> 1f;
        };
    }

    @Unique
    private static int getTackleFlatReduction(RodComponent comp) {
        if (comp == null) return 0;
        int reduction = 0;
        for (int i = 0; i < comp.getTackleCount(); i++) {
            ItemStack tackle = comp.getTackle(i);
            if (tackle.isEmpty()) continue;
            String path = Registries.ITEM.getId(tackle.getItem()).getPath();
            reduction += switch (path) {
                case "fishtool_spinner" -> 75;
                case "fishtool_dressed_spinner" -> 150;
                default -> 0;
            };
        }
        return reduction;
    }

    @Unique
    private static ItemStack getRodStack(PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (FishingLootManager.isSvRod(main)) return main;
        ItemStack off = player.getOffHandStack();
        if (FishingLootManager.isSvRod(off)) return off;
        return ItemStack.EMPTY;
    }

    @Inject(method = "tickFishingLogic", at = @At("HEAD"), cancellable = true)
    private void onTickFishingLogic(BlockPos pos, CallbackInfo ci) {
        FishingBobberEntity self = (FishingBobberEntity) (Object) this;
        Entity owner = self.getOwner();

        if (!(owner instanceof PlayerEntity player)) return;
        if (player.getEntityWorld().isClient()) return;
        if (FishingModeManager.getMode() != 2) return;

        boolean holdingSvRod = FishingLootManager.isSvRod(player.getMainHandStack())
            || FishingLootManager.isSvRod(player.getOffHandStack());
        if (!holdingSvRod) return;

        ci.cancel();

        if (this.caughtFish) {
            if (player instanceof ServerPlayerEntity sp && FishingDataStorage.getItemsForPlayer(sp) != null) {
                return;
            }
            this.escapeTicks++;
            if (this.escapeTicks >= 60) {
                this.caughtFish = false;
                this.escapeTicks = 0;
                this.stardewWaitTimer = -1;
                self.playSound(net.minecraft.sound.SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 1.0F, 0.4F + self.getRandom().nextFloat() * 0.4F);
            }
            return;
        }

        if (!self.isTouchingWater()) {
            return;
        }

        Random random = self.getRandom();

        if (this.stardewWaitTimer < 0) {
            ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
            FishingLevelManager flm = FishingLevelManager.get(serverWorld);
            int fishingLevel = flm.getLevel(player.getUuid());

            int maxWaitTicks = Math.max(12, 600 - fishingLevel * 10);
            int rolledWait = 12 + random.nextInt(maxWaitTicks - 12 + 1);

            ItemStack rod = getRodStack(player);
            RodComponent comp = rod != null ? rod.get(RodComponent.TYPE) : null;

            float baitMult = getBaitWaitMultiplier(comp);
            rolledWait = Math.max(12, Math.round(rolledWait * baitMult));

            int tackleReduction = getTackleFlatReduction(comp);
            rolledWait = Math.max(12, rolledWait - tackleReduction);

            this.stardewWaitTimer = rolledWait;
        }

        if (this.stardewWaitTimer > 0) {
            this.stardewWaitTimer--;
            return;
        }

        this.caughtFish = true;
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
            (ServerPlayerEntity) player, new S2CFishBitePayload());
    }
}
