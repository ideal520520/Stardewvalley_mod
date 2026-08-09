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

public class SprinklerState extends PersistentState {
    private static final String NAME = "stardewvalley_sprinklers";
    private final Set<BlockPos> sprinklerPositions = new HashSet<>();

    private static final Codec<List<Long>> LIST_CODEC = Codec.LONG.listOf();

    public static final Codec<SprinklerState> CODEC = LIST_CODEC.xmap(
        list -> {
            SprinklerState state = new SprinklerState();
            for (long l : list) {
                state.sprinklerPositions.add(BlockPos.fromLong(l));
            }
            return state;
        },
        state -> state.sprinklerPositions.stream().map(BlockPos::asLong).toList()
    );

    public static final PersistentStateType<SprinklerState> TYPE = new PersistentStateType<>(
        NAME, SprinklerState::new, SafeCodec.wrap(CODEC, SprinklerState::new), DataFixTypes.LEVEL
    );

    public static SprinklerState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void addPosition(BlockPos pos) {
        sprinklerPositions.add(pos.toImmutable());
        setDirty(true);
    }

    public void removePosition(BlockPos pos) {
        sprinklerPositions.remove(pos);
        setDirty(true);
    }

    public Set<BlockPos> getPositions() {
        return sprinklerPositions;
    }
}
