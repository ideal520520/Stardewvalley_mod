package stardewvalley.modid.mixin;

import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public abstract class HoeExhaustionMixin {

    @Unique
    private boolean exhaustModified = false;

    @ModifyArg(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V"), index = 0, require = 0)
    private float doubleHoeA(float original) { exhaustModified = true; return original * 2.0F; }

    @ModifyArg(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;addExhaustion(F)V"), index = 0, require = 0)
    private float doubleHoeB(float original) { exhaustModified = true; return original * 2.0F; }

    @Inject(method = "useOnBlock", at = @At("RETURN"))
    private void addHoeIfMissing(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!exhaustModified && cir.getReturnValue() == ActionResult.SUCCESS && !context.getWorld().isClient()) {
            if (context.getPlayer() != null) context.getPlayer().addExhaustion(0.01F);
        }
        exhaustModified = false;
    }
}