package stardewvalley.modid.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stardewvalley.modid.block.ModBlocks;

public class TreeSeedItem extends Item {

    private final Block plantedBlock;

    public TreeSeedItem(Settings settings, Block plantedBlock) {
        super(settings);
        this.plantedBlock = plantedBlock;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        // 只能种植在草方块、泥土等方块上
        if (isSuitableGround(state)) {
            BlockPos placePos = pos.up();
            if (world.isAir(placePos)) {
                if (!world.isClient()) {
                    world.setBlockState(placePos, plantedBlock.getDefaultState(), 3);
                    context.getStack().decrement(1);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    private boolean isSuitableGround(BlockState state) {
        return state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT)
            || state.isOf(Blocks.COARSE_DIRT) || state.isOf(Blocks.ROOTED_DIRT)
            || state.isOf(Blocks.PODZOL) || state.isOf(Blocks.FARMLAND)
            || state.isOf(ModBlocks.MOD_FARMLAND);
    }
}
