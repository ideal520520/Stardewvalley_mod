package stardewvalley.modid.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.util.SafeCodec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LightningRodState extends PersistentState {
    private static final String NAME = "stardewvalley_lightning_rods";
    private final Set<BlockPos> rodPositions = new HashSet<>();

    private static final Codec<List<Long>> LIST_CODEC = Codec.LONG.listOf();

    public static final Codec<LightningRodState> CODEC = LIST_CODEC.xmap(
        list -> {
            LightningRodState state = new LightningRodState();
            for (long l : list) {
                state.rodPositions.add(BlockPos.fromLong(l));
            }
            return state;
        },
        state -> state.rodPositions.stream().map(BlockPos::asLong).toList()
    );

    public static final PersistentStateType<LightningRodState> TYPE = new PersistentStateType<>(
        NAME, LightningRodState::new, SafeCodec.wrap(CODEC, LightningRodState::new), DataFixTypes.LEVEL
    );

    public static LightningRodState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void addRod(BlockPos pos) {
        rodPositions.add(pos.toImmutable());
        setDirty(true);
    }

    public void removeRod(BlockPos pos) {
        rodPositions.remove(pos);
        setDirty(true);
    }

    public Set<BlockPos> getRodPositions() {
        return rodPositions;
    }
}
