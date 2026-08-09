package stardewvalley.modid.block;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeluxeWormBinState extends PersistentState {
    private static final String NAME = "stardewvalley_deluxe_worm_bins";
    private final Set<BlockPos> wormBinPositions = new HashSet<>();

    private static final Codec<List<Long>> LIST_CODEC = Codec.LONG.listOf();

    public static final Codec<DeluxeWormBinState> CODEC = LIST_CODEC.xmap(
        list -> {
            DeluxeWormBinState state = new DeluxeWormBinState();
            for (long l : list) {
                state.wormBinPositions.add(BlockPos.fromLong(l));
            }
            return state;
        },
        state -> state.wormBinPositions.stream().map(BlockPos::asLong).toList()
    );

    public static final PersistentStateType<DeluxeWormBinState> TYPE = new PersistentStateType<>(
        NAME, DeluxeWormBinState::new, SafeCodec.wrap(CODEC, DeluxeWormBinState::new), DataFixTypes.LEVEL
    );

    public static DeluxeWormBinState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void addBin(BlockPos pos) {
        wormBinPositions.add(pos.toImmutable());
        setDirty(true);
    }

    public void removeBin(BlockPos pos) {
        wormBinPositions.remove(pos);
        setDirty(true);
    }

    public Set<BlockPos> getBinPositions() {
        return wormBinPositions;
    }
}
