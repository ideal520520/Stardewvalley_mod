package stardewvalley.modid.mixin;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 原版 addExhaustion 会把疲劳值封顶在 40，导致超过 40 的消耗（如金星/铱星虚空蛋黄酱）被截断成同一数值。
 * 这里去掉该上限，让高星级负面食物按实际疲劳值扣减。
 */
@Mixin(HungerManager.class)
public abstract class HungerManagerExhaustionMixin {

    @Shadow
    private float exhaustion;

    @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
    private void stardew$uncapExhaustion(float amount, CallbackInfo ci) {
        this.exhaustion += amount;
        ci.cancel();
    }
}
