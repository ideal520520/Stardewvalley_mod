package stardewvalley.modid.item;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.crop.CropQuality;
import stardewvalley.modid.effect.ModStatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map;

public class ModItems {

    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    // 鱼名 → 鱼售价，供鱼籽/腌鱼籽/熏鱼注册使用
    private static final Map<String, Integer> FISH_PRICES = Map.ofEntries(
        Map.entry("pufferfish", 200),
        Map.entry("anchovy", 30),
        Map.entry("bream", 45),
        Map.entry("tuna", 100),
        Map.entry("sardine", 40),
        Map.entry("bullhead", 45),
        Map.entry("largemouth_bass", 100),
        Map.entry("smallmouth_bass", 50),
        Map.entry("rainbow_trout", 65),
        Map.entry("salmon", 75),
        Map.entry("walleye", 105),
        Map.entry("perch", 55),
        Map.entry("carp", 30),
        Map.entry("catfish", 200),
        Map.entry("pike", 100),
        Map.entry("sunfish", 30),
        Map.entry("red_mullet", 75),
        Map.entry("herring", 30),
        Map.entry("eel", 85),
        Map.entry("octopus", 150),
        Map.entry("red_snapper", 50),
        Map.entry("squid", 80),
        Map.entry("sea_cucumber", 75),
        Map.entry("super_cucumber", 250),
        Map.entry("ghostfish", 45),
        Map.entry("stonefish", 300),
        Map.entry("ice_pip", 500),
        Map.entry("lava_eel", 700),
        Map.entry("sandfish", 75),
        Map.entry("scorpion_carp", 150),
        Map.entry("flounder", 150),
        Map.entry("midnight_carp", 150),
        Map.entry("sturgeon", 200),
        Map.entry("tiger_trout", 150),
        Map.entry("tilapia", 75),
        Map.entry("chub", 50),
        Map.entry("dorado", 100),
        Map.entry("albacore", 75),
        Map.entry("shad", 60),
        Map.entry("lingcod", 120),
        Map.entry("halibut", 80),
        Map.entry("woodskip", 75),
        Map.entry("void_salmon", 150),
        Map.entry("slimejack", 100),
        Map.entry("stingray", 180),
        Map.entry("lionfish", 100),
        Map.entry("blue_discus", 120),
        Map.entry("goby", 150),
        Map.entry("midnight_squid", 100),
        Map.entry("spook_fish", 220),
        Map.entry("blobfish", 500),
        Map.entry("crimsonfish", 1500),
        Map.entry("angler", 900),
        Map.entry("legend", 5000),
        Map.entry("glacierfish", 1000),
        Map.entry("mutant_carp", 1000),
        Map.entry("son_of_crimsonfish", 1500),
        Map.entry("ms._angler", 900),
        Map.entry("legend_ii", 5000),
        Map.entry("glacierfish_jr", 1000),
        Map.entry("radioactive_carp", 1000),
        Map.entry("lobster", 120),
        Map.entry("crayfish", 75),
        Map.entry("crab", 100),
        Map.entry("shrimp", 60),
        Map.entry("snail", 65),
        Map.entry("periwinkle", 20),
        Map.entry("green_algae", 15),
        Map.entry("white_algae", 25),
        Map.entry("sea_jelly", 200),
        Map.entry("river_jelly", 125),
        Map.entry("cave_jelly", 180)
    );

    private static final String[] CROP_NAMES = {
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

    private record FoodData(int nutrition, float saturationModifier, float healAmount, int moneyValue, float exhaustion) {}
    private record RawFoodData(int stamina, int healValue, int moneyValue) {}

    private static FoodData calcFood(int stamina, int healValue, int moneyValue) {
        float raw = stamina * 20.0f / 270.0f;
        int nutrition;
        float saturationModifier;
        float exhaustion = 0.0f;

        if (raw < 1) {
            nutrition = 1;
            saturationModifier = 0.0f;
            float extraNutrition = 1.0f - raw;
            exhaustion = extraNutrition * 4.0f;
        } else if (raw <= 16) {
            nutrition = (int) Math.floor(raw);
            saturationModifier = (raw - nutrition) / (nutrition * 2.0f);
        } else {
            nutrition = 16;
            float rawSaturation = (raw - 16) * 2;
            saturationModifier = rawSaturation / (nutrition * 2.0f);
        }

        float healAmount = healValue / 5.0f;

        return new FoodData(nutrition, saturationModifier, healAmount, moneyValue, exhaustion);
    }

    public static void registerAll() {
        registerWeapons();
        for (String name : CROP_NAMES) {
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(StardewValley.MOD_ID, name));
            Item.Settings settings = new Item.Settings().registryKey(key);

            RawFoodData rawFood = switch (name) {
                case "amaranth" -> new RawFoodData(50, 22, 150);
                case "artichoke" -> new RawFoodData(30, 13, 160);
                case "beet" -> new RawFoodData(30, 13, 100);
                case "blueberry" -> new RawFoodData(25, 11, 50);
                case "bluejazz" -> new RawFoodData(46, 20, 50);
                case "bokchoy" -> new RawFoodData(25, 11, 80);
                case "broccoli" -> new RawFoodData(64, 28, 70);
                case "cactusfruit" -> new RawFoodData(75, 33, 75);
                case "carrot" -> new RawFoodData(75, 33, 35);
                case "cauliflower" -> new RawFoodData(75, 33, 175);
                case "corn" -> new RawFoodData(25, 11, 50);
                case "cranberries" -> new RawFoodData(39, 17, 75);
                case "eggplant" -> new RawFoodData(21, 9, 60);
                case "fairyrose" -> new RawFoodData(46, 20, 290);
                case "garlic" -> new RawFoodData(21, 9, 60);
                case "grape" -> new RawFoodData(39, 17, 80);
                case "greenbean" -> new RawFoodData(25, 11, 40);
                case "hops" -> new RawFoodData(46, 20, 25);
                case "hotpepper" -> new RawFoodData(12, 5, 40);
                case "kale" -> new RawFoodData(50, 22, 110);
                case "melon" -> new RawFoodData(113, 50, 250);
                case "parsnip" -> new RawFoodData(25, 11, 35);
                case "pineapple" -> new RawFoodData(140, 62, 300);
                case "poppy" -> new RawFoodData(46, 20, 140);
                case "potato" -> new RawFoodData(25, 11, 80);
                case "pumpkin" -> new RawFoodData(64, 28, 320);
                case "qigua" -> new RawFoodData(3, 1, 1);
                case "radish" -> new RawFoodData(46, 20, 90);
                case "redcabbage" -> new RawFoodData(75, 33, 260);
                case "starfruit" -> new RawFoodData(127, 56, 750);
                case "strawberry" -> new RawFoodData(50, 22, 120);
                case "summerspangle" -> new RawFoodData(46, 20, 90);
                case "summersquash" -> new RawFoodData(64, 28, 45);
                case "sunflower" -> new RawFoodData(46, 20, 80);
                case "taroroot" -> new RawFoodData(39, 17, 100);
                case "tomato" -> new RawFoodData(21, 9, 60);
                case "tulip" -> new RawFoodData(46, 20, 30);
                case "unmilledrice" -> new RawFoodData(3, 1, 30);
                case "yam" -> new RawFoodData(46, 20, 160);
                case "apple" -> new RawFoodData(100, 38, 17);
                case "apricot" -> new RawFoodData(38, 17, 50);
                case "banana" -> new RawFoodData(150, 75, 33);
                case "cherry" -> new RawFoodData(80, 38, 17);
                case "mango" -> new RawFoodData(130, 100, 45);
                case "orange" -> new RawFoodData(100, 38, 17);
                case "peach" -> new RawFoodData(140, 38, 17);
                case "pomegranate" -> new RawFoodData(140, 38, 17);
                case "powdermelon" -> new RawFoodData(63, 28, 60);
                default -> null;
            };

            if (rawFood != null) {
                FoodData normalFood = calcFood(rawFood.stamina(), rawFood.healValue(), rawFood.moneyValue());
                CropQuality[] qualities = ("fiber".equals(name) || "qigua".equals(name)) ? new CropQuality[]{CropQuality.NORMAL} : CropQuality.values();
                for (CropQuality quality : qualities) {
                    String itemName = quality.itemName(name);
                    Identifier qualityId = Identifier.of(StardewValley.MOD_ID, itemName);
                    RegistryKey<Item> qualityKey = RegistryKey.of(RegistryKeys.ITEM, qualityId);
                    FoodData scaledFood = calcFood(
                        Math.round(rawFood.stamina() * quality.getHealMultiplier()),
                        Math.round(rawFood.healValue() * quality.getHealMultiplier()),
                        Math.round(rawFood.moneyValue() * quality.getMoneyMultiplier())
                    );
                    CropItem cropItem = new CropItem(
                        new Item.Settings().registryKey(qualityKey).maxCount(999).food(
                            new FoodComponent.Builder()
                                .nutrition(scaledFood.nutrition())
                                .saturationModifier(scaledFood.saturationModifier())
                                .alwaysEdible()
                                .build()),
                        normalFood.healAmount(),
                        normalFood.moneyValue(),
                        quality,
                        scaledFood.exhaustion()
                    );
                    Registry.register(Registries.ITEM, qualityKey, cropItem);
                    ITEMS.put(itemName, cropItem);
                }
            } else {
                boolean hasGold = switch (name) {
                    case "ancientfruit", "fiber", "pumpkin", "rhubarb", "sweetgemberry", "tealeaves", "wheat" -> true;
                    default -> false;
                };
                if (hasGold) {
                    int gold = switch (name) {
                        case "ancientfruit" -> 550;
                        case "fiber" -> 1;
                        case "pumpkin" -> 320;
                        case "rhubarb" -> 220;
                        case "sweetgemberry" -> 3000;
                        case "tealeaves" -> 50;
                        case "wheat" -> 25;
                        default -> 0;
                    };
                    CropQuality[] qualities = "fiber".equals(name) ? new CropQuality[]{CropQuality.NORMAL} : CropQuality.values();
                    for (CropQuality quality : qualities) {
                        String itemName = quality.itemName(name);
                        Identifier qualityId = Identifier.of(StardewValley.MOD_ID, itemName);
                        RegistryKey<Item> qualityKey = RegistryKey.of(RegistryKeys.ITEM, qualityId);
                        CropItem cropItem = new CropItem(
                            new Item.Settings().registryKey(qualityKey).maxCount(999),
                            0,
                            Math.round(gold * quality.getMoneyMultiplier()),
                            quality,
                            0.0f
                        );
                        Registry.register(Registries.ITEM, qualityKey, cropItem);
                        ITEMS.put(itemName, cropItem);
                    }
                } else {
                    Item item = new Item(settings.maxCount(999));
                    Registry.register(Registries.ITEM, key, item);
                    ITEMS.put(name, item);
                }
            }
        }

        registerWateringCans();
        registerFertilizersDirect();
        registerCaijiItems();
        registerStardrop();
        registerMixedSeeds();
        registerForagingItems();
        registerToolItems();
        registerKuangshiItems();
        registerMiscItems();
        registerFishItems();
        registerDishItems();
        registerSquidInk();
        registerAnimalArtisanItems();
        registerAnimalQualityItems();
        registerBaitAndTackleItems();
        registerTargetedBaitItems();
        registerArtisanItems();
        registerRingItems();
        registerRefinedItems();
        registerWildSeedItems();
        registerNewMiscItems();
        registerGuwuItems();
        registerSpecialMiscItems();
        registerSkillBooks();
        registerDwarvishSafetyManual();
        registerShoes();
        registerSlingshotItems();
        registerBookItems();
    }

