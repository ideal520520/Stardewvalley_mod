package stardewvalley.modid;

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

public class AnimalFriendshipManager extends PersistentState {

    private static final String NAME = "stardewvalley_animal_friendship";

    private final Map<UUID, Integer> friendships = new HashMap<>();
    private final Map<UUID, Long> lastFedDays = new HashMap<>();

    private record Data(Map<UUID, Integer> friendships, Map<UUID, Long> lastFedDays) {}

    private static final Codec<Map<UUID, Integer>> FRIENDSHIP_CODEC = Codec.unboundedMap(Uuids.CODEC, Codec.INT);
    private static final Codec<Map<UUID, Long>> LAST_FED_CODEC = Codec.unboundedMap(Uuids.CODEC, Codec.LONG);

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            FRIENDSHIP_CODEC.optionalFieldOf("friendships", new HashMap<>()).forGetter(Data::friendships),
            LAST_FED_CODEC.optionalFieldOf("last_fed_days", new HashMap<>()).forGetter(Data::lastFedDays)
        ).apply(instance, Data::new)
    );

    private static final Codec<AnimalFriendshipManager> CODEC = DATA_CODEC.xmap(
        data -> {
            AnimalFriendshipManager mgr = new AnimalFriendshipManager();
            mgr.friendships.putAll(data.friendships());
            mgr.lastFedDays.putAll(data.lastFedDays());
            return mgr;
        },
        mgr -> new Data(new HashMap<>(mgr.friendships), new HashMap<>(mgr.lastFedDays))
    );

    private static final PersistentStateType<AnimalFriendshipManager> TYPE = new PersistentStateType<>(
        NAME,
        AnimalFriendshipManager::new,
        SafeCodec.wrap(CODEC, AnimalFriendshipManager::new),
        DataFixTypes.LEVEL
    );

    public static AnimalFriendshipManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    /** 喂食动物，增加好感度（不超过1000），记录今天的喂食 */
    public void feed(UUID animalUuid, int amount, long currentDay) {
        int current = friendships.getOrDefault(animalUuid, 0);
        int newValue = Math.min(1000, current + amount);
        friendships.put(animalUuid, newValue);
        lastFedDays.put(animalUuid, currentDay);
        setDirty(true);
    }

    /** 增加好感度（不记录喂食，用于挤奶等操作） */
    public void addFriendship(UUID animalUuid, int amount) {
        int current = friendships.getOrDefault(animalUuid, 0);
        friendships.put(animalUuid, Math.min(1000, current + amount));
        setDirty(true);
    }

    /** 获取动物好感度 */
    public int getFriendship(UUID animalUuid) {
        return friendships.getOrDefault(animalUuid, 0);
    }

    /** 检查动物今天是否已被喂食 */
    public boolean isFedToday(UUID animalUuid, long currentDay) {
        return lastFedDays.getOrDefault(animalUuid, -1L) == currentDay;
    }

    /** 每天调用：对昨天没喂食的动物好感度减10 */
    public void onNewDay(long currentDay) {
        long yesterday = currentDay - 1;
        for (Map.Entry<UUID, Integer> entry : friendships.entrySet()) {
            UUID uuid = entry.getKey();
            long lastDay = lastFedDays.getOrDefault(uuid, -1L);
            if (lastDay < yesterday) {
                // 昨天没喂食，好感度减10
                entry.setValue(Math.max(0, entry.getValue() - 10));
                lastFedDays.put(uuid, yesterday);
            }
        }
        setDirty(true);
    }
}
