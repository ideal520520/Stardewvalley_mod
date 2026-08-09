package stardewvalley.modid.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.block.BaseCropBlock;
import stardewvalley.modid.season.Season;

public class MixedFlowerSeedItem extends Item {

    private static final String[][] SEASONAL_FLOWERS = {
        {"tulip", "bluejazz"},           // SPRING
        {"sunflower", "summerspangle", "poppy"},   // SUMMER
        {"sunflower", "fairyrose"}       // FALL
    };

    public MixedFlowerSeedItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof stardewvalley.modid.block.ModFarmlandBlock) {
            BlockPos cropPos = pos.up();
            if (world.isAir(cropPos)) {
                if (!world.isClient()) {
                    Season current = Season.fromTimeOfDay(world.getTimeOfDay());
                    String[] flowers;
                    switch (current) {
                        case SPRING -> flowers = SEASONAL_FLOWERS[0];
                        case SUMMER -> flowers = SEASONAL_FLOWERS[1];
                        case FALL -> flowers = SEASONAL_FLOWERS[2];
                        default -> flowers = SEASONAL_FLOWERS[0];
                    }
                    String cropId = flowers[world.random.nextInt(flowers.length)];
                    BaseCropBlock cropBlock = getCropBlock(cropId);
                    if (cropBlock != null) {
                        world.setBlockState(cropPos, cropBlock.getDefaultState(), 3);
                        context.getStack().decrement(1);
                    }
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    private BaseCropBlock getCropBlock(String cropId) {
        var block = Registries.BLOCK.get(Identifier.of(StardewValley.MOD_ID, cropId + "_crop"));
        if (block instanceof BaseCropBlock crop) return crop;
        return null;
    }
}