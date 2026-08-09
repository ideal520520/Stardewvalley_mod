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

public class FarmlandState extends PersistentState {
    private static final String NAME = "stardewvalley_farmland";
    private final Set<BlockPos> farmlandPositions = new HashSet<>();

    private static final Codec<List<Long>> LIST_CODEC = Codec.LONG.listOf();

    public static final Codec<FarmlandState> CODEC = LIST_CODEC.xmap(
        list -> {
            FarmlandState state = new FarmlandState();
            for (long l : list) {
                state.farmlandPositions.add(BlockPos.fromLong(l));
            }
            return state;
        },
        state -> state.farmlandPositions.stream().map(BlockPos::asLong).toList()
    );

    public static final PersistentStateType<FarmlandState> TYPE = new PersistentStateType<>(
        NAME, FarmlandState::new, SafeCodec.wrap(CODEC, FarmlandState::new), DataFixTypes.LEVEL
    );

    public static FarmlandState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void add(BlockPos pos) {
        if (farmlandPositions.add(pos.toImmutable())) {
            setDirty(true);
        }
    }

    public void remove(BlockPos pos) {
        if (farmlandPositions.remove(pos)) {
            setDirty(true);
        }
    }

    public Set<BlockPos> getPositions() {
        return farmlandPositions;
    }
}
