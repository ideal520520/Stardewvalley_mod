package stardewvalley.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataComponentTypes.class)
public class StackableMaxCountMixin {
    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(value = "CONSTANT", args = "intValue=64")
    )
    private static int getMaxCountPerStack(int original) {
        return 999;
    }
}
