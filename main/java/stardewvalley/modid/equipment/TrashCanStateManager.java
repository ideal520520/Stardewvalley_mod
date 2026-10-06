package stardewvalley.modid.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 垃圾桶升级等级（0=无, 1=铜, 2=铁, 3=金, 4=铱）+ 玩家级暂存物品（完整 ItemStack，保留组件） */
public class TrashCanStateManager extends PersistentState {
    private static final String NAME = "stardewvalley_trashcan";

    private int level = 0;
    /** playerUUID -> 垃圾桶暂存物品 */
    private final Map<UUID, ItemStack> trashStacks = new HashMap<>();

    private record SaveData(int level, Map<UUID, ItemStack> items) {}

    private static final Codec<Map<UUID, ItemStack>> ITEMS_CODEC = Codec.unboundedMap(
        Codec.STRING.xmap(UUID::fromString, UUID::toString), ItemStack.OPTIONAL_CODEC
    );
    private static final Codec<SaveData> SAVE_CODEC = RecordCodecBuilder.create(inst ->
        inst.group(
            Codec.INT.fieldOf("level").forGetter(d -> d.level),
            ITEMS_CODEC.optionalFieldOf("trashItems", Map.of()).forGetter(d -> d.items)
        ).apply(inst, SaveData::new)
    );
    public static final Codec<TrashCanStateManager> CODEC = SAVE_CODEC.xmap(
        d -> {
            TrashCanStateManager m = new TrashCanStateManager();
            m.level = d.level;
            m.trashStacks.putAll(d.items);
            return m;
        },
        m -> new SaveData(m.level, new HashMap<>(m.trashStacks))
    );
    public static final PersistentStateType<TrashCanStateManager> TYPE = new PersistentStateType<>(
        NAME, TrashCanStateManager::new, SafeCodec.wrap(CODEC, TrashCanStateManager::new), DataFixTypes.LEVEL
    );

    public static TrashCanStateManager get(ServerWorld world) {
        ServerWorld overworld = world.getServer().getOverworld();
        return overworld.getPersistentStateManager().getOrCreate(TYPE);
    }

    public int getLevel() { return level; }
    public void setLevel(int l) { level = l; setDirty(true); }

    public static double getRecycleRate(int level) {
        return switch (level) { case 1 -> 0.15; case 2 -> 0.30; case 3 -> 0.45; case 4 -> 0.60; default -> 0.0; };
    }

    // === 玩家暂存物品 ===

    public ItemStack getTrashStack(UUID playerUuid) {
        ItemStack stack = trashStacks.get(playerUuid);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public void setTrashStack(UUID playerUuid, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            trashStacks.remove(playerUuid);
        } else {
            trashStacks.put(playerUuid, stack.copy());
        }
        setDirty(true);
    }
}
