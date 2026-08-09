package stardewvalley.modid.weather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.util.SafeCodec;

public class WeatherState extends PersistentState {

    private static final String NAME = "stardewvalley_weather";

    public WeatherType todayWeather = WeatherType.SUN;
    public WeatherType tomorrowWeather = WeatherType.SUN;
    public long lastCalculationDay = -1;
    public boolean rainTotemOverride = false;
    public long rainTotemDay = -1;

    public static final Codec<WeatherState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("today_weather").forGetter(s -> s.todayWeather.name()),
            Codec.STRING.fieldOf("tomorrow_weather").forGetter(s -> s.tomorrowWeather.name()),
            Codec.LONG.fieldOf("last_calculation_day").forGetter(s -> s.lastCalculationDay),
            Codec.BOOL.optionalFieldOf("rain_totem_override", false).forGetter(s -> s.rainTotemOverride),
            Codec.LONG.optionalFieldOf("rain_totem_day", -1L).forGetter(s -> s.rainTotemDay)
        ).apply(instance, (today, tomorrow, calcDay, override, totemDay) -> {
            WeatherState state = new WeatherState();
            state.todayWeather = WeatherType.valueOf(today);
            state.tomorrowWeather = WeatherType.valueOf(tomorrow);
            state.lastCalculationDay = calcDay;
            state.rainTotemOverride = override;
            state.rainTotemDay = totemDay;
            return state;
        })
    );

    public static final PersistentStateType<WeatherState> TYPE = new PersistentStateType<>(
        NAME,
        WeatherState::new,
        SafeCodec.wrap(CODEC, WeatherState::new),
        DataFixTypes.LEVEL
    );

    public static WeatherState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }
}
