package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import stardewvalley.modid.gui.BookDataManager;
import stardewvalley.modid.gui.ModPayloads;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class CrabPotBlock extends Block implements BlockEntityProvider, Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 10, 15);

    public CrabPotBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return createCodec(CrabPotBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient() && world instanceof ServerWorld sw) {
            CrabPotHandler.registerCrabPot(sw, pos);
            if (placer != null) {
                CrabPotBlockEntity be = (CrabPotBlockEntity) world.getBlockEntity(pos);
                if (be != null) {
                    be.setPlacerUuid(placer.getUuid());
                }
            }
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld sw) {
            CrabPotHandler.removeCrabPot(sw, pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrabPotBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            // Client side - check if there's something to collect first
            CrabPotBlockEntity be = getBlockEntity(world, pos);
            if (be != null && be.getCaughtItemId().isEmpty()) {
                // Send open screen request to server
                ClientPlayNetworking.send(new ModPayloads.CrabPotOpenC2SPayload(pos));
            }
            return ActionResult.SUCCESS;
        }

        // Server side
        CrabPotBlockEntity be = getBlockEntity(world, pos);
        if (be == null) return ActionResult.PASS;

        // 仙尘：立即触发蟹笼产出（需要蟹笼有水并有鱼饵）
        if (be.getCaughtItemId().isEmpty()) {
            ItemStack held = player.getMainHandStack();
            if (held.getItem() == net.minecraft.registry.Registries.ITEM.get(
                    net.minecraft.util.Identifier.of("stardewvalley", "fairy_dust"))) {
                if (!player.isCreative()) held.decrement(1);
                CrabPotHandler.processSinglePot((ServerWorld) world, pos);
                ((ServerWorld) world).spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    20, 0.5, 0.5, 0.5, 0.1);
                return ActionResult.SUCCESS;
            }
        }

        // If there's a caught item, collect it
        if (!be.getCaughtItemId().isEmpty()) {
            String itemId = be.getCaughtItemId();
            int quality = be.getCaughtQuality();

            // Build the actual item ID with quality suffix if applicable
            String actualId = itemId;
            if (quality > 0) {
                String qualitySuffix = quality == 1 ? "_silver" : quality == 2 ? "_gold" : "_iridium";
                String qualityItemId = itemId + qualitySuffix;
                // Check if the quality variant exists
                if (Registries.ITEM.containsId(Identifier.of(qualityItemId))) {
                    actualId = qualityItemId;
                }
            }

            int count = be.getCaughtCount();
            // 捕蟹秘籍：收获者25%概率获得双倍物品
            ServerWorld serverWorld = (ServerWorld) world;
            boolean doubleCatch = BookDataManager.get(serverWorld).hasUsedBook(player.getUuid(), "the_art_o_crabbing")
                && world.random.nextFloat() < 0.25f;
            if (doubleCatch) count *= 2;

            ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.of(actualId)), count);
            if (!stack.isEmpty()) {
                if (!player.getInventory().insertStack(stack)) {
                    player.dropItem(stack, false);
                }
            }

            be.setCaughtItemId("");
            be.setCaughtQuality(0);
            be.setCaughtCount(1);
            be.markDirty();
            return ActionResult.SUCCESS;
        }

        return ActionResult.SUCCESS;
    }

    private static CrabPotBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof CrabPotBlockEntity ? (CrabPotBlockEntity) be : null;
    }
}
