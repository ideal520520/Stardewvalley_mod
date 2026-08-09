package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SprinklerBlock extends Block {

    private final int sprinklerType;
    // 1 = sprinkler (普通), 2 = quality_sprinkler (优质), 3 = iridium_sprinkler (铱)

    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 8, 16);

    public SprinklerBlock(Settings settings, int sprinklerType) {
        super(settings);
        this.sprinklerType = sprinklerType;
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return MapCodec.unit(this);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
        super.onBlockAdded(state, world, pos, oldState, moved);
        if (!world.isClient() && !oldState.isOf(state.getBlock()) && world instanceof ServerWorld serverWorld) {
            SprinklerState.get(serverWorld).addPosition(pos);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            SprinklerState.get(serverWorld).removePosition(pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    public static void waterCrops(ServerWorld world) {
        SprinklerState state = SprinklerState.get(world);

        for (BlockPos pos : state.getPositions()) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.getChunk(chunkX, chunkZ);
            }

            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            if (!(block instanceof SprinklerBlock sprinkler)) {
                continue;
            }

            int type = sprinkler.sprinklerType;
            java.util.Set<BlockPos> targets = getTargetPositions(pos, type);

            for (BlockPos target : targets) {
                waterFarmlandAt(world, target.down());
            }
        }
    }

    private static void waterFarmlandAt(ServerWorld world, BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            world.getChunk(chunkX, chunkZ);
        }
        BlockState targetState = world.getBlockState(pos);
        Block targetBlock = targetState.getBlock();
        if (targetBlock instanceof ModFarmlandBlock) {
            int currentRetain = targetState.get(ModFarmlandBlock.RETAIN_DAYS);
            world.setBlockState(pos, targetState
                .with(FarmlandBlock.MOISTURE, 7)
                .with(ModFarmlandBlock.RETAIN_DAYS, currentRetain > 0 ? currentRetain : 1), 3);
        } else if (targetBlock instanceof FarmlandBlock) {
            BlockState converted = ModFarmlandBlock.convertToModFarmland(world, pos, targetState);
            world.setBlockState(pos, converted
                .with(FarmlandBlock.MOISTURE, 7)
                .with(ModFarmlandBlock.RETAIN_DAYS, 1), 3);
        }
    }

    private static java.util.Set<BlockPos> getTargetPositions(BlockPos center, int type) {
        java.util.Set<BlockPos> targets = new java.util.HashSet<>();
        int y = center.getY();

        switch (type) {
            case 1: // sprinkler - +X, +Z, -X, -Z 各一格
                targets.add(new BlockPos(center.getX() + 1, y, center.getZ()));
                targets.add(new BlockPos(center.getX() - 1, y, center.getZ()));
                targets.add(new BlockPos(center.getX(), y, center.getZ() + 1));
                targets.add(new BlockPos(center.getX(), y, center.getZ() - 1));
                break;
            case 2: // quality_sprinkler - 周围8格
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        targets.add(new BlockPos(center.getX() + dx, y, center.getZ() + dz));
                    }
                }
                break;
            case 3: // iridium_sprinkler - 5x5正方形
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        targets.add(new BlockPos(center.getX() + dx, y, center.getZ() + dz));
                    }
                }
                break;
        }

        return targets;
    }
}
