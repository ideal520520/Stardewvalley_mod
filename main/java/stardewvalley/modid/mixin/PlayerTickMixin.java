package stardewvalley.modid.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.effect.ModStatusEffects;
import stardewvalley.modid.equipment.RingEffectHandler;

@Mixin(PlayerEntity.class)
public class PlayerTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getEntityWorld().isClient()) return;
        if (!(self instanceof ServerPlayerEntity player)) return;

        RingEffectHandler.onPlayerTick(player);

        // 精疲力尽检查：饱食度<2时获得debuff，仅通过肌肉回复药或睡觉消除
        if (player.getHungerManager().getFoodLevel() < 2 && !player.hasStatusEffect(ModStatusEffects.EXHAUSTED)) {
            player.addStatusEffect(new StatusEffectInstance(
                ModStatusEffects.EXHAUSTED, StatusEffectInstance.INFINITE, 0, false, true, true));
        }
    }
}
