package stardewvalley.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.effect.ModStatusEffects;

@Mixin(ClientPlayerEntity.class)
public class ExhaustedSprintMixin {

    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
    private void preventSprintWhenExhausted(CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.hasStatusEffect(ModStatusEffects.EXHAUSTED)) {
            cir.setReturnValue(false);
        }
    }
}
