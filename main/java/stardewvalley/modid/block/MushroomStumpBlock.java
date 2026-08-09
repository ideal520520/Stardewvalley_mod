package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.season.ForagingLevelManager;

public class MushroomStumpBlock extends Block implements BlockEntityProvider {

    public static final BooleanProperty DONE = BooleanProperty.of("done");

    public MushroomStumpBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(DONE, false));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return createCodec(MushroomStumpBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DONE);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MushroomStumpBlockEntity(pos, state);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            MushroomStumpState.get(serverWorld).registerStump(pos);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            MushroomStumpState.get(serverWorld).removeData(pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;

        if (state.get(DONE)) {
            MushroomStumpState.StumpData data = MushroomStumpState.get(serverWorld).getData(pos);
            if (data != null && !data.outputItemId().isEmpty()) {
                ItemStack mushroomStack = new ItemStack(
                    Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, data.outputItemId())),
                    data.outputCount()
                );
                if (!mushroomStack.isEmpty()) {
                    if (!player.getInventory().insertStack(mushroomStack)) {
                        player.dropItem(mushroomStack, false);
                    }
                    if (player.getUuid() != null) {
                        ForagingLevelManager.get(serverWorld).addXp(player.getUuid(), data.outputCount() * 5);
                    }
                }
                MushroomStumpState.get(serverWorld).setData(pos, new MushroomStumpState.StumpData(0, "", 0));
                if (world.getBlockEntity(pos) instanceof MushroomStumpBlockEntity be) {
                    be.setOutputItemId("");
                }
                world.setBlockState(pos, state.with(DONE, false), 3);
                return ActionResult.SUCCESS;
            }
        } else {
            // EMPTY 状态：手持仙尘时跳过等待
            ItemStack held = player.getMainHandStack();
            if (held.getItem() == net.minecraft.registry.Registries.ITEM.get(
                    net.minecraft.util.Identifier.of(StardewValley.MOD_ID, "fairy_dust"))) {
                if (!player.isCreative()) held.decrement(1);
                MushroomStumpState.get(serverWorld).applyFairyDust(serverWorld, pos);
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    20, 0.5, 0.5, 0.5, 0.1);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
