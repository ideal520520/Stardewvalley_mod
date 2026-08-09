package stardewvalley.modid.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlimeEntity.class)
public class SlimeSpawnMixin {

    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private static void onCanSpawn(EntityType<SlimeEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        // 将史莱姆的生成逻辑改为和蜘蛛/僵尸等普通敌对生物相同
        // 原版史莱姆特殊生成逻辑：特定史莱姆区块（Y<40）或沼泽（Y51-69，亮度≤7）
        // 改为：亮度等级为0的可生成方块上生成（与普通怪物相同）
        if (world instanceof ServerWorldAccess serverWorldAccess) {
            cir.setReturnValue(HostileEntity.canSpawnInDark(type, serverWorldAccess, reason, pos, random));
        }
    }
}
