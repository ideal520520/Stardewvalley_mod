package stardewvalley.modid.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataComponentTypes.class)
public class StackableDataComponentMixin {
    @ModifyExpressionValue(
            method = "method_58570",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/dynamic/Codecs;rangedInt(II)Lcom/mojang/serialization/Codec;")
    )
    private static Codec<Integer> replaceCodec(Codec<Integer> original) {
        return Codec.intRange(0, 999);
    }
}
