package stardewvalley.modid.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.util.SafeCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WarpPositionState extends PersistentState {

    private static final String NAME = "stardewvalley_warp_positions";
    private final Map<UUID, Map<String, WarpData>> playerWarps = new HashMap<>();
    // 追踪每个玩家每种图腾展示方块的位置，用于设置新位置时清除旧的
    private final Map<UUID, Map<String, Long>> totemDisplayPositions = new HashMap<>();

    public record WarpData(double x, double y, double z) {}

    private record Entry(String uuid, String totemType, double x, double y, double z) {}
    private record DisplayEntry(String uuid, String totemType, long pos) {}

    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("uuid").forGetter(Entry::uuid),
            Codec.STRING.fieldOf("totemType").forGetter(Entry::totemType),
            Codec.DOUBLE.fieldOf("x").forGetter(Entry::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Entry::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Entry::z)
        ).apply(instance, Entry::new)
    );

    private static final Codec<DisplayEntry> DISPLAY_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("uuid").forGetter(DisplayEntry::uuid),
            Codec.STRING.fieldOf("totemType").forGetter(DisplayEntry::totemType),
            Codec.LONG.fieldOf("pos").forGetter(DisplayEntry::pos)
        ).apply(instance, DisplayEntry::new)
    );

    private record AllData(List<Entry> warps, List<DisplayEntry> displays) {}
    private static final Codec<AllData> ALL_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ENTRY_CODEC.listOf().fieldOf("warps").forGetter(AllData::warps),
            DISPLAY_ENTRY_CODEC.listOf().fieldOf("displays").forGetter(AllData::displays)
        ).apply(instance, AllData::new)
    );

    public static final Codec<WarpPositionState> CODEC = ALL_DATA_CODEC.xmap(
        data -> {
            WarpPositionState state = new WarpPositionState();
            for (Entry e : data.warps()) {
                state.playerWarps
                    .computeIfAbsent(UUID.fromString(e.uuid()), k -> new HashMap<>())
                    .put(e.totemType(), new WarpData(e.x(), e.y(), e.z()));
            }
            for (DisplayEntry d : data.displays()) {
                state.totemDisplayPositions
                    .computeIfAbsent(UUID.fromString(d.uuid()), k -> new HashMap<>())
                    .put(d.totemType(), d.pos());
            }
            return state;
        },
        state -> {
            List<Entry> warps = state.playerWarps.entrySet().stream()
                .flatMap(uuidEntry -> uuidEntry.getValue().entrySet().stream()
                    .map(warpEntry -> new Entry(
                        uuidEntry.getKey().toString(),
                        warpEntry.getKey(),
                        warpEntry.getValue().x(),
                        warpEntry.getValue().y(),
                        warpEntry.getValue().z())))
                .toList();
            List<DisplayEntry> displays = state.totemDisplayPositions.entrySet().stream()
                .flatMap(uuidEntry -> uuidEntry.getValue().entrySet().stream()
                    .map(displayEntry -> new DisplayEntry(
                        uuidEntry.getKey().toString(),
                        displayEntry.getKey(),
                        displayEntry.getValue())))
                .toList();
            return new AllData(warps, displays);
        }
    );

    public static final PersistentStateType<WarpPositionState> TYPE = new PersistentStateType<>(
        NAME,
        WarpPositionState::new,
        SafeCodec.wrap(CODEC, WarpPositionState::new),
        DataFixTypes.LEVEL
    );

    public static WarpPositionState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void setPosition(UUID playerUuid, String totemType, double x, double y, double z) {
        playerWarps.computeIfAbsent(playerUuid, k -> new HashMap<>())
            .put(totemType, new WarpData(x, y, z));
        setDirty(true);
    }

    public WarpData getPosition(UUID playerUuid, String totemType) {
        Map<String, WarpData> warps = playerWarps.get(playerUuid);
        if (warps == null) return null;
        return warps.get(totemType);
    }

    /** 获取该玩家该图腾旧展示方块的位置，并更新为新位置 */
    public Long getAndSetDisplayPos(UUID playerUuid, String totemType, long newPos) {
        Map<String, Long> displays = totemDisplayPositions.computeIfAbsent(playerUuid, k -> new HashMap<>());
        Long old = displays.get(totemType);
        displays.put(totemType, newPos);
        setDirty(true);
        return old;
    }
}
