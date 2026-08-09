package stardewvalley.modid.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.util.SafeCodec;

import java.util.*;

public class CropPositionState extends PersistentState {

    private static final String NAME = "stardewvalley_crop_positions";

    private final Set<BlockPos> cropPositions = new HashSet<>();
    private final Set<BlockPos> farmlandPositions = new HashSet<>();
    // 作物方块位置 → 种植者UUID
    private final Map<BlockPos, UUID> cropPlanters = new HashMap<>();

    public Set<BlockPos> getCropPositions() {
        return cropPositions;
    }

    public Set<BlockPos> getFarmlandPositions() {
        return farmlandPositions;
    }

    public void addCropPosition(BlockPos pos) {
        if (cropPositions.add(pos.toImmutable())) {
            setDirty(true);
        }
    }

    public void removeCropPosition(BlockPos pos) {
        BlockPos immutable = pos.toImmutable();
        if (cropPositions.remove(immutable)) {
            setDirty(true);
        }
        if (cropPlanters.remove(immutable) != null) {
            setDirty(true);
        }
    }

    public void addFarmlandPosition(BlockPos pos) {
        if (farmlandPositions.add(pos.toImmutable())) {
            setDirty(true);
        }
    }

    public void removeFarmlandPosition(BlockPos pos) {
        if (farmlandPositions.remove(pos)) {
            setDirty(true);
        }
    }

    /** 保存种植者信息 */
    public void setPlanter(BlockPos cropPos, UUID planterUuid) {
        cropPlanters.put(cropPos.toImmutable(), planterUuid);
        setDirty(true);
    }

    /** 获取种植者UUID */
    public UUID getPlanter(BlockPos cropPos) {
        return cropPlanters.get(cropPos);
    }

    private record Data(Set<Long> crops, Set<Long> farmlands, Map<Long, UUID> planters) {}

    private static final Codec<Set<Long>> SET_LONG_CODEC = Codec.LONG.listOf().xmap(
        HashSet::new, ArrayList::new
    );

    private static final Codec<Map<Long, UUID>> PLANTERS_CODEC = Codec.unboundedMap(
        Codec.LONG, Uuids.CODEC
    );

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            SET_LONG_CODEC.fieldOf("crops").forGetter(Data::crops),
            SET_LONG_CODEC.fieldOf("farmlands").forGetter(Data::farmlands),
            PLANTERS_CODEC.optionalFieldOf("planters", Collections.emptyMap()).forGetter(Data::planters)
        ).apply(instance, Data::new)
    );

    public static final Codec<CropPositionState> CODEC = DATA_CODEC.xmap(
        data -> {
            CropPositionState state = new CropPositionState();
            for (long l : data.crops()) state.cropPositions.add(BlockPos.fromLong(l));
            for (long l : data.farmlands()) state.farmlandPositions.add(BlockPos.fromLong(l));
            for (var e : data.planters().entrySet()) {
                state.cropPlanters.put(BlockPos.fromLong(e.getKey()), e.getValue());
            }
            return state;
        },
        state -> {
            Map<Long, UUID> planters = new HashMap<>();
            for (var e : state.cropPlanters.entrySet()) {
                planters.put(e.getKey().asLong(), e.getValue());
            }
            return new Data(
                state.cropPositions.stream().map(BlockPos::asLong).collect(java.util.stream.Collectors.toSet()),
                state.farmlandPositions.stream().map(BlockPos::asLong).collect(java.util.stream.Collectors.toSet()),
                planters
            );
        }
    );

    public static final PersistentStateType<CropPositionState> TYPE = new PersistentStateType<>(
        NAME,
        CropPositionState::new,
        SafeCodec.wrap(CODEC, CropPositionState::new),
        DataFixTypes.LEVEL
    );

    public static CropPositionState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }
}
