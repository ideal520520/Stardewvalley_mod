package stardewvalley.modid.block;

import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Set;

public class DeluxeWormBinHandler {

    public static void registerWormBin(ServerWorld world, BlockPos pos) {
        DeluxeWormBinState.get(world).addBin(pos);
    }

    public static void removeWormBin(ServerWorld world, BlockPos pos) {
        DeluxeWormBinState.get(world).removeBin(pos);
    }

    public static void tickDeluxeWormBins(ServerWorld world, RegistryKey<net.minecraft.world.World> worldKey) {
        Set<BlockPos> positions = DeluxeWormBinState.get(world).getBinPositions();
        if (positions == null || positions.isEmpty()) return;

        Iterator<BlockPos> it = positions.iterator();

        while (it.hasNext()) {
            BlockPos pos = it.next();

            BlockState blockState = world.getBlockState(pos);
            if (!(blockState.getBlock() instanceof DeluxeWormBinBlock)) {
                it.remove();
                continue;
            }

            if (world.getBlockEntity(pos) instanceof DeluxeWormBinBlockEntity be) {
                if (!be.hasOutput()) {
                    be.setHasOutput(true);
                }
            }
        }
    }
}
