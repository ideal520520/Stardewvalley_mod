package stardewvalley.modid.block;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface GrowableCrop {
    void tryGrow(ServerWorld world, BlockPos pos, BlockState state);
}
