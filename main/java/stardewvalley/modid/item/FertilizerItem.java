package stardewvalley.modid.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stardewvalley.modid.block.ModBlocks;
import stardewvalley.modid.block.ModFarmlandBlock;
import stardewvalley.modid.season.Season;

public class FertilizerItem extends Item {

    public enum FertilizerType {
        QUALITY,
        RETAINING,
        SPEED
    }

    private final int tier;
    private final FertilizerType type;

    public FertilizerItem(Settings settings, int tier, FertilizerType type) {
        super(settings);
        this.tier = tier;
        this.type = type;
    }

    public int getTier() {
        return tier;
    }

    public FertilizerType getFertilizerType() {
        return type;
    }

    public float getGrowthBoost() {
        if (type != FertilizerType.SPEED) return 0;
        return switch (tier) {
            case 1 -> 0.10f;
            case 2 -> 0.25f;
            case 3 -> 0.40f;
            default -> 0;
        };
    }

    public int getRetainDays() {
        if (type != FertilizerType.RETAINING) return 0;
        return switch (tier) {
            case 1 -> 3;
            case 2 -> 4;
            case 3 -> 28;
            default -> 0;
        };
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        BlockPos farmlandPos = state.getBlock() instanceof FarmlandBlock ? pos : pos.down();
        BlockState farmlandState = world.getBlockState(farmlandPos);

        if (!(farmlandState.getBlock() instanceof FarmlandBlock)) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            farmlandState = ModBlocks.ensureModFarmland(world, farmlandPos);
            int seasonOrdinal = Season.fromTimeOfDay(world.getTimeOfDay()).ordinal();

            if (type == FertilizerType.QUALITY) {
                int current = farmlandState.get(ModFarmlandBlock.FERTILIZER_TIER);
                if (current > 0) return ActionResult.PASS;
                world.setBlockState(farmlandPos, farmlandState
                    .with(ModFarmlandBlock.FERTILIZER_TIER, tier)
                    .with(ModFarmlandBlock.SEASON_APPLIED, seasonOrdinal), 3);
                context.getStack().decrement(1);
            } else if (type == FertilizerType.SPEED) {
                int current = farmlandState.get(ModFarmlandBlock.GROWTH_BOOST);
                if (current > 0) return ActionResult.PASS;
                world.setBlockState(farmlandPos, farmlandState
                    .with(ModFarmlandBlock.GROWTH_BOOST, tier)
                    .with(ModFarmlandBlock.SEASON_APPLIED, seasonOrdinal), 3);
                context.getStack().decrement(1);
            } else if (type == FertilizerType.RETAINING) {
                int current = farmlandState.get(ModFarmlandBlock.RETAIN_DAYS);
                if (current > 0) return ActionResult.PASS;
                world.setBlockState(farmlandPos, farmlandState
                    .with(ModFarmlandBlock.RETAIN_DAYS, getRetainDays())
                    .with(ModFarmlandBlock.SEASON_APPLIED, seasonOrdinal)
                    .with(FarmlandBlock.MOISTURE, 7), 3);
                context.getStack().decrement(1);
            }
        }
        return ActionResult.SUCCESS;
    }
}
