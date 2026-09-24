package stardewvalley.modid.mixin;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 原版 addInternal 会把饱和值封顶在饱食度（最大 20），导致体力池无法超过 20。
 * 此处替换算法：饱食度仍封顶 20，溢出的食物值转化为饱和值，饱和值不再封顶。
 */
@Mixin(HungerManager.class)
public abstract class HungerManagerSaturationMixin {

    @Shadow
    private int foodLevel;

    @Shadow
    private float saturationLevel;

    @Inject(method = "addInternal", at = @At("HEAD"), cancellable = true)
    private void stardew$uncapSaturation(int nutrition, float saturation, CallbackInfo ci) {
        float added = saturation;
        if (this.foodLevel + nutrition > 20) {
            added += (this.foodLevel + nutrition - 20);
        }
        this.foodLevel = Math.min(this.foodLevel + nutrition, 20);
        this.saturationLevel += added;
        ci.cancel();
    }
}
