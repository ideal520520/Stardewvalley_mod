package stardewvalley.modid.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import stardewvalley.modid.season.Season;

public class ModFarmlandBlock extends FarmlandBlock {

    public static final IntProperty FERTILIZER_TIER = IntProperty.of("fertilizer_tier", 0, 3);
    public static final IntProperty RETAIN_DAYS = IntProperty.of("retain_days", 0, 28);
    public static final IntProperty GROWTH_BOOST = IntProperty.of("growth_boost", 0, 3);
    public static final IntProperty SEASON_APPLIED = IntProperty.of("season_applied", 0, 3);

    public ModFarmlandBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
            .with(MOISTURE, 0)
            .with(FERTILIZER_TIER, 0)
            .with(RETAIN_DAYS, 0)
            .with(GROWTH_BOOST, 0)
            .with(SEASON_APPLIED, 0));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE, FERTILIZER_TIER, RETAIN_DAYS, GROWTH_BOOST, SEASON_APPLIED);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
        if (!world.isClient() && !state.isOf(oldState.getBlock())) {
            BlockPos immutable = pos.toImmutable();
            if (world instanceof ServerWorld serverWorld) {
                FarmlandState.get(serverWorld).add(immutable);
                CropPositionState.get(serverWorld).addFarmlandPosition(immutable);
            }
        }
    }

    public void onNewDay(ServerWorld world, BlockPos pos, BlockState state) {
        Season currentSeason = Season.getCachedSeason();
        int storedSeason = state.get(SEASON_APPLIED);

        int fertilizerTier = state.get(FERTILIZER_TIER);
        int growthBoost = state.get(GROWTH_BOOST);
        int retainDays = state.get(RETAIN_DAYS);

        if (fertilizerTier > 0 || growthBoost > 0 || retainDays > 0) {
            if (currentSeason.ordinal() != storedSeason) {
                world.setBlockState(pos, state
                    .with(FERTILIZER_TIER, 0)
                    .with(GROWTH_BOOST, 0)
                    .with(RETAIN_DAYS, 0)
                    .with(SEASON_APPLIED, currentSeason.ordinal())
                    .with(MOISTURE, 0), 2);
                return;
            }
        }

        if (retainDays > 0) {
            if (retainDays == 1) {
                world.setBlockState(pos, state.with(RETAIN_DAYS, 0).with(MOISTURE, 0), 2);
            } else {
                world.setBlockState(pos, state.with(RETAIN_DAYS, retainDays - 1).with(MOISTURE, 7), 2);
            }
        }
    }

    public void onTime5(ServerWorld world, BlockPos pos, BlockState state) {
        if (state.get(MOISTURE) == 0) {
            BlockState above = world.getBlockState(pos.up());
            if (!(above.getBlock() instanceof BaseCropBlock)) {
                if (world.random.nextFloat() < 1.0f / 3.0f) {
                    world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 3);
                }
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean movedByPiston) {
        FarmlandState.get(world).remove(pos);
        CropPositionState.get(world).removeFarmlandPosition(pos);
        super.onStateReplaced(state, world, pos, movedByPiston);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, net.minecraft.entity.Entity entity, double fallDistance) {
    }

    /** 是否允许种植任意季节的作物（花盆用） */
    public boolean allowsAllSeasons() {
        return false;
    }

    public static BlockState convertToModFarmland(World world, BlockPos pos, BlockState vanillaFarmland) {
        int moisture = vanillaFarmland.get(MOISTURE);
        return ModBlocks.MOD_FARMLAND.getDefaultState().with(MOISTURE, moisture);
    }
}
