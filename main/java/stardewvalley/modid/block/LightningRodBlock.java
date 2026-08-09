package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;

public class LightningRodBlock extends Block implements BlockEntityProvider {

    public static final BooleanProperty CHARGED = BooleanProperty.of("charged");
    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 28, 12);

    public LightningRodBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(CHARGED, false));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return createCodec(LightningRodBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHARGED);
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
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LightningRodBlockEntity(pos, state);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
        super.onBlockAdded(state, world, pos, oldState, moved);
        if (!world.isClient() && !oldState.isOf(state.getBlock()) && world instanceof ServerWorld serverWorld) {
            LightningRodState.get(serverWorld).addRod(pos);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient() && world instanceof ServerWorld sw) {
            LightningRodState.get(sw).addRod(pos);
            if (placer != null) {
                LightningRodBlockEntity be = (LightningRodBlockEntity) world.getBlockEntity(pos);
                if (be != null) {
                    be.setPlacerUuid(placer.getUuid());
                }
            }
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, net.minecraft.entity.player.PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            LightningRodState.get(serverWorld).removeRod(pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        if (state.get(CHARGED)) {
            net.minecraft.item.Item batteryItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "battery_pack"));
            if (batteryItem != null) {
                ItemStack battery = new ItemStack(batteryItem, 1);
                if (!player.getInventory().insertStack(battery)) {
                    player.dropItem(battery, false);
                }
                LightningRodBlockEntity be = getBlockEntity(world, pos);
                if (be != null) {
                    be.setChargedItem("");
                }
                world.setBlockState(pos, state.with(CHARGED, false), 3);
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, 1.0f);
            }
            return ActionResult.SUCCESS;
        }

        player.sendMessage(Text.translatable("message.stardewvalley.lightning_rod.empty"), true);
        return ActionResult.SUCCESS;
    }

    public static void charge(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(CHARGED, true), 3);
        LightningRodBlockEntity be = (LightningRodBlockEntity) world.getBlockEntity(pos);
        if (be != null) {
            be.setChargedItem("stardewvalley:battery_pack");
        }
    }

    private static LightningRodBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof LightningRodBlockEntity ? (LightningRodBlockEntity) be : null;
    }
}
