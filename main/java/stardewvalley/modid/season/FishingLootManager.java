package stardewvalley.modid.season;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.item.ModFishingRodItem;
import stardewvalley.modid.weather.WeatherState;
import stardewvalley.modid.weather.WeatherType;

import java.util.*;

public class FishingLootManager {

    public static final String[] SV_ROD_IDS = {"bamboo_pole", "fiberglass_rod", "iridium_rod", "advanced_iridium_rod"};

    public static boolean isSvRod(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (!StardewValley.MOD_ID.equals(id.getNamespace())) return false;
        for (String rodId : SV_ROD_IDS) {
            if (id.getPath().equals(rodId)) return true;
        }
        return false;
    }

    /** 检查玩家鱼竿上是否装有魔法鱼饵 */
    public static boolean hasMagicBait(PlayerEntity player) {
        ItemStack rod = player.getMainHandStack();
        if (!isSvRod(rod)) rod = player.getOffHandStack();
        if (!isSvRod(rod)) return false;
        ItemStack bait = ModFishingRodItem.getBait(rod);
        if (bait.isEmpty()) return false;
        String path = Registries.ITEM.getId(bait.getItem()).getPath();
        return "bait_magic_bait".equals(path);
    }

    // 垃圾物品ID（不限制季节/时间/天气/群系）
    private static final String[] TRASH_IDS = {
        "joja_cola", "broken_cd", "broken_glasses", "driftwood", "soggy_newspaper", "trash_item"
    };

    // 藻类/海草（无限制）
    private static final String[] ALGAE_IDS = {
        "fish_green_algae", "fish_white_algae", "fish_seaweed"
    };

    // 凝胶（有群系限制）
    private static final String[] JELLY_IDS = {
        "fish_sea_jelly", "fish_river_jelly", "fish_cave_jelly"
    };

    // 普通垃圾XP（垃圾无钓鱼经验，但给极少）
    private static final int TRASH_XP = 3;
    private static final int ALGAE_XP = 3;
    private static final int SEAWEED_XP = 3;

    public static void handleCatch(ServerWorld world, PlayerEntity player, BlockPos bobberPos) {
        // 钓鱼消耗：鱼饵-1，渔具耐久-1
        ModFishingRodItem.applyFishingCost(player);

        boolean magicBait = hasMagicBait(player);
        ItemStack caught = selectCaughtItem(world, player, bobberPos, false, magicBait);
        if (caught == null || caught.isEmpty()) {
            spawnFishItem(world, player, bobberPos, "fish_green_algae", 1);
            addFishingXp(world, player, ALGAE_XP);
            return;
        }
        // 模式1（原始模式）：立即根据水深和钓鱼等级判定品质
        int wDepth = calculateWaterDepth(world, bobberPos);
        caught = applyFishQuality(world, player, wDepth, caught);
        ItemEntity itemEntity = new ItemEntity(world, bobberPos.getX(), bobberPos.getY(), bobberPos.getZ(), caught);
        flyItemToPlayer(itemEntity, player, bobberPos);
        world.spawnEntity(itemEntity);

        String itemName = Registries.ITEM.getId(caught.getItem()).getPath();
        addFishingXp(world, player, getItemXp(itemName));
        tryAddForagingXp(world, player, itemName);
    }

