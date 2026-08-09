package stardewvalley.modid.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.block.ModBlocks;
import stardewvalley.modid.gui.BookDataManager;
import stardewvalley.modid.season.Season;
import stardewvalley.modid.weather.WeatherState;
import stardewvalley.modid.weather.WeatherType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class HoeItem extends Item {

    public enum Tier {
        NORMAL(1, 1, 20),
        COPPER(1, 3, 20),
        STEEL(1, 5, 20),
        GOLD(3, 3, 20),
        IRIDIUM(3, 6, 20);

        public final int width;
        public final int depth;
        public final int cooldownTicks;

        Tier(int width, int depth, int cooldownTicks) {
            this.width = width;
            this.depth = depth;
            this.cooldownTicks = cooldownTicks;
        }
    }

    private final Tier tier;

    public HoeItem(Settings settings, Tier tier) {
        super(settings.maxCount(1));
        this.tier = tier;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (player != null && player.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.PASS;
        }

        boolean isDirt = state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT_PATH;
        if (!isDirt) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            Direction facing = player != null ? player.getHorizontalFacing() : Direction.NORTH;
            int halfW = tier.width / 2;
            ServerWorld serverWorld = (ServerWorld) world;

            float luckMult = StardewValley.getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player);
                    for (int w = -halfW; w <= halfW; w++) {
                for (int d = 0; d < tier.depth; d++) {
                    BlockPos target = offsetByFacing(pos, facing, w, d);
                    BlockState targetState = world.getBlockState(target);
                    if (targetState.getBlock() == Blocks.DIRT || targetState.getBlock() == Blocks.GRASS_BLOCK || targetState.getBlock() == Blocks.DIRT_PATH) {
                        world.setBlockState(target, Blocks.FARMLAND.getDefaultState(), 3);
                        ModBlocks.ensureModFarmland(world, target);
                        // 10%概率掉落黏土；绿雨天翻倍；受每日运气影响
                        boolean isGreenRain = WeatherState.get(serverWorld).todayWeather == WeatherType.GREEN_RAIN;
                        float hoeDropRate = (isGreenRain ? 0.20f : 0.10f) * luckMult;
                        if (serverWorld.random.nextFloat() < hoeDropRate) {
                            Item clay = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "clay"));
                            if (clay != null) {
                                ItemStack clayStack = new ItemStack(clay);
                                net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(serverWorld,
                                    target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5, clayStack);
                                serverWorld.spawnEntity(itemEntity);
                            }
                        }
                        // 10%概率掉落混合种子（夏季50%概率为花卉混合种子）；绿雨天翻倍
                        if (serverWorld.random.nextFloat() < hoeDropRate) {
                            Season current = Season.fromTimeOfDay(serverWorld.getTimeOfDay());
                            String mixedSeedId;
                            if (current == Season.SUMMER && serverWorld.random.nextBoolean()) {
                                mixedSeedId = "mixedflowerseeds";
                            } else {
                                mixedSeedId = "mixedseeds";
                            }
                            Item mixedSeedItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, mixedSeedId));
                            if (mixedSeedItem != null) {
                                ItemStack mixedStack = new ItemStack(mixedSeedItem);
                                net.minecraft.entity.ItemEntity mixedEntity = new net.minecraft.entity.ItemEntity(serverWorld,
                                    target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5, mixedStack);
                                serverWorld.spawnEntity(mixedEntity);
                            }
                        }

                        // 0.1%概率掉落古代种子（受每日运气影响）
                        if (serverWorld.random.nextFloat() < 0.001f * luckMult) {
                            Item ancientSeedItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "ancient_seed"));
                            if (ancientSeedItem != null) {
                                ItemStack ancientStack = new ItemStack(ancientSeedItem);
                                net.minecraft.entity.ItemEntity ancientEntity = new net.minecraft.entity.ItemEntity(serverWorld,
                                    target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5, ancientStack);
                                serverWorld.spawnEntity(ancientEntity);
                            }
                        }

                        // 0.1%概率掉落金色动物饼干（受最终运气影响）
                        if (serverWorld.random.nextFloat() < 0.001f * luckMult) {
                            Item crackerItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "golden_animal_cracker"));
                            if (crackerItem != null) {
                                ItemStack crackerStack = new ItemStack(crackerItem);
                                net.minecraft.entity.ItemEntity crackerEntity = new net.minecraft.entity.ItemEntity(serverWorld,
                                    target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5, crackerStack);
                                serverWorld.spawnEntity(crackerEntity);
                            }
                        }

                        // 铜墙铁壁：每块地被锄成modfarmland时0.3%概率掉落，每块+0.05%，掉落后稳定0.8%，最多2本
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                            int obtainCount = BookDataManager.get(serverWorld).getBookObtainCount(serverPlayer.getUuid(), "jack_be_nimble_jack_be_thick");
                            if (obtainCount < 2) {
                                boolean dropped = BookDataManager.rollProgressiveDrop(serverWorld, serverPlayer,
                                    "jack_be_nimble_jack_be_thick", 0.003f, 0.0005f, 0.008f, luckMult);
                                if (dropped) {
                                    BookDataManager.get(serverWorld).incrementBookObtainCount(serverPlayer.getUuid(), "jack_be_nimble_jack_be_thick");
                                }
                            }
                        }
                    }
                }
            }

            if (player != null) {
                player.getHungerManager().addExhaustion(1.0f);
                player.getItemCooldownManager().set(stack, tier.cooldownTicks);
            }
        }
        return ActionResult.SUCCESS;
    }

    private static BlockPos offsetByFacing(BlockPos pos, Direction facing, int lateral, int forward) {
        Direction right = facing.rotateYClockwise();
        return pos.offset(facing, forward).offset(right, lateral);
    }

    public Tier getTier() {
        return tier;
    }
}