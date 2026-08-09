package stardewvalley.modid.block;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import stardewvalley.modid.crop.CropQuality;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum MachineType {
    BEE_HOUSE((data -> null)),
    CASK((data -> null)),
    KEG((data -> {
        String inputId = data.inputId;
        CropQuality quality = data.quality;
        int count = data.count;

        if (isFruitCrop(inputId) && count >= 1) {
            String baseName = getBaseCropName(inputId);
            String fruitToWine = switch (baseName) {
                case "apple" -> "apple_wine";
                case "apricot" -> "apricot_wine";
                case "banana" -> "banana_wine";
                case "cherry" -> "cherry_wine";
                case "mango" -> "mango_wine";
                case "orange" -> "orange_wine";
                case "peach" -> "peach_wine";
                case "pomegranate" -> "pomegranate_wine";
                case "ancientfruit" -> "ancient_fruit_wine";
                case "blueberry" -> "blueberry_wine";
                case "cactusfruit" -> "cactus_fruit_wine";
                case "cranberries" -> "cranberries_wine";
                case "crystalfruit" -> "crystal_fruit_wine";
                case "grape" -> "grape_wine";
                case "hotpepper" -> "hot_pepper_wine";
                case "melon" -> "melon_wine";
                case "pineapple" -> "pineapple_wine";
                case "powdermelon" -> "powdermelon_wine";
                case "qi_fruit" -> "qi_fruit_wine";
                case "rhubarb" -> "rhubarb_wine";
                case "salmonberry" -> "salmonberry_wine";
                case "spiceberry" -> "spice_berry_wine";
                case "starfruit" -> "starfruit_wine";
                case "strawberry" -> "strawberry_wine";
                case "wildplum" -> "wild_plum_wine";
                case "blackberry" -> "blackberry_wine";
                case "coconut" -> "coconut_wine";
                case "qigua" -> "qi_fruit_wine";
                default -> null;
            };
            if (fruitToWine != null) return new RecipeResult(fruitToWine, minToTicks(8000), 1, 1);
        }
        if (isVegetableCrop(inputId) && count >= 1) {
            String baseName = getBaseCropName(inputId);
            String vegToJuice = switch (baseName) {
                case "amaranth" -> "amaranth_juice";
                case "artichoke" -> "artichoke_juice";
                case "beet" -> "beet_juice";
                case "bokchoy" -> "bok_choy_juice";
                case "broccoli" -> "broccoli_juice";
                case "carrot" -> "carrot_juice";
                case "cauliflower" -> "cauliflower_juice";
                case "corn" -> "corn_juice";
                case "eggplant" -> "eggplant_juice";
                case "garlic" -> "garlic_juice";
                case "greenbean" -> "green_bean_juice";
                case "kale" -> "kale_juice";
                case "parsnip" -> "parsnip_juice";
                case "potato" -> "potato_juice";
                case "pumpkin" -> "pumpkin_juice";
                case "radish" -> "radish_juice";
                case "redcabbage" -> "red_cabbage_juice";
                case "summersquash" -> "summer_squash_juice";
                case "taroroot" -> "taro_root_juice";
                case "tomato" -> "tomato_juice";
                case "unmilledrice" -> "unmilled_rice_juice";
                case "yam" -> "yam_juice";
                case "fiddleheadfern" -> "fiddlehead_fern_juice";
                default -> null;
            };
            if (vegToJuice != null) return new RecipeResult(vegToJuice, minToTicks(6000), 1, 1);
        }
        if (getBaseCropName(inputId).equals("wheat") && count >= 1) return new RecipeResult("beer", minToTicks(1750), 1, 1);
        if (getBaseCropName(inputId).equals("hops") && count >= 1) return new RecipeResult("pale_ale", minToTicks(2250), 1, 1);
        if ((getBaseCropName(inputId).equals("tealeaves") || getBaseCropName(inputId).equals("tea_leaves")) && count >= 1) return new RecipeResult("green_tea", minToTicks(180), 1, 1);
        if (inputId.startsWith("stardewvalley:") && inputId.contains("honey") && count >= 1) return new RecipeResult("mead", minToTicks(600), 1, 1);
        if (getBaseCropName(inputId).equals("coffeebean") && count >= 5) return new RecipeResult("coffee", minToTicks(120), 1, 5);
        if ((getBaseCropName(inputId).equals("unmilledrice") || getBaseCropName(inputId).equals("rice")) && count >= 1) return new RecipeResult("vinegar", minToTicks(600), 1, 1);
        return null;
    })),

    CHEESE_PRESS((data -> {
        if (data.count < 1) return null;
        String baseName = getBaseCropName(data.inputId);
        if (baseName.equals("milk")) return new RecipeResult("cheese", minToTicks(200), 1, 1);
        if (baseName.equals("goat_milk")) return new RecipeResult("goat_cheese", minToTicks(200), 1, 1);
        if (baseName.equals("large_milk")) return new RecipeResult("cheese_gold", minToTicks(200), 1, 1);
        if (baseName.equals("large_goat_milk")) return new RecipeResult("goat_cheese_gold", minToTicks(200), 1, 1);
        return null;
    })),

    MAYONNAISE_MACHINE((data -> {
        if (data.count < 1) return null;
        String baseName = getBaseCropName(data.inputId);
        if (baseName.equals("egg") || baseName.equals("brown_egg")) return new RecipeResult("mayonnaise", minToTicks(180), 1, 1);
        if (baseName.equals("large_egg") || baseName.equals("large_brown_egg")) return new RecipeResult("gold_mayonnaise", minToTicks(180), 3, 1);
        if (baseName.equals("gold_egg")) return new RecipeResult("gold_mayonnaise", minToTicks(180), 3, 1);
        if (baseName.equals("void_egg")) return new RecipeResult("void_mayonnaise", minToTicks(180), 1, 1);
        if (baseName.equals("duck_egg")) return new RecipeResult("duck_mayonnaise", minToTicks(180), 1, 1);
        if (baseName.equals("dinosaur_egg")) return new RecipeResult("dinosaur_mayonnaise", minToTicks(180), 1, 1);
        return null;
    })),

    PRESERVES_JAR((data -> {
        String inputId = data.inputId;
        int count = data.count;

        if (isFruitCrop(inputId) && count >= 1) {
            String baseName = getBaseCropName(inputId);
            String fruitToJelly = switch (baseName) {
                case "apple" -> "apple_jelly";
                case "apricot" -> "apricot_jelly";
                case "banana" -> "banana_jelly";
                case "cherry" -> "cherry_jelly";
                case "mango" -> "mango_jelly";
                case "orange" -> "orange_jelly";
                case "peach" -> "peach_jelly";
                case "pomegranate" -> "pomegranate_jelly";
                case "ancientfruit" -> "ancient_fruit_jelly";
                case "blueberry" -> "blueberry_jelly";
                case "cactusfruit" -> "cactus_fruit_jelly";
                case "cranberries" -> "cranberries_jelly";
                case "crystalfruit" -> "crystal_fruit_jelly";
                case "grape" -> "grape_jelly";
                case "hotpepper" -> "hot_pepper_jelly";
                case "melon" -> "melon_jelly";
                case "pineapple" -> "pineapple_jelly";
                case "powdermelon" -> "powdermelon_jelly";
                case "qi_fruit" -> "qi_fruit_jelly";
                case "rhubarb" -> "rhubarb_jelly";
                case "salmonberry" -> "salmonberry_jelly";
                case "spiceberry" -> "spice_berry_jelly";
                case "starfruit" -> "starfruit_jelly";
                case "strawberry" -> "strawberry_jelly";
                case "wildplum" -> "wild_plum_jelly";
                case "blackberry" -> "blackberry_jelly";
                case "coconut" -> "coconut_jelly";
                case "qigua" -> "qi_fruit_jelly";
                default -> null;
            };
            if (fruitToJelly != null) return new RecipeResult(fruitToJelly, minToTicks(4000), 1, 1);
        }
        if (isVegetableCrop(inputId) && count >= 1) {
            String baseName = getBaseCropName(inputId);
            String vegToPickle = switch (baseName) {
                case "amaranth" -> "amaranth_pickles";
                case "artichoke" -> "artichoke_pickles";
                case "beet" -> "beet_pickles";
                case "bokchoy" -> "bok_choy_pickles";
                case "broccoli" -> "broccoli_pickles";
                case "carrot" -> "carrot_pickles";
                case "cauliflower" -> "cauliflower_pickles";
                case "corn" -> "corn_pickles";
                case "eggplant" -> "eggplant_pickles";
                case "garlic" -> "garlic_pickles";
                case "greenbean" -> "green_bean_pickles";
                case "hops" -> "hops_pickles";
                case "kale" -> "kale_pickles";
                case "parsnip" -> "parsnip_pickles";
                case "potato" -> "potato_pickles";
                case "pumpkin" -> "pumpkin_pickles";
                case "radish" -> "radish_pickles";
                case "redcabbage" -> "red_cabbage_pickles";
                case "summersquash" -> "summer_squash_pickles";
                case "taroroot" -> "taro_root_pickles";
                case "tealeaves" -> "tea_leaves_pickles";
                case "tomato" -> "tomato_pickles";
                case "unmilledrice" -> "unmilled_rice_pickles";
                case "yam" -> "yam_pickles";
                case "fiddleheadfern" -> "fiddlehead_fern_pickles";
                default -> null;
            };
            if (vegToPickle != null) return new RecipeResult(vegToPickle, minToTicks(4000), 1, 1);
        }
        if (getBaseCropName(inputId).equals("wheat") && count >= 1) return new RecipeResult("wheat_pickles", minToTicks(4000), 1, 1);
        // 鱼籽 → 腌制鱼籽（鲟鱼籽特殊处理为鱼籽酱）
        String baseName = getBaseCropName(inputId);
        if (baseName.endsWith("_roe")) {
            if (baseName.equals("sturgeon_roe")) {
                return new RecipeResult("caviar", minToTicks(6000), 1, 1);
            }
            return new RecipeResult("aged_" + baseName, minToTicks(4000), 1, 1);
        }
        return null;
    })),

    OIL_MAKER((data -> {
        if (data.count < 1) return null;
        String baseName = getBaseCropName(data.inputId);
        if (baseName.equals("corn")) return new RecipeResult("oil", minToTicks(1000), 1, 1);
        if (baseName.equals("sunflower")) return new RecipeResult("oil", hrToTicks(1), 1, 1);
        if (baseName.contains("sunflower_seeds")) return new RecipeResult("oil", minToTicks(3200), 1, 1);
        if (baseName.equals("truffle")) return new RecipeResult("truffle_oil", minToTicks(360), 1, 1);
        return null;
    })),

    DEHYDRATOR((data -> {
        if (data.count < 5) return null;
        String baseName = getBaseCropName(data.inputId);
        String driedOutput = switch (baseName) {
            case "apple" -> "dried_apple";
            case "ancientfruit" -> "dried_ancient_fruit";
            case "apricot" -> "dried_apricot";
            case "banana" -> "dried_banana";
            case "blackberry" -> "dried_blackberry";
            case "blueberry" -> "dried_blueberry";
            case "cactusfruit" -> "dried_cactus_fruit";
            case "cherry" -> "dried_cherry";
            case "coconut" -> "dried_coconut";
            case "cranberries" -> "dried_cranberries";
            case "crystalfruit" -> "dried_crystal_fruit";
            case "grape" -> "rasins";
            case "hotpepper" -> "dried_hot_pepper";
            case "mango" -> "dried_mango";
            case "melon" -> "dried_melon";
            case "orange" -> "dried_orange";
            case "peach" -> "dried_peach";
            case "pineapple" -> "dried_pineapple";
            case "pomegranate" -> "dried_pomegranate";
            case "powdermelon" -> "dried_powdermelon";
            case "qi_fruit" -> "dried_qi_fruit";
            case "qigua" -> "dried_qi_fruit";
            case "rhubarb" -> "dried_rhubarb";
            case "salmonberry" -> "dried_salmonberry";
            case "spiceberry" -> "dried_spice_berry";
            case "starfruit" -> "dried_starfruit";
            case "strawberry" -> "dried_strawberry";
            case "wildplum" -> "dried_wild_plum";
            case "commonmushroom" -> "dried_common_mushrooms";
            case "chanterelle" -> "dried_chanterelles";
            case "magmacap" -> "dried_magma_caps";
            case "morel" -> "dried_morels";
            case "purplemushroom" -> "dried_purple_mushrooms";
            default -> null;
        };
        if (driedOutput != null) return new RecipeResult(driedOutput, minToTicks(1440), 1, 5);
        return null;
    })),

    FISH_SMOKER((data -> {
        String inputId = data.inputId;
        CropQuality quality = data.quality;
        int count = data.count;

        // 只接受鱼和贝壳类
        if (!inputId.startsWith("stardewvalley:fish_") && !inputId.startsWith("stardewvalley:caiji_")) return null;
        if (count < 1) return null;

        String baseName = getBaseCropName(inputId);
        String qualitySuffix = switch (quality) {
            case NORMAL -> "";
            case SILVER -> "_sliver";
            case GOLD -> "_gold";
            case IRIDIUM -> "_iridium";
        };

        String outputId = "smoked_" + baseName + qualitySuffix;
        // 检查输出物品是否存在，不存在则不加工
        Identifier outIdentifier = Identifier.of("stardewvalley", outputId);
        if (Registries.ITEM.get(outIdentifier) == net.minecraft.item.Items.AIR) {
            return null;
        }

        // 时间：50分钟 = 1000 ticks，消耗1个鱼
        return new RecipeResult(outputId, minToTicks(50), 1, 1);
    })),

    LOOM((data -> {
        if (data.count < 1) return null;
        String baseName = getBaseCropName(data.inputId);
        if (baseName.equals("wool")) return new RecipeResult("cloth", minToTicks(240), 1, 1);
        return null;
    })),

    FURNACE((data -> {
        String inputId = data.inputId;
        int count = data.count;

        if (!inputId.startsWith("stardewvalley:")) return null;
        String baseName = getBaseCropName(inputId);

        return switch (baseName) {
            case "copper_ore" -> count >= 5 ? new RecipeResult("copper_bar", minToTicks(30), 1, 5) : null;
            case "iron_ore" -> count >= 5 ? new RecipeResult("iron_bar", minToTicks(120), 1, 5) : null;
            case "gold_ore" -> count >= 5 ? new RecipeResult("gold_bar", minToTicks(300), 1, 5) : null;
            case "iridium_ore" -> count >= 5 ? new RecipeResult("iridium_bar", minToTicks(480), 1, 5) : null;
            case "radioactive_ore" -> count >= 5 ? new RecipeResult("radioactive_bar", minToTicks(560), 1, 5) : null;
            case "quartz" -> count >= 1 ? new RecipeResult("refined_quartz", minToTicks(90), 1, 1) : null;
            case "fire_quartz" -> count >= 1 ? new RecipeResult("refined_quartz", minToTicks(90), 3, 1) : null;
            default -> null;
        };
    })),

    CHARCOAL_KILN((data -> {
        String inputId = data.inputId;
        int count = data.count;
        String baseName = getBaseCropName(inputId);
        if (baseName.equals("wood") && count >= 10) {
            return new RecipeResult("coal", minToTicks(30), 1, 10);
        }
        return null;
    })),

    CRYSTALARIUM((data -> {
        String inputId = data.inputId;
        int count = data.count;

        if (!inputId.startsWith("stardewvalley:")) return null;
        String baseName = getBaseCropName(inputId);

        if (count < 1) return null;

        int time = switch (baseName) {
            case "emerald" -> minToTicks(2800);
            case "aquamarine" -> minToTicks(2200);
            case "ruby" -> minToTicks(2800);
            case "amethyst" -> minToTicks(1300);
            case "topaz" -> minToTicks(1100);
            case "jade" -> minToTicks(1800);
            case "diamond" -> minToTicks(7200);
            case "quartz" -> minToTicks(420);
            case "fire_quartz" -> minToTicks(1300);
            case "tear_crystal" -> minToTicks(1100);
            case "earth_crystal" -> minToTicks(800);
            default -> 0;
        };

        if (time <= 0) return null;
        // Output is the same as input (gem replication)
        return new RecipeResult(inputId, time, 1, 1);
    })),

    SEED_MAKER((data -> null)),
    GEODE_CRUSHER((data -> null)),

    SOLAR_PANEL((data -> null)),

    HEAVY_FURNACE((data -> {
        String inputId = data.inputId;
        int count = data.count;

        if (!inputId.startsWith("stardewvalley:")) return null;
        String baseName = getBaseCropName(inputId);

        return switch (baseName) {
            case "copper_ore" -> count >= 25 ? new RecipeResult("copper_bar", minToTicks(150), 5, 25) : null;
            case "iron_ore" -> count >= 25 ? new RecipeResult("iron_bar", minToTicks(600), 5, 25) : null;
            case "gold_ore" -> count >= 25 ? new RecipeResult("gold_bar", minToTicks(1500), 5, 25) : null;
            case "iridium_ore" -> count >= 25 ? new RecipeResult("iridium_bar", minToTicks(2400), 5, 25) : null;
            case "quartz" -> count >= 5 ? new RecipeResult("refined_quartz", minToTicks(450), 5, 5) : null;
            case "fire_quartz" -> count >= 5 ? new RecipeResult("refined_quartz", minToTicks(450), 15, 5) : null;
            default -> null;
        };
    })),

    RECYCLING_MACHINE((data -> null)),

    BAIT_MAKER((data -> {
        String inputId = data.inputId;
        if (!inputId.startsWith("stardewvalley:")) return null;
        String path = inputId.substring("stardewvalley:".length());

        // Only accept fish-type items (fish_ or caiji_ prefix for shellfish)
        if (!path.startsWith("fish_") && !path.startsWith("caiji_")) return null;
        // Exclude algae and jelly (not real fish)
        if (path.contains("algae") || path.contains("jelly") || path.equals("fish_seaweed")) return null;

        if (data.count < 1) return null;

        // Extract base fish name (remove quality suffix and prefix)
        String base = path;
        if (base.endsWith("_iridium")) base = base.substring(0, base.length() - "_iridium".length());
        else if (base.endsWith("_gold")) base = base.substring(0, base.length() - "_gold".length());
        else if (base.endsWith("_silver")) base = base.substring(0, base.length() - "_silver".length());
        if (base.startsWith("fish_")) base = base.substring(5);
        else if (base.startsWith("caiji_")) base = base.substring(6);

        // Verify this is a known fish
        boolean isKnownFish = false;
        for (String fishName : stardewvalley.modid.crafting.CraftingMaterialSets.ALL_FISH_NAMES) {
            if (fishName.equals(base)) { isKnownFish = true; break; }
        }
        if (!isKnownFish) return null;

        // Output: bait_{fish}_bait, 5-10 count, 10 minutes = 200 ticks
        String outputId = "bait_" + base + "_bait";
        int outputCount = 5 + new java.util.Random().nextInt(6); // 5-10
        return new RecipeResult(outputId, minToTicks(10), outputCount, 1);
    }));

    private static final int MINUTE_TICKS = 20;
    private static final int HOUR_TICKS = 1200;

    private static int minToTicks(int minutes) {
        return minutes * MINUTE_TICKS;
    }

    private static int hrToTicks(int hours) {
        return hours * HOUR_TICKS;
    }

    private final Function<InputData, RecipeResult> recipeFunction;

    MachineType(Function<InputData, RecipeResult> recipeFunction) {
        this.recipeFunction = recipeFunction;
    }

    public RecipeResult getRecipe(String inputId, CropQuality quality, int count) {
        return recipeFunction.apply(new InputData(inputId, quality, count));
    }

    public record RecipeResult(String outputId, int time, int outputCount, int consumedCount) {}
    private record InputData(String inputId, CropQuality quality, int count) {}

    private static final java.util.Set<String> FRUIT_CROPS = java.util.Set.of(
        "apple", "apricot", "banana", "cherry", "mango", "orange", "peach", "pomegranate",
        "ancientfruit", "blueberry", "cactusfruit", "cranberries", "grape", "hotpepper",
        "melon", "pineapple", "powdermelon", "qi_fruit", "rhubarb",
        "salmonberry", "starfruit", "strawberry", "wildplum", "blackberry", "coconut",
        "spiceberry", "crystalfruit", "qigua"
    );

    private static final java.util.Set<String> VEGETABLE_CROPS = java.util.Set.of(
        "amaranth", "artichoke", "beet", "bokchoy", "broccoli", "carrot", "cauliflower",
        "corn", "eggplant", "fiddleheadfern", "garlic", "greenbean", "hops", "kale",
        "parsnip", "potato", "pumpkin", "radish", "redcabbage", "summersquash", "taroroot",
        "tealeaves", "tomato", "unmilledrice", "yam"
    );

    public static boolean isFruitCrop(String inputId) {
        if (!inputId.startsWith("stardewvalley:")) return false;
        String path = inputId.substring("stardewvalley:".length());
        String base = path;
        if (path.endsWith("_iridium")) base = path.substring(0, path.length() - "_iridium".length());
        else if (path.endsWith("_gold")) base = path.substring(0, path.length() - "_gold".length());
        else if (path.endsWith("_silver")) base = path.substring(0, path.length() - "_silver".length());
        if (base.startsWith("caiji_")) base = base.substring(6);
        return FRUIT_CROPS.contains(base);
    }

    public static boolean isVegetableCrop(String inputId) {
        if (!inputId.startsWith("stardewvalley:")) return false;
        String path = inputId.substring("stardewvalley:".length());
        String base = path;
        if (path.endsWith("_iridium")) base = path.substring(0, path.length() - "_iridium".length());
        else if (path.endsWith("_gold")) base = path.substring(0, path.length() - "_gold".length());
        else if (path.endsWith("_silver")) base = path.substring(0, path.length() - "_silver".length());
        if (base.startsWith("caiji_")) base = base.substring(6);
        return VEGETABLE_CROPS.contains(base);
    }

    public static String getBaseCropName(String inputId) {
        if (!inputId.startsWith("stardewvalley:")) return inputId;
        String path = inputId.substring("stardewvalley:".length());
        String base = path;
        if (path.endsWith("_iridium")) base = path.substring(0, path.length() - "_iridium".length());
        else if (path.endsWith("_gold")) base = path.substring(0, path.length() - "_gold".length());
        else if (path.endsWith("_silver")) base = path.substring(0, path.length() - "_silver".length());
        if (base.startsWith("caiji_")) base = base.substring(6);
        if (base.startsWith("fish_")) base = base.substring(5);
        return base;
    }

    public static CropQuality getQualityFromId(String inputId) {
        if (inputId.endsWith("_iridium")) return CropQuality.IRIDIUM;
        if (inputId.endsWith("_gold")) return CropQuality.GOLD;
        if (inputId.endsWith("_silver")) return CropQuality.SILVER;
        return CropQuality.NORMAL;
    }

    public static String getAgedOutput(String inputId) {
        if (inputId.endsWith("_iridium")) return null;

        String namespace = "stardewvalley:";
        String path = inputId;
        if (inputId.startsWith(namespace)) path = inputId.substring(namespace.length());
        else path = inputId;

        // 构建有效陈酿物品白名单
        Map<String, String> agingMap = new HashMap<>();
        for (String wine : new String[]{"ancient_fruit_wine", "apple_wine", "apricot_wine", "banana_wine",
            "blackberry_wine", "blueberry_wine", "cactus_fruit_wine", "cherry_wine", "coconut_wine",
            "cranberries_wine", "crystal_fruit_wine", "grape_wine", "hot_pepper_wine", "mango_wine",
            "melon_wine", "orange_wine", "peach_wine", "pineapple_wine", "pomegranate_wine",
            "powdermelon_wine", "qi_fruit_wine", "rhubarb_wine", "salmonberry_wine", "spice_berry_wine",
            "starfruit_wine", "strawberry_wine", "wild_plum_wine"}) {
            agingMap.put(wine, wine + "_silver");
        }
        agingMap.put("mead", "mead_silver");
        agingMap.put("beer", "beer_silver");
        agingMap.put("pale_ale", "pale_ale_silver");
        agingMap.put("cheese", "cheese_silver");
        agingMap.put("goat_cheese", "goat_cheese_silver");
        agingMap.put("mayonnaise", "mayonnaise_silver");
        agingMap.put("duck_mayonnaise", "duck_mayonnaise_silver");
        agingMap.put("void_mayonnaise", "void_mayonnaise_silver");
        agingMap.put("dinosaur_mayonnaise", "dinosaur_mayonnaise_silver");

        // 处理金星品质：先验证基础物品是否在白名单中
        if (path.endsWith("_gold")) {
            String base = path.substring(0, path.length() - "_gold".length());
            if (!agingMap.containsKey(base)) return null;
            // 特殊处理：mayonnaise_gold 不存在，使用 gold_mayonnaise
            if (base.equals("mayonnaise")) return namespace + "mayonnaise_iridium";
            return namespace + base + "_iridium";
        }

        // 处理银星品质：先验证基础物品是否在白名单中
        if (path.endsWith("_silver")) {
            String base = path.substring(0, path.length() - "_silver".length());
            if (!agingMap.containsKey(base)) return null;
            // 特殊处理：mayonnaise_gold 不存在，跳转到 gold_mayonnaise
            if (base.equals("mayonnaise")) return namespace + "gold_mayonnaise";
            return namespace + base + "_gold";
        }

        // 处理 gold_mayonnaise 特殊命名（已是金星品质，陈酿到铱星）
        if (path.equals("gold_mayonnaise")) {
            if (!agingMap.containsKey("mayonnaise")) return null;
            return namespace + "mayonnaise_iridium";
        }

        // 普通品质
        String silver = agingMap.get(path);
        return silver != null ? namespace + silver : null;
    }

    public static int getAgingTime(String inputId) {
        String path = inputId;
        if (inputId.startsWith("stardewvalley:")) path = inputId.substring("stardewvalley:".length());

        // 天数 = 累计总天数，这里返回的是从当前品质到下一品质需要的天数
        if (path.contains("wine")) {
            // 总天数: silver=14, gold=28, iridium=56
            if (path.endsWith("_silver")) return 14 * 24000;  // 14→28 还需14天
            if (path.endsWith("_gold")) return 28 * 24000;    // 28→56 还需28天
            return 14 * 24000;                                  // normal→14 需14天
        }
        if (path.contains("mead") || path.contains("beer")) {
            // 总天数: silver=7, gold=14, iridium=28
            if (path.endsWith("_silver")) return 7 * 24000;   // 7→14 还需7天
            if (path.endsWith("_gold")) return 14 * 24000;    // 14→28 还需14天
            return 7 * 24000;                                   // normal→7 需7天
        }
        if (path.contains("pale_ale")) {
            // 总天数: silver=9, gold=17, iridium=34
            if (path.endsWith("_silver")) return 8 * 24000;   // 9→17 还需8天
            if (path.endsWith("_gold")) return 17 * 24000;    // 17→34 还需17天
            return 9 * 24000;                                   // normal→9 需9天
        }
        if (path.contains("cheese") || path.contains("mayonnaise")) {
            // 总天数: silver=3, gold=7, iridium=14
            if (path.endsWith("_silver")) return 4 * 24000;   // 3→7 还需4天
            if (path.endsWith("_gold")) return 7 * 24000;     // 7→14 还需7天
            if (path.equals("gold_mayonnaise")) return 7 * 24000; // gold_mayonnaise→iridium 需7天
            return 3 * 24000;                                   // normal→3 需3天
        }
        return 14 * 24000;
    }
}
