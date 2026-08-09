package stardewvalley.modid.crafting;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

import java.util.*;

/**
 * 集中管理所有合成配方的材料标签集合。
 * 所有 "any_xxx" / "item_xxx" 标签在此定义，全局适用（workbench / dishes / 服务端）。
 * 添加新物品时只需在此添加即可，无需修改各处散落的代码。
 *
 * 命名规范：
 *   any_xxx  = 一组物品的大类标签
 *   item_xxx = 单个物品（含所有品质变体）的标签
 */
public final class CraftingMaterialSets {

    private CraftingMaterialSets() {}

    // ========================================================================
    //  品质后缀
    // ========================================================================
    private static final String[] QUALITY_SUFFIXES = {"", "_silver", "_gold", "_iridium"};
    private static final Set<String> CROPS_NO_GOLD = Set.of("fiber", "qigua");

    // 只有普通品质的采集品（包含 sap）
    private static final Set<String> FORAGE_NO_QUALITY = Set.of("seaweed", "cavecarrot", "ginger", "sap");

    // 只有普通+银品质的鱼
    private static final Set<String> FISH_SILVER_ONLY = Set.of("lobster", "crayfish", "crab", "shrimp", "snail", "periwinkle");

    // 贝壳类注册为 caiji_ 前缀
    private static final Set<String> FISH_CAIJI_PREFIX = Set.of("cockle", "mussel", "oyster", "clam");

    // ========================================================================
    //  全部作物名称（与 ModItems.CROP_NAMES 一致）
    // ========================================================================
    private static final String[] ALL_CROP_NAMES = {
        "amaranth", "ancientfruit", "artichoke", "beet", "blueberry", "bluejazz",
        "bokchoy", "broccoli", "cactusfruit", "carrot", "cauliflower",
        "corn", "cranberries", "eggplant", "fairyrose", "fiber", "garlic", "grape",
        "greenbean", "hops", "hotpepper", "kale", "melon",
        "parsnip", "pineapple", "poppy", "potato", "powdermelon",
        "pumpkin", "qigua", "radish", "redcabbage", "rhubarb", "starfruit", "strawberry",
        "summerspangle", "summersquash", "sunflower", "sweetgemberry", "taroroot",
        "tealeaves", "tomato", "tulip", "unmilledrice", "wheat", "yam",
        "apple", "apricot", "banana", "cherry", "mango", "orange", "peach", "pomegranate"
    };

    // ========================================================================
    //  全部鱼名（与 ModItems.registerFishItems 使用的 FISH_PRICES keys 一致）
    // ========================================================================
    public static final String[] ALL_FISH_NAMES = {
        "pufferfish", "anchovy", "bream", "tuna", "sardine",
        "bullhead", "largemouth_bass", "smallmouth_bass", "rainbow_trout", "salmon",
        "walleye", "perch", "carp", "catfish", "pike",
        "sunfish", "red_mullet", "herring", "eel", "octopus",
        "red_snapper", "squid", "sea_cucumber", "super_cucumber", "ghostfish",
        "stonefish", "ice_pip", "lava_eel", "sandfish", "scorpion_carp",
        "flounder", "midnight_carp", "sturgeon", "tiger_trout", "tilapia",
        "chub", "dorado", "albacore", "shad", "lingcod",
        "halibut", "woodskip", "void_salmon", "slimejack", "stingray",
        "lionfish", "blue_discus", "goby", "midnight_squid", "spook_fish",
        "blobfish", "crimsonfish", "angler", "legend", "glacierfish",
        "mutant_carp", "son_of_crimsonfish", "ms._angler", "legend_ii", "glacierfish_jr",
        "radioactive_carp",
        "lobster", "crayfish", "crab", "shrimp", "snail", "periwinkle",
        "cockle", "mussel", "oyster", "clam"
    };

    // ========================================================================
    //  全部采集品名（与 ModItems.CAIJI_NAMES 一致）
    // ========================================================================
    private static final String[] ALL_FORAGE_NAMES = {
        "wildhorseradish", "daffodil", "leek", "dandelion", "springonion",
        "morel", "commonmushroom", "salmonberry", "spiceberry", "sweetpea",
        "fiddleheadfern", "wildplum", "hazelnut", "blackberry", "chanterelle",
        "redmushroom", "purplemushroom", "winterroot", "crystalfruit", "snowyam",
        "crocus", "holly", "nautilusshell", "coral", "seaurchin", "rainbow_shell",
        "clam", "cockle", "mussel", "oyster", "seaweed", "cavecarrot",
        "coconut", "ginger", "magmacap", "sap"
    };

