package stardewvalley.modid.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import stardewvalley.modid.block.CropPositionState;
import stardewvalley.modid.block.ModFarmlandBlock;
import stardewvalley.modid.season.Season;

public class WildSeedItem extends Item {

    private final Block[] cropBlocks;
    private final Season[] seasons;

    public WildSeedItem(Settings settings, Block[] cropBlocks, Season[] seasons) {
        super(settings);
        this.cropBlocks = cropBlocks;
        this.seasons = seasons;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        boolean isFarmland = state.getBlock() instanceof ModFarmlandBlock;
        if (!isFarmland) return ActionResult.PASS;

        BlockPos cropPos = pos.up();
        if (!world.isAir(cropPos)) return ActionResult.PASS;

        if (!world.isClient()) {
            if (seasons.length > 0) {
                Season current = Season.fromTimeOfDay(world.getTimeOfDay());
                boolean validSeason = false;
                for (Season s : seasons) {
                    if (s == current) {
                        validSeason = true;
                        break;
                    }
                }
                if (!validSeason) {
                    return ActionResult.PASS;
                }
            }

            int index = world.random.nextInt(cropBlocks.length);
            Block selectedCrop = cropBlocks[index];
            world.setBlockState(cropPos, selectedCrop.getDefaultState(), 3);
            context.getStack().decrement(1);

            if (world instanceof ServerWorld serverWorld) {
                CropPositionState.get(serverWorld).setPlanter(cropPos, context.getPlayer().getUuid());
            }
        }
        return ActionResult.SUCCESS;
    }
}
