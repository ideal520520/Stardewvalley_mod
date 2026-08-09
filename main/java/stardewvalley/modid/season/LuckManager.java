package stardewvalley.modid.season;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.Random;

import stardewvalley.modid.util.SafeCodec;

public class LuckManager extends PersistentState {

    private static final String NAME = "stardewvalley_luck";

    private float dailyLuck;
    private long lastRefreshDay;

    private record LuckData(float dailyLuck, long lastRefreshDay) {}

    private static final Codec<LuckData> LUCK_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("dailyLuck").forGetter(LuckData::dailyLuck),
            Codec.LONG.fieldOf("lastRefreshDay").forGetter(LuckData::lastRefreshDay)
        ).apply(instance, LuckData::new)
    );

    // 新格式：平坦结构 {dailyLuck, lastRefreshDay}
    private static final Codec<LuckManager> NEW_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("dailyLuck").forGetter(m -> m.dailyLuck),
            Codec.LONG.fieldOf("lastRefreshDay").forGetter(m -> m.lastRefreshDay)
        ).apply(instance, (luck, day) -> {
            LuckManager mgr = new LuckManager();
            mgr.dailyLuck = luck;
            mgr.lastRefreshDay = day;
            return mgr;
        })
    );

    // 旧格式：UUID → {dailyLuck, lastRefreshDay}（兼容旧版本存档）
    // 迁移策略：取第一个玩家的幸运数据
    private static final Codec<LuckManager> OLD_CODEC =
        Codec.unboundedMap(Codec.STRING, LUCK_DATA_CODEC)
            .xmap(
                map -> {
                    LuckManager mgr = new LuckManager();
                    if (!map.isEmpty()) {
                        LuckData data = map.values().iterator().next();
                        mgr.dailyLuck = data.dailyLuck;
                        mgr.lastRefreshDay = data.lastRefreshDay;
                    }
                    return mgr;
                },
                mgr -> java.util.Map.of()
            );

    // 先尝试新格式，失败则回退旧格式进行迁移
    public static final Codec<LuckManager> CODEC =
        Codec.either(NEW_CODEC, OLD_CODEC)
            .xmap(
                either -> either.map(l -> l, l -> l),
                mgr -> Either.left(mgr)
            );

    private static final PersistentStateType<LuckManager> TYPE = new PersistentStateType<>(
        NAME,
        LuckManager::new,
        SafeCodec.wrap(CODEC, LuckManager::new),
        DataFixTypes.LEVEL
    );

    public static LuckManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    /** 获取当天所有玩家共享的运气值，若未刷新则重新随机生成 */
    public float getDailyLuck(long currentDay) {
        if (lastRefreshDay != currentDay) {
            Random rand = new Random();
            dailyLuck = -0.1f + rand.nextFloat() * 0.2f;
            lastRefreshDay = currentDay;
            setDirty(true);
        }
        return dailyLuck;
    }

    /** 获取当前运气值（不触发刷新） */
    public float getCurrentLuck() {
        return dailyLuck;
    }
}
