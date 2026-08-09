package stardewvalley.modid.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.util.SafeCodec;

import java.util.*;

public class BackpackStateManager extends PersistentState {
    private static final String NAME = "stardewvalley_backpack";

    private final Map<UUID, BackpackData> playerData = new HashMap<>();

    public static class BackpackData {
        public int level; // 0=初始2行, 1=已购买backpack(3行), 2=已购买36_backpack(4行)
        public final ItemStack[] slots = new ItemStack[36];

        public BackpackData() {
            for (int i = 0; i < 36; i++) slots[i] = ItemStack.EMPTY;
        }
    }

    private record SlotEntry(String id, int count) {}
    private record PlayerBackpack(UUID uuid, int level, List<SlotEntry> slots) {}
    private record AllData(List<PlayerBackpack> players) {}

    private static final Codec<SlotEntry> SLOT_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(e -> e.id),
            Codec.INT.fieldOf("count").forGetter(e -> e.count)
        ).apply(instance, SlotEntry::new)
    );

    private static final Codec<PlayerBackpack> PLAYER_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(e -> e.uuid),
            Codec.INT.fieldOf("level").forGetter(e -> e.level),
            Codec.list(SLOT_ENTRY_CODEC).fieldOf("slots").forGetter(e -> e.slots)
        ).apply(instance, PlayerBackpack::new)
    );

    private static final Codec<AllData> ALL_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(PLAYER_CODEC).fieldOf("players").forGetter(e -> e.players)
        ).apply(instance, AllData::new)
    );

    public static final Codec<BackpackStateManager> CODEC = ALL_DATA_CODEC.xmap(
        data -> {
            BackpackStateManager mgr = new BackpackStateManager();
            for (PlayerBackpack pb : data.players) {
                BackpackData bd = new BackpackData();
                bd.level = pb.level;
                for (int i = 0; i < Math.min(pb.slots.size(), 36); i++) {
                    SlotEntry se = pb.slots.get(i);
                    if (!se.id.isEmpty()) {
                        Identifier itemId = Identifier.of(se.id);
                        var item = Registries.ITEM.get(itemId);
                        if (item != null) {
                            bd.slots[i] = new ItemStack(item, se.count);
                        }
                    }
                }
                mgr.playerData.put(pb.uuid, bd);
            }
            return mgr;
        },
        mgr -> {
            List<PlayerBackpack> list = new ArrayList<>();
            for (var entry : mgr.playerData.entrySet()) {
                BackpackData bd = entry.getValue();
                List<SlotEntry> slots = new ArrayList<>();
                for (int i = 0; i < 36; i++) {
                    ItemStack stack = bd.slots[i];
                    if (!stack.isEmpty()) {
                        slots.add(new SlotEntry(
                            Registries.ITEM.getId(stack.getItem()).toString(),
                            stack.getCount()
                        ));
                    } else {
                        slots.add(new SlotEntry("", 0));
                    }
                }
                list.add(new PlayerBackpack(entry.getKey(), bd.level, slots));
            }
            return new AllData(list);
        }
    );

    public static final PersistentStateType<BackpackStateManager> TYPE = new PersistentStateType<>(
        NAME,
        BackpackStateManager::new,
        SafeCodec.wrap(CODEC, BackpackStateManager::new),
        DataFixTypes.LEVEL
    );

    public static BackpackStateManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public BackpackData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new BackpackData());
    }

    public void setPlayerLevel(UUID uuid, int level) {
        BackpackData data = getPlayerData(uuid);
        data.level = level;
        setDirty(true);
    }

    public int getPlayerLevel(UUID uuid) {
        return getPlayerData(uuid).level;
    }

    public void setPlayerSlot(UUID uuid, int slotIndex, ItemStack stack) {
        BackpackData data = getPlayerData(uuid);
        if (slotIndex < 0 || slotIndex >= 36) return;
        data.slots[slotIndex] = stack == null ? ItemStack.EMPTY : stack;
        setDirty(true);
    }

    public ItemStack getPlayerSlot(UUID uuid, int slotIndex) {
        BackpackData data = getPlayerData(uuid);
        if (slotIndex < 0 || slotIndex >= 36) return ItemStack.EMPTY;
        return data.slots[slotIndex];
    }

    public ItemStack[] getPlayerSlots(UUID uuid) {
        BackpackData data = getPlayerData(uuid);
        ItemStack[] copy = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            copy[i] = data.slots[i].copy();
        }
        return copy;
    }

    public void setPlayerSlots(UUID uuid, ItemStack[] slots) {
        BackpackData data = getPlayerData(uuid);
        for (int i = 0; i < 36 && i < slots.length; i++) {
            data.slots[i] = slots[i] == null ? ItemStack.EMPTY : slots[i];
        }
        setDirty(true);
    }
}
