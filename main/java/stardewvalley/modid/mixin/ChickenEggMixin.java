package stardewvalley.modid.mixin;

import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.AnimalFriendshipManager;
import stardewvalley.modid.AnimalQualityHelper;
import stardewvalley.modid.ChickenBindingManager;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.GoldenAnimalCrackerManager;
import stardewvalley.modid.season.LuckManager;

@Mixin(ChickenEntity.class)
public class ChickenEggMixin {

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/ChickenEntity;forEachGiftedItem(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/registry/RegistryKey;Ljava/util/function/BiConsumer;)Z"), cancellable = true)
    private void onDropItem(CallbackInfo ci) {
        ChickenEntity chicken = (ChickenEntity)(Object)this;
        ServerWorld world = (ServerWorld) chicken.getEntityWorld();
        if (chicken.eggLayTime <= 0 && chicken.isAlive() && !chicken.isBaby()) {
            ci.cancel();

            // 检查这只鸡是否曾被鸡舍大师玩家喂食过
            boolean hasCoopmaster = ChickenBindingManager.get(world).isCoopmasterFed(chicken.getUuid());

            // 鸡舍大师：产蛋触发次数翻倍
            int loops = 1;
            if (hasCoopmaster) loops *= 2;
            // 金色动物饼干：产蛋触发次数再翻倍（与鸡舍大师叠加为4次）
            if (GoldenAnimalCrackerManager.get(world).isBoosted(chicken.getUuid())) loops *= 2;
            AnimalQualityHelper.Quality quality = AnimalQualityHelper.Quality.NORMAL;
            if (world instanceof ServerWorld sw) {
                quality = AnimalQualityHelper.getQualityForAnimal(chicken, sw);
            }
            for (int j = 0; j < loops; j++) {
                ItemStack eggStack;
                net.minecraft.item.Item baseEgg = getRandomEgg(chicken.getRandom(), hasCoopmaster);
                String baseName = Registries.ITEM.getId(baseEgg).getPath();
                Identifier qualityId = AnimalQualityHelper.getQualityItemId(baseName, quality);
                eggStack = new ItemStack(Registries.ITEM.get(qualityId), 1);
                chicken.dropStack(world, eggStack);
            }

            chicken.playSound(net.minecraft.sound.SoundEvents.ENTITY_CHICKEN_EGG, 1.0f, (chicken.getRandom().nextFloat() - chicken.getRandom().nextFloat()) * 0.2f + 1.0f);
            chicken.emitGameEvent(net.minecraft.world.event.GameEvent.ENTITY_PLACE);
            chicken.eggLayTime = chicken.getRandom().nextInt(6000) + 6000;
        }
    }

    private static net.minecraft.item.Item getRandomEgg(net.minecraft.util.math.random.Random random, boolean coopmaster) {
        if (coopmaster) {
            float r = random.nextFloat();
            if (r < 0.25f) {
                return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "egg"));
            } else if (r < 0.50f) {
                return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "brown_egg"));
            } else if (r < 0.60f) {
                return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "large_egg"));
            } else if (r < 0.70f) {
                return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "large_brown_egg"));
            } else if (r < 0.78f) {
                return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "void_egg"));
            } else if (r < 0.85f) {
                return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "gold_egg"));
            } else {
                return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "dinosaur_egg"));
            }
        }

        if (random.nextFloat() < 0.05f) {
            return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "dinosaur_egg"));
        }
        float r = random.nextFloat();
        if (r < 0.35f) {
            return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "egg"));
        } else if (r < 0.70f) {
            return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "brown_egg"));
        } else if (r < 0.80f) {
            return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "large_egg"));
        } else if (r < 0.90f) {
            return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "large_brown_egg"));
        } else if (r < 0.95f) {
            return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "void_egg"));
        } else {
            return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "gold_egg"));
        }
    }
}
