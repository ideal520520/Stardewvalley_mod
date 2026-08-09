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

public class CrabPotState extends PersistentState {
    private static final String NAME = "stardewvalley_crab_pots";
    private final Set<BlockPos> crabPotPositions = new HashSet<>();

    private static final Codec<List<Long>> LIST_CODEC = Codec.LONG.listOf();

    public static final Codec<CrabPotState> CODEC = LIST_CODEC.xmap(
        list -> {
            CrabPotState state = new CrabPotState();
            for (long l : list) {
                state.crabPotPositions.add(BlockPos.fromLong(l));
            }
            return state;
        },
        state -> state.crabPotPositions.stream().map(BlockPos::asLong).toList()
    );

    public static final PersistentStateType<CrabPotState> TYPE = new PersistentStateType<>(
        NAME, CrabPotState::new, SafeCodec.wrap(CODEC, CrabPotState::new), DataFixTypes.LEVEL
    );

    public static CrabPotState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void addPot(BlockPos pos) {
        crabPotPositions.add(pos.toImmutable());
        setDirty(true);
    }

    public void removePot(BlockPos pos) {
        crabPotPositions.remove(pos);
        setDirty(true);
    }

    public Set<BlockPos> getPotPositions() {
        return crabPotPositions;
    }
}