    private static void registerSkillBooks() {
        record SkillBookDef(String name, SkillBookItem.SkillType type) {}
        SkillBookDef[] books = {
            new SkillBookDef("stardew_valley_almanac", SkillBookItem.SkillType.FARMING),
            new SkillBookDef("woodcutters_weekly", SkillBookItem.SkillType.FORAGING),
            new SkillBookDef("mining_monthly", SkillBookItem.SkillType.MINING),
            new SkillBookDef("combat_quarterly", SkillBookItem.SkillType.COMBAT),
            new SkillBookDef("bait_and_bobber", SkillBookItem.SkillType.FISHING)
        };
        for (SkillBookDef book : books) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, book.name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            SkillBookItem item = new SkillBookItem(new Item.Settings().registryKey(key).maxCount(999), book.type);
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(book.name, item);
        }
    }

    private static void registerDwarvishSafetyManual() {
        Identifier id = Identifier.of(StardewValley.MOD_ID, "dwarvish_safety_manual");
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        DwarvishSafetyManualItem item = new DwarvishSafetyManualItem(new Item.Settings().registryKey(key).maxCount(999));
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put("dwarvish_safety_manual", item);
    }

    private record ShoeDef(String name, String displayName, int defense, int immunity) {}

    private static void registerShoes() {
        ShoeDef[] shoes = {
            new ShoeDef("sneakers", "运动鞋", 1, 0),
            new ShoeDef("rubber_boots", "橡胶靴", 0, 1),
            new ShoeDef("leather_boots", "皮靴", 1, 1),
            new ShoeDef("work_boots", "工作靴", 2, 0),
            new ShoeDef("combat_boots", "战靴", 3, 0),
            new ShoeDef("tundra_boots", "冻土靴", 2, 1),
            new ShoeDef("thermal_boots", "热能靴", 1, 2),
            new ShoeDef("dark_boots", "黑暗之靴", 4, 2),
            new ShoeDef("firewalker_boots", "蹈火者靴", 3, 3),
            new ShoeDef("genie_shoes", "神怪之鞋", 1, 6),
            new ShoeDef("space_boots", "太空之靴", 4, 4),
            new ShoeDef("cowboy_boots", "牛仔之靴", 2, 2),
            new ShoeDef("emilys_magic_boots", "艾米丽的魔法靴", 4, 4),
            new ShoeDef("leprechaun_shoes", "矮精灵鞋子", 2, 1),
            new ShoeDef("cinderclown_shoes", "灰烬小丑鞋", 6, 5),
            new ShoeDef("mermaid_boots", "美人鱼靴", 5, 8),
            new ShoeDef("dragonscale_boots", "龙鳞靴", 7, 0),
            new ShoeDef("crystal_shoes", "水晶鞋", 3, 5),
        };
        for (ShoeDef s : shoes) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, s.name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            ModShoesItem item = new ModShoesItem(
                new Item.Settings().registryKey(key).maxCount(1),
                s.defense, s.immunity
            );
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(s.name, item);
        }
    }

    private static void registerNewMiscItems() {
        String[] names = {"bat_wing", "dragon_tooth", "battery_pack", "cinder_shard",
            "golden_coconut", "ancient_treasure_decor", "galaxy_soul", "golden_pumpkin", "treasure_chest"};
        for (String name : names) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
    }

    private static void registerGuwuItems() {
        String[] names = {"amphibian_fossil", "anchor", "ancient_doll", "ancient_drum",
            "ancient_sword", "arrowhead", "bone_flute", "chewing_stick", "chicken_statue",
            "chipped_amphora", "dried_starfish", "dwarf_gadget", "dwarf_scroll_i",
            "dwarf_scroll_ii", "dwarf_scroll_iii", "dwarf_scroll_iv", "dwarvish_helm",
            "elvish_jewelry", "glass_shards", "golden_mask", "golden_relic",
            "nautilus_fossil", "ornamental_fan", "palm_fossil", "prehistoric_handaxe",
            "prehistoric_rib", "prehistoric_scapula", "prehistoric_skull",
            "prehistoric_tibia", "prehistoric_tool", "prehistoric_vertebra",
            "rare_disc", "rusty_cog", "rusty_spoon", "rusty_spur", "skeletal_hand",
            "skeletal_tail", "strange_doll_green", "strange_doll_yellow", "trilobite"};
        for (String name : names) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
    }

    private static void registerSpecialMiscItems() {
        String[] basicNames = {"mystery_box", "golden_mystery_box"};
        for (String name : basicNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
        // golden_animal_cracker - 自定义物品，需要特殊交互逻辑
        Identifier crackerId = Identifier.of(StardewValley.MOD_ID, "golden_animal_cracker");
        RegistryKey<Item> crackerKey = RegistryKey.of(RegistryKeys.ITEM, crackerId);
        GoldenAnimalCrackerItem crackerItem = new GoldenAnimalCrackerItem(new Item.Settings().registryKey(crackerKey).maxCount(999));
        Registry.register(Registries.ITEM, crackerKey, crackerItem);
        ITEMS.put("golden_animal_cracker", crackerItem);
    }

    private static void registerMiscItems() {
        String[] miscNames = {"bone_fragment", "clay", "oak_resin", "pine_tar", "misc_stone", "moss", "slime",
            "broken_cd", "broken_glasses", "driftwood", "soggy_newspaper", "trash_item",
            "wood", "hardwood", "bug_meat", "solar_essence",
            "void_essence", "hay"};
        for (String name : miscNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }

        // joja_cola - 可吃，回复13能量、5血量，获得21秒加速1
        RawFoodData rawFood = new RawFoodData(13, 5, 0);
        FoodData food = calcFood(rawFood.stamina(), rawFood.healValue(), rawFood.moneyValue());
        Identifier colaId = Identifier.of(StardewValley.MOD_ID, "joja_cola");
        RegistryKey<Item> colaKey = RegistryKey.of(RegistryKeys.ITEM, colaId);
        JojaColaItem jojaCola = new JojaColaItem(
            makeDrink(new Item.Settings().registryKey(colaKey).maxCount(999).food(
                new FoodComponent.Builder()
                    .nutrition(food.nutrition())
                    .saturationModifier(food.saturationModifier())
                    .alwaysEdible()
                    .build())),
            (float) rawFood.healValue() / 5.0f
        );
        Registry.register(Registries.ITEM, colaKey, jojaCola);
        ITEMS.put("joja_cola", jojaCola);

        // maple_syrup - 可食用，恢复50体力20生命
        RawFoodData mapleFood = new RawFoodData(50, 20, 0);
        FoodData mapleFd = calcFood(mapleFood.stamina(), mapleFood.healValue(), mapleFood.moneyValue());
        Identifier mapleId = Identifier.of(StardewValley.MOD_ID, "maple_syrup");
        RegistryKey<Item> mapleKey = RegistryKey.of(RegistryKeys.ITEM, mapleId);
        DishItem mapleSyrup = new DishItem(
            makeDrink(new Item.Settings().registryKey(mapleKey).maxCount(999).food(
                new FoodComponent.Builder()
                    .nutrition(mapleFd.nutrition())
                    .saturationModifier(mapleFd.saturationModifier())
                    .alwaysEdible()
                    .build())),
            mapleFd.healAmount()
        );
        Registry.register(Registries.ITEM, mapleKey, mapleSyrup);
        ITEMS.put("maple_syrup", mapleSyrup);
    }

    private static void registerKuangshiItems() {
        String[] oreNames = {"coal", "copper_ore", "copper_bar", "iron_ore", "iron_bar", "gold_ore", "gold_bar", "iridium_ore", "iridium_bar", "radioactive_ore", "radioactive_bar"};
        for (String name : oreNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
        String[] gemNames = {"amethyst", "aquamarine", "diamond", "emerald", "earth_crystal", "fire_quartz", "frozen_geode", "geode", "jade", "magma_geode", "omni_geode", "prismatic_shard", "ruby", "topaz", "quartz", "tear_crystal", "refined_quartz"};
        for (String name : gemNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
        String[] geodeMineralNames = {"tigerseye", "opal", "fire_opal", "alamite", "bixite", "baryte", "aerinite", "calcite", "dolomite", "esperite", "fluorapatite", "geminite", "helvite", "jamborite", "jagoite", "kyanite", "lunarite", "malachite", "neptunite", "lemon_stone", "nekoite", "orpiment", "petrified_slime", "thunder_egg", "pyrite", "ocean_stone", "ghost_crystal", "jasper", "celestine", "marble", "sandstone", "granite", "basalt", "limestone", "soapstone", "hematite", "mudstone", "obsidian", "slate", "fairy_stone", "star_shards"};
        for (String name : geodeMineralNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
    }

    private static void registerWateringCans() {
        String[] cans = {"watering_can", "copper_watering_can", "steel_watering_can", "gold_watering_can", "iridium_watering_can"};
        WateringCanItem.Tier[] tiers = {
            WateringCanItem.Tier.NORMAL,
            WateringCanItem.Tier.COPPER,
            WateringCanItem.Tier.STEEL,
            WateringCanItem.Tier.GOLD,
            WateringCanItem.Tier.IRIDIUM
        };
        for (int i = 0; i < cans.length; i++) {
            String name = cans[i];
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            WateringCanItem item = new WateringCanItem(new Item.Settings().registryKey(key).maxCount(1), tiers[i]);
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
    }

    private static void registerFertilizersDirect() {
        registerFertilizerType("basic_fertilizer", 1, FertilizerItem.FertilizerType.QUALITY);
        registerFertilizerType("quality_fertilizer", 2, FertilizerItem.FertilizerType.QUALITY);
        registerFertilizerType("deluxe_fertilizer", 3, FertilizerItem.FertilizerType.QUALITY);

        registerFertilizerType("basic_retaining_soil", 1, FertilizerItem.FertilizerType.RETAINING);
        registerFertilizerType("quality_retaining_soil", 2, FertilizerItem.FertilizerType.RETAINING);
        registerFertilizerType("deluxe_retaining_soil", 3, FertilizerItem.FertilizerType.RETAINING);

        registerFertilizerType("speed-gro", 1, FertilizerItem.FertilizerType.SPEED);
        registerFertilizerType("hyper_speed-gro", 2, FertilizerItem.FertilizerType.SPEED);
        registerFertilizerType("deluxe_speed-gro", 3, FertilizerItem.FertilizerType.SPEED);
    }

    private static void registerFertilizerType(String name, int tier, FertilizerItem.FertilizerType type) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        FertilizerItem item = new FertilizerItem(new Item.Settings().registryKey(key).maxCount(999), tier, type);
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static void registerStardrop() {
        Identifier id = Identifier.of(StardewValley.MOD_ID, "stardrop");
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        StardropItem item = new StardropItem(new Item.Settings().registryKey(key).maxCount(1).food(
            new FoodComponent.Builder().nutrition(0).saturationModifier(0.0f).alwaysEdible().build()
        ));
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put("stardrop", item);
    }

    private static final String[] CAIJI_NAMES = {
        "wildhorseradish", "daffodil", "leek", "dandelion", "springonion",
        "morel", "commonmushroom", "salmonberry", "spiceberry", "sweetpea",
        "fiddleheadfern", "wildplum", "hazelnut", "blackberry", "chanterelle",
        "redmushroom", "purplemushroom", "winterroot", "crystalfruit", "snowyam",
        "crocus", "holly", "nautilusshell", "coral", "seaurchin", "rainbow_shell",
        "clam", "cockle", "mussel", "oyster", "seaweed", "cavecarrot",
        "coconut", "ginger", "magmacap"
    };

    private static final Set<String> CAIJI_NO_QUALITY = Set.of("seaweed", "cavecarrot", "ginger");

    private static final Map<String, Integer> CAIJI_MONEY = Map.ofEntries(
        Map.entry("wildhorseradish", 50), Map.entry("daffodil", 30), Map.entry("leek", 60),
        Map.entry("dandelion", 40), Map.entry("springonion", 8), Map.entry("morel", 150),
        Map.entry("commonmushroom", 40), Map.entry("salmonberry", 5), Map.entry("spiceberry", 80),
        Map.entry("sweetpea", 50), Map.entry("fiddleheadfern", 90), Map.entry("wildplum", 80),
        Map.entry("hazelnut", 90), Map.entry("blackberry", 20), Map.entry("chanterelle", 160),
        Map.entry("redmushroom", 75), Map.entry("purplemushroom", 250), Map.entry("winterroot", 70),
        Map.entry("crystalfruit", 150), Map.entry("snowyam", 100), Map.entry("crocus", 60),
        Map.entry("holly", 80), Map.entry("nautilusshell", 120), Map.entry("coral", 80),
        Map.entry("seaurchin", 160), Map.entry("rainbow_shell", 300), Map.entry("clam", 50),
        Map.entry("cockle", 50), Map.entry("mussel", 30), Map.entry("oyster", 40),
        Map.entry("seaweed", 20), Map.entry("cavecarrot", 25), Map.entry("coconut", 100),
        Map.entry("ginger", 60), Map.entry("magmacap", 400)
    );


    private static void registerForagingItems() {
        // Sap - 可食用，-2能量，0生命，售出2g
        RawFoodData sapRaw = new RawFoodData(-2, 0, 2);
        FoodData sapFood = calcFood(sapRaw.stamina(), sapRaw.healValue(), sapRaw.moneyValue());
        Identifier sapId = Identifier.of(StardewValley.MOD_ID, "caiji_sap");
        RegistryKey<Item> sapKey = RegistryKey.of(RegistryKeys.ITEM, sapId);
        CropItem sapItem = new CropItem(
            new Item.Settings().registryKey(sapKey).maxCount(999).food(
                new FoodComponent.Builder()
                    .nutrition(sapFood.nutrition())
                    .saturationModifier(sapFood.saturationModifier())
                    .alwaysEdible()
                    .build()),
            0.0f,
            2,
            CropQuality.NORMAL,
            sapFood.exhaustion()
        );
        Registry.register(Registries.ITEM, sapKey, sapItem);
        ITEMS.put("caiji_sap", sapItem);
    }

    private static void registerToolItems() {
        String[] hoeNames = {"hoe", "copper_hoe", "steel_hoe", "gold_hoe", "iridium_hoe"};
        HoeItem.Tier[] hoeTiers = {HoeItem.Tier.NORMAL, HoeItem.Tier.COPPER, HoeItem.Tier.STEEL, HoeItem.Tier.GOLD, HoeItem.Tier.IRIDIUM};
        for (int i = 0; i < hoeNames.length; i++) {
            String name = hoeNames[i];
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            HoeItem item = new HoeItem(new Item.Settings().registryKey(key).maxCount(1), hoeTiers[i]);
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }

        // Pickaxes — .pickaxe() with 0 attack bonus, no special attack speed
        ToolMaterial[] pickaxeMaterials = {ModToolMaterials.BASIC, ModToolMaterials.COPPER, ModToolMaterials.STEEL, ModToolMaterials.GOLD, ModToolMaterials.IRIDIUM};
        String[] pickaxeNames = {"pickaxe", "copper_pickaxe", "steel_pickaxe", "gold_pickaxe", "iridium_pickaxe"};
        for (int i = 0; i < pickaxeNames.length; i++) {
            String name = pickaxeNames[i];
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(1)
                .pickaxe(pickaxeMaterials[i], 0, 0F));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }

        // Axes — .axe() with 0 attack bonus, no special attack speed
        ToolMaterial[] axeMaterials = {ModToolMaterials.BASIC, ModToolMaterials.COPPER, ModToolMaterials.STEEL, ModToolMaterials.GOLD, ModToolMaterials.IRIDIUM};
        String[] axeNames = {"axe", "copper_axe", "steel_axe", "gold_axe", "iridium_axe"};
        for (int i = 0; i < axeNames.length; i++) {
            String name = axeNames[i];
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(1)
                .axe(axeMaterials[i], 0F, 0F));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }

        // Other tools — no special mining behavior
        String[] otherToolNames = {
            "golden_scythe",
            "iridium_scythe",
            "milk_pail", "scythe", "shears"
        };
        for (String name : otherToolNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(1));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }

        // Fishing rods — with actual fishing functionality, no durability
        String[] rodNames = {"bamboo_pole", "fiberglass_rod", "iridium_rod", "advanced_iridium_rod"};
        for (String name : rodNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            ModFishingRodItem item = new ModFishingRodItem(new Item.Settings().registryKey(key).maxCount(1));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }

    }

    private static void registerMixedSeeds() {
        Identifier mixedId = Identifier.of(StardewValley.MOD_ID, "mixedseeds");
        RegistryKey<Item> mixedKey = RegistryKey.of(RegistryKeys.ITEM, mixedId);
        MixedSeedItem mixedItem = new MixedSeedItem(new Item.Settings().registryKey(mixedKey).maxCount(999));
        Registry.register(Registries.ITEM, mixedKey, mixedItem);
        ITEMS.put("mixedseeds", mixedItem);

        Identifier flowerId = Identifier.of(StardewValley.MOD_ID, "mixedflowerseeds");
        RegistryKey<Item> flowerKey = RegistryKey.of(RegistryKeys.ITEM, flowerId);
        MixedFlowerSeedItem flowerItem = new MixedFlowerSeedItem(new Item.Settings().registryKey(flowerKey).maxCount(999));
        Registry.register(Registries.ITEM, flowerKey, flowerItem);
        ITEMS.put("mixedflowerseeds", flowerItem);
    }

    private static void registerFishItems() {
        // 鱼名 → 价格映射（不包括已在 caiji 中的 clam, cockle, mussel, oyster, seaweed）
        java.util.Map<String, Integer> fishPrices = FISH_PRICES;

        // 鱼名 → (stamina, health) — 0,0 = 不能吃
        java.util.Map<String, int[]> fishFood = java.util.Map.ofEntries(
            java.util.Map.entry("pufferfish", new int[]{-100, 0}),
            java.util.Map.entry("anchovy", new int[]{13, 5}),
            java.util.Map.entry("bream", new int[]{25, 11}),
            java.util.Map.entry("tuna", new int[]{38, 17}),
            java.util.Map.entry("sardine", new int[]{13, 5}),
            java.util.Map.entry("bullhead", new int[]{13, 5}),
            java.util.Map.entry("largemouth_bass", new int[]{38, 17}),
            java.util.Map.entry("smallmouth_bass", new int[]{25, 11}),
            java.util.Map.entry("rainbow_trout", new int[]{24, 11}),
            java.util.Map.entry("salmon", new int[]{38, 17}),
            java.util.Map.entry("walleye", new int[]{30, 13}),
            java.util.Map.entry("perch", new int[]{25, 11}),
            java.util.Map.entry("carp", new int[]{13, 5}),
            java.util.Map.entry("catfish", new int[]{50, 22}),
            java.util.Map.entry("pike", new int[]{38, 17}),
            java.util.Map.entry("sunfish", new int[]{13, 5}),
            java.util.Map.entry("red_mullet", new int[]{25, 11}),
            java.util.Map.entry("herring", new int[]{13, 5}),
            java.util.Map.entry("eel", new int[]{30, 13}),
            java.util.Map.entry("octopus", new int[]{0, 0}), // 不能吃
            java.util.Map.entry("red_snapper", new int[]{25, 11}),
            java.util.Map.entry("squid", new int[]{25, 11}),
            java.util.Map.entry("sea_cucumber", new int[]{-25, 0}),
            java.util.Map.entry("super_cucumber", new int[]{125, 56}),
            java.util.Map.entry("ghostfish", new int[]{38, 17}),
            java.util.Map.entry("stonefish", new int[]{0, 0}), // 不能吃
            java.util.Map.entry("ice_pip", new int[]{38, 17}),
            java.util.Map.entry("lava_eel", new int[]{50, 22}),
            java.util.Map.entry("sandfish", new int[]{13, 5}),
            java.util.Map.entry("scorpion_carp", new int[]{-125, 0}),
            java.util.Map.entry("flounder", new int[]{38, 17}),
            java.util.Map.entry("midnight_carp", new int[]{50, 22}),
            java.util.Map.entry("sturgeon", new int[]{25, 11}),
            java.util.Map.entry("tiger_trout", new int[]{25, 11}),
            java.util.Map.entry("tilapia", new int[]{25, 11}),
            java.util.Map.entry("chub", new int[]{25, 11}),
            java.util.Map.entry("dorado", new int[]{25, 11}),
            java.util.Map.entry("albacore", new int[]{25, 11}),
            java.util.Map.entry("shad", new int[]{25, 11}),
            java.util.Map.entry("lingcod", new int[]{25, 11}),
            java.util.Map.entry("halibut", new int[]{25, 11}),
            java.util.Map.entry("woodskip", new int[]{25, 11}),
            java.util.Map.entry("void_salmon", new int[]{63, 28}),
            java.util.Map.entry("slimejack", new int[]{38, 17}),
            java.util.Map.entry("stingray", new int[]{38, 17}),
            java.util.Map.entry("lionfish", new int[]{38, 17}),
            java.util.Map.entry("blue_discus", new int[]{38, 17}),
            java.util.Map.entry("goby", new int[]{-62, 0}),
            java.util.Map.entry("midnight_squid", new int[]{38, 17}),
            java.util.Map.entry("spook_fish", new int[]{38, 17}),
            java.util.Map.entry("blobfish", new int[]{38, 17}),
            java.util.Map.entry("crimsonfish", new int[]{38, 17}),
            java.util.Map.entry("angler", new int[]{25, 11}),
            java.util.Map.entry("legend", new int[]{500, 225}),
            java.util.Map.entry("glacierfish", new int[]{25, 11}),
            java.util.Map.entry("mutant_carp", new int[]{25, 11}),
            java.util.Map.entry("son_of_crimsonfish", new int[]{38, 17}),
            java.util.Map.entry("ms._angler", new int[]{38, 17}),
            java.util.Map.entry("legend_ii", new int[]{500, 225}),
            java.util.Map.entry("glacierfish_jr", new int[]{25, 11}),
            java.util.Map.entry("radioactive_carp", new int[]{25, 11}),
            java.util.Map.entry("green_algae", new int[]{13, 5}),
            java.util.Map.entry("white_algae", new int[]{20, 9}),
            java.util.Map.entry("sea_jelly", new int[]{88, 59}),
            java.util.Map.entry("river_jelly", new int[]{75, 33}),
            java.util.Map.entry("cave_jelly", new int[]{75, 33})
        );

        // 贝壳类 — 暂不设置食物属性
        java.util.Set<String> shellItems = java.util.Set.of("lobster", "crayfish", "crab", "shrimp", "snail", "periwinkle");

        // 不能吃的鱼
        java.util.Set<String> noEatFish = java.util.Set.of("octopus", "stonefish");
        // 贝壳类只有银品质（无金/铱）
        java.util.Set<String> onlySilver = java.util.Set.of("lobster", "crayfish", "crab", "shrimp", "snail", "periwinkle");
        // 凝胶藻类只有普通品质（无品质变体）
        java.util.Set<String> onlyNormal = java.util.Set.of("green_algae", "white_algae", "sea_jelly", "river_jelly", "cave_jelly");

        for (java.util.Map.Entry<String, Integer> entry : fishPrices.entrySet()) {
            String name = entry.getKey();
            int price = entry.getValue();

            for (CropQuality quality : CropQuality.values()) {
                if (onlyNormal.contains(name) && quality != CropQuality.NORMAL) continue;
                if (onlySilver.contains(name) && (quality == CropQuality.GOLD || quality == CropQuality.IRIDIUM)) continue;

                String itemName = "fish_" + quality.itemName(name);
                Identifier id = Identifier.of(StardewValley.MOD_ID, itemName);
                RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

                Item.Settings settings = new Item.Settings().registryKey(key).maxCount(999);
                int[] foodData = fishFood.get(name);
                boolean canEat = foodData != null && !noEatFish.contains(name);
                float healAmount = 0;
                FoodData fd = null;

                if (canEat) {
                    int stamina = foodData[0];
                    int health = foodData[1];
                    healAmount = (float) health / 5.0f;
                    fd = calcFood(
                        Math.round(stamina * quality.getHealMultiplier()),
                        Math.round(health * quality.getHealMultiplier()),
                        Math.round(price * quality.getMoneyMultiplier())
                    );
                    settings = settings.food(
                        new FoodComponent.Builder()
                            .nutrition(fd.nutrition())
                            .saturationModifier(fd.saturationModifier())
                            .alwaysEdible()
                            .build()
                    );
                }

                CropItem item = new CropItem(settings, healAmount, price, quality, fd != null ? fd.exhaustion() : 0.0f);
                Registry.register(Registries.ITEM, key, item);
                ITEMS.put(itemName, item);
            }
        }
    }

    private static void registerCaijiItems() {
        for (String name : CAIJI_NAMES) {
            RawFoodData rawFood = switch (name) {
                case "wildhorseradish" -> new RawFoodData(13, 5, 50);
                case "leek" -> new RawFoodData(40, 18, 60);
                case "dandelion" -> new RawFoodData(25, 11, 40);
                case "springonion" -> new RawFoodData(13, 5, 8);
                case "morel" -> new RawFoodData(20, 9, 150);
                case "commonmushroom" -> new RawFoodData(38, 17, 40);
                case "salmonberry" -> new RawFoodData(25, 11, 5);
                case "spiceberry" -> new RawFoodData(25, 11, 80);
                case "fiddleheadfern" -> new RawFoodData(25, 11, 90);
                case "wildplum" -> new RawFoodData(25, 11, 80);
                case "hazelnut" -> new RawFoodData(30, 13, 90);
                case "blackberry" -> new RawFoodData(25, 11, 20);
                case "chanterelle" -> new RawFoodData(75, 33, 160);
                case "redmushroom" -> new RawFoodData(-50, 0, 75);
                case "purplemushroom" -> new RawFoodData(125, 56, 250);
                case "winterroot" -> new RawFoodData(25, 11, 70);
                case "crystalfruit" -> new RawFoodData(63, 28, 150);
                case "snowyam" -> new RawFoodData(30, 13, 100);
                case "holly" -> new RawFoodData(-37, 0, 80);
                case "cavecarrot" -> new RawFoodData(30, 13, 25);
                case "ginger" -> new RawFoodData(25, 11, 60);
                case "magmacap" -> new RawFoodData(175, 78, 400);
                default -> null;
            };

            int moneyValue = CAIJI_MONEY.getOrDefault(name, 0);
            boolean hasQuality = !CAIJI_NO_QUALITY.contains(name);

            if (hasQuality) {
                for (CropQuality quality : CropQuality.values()) {
                    String itemName = "caiji_" + name + quality.getSuffix();
                    Identifier id = Identifier.of(StardewValley.MOD_ID, itemName);
                    RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
                    if (rawFood != null) {
                        FoodData food = calcFood(
                            Math.round(rawFood.stamina() * quality.getHealMultiplier()),
                            Math.round(rawFood.healValue() * quality.getHealMultiplier()),
                            Math.round(moneyValue * quality.getMoneyMultiplier())
                        );
                        CropItem item = new CropItem(
                            new Item.Settings().registryKey(key).maxCount(999).food(
                                new FoodComponent.Builder()
                                    .nutrition(food.nutrition())
                                    .saturationModifier(food.saturationModifier())
                                    .alwaysEdible()
                                    .build()),
                            (float) rawFood.healValue() / 5.0f,
                            moneyValue,
                            quality,
                            food.exhaustion()
                        );
                        Registry.register(Registries.ITEM, key, item);
                        ITEMS.put(itemName, item);
                    } else {
                        CropItem item = new CropItem(
                            new Item.Settings().registryKey(key).maxCount(999),
                            0, moneyValue, quality, 0.0f
                        );
                        Registry.register(Registries.ITEM, key, item);
                        ITEMS.put(itemName, item);
                    }
                }
            } else {
                String itemName = "caiji_" + name;
                Identifier id = Identifier.of(StardewValley.MOD_ID, itemName);
                RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
                if (rawFood != null) {
                    FoodData food = calcFood(rawFood.stamina(), rawFood.healValue(), moneyValue);
                    CropItem item = new CropItem(
                        new Item.Settings().registryKey(key).maxCount(999).food(
                            new FoodComponent.Builder()
                                .nutrition(food.nutrition())
                                .saturationModifier(food.saturationModifier())
                                .alwaysEdible()
                                .build()),
                        (float) rawFood.healValue() / 5.0f,
                        moneyValue,
                        CropQuality.NORMAL,
                        food.exhaustion()
                    );
                    Registry.register(Registries.ITEM, key, item);
                    ITEMS.put(itemName, item);
                } else {
                    CropItem item = new CropItem(
                        new Item.Settings().registryKey(key).maxCount(999),
                        0, moneyValue, CropQuality.NORMAL, 0.0f
                    );
                    Registry.register(Registries.ITEM, key, item);
                    ITEMS.put(itemName, item);
                }
            }
        }
    }

    private static final String[] DISH_NAMES = {
        "algae_soup",
        "artichoke_dip",
        "autums_bounty",
        "baked_fish",
        "banana_pudding",
        "bean_hotpot",
        "blackberry_cobbler",
        "blueberry_tart",
        "bread",
        "bruschetta",
        "carp_surprise",
        "cheese_cauliflower",
        "chocolate_cake",
        "chowder",
        "coleslaw",
        "complete_breakfast",
        "cookie",
        "crab_cakes",
        "cranberry_candy",
        "cranberry_sauce",
        "crispy_bass",
        "dish_of_the_sea",
        "eggplant_parmesan",
        "escargot",
        "farmers_lunch",
        "fiddlehead_risotto",
        "fish_stew",
        "fish_taco",
        "fried_calamari",
        "fried_eel",
        "fried_egg",
        "fried_mushroom",
        "fruit_salad",
        "ginger_ale",
        "glazed_yams",
        "hashbrowns",
        "ice_cream",
        "lobster_bisque",
        "lucky_lunch",
        "maki_roll",
        "mango_sticky_rice",
        "maple_bar",
        "miners_treat",
        "moss_soup",
        "omelet",
        "pale_broth",
        "pancakes",
        "parsnip_soup",
        "pepper_poppers",
        "pink_cake",
        "pizza",
        "plum_pudding",
        "poi",
        "poppyseed_muffin",
        "pumpkin_pie",
        "pumpkin_soup",
        "radish_salad",
        "red_plate",
        "rhubarb_pie",
        "rice_pudding",
        "roasted_hazelnuts",
        "roots_platter",
        "salad",
        "salmon_dinner",
        "sashimi",
        "seafoam_pudding",
        "shrimp_cocktail",
        "spaghetti",
        "spicy_eel",
        "squid_ink_ravioli",
        "stir_fry",
        "strange_bun",
        "stuffing",
        "super_meal",
        "survival_burger",
        "tom_kha_soup",
        "tortilla",
        "triple_shot_espresso",
        "tropical_curry",
        "trout_soup",
        "vegetable_medley",
    };

    private static void registerDishItems() {
        for (String name : DISH_NAMES) {
            RawFoodData rawFood = switch (name) {
                case "algae_soup" -> new RawFoodData(75, 33, 0);
                case "artichoke_dip" -> new RawFoodData(100, 45, 0);
                case "autums_bounty" -> new RawFoodData(220, 99, 0);
                case "baked_fish" -> new RawFoodData(75, 33, 0);
                case "banana_pudding" -> new RawFoodData(125, 56, 0);
                case "bean_hotpot" -> new RawFoodData(125, 56, 0);
                case "blackberry_cobbler" -> new RawFoodData(175, 78, 0);
                case "blueberry_tart" -> new RawFoodData(125, 56, 0);
                case "bread" -> new RawFoodData(50, 22, 0);
                case "bruschetta" -> new RawFoodData(113, 50, 0);
                case "carp_surprise" -> new RawFoodData(90, 40, 0);
                case "cheese_cauliflower" -> new RawFoodData(138, 62, 0);
                case "chocolate_cake" -> new RawFoodData(150, 67, 0);
                case "chowder" -> new RawFoodData(225, 101, 0);
                case "coleslaw" -> new RawFoodData(213, 95, 0);
                case "complete_breakfast" -> new RawFoodData(200, 90, 0);
                case "cookie" -> new RawFoodData(90, 40, 0);
                case "crab_cakes" -> new RawFoodData(225, 101, 0);
                case "cranberry_candy" -> new RawFoodData(125, 56, 0);
                case "cranberry_sauce" -> new RawFoodData(125, 56, 0);
                case "crispy_bass" -> new RawFoodData(90, 40, 0);
                case "dish_of_the_sea" -> new RawFoodData(150, 67, 0);
                case "eggplant_parmesan" -> new RawFoodData(175, 78, 0);
                case "escargot" -> new RawFoodData(225, 101, 0);
                case "farmers_lunch" -> new RawFoodData(200, 90, 0);
                case "fiddlehead_risotto" -> new RawFoodData(225, 101, 0);
                case "fish_stew" -> new RawFoodData(225, 101, 0);
                case "fish_taco" -> new RawFoodData(165, 74, 0);
                case "fried_calamari" -> new RawFoodData(80, 36, 0);
                case "fried_eel" -> new RawFoodData(75, 33, 0);
                case "fried_egg" -> new RawFoodData(50, 22, 0);
                case "fried_mushroom" -> new RawFoodData(135, 60, 0);
                case "fruit_salad" -> new RawFoodData(263, 118, 0);
                case "ginger_ale" -> new RawFoodData(63, 28, 0);
                case "glazed_yams" -> new RawFoodData(200, 90, 0);
                case "hashbrowns" -> new RawFoodData(90, 40, 0);
                case "ice_cream" -> new RawFoodData(100, 45, 0);
                case "lobster_bisque" -> new RawFoodData(225, 101, 0);
                case "lucky_lunch" -> new RawFoodData(100, 45, 0);
                case "maki_roll" -> new RawFoodData(100, 45, 0);
                case "mango_sticky_rice" -> new RawFoodData(113, 50, 0);
                case "maple_bar" -> new RawFoodData(225, 101, 0);
                case "miners_treat" -> new RawFoodData(125, 56, 0);
                case "moss_soup" -> new RawFoodData(70, 31, 0);
                case "omelet" -> new RawFoodData(100, 45, 0);
                case "pale_broth" -> new RawFoodData(125, 56, 0);
                case "pancakes" -> new RawFoodData(90, 40, 0);
                case "parsnip_soup" -> new RawFoodData(85, 38, 0);
                case "pepper_poppers" -> new RawFoodData(130, 58, 0);
                case "pink_cake" -> new RawFoodData(250, 112, 0);
                case "pizza" -> new RawFoodData(150, 67, 0);
                case "plum_pudding" -> new RawFoodData(175, 78, 0);
                case "poi" -> new RawFoodData(75, 33, 0);
                case "poppyseed_muffin" -> new RawFoodData(150, 67, 0);
                case "pumpkin_pie" -> new RawFoodData(225, 101, 0);
                case "pumpkin_soup" -> new RawFoodData(200, 90, 0);
                case "radish_salad" -> new RawFoodData(200, 90, 0);
                case "red_plate" -> new RawFoodData(240, 108, 0);
                case "rhubarb_pie" -> new RawFoodData(215, 96, 0);
                case "rice_pudding" -> new RawFoodData(115, 51, 0);
                case "roasted_hazelnuts" -> new RawFoodData(175, 78, 0);
                case "roots_platter" -> new RawFoodData(125, 56, 0);
                case "salad" -> new RawFoodData(113, 50, 0);
                case "salmon_dinner" -> new RawFoodData(125, 56, 0);
                case "sashimi" -> new RawFoodData(75, 33, 0);
                case "seafoam_pudding" -> new RawFoodData(175, 78, 0);
                case "shrimp_cocktail" -> new RawFoodData(225, 101, 0);
                case "spaghetti" -> new RawFoodData(75, 33, 0);
                case "spicy_eel" -> new RawFoodData(115, 51, 0);
                case "squid_ink_ravioli" -> new RawFoodData(125, 56, 0);
                case "stir_fry" -> new RawFoodData(200, 90, 0);
                case "strange_bun" -> new RawFoodData(100, 45, 0);
                case "stuffing" -> new RawFoodData(170, 76, 0);
                case "super_meal" -> new RawFoodData(160, 72, 0);
                case "survival_burger" -> new RawFoodData(125, 56, 0);
                case "tom_kha_soup" -> new RawFoodData(175, 78, 0);
                case "tortilla" -> new RawFoodData(50, 22, 0);
                case "triple_shot_espresso" -> new RawFoodData(8, 3, 0);
                case "tropical_curry" -> new RawFoodData(150, 67, 0);
                case "trout_soup" -> new RawFoodData(100, 45, 0);
                case "vegetable_medley" -> new RawFoodData(165, 74, 0);
                default -> null;
            };

            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

            Item item;
            if (rawFood != null) {
                FoodData food = calcFood(rawFood.stamina(), rawFood.healValue(), rawFood.moneyValue());
                // 增益效果映射
                StatusEffectInstance[] effects = switch (name) {
                    // 完美早餐: 耕种 (+2), 体力值上限 (+3), 7min
                    case "complete_breakfast" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FARMING_BUFF, 8400, 1),
                        new StatusEffectInstance(ModStatusEffects.MAX_ENERGY_BUFF, 8400, 2)
                    };
                    // 幸运午餐: 运气 (+3), 11min11s
                    case "lucky_lunch" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.LUCK_BUFF, 13420, 2)
                    };
                    // 炒蘑菇: 攻击 (+2), 7min
                    case "fried_mushroom" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(StatusEffects.STRENGTH, 8400, 1)
                    };
                    // 豆类火锅: 体力值上限 (+2), 磁性 (+1), 7min
                    case "bean_hotpot" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.MAX_ENERGY_BUFF, 8400, 1),
                        new StatusEffectInstance(ModStatusEffects.MAGNETISM_BUFF, 8400, 0)
                    };
                    // 薯饼: 耕种 (+1), 5min35s
                    case "hashbrowns" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FARMING_BUFF, 6700, 0)
                    };
                    // 薄煎饼: 采集 (+2), 11min11s
                    case "pancakes" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FORAGING_BUFF, 13420, 1)
                    };
                    // 鱼肉卷: 钓鱼 (+2), 7min
                    case "fish_taco" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 8400, 1)
                    };
                    // 香酥鲈鱼: 磁性 (+2), 7min
                    case "crispy_bass" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.MAGNETISM_BUFF, 8400, 1)
                    };
                    // 爆炒青椒: 耕种 (+2), 速度 (+1), 7min
                    case "pepper_poppers" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FARMING_BUFF, 8400, 1),
                        new StatusEffectInstance(StatusEffects.SPEED, 8400, 0)
                    };
                    // 椰汁汤: 耕种 (+2), 体力值上限 (+2), 7min
                    case "tom_kha_soup" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FARMING_BUFF, 8400, 1),
                        new StatusEffectInstance(ModStatusEffects.MAX_ENERGY_BUFF, 8400, 1)
                    };
                    // 鳟鱼汤: 钓鱼 (+1), 4min39s
                    case "trout_soup" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 5578, 0)
                    };
                    // 炒鳗鱼: 运气 (+1), 7min
                    case "fried_eel" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.LUCK_BUFF, 8400, 0)
                    };
                    // 香辣鳗鱼: 运气 (+1), 速度 (+1), 7min
                    case "spicy_eel" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.LUCK_BUFF, 8400, 0),
                        new StatusEffectInstance(StatusEffects.SPEED, 8400, 0)
                    };
                    // 红之盛宴: 体力值上限 (+3), 3min30s
                    case "red_plate" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.MAX_ENERGY_BUFF, 4200, 2)
                    };
                    // 帕尔玛奶酪茄子: 采矿 (+1), 防御 (+3), 4min39s
                    case "eggplant_parmesan" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.MINING_BUFF, 5578, 0),
                        new StatusEffectInstance(ModStatusEffects.DEFENSE_BUFF, 5578, 2),
                        new StatusEffectInstance(StatusEffects.HASTE, 5578, 0)
                    };
                    // 秋日恩赐: 采集 (+2), 防御 (+2), 7min41s
                    case "autums_bounty" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FORAGING_BUFF, 9220, 1),
                        new StatusEffectInstance(ModStatusEffects.DEFENSE_BUFF, 9220, 1)
                    };
                    // 南瓜汤: 防御 (+2), 运气 (+2), 7min41s
                    case "pumpkin_soup" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.DEFENSE_BUFF, 9220, 1),
                        new StatusEffectInstance(ModStatusEffects.LUCK_BUFF, 9220, 1)
                    };
                    // 巨无霸餐: 体力值上限 (+2), 速度 (+1), 3min30s
                    case "super_meal" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.MAX_ENERGY_BUFF, 4200, 1),
                        new StatusEffectInstance(StatusEffects.SPEED, 4200, 0)
                    };
                    // 红莓酱: 采矿 (+2), 3min30s
                    case "cranberry_sauce" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.MINING_BUFF, 4200, 1),
                        new StatusEffectInstance(StatusEffects.HASTE, 4200, 1)
                    };
                    // 塞料面包: 防御 (+2), 5min35s
                    case "stuffing" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.DEFENSE_BUFF, 6700, 1)
                    };
                    // 农夫午餐: 耕种 (+3), 5min35s
                    case "farmers_lunch" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FARMING_BUFF, 6700, 2)
                    };
                    // 救生汉堡: 采集 (+3), 5min35s
                    case "survival_burger" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FORAGING_BUFF, 6700, 2)
                    };
                    // 海之菜肴: 钓鱼 (+3), 5min35s
                    case "dish_of_the_sea" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 6700, 2)
                    };
                    // 矿工特供: 采矿 (+3), 磁性 (+32), 5min35s
                    case "miners_treat" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.MINING_BUFF, 6700, 2),
                        new StatusEffectInstance(ModStatusEffects.MAGNETISM_BUFF, 6700, 31),
                        new StatusEffectInstance(StatusEffects.HASTE, 6700, 2)
                    };
                    // 块茎拼盘: 攻击 (+3), 5min35s
                    case "roots_platter" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(StatusEffects.STRENGTH, 6700, 2)
                    };
                    // 三倍浓缩咖啡: 速度 (+1), 4min12s
                    case "triple_shot_espresso" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(StatusEffects.SPEED, 5040, 0)
                    };
                    // 海泡布丁: 钓鱼 (+4), 3min30s
                    case "seafoam_pudding" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 4200, 3)
                    };
                    // 海鲜杂烩汤: 钓鱼 (+1), 16min47s
                    case "chowder" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 20140, 0)
                    };
                    // 龙虾浓汤: 钓鱼 (+3), 体力值上限 (+3), 16min47s
                    case "lobster_bisque" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 20140, 2),
                        new StatusEffectInstance(ModStatusEffects.MAX_ENERGY_BUFF, 20140, 2)
                    };
                    // 法式田螺: 钓鱼 (+2), 16min47s
                    case "escargot" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 20140, 1)
                    };
                    // 烩鱼汤: 钓鱼 (+3), 16min47s
                    case "fish_stew" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 20140, 2)
                    };
                    // 枫糖棒: 耕种 (+1), 钓鱼 (+1), 采矿 (+1), 16min47s
                    case "maple_bar" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FARMING_BUFF, 20140, 0),
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 20140, 0),
                        new StatusEffectInstance(ModStatusEffects.MINING_BUFF, 20140, 0),
                        new StatusEffectInstance(StatusEffects.HASTE, 20140, 0)
                    };
                    // 蟹黄糕: 速度 (+1), 防御 (+1), 16min47s
                    case "crab_cakes" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(StatusEffects.SPEED, 20140, 0),
                        new StatusEffectInstance(ModStatusEffects.DEFENSE_BUFF, 20140, 0)
                    };
                    // 虾鸡尾酒: 钓鱼 (+1), 运气 (+1), 10min
                    case "shrimp_cocktail" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FISHING_BUFF, 12000, 0),
                        new StatusEffectInstance(ModStatusEffects.LUCK_BUFF, 12000, 0)
                    };
                    // 姜汁汽水: 运气 (+1), 5min
                    case "ginger_ale" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.LUCK_BUFF, 6000, 0)
                    };
                    // 香蕉布丁: 采矿 (+1), 运气 (+1), 防御 (+1), 5min1s
                    case "banana_pudding" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.MINING_BUFF, 6020, 0),
                        new StatusEffectInstance(ModStatusEffects.LUCK_BUFF, 6020, 0),
                        new StatusEffectInstance(ModStatusEffects.DEFENSE_BUFF, 6020, 0),
                        new StatusEffectInstance(StatusEffects.HASTE, 6020, 0)
                    };
                    // 芒果糯米饭: 防御 (+3), 5min1s
                    case "mango_sticky_rice" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.DEFENSE_BUFF, 6020, 2)
                    };
                    // 热带咖喱: 采集 (+4), 5min1s
                    case "tropical_curry" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.FORAGING_BUFF, 6020, 3)
                    };
                    // 墨汁意大利饺: 采矿 (+1, 4min39s), 免疫负面效果 (2min59s)
                    case "squid_ink_ravioli" -> new StatusEffectInstance[]{
                        new StatusEffectInstance(ModStatusEffects.MINING_BUFF, 5578, 0),
                        new StatusEffectInstance(StatusEffects.HASTE, 5578, 0),
                        new StatusEffectInstance(ModStatusEffects.SQUID_INK_RAVIOLI_BUFF, 3580, 0)
                    };
                    default -> null;
                };
                FoodComponent foodComponent = new FoodComponent.Builder()
                    .nutrition(food.nutrition())
                    .saturationModifier(food.saturationModifier())
                    .alwaysEdible()
                    .build();
                Item.Settings settings = new Item.Settings().registryKey(key).maxCount(999).food(foodComponent);
                boolean isDrink = switch (name) {
                    case "algae_soup", "chowder", "cranberry_candy", "fish_stew", "ginger_ale",
                         "lobster_bisque", "moss_soup", "parsnip_soup", "pale_broth", "pumpkin_soup",
                         "tom_kha_soup", "tropical_curry", "trout_soup", "triple_shot_espresso" -> true;
                    default -> false;
                };
                if (isDrink) {
                    settings = makeDrink(settings);
                }
                item = new DishItem(settings, food.healAmount(), effects);
            } else {
                item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            }
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }

        // 能量饮料/药品 - 直接使用Minecraft食物属性（改为喝的动作）
        registerTonic("energy_tonic", 500, 0);

        // muscle_remedy - 食用后消除精疲力尽效果
        {
            RawFoodData mrFood = new RawFoodData(50, 22, 0);
            FoodData mrFd = calcFood(mrFood.stamina(), mrFood.healValue(), mrFood.moneyValue());
            Identifier mrId = Identifier.of(StardewValley.MOD_ID, "muscle_remedy");
            RegistryKey<Item> mrKey = RegistryKey.of(RegistryKeys.ITEM, mrId);
            Item.Settings mrSettings = new Item.Settings().registryKey(mrKey).maxCount(999)
                .food(new FoodComponent.Builder()
                    .nutrition(mrFd.nutrition())
                    .saturationModifier(mrFd.saturationModifier())
                    .alwaysEdible()
                    .build());
            mrSettings = makeDrink(mrSettings);
            DishItem mrItem = new DishItem(mrSettings, mrFd.healAmount()) {
                @Override
                public net.minecraft.item.ItemStack finishUsing(net.minecraft.item.ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.LivingEntity user) {
                    if (!world.isClient() && user instanceof net.minecraft.server.network.ServerPlayerEntity player) {
                        player.removeStatusEffect(stardewvalley.modid.effect.ModStatusEffects.EXHAUSTED);
                    }
                    return super.finishUsing(stack, world, user);
                }
            };
            Registry.register(Registries.ITEM, mrKey, mrItem);
            ITEMS.put("muscle_remedy", mrItem);
        }

        // magic_rock_candy - 500能量 220血量 采矿+2 运气+5 速度+1 防御+5 攻击+5（8分24秒）
        {
            RawFoodData rockRaw = new RawFoodData(500, 220, 0);
            FoodData rockFood = calcFood(rockRaw.stamina(), rockRaw.healValue(), rockRaw.moneyValue());
            Identifier rockId = Identifier.of(StardewValley.MOD_ID, "magic_rock_candy");
            RegistryKey<Item> rockKey = RegistryKey.of(RegistryKeys.ITEM, rockId);
            DishItem rockItem = new DishItem(
                new Item.Settings().registryKey(rockKey).maxCount(999).food(
                    new FoodComponent.Builder()
                        .nutrition(rockFood.nutrition())
                        .saturationModifier(rockFood.saturationModifier())
                        .alwaysEdible()
                        .build()),
                rockFood.healAmount(),
                new StatusEffectInstance(ModStatusEffects.MINING_BUFF, 10080, 1),
                new StatusEffectInstance(StatusEffects.HASTE, 10080, 1),
                new StatusEffectInstance(ModStatusEffects.LUCK_BUFF, 10080, 4),
                new StatusEffectInstance(StatusEffects.SPEED, 10080, 0),
                new StatusEffectInstance(ModStatusEffects.DEFENSE_BUFF, 10080, 5),
                new StatusEffectInstance(StatusEffects.STRENGTH, 10080, 4)
            );
            Registry.register(Registries.ITEM, rockKey, rockItem);
            ITEMS.put("magic_rock_candy", rockItem);
        }
    }

    private static void registerSquidInk() {
        Identifier id = Identifier.of(StardewValley.MOD_ID, "squid_ink");
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put("squid_ink", item);
    }

    private static void registerAnimalArtisanItems() {
        // 基础动物制品: vinegar, wheat_flour, oil, sugar, rice, cloth, egg, brown_egg, milk, goat_milk, gold_egg
        String[] artisanNames = {"vinegar", "wheat_flour", "oil", "sugar", "rice", "cloth", "egg", "brown_egg", "milk", "goat_milk", "gold_egg", "wool",
            "duck_egg", "large_egg", "large_brown_egg", "void_egg", "large_milk", "large_goat_milk", "dinosaur_egg", "truffle",
            "duck_feather", "rabbits_foot"};
        for (String name : artisanNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
        // cheese - 可食用
        RawFoodData cheeseFood = new RawFoodData(125, 56, 0);
        FoodData cheeseFd = calcFood(cheeseFood.stamina(), cheeseFood.healValue(), cheeseFood.moneyValue());
        Identifier cheeseId = Identifier.of(StardewValley.MOD_ID, "cheese");
        RegistryKey<Item> cheeseKey = RegistryKey.of(RegistryKeys.ITEM, cheeseId);
        Item cheeseItem = new DishItem(new Item.Settings().registryKey(cheeseKey).maxCount(999).food(
            new FoodComponent.Builder()
                .nutrition(cheeseFd.nutrition())
                .saturationModifier(cheeseFd.saturationModifier())
                .alwaysEdible()
                .build()
        ), cheeseFd.healAmount());
        Registry.register(Registries.ITEM, cheeseKey, cheeseItem);
        ITEMS.put("cheese", cheeseItem);

        // mayonnaise - 可食用
        RawFoodData mayonnaiseFood = new RawFoodData(50, 22, 190);
        FoodData mayonnaiseFd = calcFood(mayonnaiseFood.stamina(), mayonnaiseFood.healValue(), mayonnaiseFood.moneyValue());
        Identifier mayonnaiseId = Identifier.of(StardewValley.MOD_ID, "mayonnaise");
        RegistryKey<Item> mayonnaiseKey = RegistryKey.of(RegistryKeys.ITEM, mayonnaiseId);
        Item mayonnaiseItem = new DishItem(new Item.Settings().registryKey(mayonnaiseKey).maxCount(999).food(
            new FoodComponent.Builder()
                .nutrition(mayonnaiseFd.nutrition())
                .saturationModifier(mayonnaiseFd.saturationModifier())
                .alwaysEdible()
                .build()
        ), mayonnaiseFd.healAmount());
        Registry.register(Registries.ITEM, mayonnaiseKey, mayonnaiseItem);
        ITEMS.put("mayonnaise", mayonnaiseItem);

        // void_mayonnaise - 可食用(负面效果，使用CropItem应用exhaustion)
        RawFoodData voidMayonnaiseFood = new RawFoodData(-75, 0, 275);
        FoodData voidMayonnaiseFd = calcFood(voidMayonnaiseFood.stamina(), voidMayonnaiseFood.healValue(), voidMayonnaiseFood.moneyValue());
        Identifier voidMayonnaiseId = Identifier.of(StardewValley.MOD_ID, "void_mayonnaise");
        RegistryKey<Item> voidMayonnaiseKey = RegistryKey.of(RegistryKeys.ITEM, voidMayonnaiseId);
        CropItem voidMayonnaiseItem = new CropItem(
            new Item.Settings().registryKey(voidMayonnaiseKey).maxCount(999).food(
                new FoodComponent.Builder()
                    .nutrition(voidMayonnaiseFd.nutrition())
                    .saturationModifier(voidMayonnaiseFd.saturationModifier())
                    .alwaysEdible()
                    .build()),
            0.0f,
            voidMayonnaiseFood.moneyValue(),
            CropQuality.NORMAL,
            voidMayonnaiseFd.exhaustion()
        );
        Registry.register(Registries.ITEM, voidMayonnaiseKey, voidMayonnaiseItem);
        ITEMS.put("void_mayonnaise", voidMayonnaiseItem);

        // duck_mayonnaise - 可食用
        RawFoodData duckMayoFood = new RawFoodData(75, 33, 375);
        FoodData duckMayoFd = calcFood(duckMayoFood.stamina(), duckMayoFood.healValue(), duckMayoFood.moneyValue());
        Identifier duckMayoId = Identifier.of(StardewValley.MOD_ID, "duck_mayonnaise");
        RegistryKey<Item> duckMayoKey = RegistryKey.of(RegistryKeys.ITEM, duckMayoId);
        Item duckMayoItem = new DishItem(new Item.Settings().registryKey(duckMayoKey).maxCount(999).food(
            new FoodComponent.Builder().nutrition(duckMayoFd.nutrition()).saturationModifier(duckMayoFd.saturationModifier()).alwaysEdible().build()
        ), duckMayoFd.healAmount());
        Registry.register(Registries.ITEM, duckMayoKey, duckMayoItem);
        ITEMS.put("duck_mayonnaise", duckMayoItem);

        // gold_mayonnaise - 金星蛋黄酱
        RawFoodData goldMayoFood = new RawFoodData(100, 44, 475);
        FoodData goldMayoFd = calcFood(goldMayoFood.stamina(), goldMayoFood.healValue(), goldMayoFood.moneyValue());
        Identifier goldMayoId = Identifier.of(StardewValley.MOD_ID, "gold_mayonnaise");
        RegistryKey<Item> goldMayoKey = RegistryKey.of(RegistryKeys.ITEM, goldMayoId);
        Item goldMayoItem = new DishItem(new Item.Settings().registryKey(goldMayoKey).maxCount(999).food(
            new FoodComponent.Builder().nutrition(goldMayoFd.nutrition()).saturationModifier(goldMayoFd.saturationModifier()).alwaysEdible().build()
        ), goldMayoFd.healAmount());
        Registry.register(Registries.ITEM, goldMayoKey, goldMayoItem);
        ITEMS.put("gold_mayonnaise", goldMayoItem);

        // dinosaur_mayonnaise - 由恐龙蛋在蛋黄酱机加工
        RawFoodData dinoMayoFood = new RawFoodData(125, 56, 350);
        FoodData dinoMayoFd = calcFood(dinoMayoFood.stamina(), dinoMayoFood.healValue(), dinoMayoFood.moneyValue());
        Identifier dinoMayoId = Identifier.of(StardewValley.MOD_ID, "dinosaur_mayonnaise");
        RegistryKey<Item> dinoMayoKey = RegistryKey.of(RegistryKeys.ITEM, dinoMayoId);
        ArtisanItem dinoMayoItem = new ArtisanItem(
            new Item.Settings().registryKey(dinoMayoKey).maxCount(999)
                .food(new FoodComponent.Builder()
                    .nutrition(dinoMayoFd.nutrition())
                    .saturationModifier(dinoMayoFd.saturationModifier())
                    .alwaysEdible()
                    .build()),
            dinoMayoFood.stamina(), dinoMayoFd.healAmount(), dinoMayoFood.moneyValue(), true, false
        );
        Registry.register(Registries.ITEM, dinoMayoKey, dinoMayoItem);
        ITEMS.put("dinosaur_mayonnaise", dinoMayoItem);

        // truffle_oil - 由松露在产油机加工
        Identifier truffleOilId = Identifier.of(StardewValley.MOD_ID, "truffle_oil");
        RegistryKey<Item> truffleOilKey = RegistryKey.of(RegistryKeys.ITEM, truffleOilId);
        ArtisanItem truffleOilItem = new ArtisanItem(
            new Item.Settings().registryKey(truffleOilKey).maxCount(999),
            0, 0.0f, 800, false, false
        );
        Registry.register(Registries.ITEM, truffleOilKey, truffleOilItem);
        ITEMS.put("truffle_oil", truffleOilItem);

        // coffee - 可食用
        RawFoodData coffeeFood = new RawFoodData(3, 1, 0);
        FoodData coffeeFd = calcFood(coffeeFood.stamina(), coffeeFood.healValue(), coffeeFood.moneyValue());
        Identifier coffeeId = Identifier.of(StardewValley.MOD_ID, "coffee");
        RegistryKey<Item> coffeeKey = RegistryKey.of(RegistryKeys.ITEM, coffeeId);
        Item.Settings coffeeSettings = new Item.Settings().registryKey(coffeeKey).maxCount(999).food(
            new FoodComponent.Builder()
                .nutrition(coffeeFd.nutrition())
                .saturationModifier(coffeeFd.saturationModifier())
                .alwaysEdible()
                .build()
        );
        Item coffeeItem = new DishItem(makeDrink(coffeeSettings), coffeeFd.healAmount(), new StatusEffectInstance(StatusEffects.SPEED, 1660, 0));
        Registry.register(Registries.ITEM, coffeeKey, coffeeItem);
        ITEMS.put("coffee", coffeeItem);

        // 各种蜂蜜（均不可食用）
        registerHoneyItems();
    }

    private static void registerHoneyItems() {
        // 野蜂蜜(100), 郁金香蜂蜜(160), 蓝爵蜂蜜(200), 夏季亮片蜂蜜(280), 虞美人花蜂蜜(380), 向日葵蜂蜜(260), 玫瑰仙子蜂蜜(680)
        record HoneyDef(String name, int price) {}
        HoneyDef[] honeys = {
            new HoneyDef("honey", 100),
            new HoneyDef("tulip_honey", 160),
            new HoneyDef("blue_jazz_honey", 200),
            new HoneyDef("summer_spangle_honey", 280),
            new HoneyDef("poppy_honey", 380),
            new HoneyDef("sunflower_honey", 260),
            new HoneyDef("fairy_rose_honey", 680)
        };
        for (HoneyDef h : honeys) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, h.name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(h.name, item);
        }
    }

    private static void registerAnimalQualityItems() {
        // 蛋类品质变体
        String[] eggNames = {"egg", "brown_egg", "large_egg", "large_brown_egg", "duck_egg", "void_egg", "dinosaur_egg", "gold_egg"};
        String[] milkNames = {"milk", "large_milk", "goat_milk", "large_goat_milk"};
        String[] otherNames = {"duck_feather", "rabbits_foot", "wool", "truffle"};
        int[] qualities = {1, 2, 3}; // silver, gold, iridium

        for (String base : eggNames) {
            for (int q : qualities) {
                String suffix = switch (q) { case 1 -> "_silver"; case 2 -> "_gold"; default -> "_iridium"; };
                String name = base + suffix;
                Identifier id = Identifier.of(StardewValley.MOD_ID, name);
                RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
                Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
                Registry.register(Registries.ITEM, key, item);
                ITEMS.put(name, item);
            }
        }

        for (String base : milkNames) {
            for (int q : qualities) {
                String suffix = switch (q) { case 1 -> "_silver"; case 2 -> "_gold"; default -> "_iridium"; };
                String name = base + suffix;
                Identifier id = Identifier.of(StardewValley.MOD_ID, name);
                RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
                Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
                Registry.register(Registries.ITEM, key, item);
                ITEMS.put(name, item);
            }
        }

        for (String base : otherNames) {
            for (int q : qualities) {
                String suffix = switch (q) { case 1 -> "_silver"; case 2 -> "_gold"; default -> "_iridium"; };
                String name = base + suffix;
                Identifier id = Identifier.of(StardewValley.MOD_ID, name);
                RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
                Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
                Registry.register(Registries.ITEM, key, item);
                ITEMS.put(name, item);
            }
        }

        // 蛋黄酱品质变体使用 ArtisanItem
        registerArtisanItem("void_mayonnaise_silver", ArtisanItem.ArtisanType.JELLY, -75, 0, 275, true, 1);
        registerArtisanItem("void_mayonnaise_gold", ArtisanItem.ArtisanType.JELLY, -75, 0, 275, true, 2);
        registerArtisanItem("void_mayonnaise_iridium", ArtisanItem.ArtisanType.JELLY, -75, 0, 275, true, 3);
        registerArtisanItem("dinosaur_mayonnaise_silver", ArtisanItem.ArtisanType.JELLY, 125, 56, 350, true, 1);
        registerArtisanItem("dinosaur_mayonnaise_gold", ArtisanItem.ArtisanType.JELLY, 125, 56, 350, true, 2);
        registerArtisanItem("dinosaur_mayonnaise_iridium", ArtisanItem.ArtisanType.JELLY, 125, 56, 350, true, 3);

        // 蛋黄酱品质变体（银和铱，金品质已有 gold_mayonnaise）
        registerArtisanItem("mayonnaise_silver", ArtisanItem.ArtisanType.JELLY, 50, 22, 190, true, 1);
        registerArtisanItem("mayonnaise_iridium", ArtisanItem.ArtisanType.JELLY, 50, 22, 190, true, 3);
        // 鸭蛋黄酱品质变体
        registerArtisanItem("duck_mayonnaise_silver", ArtisanItem.ArtisanType.JELLY, 75, 33, 375, true, 1);
        registerArtisanItem("duck_mayonnaise_gold", ArtisanItem.ArtisanType.JELLY, 75, 33, 375, true, 2);
        registerArtisanItem("duck_mayonnaise_iridium", ArtisanItem.ArtisanType.JELLY, 75, 33, 375, true, 3);
    }

    private static void registerBaitAndTackleItems() {
        // 鱼饵物品
        String[] baitNames = {"bait_bait", "bait_challenge_bait", "bait_deluxe_bait", "bait_magic_bait", "bait_magnet", "bait_wild_bait"};
        for (String name : baitNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
        // 渔具物品（堆叠数为1，自带maxDamage耐久）
        String[] tackleNames = {"fishtool_barbed_hook", "fishtool_cork_bobber", "fishtool_curiosity_lure", "fishtool_dressed_spinner", "fishtool_lead_bobber", "fishtool_quality_bobber", "fishtool_sonar_bobber", "fishtool_spinner", "fishtool_trap_bobber", "fishtool_treasure_hunter"};
        for (String name : tackleNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new FishingTackleItem(new Item.Settings().registryKey(key).maxCount(1));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
    }

    /** 注册所有鱼的针对性鱼饵，售价 = 0.1 × 对应鱼基础售价 */
    private static void registerTargetedBaitItems() {
        // 合并鱼价格和采集品价格（cockle/mussel/oyster/clam 在 CAIJI_MONEY 中）
        java.util.Map<String, Integer> allFishPrices = new java.util.HashMap<>(FISH_PRICES);
        allFishPrices.putAll(CAIJI_MONEY);
        for (String fishName : stardewvalley.modid.crafting.CraftingMaterialSets.ALL_FISH_NAMES) {
            int fishPrice = allFishPrices.getOrDefault(fishName, 10);
            int baitPrice = Math.max(1, fishPrice / 10);
            String itemName = "bait_" + fishName + "_bait";
            Identifier id = Identifier.of(StardewValley.MOD_ID, itemName);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            // 使用自定义 BaitItem 以便查询其对应的鱼名
            Item item = new TargetedBaitItem(new Item.Settings().registryKey(key).maxCount(999), fishName, baitPrice);
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(itemName, item);
        }
    }

    /** 带有目标鱼信息的鱼饵物品 */
    public static class TargetedBaitItem extends Item {
        private final String targetFishName;
        private final int sellPrice;
        public TargetedBaitItem(Settings settings, String targetFishName, int sellPrice) {
            super(settings);
            this.targetFishName = targetFishName;
            this.sellPrice = sellPrice;
        }
        public String getTargetFishName() { return targetFishName; }
        public int getBaitSellPrice() { return sellPrice; }
    }

    /**
     * 连续幂函数对武器伤害进行平衡性调整
     * 公式: damage * (1.0 - 0.28 * (damage / 110)^1.2)
     * 伤害越高的武器降低幅度越大，伤害低的几乎不变
     */
    private static float applyDamageReduction(float damage) {
        return damage * (float)(1.0 - 0.28 * Math.pow(damage / 110.0, 1.2));
    }

    private static void registerWeapons() {
        record WeaponDef(String name, ModWeaponItem.WeaponType type, int level, float damage, float critChance,
                         int speedStat, int defenseStat, int weightStat, int critPowerStat,
                         boolean lifesteal, boolean crusader, int sellPrice) {}

        WeaponDef[] weapons = {
            // 剑类武器
            new WeaponDef("rusty_sword", ModWeaponItem.WeaponType.SWORD, 1, 3.5f, 0.02f, 0, 0, 0, 0, false, false, 50),
            new WeaponDef("steel_smallsword", ModWeaponItem.WeaponType.SWORD, 1, 6.0f, 0.02f, 2, 0, 0, 0, false, false, 50),
            new WeaponDef("wooden_blade", ModWeaponItem.WeaponType.SWORD, 1, 5.0f, 0.02f, 0, 0, 0, 0, false, false, 50),
            new WeaponDef("pirates_sword", ModWeaponItem.WeaponType.SWORD, 2, 11.0f, 0.02f, 2, 0, 0, 0, false, false, 100),
            new WeaponDef("silver_saber", ModWeaponItem.WeaponType.SWORD, 2, 11.5f, 0.02f, 0, 1, 0, 0, false, false, 100),
            new WeaponDef("cutlass", ModWeaponItem.WeaponType.SWORD, 3, 13.0f, 0.02f, 2, 0, 0, 0, false, false, 150),
            new WeaponDef("forest_sword", ModWeaponItem.WeaponType.SWORD, 3, 13.0f, 0.02f, 2, 1, 0, 0, false, false, 150),
            new WeaponDef("iron_edge", ModWeaponItem.WeaponType.SWORD, 3, 18.5f, 0.02f, -2, 1, 3, 0, false, false, 150),
            new WeaponDef("bone_sword", ModWeaponItem.WeaponType.SWORD, 5, 25.0f, 0.02f, 4, 0, 2, 0, false, false, 250),
            new WeaponDef("claymore", ModWeaponItem.WeaponType.SWORD, 5, 26.0f, 0.02f, -4, 2, 3, 0, false, false, 250),
            new WeaponDef("neptunes_glaive", ModWeaponItem.WeaponType.SWORD, 5, 26.5f, 0.02f, -1, 2, 4, 0, false, false, 250),
            new WeaponDef("templars_blade", ModWeaponItem.WeaponType.SWORD, 5, 25.5f, 0.0f, 0, 1, 0, 0, false, false, 250),
            new WeaponDef("insect_head", ModWeaponItem.WeaponType.SWORD, 6, 25.0f, 0.04f, 2, 0, 0, 2, false, false, 200),
            new WeaponDef("obsidian_edge", ModWeaponItem.WeaponType.SWORD, 6, 37.5f, 0.02f, -1, 0, 0, 10, false, false, 300),
            new WeaponDef("ossified_blade", ModWeaponItem.WeaponType.SWORD, 6, 34.0f, 0.02f, -2, 1, 2, 0, false, false, 300),
            new WeaponDef("holy_blade", ModWeaponItem.WeaponType.SWORD, 7, 23.5f, 0.02f, 4, 2, 0, 0, false, true, 350),
            new WeaponDef("tempered_broadsword", ModWeaponItem.WeaponType.SWORD, 7, 36.5f, 0.02f, -3, 3, 3, 0, false, false, 350),
            new WeaponDef("yeti_tooth", ModWeaponItem.WeaponType.SWORD, 7, 34.0f, 0.02f, 0, 4, 0, 10, false, false, 350),
            new WeaponDef("steel_falchion", ModWeaponItem.WeaponType.SWORD, 8, 37.0f, 0.02f, 4, 0, 0, 20, false, false, 400),
            new WeaponDef("dark_sword", ModWeaponItem.WeaponType.SWORD, 9, 37.5f, 0.04f, -5, 0, 5, 2, true, false, 450),
            new WeaponDef("lava_katana", ModWeaponItem.WeaponType.SWORD, 10, 59.5f, 0.015f, 0, 3, 3, 25, false, false, 500),
            new WeaponDef("dragontooth_cutlass", ModWeaponItem.WeaponType.SWORD, 13, 82.5f, 0.02f, 0, 0, 0, 50, false, false, 650),
            new WeaponDef("dwarf_sword", ModWeaponItem.WeaponType.SWORD, 13, 70.0f, 0.02f, 2, 4, 0, 0, false, false, 650),
            new WeaponDef("galaxy_sword", ModWeaponItem.WeaponType.SWORD, 13, 70.0f, 0.02f, 4, 0, 0, 0, false, false, 650),
            new WeaponDef("infinity_blade", ModWeaponItem.WeaponType.SWORD, 17, 90.0f, 0.02f, 4, 2, 0, 0, false, false, 850),
            new WeaponDef("haleys_iron", ModWeaponItem.WeaponType.SWORD, 6, 37.5f, 0.02f, -1, 0, 0, 10, false, false, 300),
            new WeaponDef("leahs_whittler", ModWeaponItem.WeaponType.SWORD, 6, 37.5f, 0.02f, -1, 0, 0, 10, false, false, 300),
            new WeaponDef("meowmere", ModWeaponItem.WeaponType.SWORD, 4, 20.0f, 0.02f, 4, 0, 2, 0, false, false, 200),
            // 匕首类武器
            new WeaponDef("carving_knife", ModWeaponItem.WeaponType.DAGGER, 1, 2.0f, 0.04f, 0, 0, 0, 2, false, false, 50),
            new WeaponDef("iron_dirk", ModWeaponItem.WeaponType.DAGGER, 1, 3.0f, 0.03f, 0, 0, 0, 2, false, false, 50),
            new WeaponDef("wind_spire", ModWeaponItem.WeaponType.DAGGER, 1, 3.0f, 0.02f, 0, 0, 5, 1, false, false, 50),
            new WeaponDef("elf_blade", ModWeaponItem.WeaponType.DAGGER, 2, 4.0f, 0.04f, 0, 0, 0, 2, false, false, 100),
            new WeaponDef("burglars_shank", ModWeaponItem.WeaponType.DAGGER, 4, 9.5f, 0.04f, 0, 0, 0, 2, false, false, 200),
            new WeaponDef("crystal_dagger", ModWeaponItem.WeaponType.DAGGER, 4, 7.0f, 0.03f, 0, 0, 5, 2, false, false, 200),
            new WeaponDef("shadow_dagger", ModWeaponItem.WeaponType.DAGGER, 4, 15.0f, 0.04f, 0, 0, 0, 2, false, false, 200),
            new WeaponDef("broken_trident", ModWeaponItem.WeaponType.DAGGER, 5, 20.5f, 0.02f, 0, 0, 0, 1, false, false, 250),
            new WeaponDef("wicked_kris", ModWeaponItem.WeaponType.DAGGER, 8, 27.0f, 0.06f, 0, 0, 0, 4, false, false, 400),
            new WeaponDef("galaxy_dagger", ModWeaponItem.WeaponType.DAGGER, 8, 35.0f, 0.02f, 1, 0, 5, 1, false, false, 400),
            new WeaponDef("dwarf_dagger", ModWeaponItem.WeaponType.DAGGER, 11, 35.0f, 0.03f, 1, 6, 5, 2, false, false, 550),
            new WeaponDef("dragontooth_shiv", ModWeaponItem.WeaponType.DAGGER, 12, 45.0f, 0.05f, 0, 0, 5, 3, false, false, 600),
            new WeaponDef("iridium_needle", ModWeaponItem.WeaponType.DAGGER, 12, 27.5f, 0.10f, 0, 0, 0, 6, false, false, 600),
            new WeaponDef("infinity_dagger", ModWeaponItem.WeaponType.DAGGER, 16, 60.0f, 0.06f, 1, 3, 5, 4, false, false, 800),
            new WeaponDef("elliotts_pencil", ModWeaponItem.WeaponType.DAGGER, 8, 27.0f, 0.06f, 0, 0, 0, 4, false, false, 200),
            new WeaponDef("abbys_planchette", ModWeaponItem.WeaponType.DAGGER, 8, 27.0f, 0.06f, 0, 0, 0, 4, false, false, 200),
            // 棍棒类武器
            new WeaponDef("femur", ModWeaponItem.WeaponType.CLUB, 2, 8.5f, 0.02f, 2, 0, 0, 0, false, false, 100),
            new WeaponDef("wood_club", ModWeaponItem.WeaponType.CLUB, 2, 12.5f, 0.02f, 0, 0, 0, 0, false, false, 100),
            new WeaponDef("wood_mallet", ModWeaponItem.WeaponType.CLUB, 3, 19.5f, 0.02f, 2, 0, 2, 0, false, false, 150),
            new WeaponDef("lead_rod", ModWeaponItem.WeaponType.CLUB, 4, 22.5f, 0.02f, -4, 0, 0, 0, false, false, 200),
            new WeaponDef("kudgel", ModWeaponItem.WeaponType.CLUB, 5, 33.5f, 0.02f, -1, 0, 2, 50, false, false, 250),
            new WeaponDef("the_slammer", ModWeaponItem.WeaponType.CLUB, 7, 47.5f, 0.02f, -2, 0, 0, 0, false, false, 350),
            new WeaponDef("galaxy_hammer", ModWeaponItem.WeaponType.CLUB, 12, 80.0f, 0.02f, 2, 0, 5, 0, false, false, 600),
            new WeaponDef("dwarf_hammer", ModWeaponItem.WeaponType.CLUB, 13, 80.0f, 0.02f, 0, 2, 5, 0, false, false, 650),
            new WeaponDef("dragontooth_club", ModWeaponItem.WeaponType.CLUB, 14, 90.0f, 0.02f, 0, 0, 3, 50, false, false, 700),
            new WeaponDef("infinity_gavel", ModWeaponItem.WeaponType.CLUB, 17, 110.0f, 0.02f, 2, 1, 5, 0, false, false, 850),
            new WeaponDef("alexs_bat", ModWeaponItem.WeaponType.CLUB, 7, 47.5f, 0.02f, -2, 0, 0, 0, false, false, 350),
            new WeaponDef("harveys_mallet", ModWeaponItem.WeaponType.CLUB, 7, 47.5f, 0.02f, -2, 0, 0, 0, false, false, 350),
            new WeaponDef("marus_wrench", ModWeaponItem.WeaponType.CLUB, 7, 47.5f, 0.02f, -2, 0, 0, 0, false, false, 150),
            new WeaponDef("pennys_fryer", ModWeaponItem.WeaponType.CLUB, 7, 47.5f, 0.02f, -2, 0, 0, 0, false, false, 150),
            new WeaponDef("sams_old_guitar", ModWeaponItem.WeaponType.CLUB, 7, 47.5f, 0.02f, -2, 0, 0, 0, false, false, 350),
            new WeaponDef("sebs_lost_mace", ModWeaponItem.WeaponType.CLUB, 7, 47.5f, 0.02f, -2, 0, 0, 0, false, false, 350),
        };

        for (WeaponDef w : weapons) {
            // 计算基础攻击间隔(ms)
            float baseCd = switch (w.type()) {
                case SWORD -> 600f;
                case DAGGER -> 300f;
                case CLUB -> 900f;
            };
            float cd = baseCd - w.speedStat() * 40f;
            float atkPerSec = 1000f / cd;
            float speedMod = atkPerSec - 4.0f;

            // 重量：每层重量 = 原版1级击退效果的一半(0.5)
            float knockback = w.weightStat() * 0.5f;

            Identifier id = Identifier.of(StardewValley.MOD_ID, w.name());
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

            net.minecraft.component.type.AttributeModifiersComponent.Builder attrBuilder = net.minecraft.component.type.AttributeModifiersComponent.builder()
                .add(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE,
                    new net.minecraft.entity.attribute.EntityAttributeModifier(ModWeaponItem.ATTACK_DAMAGE_MODIFIER_ID,
                        applyDamageReduction(w.damage()), net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND)
                .add(net.minecraft.entity.attribute.EntityAttributes.ATTACK_SPEED,
                    new net.minecraft.entity.attribute.EntityAttributeModifier(ModWeaponItem.ATTACK_SPEED_MODIFIER_ID,
                        speedMod, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND);

            if (knockback > 0) {
                attrBuilder.add(net.minecraft.entity.attribute.EntityAttributes.ATTACK_KNOCKBACK,
                    new net.minecraft.entity.attribute.EntityAttributeModifier(
                        Identifier.of(StardewValley.MOD_ID, "knockback"),
                        knockback, net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
            }

            // 攻击距离：棍棒+0.5到3.5（匕首由AttackRangeComponent+mixin控制）
            // 已移除range attribute，避免显示"实体交互距离+0.5"

            Item.Settings settings = new Item.Settings().registryKey(key).maxCount(1);
            if (w.type() == ModWeaponItem.WeaponType.DAGGER) {
                // 匕首：突刺动画+AttackRange限制2格
                settings = settings
                    .component(net.minecraft.component.DataComponentTypes.SWING_ANIMATION,
                        new net.minecraft.component.type.SwingAnimationComponent(
                            net.minecraft.util.SwingAnimationType.STAB, 6))
                    .component(net.minecraft.component.DataComponentTypes.ATTACK_RANGE,
                        new net.minecraft.component.type.AttackRangeComponent(0f, 2f, 0f, 3f, 0.5f, 1f));
            }
            ModWeaponItem item;
            if (w.type() == ModWeaponItem.WeaponType.DAGGER) {
                item = new ModDaggerItem(
                    settings.component(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build()),
                    applyDamageReduction(w.damage()), atkPerSec, w.critChance(), w.critPowerStat(),
                    w.defenseStat(), w.weightStat(), w.lifesteal(), w.crusader(), w.level(), w.sellPrice()
                );
            } else {
                item = new ModWeaponItem(
                    settings.component(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build()),
                    w.type(), applyDamageReduction(w.damage()), atkPerSec, w.critChance(), w.critPowerStat(),
                    w.defenseStat(), w.weightStat(), w.lifesteal(), w.crusader(), w.level(), w.sellPrice()
                );
            }
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(w.name(), item);
        }
    }

    private static void registerArtisanItems() {
        // WINE
        registerArtisanItem("ancient_fruit_wine", ArtisanItem.ArtisanType.WINE, 0, 0, 550, false, 0);
        registerArtisanItem("apple_wine", ArtisanItem.ArtisanType.WINE, 100, 38, 17, true, 0);
        registerArtisanItem("apricot_wine", ArtisanItem.ArtisanType.WINE, 38, 17, 50, true, 0);
        registerArtisanItem("banana_wine", ArtisanItem.ArtisanType.WINE, 150, 75, 33, true, 0);
        registerArtisanItem("blackberry_wine", ArtisanItem.ArtisanType.WINE, 25, 11, 20, true, 0);
        registerArtisanItem("blueberry_wine", ArtisanItem.ArtisanType.WINE, 25, 11, 50, true, 0);
        registerArtisanItem("cactus_fruit_wine", ArtisanItem.ArtisanType.WINE, 75, 33, 75, true, 0);
        registerArtisanItem("cherry_wine", ArtisanItem.ArtisanType.WINE, 80, 38, 17, true, 0);
        registerArtisanItem("coconut_wine", ArtisanItem.ArtisanType.WINE, 0, 0, 100, false, 0);
        registerArtisanItem("cranberries_wine", ArtisanItem.ArtisanType.WINE, 39, 17, 75, true, 0);
        registerArtisanItem("crystal_fruit_wine", ArtisanItem.ArtisanType.WINE, 63, 28, 150, true, 0);
        registerArtisanItem("grape_wine", ArtisanItem.ArtisanType.WINE, 39, 17, 80, true, 0);
        registerArtisanItem("hot_pepper_wine", ArtisanItem.ArtisanType.WINE, 12, 5, 40, true, 0);
        registerArtisanItem("mango_wine", ArtisanItem.ArtisanType.WINE, 130, 100, 45, true, 0);
        registerArtisanItem("melon_wine", ArtisanItem.ArtisanType.WINE, 113, 50, 250, true, 0);
        registerArtisanItem("orange_wine", ArtisanItem.ArtisanType.WINE, 100, 38, 17, true, 0);
        registerArtisanItem("peach_wine", ArtisanItem.ArtisanType.WINE, 140, 38, 17, true, 0);
        registerArtisanItem("pineapple_wine", ArtisanItem.ArtisanType.WINE, 140, 62, 300, true, 0);
        registerArtisanItem("pomegranate_wine", ArtisanItem.ArtisanType.WINE, 140, 38, 17, true, 0);
        registerArtisanItem("powdermelon_wine", ArtisanItem.ArtisanType.WINE, 63, 28, 60, true, 0);
        registerArtisanItem("qi_fruit_wine", ArtisanItem.ArtisanType.WINE, 3, 1, 1, true, 0);
        registerArtisanItem("rhubarb_wine", ArtisanItem.ArtisanType.WINE, 0, 0, 220, false, 0);
        registerArtisanItem("salmonberry_wine", ArtisanItem.ArtisanType.WINE, 25, 11, 5, true, 0);
        registerArtisanItem("spice_berry_wine", ArtisanItem.ArtisanType.WINE, 25, 11, 80, true, 0);
        registerArtisanItem("starfruit_wine", ArtisanItem.ArtisanType.WINE, 127, 56, 750, true, 0);
        registerArtisanItem("strawberry_wine", ArtisanItem.ArtisanType.WINE, 50, 22, 120, true, 0);
        registerArtisanItem("wild_plum_wine", ArtisanItem.ArtisanType.WINE, 25, 11, 80, true, 0);
        // WINE quality variants
        registerArtisanItem("ancient_fruit_wine_silver", ArtisanItem.ArtisanType.WINE, 0, 0, 550, false, 1);
        registerArtisanItem("ancient_fruit_wine_gold", ArtisanItem.ArtisanType.WINE, 0, 0, 550, false, 2);
        registerArtisanItem("ancient_fruit_wine_iridium", ArtisanItem.ArtisanType.WINE, 0, 0, 550, false, 3);
        registerArtisanItem("apple_wine_silver", ArtisanItem.ArtisanType.WINE, 100, 38, 17, true, 1);
        registerArtisanItem("apple_wine_gold", ArtisanItem.ArtisanType.WINE, 100, 38, 17, true, 2);
        registerArtisanItem("apple_wine_iridium", ArtisanItem.ArtisanType.WINE, 100, 38, 17, true, 3);
        registerArtisanItem("apricot_wine_silver", ArtisanItem.ArtisanType.WINE, 38, 17, 50, true, 1);
        registerArtisanItem("apricot_wine_gold", ArtisanItem.ArtisanType.WINE, 38, 17, 50, true, 2);
        registerArtisanItem("apricot_wine_iridium", ArtisanItem.ArtisanType.WINE, 38, 17, 50, true, 3);
        registerArtisanItem("banana_wine_silver", ArtisanItem.ArtisanType.WINE, 150, 75, 33, true, 1);
        registerArtisanItem("banana_wine_gold", ArtisanItem.ArtisanType.WINE, 150, 75, 33, true, 2);
        registerArtisanItem("banana_wine_iridium", ArtisanItem.ArtisanType.WINE, 150, 75, 33, true, 3);
        registerArtisanItem("blackberry_wine_silver", ArtisanItem.ArtisanType.WINE, 25, 11, 20, true, 1);
        registerArtisanItem("blackberry_wine_gold", ArtisanItem.ArtisanType.WINE, 25, 11, 20, true, 2);
        registerArtisanItem("blackberry_wine_iridium", ArtisanItem.ArtisanType.WINE, 25, 11, 20, true, 3);
        registerArtisanItem("blueberry_wine_silver", ArtisanItem.ArtisanType.WINE, 25, 11, 50, true, 1);
        registerArtisanItem("blueberry_wine_gold", ArtisanItem.ArtisanType.WINE, 25, 11, 50, true, 2);
        registerArtisanItem("blueberry_wine_iridium", ArtisanItem.ArtisanType.WINE, 25, 11, 50, true, 3);
        registerArtisanItem("cactus_fruit_wine_silver", ArtisanItem.ArtisanType.WINE, 75, 33, 75, true, 1);
        registerArtisanItem("cactus_fruit_wine_gold", ArtisanItem.ArtisanType.WINE, 75, 33, 75, true, 2);
        registerArtisanItem("cactus_fruit_wine_iridium", ArtisanItem.ArtisanType.WINE, 75, 33, 75, true, 3);
        registerArtisanItem("cherry_wine_silver", ArtisanItem.ArtisanType.WINE, 80, 38, 17, true, 1);
        registerArtisanItem("cherry_wine_gold", ArtisanItem.ArtisanType.WINE, 80, 38, 17, true, 2);
        registerArtisanItem("cherry_wine_iridium", ArtisanItem.ArtisanType.WINE, 80, 38, 17, true, 3);
        registerArtisanItem("coconut_wine_silver", ArtisanItem.ArtisanType.WINE, 0, 0, 100, false, 1);
        registerArtisanItem("coconut_wine_gold", ArtisanItem.ArtisanType.WINE, 0, 0, 100, false, 2);
        registerArtisanItem("coconut_wine_iridium", ArtisanItem.ArtisanType.WINE, 0, 0, 100, false, 3);
        registerArtisanItem("cranberries_wine_silver", ArtisanItem.ArtisanType.WINE, 39, 17, 75, true, 1);
        registerArtisanItem("cranberries_wine_gold", ArtisanItem.ArtisanType.WINE, 39, 17, 75, true, 2);
        registerArtisanItem("cranberries_wine_iridium", ArtisanItem.ArtisanType.WINE, 39, 17, 75, true, 3);
        registerArtisanItem("crystal_fruit_wine_silver", ArtisanItem.ArtisanType.WINE, 63, 28, 150, true, 1);
        registerArtisanItem("crystal_fruit_wine_gold", ArtisanItem.ArtisanType.WINE, 63, 28, 150, true, 2);
        registerArtisanItem("crystal_fruit_wine_iridium", ArtisanItem.ArtisanType.WINE, 63, 28, 150, true, 3);
        registerArtisanItem("grape_wine_silver", ArtisanItem.ArtisanType.WINE, 39, 17, 80, true, 1);
        registerArtisanItem("grape_wine_gold", ArtisanItem.ArtisanType.WINE, 39, 17, 80, true, 2);
        registerArtisanItem("grape_wine_iridium", ArtisanItem.ArtisanType.WINE, 39, 17, 80, true, 3);
        registerArtisanItem("hot_pepper_wine_silver", ArtisanItem.ArtisanType.WINE, 12, 5, 40, true, 1);
        registerArtisanItem("hot_pepper_wine_gold", ArtisanItem.ArtisanType.WINE, 12, 5, 40, true, 2);
        registerArtisanItem("hot_pepper_wine_iridium", ArtisanItem.ArtisanType.WINE, 12, 5, 40, true, 3);
        registerArtisanItem("mango_wine_silver", ArtisanItem.ArtisanType.WINE, 130, 100, 45, true, 1);
        registerArtisanItem("mango_wine_gold", ArtisanItem.ArtisanType.WINE, 130, 100, 45, true, 2);
        registerArtisanItem("mango_wine_iridium", ArtisanItem.ArtisanType.WINE, 130, 100, 45, true, 3);
        registerArtisanItem("melon_wine_silver", ArtisanItem.ArtisanType.WINE, 113, 50, 250, true, 1);
        registerArtisanItem("melon_wine_gold", ArtisanItem.ArtisanType.WINE, 113, 50, 250, true, 2);
        registerArtisanItem("melon_wine_iridium", ArtisanItem.ArtisanType.WINE, 113, 50, 250, true, 3);
        registerArtisanItem("orange_wine_silver", ArtisanItem.ArtisanType.WINE, 100, 38, 17, true, 1);
        registerArtisanItem("orange_wine_gold", ArtisanItem.ArtisanType.WINE, 100, 38, 17, true, 2);
        registerArtisanItem("orange_wine_iridium", ArtisanItem.ArtisanType.WINE, 100, 38, 17, true, 3);
        registerArtisanItem("peach_wine_silver", ArtisanItem.ArtisanType.WINE, 140, 38, 17, true, 1);
        registerArtisanItem("peach_wine_gold", ArtisanItem.ArtisanType.WINE, 140, 38, 17, true, 2);
        registerArtisanItem("peach_wine_iridium", ArtisanItem.ArtisanType.WINE, 140, 38, 17, true, 3);
        registerArtisanItem("pineapple_wine_silver", ArtisanItem.ArtisanType.WINE, 140, 62, 300, true, 1);
        registerArtisanItem("pineapple_wine_gold", ArtisanItem.ArtisanType.WINE, 140, 62, 300, true, 2);
        registerArtisanItem("pineapple_wine_iridium", ArtisanItem.ArtisanType.WINE, 140, 62, 300, true, 3);
        registerArtisanItem("pomegranate_wine_silver", ArtisanItem.ArtisanType.WINE, 140, 38, 17, true, 1);
        registerArtisanItem("pomegranate_wine_gold", ArtisanItem.ArtisanType.WINE, 140, 38, 17, true, 2);
        registerArtisanItem("pomegranate_wine_iridium", ArtisanItem.ArtisanType.WINE, 140, 38, 17, true, 3);
        registerArtisanItem("powdermelon_wine_silver", ArtisanItem.ArtisanType.WINE, 63, 28, 60, true, 1);
        registerArtisanItem("powdermelon_wine_gold", ArtisanItem.ArtisanType.WINE, 63, 28, 60, true, 2);
        registerArtisanItem("powdermelon_wine_iridium", ArtisanItem.ArtisanType.WINE, 63, 28, 60, true, 3);
        registerArtisanItem("qi_fruit_wine_silver", ArtisanItem.ArtisanType.WINE, 3, 1, 1, true, 1);
        registerArtisanItem("qi_fruit_wine_gold", ArtisanItem.ArtisanType.WINE, 3, 1, 1, true, 2);
        registerArtisanItem("qi_fruit_wine_iridium", ArtisanItem.ArtisanType.WINE, 3, 1, 1, true, 3);
        registerArtisanItem("rhubarb_wine_silver", ArtisanItem.ArtisanType.WINE, 0, 0, 220, false, 1);
        registerArtisanItem("rhubarb_wine_gold", ArtisanItem.ArtisanType.WINE, 0, 0, 220, false, 2);
        registerArtisanItem("rhubarb_wine_iridium", ArtisanItem.ArtisanType.WINE, 0, 0, 220, false, 3);
        registerArtisanItem("salmonberry_wine_silver", ArtisanItem.ArtisanType.WINE, 25, 11, 5, true, 1);
        registerArtisanItem("salmonberry_wine_gold", ArtisanItem.ArtisanType.WINE, 25, 11, 5, true, 2);
        registerArtisanItem("salmonberry_wine_iridium", ArtisanItem.ArtisanType.WINE, 25, 11, 5, true, 3);
        registerArtisanItem("spice_berry_wine_silver", ArtisanItem.ArtisanType.WINE, 25, 11, 80, true, 1);
        registerArtisanItem("spice_berry_wine_gold", ArtisanItem.ArtisanType.WINE, 25, 11, 80, true, 2);
        registerArtisanItem("spice_berry_wine_iridium", ArtisanItem.ArtisanType.WINE, 25, 11, 80, true, 3);
        registerArtisanItem("starfruit_wine_silver", ArtisanItem.ArtisanType.WINE, 127, 56, 750, true, 1);
        registerArtisanItem("starfruit_wine_gold", ArtisanItem.ArtisanType.WINE, 127, 56, 750, true, 2);
        registerArtisanItem("starfruit_wine_iridium", ArtisanItem.ArtisanType.WINE, 127, 56, 750, true, 3);
        registerArtisanItem("strawberry_wine_silver", ArtisanItem.ArtisanType.WINE, 50, 22, 120, true, 1);
        registerArtisanItem("strawberry_wine_gold", ArtisanItem.ArtisanType.WINE, 50, 22, 120, true, 2);
        registerArtisanItem("strawberry_wine_iridium", ArtisanItem.ArtisanType.WINE, 50, 22, 120, true, 3);
        registerArtisanItem("wild_plum_wine_silver", ArtisanItem.ArtisanType.WINE, 25, 11, 80, true, 1);
        registerArtisanItem("wild_plum_wine_gold", ArtisanItem.ArtisanType.WINE, 25, 11, 80, true, 2);
        registerArtisanItem("wild_plum_wine_iridium", ArtisanItem.ArtisanType.WINE, 25, 11, 80, true, 3);

        // JELLY
        registerArtisanItem("ancient_fruit_jelly", ArtisanItem.ArtisanType.JELLY, 0, 0, 550, false, 0);
        registerArtisanItem("apple_jelly", ArtisanItem.ArtisanType.JELLY, 100, 38, 17, true, 0);
        registerArtisanItem("apricot_jelly", ArtisanItem.ArtisanType.JELLY, 38, 17, 50, true, 0);
        registerArtisanItem("banana_jelly", ArtisanItem.ArtisanType.JELLY, 150, 75, 33, true, 0);
        registerArtisanItem("blackberry_jelly", ArtisanItem.ArtisanType.JELLY, 25, 11, 20, true, 0);
        registerArtisanItem("blueberry_jelly", ArtisanItem.ArtisanType.JELLY, 25, 11, 50, true, 0);
        registerArtisanItem("cactus_fruit_jelly", ArtisanItem.ArtisanType.JELLY, 75, 33, 75, true, 0);
        registerArtisanItem("cherry_jelly", ArtisanItem.ArtisanType.JELLY, 80, 38, 17, true, 0);
        registerArtisanItem("coconut_jelly", ArtisanItem.ArtisanType.JELLY, 0, 0, 100, false, 0);
        registerArtisanItem("cranberries_jelly", ArtisanItem.ArtisanType.JELLY, 39, 17, 75, true, 0);
        registerArtisanItem("crystal_fruit_jelly", ArtisanItem.ArtisanType.JELLY, 63, 28, 150, true, 0);
        registerArtisanItem("grape_jelly", ArtisanItem.ArtisanType.JELLY, 39, 17, 80, true, 0);
        registerArtisanItem("hot_pepper_jelly", ArtisanItem.ArtisanType.JELLY, 12, 5, 40, true, 0);
        registerArtisanItem("mango_jelly", ArtisanItem.ArtisanType.JELLY, 130, 100, 45, true, 0);
        registerArtisanItem("melon_jelly", ArtisanItem.ArtisanType.JELLY, 113, 50, 250, true, 0);
        registerArtisanItem("orange_jelly", ArtisanItem.ArtisanType.JELLY, 100, 38, 17, true, 0);
        registerArtisanItem("peach_jelly", ArtisanItem.ArtisanType.JELLY, 140, 38, 17, true, 0);
        registerArtisanItem("pineapple_jelly", ArtisanItem.ArtisanType.JELLY, 140, 62, 300, true, 0);
        registerArtisanItem("pomegranate_jelly", ArtisanItem.ArtisanType.JELLY, 140, 38, 17, true, 0);
        registerArtisanItem("powdermelon_jelly", ArtisanItem.ArtisanType.JELLY, 63, 28, 60, true, 0);
        registerArtisanItem("qi_fruit_jelly", ArtisanItem.ArtisanType.JELLY, 3, 1, 1, true, 0);
        registerArtisanItem("rhubarb_jelly", ArtisanItem.ArtisanType.JELLY, 0, 0, 220, false, 0);
        registerArtisanItem("salmonberry_jelly", ArtisanItem.ArtisanType.JELLY, 25, 11, 5, true, 0);
        registerArtisanItem("spice_berry_jelly", ArtisanItem.ArtisanType.JELLY, 25, 11, 80, true, 0);
        registerArtisanItem("starfruit_jelly", ArtisanItem.ArtisanType.JELLY, 127, 56, 750, true, 0);
        registerArtisanItem("strawberry_jelly", ArtisanItem.ArtisanType.JELLY, 50, 22, 120, true, 0);
        registerArtisanItem("wild_plum_jelly", ArtisanItem.ArtisanType.JELLY, 25, 11, 80, true, 0);

        // DRIED_FRUIT
        registerArtisanItem("dried_ancient_fruit", ArtisanItem.ArtisanType.DRIED_FRUIT, 0, 0, 550, false, 0);
        registerArtisanItem("dried_apple", ArtisanItem.ArtisanType.DRIED_FRUIT, 100, 38, 17, true, 0);
        registerArtisanItem("dried_apricot", ArtisanItem.ArtisanType.DRIED_FRUIT, 38, 17, 50, true, 0);
        registerArtisanItem("dried_banana", ArtisanItem.ArtisanType.DRIED_FRUIT, 150, 75, 33, true, 0);
        registerArtisanItem("dried_blackberry", ArtisanItem.ArtisanType.DRIED_FRUIT, 25, 11, 20, true, 0);
        registerArtisanItem("dried_blueberry", ArtisanItem.ArtisanType.DRIED_FRUIT, 25, 11, 50, true, 0);
        registerArtisanItem("dried_cactus_fruit", ArtisanItem.ArtisanType.DRIED_FRUIT, 75, 33, 75, true, 0);
        registerArtisanItem("dried_cherry", ArtisanItem.ArtisanType.DRIED_FRUIT, 80, 38, 17, true, 0);
        registerArtisanItem("dried_coconut", ArtisanItem.ArtisanType.DRIED_FRUIT, 0, 0, 100, false, 0);
        registerArtisanItem("dried_cranberries", ArtisanItem.ArtisanType.DRIED_FRUIT, 39, 17, 75, true, 0);
        registerArtisanItem("dried_crystal_fruit", ArtisanItem.ArtisanType.DRIED_FRUIT, 63, 28, 150, true, 0);
        registerArtisanItem("dried_hot_pepper", ArtisanItem.ArtisanType.DRIED_FRUIT, 12, 5, 40, true, 0);
        registerArtisanItem("dried_mango", ArtisanItem.ArtisanType.DRIED_FRUIT, 130, 100, 45, true, 0);
        registerArtisanItem("dried_melon", ArtisanItem.ArtisanType.DRIED_FRUIT, 113, 50, 250, true, 0);
        registerArtisanItem("dried_orange", ArtisanItem.ArtisanType.DRIED_FRUIT, 100, 38, 17, true, 0);
        registerArtisanItem("dried_peach", ArtisanItem.ArtisanType.DRIED_FRUIT, 140, 38, 17, true, 0);
        registerArtisanItem("dried_pineapple", ArtisanItem.ArtisanType.DRIED_FRUIT, 140, 62, 300, true, 0);
        registerArtisanItem("dried_pomegranate", ArtisanItem.ArtisanType.DRIED_FRUIT, 140, 38, 17, true, 0);
        registerArtisanItem("dried_powdermelon", ArtisanItem.ArtisanType.DRIED_FRUIT, 63, 28, 60, true, 0);
        registerArtisanItem("dried_qi_fruit", ArtisanItem.ArtisanType.DRIED_FRUIT, 3, 1, 1, true, 0);
        registerArtisanItem("dried_rhubarb", ArtisanItem.ArtisanType.DRIED_FRUIT, 0, 0, 220, false, 0);
        registerArtisanItem("dried_salmonberry", ArtisanItem.ArtisanType.DRIED_FRUIT, 25, 11, 5, true, 0);
        registerArtisanItem("dried_spice_berry", ArtisanItem.ArtisanType.DRIED_FRUIT, 25, 11, 80, true, 0);
        registerArtisanItem("dried_starfruit", ArtisanItem.ArtisanType.DRIED_FRUIT, 127, 56, 750, true, 0);
        registerArtisanItem("dried_strawberry", ArtisanItem.ArtisanType.DRIED_FRUIT, 50, 22, 120, true, 0);
        registerArtisanItem("dried_wild_plum", ArtisanItem.ArtisanType.DRIED_FRUIT, 25, 11, 80, true, 0);
        registerArtisanItem("rasins", ArtisanItem.ArtisanType.DRIED_FRUIT, 39, 17, 80, true, 0);

        // JUICE
        registerArtisanItem("amaranth_juice", ArtisanItem.ArtisanType.JUICE, 50, 22, 150, true, 0);
        registerArtisanItem("artichoke_juice", ArtisanItem.ArtisanType.JUICE, 30, 13, 160, true, 0);
        registerArtisanItem("beet_juice", ArtisanItem.ArtisanType.JUICE, 30, 13, 100, true, 0);
        registerArtisanItem("bok_choy_juice", ArtisanItem.ArtisanType.JUICE, 25, 11, 80, true, 0);
        registerArtisanItem("broccoli_juice", ArtisanItem.ArtisanType.JUICE, 64, 28, 70, true, 0);
        registerArtisanItem("carrot_juice", ArtisanItem.ArtisanType.JUICE, 75, 33, 35, true, 0);
        registerArtisanItem("cauliflower_juice", ArtisanItem.ArtisanType.JUICE, 75, 33, 175, true, 0);
        registerArtisanItem("corn_juice", ArtisanItem.ArtisanType.JUICE, 25, 11, 50, true, 0);
        registerArtisanItem("eggplant_juice", ArtisanItem.ArtisanType.JUICE, 21, 9, 60, true, 0);
        registerArtisanItem("fiddlehead_fern_juice", ArtisanItem.ArtisanType.JUICE, 25, 11, 90, true, 0);
        registerArtisanItem("garlic_juice", ArtisanItem.ArtisanType.JUICE, 21, 9, 60, true, 0);
        registerArtisanItem("green_bean_juice", ArtisanItem.ArtisanType.JUICE, 25, 11, 40, true, 0);
        registerArtisanItem("kale_juice", ArtisanItem.ArtisanType.JUICE, 50, 22, 110, true, 0);
        registerArtisanItem("parsnip_juice", ArtisanItem.ArtisanType.JUICE, 25, 11, 35, true, 0);
        registerArtisanItem("potato_juice", ArtisanItem.ArtisanType.JUICE, 25, 11, 80, true, 0);
        registerArtisanItem("pumpkin_juice", ArtisanItem.ArtisanType.JUICE, 0, 0, 320, false, 0);
        registerArtisanItem("radish_juice", ArtisanItem.ArtisanType.JUICE, 46, 20, 90, true, 0);
        registerArtisanItem("red_cabbage_juice", ArtisanItem.ArtisanType.JUICE, 75, 33, 260, true, 0);
        registerArtisanItem("summer_squash_juice", ArtisanItem.ArtisanType.JUICE, 64, 28, 45, true, 0);
        registerArtisanItem("taro_root_juice", ArtisanItem.ArtisanType.JUICE, 39, 17, 100, true, 0);
        registerArtisanItem("tomato_juice", ArtisanItem.ArtisanType.JUICE, 21, 9, 60, true, 0);
        registerArtisanItem("unmilled_rice_juice", ArtisanItem.ArtisanType.JUICE, 3, 1, 30, true, 0);
        registerArtisanItem("yam_juice", ArtisanItem.ArtisanType.JUICE, 46, 20, 160, true, 0);

        // PICKLE
        registerArtisanItem("amaranth_pickles", ArtisanItem.ArtisanType.PICKLE, 50, 22, 150, true, 0);
        registerArtisanItem("artichoke_pickles", ArtisanItem.ArtisanType.PICKLE, 30, 13, 160, true, 0);
        registerArtisanItem("beet_pickles", ArtisanItem.ArtisanType.PICKLE, 30, 13, 100, true, 0);
        registerArtisanItem("bok_choy_pickles", ArtisanItem.ArtisanType.PICKLE, 25, 11, 80, true, 0);
        registerArtisanItem("broccoli_pickles", ArtisanItem.ArtisanType.PICKLE, 64, 28, 70, true, 0);
        registerArtisanItem("carrot_pickles", ArtisanItem.ArtisanType.PICKLE, 75, 33, 35, true, 0);
        registerArtisanItem("cauliflower_pickles", ArtisanItem.ArtisanType.PICKLE, 75, 33, 175, true, 0);
        registerArtisanItem("corn_pickles", ArtisanItem.ArtisanType.PICKLE, 25, 11, 50, true, 0);
        registerArtisanItem("eggplant_pickles", ArtisanItem.ArtisanType.PICKLE, 21, 9, 60, true, 0);
        registerArtisanItem("fiddlehead_fern_pickles", ArtisanItem.ArtisanType.PICKLE, 25, 11, 90, true, 0);
        registerArtisanItem("garlic_pickles", ArtisanItem.ArtisanType.PICKLE, 21, 9, 60, true, 0);
        registerArtisanItem("green_bean_pickles", ArtisanItem.ArtisanType.PICKLE, 25, 11, 40, true, 0);
        registerArtisanItem("hops_pickles", ArtisanItem.ArtisanType.PICKLE, 46, 20, 25, true, 0);
        registerArtisanItem("kale_pickles", ArtisanItem.ArtisanType.PICKLE, 50, 22, 110, true, 0);
        registerArtisanItem("parsnip_pickles", ArtisanItem.ArtisanType.PICKLE, 25, 11, 35, true, 0);
        registerArtisanItem("potato_pickles", ArtisanItem.ArtisanType.PICKLE, 25, 11, 80, true, 0);
        registerArtisanItem("pumpkin_pickles", ArtisanItem.ArtisanType.PICKLE, 0, 0, 320, false, 0);
        registerArtisanItem("radish_pickles", ArtisanItem.ArtisanType.PICKLE, 46, 20, 90, true, 0);
        registerArtisanItem("red_cabbage_pickles", ArtisanItem.ArtisanType.PICKLE, 75, 33, 260, true, 0);
        registerArtisanItem("summer_squash_pickles", ArtisanItem.ArtisanType.PICKLE, 64, 28, 45, true, 0);
        registerArtisanItem("taro_root_pickles", ArtisanItem.ArtisanType.PICKLE, 39, 17, 100, true, 0);
        registerArtisanItem("tea_leaves_pickles", ArtisanItem.ArtisanType.PICKLE, 0, 0, 50, false, 0);
        registerArtisanItem("tomato_pickles", ArtisanItem.ArtisanType.PICKLE, 21, 9, 60, true, 0);
        registerArtisanItem("unmilled_rice_pickles", ArtisanItem.ArtisanType.PICKLE, 3, 1, 30, true, 0);
        registerArtisanItem("wheat_pickles", ArtisanItem.ArtisanType.PICKLE, 0, 0, 25, false, 0);
        registerArtisanItem("yam_pickles", ArtisanItem.ArtisanType.PICKLE, 46, 20, 160, true, 0);

        // Special drinks (fixed values, not formula-based)
        registerSpecialDrink("beer", 50, 22, 200, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("pale_ale", 50, 22, 300, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("mead", 75, 33, 300, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("green_tea", 13, 5, 100,
            new StatusEffectInstance(StatusEffects.SPEED, 5040, 0),
            new StatusEffectInstance(ModStatusEffects.MAX_ENERGY_BUFF, 5040, 0));

        // Quality variants for special drinks
        registerSpecialDrink("beer_silver", 70, 30, 250, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("beer_gold", 90, 39, 300, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("beer_iridium", 130, 57, 400, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("pale_ale_silver", 70, 30, 375, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("pale_ale_gold", 90, 39, 450, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("pale_ale_iridium", 130, 57, 600, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("mead_silver", 105, 46, 375, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("mead_gold", 135, 59, 450, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
        registerSpecialDrink("mead_iridium", 195, 85, 600, new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));

        // Cheese quality variants
        registerArtisanItem("cheese_silver", ArtisanItem.ArtisanType.JELLY, 125, 56, 0, true, 1);
        registerArtisanItem("cheese_gold", ArtisanItem.ArtisanType.JELLY, 125, 56, 0, true, 2);
        registerArtisanItem("cheese_iridium", ArtisanItem.ArtisanType.JELLY, 125, 56, 0, true, 3);

        // Goat cheese (base + quality)
        registerArtisanItem("goat_cheese", ArtisanItem.ArtisanType.JELLY, 125, 56, 0, true, 0);
        registerArtisanItem("goat_cheese_silver", ArtisanItem.ArtisanType.JELLY, 125, 56, 0, true, 1);
        registerArtisanItem("goat_cheese_gold", ArtisanItem.ArtisanType.JELLY, 125, 56, 0, true, 2);
        registerArtisanItem("goat_cheese_iridium", ArtisanItem.ArtisanType.JELLY, 125, 56, 0, true, 3);

        // Roe (fresh)
        registerRoeItems("albacore_roe", "anchovy_roe", "angler_roe", "blobfish_roe",
            "blue_discus_roe", "bream_roe", "bullhead_roe", "carp_roe", "catfish_roe", "chub_roe",
            "clam_roe", "cockle_roe", "crab_roe", "crayfish_roe", "crimsonfish_roe",
            "dorado_roe", "eel_roe", "flounder_roe", "ghostfish_roe", "glacierfish_jr._roe",
            "glacierfish_roe", "goby_roe", "halibut_roe", "herring_roe", "ice_pip_roe",
            "largemouth_bass_roe", "lava_eel_roe", "legend_ii_roe", "legend_roe", "lingcod_roe",
            "lionfish_roe", "lobster_roe", "midnight_carp_roe", "midnight_squid_roe",
            "ms._angler_roe", "mussel_roe", "mutant_carp_roe", "octopus_roe", "oyster_roe",
            "perch_roe", "periwinkle_roe", "pike_roe", "pufferfish_roe", "radioactive_carp_roe",
            "rainbow_trout_roe", "red_mullet_roe", "red_snapper_roe", "salmon_roe",
            "sandfish_roe", "sardine_roe", "scorpion_carp_roe", "sea_cucumber_roe",
            "sea_urchin_roe", "shad_roe", "shrimp_roe", "slimejack_roe", "smallmouth_bass_roe",
            "snail_roe", "son_of_crimsonfish_roe", "spook_fish_roe", "squid_roe",
            "stingray_roe", "stonefish_roe", "sturgeon_roe", "sunfish_roe",
            "super_cucumber_roe", "tiger_trout_roe", "tilapia_roe", "tuna_roe",
            "void_salmon_roe", "walleye_roe", "woodskip_roe");

        // Aged roe (all)
        registerAgedRoeItems("aged_albacore_roe", "aged_anchovy_roe", "aged_angler_roe",
            "aged_blobfish_roe", "aged_blue_discus_roe", "aged_bream_roe", "aged_bullhead_roe",
            "aged_carp_roe", "aged_catfish_roe", "aged_chub_roe", "aged_clam_roe",
            "aged_cockle_roe", "aged_crab_roe", "aged_crayfish_roe", "aged_crimsonfish_roe",
            "aged_dorado_roe", "aged_eel_roe", "aged_flounder_roe", "aged_ghostfish_roe",
            "aged_glacierfish_jr._roe", "aged_glacierfish_roe", "aged_goby_roe",
            "aged_halibut_roe", "aged_herring_roe", "aged_ice_pip_roe",
            "aged_largemouth_bass_roe", "aged_lava_eel_roe", "aged_legend_ii_roe",
            "aged_legend_roe", "aged_lingcod_roe", "aged_lionfish_roe", "aged_lobster_roe",
            "aged_midnight_carp_roe", "aged_midnight_squid_roe", "aged_ms._angler_roe",
            "aged_mussel_roe", "aged_mutant_carp_roe", "aged_octopus_roe", "aged_oyster_roe",
            "aged_perch_roe", "aged_periwinkle_roe", "aged_pike_roe", "aged_pufferfish_roe",
            "aged_radioactive_carp_roe", "aged_rainbow_trout_roe", "aged_red_mullet_roe",
            "aged_red_snapper_roe", "aged_salmon_roe", "aged_sandfish_roe", "aged_sardine_roe",
            "aged_scorpion_carp_roe", "aged_sea_cucumber_roe", "aged_sea_urchin_roe",
            "aged_shad_roe", "aged_shrimp_roe", "aged_slimejack_roe",
            "aged_smallmouth_bass_roe", "aged_snail_roe", "aged_son_of_crimsonfish_roe",
            "aged_spook_fish_roe", "aged_squid_roe", "aged_stingray_roe", "aged_stonefish_roe",
            "aged_sunfish_roe", "aged_super_cucumber_roe", "aged_tiger_trout_roe",
            "aged_tilapia_roe", "aged_tuna_roe", "aged_void_salmon_roe", "aged_walleye_roe",
            "aged_woodskip_roe");

        // Caviar
        registerArtisanItem("caviar", ArtisanItem.ArtisanType.CAVIAR, 175, 78, 500, true, 0);

        // Dried mushrooms (价格 = 基础价格 × 7.5 + 25, 恢复 = 基础恢复 × 3)
        registerSpecialDrink("dried_common_mushrooms", 38 * 3, 17 * 3, (int)(40 * 7.5f + 25), false);
        registerSpecialDrink("dried_chanterelles", 75 * 3, 33 * 3, (int)(160 * 7.5f + 25), false);
        registerSpecialDrink("dried_magma_caps", 175 * 3, 78 * 3, (int)(400 * 7.5f + 25), false);
        registerSpecialDrink("dried_morels", 20 * 3, 9 * 3, (int)(150 * 7.5f + 25), false);
        registerSpecialDrink("dried_purple_mushrooms", 125 * 3, 56 * 3, (int)(250 * 7.5f + 25), false);

        // Smoked fish (每条鱼单独注册)
        registerSmokedFishItems();
    }

    private static void registerSmokedFishItems() {
        Object[][] fishData = {
            {"pufferfish", 200, -100, 0, true},
            {"anchovy", 30, 13, 5, true},
            {"bream", 45, 25, 11, true},
            {"tuna", 100, 38, 17, true},
            {"sardine", 40, 13, 5, true},
            {"bullhead", 45, 13, 5, true},
            {"largemouth_bass", 100, 38, 17, true},
            {"smallmouth_bass", 50, 25, 11, true},
            {"rainbow_trout", 65, 24, 11, true},
            {"salmon", 75, 38, 17, true},
            {"walleye", 105, 30, 13, true},
            {"perch", 55, 25, 11, true},
            {"carp", 30, 13, 5, true},
            {"catfish", 200, 50, 22, true},
            {"pike", 100, 38, 17, true},
            {"sunfish", 30, 13, 5, true},
            {"red_mullet", 75, 25, 11, true},
            {"herring", 30, 13, 5, true},
            {"eel", 85, 30, 13, true},
            {"octopus", 150, 0, 0, false},
            {"red_snapper", 50, 25, 11, true},
            {"squid", 80, 25, 11, true},
            {"sea_cucumber", 75, -25, 0, true},
            {"super_cucumber", 250, 125, 56, true},
            {"ghostfish", 45, 38, 17, true},
            {"stonefish", 300, 0, 0, false},
            {"ice_pip", 500, 38, 17, true},
            {"lava_eel", 700, 50, 22, true},
            {"sandfish", 75, 13, 5, true},
            {"scorpion_carp", 150, -125, 0, true},
            {"flounder", 150, 38, 17, true},
            {"midnight_carp", 150, 50, 22, true},
            {"sturgeon", 200, 25, 11, true},
            {"tiger_trout", 150, 25, 11, true},
            {"tilapia", 75, 25, 11, true},
            {"chub", 50, 25, 11, true},
            {"dorado", 100, 25, 11, true},
            {"albacore", 75, 25, 11, true},
            {"shad", 60, 25, 11, true},
            {"lingcod", 120, 25, 11, true},
            {"halibut", 80, 25, 11, true},
            {"woodskip", 75, 25, 11, true},
            {"void_salmon", 150, 63, 28, true},
            {"slimejack", 100, 38, 17, true},
            {"stingray", 180, 38, 17, true},
            {"lionfish", 100, 38, 17, true},
            {"blue_discus", 120, 38, 17, true},
            {"goby", 150, -62, 0, true},
            {"midnight_squid", 100, 38, 17, true},
            {"spook_fish", 220, 38, 17, true},
            {"blobfish", 500, 38, 17, true},
            {"crimsonfish", 1500, 38, 17, true},
            {"angler", 900, 25, 11, true},
            {"legend", 5000, 500, 225, true},
            {"glacierfish", 1000, 25, 11, true},
            {"mutant_carp", 1000, 25, 11, true},
            {"son_of_crimsonfish", 1500, 38, 17, true},
            {"ms._angler", 900, 38, 17, true},
            {"legend_ii", 5000, 500, 225, true},
            {"glacierfish_jr", 1000, 25, 11, true},
            {"radioactive_carp", 1000, 25, 11, true},
            {"lobster", 120, 0, 0, false},
            {"crayfish", 75, 0, 0, false},
            {"crab", 100, 0, 0, false},
            {"shrimp", 60, 0, 0, false},
            {"snail", 65, 0, 0, false},
            {"periwinkle", 20, 0, 0, false},
            {"cockle", 50, 0, 0, false},
            {"mussel", 30, 0, 0, false},
            {"oyster", 40, 0, 0, false},
            {"clam", 50, 0, 0, false},
        };

        java.util.Set<String> onlySilver = java.util.Set.of("lobster", "crayfish", "crab", "shrimp", "snail", "periwinkle");

        for (Object[] data : fishData) {
            String name = (String) data[0];
            int price = (int) data[1];
            int stamina = (int) data[2];
            int health = (int) data[3];
            boolean hasFood = (boolean) data[4];
            boolean isSilverOnly = onlySilver.contains(name);

            int[] qualities = isSilverOnly ? new int[]{0, 1} : new int[]{0, 1, 2, 3};

            for (int qt : qualities) {
                String suffix = switch (qt) {
                    case 0 -> "";
                    case 1 -> "_sliver";
                    case 2 -> "_gold";
                    case 3 -> "_iridium";
                    default -> "";
                };
                String itemId = "smoked_" + name + suffix;
                registerArtisanItem(itemId, ArtisanItem.ArtisanType.SMOKED_FISH, stamina, health, price, hasFood, qt);
            }
        }
    }

    private static void registerArtisanItem(String name, ArtisanItem.ArtisanType type, int baseStamina, int baseHeal, int basePrice, boolean hasFood, int qualityTier) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(999);

        float qualityHealMul = switch (qualityTier) { case 0 -> 1.0f; case 1 -> 1.4f; case 2 -> 1.8f; case 3 -> 2.6f; default -> 1.0f; };

        boolean hasPositiveFood = hasFood && baseStamina > 0;
        int finalStamina = 0;
        switch (type) {
            case WINE -> {
                if (hasPositiveFood) finalStamina = Math.round(baseStamina * 1.75f * qualityHealMul);
                else if (hasFood && baseStamina < 0) finalStamina = Math.round(baseStamina * qualityHealMul);
                else finalStamina = Math.round(basePrice * 0.1f * qualityHealMul);
            }
            case JELLY -> {
                if (hasPositiveFood) finalStamina = Math.round(baseStamina * 2.0f * qualityHealMul);
                else if (hasFood && baseStamina < 0) finalStamina = Math.round(baseStamina * qualityHealMul);
                else finalStamina = Math.round(basePrice * 0.2f * qualityHealMul);
            }
            case DRIED_FRUIT -> {
                if (hasPositiveFood) finalStamina = Math.round(baseStamina * 3.0f * qualityHealMul);
                else if (hasFood && baseStamina < 0) finalStamina = Math.round(baseStamina * qualityHealMul);
                else finalStamina = Math.round(basePrice * 0.5f * qualityHealMul);
            }
            case JUICE -> {
                if (hasPositiveFood) finalStamina = Math.round(baseStamina * 2.0f * qualityHealMul);
                else if (hasFood && baseStamina < 0) finalStamina = Math.round(baseStamina * qualityHealMul);
                else finalStamina = Math.round(basePrice * 0.4f * qualityHealMul);
            }
            case PICKLE -> {
                if (hasPositiveFood) finalStamina = Math.round(baseStamina * 1.75f * qualityHealMul);
                else if (hasFood && baseStamina < 0) finalStamina = Math.round(baseStamina * qualityHealMul);
                else finalStamina = Math.round(basePrice * 0.25f * qualityHealMul);
            }
            case SMOKED_FISH -> {
                if (hasPositiveFood) finalStamina = Math.round(baseStamina * 1.5f * qualityHealMul);
                else if (hasFood && baseStamina < 0) finalStamina = Math.round(baseStamina * 1.5f * qualityHealMul);
                else finalStamina = Math.round(basePrice * 2.0f / 3.0f * qualityHealMul);
            }
            case ROE -> {
                if (hasPositiveFood) finalStamina = Math.round(baseStamina * qualityHealMul);
                else if (hasFood && baseStamina < 0) finalStamina = Math.round(baseStamina * qualityHealMul);
                else finalStamina = 0;
            }
            case AGED_ROE -> {
                if (hasPositiveFood) finalStamina = Math.round(baseStamina * qualityHealMul);
                else finalStamina = 0;
            }
            case CAVIAR -> {
                if (hasPositiveFood) finalStamina = Math.round(baseStamina * qualityHealMul);
                else finalStamina = 0;
            }
            default -> {}
        }

        if (finalStamina > 0) {
            int nutrition = calcNutrition(finalStamina);
            float saturation = calcSaturation(finalStamina);
            settings = settings.food(new FoodComponent.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .alwaysEdible()
                .build());
        }

        if (type == ArtisanItem.ArtisanType.WINE || type == ArtisanItem.ArtisanType.JUICE) {
            settings = makeDrink(settings);
        }

        ArtisanItem item = new ArtisanItem(settings, type, baseStamina, baseHeal, basePrice, hasFood, qualityTier);
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static int calcNutrition(int stamina) {
        float raw = stamina * 20.0f / 270.0f;
        if (raw < 1) return 1;
        if (raw <= 16) return (int) Math.floor(raw);
        return 16;
    }

    private static float calcSaturation(int stamina) {
        float raw = stamina * 20.0f / 270.0f;
        int nutrition;
        if (raw < 1) return 0.0f;
        if (raw <= 16) {
            nutrition = (int) Math.floor(raw);
            return (raw - nutrition) / (nutrition * 2.0f);
        }
        nutrition = 16;
        float rawSat = (raw - 16) * 2;
        return rawSat / (nutrition * 2.0f);
    }

    private static void registerRoeItems(String... names) {
        for (String name : names) {
            // 从鱼籽名提取鱼名（去掉 "_roe" 后缀，特殊处理 glacierfish_jr._roe）
            String fishName = name.endsWith("_roe") ? name.substring(0, name.length() - 4) : name;
            if (fishName.endsWith(".")) fishName = fishName.substring(0, fishName.length() - 1); // glacierfish_jr.
            Integer fishPrice = FISH_PRICES.get(fishName);
            int roePrice = fishPrice != null ? 30 + fishPrice / 2 : 30;
            registerArtisanItem(name, ArtisanItem.ArtisanType.ROE, 50, 22, roePrice, true, 0);
        }
    }

    private static void registerAgedRoeItems(String... names) {
        for (String name : names) {
            // 从腌制鱼籽名提取鱼名（去掉 "aged_" 前缀和 "_roe" 后缀）
            String roeName = name.startsWith("aged_") ? name.substring(5) : name;
            String fishName = roeName.endsWith("_roe") ? roeName.substring(0, roeName.length() - 4) : roeName;
            if (fishName.endsWith(".")) fishName = fishName.substring(0, fishName.length() - 1);
            Integer fishPrice = FISH_PRICES.get(fishName);
            int roePrice = fishPrice != null ? 30 + fishPrice / 2 : 30;
            registerArtisanItem(name, ArtisanItem.ArtisanType.AGED_ROE, 100, 45, roePrice, true, 0);
        }
    }

    private static void registerRingItems() {
        String[] ringNames = {
            "amethyst_ring", "aquamarine_ring", "burglars_ring", "crabshell_ring", "emerald_ring",
            "glow_ring", "glowstone_ring", "hot_java_ring", "immunity_band", "iridium_band",
            "jade_ring", "jukebox_ring", "lucky_ring", "magnet_ring", "napalm_ring",
            "phoenix_ring", "protection_ring", "ring_of_yoba", "ruby_ring", "savage_ring",
            "slime_charmer_ring", "small_glow_ring", "small_magnet_ring", "soul_sapper_ring",
            "sturdy_ring", "thorns_ring", "topaz_ring", "vampire_ring", "warrior_ring"
        };
        for (String name : ringNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item = new Item(new Item.Settings().registryKey(key).maxCount(1));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
    }

    private static void registerRefinedItems() {
        // 有食物属性的精炼物品
        registerFoodRefinedItem("field_snack", 45, 18, 0);
        registerFoodRefinedItem("bug_steak", 45, 30, 0);
        registerFoodRefinedItem("life_elixir", 200, 80, 0, true);

        // 无食物属性的精炼物品
        String[] refinedNames = {
            "explosive_ammo", "fairy_dust",
            "staircase"
        };
        for (String name : refinedNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item;
            if (name.equals("fairy_dust")) {
                item = new FairyDustItem(new Item.Settings().registryKey(key).maxCount(999));
            } else {
                item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            }
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }

        // 蒜油：可饮用，减少怪物刷新上限为1/2
        registerBuffDrink("oil_of_garlic", ModStatusEffects.OIL_OF_GARLIC, 12000);
        // 怪兽香水：可饮用，增加怪物刷新上限为2倍
        registerBuffDrink("monster_musk", ModStatusEffects.MONSTER_MUSK, 12000);

        // 雨水图腾 - 自定义物品
        Identifier rainTotemId = Identifier.of(StardewValley.MOD_ID, "rain_totem");
        RegistryKey<Item> rainTotemKey = RegistryKey.of(RegistryKeys.ITEM, rainTotemId);
        RainTotemItem rainTotem = new RainTotemItem(new Item.Settings().registryKey(rainTotemKey).maxCount(999));
        Registry.register(Registries.ITEM, rainTotemKey, rainTotem);
        ITEMS.put("rain_totem", rainTotem);

        // 传送图腾 - 自定义物品（海滩、山岭、农场、沙漠、姜岛）
        String[] warpTypes = {"beach", "mountains", "farm", "desert", "island"};
        for (String type : warpTypes) {
            String itemName = "warp_totem_" + type;
            Identifier id = Identifier.of(StardewValley.MOD_ID, itemName);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            WarpTotemItem item = new WarpTotemItem(new Item.Settings().registryKey(key).maxCount(999), type);
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(itemName, item);
        }

    }

    private static void registerFoodRefinedItem(String name, int stamina, int healValue, int moneyValue) {
        registerFoodRefinedItem(name, stamina, healValue, moneyValue, false);
    }

    private static void registerFoodRefinedItem(String name, int stamina, int healValue, int moneyValue, boolean isDrink) {
        RawFoodData rawFood = new RawFoodData(stamina, healValue, moneyValue);
        FoodData food = calcFood(rawFood.stamina(), rawFood.healValue(), rawFood.moneyValue());
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(999).food(
            new FoodComponent.Builder()
                .nutrition(food.nutrition())
                .saturationModifier(food.saturationModifier())
                .alwaysEdible()
                .build());
        if (isDrink) {
            settings = makeDrink(settings);
        }
        DishItem item = new DishItem(settings, food.healAmount());
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static void registerWildSeedItems() {
        String[] seedNames = {
            "ancient_seed", "grass_starter"
        };
        for (String name : seedNames) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item item;
            if (name.equals("grass_starter")) {
                item = new GrassStarterItem(new Item.Settings().registryKey(key).maxCount(999));
            } else {
                item = new Item(new Item.Settings().registryKey(key).maxCount(999));
            }
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(name, item);
        }
    }

    private static final ConsumableComponent DRINK_NO_PARTICLES = ConsumableComponent.builder()
        .consumeSeconds(1.6f)
        .useAction(UseAction.DRINK)
        .sound(SoundEvents.ENTITY_GENERIC_DRINK)
        .consumeParticles(false)
        .build();

    private static Item.Settings makeDrink(Item.Settings settings) {
        return settings.component(DataComponentTypes.CONSUMABLE, DRINK_NO_PARTICLES);
    }

    private static void registerSpecialDrink(String name, int stamina, int heal, int sellPrice, StatusEffectInstance... effects) {
        registerSpecialDrink(name, stamina, heal, sellPrice, true, effects);
    }

    private static void registerSpecialDrink(String name, int stamina, int heal, int sellPrice, boolean isDrink, StatusEffectInstance... effects) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        int nutrition = calcNutrition(stamina);
        float saturation = calcSaturation(stamina);
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(999);
        if (stamina > 0) {
            settings = settings.food(new FoodComponent.Builder()
                .nutrition(nutrition).saturationModifier(saturation).alwaysEdible().build());
        }
        if (isDrink) {
            settings = makeDrink(settings);
        }
        ArtisanItem item = new ArtisanItem(settings, stamina, heal / 5.0f, sellPrice, stamina > 0, isDrink, effects);
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static void registerTonic(String name, int stamina, int healValue) {
        RawFoodData rawFood = new RawFoodData(stamina, healValue, 0);
        FoodData food = calcFood(rawFood.stamina(), rawFood.healValue(), rawFood.moneyValue());
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(999).food(
            new FoodComponent.Builder()
                .nutrition(food.nutrition())
                .saturationModifier(food.saturationModifier())
                .alwaysEdible()
                .build());
        settings = makeDrink(settings);
        DishItem item = new DishItem(settings, food.healAmount());
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static void registerBuffDrink(String name, RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect, int durationTicks) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(999).food(
            new FoodComponent.Builder().nutrition(1).saturationModifier(0).alwaysEdible().build());
        settings = makeDrink(settings);
        DishItem item = new DishItem(settings, 0, new StatusEffectInstance(effect, durationTicks, 0));
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static void registerSlingshotItems() {
        // 弹弓 - 作为弓使用，无限耐久
        record SlingshotDef(String name, boolean isMaster) {}
        SlingshotDef[] slingshots = {
            new SlingshotDef("slingshot", false),
            new SlingshotDef("master_slingshot", true)
        };
        for (SlingshotDef s : slingshots) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, s.name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            ModSlingshotItem item = new ModSlingshotItem(
                new Item.Settings().registryKey(key).maxCount(1).maxDamage(0),
                s.isMaster
            );
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(s.name, item);
        }
    }

    private static void registerBookItems() {
        // 任务书 - 可重复使用的mod指南书
        {
            Identifier id = Identifier.of(StardewValley.MOD_ID, "guide_book");
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            GuideBookItem item = new GuideBookItem(new Item.Settings().registryKey(key).maxCount(1));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put("guide_book", item);
        }

        // 星之书 - 可重复使用，获得所有技能250XP
        {
            Identifier id = Identifier.of(StardewValley.MOD_ID, "book_of_stars");
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            BookOfStarsItem item = new BookOfStarsItem(new Item.Settings().registryKey(key).maxCount(999));
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put("book_of_stars", item);
        }

        // 一次性书籍 - 普通效果由Mixin实现
        record BookDef(String name, java.util.function.BiFunction<Item.Settings, String, StardewBookItem> factory) {}
        BookDef[] oneTimeBooks = {
            new BookDef("the_alleyway_buffet", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("the_art_o_crabbing", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("jewels_of_the_sea", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("ways_of_the_wild", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("woodys_secret", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("jack_be_nimble_jack_be_thick", (s, n) -> new CopperWallBookItem(s)),
            new BookDef("friendship_101", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("monster_compendium", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("mapping_cave_systems", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("treasure_appraisal_guide", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("way_of_the_wind_pt._1", (s, n) -> new WayOfWindBookItem(s, n)),
            new BookDef("way_of_the_wind_pt._2", (s, n) -> new WayOfWindBookItem(s, n)),
            new BookDef("horse_the_book", (s, n) -> new HorseBookItem(s)),
            new BookDef("ol_slitherlegs", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("queen_of_sauce_cookbook", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("price_catalogue", (s, n) -> new PriceCatalogueBookItem(s)),
            new BookDef("book_of_mysteries", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            }),
            new BookDef("animal_catalogue", (s, n) -> new AnimalCatalogueBookItem(s)),
            new BookDef("the_diamond_hunter", (s, n) -> new StardewBookItem(s, n, false) {
                @Override protected void applyEffect(ServerWorld world, PlayerEntity player) {}
            })
        };
        for (BookDef book : oneTimeBooks) {
            Identifier id = Identifier.of(StardewValley.MOD_ID, book.name);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            StardewBookItem item = book.factory.apply(new Item.Settings().registryKey(key).maxCount(999), book.name);
            Registry.register(Registries.ITEM, key, item);
            ITEMS.put(book.name, item);
        }
    }

}