    // ========================================================================
    //  全部菜品名（与 ModItems.DISH_NAMES 一致）
    // ========================================================================
    private static final String[] ALL_DISH_NAMES = {
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

    // 酒类基础名称（不含品质后缀）
    private static final String[] WINE_BASE_NAMES = {
        "ancient_fruit", "apple", "apricot", "banana", "blackberry",
        "blueberry", "cactus_fruit", "cherry", "coconut", "cranberries",
        "crystal_fruit", "grape", "hot_pepper", "mango", "melon",
        "orange", "peach", "pineapple", "pomegranate", "powdermelon",
        "qi_fruit", "rhubarb", "salmonberry", "spice_berry", "starfruit",
        "strawberry", "wild_plum"
    };

    // 果酱基础名称
    private static final String[] JELLY_BASE_NAMES = {
        "ancient_fruit", "apple", "apricot", "banana", "blackberry",
        "blueberry", "cactus_fruit", "cherry", "coconut", "cranberries",
        "crystal_fruit", "grape", "hot_pepper", "mango", "melon",
        "orange", "peach", "pineapple", "pomegranate", "powdermelon",
        "qi_fruit", "rhubarb", "salmonberry", "spice_berry", "starfruit",
        "strawberry", "wild_plum"
    };

    // 水果干基础名称
    private static final String[] DRIED_FRUIT_NAMES = {
        "ancient_fruit", "apple", "apricot", "banana", "blackberry",
        "blueberry", "cactus_fruit", "cherry", "coconut", "cranberries",
        "crystal_fruit", "hot_pepper", "mango", "melon",
        "orange", "peach", "pineapple", "pomegranate", "powdermelon",
        "qi_fruit", "rhubarb", "salmonberry", "spice_berry", "starfruit",
        "strawberry", "wild_plum"
    };
    private static final String[] DRIED_FRUIT_EXTRA = {"rasins"};

    // 果汁基础名称
    private static final String[] JUICE_NAMES = {
        "amaranth", "artichoke", "beet", "bok_choy", "broccoli",
        "carrot", "cauliflower", "corn", "eggplant", "fiddlehead_fern",
        "garlic", "green_bean", "kale", "parsnip", "potato",
        "pumpkin", "radish", "red_cabbage", "summer_squash", "taro_root",
        "tomato", "unmilled_rice", "yam"
    };

    // 腌菜基础名称
    private static final String[] PICKLE_NAMES = {
        "amaranth", "artichoke", "beet", "bok_choy", "broccoli",
        "carrot", "cauliflower", "corn", "eggplant", "fiddlehead_fern",
        "garlic", "green_bean", "hops", "kale", "parsnip",
        "potato", "pumpkin", "radish", "red_cabbage", "summer_squash",
        "taro_root", "tea_leaves", "tomato", "unmilled_rice", "wheat", "yam"
    };

    // 特殊酒类（啤酒/淡啤酒/蜂蜜酒/绿茶 + 品质）
    private static final String[] SPECIAL_DRINKS = {
        "beer", "beer_silver", "beer_gold", "beer_iridium",
        "pale_ale", "pale_ale_silver", "pale_ale_gold", "pale_ale_iridium",
        "mead", "mead_silver", "mead_gold", "mead_iridium",
        "green_tea"
    };

    // 蜂蜜种类
    private static final String[] HONEY_NAMES = {
        "honey", "tulip_honey", "blue_jazz_honey", "summer_spangle_honey",
        "poppy_honey", "sunflower_honey", "fairy_rose_honey"
    };

    // 奶酪（含品质变体）
    private static final String[] CHEESE_NAMES = {
        "cheese", "cheese_silver", "cheese_gold", "cheese_iridium"
    };

    // 山羊奶酪（含品质变体）
    private static final String[] GOAT_CHEESE_NAMES = {
        "goat_cheese", "goat_cheese_silver", "goat_cheese_gold", "goat_cheese_iridium"
    };

    // 蘑菇干
    private static final String[] DRIED_MUSHROOM_NAMES = {
        "dried_common_mushrooms", "dried_chanterelles", "dried_magma_caps",
        "dried_morels", "dried_purple_mushrooms"
    };

    // 徽章/戒指名称（与 ModItems.registerRingItems 一致）
    private static final String[] ALL_RING_NAMES = {
        "amethyst_ring", "aquamarine_ring", "burglars_ring", "crabshell_ring", "emerald_ring",
        "glow_ring", "glowstone_ring", "hot_java_ring", "immunity_band", "iridium_band",
        "jade_ring", "jukebox_ring", "lucky_ring", "magnet_ring", "napalm_ring",
        "phoenix_ring", "protection_ring", "ring_of_yoba", "ruby_ring", "savage_ring",
        "slime_charmer_ring", "small_glow_ring", "small_magnet_ring", "soul_sapper_ring",
        "sturdy_ring", "thorns_ring", "topaz_ring", "vampire_ring", "warrior_ring"
    };

    // 矿石和宝石名称（与 ModItems.registerKuangshiItems 一致）
    private static final String[] ORE_NAMES = {
        "coal", "copper_ore", "copper_bar", "iron_ore", "iron_bar",
        "gold_ore", "gold_bar", "iridium_ore", "iridium_bar",
        "radioactive_ore", "radioactive_bar"
    };
    private static final String[] GEM_NAMES = {
        "amethyst", "aquamarine", "diamond", "emerald", "earth_crystal",
        "fire_quartz", "frozen_geode", "geode", "jade", "magma_geode",
        "omni_geode", "prismatic_shard", "ruby", "topaz", "quartz",
        "tear_crystal", "refined_quartz"
    };
    private static final String[] GEODE_MINERAL_NAMES = {
        "tigerseye", "opal", "fire_opal", "alamite", "bixite", "baryte",
        "aerinite", "calcite", "dolomite", "esperite", "fluorapatite", "geminite",
        "helvite", "jamborite", "jagoite", "kyanite", "lunarite", "malachite",
        "neptunite", "lemon_stone", "nekoite", "orpiment", "petrified_slime", "thunder_egg",
        "pyrite", "ocean_stone", "ghost_crystal", "jasper", "celestine", "marble",
        "sandstone", "granite", "basalt", "limestone", "soapstone", "hematite",
        "mudstone", "obsidian", "slate", "fairy_stone", "star_shards"
    };

    // ========================================================================
    //  辅助：生成品质变体
    // ========================================================================
    private static Set<String> withQualities(String baseName) {
        Set<String> s = new LinkedHashSet<>();
        for (String suffix : QUALITY_SUFFIXES) {
            s.add(baseName + suffix);
        }
        return s;
    }

    private static Set<String> withFishQualities(String baseName) {
        Set<String> s = new LinkedHashSet<>();
        String prefix = FISH_CAIJI_PREFIX.contains(baseName) ? "caiji_" : "fish_";
        if (FISH_SILVER_ONLY.contains(baseName)) {
            s.add(prefix + baseName);
            s.add(prefix + baseName + "_silver");
        } else {
            for (String suffix : QUALITY_SUFFIXES) {
                s.add(prefix + baseName + suffix);
            }
        }
        return s;
    }

    private static Set<String> withForageQualities(String baseName) {
        Set<String> s = new LinkedHashSet<>();
        if (FORAGE_NO_QUALITY.contains(baseName)) {
            s.add("caiji_" + baseName);
        } else {
            for (String suffix : QUALITY_SUFFIXES) {
                s.add("caiji_" + baseName + suffix);
            }
        }
        return s;
    }

    // ========================================================================
    //  构建所有标签集
    // ========================================================================

    // ---- 1. 每种作物（含品质） ----
    public static final Map<String, Set<String>> CROP_TAGS;
    // ---- 2. 每种鱼（含品质） ----
    public static final Map<String, Set<String>> FISH_TAGS;
    // ---- 3. 每种采集品（含品质） ----
    public static final Map<String, Set<String>> FORAGE_TAGS;

    // ---- 大类标签 ----
    public static final Set<String> ANY_FISH;
    public static final Set<String> ANY_EGG;
    public static final Set<String> ANY_MILK;
    public static final Set<String> ANY_WILD_SEED;
    public static final Set<String> ANY_CROP;
    public static final Set<String> ANY_FORAGE;
    public static final Set<String> ANY_CAIJI;      // 同 ANY_FORAGE
    public static final Set<String> ANY_DISH;
    public static final Set<String> ANY_WINE;
    public static final Set<String> ANY_JELLY;
    public static final Set<String> ANY_JUICE;
    public static final Set<String> ANY_HONEY;
    public static final Set<String> ANY_CHEESE;
    public static final Set<String> ANY_GOAT_CHEESE;
    public static final Set<String> ANY_DRIED_MUSHROOM;
    public static final Set<String> ANY_AGED_ROE;
    public static final Set<String> ANY_DRIED_FRUIT;
    public static final Set<String> ANY_PICKLE;
    public static final Set<String> ANY_ROE;
    public static final Set<String> ANY_SMOKED_FISH;
    public static final Set<String> ANY_ORE_AND_GEM;
    public static final Set<String> ANY_RING;
    public static final Set<String> ANY_ARTISAN;
    public static final Set<String> ANY_RESIN;
    public static final Set<String> ANY_OIL;
    public static final Set<String> ANY_MINERAL;
    public static final Set<String> ANY_GEMSTONE;
    public static final Set<String> ANY_ANIMAL_PRODUCT;

    static {
        // ====== 作物标签 ======
        Map<String, Set<String>> cropTags = new LinkedHashMap<>();
        Set<String> allCrops = new LinkedHashSet<>();
        for (String name : ALL_CROP_NAMES) {
            boolean hasQualities = !CROPS_NO_GOLD.contains(name);
            if (hasQualities) {
                Set<String> variants = withQualities(name);
                cropTags.put("item_" + name, Collections.unmodifiableSet(variants));
                allCrops.addAll(variants);
            } else {
                cropTags.put("item_" + name, Collections.singleton(name));
                allCrops.add(name);
            }
        }
        CROP_TAGS = Collections.unmodifiableMap(cropTags);
        ANY_CROP = Collections.unmodifiableSet(allCrops);

        // ====== 鱼标签 ======
        Map<String, Set<String>> fishTags = new LinkedHashMap<>();
        Set<String> allFish = new LinkedHashSet<>();
        for (String name : ALL_FISH_NAMES) {
            Set<String> variants = withFishQualities(name);
            fishTags.put("item_" + name, Collections.unmodifiableSet(variants));
            allFish.addAll(variants);
        }
        FISH_TAGS = Collections.unmodifiableMap(fishTags);
        ANY_FISH = Collections.unmodifiableSet(allFish);

        // ====== 蛋标签 ======
        ANY_EGG = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "egg", "brown_egg", "duck_egg", "large_egg", "large_brown_egg", "void_egg", "gold_egg", "dinosaur_egg",
            "egg_silver", "brown_egg_silver", "duck_egg_silver", "large_egg_silver", "large_brown_egg_silver", "void_egg_silver", "gold_egg_silver", "dinosaur_egg_silver",
            "egg_gold", "brown_egg_gold", "duck_egg_gold", "large_egg_gold", "large_brown_egg_gold", "void_egg_gold", "gold_egg_gold", "dinosaur_egg_gold",
            "egg_iridium", "brown_egg_iridium", "duck_egg_iridium", "large_egg_iridium", "large_brown_egg_iridium", "void_egg_iridium", "gold_egg_iridium", "dinosaur_egg_iridium"
        )));

        // ====== 奶标签 ======
        ANY_MILK = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "milk", "large_milk", "goat_milk", "large_goat_milk",
            "milk_silver", "large_milk_silver", "goat_milk_silver", "large_goat_milk_silver",
            "milk_gold", "large_milk_gold", "goat_milk_gold", "large_goat_milk_gold",
            "milk_iridium", "large_milk_iridium", "goat_milk_iridium", "large_goat_milk_iridium"
        )));

        // ====== 野生种子标签 ======
        ANY_WILD_SEED = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "spring_seeds", "summer_seeds", "fall_seeds", "winter_seeds"
        )));

        // ====== 采集品标签 ======
        Map<String, Set<String>> forageTags = new LinkedHashMap<>();
        Set<String> allForage = new LinkedHashSet<>();
        for (String name : ALL_FORAGE_NAMES) {
            Set<String> variants = withForageQualities(name);
            forageTags.put("item_" + name, Collections.unmodifiableSet(variants));
            allForage.addAll(variants);
        }
        FORAGE_TAGS = Collections.unmodifiableMap(forageTags);
        ANY_FORAGE = Collections.unmodifiableSet(allForage);
        ANY_CAIJI = ANY_FORAGE;

        // ====== 菜品标签（含咖啡） ======
        Set<String> allDishes = new LinkedHashSet<>();
        for (String name : ALL_DISH_NAMES) {
            allDishes.add(name);
        }
        allDishes.add("coffee");
        ANY_DISH = Collections.unmodifiableSet(allDishes);

        // ====== 果酒标签（含品质变体） ======
        Set<String> wines = new LinkedHashSet<>();
        for (String base : WINE_BASE_NAMES) {
            wines.add(base + "_wine");
            wines.add(base + "_wine_silver");
            wines.add(base + "_wine_gold");
            wines.add(base + "_wine_iridium");
        }
        wines.addAll(Arrays.asList(SPECIAL_DRINKS));
        ANY_WINE = Collections.unmodifiableSet(wines);

        // ====== 果酱标签 ======
        Set<String> jellies = new LinkedHashSet<>();
        for (String base : JELLY_BASE_NAMES) {
            jellies.add(base + "_jelly");
        }
        ANY_JELLY = Collections.unmodifiableSet(jellies);

        // ====== 果汁标签 ======
        Set<String> juices = new LinkedHashSet<>();
        for (String base : JUICE_NAMES) {
            juices.add(base + "_juice");
        }
        ANY_JUICE = Collections.unmodifiableSet(juices);

        // ====== 蜂蜜标签 ======
        ANY_HONEY = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(HONEY_NAMES)));

        // ====== 奶酪标签 ======
        ANY_CHEESE = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(CHEESE_NAMES)));

        // ====== 山羊奶酪标签 ======
        ANY_GOAT_CHEESE = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(GOAT_CHEESE_NAMES)));

        // ====== 蘑菇干标签 ======
        ANY_DRIED_MUSHROOM = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(DRIED_MUSHROOM_NAMES)));

        // ====== 水果干标签 ======
        Set<String> driedFruits = new LinkedHashSet<>();
        for (String base : DRIED_FRUIT_NAMES) {
            driedFruits.add("dried_" + base);
        }
        driedFruits.addAll(Arrays.asList(DRIED_FRUIT_EXTRA));
        ANY_DRIED_FRUIT = Collections.unmodifiableSet(driedFruits);

        // ====== 腌菜标签 ======
        Set<String> pickles = new LinkedHashSet<>();
        for (String base : PICKLE_NAMES) {
            pickles.add(base + "_pickles");
        }
        ANY_PICKLE = Collections.unmodifiableSet(pickles);

        // ====== 新鲜鱼籽标签 ======
        // 从 ModItems.registerRoeItems 获取所有鱼籽名称
        Set<String> roeSet = new LinkedHashSet<>();
        for (String fish : ALL_FISH_NAMES) {
            String roeName;
            if (fish.equals("ms._angler")) {
                roeName = "ms._angler_roe";
            } else if (fish.equals("glacierfish_jr")) {
                roeName = "glacierfish_jr._roe";
            } else if (fish.equals("sea_urchin")) {
                roeName = "sea_urchin_roe";
            } else {
                roeName = fish + "_roe";
            }
            roeSet.add(roeName);
        }
        ANY_ROE = Collections.unmodifiableSet(roeSet);

        // ====== 腌制鱼籽标签 ======
        Set<String> agedRoeSet = new LinkedHashSet<>();
        for (String roeName : roeSet) {
            agedRoeSet.add("aged_" + roeName);
        }
        ANY_AGED_ROE = Collections.unmodifiableSet(agedRoeSet);

        // ====== 熏鱼标签（含品质变体） ======
        Set<String> smokedFish = new LinkedHashSet<>();
        for (String fish : ALL_FISH_NAMES) {
            if (FISH_SILVER_ONLY.contains(fish)) {
                smokedFish.add("smoked_" + fish);
                smokedFish.add("smoked_" + fish + "_sliver"); // 注意 ModItems 中是 _sliver 不是 _silver
            } else {
                smokedFish.add("smoked_" + fish);
                smokedFish.add("smoked_" + fish + "_sliver");
                smokedFish.add("smoked_" + fish + "_gold");
                smokedFish.add("smoked_" + fish + "_iridium");
            }
        }
        ANY_SMOKED_FISH = Collections.unmodifiableSet(smokedFish);

        // ====== 矿石和宝石标签 ======
        Set<String> oreAndGem = new LinkedHashSet<>();
        oreAndGem.addAll(Arrays.asList(ORE_NAMES));
        oreAndGem.addAll(Arrays.asList(GEM_NAMES));
        ANY_ORE_AND_GEM = Collections.unmodifiableSet(oreAndGem);

        // ====== 矿物标签（铁匠加成用） ======
        ANY_MINERAL = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "coal", "copper_ore", "copper_bar", "iron_ore", "iron_bar",
            "gold_ore", "gold_bar", "iridium_ore", "iridium_bar",
            "radioactive_ore", "radioactive_bar"
        )));

        // ====== 宝石标签（宝石专家加成用） ======
        Set<String> gemstoneSet = new LinkedHashSet<>();
        gemstoneSet.addAll(Arrays.asList(GEM_NAMES));
        gemstoneSet.addAll(Arrays.asList(GEODE_MINERAL_NAMES));
        ANY_GEMSTONE = Collections.unmodifiableSet(gemstoneSet);

        // ====== 动物制品标签（畜牧人加成用，不含奶酪和蛋黄酱） ======
        Set<String> animalProductSet = new LinkedHashSet<>();
        // 蛋（所有品质）
        animalProductSet.addAll(Arrays.asList(
            "egg", "brown_egg", "duck_egg", "large_egg", "large_brown_egg", "void_egg", "gold_egg", "dinosaur_egg",
            "egg_silver", "brown_egg_silver", "duck_egg_silver", "large_egg_silver", "large_brown_egg_silver", "void_egg_silver", "gold_egg_silver", "dinosaur_egg_silver",
            "egg_gold", "brown_egg_gold", "duck_egg_gold", "large_egg_gold", "large_brown_egg_gold", "void_egg_gold", "gold_egg_gold", "dinosaur_egg_gold",
            "egg_iridium", "brown_egg_iridium", "duck_egg_iridium", "large_egg_iridium", "large_brown_egg_iridium", "void_egg_iridium", "gold_egg_iridium", "dinosaur_egg_iridium"
        ));
        // 奶（所有品质）
        animalProductSet.addAll(Arrays.asList(
            "milk", "large_milk", "goat_milk", "large_goat_milk",
            "milk_silver", "large_milk_silver", "goat_milk_silver", "large_goat_milk_silver",
            "milk_gold", "large_milk_gold", "goat_milk_gold", "large_goat_milk_gold",
            "milk_iridium", "large_milk_iridium", "goat_milk_iridium", "large_goat_milk_iridium"
        ));
        // 毛、鸭毛、兔子脚、松露（所有品质）
        String[] animalBaseNames = {"wool", "duck_feather", "rabbits_foot", "truffle"};
        for (String base : animalBaseNames) {
            animalProductSet.add(base);
            animalProductSet.add(base + "_silver");
            animalProductSet.add(base + "_gold");
            animalProductSet.add(base + "_iridium");
        }
        ANY_ANIMAL_PRODUCT = Collections.unmodifiableSet(animalProductSet);

        // ====== 戒指标签 ======
        ANY_RING = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(ALL_RING_NAMES)));

        // ====== 工匠物品标签 ======
        Set<String> artisan = new LinkedHashSet<>();
        artisan.addAll(ANY_JELLY);
        artisan.addAll(ANY_JUICE);
        artisan.addAll(ANY_HONEY);
        artisan.addAll(ANY_CHEESE);
        artisan.addAll(ANY_GOAT_CHEESE);
        artisan.addAll(ANY_DRIED_MUSHROOM);
        artisan.addAll(ANY_AGED_ROE);
        artisan.addAll(ANY_DRIED_FRUIT);
        artisan.addAll(ANY_PICKLE);
        artisan.addAll(ANY_SMOKED_FISH);
        artisan.addAll(Arrays.asList(
            "mayonnaise", "duck_mayonnaise", "gold_mayonnaise", "void_mayonnaise", "dinosaur_mayonnaise",
            "mayonnaise_silver", "duck_mayonnaise_silver", "void_mayonnaise_silver", "dinosaur_mayonnaise_silver",
            "mayonnaise_gold", "duck_mayonnaise_gold", "void_mayonnaise_gold", "dinosaur_mayonnaise_gold",
            "mayonnaise_iridium", "duck_mayonnaise_iridium", "void_mayonnaise_iridium", "dinosaur_mayonnaise_iridium",
            "truffle_oil"
        ));
        ANY_ARTISAN = Collections.unmodifiableSet(artisan);

        // ====== 树脂产品 ======
        Set<String> resin = new LinkedHashSet<>(Arrays.asList(
            "maple_syrup", "oak_resin", "pine_tar", "caiji_sap"
        ));
        // 加入所有蘑菇类采集品
        for (String name : ALL_FORAGE_NAMES) {
            if (name.endsWith("mushroom") || name.equals("chanterelle") || name.equals("morel") || name.equals("magmacap") || name.equals("fiddleheadfern")) {
                for (String suffix : QUALITY_SUFFIXES) {
                    resin.add("caiji_" + name + suffix);
                }
            }
        }
        ANY_RESIN = Collections.unmodifiableSet(resin);

        // ====== 油标签 ======
        Set<String> oil = new LinkedHashSet<>(Arrays.asList(
            "oil", "truffle_oil"
        ));
        ANY_OIL = Collections.unmodifiableSet(oil);
    }

    // ========================================================================
    //  工具方法
    // ========================================================================

    /**
     * 获取指定标签对应的物品 ID 集合。
     * 支持：大类标签（any_xxx）、单个物品标签（item_xxx）、以及直接物品 ID。
     */
    public static Set<String> getTagSet(String itemId) {
        if (itemId == null) return null;

        // 先查大类标签
        Set<String> result = getGroupTag(itemId);
        if (result != null) return result;

        // 查单个物品标签（item_xxx）
        if (itemId.startsWith("item_")) {
            String itemName = itemId.substring(5);
            // 作物
            if (CROP_TAGS.containsKey(itemId)) return CROP_TAGS.get(itemId);
            // 鱼 - item_pufferfish 等
            Set<String> fishSet = FISH_TAGS.get("item_" + itemName); // 直接取 itemName 作为 fish name
            if (fishSet != null) return fishSet;
            // 采集品
            Set<String> forageSet = FORAGE_TAGS.get("item_" + itemName);
            if (forageSet != null) return forageSet;
        }

        // 检查是否为 item_xxx 但 xxx 是 crop/fish/forage 名
        for (String crop : ALL_CROP_NAMES) {
            if (("item_" + crop).equals(itemId)) {
                Set<String> s = CROP_TAGS.get(itemId);
                if (s != null) return s;
            }
        }
        for (String fish : ALL_FISH_NAMES) {
            if (("item_" + fish).equals(itemId)) {
                Set<String> s = FISH_TAGS.get(itemId);
                if (s != null) return s;
            }
        }
        for (String forage : ALL_FORAGE_NAMES) {
            if (("item_" + forage).equals(itemId)) {
                Set<String> s = FORAGE_TAGS.get(itemId);
                if (s != null) return s;
            }
        }

        return null;
    }

    private static Set<String> getGroupTag(String itemId) {
        return switch (itemId) {
            case "any_fish" -> ANY_FISH;
            case "any_egg" -> ANY_EGG;
            case "any_milk" -> ANY_MILK;
            case "any_wild_seed" -> ANY_WILD_SEED;
            case "any_crop" -> ANY_CROP;
            case "any_forage", "any_caiji" -> ANY_FORAGE;
            case "any_dish" -> ANY_DISH;
            case "any_wine" -> ANY_WINE;
            case "any_jelly" -> ANY_JELLY;
            case "any_juice" -> ANY_JUICE;
            case "any_honey" -> ANY_HONEY;
            case "any_cheese" -> ANY_CHEESE;
            case "any_goat_cheese" -> ANY_GOAT_CHEESE;
            case "any_dried_mushroom" -> ANY_DRIED_MUSHROOM;
            case "any_aged_roe" -> ANY_AGED_ROE;
            case "any_dried_fruit" -> ANY_DRIED_FRUIT;
            case "any_pickle" -> ANY_PICKLE;
            case "any_roe" -> ANY_ROE;
            case "any_smoked_fish" -> ANY_SMOKED_FISH;
            case "any_ore_and_gem" -> ANY_ORE_AND_GEM;
            case "any_ring" -> ANY_RING;
            case "any_artisan" -> ANY_ARTISAN;
            case "any_resin" -> ANY_RESIN;
            case "any_oil" -> ANY_OIL;
            case "any_mineral" -> ANY_MINERAL;
            case "any_gemstone" -> ANY_GEMSTONE;
            case "any_animal_product" -> ANY_ANIMAL_PRODUCT;
            default -> null;
        };
    }

    /**
     * 大类标签（any_xxx）的中文显示名。若返回 null，则界面回退到"任意 + 代表物品名"。
     */
    public static String getTagDisplayName(String itemId) {
        if (itemId == null) return null;
        return switch (itemId) {
            case "any_fish" -> "任意鱼";
            case "any_egg" -> "任意蛋";
            case "any_milk" -> "任意奶";
            case "any_wild_seed" -> "任意野生种子";
            case "any_crop" -> "任意作物";
            case "any_forage", "any_caiji" -> "任意采集品";
            case "any_dish" -> "任意菜肴";
            case "any_wine" -> "任意果酒";
            case "any_jelly" -> "任意果酱";
            case "any_juice" -> "任意果汁";
            case "any_honey" -> "任意蜂蜜";
            case "any_cheese" -> "任意奶酪";
            case "any_goat_cheese" -> "任意山羊奶酪";
            case "any_dried_mushroom" -> "任意干蘑菇";
            case "any_aged_roe" -> "任意陈年鱼籽";
            case "any_dried_fruit" -> "任意干果";
            case "any_pickle" -> "任意腌菜";
            case "any_roe" -> "任意鱼籽";
            case "any_smoked_fish" -> "任意熏鱼";
            case "any_ore_and_gem" -> "任意矿石与宝石";
            case "any_ring" -> "任意戒指";
            case "any_artisan" -> "任意工匠制品";
            case "any_resin" -> "任意树脂";
            case "any_oil" -> "任意油";
            case "any_mineral" -> "任意矿物";
            case "any_gemstone" -> "任意宝石";
            case "any_animal_product" -> "任意动物制品";
            default -> null;
        };
    }

    /**
     * 获取某物品的所有品质变体（包括普通品质）。
     */
    public static Set<String> getQualityVariants(String baseItem) {
        // 先检查是否为已知作物/鱼/采集品
        for (String crop : ALL_CROP_NAMES) {
            if (crop.equals(baseItem)) {
                if (CROPS_NO_GOLD.contains(baseItem)) {
                    return Collections.singleton(baseItem);
                }
                return withQualities(baseItem);
            }
        }
        for (String fish : ALL_FISH_NAMES) {
            if (fish.equals(baseItem)) {
                return withFishQualities(baseItem);
            }
        }
        for (String forage : ALL_FORAGE_NAMES) {
            if (forage.equals(baseItem)) {
                return withForageQualities(baseItem);
            }
        }
        // 未知物品：假设有品质变体
        return new LinkedHashSet<>(Arrays.asList(
            baseItem,
            baseItem + "_silver",
            baseItem + "_gold",
            baseItem + "_iridium"
        ));
    }

    /**
     * 获取标签集合中按字母顺序排列的第一个物品 ID，用于UI显示图标。
     */
    public static String getFirstItemForDisplay(String itemId) {
        Set<String> tagSet = getTagSet(itemId);
        if (tagSet == null) return itemId;
        TreeSet<String> sorted = new TreeSet<>(tagSet);
        return sorted.first();
    }

    /**
     * 统计玩家背包中符合某物品 ID（支持标签和品质变体）的总数量。
     */
    public static int countItemInInventory(String itemId, net.minecraft.client.MinecraftClient client) {
        if (client == null || client.player == null) return 0;

        Set<String> ids = getTagSet(itemId);
        if (ids != null) return countMatching(ids, client);

        if (!itemId.matches("[a-z0-9/._-]+")) return 0;
        Set<String> variants = getQualityVariants(itemId);
        return countMatching(variants, client);
    }

    private static int countMatching(Set<String> ids, net.minecraft.client.MinecraftClient client) {
        int count = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            net.minecraft.item.ItemStack stack = client.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                String path = Registries.ITEM.getId(stack.getItem()).getPath();
                if (ids.contains(path)) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    public static boolean hasEnoughInInventory(String itemId, int required, net.minecraft.server.network.ServerPlayerEntity player) {
        return countItemServer(itemId, player) >= required;
    }

    public static int countItemServer(String itemId, net.minecraft.server.network.ServerPlayerEntity player) {
        if (player == null) return 0;

        Set<String> ids = getTagSet(itemId);
        if (ids != null) return countMatchingServer(ids, player);

        if (!itemId.matches("[a-z0-9/._-]+")) return 0;
        Set<String> variants = getQualityVariants(itemId);
        return countMatchingServer(variants, player);
    }

    private static int countMatchingServer(Set<String> ids, net.minecraft.server.network.ServerPlayerEntity player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            net.minecraft.item.ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                String path = Registries.ITEM.getId(stack.getItem()).getPath();
                if (ids.contains(path)) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    public static int removeFromInventory(String itemId, int toRemove, net.minecraft.server.network.ServerPlayerEntity player) {
        if (player == null || toRemove <= 0) return 0;

        Set<String> ids = getTagSet(itemId);
        if (ids != null) return removeMatching(ids, toRemove, player);

        if (!itemId.matches("[a-z0-9/._-]+")) return 0;
        Set<String> variants = getQualityVariants(itemId);
        return removeMatching(variants, toRemove, player);
    }

    private static int removeMatching(Set<String> ids, int toRemove, net.minecraft.server.network.ServerPlayerEntity player) {
        int removed = 0;
        for (int i = 0; i < player.getInventory().size() && toRemove > 0; i++) {
            net.minecraft.item.ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                String path = Registries.ITEM.getId(stack.getItem()).getPath();
                if (ids.contains(path)) {
                    int deduct = Math.min(toRemove, stack.getCount());
                    stack.decrement(deduct);
                    toRemove -= deduct;
                    removed += deduct;
                }
            }
        }
        return removed;
    }

    /**
     * 获取指定物品 ID 对应的 Item 对象（如果是标签，返回集合中第一个物品；否则直接查找）。
     */
    public static Item getItemForDisplay(String itemId) {
        Set<String> idSet = getTagSet(itemId);
        if (idSet != null && !idSet.isEmpty()) {
            TreeSet<String> sorted = new TreeSet<>(idSet);
            String firstId = sorted.first();
            if (firstId.contains(":")) {
                return Registries.ITEM.get(Identifier.of(firstId));
            }
            return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, firstId));
        }
        if (itemId.contains(":")) {
            return Registries.ITEM.get(Identifier.of(itemId));
        }
        return Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, itemId));
    }
}
