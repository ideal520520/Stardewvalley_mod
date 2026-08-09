package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeluxeWormBinBlock extends Block implements BlockEntityProvider {
    public DeluxeWormBinBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return createCodec(DeluxeWormBinBlock::new);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DeluxeWormBinBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            DeluxeWormBinHandler.registerWormBin(serverWorld, pos);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            DeluxeWormBinHandler.removeWormBin(serverWorld, pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;

        if (world.getBlockEntity(pos) instanceof DeluxeWormBinBlockEntity be) {
            if (be.hasOutput()) {
                ItemStack baitStack = new ItemStack(Registries.ITEM.get(Identifier.of("stardewvalley", "bait_deluxe_bait")), 5);
                if (!baitStack.isEmpty()) {
                    if (!player.getInventory().insertStack(baitStack)) {
                        player.dropItem(baitStack, false);
                    }
                }
                be.setHasOutput(false);
                return ActionResult.SUCCESS;
            }
            // 仙尘：立即产出
            ItemStack held = player.getMainHandStack();
            if (held.getItem() == net.minecraft.registry.Registries.ITEM.get(
                    net.minecraft.util.Identifier.of("stardewvalley", "fairy_dust"))) {
                if (!player.isCreative()) held.decrement(1);
                be.setHasOutput(true);
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    20, 0.5, 0.5, 0.5, 0.1);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
