package stardewvalley.modid.item;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stardewvalley.modid.block.ModBlocks;
import stardewvalley.modid.block.StardewGrassState;

public class GrassStarterItem extends Item {

    public GrassStarterItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos targetPos = context.getBlockPos();
        Block targetBlock = world.getBlockState(targetPos).getBlock();

        if (targetBlock != Blocks.GRASS_BLOCK && targetBlock != Blocks.DIRT) {
            return ActionResult.PASS;
        }

        BlockPos placePos = targetPos.up();
        if (!world.isAir(placePos)) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            world.setBlockState(placePos, ModBlocks.STARDEW_GRASS.getDefaultState(), 3);
            if (world instanceof ServerWorld sw) {
                StardewGrassState.get(sw).incrementTotalCount();
            }
            ItemStack stack = context.getStack();
            if (!context.getPlayer().isCreative()) {
                stack.decrement(1);
            }
        }

        return ActionResult.SUCCESS;
    }
}
