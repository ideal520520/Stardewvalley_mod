package stardewvalley.modid.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 垃圾桶升级等级（0=无, 1=铜, 2=铁, 3=金, 4=铱）+ 玩家级暂存物品 */
public class TrashCanStateManager extends PersistentState {
    private static final String NAME = "stardewvalley_trashcan";

    private int level = 0;
    /** playerUUID -> (itemId, count) */
    private final Map<UUID, PendingItem> pendingItems = new HashMap<>();

    public static class PendingItem {
        public String itemId = "";
        public int count = 0;
        public PendingItem() {}
        public PendingItem(String itemId, int count) { this.itemId = itemId; this.count = count; }
    }

    private record SaveData(int level, Map<UUID, PendingItem> items) {}
    private static final Codec<PendingItem> ITEM_CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.STRING.fieldOf("itemId").forGetter(p -> p.itemId),
            Codec.INT.fieldOf("count").forGetter(p -> p.count)
        ).apply(inst, PendingItem::new)
    );
    private static final Codec<Map<UUID, PendingItem>> ITEMS_CODEC = Codec.unboundedMap(
        Codec.STRING.xmap(UUID::fromString, UUID::toString), ITEM_CODEC
    );
    private static final Codec<SaveData> SAVE_CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.INT.fieldOf("level").forGetter(d -> d.level),
            ITEMS_CODEC.optionalFieldOf("pendingItems", Map.of()).forGetter(d -> d.items)
        ).apply(inst, SaveData::new)
    );
    public static final Codec<TrashCanStateManager> CODEC = SAVE_CODEC.xmap(
        d -> { TrashCanStateManager m = new TrashCanStateManager(); m.level = d.level; m.pendingItems.putAll(d.items); return m; },
        m -> new SaveData(m.level, new HashMap<>(m.pendingItems))
    );
    public static final PersistentStateType<TrashCanStateManager> TYPE = new PersistentStateType<>(
        NAME, TrashCanStateManager::new, SafeCodec.wrap(CODEC, TrashCanStateManager::new), DataFixTypes.LEVEL
    );

    public static TrashCanStateManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public int getLevel() { return level; }
    public void setLevel(int l) { level = l; setDirty(true); }

    public static double getRecycleRate(int level) {
        return switch (level) { case 1 -> 0.15; case 2 -> 0.30; case 3 -> 0.45; case 4 -> 0.60; default -> 0.0; };
    }

    // === 玩家暂存物品 ===

    public PendingItem getPendingItem(UUID playerUuid) {
        return pendingItems.getOrDefault(playerUuid, new PendingItem());
    }

    public void setPendingItem(UUID playerUuid, String itemId, int count) {
        if (count <= 0 || itemId.isEmpty()) {
            pendingItems.remove(playerUuid);
        } else {
            pendingItems.put(playerUuid, new PendingItem(itemId, count));
        }
        setDirty(true);
    }

    public void clearPendingItem(UUID playerUuid) {
        pendingItems.remove(playerUuid);
        setDirty(true);
    }
}
