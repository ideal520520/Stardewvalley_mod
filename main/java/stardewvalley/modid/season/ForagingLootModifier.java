package stardewvalley.modid.season;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import stardewvalley.modid.gui.BookDataManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import stardewvalley.modid.gui.BookDataManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.block.ModBlocks;
import stardewvalley.modid.crop.CropQuality;
import stardewvalley.modid.skill.SkillEffectHelper;
import stardewvalley.modid.weather.WeatherState;
import stardewvalley.modid.weather.WeatherType;

import java.util.List;

public class ForagingLootModifier {

    public static void register() {
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register(
            (world, player, pos, state, blockEntity) -> {
                if (world.isClient()) return;
                if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

                ServerWorld sw = (ServerWorld) world;
                Block block = state.getBlock();
                Season season = Season.fromTimeOfDay(world.getTimeOfDay());
                float luckMult = StardewValley.getFinalLuckMultiplier(sw, serverPlayer);

                List<String> candidates = null;

                boolean isStardewGrass = block == ModBlocks.STARDEW_GRASS;
                if (block == Blocks.GRASS_BLOCK || block == Blocks.TALL_GRASS || block == Blocks.SHORT_GRASS || block == Blocks.DIRT
                    || block == Blocks.LEAF_LITTER || isStardewGrass) {
                    candidates = getSeasonalGrassItems(season);
                } else if (block == Blocks.STONE) {
                    candidates = getDarkStoneItems();
                } else if (block == Blocks.MYCELIUM) {
                    candidates = getMyceliumItems(season);
                } else if (block == Blocks.SAND || block == Blocks.RED_SAND) {
                    String biomeId = world.getBiome(pos).getKey().map(k -> k.getValue().toString()).orElse("");
                    if (biomeId.contains("beach") || biomeId.contains("ocean") || biomeId.contains("river")) {
                        candidates = getWaterItems();
                    } else if (biomeId.contains("desert")) {
                        candidates = getDesertItems();
                    }
                } else if (block == Blocks.GRAVEL) {
                    candidates = getDarkStoneItems();
                } else if (block == Blocks.KELP || block == Blocks.SEAGRASS || block == Blocks.TALL_SEAGRASS) {
                    candidates = getWaterItems();
                } else if (state.isIn(BlockTags.LOGS)) {
                    candidates = getLogItems(season);
                    // 2%概率掉落硬木（受最终运气影响）
                    if (world.random.nextFloat() < 0.02f * luckMult) {
                        Item hardwoodItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "hardwood"));
                        if (hardwoodItem != null) {
                            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(hardwoodItem)));
                        }
                    }
                } else if (block == Blocks.BROWN_MUSHROOM_BLOCK || block == Blocks.RED_MUSHROOM_BLOCK) {
                    candidates = getMushroomLogItems();
                } else if (block == Blocks.CACTUS) {
                    float cactusChance = 0.2f * luckMult;
                    if (world.random.nextFloat() < cactusChance) {
                        ForagingLevelManager flm2 = ForagingLevelManager.get(sw);
                        int lvl2 = stardewvalley.modid.effect.SkillBuffHelper.getEffectiveForagingLevel(sw, player.getUuid(), serverPlayer);
                        stardewvalley.modid.crop.CropQuality cq = rollQuality(lvl2, world.random, luckMult);
                        String cid = "cactusfruit" + cq.getSuffix();
                        Item ci = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, cid));
                        if (ci == null) ci = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "cactusfruit"));
                        world.spawnEntity(new ItemEntity(world, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, new ItemStack(ci)));
                        flm2.addXp(player.getUuid(), 10);
                    }
                    candidates = null;
                }

                if (candidates != null && !candidates.isEmpty()) {
                    ForagingLevelManager flm = ForagingLevelManager.get(sw);
                    int level = stardewvalley.modid.effect.SkillBuffHelper.getEffectiveForagingLevel(sw, player.getUuid(), serverPlayer);
                    boolean addedXp = false;

                    for (String dropId : candidates) {
                    int price = ITEM_VALUES.getOrDefault(dropId, 50);
                    float x = price / ALL_ITEMS_TOTAL_PRICE;
                    float dropChance = (float)(Math.log(1.0 / x + 1) - 1) / 100.0f - 0.015f;
                    // 星露谷草额外+2%掉落概率
                    if (isStardewGrass) dropChance += 0.02f;
                    // 绿雨天采集概率翻倍
                    boolean greenRain = WeatherState.get(sw).todayWeather == WeatherType.GREEN_RAIN;
                    if (greenRain) dropChance *= 2;
                    // 追踪者：采集品掉落概率翻倍
                    if (SkillEffectHelper.hasTracker(sw, player.getUuid())) dropChance *= 2;
                    // 采集品掉落概率
                    if (dropChance <= 0 || world.random.nextFloat() >= dropChance) continue;

                    boolean hasQuality = !"caiji_seaweed".equals(dropId) && !"caiji_cavecarrot".equals(dropId) && !"caiji_ginger".equals(dropId);

                    ItemStack drop;
                    if (hasQuality) {
                        // 植物学家：采集品必定铱星
                        CropQuality quality;
                        if (SkillEffectHelper.hasBotanist(sw, player.getUuid())) {
                            quality = CropQuality.IRIDIUM;
                        } else {
                            quality = rollQuality(level, world.random, luckMult);
                        }
                        String qualityId = dropId + quality.getSuffix();
                        Item item = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, qualityId));
                        if (item == null) item = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, dropId));
                        drop = new ItemStack(item);
                    } else {
                        Item item = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, dropId));
                        drop = new ItemStack(item);
                    }

                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                    world.spawnEntity(itemEntity);
                    addedXp = true;

                    // 收集者：33% 概率双倍采集品（受最终运气影响）
                    if (SkillEffectHelper.hasGatherer(sw, player.getUuid()) && world.random.nextFloat() < 0.33f * luckMult) {
                        ItemStack doubleDrop = drop.copy();
                        ItemEntity doubleEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, doubleDrop);
                        world.spawnEntity(doubleEntity);
                    }
                }

                    if (addedXp) flm.addXp(player.getUuid(), 10);

                    // 采集精通：采集到物品时可找到金色迷之盒（受最终运气影响）
                    if (addedXp && stardewvalley.modid.skill.MasteryManager.hasMasteredSkill(sw, player.getUuid(),
                        stardewvalley.modid.skill.SkillRegistry.Category.FORAGING)
                        && world.random.nextFloat() < 0.01f * luckMult) {
                        Item gmb = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "golden_mystery_box"));
                        if (gmb != null) {
                            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(gmb)));
                        }
                    }
                }

                // ========== 书籍效果 ==========

                boolean isGrass = block == Blocks.GRASS_BLOCK || block == Blocks.TALL_GRASS || block == Blocks.SHORT_GRASS
                    || block == Blocks.DIRT || block == Blocks.LEAF_LITTER || isStardewGrass;
                boolean isLog = state.isIn(BlockTags.LOGS);
                boolean isStone = block == Blocks.STONE;
                boolean hasBook = false;

                // 小巷自助餐：破坏草2%概率掉落星露谷菜品（×最终运气）
                if (isGrass && BookDataManager.get(sw).hasUsedBook(player.getUuid(), "the_alleyway_buffet")
                    && world.random.nextFloat() < 0.02f * luckMult) {
                    Random rand = world.random;
                    String[] dishNames = {
                        "algae_soup", "artichoke_dip", "autums_bounty", "baked_fish",
                        "banana_pudding", "bean_hotpot", "blackberry_cobbler", "blueberry_tart",
                        "bread", "bruschetta", "carp_surprise", "cheese_cauliflower",
                        "chocolate_cake", "chowder", "coleslaw", "complete_breakfast",
                        "cookie", "crab_cakes", "cranberry_candy", "cranberry_sauce",
                        "crispy_bass", "dish_of_the_sea", "eggplant_parmesan", "escargot",
                        "farmers_lunch", "fiddlehead_risotto", "fish_stew", "fish_taco",
                        "fried_calamari", "fried_eel", "fried_egg", "fried_mushroom",
                        "fruit_salad", "ginger_ale", "glazed_yams", "hashbrowns",
                        "ice_cream", "lobster_bisque", "lucky_lunch", "maki_roll",
                        "mango_sticky_rice", "maple_bar", "miners_treat", "moss_soup",
                        "omelet", "pale_broth", "pancakes", "parsnip_soup",
                        "pepper_poppers", "pink_cake", "pizza", "plum_pudding",
                        "poi", "poppyseed_muffin", "pumpkin_pie", "pumpkin_soup",
                        "radish_salad", "red_plate", "rhubarb_pie", "rice_pudding",
                        "roasted_hazelnuts", "roots_platter", "salad", "salmon_dinner",
                        "sashimi", "seafoam_pudding", "shrimp_cocktail", "spaghetti",
                        "spicy_eel", "squid_ink_ravioli", "stir_fry", "strange_bun",
                        "stuffing", "super_meal", "survival_burger", "tom_kha_soup",
                        "tortilla", "triple_shot_espresso", "tropical_curry", "trout_soup",
                        "vegetable_medley"
                    };
                    String dishName = dishNames[rand.nextInt(dishNames.length)];
                    Item dishItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, dishName));
                    if (dishItem != null) {
                        world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(dishItem)));
                    }
                }

                // 伐木秘事：破坏原木10%概率额外掉落一个原木（×最终运气）
                if (isLog && BookDataManager.get(sw).hasUsedBook(player.getUuid(), "woodys_secret")
                    && world.random.nextFloat() < 0.1f * luckMult) {
                    String logId = Registries.BLOCK.getId(state.getBlock()).getPath();
                    Item logItem = Registries.ITEM.get(Identifier.of(logId));
                    if (logItem != null) {
                        world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(logItem)));
                    }
                }

                // 伐木秘事（书本掉落）：星露谷斧头砍原木0.02%起，每次+0.001%，掉落后稳定0.2%
                if (isLog && player instanceof ServerPlayerEntity sp) {
                    ItemStack held = player.getMainHandStack();
                    boolean isSvAxe = !held.isEmpty()
                        && StardewValley.MOD_ID.equals(Registries.ITEM.getId(held.getItem()).getNamespace())
                        && Registries.ITEM.getId(held.getItem()).getPath().contains("axe");
                    if (isSvAxe) {
                        BookDataManager.rollProgressiveDrop(sw, sp, "woodys_secret", 0.0002f, 0.00001f, 0.002f, luckMult);
                    }
                }

                // 钻石猎人：星露谷镐挖石头1%概率掉落钻石（×最终运气），受地质学家影响双倍
                if (isStone && BookDataManager.get(sw).hasUsedBook(player.getUuid(), "the_diamond_hunter")) {
                    ItemStack held = player.getMainHandStack();
                    boolean isSvPickaxe = false;
                    if (!held.isEmpty()) {
                        String itemPath = Registries.ITEM.getId(held.getItem()).getPath();
                        if (StardewValley.MOD_ID.equals(Registries.ITEM.getId(held.getItem()).getNamespace())
                            && itemPath.contains("pickaxe")) {
                            isSvPickaxe = true;
                        }
                    }
                    if (isSvPickaxe && world.random.nextFloat() < 0.01f * luckMult) {
                        Item diamond = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "diamond"));
                        if (diamond != null) {
                            int diamondCount = 1;
                            // 地质学家：50%概率×最终运气再掉一个
                            if (SkillEffectHelper.hasGeologist(sw, player.getUuid()) && world.random.nextFloat() < 0.5f * luckMult) {
                                diamondCount = 2;
                            }
                            for (int di = 0; di < diamondCount; di++) {
                                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(diamond)));
                            }
                            // 钻石猎人（书本掉落）：每掉落一个星露谷钻石，0.8%起+0.1%每次，掉落后稳定1.5%
                            if (player instanceof ServerPlayerEntity sp) {
                                for (int di = 0; di < diamondCount; di++) {
                                    BookDataManager.rollProgressiveDrop(sw, sp, "the_diamond_hunter", 0.008f, 0.001f, 0.015f, luckMult);
                                }
                            }
                        }
                    }
                }

                // 0.1%概率掉落金色动物饼干（星露谷斧头砍原木，受最终运气影响）
                if (isLog) {
                    ItemStack held = player.getMainHandStack();
                    boolean isSvAxe = !held.isEmpty()
                        && StardewValley.MOD_ID.equals(Registries.ITEM.getId(held.getItem()).getNamespace())
                        && Registries.ITEM.getId(held.getItem()).getPath().contains("axe");
                    if (isSvAxe && world.random.nextFloat() < 0.001f * luckMult) {
                        Item cracker = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "golden_animal_cracker"));
                        if (cracker != null) {
                            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(cracker)));
                        }
                    }
                }

                // 天数>50后，星露谷斧头砍原木概率掉落迷之盒和金色迷之盒（独立判定，等概率，×最终运气）
                long currentDay = sw.getTimeOfDay() / 24000L;
                if (isLog && currentDay > 50) {
                    ItemStack held = player.getMainHandStack();
                    boolean isSvAxe = !held.isEmpty()
                        && StardewValley.MOD_ID.equals(Registries.ITEM.getId(held.getItem()).getNamespace())
                        && Registries.ITEM.getId(held.getItem()).getPath().contains("axe");
                    if (isSvAxe) {
                        float mysteryChance = 0.01f * luckMult;
                        // 迷之书：使用后概率×1.33
                        if (BookDataManager.get(sw).hasUsedBook(player.getUuid(), "book_of_mysteries")) {
                            mysteryChance *= 1.33f;
                        }
                        // 迷之盒（独立判定）
                        if (world.random.nextFloat() < mysteryChance) {
                            Item mysteryBox = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "mystery_box"));
                            if (mysteryBox != null) {
                                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(mysteryBox)));
                            }
                        }
                        // 金色迷之盒（独立判定，需采集精通）
                        if (stardewvalley.modid.skill.MasteryManager.hasMasteredSkill(sw, player.getUuid(),
                            stardewvalley.modid.skill.SkillRegistry.Category.FORAGING)
                            && world.random.nextFloat() < mysteryChance) {
                            Item goldenMysteryBox = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "golden_mystery_box"));
                            if (goldenMysteryBox != null) {
                                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(goldenMysteryBox)));
                            }
                        }
                    }
                }
            }
        );
    }

    private static final float[][] QUALITY_TABLE = {
        {1.00f, 0.00f, 0.00f},
        {0.90f, 0.07f, 0.03f},
        {0.81f, 0.12f, 0.07f},
        {0.72f, 0.18f, 0.10f},
        {0.64f, 0.23f, 0.13f},
        {0.55f, 0.28f, 0.17f},
        {0.48f, 0.32f, 0.20f},
        {0.41f, 0.36f, 0.23f},
        {0.34f, 0.39f, 0.27f},
        {0.28f, 0.42f, 0.30f},
        {0.22f, 0.45f, 0.33f},
        {0.17f, 0.46f, 0.37f},
        {0.12f, 0.48f, 0.40f},
        {0.08f, 0.49f, 0.43f},
        {0.03f, 0.50f, 0.47f},
        {0.00f, 0.50f, 0.50f},
    };

    private static CropQuality rollQuality(int level, net.minecraft.util.math.random.Random random, float luckMultiplier) {
        int idx = Math.min(level, 15);
        float[] probs = QUALITY_TABLE[idx];
        float goldChance = probs[2] * luckMultiplier;
        float silverChance = probs[1] * luckMultiplier;
        float roll = random.nextFloat();
        if (roll < goldChance) return CropQuality.GOLD;
        if (roll < goldChance + silverChance) return CropQuality.SILVER;
        return CropQuality.NORMAL;
    }

    private static final java.util.Map<String, Integer> ITEM_VALUES = java.util.Map.ofEntries(
        java.util.Map.entry("caiji_wildhorseradish", 50),
        java.util.Map.entry("caiji_daffodil", 30),
        java.util.Map.entry("caiji_leek", 60),
        java.util.Map.entry("caiji_dandelion", 40),
        java.util.Map.entry("caiji_springonion", 8),
        java.util.Map.entry("caiji_salmonberry", 5),
        java.util.Map.entry("caiji_spiceberry", 80),
        java.util.Map.entry("caiji_sweetpea", 50),
        java.util.Map.entry("caiji_fiddleheadfern", 90),
        java.util.Map.entry("caiji_wildplum", 80),
        java.util.Map.entry("caiji_hazelnut", 90),
        java.util.Map.entry("caiji_blackberry", 20),
        java.util.Map.entry("caiji_morel", 150),
        java.util.Map.entry("caiji_commonmushroom", 40),
        java.util.Map.entry("caiji_redmushroom", 75),
        java.util.Map.entry("caiji_purplemushroom", 250),
        java.util.Map.entry("caiji_chanterelle", 160),
        java.util.Map.entry("caiji_winterroot", 70),
        java.util.Map.entry("caiji_crystalfruit", 150),
        java.util.Map.entry("caiji_snowyam", 100),
        java.util.Map.entry("caiji_crocus", 60),
        java.util.Map.entry("caiji_holly", 80),
        java.util.Map.entry("caiji_cavecarrot", 25),
        java.util.Map.entry("caiji_nautilusshell", 120),
        java.util.Map.entry("caiji_coral", 80),
        java.util.Map.entry("caiji_seaurchin", 160),
        java.util.Map.entry("caiji_rainbow_shell", 300),
        java.util.Map.entry("caiji_clam", 50),
        java.util.Map.entry("caiji_cockle", 50),
        java.util.Map.entry("caiji_mussel", 30),
        java.util.Map.entry("caiji_oyster", 40),
        java.util.Map.entry("caiji_seaweed", 20),
        java.util.Map.entry("caiji_coconut", 100),
        java.util.Map.entry("caiji_ginger", 60),
        java.util.Map.entry("cactusfruit", 75),
        java.util.Map.entry("caiji_magmacap", 400)
    );

    private static final float ALL_ITEMS_TOTAL_PRICE;
    static {
        float sum = 0;
        for (int v : ITEM_VALUES.values()) sum += v;
        ALL_ITEMS_TOTAL_PRICE = sum;
    }

    private static String rollFromList(List<String> items, net.minecraft.util.math.random.Random random) {
        if (items.isEmpty()) return null;
        for (String id : items) {
            int price = ITEM_VALUES.getOrDefault(id, 50);
            float x = price / ALL_ITEMS_TOTAL_PRICE;
            float dropChance = (float)(Math.log(1.0 / x + 1) - 1) / 100.0f;
            if (dropChance > 0 && random.nextFloat() < dropChance) {
                return id;
            }
        }
        return null;
    }

    private static List<String> getSeasonalGrassItems(Season season) {
        return switch (season) {
            case SPRING -> List.of("caiji_wildhorseradish", "caiji_daffodil", "caiji_leek", "caiji_dandelion", "caiji_springonion", "caiji_salmonberry");
            case SUMMER -> List.of("caiji_spiceberry", "caiji_sweetpea", "caiji_fiddleheadfern");
            case FALL -> List.of("caiji_wildplum", "caiji_hazelnut", "caiji_blackberry");
            case WINTER -> List.of("caiji_winterroot", "caiji_crystalfruit", "caiji_snowyam", "caiji_crocus", "caiji_holly");
        };
    }

    private static List<String> getDarkStoneItems() {
        return List.of("caiji_redmushroom", "caiji_purplemushroom", "caiji_cavecarrot");
    }

    private static List<String> getMyceliumItems(Season season) {
        return switch (season) {
            case SPRING -> List.of("caiji_morel", "caiji_commonmushroom");
            case SUMMER -> List.of("caiji_commonmushroom", "caiji_redmushroom");
            case FALL -> List.of("caiji_commonmushroom", "caiji_chanterelle", "caiji_redmushroom", "caiji_purplemushroom");
            case WINTER -> List.of();
        };
    }

    private static List<String> getWaterItems() {
        return List.of("caiji_nautilusshell", "caiji_coral", "caiji_seaurchin", "caiji_rainbow_shell",
                "caiji_clam", "caiji_cockle", "caiji_mussel", "caiji_oyster", "caiji_seaweed");
    }

    private static List<String> getDesertItems() {
        return List.of("caiji_coconut", "cactusfruit");
    }

    private static List<String> getLogItems(Season season) {
        if (season == Season.SUMMER) {
            return List.of("caiji_fiddleheadfern");
        }
        return List.of();
    }

    private static List<String> getMushroomLogItems() {
        return List.of("caiji_redmushroom", "caiji_purplemushroom", "caiji_chanterelle", "caiji_commonmushroom", "caiji_morel");
    }
}