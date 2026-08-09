package stardewvalley.modid.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import stardewvalley.modid.block.ModFarmlandBlock;
import stardewvalley.modid.crop.CropConfig;
import stardewvalley.modid.season.Season;

public class BaseSeedItem extends Item {

    private final Block cropBlock;
    private final CropConfig config;

    public BaseSeedItem(Settings settings, Block cropBlock, CropConfig config) {
        super(settings);
        this.cropBlock = cropBlock;
        this.config = config;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (config.groundTest().test(state)) {
            BlockPos cropPos = pos.up();
            if (world.isAir(cropPos)) {
                if (!world.isClient()) {
                    // 花盆(ModFarmlandBlock.allowsAllSeasons)上种植跳过季节检查
                    boolean isAllSeason = state.getBlock() instanceof ModFarmlandBlock farmland && farmland.allowsAllSeasons();
                    if (!isAllSeason && config.seasons().length > 0) {
                        Season current = Season.fromTimeOfDay(world.getTimeOfDay());
                        boolean validSeason = false;
                        for (Season s : config.seasons()) {
                            if (s == current) {
                                validSeason = true;
                                break;
                            }
                        }
                        if (!validSeason) {
                            return ActionResult.PASS;
                        }
                    }
                    world.setBlockState(cropPos, cropBlock.getDefaultState(), 3);
                    context.getStack().decrement(1);
                    // 保存种植者UUID，用于农业学家技能判定
                    if (world instanceof ServerWorld serverWorld) {
                        CropPositionState.get(serverWorld).setPlanter(cropPos, context.getPlayer().getUuid());
                    }
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
