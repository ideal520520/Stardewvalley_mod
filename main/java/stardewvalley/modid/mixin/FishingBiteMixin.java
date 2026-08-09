package stardewvalley.modid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import stardewvalley.modid.fishing.FishingModeManager;
import stardewvalley.modid.fishing.FishingSounds;
import stardewvalley.modid.fishing.S2CFishBitePayload;
import stardewvalley.modid.season.FishingLootManager;

/**
 * 模式1（原始模式）鱼咬钩时，替换世界广播音效为仅通知钓鱼玩家自己
 */
@Mixin(FishingBobberEntity.class)
public class FishingBiteMixin {

    @Redirect(method = "tickFishingLogic", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    private void redirectBiteSound(FishingBobberEntity instance, SoundEvent event, float volume, float pitch) {
        if (FishingModeManager.getMode() == 1) {
            Entity owner = instance.getOwner();
            if (owner instanceof PlayerEntity player && !player.getEntityWorld().isClient()) {
                boolean holdingSvRod = FishingLootManager.isSvRod(player.getMainHandStack())
                    || FishingLootManager.isSvRod(player.getOffHandStack());
                if (holdingSvRod) {
                    // 仅发送网络包，玩家本地播放，不广播世界音效
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                        (ServerPlayerEntity) player, new S2CFishBitePayload());
                    return;
                }
            }
            // 非SV鱼竿仍使用星露谷风格提示音，但只自己听
            instance.playSound(FishingSounds.FISH_BITE, 2.0F, 1.0F);
        } else {
            instance.playSound(event, volume, pitch);
        }
    }
}
