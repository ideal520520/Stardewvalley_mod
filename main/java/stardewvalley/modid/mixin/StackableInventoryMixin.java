package stardewvalley.modid.mixin;

import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public interface StackableInventoryMixin {
    @Inject(method = "getMaxCountPerStack", at = @At("RETURN"), cancellable = true)
    default void onGetMaxCountPerStack(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(999);
    }
}
