package stardewvalley.modid.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.block.ModBlocks;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModItemGroups {

    public static final ItemGroup CROPS_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "crops"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.crops"))
            .icon(() -> new ItemStack(ModItems.ITEMS.get("amaranth")))
            .entries((context, entries) -> {
                entries.add(ModBlocks.AMARANTH_SEEDS);
                entries.add(ModBlocks.ANCIENT_FRUIT_SEEDS);
                entries.add(ModBlocks.ARTICHOKE_SEEDS);
                entries.add(ModBlocks.BEET_SEEDS);
                entries.add(ModBlocks.BLUEBERRY_SEEDS);
                entries.add(ModBlocks.BLUEJAZZ_SEEDS);
                entries.add(ModBlocks.BOKCHOY_SEEDS);
                entries.add(ModBlocks.BROCCOLI_SEEDS);
                entries.add(ModBlocks.CACTUSFRUIT_SEEDS);
                entries.add(ModBlocks.PARSNIP_SEEDS);
                entries.add(ModBlocks.PINEAPPLE_SEEDS);
                entries.add(ModBlocks.POPPY_SEEDS);
                entries.add(ModBlocks.POTATO_SEEDS);
                entries.add(ModBlocks.POWDERMELON_SEEDS);
                entries.add(ModBlocks.PUMPKIN_SEEDS);
                entries.add(ModBlocks.QIGUA_SEEDS);
                entries.add(ModBlocks.RADISH_SEEDS);
                entries.add(ModBlocks.REDCABBAGE_SEEDS);
                entries.add(ModBlocks.RHUBARB_SEEDS);
                entries.add(ModBlocks.STARFRUIT_SEEDS);
                entries.add(ModBlocks.STRAWBERRY_SEEDS);
                entries.add(ModBlocks.SUMMERSPANGLE_SEEDS);
                entries.add(ModBlocks.SUMMERSQUASH_SEEDS);
                entries.add(ModBlocks.SUNFLOWER_SEEDS);
                entries.add(ModBlocks.SWEETGEMBERRY_SEEDS);
                entries.add(ModBlocks.TAROROOT_SEEDS);
                entries.add(ModBlocks.TEALEAVES_SEEDS);
                entries.add(ModBlocks.TOMATO_SEEDS);
                entries.add(ModBlocks.TULIP_SEEDS);
                entries.add(ModBlocks.UNMILLEDRICE_SEEDS);
                entries.add(ModBlocks.WHEAT_SEEDS);
                entries.add(ModBlocks.YAM_SEEDS);
                entries.add(ModBlocks.CARROT_SEEDS);
                entries.add(ModBlocks.CAULIFLOWER_SEEDS);
                entries.add(ModBlocks.CORN_SEEDS);
                entries.add(ModBlocks.CRANBERRIES_SEEDS);
                entries.add(ModBlocks.EGGPLANT_SEEDS);
                entries.add(ModBlocks.FAIRYROSE_SEEDS);
                entries.add(ModBlocks.FIBER_SEEDS);
                entries.add(ModBlocks.GARLIC_SEEDS);
                entries.add(ModBlocks.GRAPE_SEEDS);
                entries.add(ModBlocks.GREENBEAN_SEEDS);
                entries.add(ModBlocks.HOPS_SEEDS);
                entries.add(ModBlocks.HOTPEPPER_SEEDS);
                entries.add(ModBlocks.KALE_SEEDS);
                entries.add(ModBlocks.MELON_SEEDS);
                entries.add(ModBlocks.COFFEEBEAN_SEEDS);
                entries.add(ModBlocks.COFFEEBEAN_SEEDS_SILVER);
                entries.add(ModBlocks.COFFEEBEAN_SEEDS_GOLD);
                entries.add(ModBlocks.COFFEEBEAN_SEEDS_IRIDIUM);
                entries.add(ModItems.ITEMS.get("mixedseeds"));
                entries.add(ModItems.ITEMS.get("mixedflowerseeds"));
                entries.add(ModBlocks.SPRING_SEEDS);
                entries.add(ModBlocks.SUMMER_SEEDS);
                entries.add(ModBlocks.FALL_SEEDS);
                entries.add(ModBlocks.WINTER_SEEDS);
                java.util.Set<String> toolKeys = java.util.Set.of(
                    // Hoes
                    "hoe", "copper_hoe", "steel_hoe", "gold_hoe", "iridium_hoe",
                    // Axes
                    "axe", "copper_axe", "steel_axe", "gold_axe", "iridium_axe",
                    // Pickaxes
                    "pickaxe", "copper_pickaxe", "steel_pickaxe", "gold_pickaxe", "iridium_pickaxe",
                    // Watering cans
                    "watering_can", "copper_watering_can", "steel_watering_can", "gold_watering_can", "iridium_watering_can",
                    // Scythes
                    "scythe", "golden_scythe", "iridium_scythe",
                    // Fishing rods
                    "bamboo_pole", "fiberglass_rod", "iridium_rod", "advanced_iridium_rod",
                    // Other
                    "milk_pail", "shears"
                );
                java.util.Set<String> nonCropKeys = java.util.Set.of(
                    // Ores
                    "coal", "copper_ore", "copper_bar", "iron_ore", "iron_bar", "gold_ore", "gold_bar", "iridium_ore", "iridium_bar", "radioactive_ore", "radioactive_bar",
                    // Gems & geodes
                    "amethyst", "aquamarine", "diamond", "emerald", "earth_crystal", "fire_quartz", "frozen_geode", "geode", "jade", "magma_geode", "omni_geode", "prismatic_shard", "ruby", "topaz", "quartz", "tear_crystal", "refined_quartz",
                    // Geode minerals (晶球矿物)
                    "aerinite", "alamite", "baryte", "basalt", "bixite", "calcite", "celestine", "chalcocite",
                    "dolomite", "esperite", "fairy_stone", "fluorapatite", "geminite", "ghost_crystal", "granite",
                    "green_nickel_ore", "hematite", "iron_lead_ore", "jasper", "kyanite", "lemon_stone",
                    "limestone", "lunarite", "malachite", "marble", "mudstone", "neolite", "neptunite",
                    "ocean_stone", "opal", "orpiment", "petrified_slime", "pyrite", "sandstone",
                    "slate", "soapstone", "star_shards", "thunder_egg", "tigerseye", "tremolite", "wollastonite",
                    "obsidian", "ceramic_fragment", "fire_opal", "heliodor", "helvite", "jamborite", "jagoite", "nekoite",
                    // Misc
                "bone_fragment", "clay", "oak_resin", "pine_tar", "maple_syrup", "misc_stone", "moss",
                "broken_cd", "broken_glasses", "driftwood", "soggy_newspaper", "trash_item", "joja_cola",
                "wood", "hardwood", "bug_meat", "solar_essence",
                    // 动物制品/工匠物品
                    "cheese", "goat_cheese", "cheese_silver", "cheese_gold", "cheese_iridium", "goat_cheese_silver", "goat_cheese_gold", "goat_cheese_iridium",
                    "mayonnaise", "void_mayonnaise", "duck_mayonnaise", "gold_mayonnaise",
                    "mayonnaise_silver", "mayonnaise_iridium",
                    "void_mayonnaise_silver", "void_mayonnaise_gold", "void_mayonnaise_iridium",
                    "duck_mayonnaise_silver", "duck_mayonnaise_gold", "duck_mayonnaise_iridium",
                    "dinosaur_mayonnaise_silver", "dinosaur_mayonnaise_gold", "dinosaur_mayonnaise_iridium",
                    "coffee", "cloth", "goat_milk", "gold_egg",
                    "egg", "brown_egg", "milk", "wool", "slime",
                    "duck_egg", "large_egg", "large_brown_egg", "void_egg", "large_milk", "large_goat_milk",
                    "egg_silver", "egg_gold", "egg_iridium",
                    "brown_egg_silver", "brown_egg_gold", "brown_egg_iridium",
                    "large_egg_silver", "large_egg_gold", "large_egg_iridium",
                    "large_brown_egg_silver", "large_brown_egg_gold", "large_brown_egg_iridium",
                    "duck_egg_silver", "duck_egg_gold", "duck_egg_iridium",
                    "void_egg_silver", "void_egg_gold", "void_egg_iridium",
                    "gold_egg_silver", "gold_egg_gold", "gold_egg_iridium",
                    "dinosaur_egg_silver", "dinosaur_egg_gold", "dinosaur_egg_iridium",
                    "milk_silver", "milk_gold", "milk_iridium",
                    "large_milk_silver", "large_milk_gold", "large_milk_iridium",
                    "goat_milk_silver", "goat_milk_gold", "goat_milk_iridium",
                    "large_goat_milk_silver", "large_goat_milk_gold", "large_goat_milk_iridium",
                    "duck_feather_silver", "duck_feather_gold", "duck_feather_iridium",
                    "rabbits_foot_silver", "rabbits_foot_gold", "rabbits_foot_iridium",
                    "wool_silver", "wool_gold", "wool_iridium",
                    "truffle_silver", "truffle_gold", "truffle_iridium",
                    // 食物原材料
                    "vinegar", "wheat_flour", "oil", "sugar", "rice", "honey",
                    "squid_ink",
                    "aged_albacore_roe", "aged_anchovy_roe", "aged_angler_roe", "aged_blobfish_roe", "aged_blue_discus_roe", "aged_bream_roe", "aged_bullhead_roe", "aged_carp_roe", "aged_catfish_roe", "aged_chub_roe",
                    "aged_clam_roe", "aged_cockle_roe", "aged_crab_roe", "aged_crayfish_roe", "aged_crimsonfish_roe", "aged_dorado_roe", "aged_eel_roe", "aged_flounder_roe", "aged_ghostfish_roe", "aged_glacierfish_jr._roe",
                    "aged_glacierfish_roe", "aged_goby_roe", "aged_halibut_roe", "aged_herring_roe", "aged_ice_pip_roe", "aged_largemouth_bass_roe", "aged_lava_eel_roe", "aged_legend_ii_roe", "aged_legend_roe", "aged_lingcod_roe",
                    "aged_lionfish_roe", "aged_lobster_roe", "aged_midnight_carp_roe", "aged_midnight_squid_roe", "aged_ms._angler_roe", "aged_mussel_roe", "aged_mutant_carp_roe", "aged_octopus_roe", "aged_oyster_roe", "aged_perch_roe",
                    "aged_periwinkle_roe", "aged_pike_roe", "aged_pufferfish_roe", "aged_radioactive_carp_roe", "aged_rainbow_trout_roe", "aged_red_mullet_roe", "aged_red_snapper_roe", "aged_salmon_roe", "aged_sandfish_roe", "aged_sardine_roe",
                    "aged_scorpion_carp_roe", "aged_sea_cucumber_roe", "aged_sea_urchin_roe", "aged_shad_roe", "aged_shrimp_roe", "aged_slimejack_roe", "aged_smallmouth_bass_roe", "aged_snail_roe", "aged_son_of_crimsonfish_roe", "aged_spook_fish_roe",
                    "aged_squid_roe", "aged_stingray_roe", "aged_stonefish_roe", "aged_sunfish_roe", "aged_super_cucumber_roe", "aged_tiger_trout_roe", "aged_tilapia_roe", "aged_tuna_roe", "aged_void_salmon_roe", "aged_walleye_roe",
                    "aged_woodskip_roe", "caviar", "dried_ancient_fruit", "dried_apple", "dried_apricot", "dried_banana", "dried_blackberry", "dried_blueberry", "dried_cactus_fruit", "dried_cherry",
                    "dried_coconut", "dried_cranberries", "dried_crystal_fruit", "dried_hot_pepper", "dried_mango", "dried_melon", "dried_orange", "dried_peach", "dried_pineapple", "dried_pomegranate",
                    "dried_powdermelon", "dried_qi_fruit", "dried_rhubarb", "dried_salmonberry", "dried_spice_berry", "dried_starfruit", "dried_strawberry", "dried_wild_plum", "rasins", "ancient_fruit_jelly",
                    "apple_jelly", "apricot_jelly", "banana_jelly", "blackberry_jelly", "blueberry_jelly", "cactus_fruit_jelly", "cherry_jelly", "coconut_jelly", "cranberries_jelly", "crystal_fruit_jelly",
                    "grape_jelly", "hot_pepper_jelly", "mango_jelly", "melon_jelly", "orange_jelly", "peach_jelly", "pineapple_jelly", "pomegranate_jelly", "powdermelon_jelly",
                    "qi_fruit_jelly", "rhubarb_jelly", "salmonberry_jelly", "spice_berry_jelly", "starfruit_jelly", "strawberry_jelly", "wild_plum_jelly", "ancient_fruit_wine", "apple_wine", "apricot_wine", "banana_wine",
                    "blackberry_wine", "blueberry_wine", "cactus_fruit_wine", "cherry_wine", "coconut_wine", "cranberries_wine", "crystal_fruit_wine", "grape_wine", "hot_pepper_wine", "mango_wine",
                    "mead", "melon_wine", "orange_wine", "peach_wine", "pineapple_wine", "pomegranate_wine", "powdermelon_wine", "qi_fruit_wine", "rhubarb_wine", "salmonberry_wine",
                    "spice_berry_wine", "starfruit_wine", "strawberry_wine", "wild_plum_wine", "amaranth_juice", "artichoke_juice", "beer", "beet_juice", "bok_choy_juice", "broccoli_juice",
                    "carrot_juice", "cauliflower_juice", "corn_juice", "eggplant_juice", "fiddlehead_fern_juice", "garlic_juice", "green_bean_juice", "green_tea", "kale_juice", "pale_ale",
                    "parsnip_juice", "potato_juice", "pumpkin_juice", "radish_juice", "red_cabbage_juice", "summer_squash_juice", "taro_root_juice", "tomato_juice", "unmilled_rice_juice", "yam_juice",
                    "amaranth_pickles", "artichoke_pickles", "beet_pickles", "bok_choy_pickles", "broccoli_pickles", "carrot_pickles", "cauliflower_pickles", "corn_pickles", "eggplant_pickles", "fiddlehead_fern_pickles",
                    "garlic_pickles", "green_bean_pickles", "hops_pickles", "kale_pickles", "parsnip_pickles", "potato_pickles", "pumpkin_pickles", "radish_pickles", "red_cabbage_pickles", "summer_squash_pickles",
                    "taro_root_pickles", "tea_leaves_pickles", "tomato_pickles", "unmilled_rice_pickles", "wheat_pickles", "yam_pickles", "albacore_roe", "anchovy_roe", "angler_roe", "blobfish_roe",
                    "blue_discus_roe", "bream_roe", "bullhead_roe", "carp_roe", "catfish_roe", "chub_roe", "clam_roe", "cockle_roe", "crab_roe", "crayfish_roe",
                    "crimsonfish_roe", "dorado_roe", "eel_roe", "flounder_roe", "ghostfish_roe", "glacierfish_jr._roe", "glacierfish_roe", "goby_roe", "halibut_roe", "herring_roe",
                    "ice_pip_roe", "largemouth_bass_roe", "lava_eel_roe", "legend_ii_roe", "legend_roe", "lingcod_roe", "lionfish_roe", "lobster_roe", "midnight_carp_roe", "midnight_squid_roe",
                    "ms._angler_roe", "mussel_roe", "mutant_carp_roe", "octopus_roe", "oyster_roe", "perch_roe", "periwinkle_roe", "pike_roe", "pufferfish_roe", "radioactive_carp_roe",
                    "rainbow_trout_roe", "red_mullet_roe", "red_snapper_roe", "salmon_roe", "sandfish_roe", "sardine_roe", "scorpion_carp_roe", "sea_cucumber_roe", "sea_urchin_roe", "shad_roe",
                    "shrimp_roe", "slimejack_roe", "smallmouth_bass_roe", "snail_roe", "son_of_crimsonfish_roe", "spook_fish_roe", "squid_roe", "stingray_roe", "stonefish_roe", "sturgeon_roe",
                    "sunfish_roe", "super_cucumber_roe", "tiger_trout_roe", "tilapia_roe", "tuna_roe", "void_salmon_roe", "walleye_roe", "woodskip_roe",
                    "ancient_fruit_wine_silver", "apple_wine_silver", "apricot_wine_silver", "banana_wine_silver", "beer_silver", "blackberry_wine_silver", "blueberry_wine_silver", "cactus_fruit_wine_silver", "cherry_wine_silver", "coconut_wine_silver",
                    "cranberries_wine_silver", "crystal_fruit_wine_silver", "grape_wine_silver", "hot_pepper_wine_silver", "mango_wine_silver", "mead_silver", "melon_wine_silver", "orange_wine_silver", "pale_ale_silver", "peach_wine_silver",
                    "pineapple_wine_silver", "pomegranate_wine_silver", "powdermelon_wine_silver", "qi_fruit_wine_silver", "rhubarb_wine_silver", "salmonberry_wine_silver", "spice_berry_wine_silver", "starfruit_wine_silver", "strawberry_wine_silver", "wild_plum_wine_silver",
                    "ancient_fruit_wine_gold", "apple_wine_gold", "apricot_wine_gold", "banana_wine_gold", "beer_gold", "blackberry_wine_gold", "blueberry_wine_gold", "cactus_fruit_wine_gold", "cherry_wine_gold", "coconut_wine_gold",
                    "cranberries_wine_gold", "crystal_fruit_wine_gold", "grape_wine_gold", "hot_pepper_wine_gold", "mango_wine_gold", "mead_gold", "melon_wine_gold", "orange_wine_gold", "pale_ale_gold", "peach_wine_gold",
                    "pineapple_wine_gold", "pomegranate_wine_gold", "powdermelon_wine_gold", "qi_fruit_wine_gold", "rhubarb_wine_gold", "salmonberry_wine_gold", "spice_berry_wine_gold", "starfruit_wine_gold", "strawberry_wine_gold", "wild_plum_wine_gold",
                    "ancient_fruit_wine_iridium", "apple_wine_iridium", "apricot_wine_iridium", "banana_wine_iridium", "beer_iridium", "blackberry_wine_iridium", "blueberry_wine_iridium", "cactus_fruit_wine_iridium", "cherry_wine_iridium", "coconut_wine_iridium",
                    "cranberries_wine_iridium", "crystal_fruit_wine_iridium", "grape_wine_iridium", "hot_pepper_wine_iridium", "mango_wine_iridium", "mead_iridium", "melon_wine_iridium", "orange_wine_iridium", "pale_ale_iridium", "peach_wine_iridium",
                    "pineapple_wine_iridium", "pomegranate_wine_iridium", "powdermelon_wine_iridium", "qi_fruit_wine_iridium", "rhubarb_wine_iridium", "salmonberry_wine_iridium", "spice_berry_wine_iridium", "starfruit_wine_iridium", "strawberry_wine_iridium", "wild_plum_wine_iridium",
                    "dried_common_mushrooms", "dried_chanterelles", "dried_magma_caps", "dried_morels", "dried_purple_mushrooms",
                    "tulip_honey", "blue_jazz_honey", "summer_spangle_honey", "poppy_honey", "sunflower_honey", "fairy_rose_honey",
                    "dinosaur_egg", "truffle", "truffle_oil", "dinosaur_mayonnaise",
                    // 戒指 - 应在weapons标签栏
                    "amethyst_ring", "aquamarine_ring", "burglars_ring", "crabshell_ring", "emerald_ring",
                    "glow_ring", "glowstone_ring", "hot_java_ring", "immunity_band", "iridium_band",
                    "jade_ring", "jukebox_ring", "lucky_ring", "magnet_ring", "napalm_ring",
                    "phoenix_ring", "protection_ring", "ring_of_yoba", "ruby_ring", "savage_ring",
                    "slime_charmer_ring", "small_glow_ring", "small_magnet_ring", "soul_sapper_ring",
                    "sturdy_ring", "thorns_ring", "topaz_ring", "vampire_ring", "warrior_ring",
                    // 精炼物品 - 应在星露谷精炼物品标签栏
                    "field_snack", "bug_steak", "life_elixir", "bomb", "cherry_bomb", "mega_bomb",
                    "explosive_ammo", "fairy_dust", "monster_musk", "rain_totem", "staircase",
                    "warp_totem_beach", "warp_totem_mountains", "warp_totem_farm",
                    "warp_totem_desert", "warp_totem_island",
                    // void_essence - 应在杂项标签栏
                    "void_essence",
                    // 新物品
                    "bat_wing", "dragon_tooth", "battery_pack", "cinder_shard",
                    // hay - 杂物
                    "hay",
                    // 作者的话 - 特殊物品标签栏
                    "guide_book",
                    // 技能书
                    "stardew_valley_almanac", "woodcutters_weekly", "mining_monthly", "combat_quarterly", "bait_and_bobber",
                    // 动物制品
                    "duck_feather", "rabbits_foot",
                    // 精炼物品
                    "oil_of_garlic",
                    // 杂项/特殊
                    "golden_coconut", "ancient_treasure_decor", "dwarvish_safety_manual", "galaxy_soul",
                    "golden_pumpkin", "treasure_chest",
                    // 杂项/特殊靴子 - 应在武器标签栏
                    "sneakers", "rubber_boots", "leather_boots", "work_boots", "combat_boots",
                    "tundra_boots", "thermal_boots", "dark_boots", "firewalker_boots", "genie_shoes",
                    "space_boots", "cowboy_boots", "emilys_magic_boots", "leprechaun_shoes", "cinderclown_shoes",
                    "mermaid_boots", "dragonscale_boots", "crystal_shoes",
                    // 特殊物品 - 星露谷特殊标签栏
                    "mystery_box", "golden_mystery_box", "golden_animal_cracker",
                    // 古物 - 星露谷古物标签栏
                    "amphibian_fossil", "anchor", "ancient_doll", "ancient_drum",
                    "ancient_sword", "arrowhead", "bone_flute",
                    "chewing_stick", "chicken_statue", "chipped_amphora",
                    "dried_starfish", "dwarf_gadget",
                    "dwarf_scroll_i", "dwarf_scroll_ii", "dwarf_scroll_iii",
                    "dwarf_scroll_iv", "dwarvish_helm", "elvish_jewelry",
                    "glass_shards", "golden_mask", "golden_relic",
                    "nautilus_fossil", "ornamental_fan", "palm_fossil",
                    "prehistoric_handaxe", "prehistoric_rib", "prehistoric_scapula",
                    "prehistoric_skull", "prehistoric_tibia", "prehistoric_tool",
                    "prehistoric_vertebra", "rare_disc", "rusty_cog", "rusty_spoon",
                    "rusty_spur", "skeletal_hand", "skeletal_tail",
                    "strange_doll_green", "strange_doll_yellow", "trilobite"
                );
                java.util.Set<String> dishKeys = java.util.Set.of(
                    "algae_soup", "artichoke_dip", "autums_bounty", "baked_fish", "banana_pudding",
                    "bean_hotpot", "blackberry_cobbler", "blueberry_tart", "bread", "bruschetta",
                    "carp_surprise", "cheese_cauliflower", "chocolate_cake", "chowder", "coleslaw",
                    "complete_breakfast", "cookie", "crab_cakes", "cranberry_candy", "cranberry_sauce",
                    "crispy_bass", "dish_of_the_sea", "eggplant_parmesan", "escargot", "farmers_lunch",
                    "fiddlehead_risotto", "fish_stew", "fish_taco", "fried_calamari", "fried_eel",
                    "fried_egg", "fried_mushroom", "fruit_salad", "ginger_ale", "glazed_yams",
                    "hashbrowns", "ice_cream", "lobster_bisque", "lucky_lunch", "maki_roll",
                    "mango_sticky_rice", "maple_bar", "miners_treat", "moss_soup", "omelet",
                    "pale_broth", "pancakes", "parsnip_soup", "pepper_poppers", "pink_cake",
                    "pizza", "plum_pudding", "poi", "poppyseed_muffin", "pumpkin_pie",
                    "pumpkin_soup", "radish_salad", "red_plate", "rhubarb_pie", "rice_pudding",
                    "roasted_hazelnuts", "roots_platter", "salad", "salmon_dinner", "sashimi",
                    "seafoam_pudding", "shrimp_cocktail", "spaghetti", "spicy_eel", "squid_ink_ravioli",
                    "stir_fry", "strange_bun", "stuffing", "super_meal", "survival_burger",
                    "tom_kha_soup", "tortilla", "triple_shot_espresso", "tropical_curry", "trout_soup",
                    "vegetable_medley",
                    "energy_tonic", "muscle_remedy", "magic_rock_candy"
                );
                for (java.util.Map.Entry<String, Item> entry : ModItems.ITEMS.entrySet()) {
                    String key = entry.getKey();
                    Item item = entry.getValue();
                    if (key.startsWith("caiji_") || key.startsWith("fish_") || key.startsWith("bait_") || key.startsWith("fishtool_") || key.startsWith("smoked_") || key.startsWith("ammo_") || toolKeys.contains(key) || nonCropKeys.contains(key) || dishKeys.contains(key) || key.equals("stardrop") || key.equals("mixedseeds") || key.equals("mixedflowerseeds")) continue;
                    if (item instanceof ModWeaponItem || item instanceof ModDaggerItem) continue;
                    if (key.equals("slingshot") || key.equals("master_slingshot")) continue;
                    String[] bookKeys = {"stardew_valley_almanac", "woodcutters_weekly", "mining_monthly", "combat_quarterly", "bait_and_bobber", 
                        "book_of_stars", "book_of_mysteries", "the_alleyway_buffet", "the_art_o_crabbing",
                        "jewels_of_the_sea", "ways_of_the_wild", "woodys_secret", "jack_be_nimble_jack_be_thick",
                        "friendship_101", "monster_compendium", "mapping_cave_systems", "treasure_appraisal_guide",
                        "way_of_the_wind_pt._1", "way_of_the_wind_pt._2", "horse_the_book", "ol_slitherlegs",
                        "queen_of_sauce_cookbook", "price_catalogue", "animal_catalogue", "the_diamond_hunter"};
                    if (java.util.Arrays.asList(bookKeys).contains(key)) continue;
                    entries.add(item);
                }
            })
            .build()
    );


    public static final ItemGroup SPECIAL_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "special"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.special"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("stardrop", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                Item stardrop = ModItems.ITEMS.get("stardrop");
                if (stardrop != null) entries.add(stardrop);
                Item squidInk = ModItems.ITEMS.get("squid_ink");
                if (squidInk != null) entries.add(squidInk);
                // 银河之魂
                Item galaxySoul = ModItems.ITEMS.get("galaxy_soul");
                if (galaxySoul != null) entries.add(galaxySoul);
                // 特殊物品：谜之盒、金色谜之盒、金色动物饼干
                String[] specialItems = {"mystery_box", "golden_mystery_box", "golden_animal_cracker"};
                for (String name : specialItems) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // 技能书
                String[] skillBooks = {"stardew_valley_almanac", "woodcutters_weekly", "mining_monthly", "combat_quarterly", "bait_and_bobber"};
                for (String name : skillBooks) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // 矮人安全手册
                Item dwarvishManual = ModItems.ITEMS.get("dwarvish_safety_manual");
                if (dwarvishManual != null) entries.add(dwarvishManual);
                // 书籍
                String[] bookItems = {
                    "book_of_stars", "book_of_mysteries", "the_alleyway_buffet", "the_art_o_crabbing",
                    "jewels_of_the_sea", "ways_of_the_wild", "woodys_secret", "jack_be_nimble_jack_be_thick",
                    "friendship_101", "monster_compendium", "mapping_cave_systems", "treasure_appraisal_guide",
                    "way_of_the_wind_pt._1", "way_of_the_wind_pt._2", "horse_the_book", "ol_slitherlegs",
                    "queen_of_sauce_cookbook", "price_catalogue", "animal_catalogue", "the_diamond_hunter"
                };
                for (String name : bookItems) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // 作者的话（放到特殊物品标签栏最后）
                Item guideBook = ModItems.ITEMS.get("guide_book");
                if (guideBook != null) entries.add(guideBook);
            })
            .build()
    );

    public static final ItemGroup COLLECTIBLES_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "collectibles"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.collectibles"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("caiji_wildhorseradish", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                String[] caijiNames = {
                    "wildhorseradish", "daffodil", "leek", "dandelion", "springonion",
                    "morel", "commonmushroom", "salmonberry", "spiceberry", "sweetpea",
                    "fiddleheadfern", "wildplum", "hazelnut", "blackberry", "chanterelle",
                    "redmushroom", "purplemushroom", "winterroot", "crystalfruit", "snowyam",
                    "crocus", "holly", "nautilusshell", "coral", "seaurchin", "rainbow_shell",
                    "clam", "cockle", "mussel", "oyster", "seaweed", "cavecarrot",
                    "coconut", "ginger", "magmacap", "sap"
                };
                java.util.Set<String> noQuality = java.util.Set.of("seaweed", "cavecarrot", "ginger", "sap");
                for (String name : caijiNames) {
                    if (noQuality.contains(name)) {
                        Item item = ModItems.ITEMS.get("caiji_" + name);
                        if (item != null) entries.add(item);
                    } else {
                        for (stardewvalley.modid.crop.CropQuality q : stardewvalley.modid.crop.CropQuality.values()) {
                            Item item = ModItems.ITEMS.get("caiji_" + name + q.getSuffix());
                            if (item != null) entries.add(item);
                        }
                    }
                }
            })
            .build()
    );


    public static final ItemGroup TOOLS_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "tools"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.tools"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("axe", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                // Axes
                for (String name : new String[]{"axe","copper_axe","steel_axe","gold_axe","iridium_axe"}) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // Hoes
                for (String name : new String[]{"hoe","copper_hoe","steel_hoe","gold_hoe","iridium_hoe"}) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // Pickaxes
                for (String name : new String[]{"pickaxe","copper_pickaxe","steel_pickaxe","gold_pickaxe","iridium_pickaxe"}) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // Watering cans
                for (String name : new String[]{"watering_can","copper_watering_can","steel_watering_can","gold_watering_can","iridium_watering_can"}) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // Scythes
                for (String name : new String[]{"scythe","golden_scythe","iridium_scythe"}) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // Fishing rods
                for (String name : new String[]{"bamboo_pole","fiberglass_rod","iridium_rod","advanced_iridium_rod"}) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // Other
                for (String name : new String[]{"milk_pail","shears"}) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }

            })
            .build()
    );

    public static final ItemGroup ORES_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "ores"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.ores"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("copper_ore", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                String[] oreNames = {"coal", "copper_ore", "copper_bar", "iron_ore", "iron_bar", "gold_ore", "gold_bar", "iridium_ore", "iridium_bar", "radioactive_ore", "radioactive_bar"};
                for (String name : oreNames) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                String[] gemNames = {"amethyst", "aquamarine", "diamond", "emerald", "earth_crystal", "fire_quartz", "frozen_geode", "geode", "jade", "magma_geode", "omni_geode", "prismatic_shard", "ruby", "topaz", "quartz", "tear_crystal", "refined_quartz", "cinder_shard"};
                for (String name : gemNames) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                String[] geodeMineralNames = {"tigerseye", "opal", "fire_opal", "alamite", "bixite", "baryte", "aerinite", "calcite", "dolomite", "esperite", "fluorapatite", "geminite", "helvite", "jamborite", "jagoite", "kyanite", "lunarite", "malachite", "neptunite", "lemon_stone", "nekoite", "orpiment", "petrified_slime", "thunder_egg", "pyrite", "ocean_stone", "ghost_crystal", "jasper", "celestine", "marble", "sandstone", "granite", "basalt", "limestone", "soapstone", "hematite", "mudstone", "obsidian", "slate", "fairy_stone", "star_shards"};
                for (String name : geodeMineralNames) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
            })
            .build()
    );

    public static final ItemGroup MISC_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "misc"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.misc"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("clay", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                String[] miscNames = {"bone_fragment", "clay", "oak_resin", "pine_tar", "maple_syrup", "misc_stone", "moss", "slime",
                    "broken_cd", "broken_glasses", "driftwood", "soggy_newspaper", "trash_item", "joja_cola",
                    "wood", "hardwood", "bug_meat", "solar_essence", "void_essence",
                    "bat_wing", "dragon_tooth", "battery_pack", "hay",
                    "golden_coconut", "ancient_treasure_decor", "golden_pumpkin", "treasure_chest"};
                for (String name : miscNames) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                entries.add(ModBlocks.ACORN);
                entries.add(ModBlocks.MAPLE_SEED);
                entries.add(ModBlocks.PINE_CONE);
                entries.add(ModBlocks.MAHOGANY_SEED);
            })
            .build()
    );

    public static final ItemGroup FISH_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "fish"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.fish"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("fish_pufferfish", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                // 按指定顺序排列的鱼类
                java.util.Set<String> onlyNormal = java.util.Set.of("green_algae", "white_algae", "sea_jelly", "river_jelly", "cave_jelly");
                java.util.Set<String> onlySilver = java.util.Set.of("lobster", "crayfish", "crab", "shrimp", "snail", "periwinkle");
                String[] allQualities = {"", "_silver", "_gold", "_iridium"};
                String[] orderedFish = {
                    "pufferfish", "anchovy", "tuna", "sardine", "bream",
                    "largemouth_bass", "smallmouth_bass", "rainbow_trout", "salmon", "walleye",
                    "perch", "carp", "catfish", "pike", "sunfish",
                    "red_mullet", "herring", "eel", "octopus", "red_snapper",
                    "squid", "sea_cucumber", "super_cucumber", "ghostfish", "stonefish",
                    "ice_pip", "lava_eel", "sandfish", "scorpion_carp", "flounder",
                    "midnight_carp", "sturgeon", "tiger_trout", "bullhead", "tilapia",
                    "chub", "dorado", "albacore", "shad", "lingcod",
                    "halibut", "woodskip", "void_salmon", "slimejack", "stingray",
                    "lionfish", "blue_discus", "goby", "midnight_squid", "spook_fish",
                    "blobfish", "crimsonfish", "angler", "legend", "glacierfish",
                    "mutant_carp", "son_of_crimsonfish", "ms._angler", "legend_ii", "glacierfish_jr",
                    "radioactive_carp",
                    "lobster", "crayfish", "crab", "shrimp", "snail", "periwinkle",
                    "green_algae", "white_algae", "sea_jelly", "river_jelly", "cave_jelly"
                };
                for (String name : orderedFish) {
                    if (onlyNormal.contains(name)) {
                        Item item = ModItems.ITEMS.get("fish_" + name);
                        if (item != null) entries.add(item);
                    } else if (onlySilver.contains(name)) {
                        for (String suffix : new String[]{"", "_silver"}) {
                            Item item = ModItems.ITEMS.get("fish_" + name + suffix);
                            if (item != null) entries.add(item);
                        }
                    } else {
                        for (String suffix : allQualities) {
                            Item item = ModItems.ITEMS.get("fish_" + name + suffix);
                            if (item != null) entries.add(item);
                        }
                    }
                }
                // 贝壳类（caiji_ 前缀）
                String[] caijiFishNames = {"clam", "cockle", "mussel", "oyster", "seaweed"};
                for (String name : caijiFishNames) {
                    for (String suffix : allQualities) {
                        if ("seaweed".equals(name) && !suffix.isEmpty()) continue;
                        Item item = ModItems.ITEMS.get("caiji_" + name + suffix);
                        if (item != null) entries.add(item);
                    }
                }
                // 鱼饵物品
                String[] baitNames = {"bait_bait", "bait_challenge_bait", "bait_deluxe_bait", "bait_magic_bait", "bait_magnet", "bait_wild_bait"};
                for (String name : baitNames) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // 所有鱼的针对性鱼饵
                for (String fishName : stardewvalley.modid.crafting.CraftingMaterialSets.ALL_FISH_NAMES) {
                    String baitName = "bait_" + fishName + "_bait";
                    Item item = ModItems.ITEMS.get(baitName);
                    if (item != null) entries.add(item);
                }
                // 渔具物品
                String[] tackleNames = {"fishtool_barbed_hook", "fishtool_cork_bobber", "fishtool_curiosity_lure", "fishtool_dressed_spinner", "fishtool_lead_bobber", "fishtool_quality_bobber", "fishtool_sonar_bobber", "fishtool_spinner", "fishtool_trap_bobber", "fishtool_treasure_hunter"};
                for (String name : tackleNames) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
            })
            .build()
    );

    public static final ItemGroup DISHES_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "dishes"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.dishes"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("complete_breakfast", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                entries.add(ModItems.ITEMS.get("algae_soup"));
                entries.add(ModItems.ITEMS.get("artichoke_dip"));
                entries.add(ModItems.ITEMS.get("autums_bounty"));
                entries.add(ModItems.ITEMS.get("baked_fish"));
                entries.add(ModItems.ITEMS.get("banana_pudding"));
                entries.add(ModItems.ITEMS.get("bean_hotpot"));
                entries.add(ModItems.ITEMS.get("blackberry_cobbler"));
                entries.add(ModItems.ITEMS.get("blueberry_tart"));
                entries.add(ModItems.ITEMS.get("bread"));
                entries.add(ModItems.ITEMS.get("bruschetta"));
                entries.add(ModItems.ITEMS.get("carp_surprise"));
                entries.add(ModItems.ITEMS.get("cheese_cauliflower"));
                entries.add(ModItems.ITEMS.get("chocolate_cake"));
                entries.add(ModItems.ITEMS.get("chowder"));
                entries.add(ModItems.ITEMS.get("coleslaw"));
                entries.add(ModItems.ITEMS.get("complete_breakfast"));
                entries.add(ModItems.ITEMS.get("cookie"));
                entries.add(ModItems.ITEMS.get("crab_cakes"));
                entries.add(ModItems.ITEMS.get("cranberry_candy"));
                entries.add(ModItems.ITEMS.get("cranberry_sauce"));
                entries.add(ModItems.ITEMS.get("crispy_bass"));
                entries.add(ModItems.ITEMS.get("dish_of_the_sea"));
                entries.add(ModItems.ITEMS.get("eggplant_parmesan"));
                entries.add(ModItems.ITEMS.get("escargot"));
                entries.add(ModItems.ITEMS.get("farmers_lunch"));
                entries.add(ModItems.ITEMS.get("fiddlehead_risotto"));
                entries.add(ModItems.ITEMS.get("fish_stew"));
                entries.add(ModItems.ITEMS.get("fish_taco"));
                entries.add(ModItems.ITEMS.get("fried_calamari"));
                entries.add(ModItems.ITEMS.get("fried_eel"));
                entries.add(ModItems.ITEMS.get("fried_egg"));
                entries.add(ModItems.ITEMS.get("fried_mushroom"));
                entries.add(ModItems.ITEMS.get("fruit_salad"));
                entries.add(ModItems.ITEMS.get("ginger_ale"));
                entries.add(ModItems.ITEMS.get("glazed_yams"));
                entries.add(ModItems.ITEMS.get("hashbrowns"));
                entries.add(ModItems.ITEMS.get("ice_cream"));
                entries.add(ModItems.ITEMS.get("lobster_bisque"));
                entries.add(ModItems.ITEMS.get("lucky_lunch"));
                entries.add(ModItems.ITEMS.get("maki_roll"));
                entries.add(ModItems.ITEMS.get("mango_sticky_rice"));
                entries.add(ModItems.ITEMS.get("maple_bar"));
                entries.add(ModItems.ITEMS.get("miners_treat"));
                entries.add(ModItems.ITEMS.get("moss_soup"));
                entries.add(ModItems.ITEMS.get("omelet"));
                entries.add(ModItems.ITEMS.get("pale_broth"));
                entries.add(ModItems.ITEMS.get("pancakes"));
                entries.add(ModItems.ITEMS.get("parsnip_soup"));
                entries.add(ModItems.ITEMS.get("pepper_poppers"));
                entries.add(ModItems.ITEMS.get("pink_cake"));
                entries.add(ModItems.ITEMS.get("pizza"));
                entries.add(ModItems.ITEMS.get("plum_pudding"));
                entries.add(ModItems.ITEMS.get("poi"));
                entries.add(ModItems.ITEMS.get("poppyseed_muffin"));
                entries.add(ModItems.ITEMS.get("pumpkin_pie"));
                entries.add(ModItems.ITEMS.get("pumpkin_soup"));
                entries.add(ModItems.ITEMS.get("radish_salad"));
                entries.add(ModItems.ITEMS.get("red_plate"));
                entries.add(ModItems.ITEMS.get("rhubarb_pie"));
                entries.add(ModItems.ITEMS.get("rice_pudding"));
                entries.add(ModItems.ITEMS.get("roasted_hazelnuts"));
                entries.add(ModItems.ITEMS.get("roots_platter"));
                entries.add(ModItems.ITEMS.get("salad"));
                entries.add(ModItems.ITEMS.get("salmon_dinner"));
                entries.add(ModItems.ITEMS.get("sashimi"));
                entries.add(ModItems.ITEMS.get("seafoam_pudding"));
                entries.add(ModItems.ITEMS.get("shrimp_cocktail"));
                entries.add(ModItems.ITEMS.get("spaghetti"));
                entries.add(ModItems.ITEMS.get("spicy_eel"));
                entries.add(ModItems.ITEMS.get("squid_ink_ravioli"));
                entries.add(ModItems.ITEMS.get("stir_fry"));
                entries.add(ModItems.ITEMS.get("strange_bun"));
                entries.add(ModItems.ITEMS.get("stuffing"));
                entries.add(ModItems.ITEMS.get("super_meal"));
                entries.add(ModItems.ITEMS.get("survival_burger"));
                entries.add(ModItems.ITEMS.get("tom_kha_soup"));
                entries.add(ModItems.ITEMS.get("tortilla"));
                entries.add(ModItems.ITEMS.get("triple_shot_espresso"));
                entries.add(ModItems.ITEMS.get("tropical_curry"));
                entries.add(ModItems.ITEMS.get("trout_soup"));
                entries.add(ModItems.ITEMS.get("vegetable_medley"));
                entries.add(ModItems.ITEMS.get("energy_tonic"));
                entries.add(ModItems.ITEMS.get("muscle_remedy"));
                entries.add(ModItems.ITEMS.get("magic_rock_candy"));
            })
            .build()
    );

    public static final ItemGroup ARTISAN_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "artisan"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.artisan"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("cheese", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                entries.add(ModItems.ITEMS.get("vinegar"));
                entries.add(ModItems.ITEMS.get("wheat_flour"));
                entries.add(ModItems.ITEMS.get("oil"));
                entries.add(ModItems.ITEMS.get("truffle_oil"));
                entries.add(ModItems.ITEMS.get("sugar"));
                entries.add(ModItems.ITEMS.get("rice"));
                entries.add(ModItems.ITEMS.get("cheese"));
                entries.add(ModItems.ITEMS.get("cheese_silver"));
                entries.add(ModItems.ITEMS.get("cheese_gold"));
                entries.add(ModItems.ITEMS.get("cheese_iridium"));
                entries.add(ModItems.ITEMS.get("goat_cheese"));
                entries.add(ModItems.ITEMS.get("goat_cheese_silver"));
                entries.add(ModItems.ITEMS.get("goat_cheese_gold"));
                entries.add(ModItems.ITEMS.get("goat_cheese_iridium"));
                entries.add(ModItems.ITEMS.get("coffee"));
                entries.add(ModItems.ITEMS.get("mayonnaise"));
                entries.add(ModItems.ITEMS.get("mayonnaise_silver"));
                entries.add(ModItems.ITEMS.get("gold_mayonnaise"));
                entries.add(ModItems.ITEMS.get("mayonnaise_iridium"));
                entries.add(ModItems.ITEMS.get("duck_mayonnaise"));
                entries.add(ModItems.ITEMS.get("duck_mayonnaise_silver"));
                entries.add(ModItems.ITEMS.get("duck_mayonnaise_gold"));
                entries.add(ModItems.ITEMS.get("duck_mayonnaise_iridium"));
                entries.add(ModItems.ITEMS.get("void_mayonnaise"));
                entries.add(ModItems.ITEMS.get("void_mayonnaise_silver"));
                entries.add(ModItems.ITEMS.get("void_mayonnaise_gold"));
                entries.add(ModItems.ITEMS.get("void_mayonnaise_iridium"));
                entries.add(ModItems.ITEMS.get("dinosaur_mayonnaise"));
                entries.add(ModItems.ITEMS.get("dinosaur_mayonnaise_silver"));
                entries.add(ModItems.ITEMS.get("dinosaur_mayonnaise_gold"));
                entries.add(ModItems.ITEMS.get("dinosaur_mayonnaise_iridium"));
                entries.add(ModItems.ITEMS.get("squid_ink"));
                entries.add(ModItems.ITEMS.get("cloth"));
                entries.add(ModItems.ITEMS.get("honey"));
                entries.add(ModItems.ITEMS.get("tulip_honey"));
                entries.add(ModItems.ITEMS.get("blue_jazz_honey"));
                entries.add(ModItems.ITEMS.get("summer_spangle_honey"));
                entries.add(ModItems.ITEMS.get("poppy_honey"));
                entries.add(ModItems.ITEMS.get("sunflower_honey"));
                entries.add(ModItems.ITEMS.get("fairy_rose_honey"));
                entries.add(ModItems.ITEMS.get("dried_common_mushrooms"));
                entries.add(ModItems.ITEMS.get("dried_chanterelles"));
                entries.add(ModItems.ITEMS.get("dried_magma_caps"));
                entries.add(ModItems.ITEMS.get("dried_morels"));
                entries.add(ModItems.ITEMS.get("dried_purple_mushrooms"));
                addArtisanGroupItems(entries);
            })
            .build()
    );

    public static final ItemGroup ANIMAL_PRODUCTS_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "animal_products"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.animal_products"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("egg", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                // 蛋类
                String[][] eggGroups = {
                    {"egg", "brown_egg"},
                    {"large_egg", "large_brown_egg"},
                    {"duck_egg"},
                    {"void_egg"},
                    {"gold_egg"},
                    {"dinosaur_egg"}
                };
                String[] eggQualities = {"", "_silver", "_gold", "_iridium"};
                for (String[] group : eggGroups) {
                    for (String base : group) {
                        for (String suffix : eggQualities) {
                            Item item = ModItems.ITEMS.get(base + suffix);
                            if (item != null) entries.add(item);
                        }
                    }
                }
                // 奶类
                String[][] milkGroups = {
                    {"milk", "large_milk"},
                    {"goat_milk", "large_goat_milk"}
                };
                for (String[] group : milkGroups) {
                    for (String base : group) {
                        for (String suffix : eggQualities) {
                            Item item = ModItems.ITEMS.get(base + suffix);
                            if (item != null) entries.add(item);
                        }
                    }
                }
                // 其他动物制品
                String[][] otherProducts = {
                    {"wool"},
                    {"truffle"},
                    {"duck_feather"},
                    {"rabbits_foot"}
                };
                for (String[] group : otherProducts) {
                    for (String base : group) {
                        for (String suffix : eggQualities) {
                            Item item = ModItems.ITEMS.get(base + suffix);
                            if (item != null) entries.add(item);
                        }
                    }
                }
            })
            .build()
    );

    // 星露谷精炼物品标签栏
	public static final ItemGroup REFINED_GROUP = Registry.register(
		Registries.ITEM_GROUP,
		Identifier.of(StardewValley.MOD_ID, "refined"),
		FabricItemGroup.builder()
			.displayName(Text.translatable("itemGroup.stardewvalley.refined"))
			.icon(() -> new ItemStack(ModBlocks.SOLAR_PANEL))
			.entries((context, entries) -> {
				// 精炼物品
				String[] refinedNames = {
					"field_snack", "bug_steak", "life_elixir",
					"explosive_ammo", "fairy_dust", "monster_musk",
					"rain_totem", "staircase", "oil_of_garlic",
					"warp_totem_beach", "warp_totem_mountains", "warp_totem_farm",
					"warp_totem_desert", "warp_totem_island"
				};
				for (String name : refinedNames) {
					Item item = ModItems.ITEMS.get(name);
					if (item != null) entries.add(item);
				}
				// 炸弹物品
				entries.add(ModBlocks.CHERRY_BOMB_ITEM);
				entries.add(ModBlocks.BOMB_BLOCK_ITEM);
				entries.add(ModBlocks.MEGA_BOMB_ITEM);
				// 精炼机器方块
				entries.add(ModBlocks.SOLAR_PANEL);
				entries.add(ModBlocks.SEED_MAKER);
				entries.add(ModBlocks.GEODE_CRUSHER);
				entries.add(ModBlocks.HEAVY_FURNACE);
				entries.add(ModBlocks.RECYCLING_MACHINE);
				// 工匠设备
				entries.add(ModBlocks.BEE_HOUSE_ITEM);
				entries.add(ModBlocks.CASK_ITEM);
				entries.add(ModBlocks.CHEESE_PRESS_ITEM);
				entries.add(ModBlocks.DEHYDRATOR_ITEM);
				entries.add(ModBlocks.FISH_SMOKER_ITEM);
				entries.add(ModBlocks.KEG_ITEM);
				entries.add(ModBlocks.LOOM_ITEM);
				entries.add(ModBlocks.MAYONNAISE_MACHINE_ITEM);
				entries.add(ModBlocks.OIL_MAKER_ITEM);
				entries.add(ModBlocks.PRESERVES_JAR_ITEM);
				// 精炼方块
				entries.add(ModBlocks.TAPPER_ITEM);
				entries.add(ModBlocks.HEAVY_TAPPER_ITEM);
				entries.add(ModBlocks.LIGHTNING_ROD_ITEM);
				entries.add(ModBlocks.SPRINKLER_ITEM);
				entries.add(ModBlocks.QUALITY_SPRINKLER_ITEM);
				entries.add(ModBlocks.IRIDIUM_SPRINKLER_ITEM);
				entries.add(ModBlocks.FURNACE_ITEM);
				entries.add(ModBlocks.CHARCOAL_KILN_ITEM);
				entries.add(ModBlocks.BAIT_MAKER_ITEM);
				entries.add(ModBlocks.CRAB_POT_ITEM);
				entries.add(ModBlocks.CRYSTALARIUM_ITEM);
				entries.add(ModBlocks.DELUXE_WORM_BIN_ITEM);
				entries.add(ModBlocks.GARDEN_POT_ITEM);
				entries.add(ModBlocks.MUSHROOM_LOG_ITEM);
			})
			.build()
	);

    public static final ItemGroup WEAPONS_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "weapons"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.weapons"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("galaxy_sword", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                // 剑类武器
                String[] swords = {"rusty_sword","steel_smallsword","wooden_blade","pirates_sword","silver_saber",
                    "cutlass","forest_sword","iron_edge","bone_sword","claymore",
                    "neptunes_glaive","templars_blade","insect_head","obsidian_edge","ossified_blade",
                    "holy_blade","tempered_broadsword","yeti_tooth","steel_falchion","dark_sword",
                    "lava_katana","dragontooth_cutlass","dwarf_sword","galaxy_sword","infinity_blade",
                    "haleys_iron","leahs_whittler","meowmere"};
                // 匕首类武器
                String[] daggers = {"carving_knife","iron_dirk","wind_spire","elf_blade","burglars_shank",
                    "crystal_dagger","shadow_dagger","broken_trident","wicked_kris","galaxy_dagger",
                    "dwarf_dagger","dragontooth_shiv","iridium_needle","infinity_dagger",
                    "elliotts_pencil","abbys_planchette"};
                // 棍棒类武器
                String[] clubs = {"femur","wood_club","wood_mallet","lead_rod","kudgel",
                    "the_slammer","galaxy_hammer","dwarf_hammer","dragontooth_club","infinity_gavel",
                    "alexs_bat","harveys_mallet","marus_wrench","pennys_fryer","sams_old_guitar",
                    "sebs_lost_mace"};

                for (String name : swords) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                for (String name : daggers) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                for (String name : clubs) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // 鞋子
                String[] shoes = {"sneakers","rubber_boots","leather_boots","work_boots","combat_boots",
                    "tundra_boots","thermal_boots","dark_boots","firewalker_boots","genie_shoes",
                    "space_boots","cowboy_boots","emilys_magic_boots","leprechaun_shoes","cinderclown_shoes",
                    "mermaid_boots","dragonscale_boots","crystal_shoes"};
                for (String name : shoes) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // 戒指
                String[] ringNames = {
                    "amethyst_ring","aquamarine_ring","burglars_ring","crabshell_ring","emerald_ring",
                    "glow_ring","glowstone_ring","hot_java_ring","immunity_band","iridium_band",
                    "jade_ring","jukebox_ring","lucky_ring","magnet_ring","napalm_ring",
                    "phoenix_ring","protection_ring","ring_of_yoba","ruby_ring","savage_ring",
                    "slime_charmer_ring","small_glow_ring","small_magnet_ring","soul_sapper_ring",
                    "sturdy_ring","thorns_ring","topaz_ring","vampire_ring","warrior_ring"
                };
                for (String name : ringNames) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
                // 弹弓
                for (String name : new String[]{"slingshot", "master_slingshot"}) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
            })
            .build()
    );

    // 星露谷古物标签栏
    public static final ItemGroup GUWU_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(StardewValley.MOD_ID, "guwu"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.stardewvalley.guwu"))
            .icon(() -> new ItemStack(ModItems.ITEMS.getOrDefault("ancient_doll", net.minecraft.item.Items.AIR)))
            .entries((context, entries) -> {
                String[] guwuNames = {
                    "amphibian_fossil", "anchor", "ancient_doll", "ancient_drum",
                    "ancient_seed", "ancient_sword", "arrowhead", "bone_flute",
                    "chewing_stick", "chicken_statue", "chipped_amphora",
                    "dinosaur_egg", "dried_starfish", "dwarf_gadget",
                    "dwarf_scroll_i", "dwarf_scroll_ii", "dwarf_scroll_iii",
                    "dwarf_scroll_iv", "dwarvish_helm", "elvish_jewelry",
                    "glass_shards", "golden_mask", "golden_relic",
                    "nautilus_fossil", "ornamental_fan", "palm_fossil",
                    "prehistoric_handaxe", "prehistoric_rib", "prehistoric_scapula",
                    "prehistoric_skull", "prehistoric_tibia", "prehistoric_tool",
                    "prehistoric_vertebra", "rare_disc", "rusty_cog", "rusty_spoon",
                    "rusty_spur", "skeletal_hand", "skeletal_tail",
                    "strange_doll_green", "strange_doll_yellow", "trilobite"
                };
                for (String name : guwuNames) {
                    Item item = ModItems.ITEMS.get(name);
                    if (item != null) entries.add(item);
                }
            })
            .build()
    );

    private static void addArtisanGroupItems(ItemGroup.Entries entries) {
        Map<String, String[]> orderedItems = new LinkedHashMap<>();
        orderedItems.put("aged_roe", new String[]{"aged_albacore_roe", "aged_anchovy_roe", "aged_angler_roe", "aged_blobfish_roe", "aged_blue_discus_roe", "aged_bream_roe", "aged_bullhead_roe", "aged_carp_roe", "aged_catfish_roe", "aged_chub_roe", "aged_clam_roe", "aged_cockle_roe", "aged_crab_roe", "aged_crayfish_roe", "aged_crimsonfish_roe", "aged_dorado_roe", "aged_eel_roe", "aged_flounder_roe", "aged_ghostfish_roe", "aged_glacierfish_jr._roe", "aged_glacierfish_roe", "aged_goby_roe", "aged_halibut_roe", "aged_herring_roe", "aged_ice_pip_roe", "aged_largemouth_bass_roe", "aged_lava_eel_roe", "aged_legend_ii_roe", "aged_legend_roe", "aged_lingcod_roe", "aged_lionfish_roe", "aged_lobster_roe", "aged_midnight_carp_roe", "aged_midnight_squid_roe", "aged_ms._angler_roe", "aged_mussel_roe", "aged_mutant_carp_roe", "aged_octopus_roe", "aged_oyster_roe", "aged_perch_roe", "aged_periwinkle_roe", "aged_pike_roe", "aged_pufferfish_roe", "aged_radioactive_carp_roe", "aged_rainbow_trout_roe", "aged_red_mullet_roe", "aged_red_snapper_roe", "aged_salmon_roe", "aged_sandfish_roe", "aged_sardine_roe", "aged_scorpion_carp_roe", "aged_sea_cucumber_roe", "aged_sea_urchin_roe", "aged_shad_roe", "aged_shrimp_roe", "aged_slimejack_roe", "aged_smallmouth_bass_roe", "aged_snail_roe", "aged_son_of_crimsonfish_roe", "aged_spook_fish_roe", "aged_squid_roe", "aged_stingray_roe", "aged_stonefish_roe", "aged_sunfish_roe", "aged_super_cucumber_roe", "aged_tiger_trout_roe", "aged_tilapia_roe", "aged_tuna_roe", "aged_void_salmon_roe", "aged_walleye_roe", "aged_woodskip_roe"});
        orderedItems.put("caviar", new String[]{"caviar"});
        orderedItems.put("dried_fruit", new String[]{"dried_ancient_fruit", "dried_apple", "dried_apricot", "dried_banana", "dried_blackberry", "dried_blueberry", "dried_cactus_fruit", "dried_cherry", "dried_coconut", "dried_cranberries", "dried_crystal_fruit", "dried_hot_pepper", "dried_mango", "dried_melon", "dried_orange", "dried_peach", "dried_pineapple", "dried_pomegranate", "dried_powdermelon", "dried_qi_fruit", "dried_rhubarb", "dried_salmonberry", "dried_spice_berry", "dried_starfruit", "dried_strawberry", "dried_wild_plum", "rasins"});
        orderedItems.put("fruit_jelly", new String[]{"ancient_fruit_jelly", "apple_jelly", "apricot_jelly", "banana_jelly", "blackberry_jelly", "blueberry_jelly", "cactus_fruit_jelly", "cherry_jelly", "coconut_jelly", "cranberries_jelly", "crystal_fruit_jelly", "grape_jelly", "hot_pepper_jelly", "mango_jelly", "melon_jelly", "orange_jelly", "peach_jelly", "pineapple_jelly", "pomegranate_jelly", "powdermelon_jelly", "qi_fruit_jelly", "rhubarb_jelly", "salmonberry_jelly", "spice_berry_jelly", "starfruit_jelly", "strawberry_jelly", "wild_plum_jelly"});
        orderedItems.put("ancient_fruit_wine", new String[]{"ancient_fruit_wine", "ancient_fruit_wine_silver", "ancient_fruit_wine_gold", "ancient_fruit_wine_iridium"});
        orderedItems.put("apple_wine", new String[]{"apple_wine", "apple_wine_silver", "apple_wine_gold", "apple_wine_iridium"});
        orderedItems.put("apricot_wine", new String[]{"apricot_wine", "apricot_wine_silver", "apricot_wine_gold", "apricot_wine_iridium"});
        orderedItems.put("banana_wine", new String[]{"banana_wine", "banana_wine_silver", "banana_wine_gold", "banana_wine_iridium"});
        orderedItems.put("blackberry_wine", new String[]{"blackberry_wine", "blackberry_wine_silver", "blackberry_wine_gold", "blackberry_wine_iridium"});
        orderedItems.put("blueberry_wine", new String[]{"blueberry_wine", "blueberry_wine_silver", "blueberry_wine_gold", "blueberry_wine_iridium"});
        orderedItems.put("cactus_fruit_wine", new String[]{"cactus_fruit_wine", "cactus_fruit_wine_silver", "cactus_fruit_wine_gold", "cactus_fruit_wine_iridium"});
        orderedItems.put("cherry_wine", new String[]{"cherry_wine", "cherry_wine_silver", "cherry_wine_gold", "cherry_wine_iridium"});
        orderedItems.put("coconut_wine", new String[]{"coconut_wine", "coconut_wine_silver", "coconut_wine_gold", "coconut_wine_iridium"});
        orderedItems.put("cranberries_wine", new String[]{"cranberries_wine", "cranberries_wine_silver", "cranberries_wine_gold", "cranberries_wine_iridium"});
        orderedItems.put("crystal_fruit_wine", new String[]{"crystal_fruit_wine", "crystal_fruit_wine_silver", "crystal_fruit_wine_gold", "crystal_fruit_wine_iridium"});
        orderedItems.put("grape_wine", new String[]{"grape_wine", "grape_wine_silver", "grape_wine_gold", "grape_wine_iridium"});
        orderedItems.put("hot_pepper_wine", new String[]{"hot_pepper_wine", "hot_pepper_wine_silver", "hot_pepper_wine_gold", "hot_pepper_wine_iridium"});
        orderedItems.put("mango_wine", new String[]{"mango_wine", "mango_wine_silver", "mango_wine_gold", "mango_wine_iridium"});
        orderedItems.put("mead", new String[]{"mead", "mead_silver", "mead_gold", "mead_iridium"});
        orderedItems.put("melon_wine", new String[]{"melon_wine", "melon_wine_silver", "melon_wine_gold", "melon_wine_iridium"});
        orderedItems.put("orange_wine", new String[]{"orange_wine", "orange_wine_silver", "orange_wine_gold", "orange_wine_iridium"});
        orderedItems.put("peach_wine", new String[]{"peach_wine", "peach_wine_silver", "peach_wine_gold", "peach_wine_iridium"});
        orderedItems.put("pineapple_wine", new String[]{"pineapple_wine", "pineapple_wine_silver", "pineapple_wine_gold", "pineapple_wine_iridium"});
        orderedItems.put("pomegranate_wine", new String[]{"pomegranate_wine", "pomegranate_wine_silver", "pomegranate_wine_gold", "pomegranate_wine_iridium"});
        orderedItems.put("powdermelon_wine", new String[]{"powdermelon_wine", "powdermelon_wine_silver", "powdermelon_wine_gold", "powdermelon_wine_iridium"});
        orderedItems.put("qi_fruit_wine", new String[]{"qi_fruit_wine", "qi_fruit_wine_silver", "qi_fruit_wine_gold", "qi_fruit_wine_iridium"});
        orderedItems.put("rhubarb_wine", new String[]{"rhubarb_wine", "rhubarb_wine_silver", "rhubarb_wine_gold", "rhubarb_wine_iridium"});
        orderedItems.put("salmonberry_wine", new String[]{"salmonberry_wine", "salmonberry_wine_silver", "salmonberry_wine_gold", "salmonberry_wine_iridium"});
        orderedItems.put("spice_berry_wine", new String[]{"spice_berry_wine", "spice_berry_wine_silver", "spice_berry_wine_gold", "spice_berry_wine_iridium"});
        orderedItems.put("starfruit_wine", new String[]{"starfruit_wine", "starfruit_wine_silver", "starfruit_wine_gold", "starfruit_wine_iridium"});
        orderedItems.put("strawberry_wine", new String[]{"strawberry_wine", "strawberry_wine_silver", "strawberry_wine_gold", "strawberry_wine_iridium"});
        orderedItems.put("wild_plum_wine", new String[]{"wild_plum_wine", "wild_plum_wine_silver", "wild_plum_wine_gold", "wild_plum_wine_iridium"});
                orderedItems.put("beer", new String[]{"beer", "beer_silver", "beer_gold", "beer_iridium"});
        orderedItems.put("pale_ale", new String[]{"pale_ale", "pale_ale_silver", "pale_ale_gold", "pale_ale_iridium"});
        orderedItems.put("vegetable_juice", new String[]{"amaranth_juice", "artichoke_juice", "beet_juice", "bok_choy_juice", "broccoli_juice", "carrot_juice", "cauliflower_juice", "corn_juice", "eggplant_juice", "fiddlehead_fern_juice", "garlic_juice", "green_bean_juice", "kale_juice", "parsnip_juice", "potato_juice", "pumpkin_juice", "radish_juice", "red_cabbage_juice", "summer_squash_juice", "taro_root_juice", "tomato_juice", "unmilled_rice_juice", "yam_juice"});
        orderedItems.put("green_tea", new String[]{"green_tea"});
        orderedItems.put("vegetable_pickles", new String[]{"amaranth_pickles", "artichoke_pickles", "beet_pickles", "bok_choy_pickles", "broccoli_pickles", "carrot_pickles", "cauliflower_pickles", "corn_pickles", "eggplant_pickles", "fiddlehead_fern_pickles", "garlic_pickles", "green_bean_pickles", "hops_pickles", "kale_pickles", "parsnip_pickles", "potato_pickles", "pumpkin_pickles", "radish_pickles", "red_cabbage_pickles", "summer_squash_pickles", "taro_root_pickles", "tea_leaves_pickles", "tomato_pickles", "unmilled_rice_pickles", "wheat_pickles", "yam_pickles"});
        orderedItems.put("roe", new String[]{"albacore_roe", "anchovy_roe", "angler_roe", "blobfish_roe", "blue_discus_roe", "bream_roe", "bullhead_roe", "carp_roe", "catfish_roe", "chub_roe", "clam_roe", "cockle_roe", "crab_roe", "crayfish_roe", "crimsonfish_roe", "dorado_roe", "eel_roe", "flounder_roe", "ghostfish_roe", "glacierfish_jr._roe", "glacierfish_roe", "goby_roe", "halibut_roe", "herring_roe", "ice_pip_roe", "largemouth_bass_roe", "lava_eel_roe", "legend_ii_roe", "legend_roe", "lingcod_roe", "lionfish_roe", "lobster_roe", "midnight_carp_roe", "midnight_squid_roe", "ms._angler_roe", "mussel_roe", "mutant_carp_roe", "octopus_roe", "oyster_roe", "perch_roe", "periwinkle_roe", "pike_roe", "pufferfish_roe", "radioactive_carp_roe", "rainbow_trout_roe", "red_mullet_roe", "red_snapper_roe", "salmon_roe", "sandfish_roe", "sardine_roe", "scorpion_carp_roe", "sea_cucumber_roe", "sea_urchin_roe", "shad_roe", "shrimp_roe", "slimejack_roe", "smallmouth_bass_roe", "snail_roe", "son_of_crimsonfish_roe", "spook_fish_roe", "squid_roe", "stingray_roe", "stonefish_roe", "sturgeon_roe", "sunfish_roe", "super_cucumber_roe", "tiger_trout_roe", "tilapia_roe", "tuna_roe", "void_salmon_roe", "walleye_roe", "woodskip_roe"});
        orderedItems.put("smoked_fish", new String[]{"smoked_albacore", "smoked_albacore_sliver", "smoked_albacore_gold", "smoked_albacore_iridium", "smoked_anchovy", "smoked_anchovy_sliver", "smoked_anchovy_gold", "smoked_anchovy_iridium", "smoked_angler", "smoked_angler_sliver", "smoked_angler_gold", "smoked_angler_iridium", "smoked_blobfish", "smoked_blobfish_sliver", "smoked_blobfish_gold", "smoked_blobfish_iridium", "smoked_blue_discus", "smoked_blue_discus_sliver", "smoked_blue_discus_gold", "smoked_blue_discus_iridium", "smoked_bream", "smoked_bream_sliver", "smoked_bream_gold", "smoked_bream_iridium", "smoked_bullhead", "smoked_bullhead_sliver", "smoked_bullhead_gold", "smoked_bullhead_iridium", "smoked_carp", "smoked_carp_sliver", "smoked_carp_gold", "smoked_carp_iridium", "smoked_catfish", "smoked_catfish_sliver", "smoked_catfish_gold", "smoked_catfish_iridium", "smoked_chub", "smoked_chub_sliver", "smoked_chub_gold", "smoked_chub_iridium", "smoked_crimsonfish", "smoked_crimsonfish_sliver", "smoked_crimsonfish_gold", "smoked_crimsonfish_iridium", "smoked_dorado", "smoked_dorado_sliver", "smoked_dorado_gold", "smoked_dorado_iridium", "smoked_eel", "smoked_eel_sliver", "smoked_eel_gold", "smoked_eel_iridium", "smoked_flounder", "smoked_flounder_sliver", "smoked_flounder_gold", "smoked_flounder_iridium", "smoked_ghostfish", "smoked_ghostfish_sliver", "smoked_ghostfish_gold", "smoked_ghostfish_iridium", "smoked_glacierfish", "smoked_glacierfish_sliver", "smoked_glacierfish_gold", "smoked_glacierfish_iridium", "smoked_glacierfish_jr.", "smoked_glacierfish_jr._sliver", "smoked_glacierfish_jr._gold", "smoked_glacierfish_jr._iridium", "smoked_goby", "smoked_goby_sliver", "smoked_goby_gold", "smoked_goby_iridium", "smoked_halibut", "smoked_halibut_sliver", "smoked_halibut_gold", "smoked_halibut_iridium", "smoked_herring", "smoked_herring_sliver", "smoked_herring_gold", "smoked_herring_iridium", "smoked_ice_pip", "smoked_ice_pip_sliver", "smoked_ice_pip_gold", "smoked_ice_pip_iridium", "smoked_largemouth_bass", "smoked_largemouth_bass_sliver", "smoked_largemouth_bass_gold", "smoked_largemouth_bass_iridium", "smoked_lava_eel", "smoked_lava_eel_sliver", "smoked_lava_eel_gold", "smoked_lava_eel_iridium", "smoked_legend", "smoked_legend_sliver", "smoked_legend_gold", "smoked_legend_iridium", "smoked_legend_ii", "smoked_legend_ii_sliver", "smoked_legend_ii_gold", "smoked_legend_ii_iridium", "smoked_lingcod", "smoked_lingcod_sliver", "smoked_lingcod_gold", "smoked_lingcod_iridium", "smoked_lionfish", "smoked_lionfish_sliver", "smoked_lionfish_gold", "smoked_lionfish_iridium", "smoked_lobster", "smoked_lobster_sliver", "smoked_midnight_carp", "smoked_midnight_carp_sliver", "smoked_midnight_carp_gold", "smoked_midnight_carp_iridium", "smoked_midnight_squid", "smoked_midnight_squid_sliver", "smoked_midnight_squid_gold", "smoked_midnight_squid_iridium", "smoked_ms._angler", "smoked_ms._angler_sliver", "smoked_ms._angler_gold", "smoked_ms._angler_iridium", "smoked_mutant_carp", "smoked_mutant_carp_sliver", "smoked_mutant_carp_gold", "smoked_mutant_carp_iridium", "smoked_octopus", "smoked_octopus_sliver", "smoked_octopus_gold", "smoked_octopus_iridium", "smoked_perch", "smoked_perch_sliver", "smoked_perch_gold", "smoked_perch_iridium", "smoked_periwinkle", "smoked_periwinkle_sliver", "smoked_pike", "smoked_pike_sliver", "smoked_pike_gold", "smoked_pike_iridium", "smoked_pufferfish", "smoked_pufferfish_sliver", "smoked_pufferfish_gold", "smoked_pufferfish_iridium", "smoked_radioactive_carp", "smoked_radioactive_carp_sliver", "smoked_radioactive_carp_gold", "smoked_radioactive_carp_iridium", "smoked_rainbow_trout", "smoked_rainbow_trout_sliver", "smoked_rainbow_trout_gold", "smoked_rainbow_trout_iridium", "smoked_red_mullet", "smoked_red_mullet_sliver", "smoked_red_mullet_gold", "smoked_red_mullet_iridium", "smoked_red_snapper", "smoked_red_snapper_sliver", "smoked_red_snapper_gold", "smoked_red_snapper_iridium", "smoked_salmon", "smoked_salmon_sliver", "smoked_salmon_gold", "smoked_salmon_iridium", "smoked_sandfish", "smoked_sandfish_sliver", "smoked_sandfish_gold", "smoked_sandfish_iridium", "smoked_sardine", "smoked_sardine_sliver", "smoked_sardine_gold", "smoked_sardine_iridium", "smoked_scorpion_carp", "smoked_scorpion_carp_sliver", "smoked_scorpion_carp_gold", "smoked_scorpion_carp_iridium", "smoked_sea_cucumber", "smoked_sea_cucumber_sliver", "smoked_sea_cucumber_gold", "smoked_sea_cucumber_iridium", "smoked_shad", "smoked_shad_sliver", "smoked_shad_gold", "smoked_shad_iridium", "smoked_shrimp", "smoked_shrimp_sliver", "smoked_slimejack", "smoked_slimejack_sliver", "smoked_slimejack_gold", "smoked_slimejack_iridium", "smoked_smallmouth_bass", "smoked_smallmouth_bass_sliver", "smoked_smallmouth_bass_gold", "smoked_smallmouth_bass_iridium", "smoked_snail", "smoked_snail_sliver", "smoked_son_of_crimsonfish", "smoked_son_of_crimsonfish_sliver", "smoked_son_of_crimsonfish_gold", "smoked_son_of_crimsonfish_iridium", "smoked_spook_fish", "smoked_spook_fish_sliver", "smoked_spook_fish_gold", "smoked_spook_fish_iridium", "smoked_squid", "smoked_squid_sliver", "smoked_squid_gold", "smoked_squid_iridium", "smoked_stingray", "smoked_stingray_sliver", "smoked_stingray_gold", "smoked_stingray_iridium", "smoked_stonefish", "smoked_stonefish_sliver", "smoked_stonefish_gold", "smoked_stonefish_iridium", "smoked_sturgeon", "smoked_sturgeon_sliver", "smoked_sturgeon_gold", "smoked_sturgeon_iridium", "smoked_sunfish", "smoked_sunfish_sliver", "smoked_sunfish_gold", "smoked_sunfish_iridium", "smoked_super_cucumber", "smoked_super_cucumber_sliver", "smoked_super_cucumber_gold", "smoked_super_cucumber_iridium", "smoked_tiger_trout", "smoked_tiger_trout_sliver", "smoked_tiger_trout_gold", "smoked_tiger_trout_iridium", "smoked_tilapia", "smoked_tilapia_sliver", "smoked_tilapia_gold", "smoked_tilapia_iridium", "smoked_tuna", "smoked_tuna_sliver", "smoked_tuna_gold", "smoked_tuna_iridium", "smoked_void_salmon", "smoked_void_salmon_sliver", "smoked_void_salmon_gold", "smoked_void_salmon_iridium", "smoked_walleye", "smoked_walleye_sliver", "smoked_walleye_gold", "smoked_walleye_iridium", "smoked_woodskip", "smoked_woodskip_sliver", "smoked_woodskip_gold", "smoked_woodskip_iridium", "smoked_clam", "smoked_clam_sliver", "smoked_clam_gold", "smoked_clam_iridium", "smoked_cockle", "smoked_cockle_sliver", "smoked_cockle_gold", "smoked_cockle_iridium", "smoked_mussel", "smoked_mussel_sliver", "smoked_mussel_gold", "smoked_mussel_iridium", "smoked_oyster", "smoked_oyster_sliver", "smoked_oyster_gold", "smoked_oyster_iridium", "smoked_crab", "smoked_crab_sliver", "smoked_crayfish", "smoked_crayfish_sliver"});

        for (Map.Entry<String, String[]> entry : orderedItems.entrySet()) {
            for (String itemName : entry.getValue()) {
                Item item = ModItems.ITEMS.get(itemName);
                if (item != null) entries.add(item);
            }
        }
    }

    public static void registerAll() {
        // 初始化所有标签栏，触发Registry.register
        CROPS_GROUP.toString();
        SPECIAL_GROUP.toString();
        COLLECTIBLES_GROUP.toString();
        TOOLS_GROUP.toString();
        ORES_GROUP.toString();
        MISC_GROUP.toString();
        FISH_GROUP.toString();
        WEAPONS_GROUP.toString();
        DISHES_GROUP.toString();
        ARTISAN_GROUP.toString();
        ANIMAL_PRODUCTS_GROUP.toString();
        REFINED_GROUP.toString();
        GUWU_GROUP.toString();
    }
}
