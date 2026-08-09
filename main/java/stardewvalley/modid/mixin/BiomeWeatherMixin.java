package stardewvalley.modid.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Precipitation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.weather.WeatherType;

@Mixin(Biome.class)
public class BiomeWeatherMixin {

    @Inject(method = "getPrecipitation", at = @At("RETURN"), cancellable = true)
    private void onGetPrecipitation(BlockPos pos, int seaLevel, CallbackInfoReturnable<Precipitation> cir) {
        if (stardewvalley.modid.gui.ClockHudRenderer.getClientWeather() == WeatherType.SNOW) {
            Precipitation original = cir.getReturnValue();
            if (original == Precipitation.RAIN) {
                cir.setReturnValue(Precipitation.SNOW);
            }
        }
    }
}
