package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import stardewvalley.modid.StardewValley;

public class StardewGrassBlock extends Block {

    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 8, 16);

    public static final MapCodec<StardewGrassBlock> CODEC = createCodec(StardewGrassBlock::new);

    public StardewGrassBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
        super.onBlockAdded(state, world, pos, oldState, moved);
        if (!world.isClient() && world instanceof ServerWorld sw) {
            StardewGrassState.get(sw).addPos(pos);
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.down());
        return below.isSideSolidFullSquare(world, pos.down(), Direction.UP)
            || below.getBlock() instanceof FarmlandBlock
            || below.getBlock() instanceof ModFarmlandBlock;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.DOWN && !canPlaceAt(state, world, pos)) {
            return net.minecraft.block.Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld sw) {
            StardewGrassState grassState = StardewGrassState.get(sw);
            grassState.removePos(pos);
            grassState.decrementTotalCount();
            // 掉落采集品、纤维
            dropStardewItems(sw, pos, player);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack held = player.getMainHandStack();
        Identifier heldId = Registries.ITEM.getId(held.getItem());
        boolean isScythe = heldId.getNamespace().equals(StardewValley.MOD_ID) &&
            (heldId.getPath().equals("scythe") || heldId.getPath().equals("golden_scythe") || heldId.getPath().equals("iridium_scythe"));

        if (!isScythe) return ActionResult.PASS;

        // 客户端显式播放挥手动效
        if (world.isClient()) {
            player.swingHand(Hand.MAIN_HAND);
            return ActionResult.SUCCESS;
        }

        // 服务端逻辑
        ServerWorld sw = (ServerWorld) world;

        int hayCount = 1;
        if (heldId.getPath().equals("golden_scythe") && sw.random.nextFloat() < 0.5f) {
            hayCount = 2;
        } else if (heldId.getPath().equals("iridium_scythe") && sw.random.nextFloat() < 0.33f) {
            hayCount = 2;
        }

        // 干草掉落在地面，不直接进背包
        net.minecraft.item.Item hayItem = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(StardewValley.MOD_ID, "hay"));
        if (hayItem != null) {
            dropStack(sw, pos, new ItemStack(hayItem, hayCount));
        }

        sw.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
        // breakBlock 会播放破坏粒子和动画
        world.breakBlock(pos, false);
        return ActionResult.SUCCESS;
    }

    /**
     * 破坏草时掉落纤维（采集品由 ForagingLootModifier 处理，概率+2%）
     */
    private void dropStardewItems(ServerWorld world, BlockPos pos, PlayerEntity player) {
        // 检查玩家手持镰刀类型
        net.minecraft.item.ItemStack held = player.getMainHandStack();
        net.minecraft.util.Identifier heldId = net.minecraft.registry.Registries.ITEM.getId(held.getItem());
        boolean isScythe = heldId.getNamespace().equals(StardewValley.MOD_ID) && heldId.getPath().contains("scythe");
        boolean guaranteedFiber = isScythe && (heldId.getPath().equals("golden_scythe") || heldId.getPath().equals("iridium_scythe"));

        // 纤维掉落：镰刀左键时，普通75%，金/铱100%
        if (isScythe) {
            if (guaranteedFiber || world.random.nextFloat() < 0.75f) {
                net.minecraft.item.Item fiber = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(StardewValley.MOD_ID, "fiber"));
                if (fiber != null) {
                    dropStack(world, pos, new ItemStack(fiber));
                }
            }
        }
    }
}
