package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import stardewvalley.modid.StardewValley;

public class TreeSeedBlock extends Block {

    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 5, 12);
    private final String seedItemId;
    private boolean creativeBreaking;
    private String lastPlayerName;

    public TreeSeedBlock(Settings settings, String seedItemId) {
        super(settings);
        this.seedItemId = seedItemId;
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
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return isSuitableGround(world.getBlockState(pos.down()));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.DOWN && !isSuitableGround(neighborState)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, net.minecraft.block.entity.BlockEntity blockEntity, ItemStack stack) {
        creativeBreaking = player.isCreative();
        lastPlayerName = player.getName().getString();
        super.afterBreak(world, player, pos, state, blockEntity, stack);
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean movedByPiston) {
        boolean wasCreative = creativeBreaking;
        creativeBreaking = false;
        lastPlayerName = null;

        if (!world.getBlockState(pos).isOf(this)) {
            var seed = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, seedItemId));
            if (seed != null && !wasCreative) {
                dropStack(world, pos, new ItemStack(seed));
            }
        }
        super.onStateReplaced(state, world, pos, movedByPiston);
    }

    private boolean isSuitableGround(BlockState state) {
        return state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT)
            || state.isOf(Blocks.COARSE_DIRT) || state.isOf(Blocks.ROOTED_DIRT)
            || state.isOf(Blocks.PODZOL) || state.isOf(Blocks.FARMLAND)
            || state.isOf(ModBlocks.MOD_FARMLAND);
    }
}
