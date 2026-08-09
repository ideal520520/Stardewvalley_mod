package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;

import java.util.Map;
import java.util.Set;

public class TapperBlock extends HorizontalFacingBlock implements BlockEntityProvider {

    public static final BooleanProperty DONE = BooleanProperty.of("done");

    public static final Set<String> VALID_WOOD_IDS = Set.of(
        "oak_log", "dark_oak_log", "pale_oak_log", "cherry_log", "birch_log", "acacia_log", "spruce_log",
        "oak_wood", "dark_oak_wood", "pale_oak_wood", "cherry_wood", "birch_wood", "acacia_wood", "spruce_wood"
    );

    public static final Map<String, String> WOOD_TO_PRODUCT = Map.ofEntries(
        Map.entry("oak_log", "oak_resin"), Map.entry("dark_oak_log", "oak_resin"), Map.entry("pale_oak_log", "oak_resin"),
        Map.entry("oak_wood", "oak_resin"), Map.entry("dark_oak_wood", "oak_resin"), Map.entry("pale_oak_wood", "oak_resin"),
        Map.entry("cherry_log", "maple_syrup"), Map.entry("birch_log", "maple_syrup"),
        Map.entry("cherry_wood", "maple_syrup"), Map.entry("birch_wood", "maple_syrup"),
        Map.entry("acacia_log", "pine_tar"), Map.entry("spruce_log", "pine_tar"),
        Map.entry("acacia_wood", "pine_tar"), Map.entry("spruce_wood", "pine_tar")
    );

    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0, 0, 15, 16, 32, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(0, 0, 0, 16, 32, 1);
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(15, 0, 0, 16, 32, 16);
    private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0, 0, 0, 1, 32, 16);

    private final boolean isHeavy;

    public TapperBlock(Settings settings) {
        this(settings, false);
    }

    public TapperBlock(Settings settings, boolean isHeavy) {
        super(settings);
        this.isHeavy = isHeavy;
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(DONE, false));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return createCodec(TapperBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, DONE);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getOutlineShape(state, world, pos, context);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction side = ctx.getSide();
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();

        if (side.getAxis() == Direction.Axis.Y) return null;

        // 树液采集器贴着方块放置，检查贴着的方块和它上面的方块是否都是有效木头
        BlockPos attachedPos = pos.offset(side.getOpposite());
        BlockPos aboveAttached = attachedPos.up();
        String id1 = getBlockId(world.getBlockState(attachedPos));
        String id2 = getBlockId(world.getBlockState(aboveAttached));

        if (!VALID_WOOD_IDS.contains(id1) || !VALID_WOOD_IDS.contains(id2)) return null;

        return getDefaultState().with(FACING, side);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
        super.onBlockAdded(state, world, pos, oldState, moved);
        if (!world.isClient() && !oldState.isOf(state.getBlock()) && world instanceof ServerWorld serverWorld) {
            TapperState tapperState = TapperState.get(serverWorld);
            if (!tapperState.hasData(pos)) {
                tapperState.registerTapper(pos);
            }
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            TapperState.get(serverWorld).removeData(pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TapperBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        ServerWorld serverWorld = (ServerWorld) world;

        if (state.get(DONE)) {
            TapperState tapperState = TapperState.get(serverWorld);
            TapperState.TapperData data = tapperState.getData(pos);

            if (data != null && !data.outputItemId().isEmpty()) {
                ItemStack output = new ItemStack(
                    Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, data.outputItemId())),
                    data.outputCount()
                );
                if (!output.isEmpty()) {
                    if (!player.getInventory().insertStack(output)) {
                        player.dropItem(output, false);
                    }
                }
            }

            // 清除BlockEntity的物品数据（客户端停止渲染）
            TapperBlockEntity be = getBlockEntity(world, pos);
            if (be != null) {
                be.setOutputItemId("");
            }

            world.setBlockState(pos, state.with(DONE, false), 3);
            tapperState.registerTapper(pos);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, 1.0f);
            return ActionResult.SUCCESS;
        }

        // 仙尘：立即完成
        ItemStack held = player.getMainHandStack();
        if (held.getItem() == Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "fairy_dust"))) {
            if (!player.isCreative()) held.decrement(1);
            TapperState tapperState = TapperState.get(serverWorld);
            String woodId = TapperBlock.getBlockId(world.getBlockState(pos.offset(state.get(FACING).getOpposite())));
            int requiredDays = TapperBlock.getDaysForWood(woodId, isHeavy);
            String productId = TapperBlock.getProductForWood(woodId);
            tapperState.setData(pos, new TapperState.TapperData(requiredDays, productId, 1));
            if (world.getBlockEntity(pos) instanceof TapperBlockEntity be) {
                be.setOutputItemId("stardewvalley:" + productId);
            }
            world.setBlockState(pos, state.with(DONE, true), 3);
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                20, 0.5, 0.5, 0.5, 0.1);
            return ActionResult.SUCCESS;
        }

        player.sendMessage(Text.translatable("message.stardewvalley.tapper.processing"), true);
        return ActionResult.SUCCESS;
    }

    private static TapperBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof TapperBlockEntity ? (TapperBlockEntity) be : null;
    }

    public static String getBlockId(BlockState state) {
        return Registries.BLOCK.getId(state.getBlock()).getPath();
    }

    public static String getProductForWood(String woodId) {
        return WOOD_TO_PRODUCT.getOrDefault(woodId, "oak_resin");
    }

    public static int getDaysForWood(String woodId, boolean isHeavy) {
        if (isHeavy) {
            return woodId.endsWith("_log") ? 3 : 2;
        }
        return woodId.endsWith("_log") ? 6 : 5;
    }

    public boolean isHeavy() {
        return isHeavy;
    }
}
