package stardewvalley.modid.mixin;

import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.screen.ScreenTexts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectsDisplay.class)
public class StatusEffectDisplayMixin {

    @Inject(method = "getStatusEffectDescription", at = @At("HEAD"), cancellable = true)
    private void fixAmplifierDisplay(StatusEffectInstance instance, CallbackInfoReturnable<Text> cir) {
        int amplifier = instance.getAmplifier();
        MutableText text = instance.getEffectType().value().getName().copy();
        if (amplifier >= 1) {
            text.append(ScreenTexts.SPACE).append(Text.literal(toRoman(amplifier + 1)));
        }
        cir.setReturnValue(text);
    }

    @Unique
    private static String toRoman(int num) {
        if (num <= 0) return "";
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                sb.append(symbols[i]);
                num -= values[i];
            }
        }
        return sb.toString();
    }
}