    private static void flyItemToPlayer(ItemEntity itemEntity, PlayerEntity player, BlockPos bobberPos) {
        double dx = player.getX() - bobberPos.getX();
        double dy = player.getY() - bobberPos.getY() + 1.0;
        double dz = player.getZ() - bobberPos.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > 0) {
            itemEntity.setVelocity(
                dx * 0.1,
                dy * 0.1 + Math.sqrt(distance) * 0.08,
                dz * 0.1
            );
        }
        itemEntity.setPickupDelay(5);
    }

    /**
     * 用于 stardewfishing 集成：生成一个鱼/垃圾 ItemStack（符合当前玩家位置条件）
     * 返回的 List 包含一个 ItemStack 或为空
     */
    public static List<ItemStack> generateCustomLoot(PlayerEntity player, BlockPos pos) {
        return generateCustomLoot(player, pos, false, false);
    }

    public static List<ItemStack> generateCustomLoot(PlayerEntity player, BlockPos pos, boolean curiosityLure) {
        return generateCustomLoot(player, pos, curiosityLure, false);
    }

    public static List<ItemStack> generateCustomLoot(PlayerEntity player, BlockPos pos, boolean curiosityLure, boolean magicBait) {
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) return List.of();
        ItemStack caught = selectCaughtItem(serverWorld, player, pos, curiosityLure, magicBait);
        if (caught == null || caught.isEmpty()) return List.of();
        return List.of(caught);
    }

    private static ItemStack selectCaughtItem(ServerWorld world, PlayerEntity player, BlockPos bobberPos) {
        return selectCaughtItem(world, player, bobberPos, false, false);
    }

    private static ItemStack selectCaughtItem(ServerWorld world, PlayerEntity player, BlockPos bobberPos, boolean curiosityLure) {
        return selectCaughtItem(world, player, bobberPos, curiosityLure, false);
    }

    private static ItemStack selectCaughtItem(ServerWorld world, PlayerEntity player, BlockPos bobberPos, boolean curiosityLure, boolean magicBait) {
        int waterDepth = calculateWaterDepth(world, bobberPos);

        if (waterDepth <= 3) {
            String[] shallowPool = {
                "joja_cola", "broken_cd", "broken_glasses", "driftwood", "soggy_newspaper", "trash_item",
                "fish_green_algae", "fish_white_algae", "fish_seaweed"
            };
            String selected = shallowPool[world.random.nextInt(shallowPool.length)];
            int count = isTrash(selected) ? 1 : 1 + world.random.nextInt(3);
            return buildStack(selected, count);
        }

        double depth = waterDepth;
        double trashChance = 16.0 / depth;
        double algaeChance = 32.0 / depth;

        double roll = world.random.nextDouble() * 100.0;

        if (roll < trashChance) {
            return buildStack(TRASH_IDS[world.random.nextInt(TRASH_IDS.length)], 1);
        }

        roll -= trashChance;
        if (roll < algaeChance) {
            String selected = rollAlgaeOrJelly(world, bobberPos);
            if (selected != null) {
                int count = isGel(selected) ? 1 : 1 + world.random.nextInt(3);
                return buildStack(selected, count);
            }
            return buildStack("fish_green_algae", 1 + world.random.nextInt(3));
        }

        Identifier biomeId = world.getBiome(bobberPos).getKey()
            .map(key -> key.getValue()).orElse(Identifier.of("minecraft", "plains"));
        String biomeStr = biomeId.toString();

        String biomeCategory = categorizeBiome(biomeStr);
        Set<String> specificBiomes = getSpecificBiomes(biomeStr);

        List<FishEntry> eligible = new ArrayList<>();
        for (FishEntry fish : ALL_FISH) {
            if (fish.disabled) continue;
            if (!fish.isRealFish) continue;
            if (!matchesBiome(fish, biomeCategory, specificBiomes, biomeStr)) continue;
            if (!magicBait) {
                long timeOfDay = world.getTimeOfDay();
                long tickInDay = timeOfDay % 24000L;
                Season season = Season.fromTimeOfDay(timeOfDay);
                boolean isRaining = isSvWeatherRainy(world);
                if (!matchesTime(fish, tickInDay)) continue;
                if (!matchesSeason(fish, season)) continue;
                if (!matchesWeather(fish, isRaining)) continue;
            }
            eligible.add(fish);
        }

        if (eligible.isEmpty()) {
            return buildStack("fish_green_algae", 1 + world.random.nextInt(3));
        }

        // 检查是否有针对性鱼饵
        String targetFishName = getTargetedBaitFishName(player);
        float luckMult = player instanceof net.minecraft.server.network.ServerPlayerEntity sp
            ? StardewValley.getFinalLuckMultiplier(world, sp)
            : StardewValley.getLuckMultiplier(world);

        FishEntry selected;
        if (targetFishName != null) {
            // 映射：变种传说鱼 → 基础传说鱼
            String legendaryTarget = mapToLegendaryFish(targetFishName);
            if (legendaryTarget != null) {
                // 传说鱼饵：检查条件并尝试产出传说鱼
                String legendaryItemName = "fish_" + legendaryTarget;
                float legendaryChance = 0.33f * luckMult;
                if (world.random.nextFloat() < legendaryChance && canCatchLegendaryFish(world, bobberPos, legendaryTarget)) {
                    selected = findFishEntry(legendaryItemName);
                    if (selected == null) selected = eligible.get(world.random.nextInt(eligible.size()));
                } else {
                    selected = eligible.get(world.random.nextInt(eligible.size()));
                }
            } else {
                // 普通鱼饵：70% * 最终运气 概率钓到目标鱼
                String targetItemName = "fish_" + targetFishName;
                FishEntry targetFish = findFishEntry(targetItemName);
                float targetChance = 0.70f * luckMult;
                if (targetFish != null && eligible.contains(targetFish) && world.random.nextFloat() < targetChance) {
                    selected = targetFish;
                } else {
                    selected = eligible.get(world.random.nextInt(eligible.size()));
                }
            }
        } else if (curiosityLure) {
            // 按售价×难度加权，售价和难度越高概率越大
            double totalWeight = 0;
            for (FishEntry fish : eligible) {
                totalWeight += Math.max(1, fish.weight * fish.rarity);
            }
            double rollW = world.random.nextDouble() * totalWeight;
            double cumW = 0;
            selected = eligible.get(0);
            for (FishEntry fish : eligible) {
                cumW += Math.max(1, fish.weight * fish.rarity);
                if (rollW <= cumW) {
                    selected = fish;
                    break;
                }
            }
        } else {
            selected = eligible.get(world.random.nextInt(eligible.size()));
        }

        return buildStack(selected.itemName, 1);
    }

    /** 从玩家鱼竿的鱼饵中提取目标鱼名（如果是针对性鱼饵） */
    private static String getTargetedBaitFishName(PlayerEntity player) {
        ItemStack rod = player.getMainHandStack();
        if (!isSvRod(rod)) rod = player.getOffHandStack();
        if (!isSvRod(rod)) return null;
        ItemStack bait = ModFishingRodItem.getBait(rod);
        if (bait.isEmpty()) return null;
        String path = Registries.ITEM.getId(bait.getItem()).getPath();
        // bait_{fishname}_bait → extract fishname
        if (path.startsWith("bait_") && path.endsWith("_bait")) {
            String middle = path.substring(5, path.length() - 5); // remove "bait_" and "_bait"
            if (!middle.isEmpty()) return middle;
        }
        return null;
    }

    /** 变种传说鱼 → 基础传说鱼映射 */
    private static final java.util.Map<String, String> LEGENDARY_VARIANT_MAP = java.util.Map.of(
        "son_of_crimsonfish", "crimsonfish",
        "ms._angler", "angler",
        "legend_ii", "legend",
        "glacierfish_jr", "glacierfish",
        "radioactive_carp", "mutant_carp"
    );

    /** 基础传说鱼列表 */
    private static final java.util.Set<String> BASE_LEGENDARY_FISH = java.util.Set.of(
        "crimsonfish", "angler", "legend", "glacierfish", "mutant_carp"
    );

    /**
     * 将鱼名映射到对应的传说鱼（如果是传说鱼/变种传说鱼）。
     * 普通鱼返回 null。
     */
    private static String mapToLegendaryFish(String fishName) {
        if (fishName == null) return null;
        if (BASE_LEGENDARY_FISH.contains(fishName)) return fishName;
        return LEGENDARY_VARIANT_MAP.get(fishName);
    }

    /**
     * 检查当前环境是否满足钓到指定传说鱼的条件。
     * 条件与挑战鱼饵 tryReplaceWithLegendaryFish 一致。
     */
    private static boolean canCatchLegendaryFish(ServerWorld world, BlockPos pos, String legendaryFishName) {
        int waterDepth = calculateWaterDepth(world, pos);
        if (waterDepth <= 10) return false;

        long timeOfDay = world.getTimeOfDay();
        Season season = Season.fromTimeOfDay(timeOfDay);
        boolean isRaining = isSvWeatherRainy(world);

        Identifier biomeId = world.getBiome(pos).getKey()
            .map(key -> key.getValue()).orElse(Identifier.of("minecraft", "plains"));
        String biomeStr = biomeId.toString();
        String b = biomeStr.contains(":") ? biomeStr.split(":", 2)[1] : biomeStr;

        return switch (legendaryFishName) {
            case "legend" -> season == Season.SPRING;
            case "crimsonfish" -> season == Season.SUMMER;
            case "angler" -> season == Season.FALL;
            case "glacierfish" -> season == Season.WINTER;
            case "mutant_carp" -> isRaining && (b.contains("swamp") || b.contains("mangrove"));
            default -> false;
        };
    }

    /** 在 ALL_FISH 中按 itemName 查找 FishEntry */
    private static FishEntry findFishEntry(String itemName) {
        for (FishEntry fish : ALL_FISH) {
            if (fish.itemName.equals(itemName)) return fish;
        }
        return null;
    }

    private static ItemStack buildStack(String itemId, int count) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, itemId);
        Item item = Registries.ITEM.get(id);
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, count);
    }

    private static int getItemXp(String itemName) {
        if (isTrash(itemName)) return TRASH_XP;
        if (itemName.contains("jelly")) return 3;
        if (itemName.contains("algae") || itemName.equals("fish_seaweed")) return ALGAE_XP;

        // 提取品质值
        int quality = QUALITY_NORMAL;
        String baseName = itemName;
        if (itemName.endsWith("_iridium")) {
            quality = QUALITY_IRIDIUM;
            baseName = itemName.substring(0, itemName.length() - "_iridium".length());
        } else if (itemName.endsWith("_gold")) {
            quality = QUALITY_GOLD;
            baseName = itemName.substring(0, itemName.length() - "_gold".length());
        } else if (itemName.endsWith("_silver")) {
            quality = QUALITY_SILVER;
            baseName = itemName.substring(0, itemName.length() - "_silver".length());
        }

        int difficulty = getFishDifficulty(baseName);
        return calcFishingXp(quality, difficulty);
    }

    public static int calculateWaterDepth(ServerWorld world, BlockPos pos) {
        int depth = 0;
        // 从浮漂位置向下数水源方块
        for (int y = pos.getY() - 1; y > world.getBottomY(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (world.getFluidState(checkPos).isIn(FluidTags.WATER)) {
                depth++;
            } else {
                break;
            }
        }
        return depth;
    }

    private static String rollAlgaeOrJelly(ServerWorld world, BlockPos pos) {
        String biomeStr = world.getBiome(pos).getKey()
            .map(key -> key.getValue().toString()).orElse("");
        String category = categorizeBiome(biomeStr);

        // 先收集所有可用的藻类+凝胶
        List<String> candidates = new ArrayList<>();
        candidates.add("fish_green_algae");
        candidates.add("fish_white_algae");
        candidates.add("fish_seaweed");

        if (category.equals("ocean")) {
            candidates.add("fish_sea_jelly");
        } else if (category.equals("cave") || isNetherBiome(biomeStr)) {
            candidates.add("fish_cave_jelly");
        } else {
            // 非海洋/非洞穴 = 河凝胶
            candidates.add("fish_river_jelly");
        }

        return candidates.get(world.random.nextInt(candidates.size()));
    }

    private static boolean isNetherBiome(String biome) {
        String b = biome.contains(":") ? biome.split(":", 2)[1] : biome;
        return b.contains("nether") || b.contains("basalt") || b.contains("soul")
            || b.equals("crimson_forest") || b.equals("warped_forest");
    }

    private static boolean isTrash(String itemId) {
        for (String t : TRASH_IDS) {
            if (t.equals(itemId)) return true;
        }
        return false;
    }

    private static boolean isGel(String itemId) {
        return itemId.contains("jelly");
    }

    public static boolean isRealFishItem(String itemName) {
        if (!itemName.startsWith("fish_")) return false;
        if (itemName.contains("algae") || itemName.contains("jelly") || itemName.equals("fish_seaweed")) return false;
        for (FishEntry fish : ALL_FISH) {
            if (fish.itemName.equals(itemName)) return true;
        }
        return false;
    }

    public static void addFishingXp(ServerWorld world, PlayerEntity player, int amount) {
        FishingLevelManager flm = FishingLevelManager.get(world);
        flm.addXp(player.getUuid(), amount);
    }

    private static void tryAddForagingXp(ServerWorld world, PlayerEntity player, String itemName) {
        if ("fish_seaweed".equals(itemName)) {
            ForagingLevelManager forLm = ForagingLevelManager.get(world);
            forLm.addXp(player.getUuid(), 10);
        }
    }

    private static void spawnFishItem(ServerWorld world, PlayerEntity player, BlockPos pos, String itemId, int count) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, itemId);
        Item item = Registries.ITEM.get(id);
        if (item == null) return;
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(item, count));
        flyItemToPlayer(itemEntity, player, pos);
        world.spawnEntity(itemEntity);
    }

    private static String categorizeBiome(String biome) {
        String b = biome.contains(":") ? biome.split(":", 2)[1] : biome;
        if (b.contains("ocean") || b.contains("deep_ocean") || b.equals("deep_lukewarm_ocean")
            || b.equals("deep_cold_ocean") || b.equals("deep_frozen_ocean")
            || b.equals("lukewarm_ocean") || b.equals("warm_ocean") || b.equals("cold_ocean")
            || b.equals("frozen_ocean") || b.contains("mushroom")) {
            return "ocean";
        }
        if (b.contains("swamp") || b.contains("mangrove") || b.contains("river")
            || b.equals("frozen_river") || b.contains("beach") || b.contains("shore")
            || b.equals("stony_shore")) {
            return "wetland";
        }
        if (b.contains("dripstone") || b.contains("lush_caves") || b.equals("deep_dark")) {
            return "cave";
        }
        if (b.contains("savanna") || b.contains("badlands") || b.equals("desert")
            || b.equals("eroded_badlands") || b.equals("wooded_badlands")) {
            return "arid";
        }
        if (b.contains("taiga") || b.equals("snowy_plains") || b.equals("ice_spikes")
            || b.equals("snowy_beach") || b.equals("snowy_taiga") || b.equals("old_growth_pine_taiga")
            || b.equals("old_growth_spruce_taiga") || b.contains("frozen")) {
            return "cold";
        }
        if (b.contains("jungle") || b.contains("bamboo") || b.contains("forest")
            || b.equals("cherry_grove") || b.equals("flower_forest") || b.equals("birch_forest")
            || b.equals("old_growth_birch_forest") || b.equals("dark_forest")
            || b.equals("windswept_forest")) {
            return "forest";
        }
        if (b.equals("lukewarm_ocean") || b.equals("warm_ocean") || b.equals("deep_lukewarm_ocean")) {
            return "warm_ocean";
        }
        if (b.equals("cold_ocean") || b.equals("frozen_ocean") || b.equals("deep_cold_ocean") || b.equals("deep_frozen_ocean")) {
            return "cold_ocean";
        }
        if (b.contains("plains") || b.contains("meadow") || b.contains("sunflower")) {
            return "plains";
        }
        return "other";
    }

    private static Set<String> getSpecificBiomes(String biome) {
        Set<String> result = new HashSet<>();
        String b = biome.contains(":") ? biome.split(":", 2)[1] : biome;
        result.add(b);
        if (b.equals("jungle")) result.addAll(Set.of("bamboo_jungle", "sparse_jungle"));
        if (b.contains("taiga")) result.addAll(Set.of("old_growth_pine_taiga", "old_growth_spruce_taiga", "snowy_taiga", "taiga"));
        if (b.contains("swamp")) result.add("mangrove_swamp");
        return result;
    }

    private static boolean matchesBiome(FishEntry fish, String category, Set<String> specificBiomes, String biomeStr) {
        String b = biomeStr.contains(":") ? biomeStr.split(":", 2)[1] : biomeStr;

        if (fish.excludedCategories != null) {
            for (String excl : fish.excludedCategories) {
                if (excl.equals(category)) return false;
            }
            if (fish.excludedCategories.contains("ocean+cave+arid")) {
                if (category.equals("ocean") || category.equals("cave") || category.equals("arid")) return false;
            }
            if (fish.excludedCategories.contains("ocean|arid")) {
                if (category.equals("ocean") || category.equals("arid")) return false;
            }
            if (fish.excludedCategories.contains("ocean")) {
                if (category.equals("ocean") || category.equals("warm_ocean") || category.equals("cold_ocean")) return false;
            }
            if (fish.excludedCategories.contains("cave")) {
                if (category.equals("cave")) return false;
            }
            if (fish.excludedCategories.contains("arid")) {
                if (category.equals("arid")) return false;
            }
        }

        if (fish.specificBiomes != null && !fish.specificBiomes.isEmpty()) {
            for (String sb : fish.specificBiomes) {
                String sbc = sb.contains(":") ? sb.split(":", 2)[1] : sb;
                if (b.equals(sbc) || specificBiomes.contains(sbc)) return true;
            }
            return false;
        }

        if (fish.biomeCategories != null && !fish.biomeCategories.isEmpty()) {
            for (String cat : fish.biomeCategories) {
                if (cat.equals(category)) return true;
                if (cat.equals("warm_ocean") && category.equals("warm_ocean")) return true;
                if (cat.equals("cold_ocean") && category.equals("cold_ocean")) return true;
                if (cat.equals("forest") && category.equals("forest")) return true;
            }
            return false;
        }

        return true;
    }

    private static boolean matchesTime(FishEntry fish, long tickInDay) {
        if (fish.timeStart == null || fish.timeEnd == null) return true;
        int start = fish.timeStart;
        int end = fish.timeEnd;
        if (start == end) return true;
        if (start < end) {
            return tickInDay >= start && tickInDay < end;
        } else {
            return tickInDay >= start || tickInDay < end;
        }
    }

    private static boolean matchesSeason(FishEntry fish, Season season) {
        return fish.seasons == null || fish.seasons.isEmpty() || fish.seasons.contains(season);
    }

    private static boolean matchesWeather(FishEntry fish, boolean isRaining) {
        return switch (fish.weather) {
            case ANY -> true;
            case RAIN -> isRaining;
            case SUNNY -> !isRaining;
        };
    }

    /** 使用星露谷天气系统判断是否为雨天（雨/雷雨/绿雨算下雨，雪天不算） */
    private static boolean isSvWeatherRainy(ServerWorld world) {
        WeatherState state = WeatherState.get(world);
        return switch (state.todayWeather) {
            case RAIN, STORM, GREEN_RAIN -> true;
            default -> false;
        };
    }

    // ====== 鱼数据 ======

    public record FishEntry(
        String itemName,
        int stamina,
        int health,
        int fishXp,
        int weight,
        int rarity,
        boolean junk,
        boolean disabled,
        boolean isRealFish,    // true = 真正的鱼（非垃圾/藻类/凝胶）
        Weather weather,
        List<Season> seasons,
        Integer timeStart,
        Integer timeEnd,
        List<String> biomeCategories,
        List<String> specificBiomes,
        List<String> excludedCategories
    ) {}

    public enum Weather { ANY, SUNNY, RAIN }

    private static final int T_6AM = 0;
    private static final int T_7AM = 1000;
    private static final int T_8AM = 2000;
    private static final int T_9AM = 3000;
    private static final int T_10AM = 4000;
    private static final int T_11AM = 5000;
    private static final int T_12PM = 6000;
    private static final int T_1PM = 7000;
    private static final int T_2PM = 8000;
    private static final int T_4PM = 10000;
    private static final int T_6PM = 12000;
    private static final int T_7PM = 13000;
    private static final int T_8PM = 14000;
    private static final int T_10PM = 16000;
    private static final int T_12AM = 18000;
    private static final int T_2AM = 20000;

    private static final List<String> CAT_OCEAN = List.of("ocean");
    private static final List<String> CAT_WETLAND = List.of("wetland");
    private static final List<String> CAT_CAVE = List.of("cave");
    private static final List<String> CAT_ARID = List.of("arid");
    private static final List<String> CAT_FOREST = List.of("forest");
    private static final List<String> CAT_COLD = List.of("cold");
    private static final List<String> CAT_WARM_OCEAN = List.of("warm_ocean");
    private static final List<String> CAT_COLD_OCEAN = List.of("cold_ocean");
    private static final List<String> CAT_SWAMP = List.of("wetland");
    private static final List<String> CAT_TROPICAL = List.of("arid", "forest");

    private static final List<String> EXCLUDE_NONE = null;
    private static final List<String> EXCLUDE_OCEAN_CAVE_ARID = List.of("ocean+cave+arid");
    private static final List<String> EXCLUDE_OCEAN_ARID = List.of("ocean|arid");
    private static final List<String> EXCLUDE_OCEAN = List.of("ocean");
    private static final List<String> EXCLUDE_CAVE = List.of("cave");
    private static final List<String> EXCLUDE_ARID = List.of("arid");
    private static final List<String> EXCLUDE_CAVE_ARID = List.of("cave", "arid");

    private static final List<Season> ALL_SEASONS = List.of(Season.SPRING, Season.SUMMER, Season.FALL, Season.WINTER);
    private static final List<Season> SPRING = List.of(Season.SPRING);
    private static final List<Season> SUMMER = List.of(Season.SUMMER);
    private static final List<Season> FALL = List.of(Season.FALL);
    private static final List<Season> WINTER = List.of(Season.WINTER);
    private static final List<Season> SPRING_SUMMER = List.of(Season.SPRING, Season.SUMMER);
    private static final List<Season> SPRING_FALL = List.of(Season.SPRING, Season.FALL);
    private static final List<Season> SUMMER_FALL = List.of(Season.SUMMER, Season.FALL);
    private static final List<Season> FALL_WINTER = List.of(Season.FALL, Season.WINTER);
    private static final List<Season> SPRING_SUMMER_FALL = List.of(Season.SPRING, Season.SUMMER, Season.FALL);
    private static final List<Season> SUMMER_WINTER = List.of(Season.SUMMER, Season.WINTER);
    private static final List<Season> SPRING_WINTER = List.of(Season.SPRING, Season.WINTER);
    private static final List<Season> SPRING_SUMMER_WINTER = List.of(Season.SPRING, Season.SUMMER, Season.WINTER);
    private static final List<Season> SPRING_FALL_WINTER = List.of(Season.SPRING, Season.FALL, Season.WINTER);

    private static final List<String> SPECIFIC_COLD_OCEAN = List.of("minecraft:cold_ocean", "minecraft:deep_cold_ocean",
        "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean",
        "minecraft:snowy_plains", "minecraft:ice_spikes", "minecraft:snowy_beach",
        "minecraft:frozen_river", "minecraft:snowy_taiga");
    private static final List<String> SPECIFIC_WARM_OCEAN = List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean",
        "minecraft:deep_lukewarm_ocean");
    private static final List<String> SPECIFIC_ARID = List.of("minecraft:desert", "minecraft:badlands",
        "minecraft:eroded_badlands", "minecraft:wooded_badlands");
    private static final List<String> SPECIFIC_MANGROVE_SWAMP = List.of("minecraft:swamp", "minecraft:mangrove_swamp");
    private static final List<String> SPECIFIC_TROPICAL = List.of("minecraft:savanna", "minecraft:savanna_plateau",
        "minecraft:windswept_savanna", "minecraft:jungle", "minecraft:bamboo_jungle", "minecraft:sparse_jungle");

    // 所有鱼数据
    // isRealFish=true = 真正的鱼，仅在depth>=4时出现
    public static final FishEntry[] ALL_FISH = {
        new FishEntry("fish_pufferfish", -100, 0, 29, 200, 4, false, false, true, Weather.SUNNY, SUMMER, T_12PM, T_4PM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_anchovy", 13, 5, 13, 30, 2, false, false, true, Weather.ANY, SPRING_FALL, T_6AM, T_7PM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_tuna", 38, 17, 26, 100, 3, false, false, true, Weather.ANY, SUMMER_WINTER, T_6AM, T_7PM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_sardine", 13, 5, 13, 40, 2, false, false, true, Weather.ANY, SPRING_SUMMER_FALL, T_6AM, T_7PM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_bullhead", 13, 5, 14, 45, 2, false, false, true, Weather.ANY, ALL_SEASONS, T_6PM, T_2AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_largemouth_bass", 38, 17, 19, 100, 3, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_7PM, CAT_FOREST, null, EXCLUDE_NONE),
        new FishEntry("fish_smallmouth_bass", 25, 11, 12, 50, 2, false, false, true, Weather.ANY, SPRING_FALL, T_6AM, T_7PM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_rainbow_trout", 24, 11, 18, 65, 3, false, false, true, Weather.SUNNY, SUMMER, T_6AM, T_7PM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_salmon", 38, 17, 19, 75, 3, false, false, true, Weather.ANY, FALL, T_6AM, T_7PM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_walleye", 30, 13, 18, 105, 3, false, false, true, Weather.RAIN, FALL_WINTER, T_12PM, T_2AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_perch", 25, 11, 14, 55, 2, false, false, true, Weather.ANY, WINTER, null, null, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_carp", 13, 5, 8, 30, 1, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_catfish", 50, 22, 28, 200, 4, false, false, true, Weather.RAIN, SPRING_FALL, T_6AM, T_12AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_pike", 38, 17, 23, 100, 3, false, false, true, Weather.ANY, SUMMER_WINTER, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_sunfish", 13, 5, 13, 30, 2, false, false, true, Weather.SUNNY, SPRING_SUMMER, T_6AM, T_7PM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_red_mullet", 25, 11, 21, 75, 3, false, false, true, Weather.ANY, SUMMER_WINTER, T_6AM, T_7PM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_herring", 13, 5, 11, 30, 2, false, false, true, Weather.ANY, SPRING_WINTER, T_6AM, T_6AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_eel", 30, 13, 26, 85, 3, false, false, true, Weather.RAIN, SPRING_FALL, T_4PM, T_2AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_octopus", 0, 0, 34, 150, 4, false, false, true, Weather.ANY, SUMMER, T_6AM, T_1PM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_red_snapper", 25, 11, 16, 50, 2, false, false, true, Weather.RAIN, SPRING_SUMMER_FALL, T_6AM, T_7PM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_squid", 25, 11, 28, 80, 3, false, false, true, Weather.ANY, WINTER, T_6PM, T_2AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_sea_cucumber", -25, 0, 16, 75, 3, false, false, true, Weather.ANY, FALL_WINTER, T_6AM, T_7PM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_super_cucumber", 125, 56, 29, 250, 5, false, false, true, Weather.ANY, SUMMER_FALL, T_6PM, T_2AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_ghostfish", 38, 17, 19, 45, 3, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_CAVE, null, EXCLUDE_NONE),
        new FishEntry("fish_stonefish", 0, 0, 24, 300, 5, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_CAVE, null, EXCLUDE_NONE),
        new FishEntry("fish_ice_pip", 38, 17, 31, 500, 5, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_COLD_OCEAN, SPECIFIC_COLD_OCEAN, EXCLUDE_NONE),
        new FishEntry("fish_lava_eel", 50, 22, 33, 700, 6, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM,
            CAT_CAVE, List.of("minecraft:nether_wastes", "minecraft:basalt_deltas", "minecraft:soul_sand_valley",
                "minecraft:crimson_forest", "minecraft:warped_forest"), EXCLUDE_NONE),
        new FishEntry("fish_sandfish", 13, 5, 24, 75, 3, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_8PM, CAT_ARID, SPECIFIC_ARID, EXCLUDE_NONE),
        new FishEntry("fish_scorpion_carp", -125, 0, 33, 150, 4, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_8PM, CAT_ARID, SPECIFIC_ARID, EXCLUDE_NONE),
        new FishEntry("fish_flounder", 38, 17, 19, 150, 3, false, false, true, Weather.ANY, SPRING_SUMMER, T_6AM, T_8PM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_midnight_carp", 50, 22, 21, 150, 3, false, false, true, Weather.ANY, ALL_SEASONS, T_10PM, T_2AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_sturgeon", 25, 11, 29, 200, 4, false, false, true, Weather.ANY, SUMMER_WINTER, T_6AM, T_7PM, null,
            List.of("minecraft:river", "minecraft:stony_shore", "minecraft:frozen_river",
                "minecraft:jungle", "minecraft:bamboo_jungle", "minecraft:sparse_jungle"), EXCLUDE_NONE),
        new FishEntry("fish_tiger_trout", 25, 11, 23, 150, 3, false, false, true, Weather.ANY, FALL_WINTER, T_6AM, T_7PM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_tilapia", 25, 11, 19, 75, 2, false, false, true, Weather.ANY, SUMMER_FALL, T_6AM, T_2PM, null, null, EXCLUDE_CAVE_ARID),
        new FishEntry("fish_chub", 25, 11, 14, 50, 2, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_dorado", 25, 11, 29, 100, 3, false, false, true, Weather.ANY, SUMMER, T_6AM, T_7PM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_albacore", 25, 11, 23, 75, 3, false, false, true, Weather.ANY, FALL_WINTER, T_6AM, T_6AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_shad", 25, 11, 18, 60, 2, false, false, true, Weather.RAIN, SPRING_SUMMER_FALL, T_9AM, T_2AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_lingcod", 25, 11, 31, 120, 3, false, false, true, Weather.ANY, WINTER, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_halibut", 25, 11, 19, 80, 3, false, false, true, Weather.ANY, SPRING_SUMMER_WINTER, T_6AM, T_6AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_woodskip", 25, 11, 19, 75, 3, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, null,
            List.of("minecraft:swamp", "minecraft:mangrove_swamp", "minecraft:jungle", "minecraft:bamboo_jungle",
                "minecraft:sparse_jungle", "minecraft:taiga", "minecraft:snowy_taiga",
                "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga"), EXCLUDE_NONE),
        new FishEntry("fish_void_salmon", 63, 28, 29, 150, 4, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_SWAMP, SPECIFIC_MANGROVE_SWAMP, EXCLUDE_NONE),
        new FishEntry("fish_slimejack", 38, 17, 21, 100, 3, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_SWAMP, SPECIFIC_MANGROVE_SWAMP, EXCLUDE_NONE),
        new FishEntry("fish_stingray", 38, 17, 29, 180, 4, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_TROPICAL, SPECIFIC_TROPICAL, EXCLUDE_NONE),
        new FishEntry("fish_lionfish", 38, 17, 19, 100, 3, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_WARM_OCEAN, SPECIFIC_WARM_OCEAN, EXCLUDE_NONE),
        new FishEntry("fish_blue_discus", 38, 17, 23, 120, 3, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_TROPICAL, SPECIFIC_TROPICAL, EXCLUDE_NONE),
        new FishEntry("fish_goby", -62, 0, 21, 150, 3, false, false, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, null,
            List.of("minecraft:dripstone_caves", "minecraft:lush_caves", "minecraft:jungle", "minecraft:bamboo_jungle",
                "minecraft:sparse_jungle", "minecraft:taiga", "minecraft:snowy_taiga",
                "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga"), EXCLUDE_NONE),
        // 暂不可钓鱼种
        new FishEntry("fish_midnight_squid", 38, 17, 21, 100, 5, false, true, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_spook_fish", 38, 17, 23, 220, 5, false, true, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_blobfish", 38, 17, 28, 500, 6, false, true, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_crimsonfish", 38, 17, 170, 1500, 95, false, true, true, Weather.ANY, SUMMER, T_6AM, T_6AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_angler", 25, 11, 155, 900, 85, false, true, true, Weather.ANY, FALL, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_legend", 500, 225, 195, 5000, 110, false, true, true, Weather.ANY, SPRING, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_glacierfish", 25, 11, 180, 1000, 100, false, true, true, Weather.ANY, WINTER, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_mutant_carp", 25, 11, 145, 1000, 80, false, true, true, Weather.RAIN, ALL_SEASONS, T_6AM, T_6AM, null, SPECIFIC_MANGROVE_SWAMP, EXCLUDE_NONE),
        new FishEntry("fish_son_of_crimsonfish", 38, 17, 34, 1500, 6, false, true, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, CAT_OCEAN, null, EXCLUDE_NONE),
        new FishEntry("fish_ms._angler", 38, 17, 31, 900, 6, false, true, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_legend_ii", 500, 225, 39, 5000, 10, false, true, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_glacierfish_jr", 25, 11, 36, 1000, 6, false, true, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
        new FishEntry("fish_radioactive_carp", 25, 11, 29, 1000, 6, false, true, true, Weather.ANY, ALL_SEASONS, T_6AM, T_6AM, null, null, EXCLUDE_OCEAN_CAVE_ARID),
    };

    // ====== 鱼类品质系统 ======

    /** 品质值：普通=0，银星=1，金星=2，铱星=4 */
    private static final int QUALITY_NORMAL = 0;
    private static final int QUALITY_SILVER = 1;
    private static final int QUALITY_GOLD = 2;
    private static final int QUALITY_IRIDIUM = 4;

    /** levelGroups 索引映射：fishingLevel → groupIndex */
    private static int levelGroupIndex(int fishingLevel) {
        return Math.min(fishingLevel / 2, 9); // 0-1→0, 2-3→1, ..., 18-19→9
    }

    /** 品质概率表：[zone][groupIndex] = {normal%, silver%, gold%} */
    private static final int[][][] QUALITY_TABLE = {
        // zone 1 (depth 4-5): 索引0
        new int[][] {
            {100, 0, 0},  // lv 0-1
            {100, 0, 0},  // lv 2-3
            {100, 0, 0},  // lv 4-5
            {100, 0, 0},  // lv 6-7
            {100, 0, 0},  // lv 8-9
            {100, 0, 0},  // lv 10-11
            {100, 0, 0},  // lv 12-13
            {67,  33, 0}, // lv 14-15
            {10,  90, 0}, // lv 16-17
            {10,  90, 0}, // lv 18-19
        },
        // zone 2 (depth 6-11): 索引1
        new int[][] {
            {73, 27, 0},  // lv 0-1
            {67, 33, 0},  // lv 2-3
            {56, 44, 0},  // lv 4-5
            {33, 67, 0},  // lv 6-7
            {0,  100, 0}, // lv 8-9
            {0,  100, 0}, // lv 10-11
            {0,  100, 0}, // lv 12-13
            {0,  67, 33}, // lv 14-15
            {0,  10, 90}, // lv 16-17
            {0,  10, 90}, // lv 18-19
        },
        // zone 3 (depth 12-17): 索引2
        new int[][] {
            {42, 57, 1},  // lv 0-1
            {27, 71, 1},  // lv 2-3
            {3,  95, 2},  // lv 4-5
            {0,  98, 2},  // lv 6-7
            {0,  95, 5},  // lv 8-9
            {0,  10, 90}, // lv 10-11
            {0,  0,  100},// lv 12-13
            {0,  0,  100},// lv 14-15
            {0,  0,  100},// lv 16-17
            {0,  0,  100},// lv 18-19
        },
        // zone 4 (保留，使用 zone 3 数据): 索引3
        null,
        // zone 5 (depth >= 18): 索引4
        new int[][] {
            {20, 39, 41}, // lv 0-1
            {0,  49, 51}, // lv 2-3
            {0,  32, 68}, // lv 4-5
            {0,  0,  100},// lv 6-7
            {0,  0,  100},// lv 8-9
            {0,  0,  100},// lv 10-11
            {0,  0,  100},// lv 12-13
            {0,  0,  100},// lv 14-15
            {0,  0,  100},// lv 16-17
            {0,  0,  100},// lv 18-19
        },
    };

    /** 根据水深获取钓鱼区 */
    public static int getFishingZone(int waterDepth) {
        if (waterDepth <= 5) return 1;
        if (waterDepth <= 11) return 2;
        if (waterDepth <= 17) return 3;
        return 5;
    }

    /** 根据钓鱼区和钓鱼等级随机决定品质，返回品质值 */
    public static int rollQuality(int waterDepth, int fishingLevel, ServerWorld world) {
        int zone = getFishingZone(waterDepth);
        int zi = zone == 5 ? 4 : zone - 1;
        if (zi < 0 || zi >= QUALITY_TABLE.length || QUALITY_TABLE[zi] == null) return QUALITY_NORMAL;

        int gi = levelGroupIndex(fishingLevel);
        int[] probs = QUALITY_TABLE[zi][gi];
        int roll = world.random.nextInt(100);
        int cum = 0;
        cum += probs[0]; if (roll < cum) return QUALITY_NORMAL;
        cum += probs[1]; if (roll < cum) return QUALITY_SILVER;
        return QUALITY_GOLD;
    }

    /** 品质值 → 物品名后缀 */
    public static String qualitySuffix(int quality) {
        return switch (quality) {
            case QUALITY_SILVER -> "_silver";
            case QUALITY_GOLD -> "_gold";
            case QUALITY_IRIDIUM -> "_iridium";
            default -> "";
        };
    }

    /** 判断是否为鱼王（传奇鱼类） */
    private static final Set<String> LEGENDARY_FISH = Set.of(
        "fish_crimsonfish", "fish_angler", "fish_legend", "fish_glacierfish", "fish_mutant_carp"
    );

    public static boolean isLegendaryFish(String itemName) {
        return LEGENDARY_FISH.contains(itemName);
    }

    /**
     * 挑战鱼饵专用：尝试将当前鱼替换为符合条件的鱼王
     * @return 鱼王的ItemStack，若不满足条件则返回null
     */
    public static ItemStack tryReplaceWithLegendaryFish(ServerWorld world, PlayerEntity player, BlockPos pos, boolean isChallengeBait) {
        if (!isChallengeBait) return null;

        // 水深要求：大于10
        int waterDepth = calculateWaterDepth(world, pos);
        if (waterDepth <= 10) return null;

        // 33%概率判定
        if (world.random.nextFloat() >= 0.33f) return null;

        long timeOfDay = world.getTimeOfDay();
        Season season = Season.fromTimeOfDay(timeOfDay);
        boolean isRaining = isSvWeatherRainy(world);

        // 获取生物群系（仅用于变种鲤鱼判定）
        Identifier biomeId = world.getBiome(pos).getKey()
            .map(key -> key.getValue()).orElse(Identifier.of("minecraft", "plains"));
        String biomeStr = biomeId.toString();
        String b = biomeStr.contains(":") ? biomeStr.split(":", 2)[1] : biomeStr;

        // 按季节优先级检查各鱼王条件
        // 1. 传说之鱼 - 春季（无群系要求）
        if (season == Season.SPRING) {
            return buildStack("fish_legend", 1);
        }

        // 2. 绯红鱼 - 夏季（无群系要求）
        if (season == Season.SUMMER) {
            return buildStack("fish_crimsonfish", 1);
        }

        // 3. 𩽾𩾌鱼 - 秋季（无群系要求）
        if (season == Season.FALL) {
            return buildStack("fish_angler", 1);
        }

        // 4. 冰川鱼 - 冬季（无群系要求）
        if (season == Season.WINTER) {
            return buildStack("fish_glacierfish", 1);
        }

        // 5. 变种鲤鱼 - 任意季节，雨天，沼泽群系
        if (isRaining && (b.contains("swamp") || b.contains("mangrove"))) {
            return buildStack("fish_mutant_carp", 1);
        }

        return null;
    }

    /** 获取鱼的难度（rarity 字段） */
    public static int getFishDifficulty(String baseItemName) {
        for (FishEntry fish : ALL_FISH) {
            if (fish.itemName.equals(baseItemName)) return fish.rarity;
        }
        return 1;
    }

    /** 计算钓鱼经验：XP = (品质值 + 1) * 3 + 难度 / 3 */
    public static int calcFishingXp(int quality, int difficulty) {
        return (quality + 1) * 3 + difficulty / 3;
    }

    /** 根据水深和钓鱼等级对鱼物品应用品质，非鱼/凝胶/藻类不处理 */
    public static ItemStack applyFishQuality(ServerWorld world, PlayerEntity player, int waterDepth, ItemStack fish) {
        String baseName = Registries.ITEM.getId(fish.getItem()).getPath();
        if (!baseName.startsWith("fish_") || baseName.contains("jelly") || baseName.contains("algae") || baseName.equals("fish_seaweed")) {
            return fish;
        }
        FishingLevelManager flm = FishingLevelManager.get(world);
        int fishingLevel = flm.getLevel(player.getUuid());
        int quality = rollQuality(waterDepth, fishingLevel, world);
        String suffix = qualitySuffix(quality);
        if (!suffix.isEmpty()) {
            String testName = baseName + suffix;
            Identifier testId = Identifier.of(StardewValley.MOD_ID, testName);
            if (Registries.ITEM.containsId(testId)) {
                Item upgradedItem = Registries.ITEM.get(testId);
                return new ItemStack(upgradedItem, fish.getCount());
            }
        }
        return fish;
    }
}
