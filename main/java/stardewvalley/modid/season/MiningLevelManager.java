package stardewvalley.modid.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.util.SafeCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiningLevelManager extends PersistentState {

    private static final String NAME = "stardewvalley_mining";

    private final Map<UUID, MiningData> playerData = new HashMap<>();

    public static class MiningData {
        public int level;
        public int xp;

        public MiningData(int level, int xp) {
            this.level = level;
            this.xp = xp;
        }
    }

    public static final Codec<MiningData> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("level").forGetter(d -> d.level),
            Codec.INT.fieldOf("xp").forGetter(d -> d.xp)
        ).apply(instance, MiningData::new)
    );

    public static final Codec<MiningLevelManager> CODEC = Codec.unboundedMap(
        Uuids.CODEC, DATA_CODEC
    ).xmap(
        map -> {
            MiningLevelManager manager = new MiningLevelManager();
            manager.playerData.putAll(map);
            return manager;
        },
        manager -> manager.playerData
    );

    public static final PersistentStateType<MiningLevelManager> TYPE = new PersistentStateType<>(
        NAME,
        MiningLevelManager::new,
        SafeCodec.wrap(CODEC, MiningLevelManager::new),
        DataFixTypes.LEVEL
    );

    public static MiningLevelManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public int getLevel(UUID playerUuid) {
        return playerData.getOrDefault(playerUuid, new MiningData(0, 0)).level;
    }

    public int getXp(UUID playerUuid) {
        return playerData.getOrDefault(playerUuid, new MiningData(0, 0)).xp;
    }

    private static final int[] XP_THRESHOLDS = {100, 380, 770, 1300, 2150, 3300, 4800, 6900, 10000, 15000};
    private static final int MAX_LEVEL = 10;

    public void addXp(UUID playerUuid, int amount) {
        MiningData data = playerData.computeIfAbsent(playerUuid, k -> new MiningData(0, 0));
        data.xp += amount;
        data.level = 0;
        for (int lvl = MAX_LEVEL; lvl >= 1; lvl--) {
            if (data.xp >= XP_THRESHOLDS[lvl - 1]) {
                data.level = lvl;
                break;
            }
        }
        setDirty(true);
    }
}
