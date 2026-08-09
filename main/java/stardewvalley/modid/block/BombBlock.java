package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class BombBlock extends FacingBlock {

    public static final IntProperty STATE = IntProperty.of("state", 0, 1);

    // Track blast tick (absolute world time) for each bomb, reset when block is removed
    private static final ConcurrentHashMap<BlockPos, Long> BLAST_TIME = new ConcurrentHashMap<>();

    // Track placer UUID for each bomb (used by explosion drops for luck calculation)
    private static final ConcurrentHashMap<BlockPos, UUID> PLACER_MAP = new ConcurrentHashMap<>();

    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0, 0, 0, 16, 16, 1);
    private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(0, 0, 15, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(0, 0, 0, 1, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(15, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_DOWN = Block.createCuboidShape(0, 0, 0, 16, 1, 16);
    private static final VoxelShape SHAPE_UP = Block.createCuboidShape(0, 15, 0, 16, 16, 16);

    public static final int FUSE_TICKS = 100; // 5 seconds

    private static final AtomicInteger ACTIVE_EXPLOSIONS = new AtomicInteger(0);

    private final float explosionPower;

    public static boolean hasActiveExplosion() {
        return ACTIVE_EXPLOSIONS.get() > 0;
    }

    public BombBlock(Settings settings, float explosionPower) {
        super(settings);
        this.explosionPower = explosionPower;
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(STATE, 0));
    }

    @Override
    protected MapCodec<? extends FacingBlock> getCodec() {
        return createCodec(s -> new BombBlock(s, 4.0f));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getShapeForFacing(state.get(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getShapeForFacing(state.get(FACING));
    }

    private static VoxelShape getShapeForFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> SHAPE_SOUTH;
            case SOUTH -> SHAPE_NORTH;
            case WEST -> SHAPE_EAST;
            case EAST -> SHAPE_WEST;
            case DOWN -> SHAPE_UP;
            case UP -> SHAPE_DOWN;
        };
    }

    public static void startFuse(ServerWorld world, BlockPos pos) {
        long blastTime = world.getTime() + FUSE_TICKS;
        BLAST_TIME.put(pos, blastTime);
        world.scheduleBlockTick(pos, world.getBlockState(pos).getBlock(), 10);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, @Nullable ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient()) {
            startFuse((ServerWorld) world, pos);
            if (placer != null) {
                PLACER_MAP.put(pos, placer.getUuid());
            }
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (!BLAST_TIME.containsKey(pos)) {
            world.removeBlock(pos, false);
            return;
        }

        long blastTime = BLAST_TIME.get(pos);
        long currentTime = world.getTime();

        if (currentTime >= blastTime) {
            BLAST_TIME.remove(pos);
            Vec3d center = state.getBlock() instanceof BombBlock
                ? getExplosionCenter(pos, state.get(FACING))
                : new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            ACTIVE_EXPLOSIONS.incrementAndGet();
            world.createExplosion(null, center.x, center.y, center.z, explosionPower, World.ExplosionSourceType.TNT);
            ACTIVE_EXPLOSIONS.decrementAndGet();

            world.removeBlock(pos, false);
            return;
        }

        // Toggle visual state only if block still exists
        if (state.getBlock() instanceof BombBlock) {
            int currentState = state.get(STATE);
            world.setBlockState(pos, state.with(STATE, currentState == 0 ? 1 : 0), 3);
        }

        long remaining = blastTime - currentTime;
        int nextDelay = (int) Math.min(remaining, 10);
        world.scheduleBlockTick(pos, this, Math.max(nextDelay, 1));
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        BLAST_TIME.remove(pos);
        PLACER_MAP.remove(pos);
        super.onStateReplaced(state, world, pos, moved);
    }

    @Override
    public void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (!BLAST_TIME.containsKey(pos)) return;
        long remaining = BLAST_TIME.get(pos) - world.getTime();
        if (remaining > 5) {
            BLAST_TIME.put(pos, world.getTime() + 5);
            world.scheduleBlockTick(pos, this, 5);
        }
    }

    private static Vec3d getExplosionCenter(BlockPos pos, Direction facing) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        return switch (facing) {
            case NORTH -> new Vec3d(cx, cy, pos.getZ());
            case SOUTH -> new Vec3d(cx, cy, pos.getZ() + 1);
            case WEST -> new Vec3d(pos.getX(), cy, cz);
            case EAST -> new Vec3d(pos.getX() + 1, cy, cz);
            case DOWN -> new Vec3d(cx, pos.getY(), cz);
            case UP -> new Vec3d(cx, pos.getY() + 1, cz);
        };
    }

    public static UUID getPlacer(BlockPos pos) {
        return PLACER_MAP.get(pos);
    }

    public float getExplosionPower() {
        return explosionPower;
    }
}
