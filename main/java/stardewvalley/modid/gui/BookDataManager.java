package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.*;

public class BookDataManager extends PersistentState {

    private static final String NAME = "stardewvalley_books";

    private static final Codec<Set<UUID>> UUID_SET_CODEC = Uuids.CODEC.listOf().xmap(
        HashSet::new, List::copyOf
    );

    private static final Codec<Map<String, Set<UUID>>> USED_BOOKS_CODEC = Codec.unboundedMap(Codec.STRING, UUID_SET_CODEC)
        .xmap(HashMap::new, HashMap::new);

    private static final Codec<Map<UUID, Integer>> COUNTER_CODEC = Codec.unboundedMap(Uuids.CODEC, Codec.INT)
        .xmap(HashMap::new, HashMap::new);

    // 拆开写避免类型推断问题
    private static final Codec<Map<UUID, Integer>> INNER_MAP_CODEC = Codec.unboundedMap(Uuids.CODEC, Codec.INT)
        .xmap((Map<UUID, Integer> m) -> new HashMap<>(m), (Map<UUID, Integer> m) -> new HashMap<>(m));
    private static final Codec<Map<String, Map<UUID, Integer>>> DROP_COUNTER_CODEC = Codec.unboundedMap(Codec.STRING, INNER_MAP_CODEC)
        .xmap((Map<String, Map<UUID, Integer>> m) -> new HashMap<>(m), (Map<String, Map<UUID, Integer>> m) -> new HashMap<>(m));

    private record Data(Map<String, Set<UUID>> usedBooks, Map<UUID, Integer> counters, Map<String, Map<UUID, Integer>> dropCounters) {}

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            USED_BOOKS_CODEC.fieldOf("used_books").forGetter(Data::usedBooks),
            COUNTER_CODEC.fieldOf("counters").forGetter(Data::counters),
            DROP_COUNTER_CODEC.fieldOf("drop_counters").forGetter(Data::dropCounters)
        ).apply(instance, Data::new)
    );

    private static final Codec<BookDataManager> CODEC = DATA_CODEC.xmap(
        data -> {
            BookDataManager m = new BookDataManager(data.usedBooks);
            m.counters.putAll(data.counters);
            m.dropCounters.putAll(data.dropCounters);
            return m;
        },
        m -> new Data(m.usedBooks, m.counters, m.dropCounters)
    );

    public static final PersistentStateType<BookDataManager> TYPE = new PersistentStateType<>(
        NAME,
        BookDataManager::new,
        SafeCodec.wrap(CODEC, BookDataManager::new),
        DataFixTypes.LEVEL
    );

    private final Map<String, Set<UUID>> usedBooks;
    private final Map<UUID, Integer> counters = new HashMap<>();
    private final Map<String, Map<UUID, Integer>> dropCounters = new HashMap<>();

    public BookDataManager() {
        this(new HashMap<>());
    }

    public BookDataManager(Map<String, Set<UUID>> usedBooks) {
        this.usedBooks = usedBooks;
    }

    public static BookDataManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public boolean hasUsedBook(UUID playerUuid, String bookId) {
        Set<UUID> players = usedBooks.get(bookId);
        return players != null && players.contains(playerUuid);
    }

    public void markBookUsed(UUID playerUuid, String bookId) {
        usedBooks.computeIfAbsent(bookId, k -> new HashSet<>()).add(playerUuid);
        setDirty(true);
    }

    public int getAndIncrementCounter(UUID playerUuid) {
        int val = counters.getOrDefault(playerUuid, 0) + 1;
        counters.put(playerUuid, val);
        setDirty(true);
        return val;
    }

    public int getCounter(UUID playerUuid) {
        return counters.getOrDefault(playerUuid, 0);
    }

    // ====== 渐进掉落概率追踪 ======

    /** 获取玩家对某本书的当前尝试次数 */
    public int getDropAttempts(UUID playerUuid, String bookId) {
        Map<UUID, Integer> perPlayer = dropCounters.get(bookId);
        return perPlayer != null ? perPlayer.getOrDefault(playerUuid, 0) : 0;
    }

    /** 增加尝试次数并返回 */
    public int incrementDropAttempts(UUID playerUuid, String bookId) {
        Map<UUID, Integer> perPlayer = dropCounters.computeIfAbsent(bookId, k -> new HashMap<>());
        int val = perPlayer.getOrDefault(playerUuid, 0) + 1;
        perPlayer.put(playerUuid, val);
        setDirty(true);
        return val;
    }

    /** 重置尝试次数（掉落成功后调用） */
    public void resetDropAttempts(UUID playerUuid, String bookId) {
        Map<UUID, Integer> perPlayer = dropCounters.get(bookId);
        if (perPlayer != null) {
            perPlayer.remove(playerUuid);
            setDirty(true);
        }
    }

    /** 检查玩家是否曾掉落过某本书（用"drop_"+bookId作为key记录） */
    public boolean hasDroppedBook(UUID playerUuid, String bookId) {
        return hasUsedBook(playerUuid, "drop_" + bookId);
    }

    /** 标记玩家已掉落过某本书 */
    public void markBookDropped(UUID playerUuid, String bookId) {
        markBookUsed(playerUuid, "drop_" + bookId);
    }

    /** 追踪玩家获得某本书的次数（用于max 2的情况） */
    public int getBookObtainCount(UUID playerUuid, String bookId) {
        return getDropAttempts(playerUuid, "obtain_" + bookId);
    }
    public int incrementBookObtainCount(UUID playerUuid, String bookId) {
        return incrementDropAttempts(playerUuid, "obtain_" + bookId);
    }

    /**
     * 渐进掉落判定
     * @return true=掉落书本, false=未掉落
     */
    public static boolean rollProgressiveDrop(ServerWorld world, ServerPlayerEntity player, String bookId,
                                               float baseRate, float incrementPerAttempt, float stableRate, float luckMult) {
        BookDataManager mgr = get(world);
        boolean alreadyDropped = mgr.hasDroppedBook(player.getUuid(), bookId);

        float prob;
        if (alreadyDropped) {
            prob = stableRate;
        } else {
            int attempts = mgr.getDropAttempts(player.getUuid(), bookId);
            prob = baseRate + attempts * incrementPerAttempt;
        }

        if (world.random.nextFloat() < prob * luckMult) {
            net.minecraft.item.Item bookItem = net.minecraft.registry.Registries.ITEM.get(
                net.minecraft.util.Identifier.of(stardewvalley.modid.StardewValley.MOD_ID, bookId));
            if (bookItem != null) {
                net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
                    world, player.getX(), player.getY() + 0.5, player.getZ(),
                    new net.minecraft.item.ItemStack(bookItem, 1));
                world.spawnEntity(itemEntity);
            }
            if (!alreadyDropped) {
                mgr.markBookDropped(player.getUuid(), bookId);
                mgr.resetDropAttempts(player.getUuid(), bookId);
            }
            return true;
        } else {
            if (!alreadyDropped) {
                mgr.incrementDropAttempts(player.getUuid(), bookId);
            }
            return false;
        }
    }
}