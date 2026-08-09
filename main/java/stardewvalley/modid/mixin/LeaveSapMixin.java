package stardewvalley.modid.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.weather.WeatherState;
import stardewvalley.modid.weather.WeatherType;

@Mixin(LeavesBlock.class)
public class LeaveSapMixin {

    @Inject(method = "scheduledTick", at = @At("HEAD"))
    private void onLeafDecay(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (world.isClient()) return;
        if (state.get(LeavesBlock.DISTANCE) == 7) {
            // 15%概率掉落树液
            if (random.nextFloat() < 0.15f) {
                Item sap = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "caiji_sap"));
                if (sap != null) {
                    ItemStack stack = new ItemStack(sap);
                    net.minecraft.entity.ItemEntity entity = new net.minecraft.entity.ItemEntity(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    world.spawnEntity(entity);
                }
            }
            // 绿雨天25%概率掉落苔藓
            boolean isGreenRain = WeatherState.get(world).todayWeather == WeatherType.GREEN_RAIN;
            if (isGreenRain && random.nextFloat() < 0.25f) {
                Item moss = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "moss"));
                if (moss != null) {
                    ItemStack stack = new ItemStack(moss);
                    net.minecraft.entity.ItemEntity entity = new net.minecraft.entity.ItemEntity(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    world.spawnEntity(entity);
                }
            }
            // 雨天50%概率掉落苔藓（非绿雨天的原逻辑）
            if (!isGreenRain && world.isRaining() && random.nextFloat() < 0.50f) {
                Item moss = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "moss"));
                if (moss != null) {
                    ItemStack stack = new ItemStack(moss);
                    net.minecraft.entity.ItemEntity entity = new net.minecraft.entity.ItemEntity(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    world.spawnEntity(entity);
                }
            }

            // 树叶自然腐烂时2%概率*每日运气掉落星露谷种子
            float luckMult = StardewValley.getLuckMultiplier(world);
            String leafPath = Registries.BLOCK.getId(state.getBlock()).getPath();
            String seedId = null;
            if (leafPath.contains("oak")) {
                seedId = "acorn";
            } else if (leafPath.contains("cherry") || leafPath.contains("birch")) {
                seedId = "maple_seed";
            } else if (leafPath.contains("acacia") || leafPath.contains("spruce")) {
                seedId = "pine_cone";
            }
            if (seedId != null && random.nextFloat() < 0.02f * luckMult) {
                Item seed = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, seedId));
                if (seed != null) {
                    ItemStack stack = new ItemStack(seed);
                    net.minecraft.entity.ItemEntity entity = new net.minecraft.entity.ItemEntity(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    world.spawnEntity(entity);
                }
            }
        }
    }
}
