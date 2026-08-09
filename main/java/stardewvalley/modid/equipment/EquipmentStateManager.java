package stardewvalley.modid.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.util.SafeCodec;

import java.util.*;

/**
 * 使用 PersistentState 在服务端持久化存储所有玩家的额外装备栏数据。
 */
public class EquipmentStateManager extends PersistentState {

    private static final String NAME = "stardewvalley_equipment";

    private final Map<UUID, EquipmentInventory> playerData = new HashMap<>();

    /** 用于 Codec 的中间数据记录 */
    private record SlotEntry(String id, int count) {}
    private record PlayerSlots(UUID uuid, List<SlotEntry> slots) {}
    private record AllData(List<PlayerSlots> players) {}

    private static final Codec<SlotEntry> SLOT_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(e -> e.id),
            Codec.INT.fieldOf("count").forGetter(e -> e.count)
        ).apply(instance, SlotEntry::new)
    );

    private static final Codec<PlayerSlots> PLAYER_SLOTS_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(e -> e.uuid),
            Codec.list(SLOT_ENTRY_CODEC).fieldOf("slots").forGetter(e -> e.slots)
        ).apply(instance, PlayerSlots::new)
    );

    private static final Codec<AllData> ALL_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(PLAYER_SLOTS_CODEC).fieldOf("players").forGetter(e -> e.players)
        ).apply(instance, AllData::new)
    );

    public static final Codec<EquipmentStateManager> CODEC = ALL_DATA_CODEC.xmap(
        data -> {
            EquipmentStateManager mgr = new EquipmentStateManager();
            for (PlayerSlots ps : data.players) {
                EquipmentInventory inv = new EquipmentInventory();
                for (int i = 0; i < Math.min(ps.slots.size(), EquipmentInventory.SLOT_COUNT); i++) {
                    SlotEntry se = ps.slots.get(i);
                    if (!se.id.isEmpty()) {
                        Identifier itemId = Identifier.of(se.id);
                        var item = Registries.ITEM.get(itemId);
                        if (item != null) {
                            inv.setSlot(i, new ItemStack(item, se.count));
                        }
                    }
                }
                mgr.playerData.put(ps.uuid, inv);
            }
            return mgr;
        },
        mgr -> {
            List<PlayerSlots> list = new ArrayList<>();
            for (var entry : mgr.playerData.entrySet()) {
                List<SlotEntry> slots = new ArrayList<>();
                for (int i = 0; i < EquipmentInventory.SLOT_COUNT; i++) {
                    ItemStack stack = entry.getValue().getSlot(i);
                    if (!stack.isEmpty()) {
                        slots.add(new SlotEntry(
                            Registries.ITEM.getId(stack.getItem()).toString(),
                            stack.getCount()
                        ));
                    } else {
                        slots.add(new SlotEntry("", 0));
                    }
                }
                list.add(new PlayerSlots(entry.getKey(), slots));
            }
            return new AllData(list);
        }
    );

    public static final PersistentStateType<EquipmentStateManager> TYPE = new PersistentStateType<>(
        NAME,
        EquipmentStateManager::new,
        SafeCodec.wrap(CODEC, EquipmentStateManager::new),
        DataFixTypes.LEVEL
    );

    public static EquipmentStateManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    /** 获取玩家装备栏数据，不存在则返回新的空实例 */
    public EquipmentInventory getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new EquipmentInventory());
    }

    /** 设置玩家装备栏数据 */
    public void setPlayerData(UUID uuid, EquipmentInventory inv) {
        playerData.put(uuid, inv);
        setDirty(true);
    }
}
