package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.item.ArtisanItem;
import stardewvalley.modid.item.CropItem;
import stardewvalley.modid.skill.SkillEffectHelper;
import stardewvalley.modid.util.SafeCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GoldManager extends PersistentState {

    private static final String NAME = "stardewvalley_economy";

    private int gold = 2000;
    private final List<ShippingEntry> shippingItems = new ArrayList<>();

    private static final UUID FALLBACK_UUID = new UUID(0L, 0L);

    public static class ShippingEntry {
        public Identifier itemId;
        public int count;
        public UUID playerUuid;  // 谁添加了这个物品（用于技能价格加成）

        public ShippingEntry(Identifier itemId, int count, UUID playerUuid) {
            this.itemId = itemId;
            this.count = count;
            this.playerUuid = playerUuid;
        }
    }

    private record SimpleData(int gold, List<ShippingEntry> items) {}

    public static final Codec<ShippingEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Identifier.CODEC.fieldOf("item").forGetter(e -> e.itemId),
            Codec.INT.fieldOf("count").forGetter(e -> e.count),
            Uuids.CODEC.optionalFieldOf("player", GoldManager.FALLBACK_UUID).forGetter(e -> e.playerUuid)
        ).apply(instance, (id, count, uuid) -> new ShippingEntry(id, count, uuid.equals(FALLBACK_UUID) ? null : uuid))
    );

    public static final Codec<List<ShippingEntry>> SHIPPING_CODEC = Codec.list(ENTRY_CODEC);

    public static final Codec<SimpleData> SIMPLE_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("gold").forGetter(d -> d.gold),
            SHIPPING_CODEC.fieldOf("shipping_items").forGetter(d -> d.items)
        ).apply(instance, SimpleData::new)
    );

    public static final Codec<GoldManager> CODEC = SIMPLE_DATA_CODEC.xmap(
        data -> {
            GoldManager manager = new GoldManager();
            manager.gold = data.gold;
            manager.shippingItems.addAll(data.items);
            return manager;
        },
        manager -> new SimpleData(manager.gold, List.copyOf(manager.shippingItems))
    );

    public static final PersistentStateType<GoldManager> TYPE = new PersistentStateType<>(
        NAME,
        GoldManager::new,
        SafeCodec.wrap(CODEC, GoldManager::new),
        DataFixTypes.LEVEL
    );

    public static GoldManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        gold += amount;
        setDirty(true);
    }

    public List<ShippingEntry> getShippingItems() {
        return shippingItems;
    }

    public void addToShipping(Identifier itemId, int count, UUID playerUuid) {
        for (ShippingEntry entry : shippingItems) {
            if (entry.itemId.equals(itemId) && java.util.Objects.equals(entry.playerUuid, playerUuid)) {
                entry.count += count;
                setDirty(true);
                return;
            }
        }
        shippingItems.add(new ShippingEntry(itemId, count, playerUuid));
        setDirty(true);
    }

    public int settleShipping(ServerWorld world) {
        int total = 0;
        for (ShippingEntry entry : shippingItems) {
            int value = getItemMoneyValue(entry.itemId);
            // 按每个玩家自己的技能倍率计算价格
            float mult = entry.playerUuid != null
                ? SkillEffectHelper.getSellPriceMultiplier(world, entry.playerUuid, entry.itemId)
                : 1.0f;
            // 古代珍宝鉴定指南：古物售价4倍
            if (entry.playerUuid != null && isArtifactItem(entry.itemId)) {
                mult *= getArtifactPriceMultiplier(entry.playerUuid, world);
            }
            total += Math.round(value * mult) * entry.count;
        }
        shippingItems.clear();
        if (total > 0) {
            gold += total;
            setDirty(true);
            StardewValley.LOGGER.info("Shipping settled: +{}g (total: {}g)", total, gold);
        }
        return total;
    }

    private static final java.util.Map<String, Integer> SEED_SELL_PRICES = java.util.Map.ofEntries(
        java.util.Map.entry("coffeebean", 15),
        java.util.Map.entry("coffeebean_silver", Math.round(15 * 1.25f)),
        java.util.Map.entry("coffeebean_gold", Math.round(15 * 1.5f)),
        java.util.Map.entry("coffeebean_iridium", Math.round(15 * 2.0f)),
        // 春季作物种子
        java.util.Map.entry("bluejazz_seeds", 15),
        java.util.Map.entry("carrot_seeds", 15),
        java.util.Map.entry("cauliflower_seeds", 40),
        java.util.Map.entry("garlic_seeds", 20),
        java.util.Map.entry("beanstarter", 30),
        java.util.Map.entry("kale_seeds", 35),
        java.util.Map.entry("parsnip_seeds", 10),
        java.util.Map.entry("potato_seeds", 25),
        java.util.Map.entry("rhubarb_seeds", 50),
        java.util.Map.entry("strawberry_seeds", 50),
        java.util.Map.entry("tulip_seeds", 10),
        java.util.Map.entry("unmilledrice_seeds", 20),
        // 夏季作物种子
        java.util.Map.entry("blueberry_seeds", 40),
        java.util.Map.entry("corn_seeds", 75),
        java.util.Map.entry("hopsstarter", 30),
        java.util.Map.entry("hotpepper_seeds", 20),
        java.util.Map.entry("melon_seeds", 40),
        java.util.Map.entry("poppy_seeds", 50),
        java.util.Map.entry("radish_seeds", 20),
        java.util.Map.entry("redcabbage_seeds", 50),
        java.util.Map.entry("starfruit_seeds", 200),
        java.util.Map.entry("summerspangle_seeds", 25),
        java.util.Map.entry("summersquash_seeds", 20),
        java.util.Map.entry("sunflower_seeds", 100),
        java.util.Map.entry("tomato_seeds", 25),
        java.util.Map.entry("wheat_seeds", 5),
        // 秋季作物种子
        java.util.Map.entry("amaranth_seeds", 35),
        java.util.Map.entry("artichoke_seeds", 15),
        java.util.Map.entry("beet_seeds", 10),
        java.util.Map.entry("bokchoy_seeds", 25),
        java.util.Map.entry("broccoli_seeds", 40),
        java.util.Map.entry("cranberries_seeds", 120),
        java.util.Map.entry("eggplant_seeds", 10),
        java.util.Map.entry("fairyrose_seeds", 100),
        java.util.Map.entry("grapestarter", 30),
        java.util.Map.entry("pumpkin_seeds", 50),
        java.util.Map.entry("yam_seeds", 30),
        // 冬季/特殊作物种子
        java.util.Map.entry("powdermelon_seeds", 20),
        java.util.Map.entry("sweetgemberry_seeds", 200),
        java.util.Map.entry("ancientfruit_seeds", 30),
        java.util.Map.entry("cactusfruit_seeds", 75),
        java.util.Map.entry("pineapple_seeds", 240),
        java.util.Map.entry("taroroot_seeds", 20),
        // 混合/野生种子
        java.util.Map.entry("mixedseeds", 1),
        java.util.Map.entry("mixedflowerseeds", 1),
        java.util.Map.entry("spring_seeds", 35),
        java.util.Map.entry("summer_seeds", 55),
        java.util.Map.entry("fall_seeds", 45),
        java.util.Map.entry("winter_seeds", 30),
        java.util.Map.entry("fiber_seeds", 5),
        java.util.Map.entry("tealeaves_seeds", 250),
        java.util.Map.entry("qigua_seeds", 1)
    );

    private static final java.util.Map<String, Integer> ORE_SELL_PRICES = java.util.Map.ofEntries(
        java.util.Map.entry("coal", 15),
        java.util.Map.entry("copper_ore", 5),
        java.util.Map.entry("iron_ore", 10),
        java.util.Map.entry("gold_ore", 25),
        java.util.Map.entry("iridium_ore", 100),
        java.util.Map.entry("copper_bar", 60),
        java.util.Map.entry("iron_bar", 120),
        java.util.Map.entry("gold_bar", 150),
        java.util.Map.entry("iridium_bar", 1000),
        java.util.Map.entry("amethyst", 130),
        java.util.Map.entry("aquamarine", 180),
        java.util.Map.entry("diamond", 750),
        java.util.Map.entry("emerald", 250),
        java.util.Map.entry("jade", 200),
        java.util.Map.entry("ruby", 250),
        java.util.Map.entry("topaz", 80),
        java.util.Map.entry("prismatic_shard", 3000),
        java.util.Map.entry("geode", 50),
        java.util.Map.entry("frozen_geode", 100),
        java.util.Map.entry("magma_geode", 150),
        java.util.Map.entry("omni_geode", 150),
        java.util.Map.entry("earth_crystal", 50),
        java.util.Map.entry("fire_quartz", 100),
        java.util.Map.entry("quartz", 25),
        java.util.Map.entry("tear_crystal", 75),
        // 晶球矿物
        java.util.Map.entry("tigerseye", 275),
        java.util.Map.entry("opal", 150),
        java.util.Map.entry("fire_opal", 350),
        java.util.Map.entry("alamite", 150),
        java.util.Map.entry("bixite", 300),
        java.util.Map.entry("baryte", 50),
        java.util.Map.entry("aerinite", 125),
        java.util.Map.entry("calcite", 75),
        java.util.Map.entry("dolomite", 300),
        java.util.Map.entry("esperite", 100),
        java.util.Map.entry("fluorapatite", 200),
        java.util.Map.entry("geminite", 150),
        java.util.Map.entry("helvite", 450),
        java.util.Map.entry("jamborite", 150),
        java.util.Map.entry("jagoite", 115),
        java.util.Map.entry("kyanite", 250),
        java.util.Map.entry("lunarite", 200),
        java.util.Map.entry("malachite", 100),
        java.util.Map.entry("neptunite", 400),
        java.util.Map.entry("lemon_stone", 200),
        java.util.Map.entry("nekoite", 80),
        java.util.Map.entry("orpiment", 80),
        java.util.Map.entry("petrified_slime", 120),
        java.util.Map.entry("thunder_egg", 100),
        java.util.Map.entry("pyrite", 120),
        java.util.Map.entry("ocean_stone", 220),
        java.util.Map.entry("ghost_crystal", 200),
        java.util.Map.entry("jasper", 150),
        java.util.Map.entry("celestine", 125),
        java.util.Map.entry("marble", 110),
        java.util.Map.entry("sandstone", 60),
        java.util.Map.entry("granite", 75),
        java.util.Map.entry("basalt", 175),
        java.util.Map.entry("limestone", 15),
        java.util.Map.entry("soapstone", 120),
        java.util.Map.entry("hematite", 150),
        java.util.Map.entry("mudstone", 25),
        java.util.Map.entry("obsidian", 200),
        java.util.Map.entry("slate", 85),
        java.util.Map.entry("fairy_stone", 250),
        java.util.Map.entry("star_shards", 500)
    );

    private static final java.util.Map<String, Integer> MISC_SELL_PRICES = java.util.Map.ofEntries(
        java.util.Map.entry("basic_fertilizer", 2),
        java.util.Map.entry("quality_fertilizer", 10),
        java.util.Map.entry("deluxe_fertilizer", 70),
        java.util.Map.entry("basic_retaining_soil", 4),
        java.util.Map.entry("quality_retaining_soil", 5),
        java.util.Map.entry("deluxe_retaining_soil", 30),
        java.util.Map.entry("speed-gro", 20),
        java.util.Map.entry("hyper_speed-gro", 40),
        java.util.Map.entry("deluxe_speed-gro", 70),
        java.util.Map.entry("misc_stone", 2),
        java.util.Map.entry("bone_fragment", 12),
        java.util.Map.entry("pine_tar", 100),
        java.util.Map.entry("oak_resin", 150),
        java.util.Map.entry("maple_syrup", 200),
        java.util.Map.entry("clay", 20),
        java.util.Map.entry("moss", 5),
        java.util.Map.entry("slime", 5),
        java.util.Map.entry("wood", 2),
        java.util.Map.entry("hardwood", 15),
        java.util.Map.entry("caiji_sap", 2),
        java.util.Map.entry("bug_meat", 8),
        java.util.Map.entry("solar_essence", 40),
        // 工匠制品
        java.util.Map.entry("vinegar", 50),
        java.util.Map.entry("wheat_flour", 50),
        java.util.Map.entry("oil", 100),
        java.util.Map.entry("sugar", 100),
        java.util.Map.entry("rice", 100),
        // 动物制品价格
        java.util.Map.entry("cheese", 230),
        java.util.Map.entry("egg", 50),
        java.util.Map.entry("brown_egg", 50),
        java.util.Map.entry("large_egg", 95),
        java.util.Map.entry("large_brown_egg", 95),
        java.util.Map.entry("duck_egg", 95),
        java.util.Map.entry("void_egg", 65),
        java.util.Map.entry("gold_egg", 500),
        java.util.Map.entry("milk", 125),
        java.util.Map.entry("goat_milk", 225),
        java.util.Map.entry("large_milk", 190),
        java.util.Map.entry("large_goat_milk", 345),
        java.util.Map.entry("wool", 340),
        // 动物制品品质变体价格
        java.util.Map.entry("egg_silver", Math.round(50 * 1.25f)),
        java.util.Map.entry("egg_gold", Math.round(50 * 1.5f)),
        java.util.Map.entry("egg_iridium", Math.round(50 * 2.0f)),
        java.util.Map.entry("brown_egg_silver", Math.round(50 * 1.25f)),
        java.util.Map.entry("brown_egg_gold", Math.round(50 * 1.5f)),
        java.util.Map.entry("brown_egg_iridium", Math.round(50 * 2.0f)),
        java.util.Map.entry("large_egg_silver", Math.round(95 * 1.25f)),
        java.util.Map.entry("large_egg_gold", Math.round(95 * 1.5f)),
        java.util.Map.entry("large_egg_iridium", Math.round(95 * 2.0f)),
        java.util.Map.entry("large_brown_egg_silver", Math.round(95 * 1.25f)),
        java.util.Map.entry("large_brown_egg_gold", Math.round(95 * 1.5f)),
        java.util.Map.entry("large_brown_egg_iridium", Math.round(95 * 2.0f)),
        java.util.Map.entry("duck_egg_silver", Math.round(95 * 1.25f)),
        java.util.Map.entry("duck_egg_gold", Math.round(95 * 1.5f)),
        java.util.Map.entry("duck_egg_iridium", Math.round(95 * 2.0f)),
        java.util.Map.entry("void_egg_silver", Math.round(65 * 1.25f)),
        java.util.Map.entry("void_egg_gold", Math.round(65 * 1.5f)),
        java.util.Map.entry("void_egg_iridium", Math.round(65 * 2.0f)),
        java.util.Map.entry("dinosaur_egg_silver", Math.round(625 * 1.25f)),
        java.util.Map.entry("dinosaur_egg_gold", Math.round(625 * 1.5f)),
        java.util.Map.entry("dinosaur_egg_iridium", Math.round(625 * 2.0f)),
        java.util.Map.entry("gold_egg_silver", Math.round(500 * 1.25f)),
        java.util.Map.entry("gold_egg_gold", Math.round(500 * 1.5f)),
        java.util.Map.entry("gold_egg_iridium", Math.round(500 * 2.0f)),
        java.util.Map.entry("milk_silver", Math.round(125 * 1.25f)),
        java.util.Map.entry("milk_gold", Math.round(125 * 1.5f)),
        java.util.Map.entry("milk_iridium", Math.round(125 * 2.0f)),
        java.util.Map.entry("large_milk_silver", Math.round(190 * 1.25f)),
        java.util.Map.entry("large_milk_gold", Math.round(190 * 1.5f)),
        java.util.Map.entry("large_milk_iridium", Math.round(190 * 2.0f)),
        java.util.Map.entry("goat_milk_silver", Math.round(225 * 1.25f)),
        java.util.Map.entry("goat_milk_gold", Math.round(225 * 1.5f)),
        java.util.Map.entry("goat_milk_iridium", Math.round(225 * 2.0f)),
        java.util.Map.entry("large_goat_milk_silver", Math.round(345 * 1.25f)),
        java.util.Map.entry("large_goat_milk_gold", Math.round(345 * 1.5f)),
        java.util.Map.entry("large_goat_milk_iridium", Math.round(345 * 2.0f)),
        java.util.Map.entry("duck_feather_silver", Math.round(250 * 1.25f)),
        java.util.Map.entry("duck_feather_gold", Math.round(250 * 1.5f)),
        java.util.Map.entry("duck_feather_iridium", Math.round(250 * 2.0f)),
        java.util.Map.entry("rabbits_foot_silver", Math.round(565 * 1.25f)),
        java.util.Map.entry("rabbits_foot_gold", Math.round(565 * 1.5f)),
        java.util.Map.entry("rabbits_foot_iridium", Math.round(565 * 2.0f)),
        java.util.Map.entry("wool_silver", Math.round(340 * 1.25f)),
        java.util.Map.entry("wool_gold", Math.round(340 * 1.5f)),
        java.util.Map.entry("wool_iridium", Math.round(340 * 2.0f)),
        java.util.Map.entry("truffle_silver", Math.round(625 * 1.25f)),
        java.util.Map.entry("truffle_gold", Math.round(625 * 1.5f)),
        java.util.Map.entry("truffle_iridium", Math.round(625 * 2.0f)),
        java.util.Map.entry("coffee", 150),
        // 蜂蜜价格
        java.util.Map.entry("honey", 100),
        java.util.Map.entry("tulip_honey", 160),
        java.util.Map.entry("blue_jazz_honey", 200),
        java.util.Map.entry("summer_spangle_honey", 280),
        java.util.Map.entry("poppy_honey", 380),
        java.util.Map.entry("sunflower_honey", 260),
        java.util.Map.entry("fairy_rose_honey", 680),
        // 菜品售出价格
        java.util.Map.entry("fried_egg", 35),
        java.util.Map.entry("baked_fish", 100),
        java.util.Map.entry("omelet", 125),
        java.util.Map.entry("salad", 110),
        java.util.Map.entry("cheese_cauliflower", 300),
        java.util.Map.entry("parsnip_soup", 120),
        java.util.Map.entry("vegetable_medley", 120),
        java.util.Map.entry("complete_breakfast", 350),
        java.util.Map.entry("fried_calamari", 150),
        java.util.Map.entry("lucky_lunch", 250),
        java.util.Map.entry("fried_mushroom", 200),
        java.util.Map.entry("pizza", 300),
        java.util.Map.entry("bean_hotpot", 100),
        java.util.Map.entry("glazed_yams", 200),
        java.util.Map.entry("carp_surprise", 150),
        java.util.Map.entry("hashbrowns", 120),
        java.util.Map.entry("pancakes", 80),
        java.util.Map.entry("salmon_dinner", 300),
        java.util.Map.entry("crispy_bass", 150),
        java.util.Map.entry("pepper_poppers", 200),
        java.util.Map.entry("bread", 60),
        java.util.Map.entry("tom_kha_soup", 250),
        java.util.Map.entry("trout_soup", 100),
        java.util.Map.entry("chocolate_cake", 200),
        java.util.Map.entry("pink_cake", 480),
        java.util.Map.entry("rhubarb_pie", 400),
        java.util.Map.entry("cookie", 140),
        java.util.Map.entry("spaghetti", 120),
        java.util.Map.entry("fried_eel", 120),
        java.util.Map.entry("spicy_eel", 175),
        java.util.Map.entry("sashimi", 75),
        java.util.Map.entry("maki_roll", 220),
        java.util.Map.entry("tortilla", 50),
        java.util.Map.entry("red_plate", 400),
        java.util.Map.entry("eggplant_parmesan", 200),
        java.util.Map.entry("rice_pudding", 260),
        java.util.Map.entry("ice_cream", 120),
        java.util.Map.entry("blueberry_tart", 150),
        java.util.Map.entry("autums_bounty", 350),
        java.util.Map.entry("pumpkin_soup", 300),
        java.util.Map.entry("super_meal", 220),
        java.util.Map.entry("cranberry_sauce", 120),
        java.util.Map.entry("stuffing", 165),
        java.util.Map.entry("farmers_lunch", 150),
        java.util.Map.entry("survival_burger", 180),
        java.util.Map.entry("dish_of_the_sea", 220),
        java.util.Map.entry("miners_treat", 200),
        java.util.Map.entry("roots_platter", 100),
        java.util.Map.entry("triple_shot_espresso", 450),
        java.util.Map.entry("algae_soup", 100),
        java.util.Map.entry("pale_broth", 150),
        java.util.Map.entry("plum_pudding", 260),
        java.util.Map.entry("artichoke_dip", 210),
        java.util.Map.entry("stir_fry", 335),
        java.util.Map.entry("roasted_hazelnuts", 270),
        java.util.Map.entry("pumpkin_pie", 385),
        java.util.Map.entry("radish_salad", 300),
        java.util.Map.entry("blackberry_cobbler", 260),
        java.util.Map.entry("bruschetta", 210),
        java.util.Map.entry("poppyseed_muffin", 250),
        java.util.Map.entry("chowder", 135),
        java.util.Map.entry("lobster_bisque", 205),
        java.util.Map.entry("fish_stew", 175),
        java.util.Map.entry("maple_bar", 300),
        java.util.Map.entry("crab_cakes", 275),
        java.util.Map.entry("shrimp_cocktail", 160),
        java.util.Map.entry("ginger_ale", 200),
        java.util.Map.entry("poi", 400),
        java.util.Map.entry("tropical_curry", 500),
        java.util.Map.entry("moss_soup", 80),
        // 以下菜品补充价格
        java.util.Map.entry("banana_pudding", 260),
        java.util.Map.entry("coleslaw", 345),
        java.util.Map.entry("cranberry_candy", 175),
        java.util.Map.entry("escargot", 125),
        java.util.Map.entry("fiddlehead_risotto", 350),
        java.util.Map.entry("fish_taco", 500),
        java.util.Map.entry("fruit_salad", 450),
        java.util.Map.entry("mango_sticky_rice", 250),
        java.util.Map.entry("seafoam_pudding", 300),
        java.util.Map.entry("squid_ink_ravioli", 150),
        java.util.Map.entry("strange_bun", 225),
        // 新增物品价格
        java.util.Map.entry("dinosaur_egg", 625),
        java.util.Map.entry("truffle", 1065),
        // 鱼饵价格(均为1)
        java.util.Map.entry("bait_bait", 1),
        java.util.Map.entry("bait_challenge_bait", 1),
        java.util.Map.entry("bait_deluxe_bait", 1),
        java.util.Map.entry("bait_magic_bait", 1),
        java.util.Map.entry("bait_magnet", 1),
        java.util.Map.entry("bait_wild_bait", 1),
        // 渔具价格
        java.util.Map.entry("fishtool_barbed_hook", 500),
        java.util.Map.entry("fishtool_cork_bobber", 250),
        java.util.Map.entry("fishtool_curiosity_lure", 500),
        java.util.Map.entry("fishtool_dressed_spinner", 500),
        java.util.Map.entry("fishtool_lead_bobber", 150),
        java.util.Map.entry("fishtool_quality_bobber", 300),
        java.util.Map.entry("fishtool_sonar_bobber", 250),
        java.util.Map.entry("fishtool_spinner", 250),
        java.util.Map.entry("fishtool_trap_bobber", 200),
        java.util.Map.entry("fishtool_treasure_hunter", 250),
        // 戒指价格
        java.util.Map.entry("small_glow_ring", 50),
        java.util.Map.entry("glow_ring", 100),
        java.util.Map.entry("small_magnet_ring", 50),
        java.util.Map.entry("magnet_ring", 100),
        java.util.Map.entry("slime_charmer_ring", 350),
        java.util.Map.entry("warrior_ring", 750),
        java.util.Map.entry("vampire_ring", 750),
        java.util.Map.entry("savage_ring", 750),
        java.util.Map.entry("ring_of_yoba", 750),
        java.util.Map.entry("sturdy_ring", 750),
        java.util.Map.entry("burglars_ring", 750),
        java.util.Map.entry("iridium_band", 1000),
        java.util.Map.entry("jukebox_ring", 100),
        java.util.Map.entry("amethyst_ring", 100),
        java.util.Map.entry("topaz_ring", 100),
        java.util.Map.entry("aquamarine_ring", 200),
        java.util.Map.entry("jade_ring", 200),
        java.util.Map.entry("emerald_ring", 300),
        java.util.Map.entry("ruby_ring", 300),
        java.util.Map.entry("crabshell_ring", 1000),
        java.util.Map.entry("napalm_ring", 1000),
        java.util.Map.entry("thorns_ring", 200),
        java.util.Map.entry("lucky_ring", 200),
        java.util.Map.entry("hot_java_ring", 200),
        java.util.Map.entry("protection_ring", 200),
        java.util.Map.entry("soul_sapper_ring", 200),
        java.util.Map.entry("phoenix_ring", 200),
        java.util.Map.entry("immunity_band", 500),
        java.util.Map.entry("glowstone_ring", 200),
        // 精炼物品价格
        java.util.Map.entry("field_snack", 20),
        java.util.Map.entry("bug_steak", 30),
        java.util.Map.entry("life_elixir", 250),
        java.util.Map.entry("cherry_bomb", 50),
        java.util.Map.entry("bomb", 80),
        java.util.Map.entry("mega_bomb", 150),
        java.util.Map.entry("explosive_ammo", 50),
        java.util.Map.entry("fairy_dust", 500),
        java.util.Map.entry("monster_musk", 500),
        java.util.Map.entry("rain_totem", 500),
        java.util.Map.entry("staircase", 1),
        java.util.Map.entry("warp_totem_beach", 500),
        java.util.Map.entry("warp_totem_mountains", 500),
        java.util.Map.entry("warp_totem_farm", 500),
        java.util.Map.entry("warp_totem_desert", 500),
        java.util.Map.entry("warp_totem_island", 500),
        // 杂项物品价格
        java.util.Map.entry("void_essence", 50),
        java.util.Map.entry("acorn", 1),
        java.util.Map.entry("maple_seed", 1),
        java.util.Map.entry("pine_cone", 1),
        java.util.Map.entry("mahogany_seed", 1),
        // 野生种子价格
        java.util.Map.entry("spring_seeds", 35),
        java.util.Map.entry("summer_seeds", 55),
        java.util.Map.entry("fall_seeds", 45),
        java.util.Map.entry("winter_seeds", 30),
        java.util.Map.entry("ancient_seed", 1),
        java.util.Map.entry("grass_starter", 1),
        // 新增物品
        java.util.Map.entry("bat_wing", 15),
        java.util.Map.entry("dragon_tooth", 500),
        java.util.Map.entry("battery_pack", 500),
        java.util.Map.entry("cinder_shard", 5),
        java.util.Map.entry("dwarvish_safety_manual", 1000),
        // 古物价格
        java.util.Map.entry("dwarf_scroll_i", 1),
        java.util.Map.entry("dwarf_scroll_ii", 1),
        java.util.Map.entry("dwarf_scroll_iii", 1),
        java.util.Map.entry("dwarf_scroll_iv", 1),
        java.util.Map.entry("chipped_amphora", 40),
        java.util.Map.entry("arrowhead", 40),
        java.util.Map.entry("ancient_doll", 60),
        java.util.Map.entry("elvish_jewelry", 200),
        java.util.Map.entry("chewing_stick", 50),
        java.util.Map.entry("ornamental_fan", 300),
        java.util.Map.entry("rare_disc", 300),
        java.util.Map.entry("ancient_sword", 100),
        java.util.Map.entry("rusty_spoon", 25),
        java.util.Map.entry("rusty_spur", 25),
        java.util.Map.entry("rusty_cog", 25),
        java.util.Map.entry("chicken_statue", 50),
        java.util.Map.entry("prehistoric_tool", 50),
        java.util.Map.entry("dried_starfish", 40),
        java.util.Map.entry("anchor", 100),
        java.util.Map.entry("glass_shards", 20),
        java.util.Map.entry("bone_flute", 100),
        java.util.Map.entry("prehistoric_handaxe", 50),
        java.util.Map.entry("dwarvish_helm", 100),
        java.util.Map.entry("dwarf_gadget", 200),
        java.util.Map.entry("ancient_drum", 100),
        java.util.Map.entry("golden_mask", 500),
        java.util.Map.entry("golden_relic", 250),
        java.util.Map.entry("strange_doll_green", 1000),
        java.util.Map.entry("strange_doll_yellow", 1000),
        java.util.Map.entry("prehistoric_scapula", 100),
        java.util.Map.entry("prehistoric_tibia", 100),
        java.util.Map.entry("prehistoric_skull", 100),
        java.util.Map.entry("skeletal_hand", 100),
        java.util.Map.entry("prehistoric_rib", 100),
        java.util.Map.entry("prehistoric_vertebra", 100),
        java.util.Map.entry("skeletal_tail", 100),
        java.util.Map.entry("nautilus_fossil", 80),
        java.util.Map.entry("amphibian_fossil", 150),
        java.util.Map.entry("palm_fossil", 100),
        java.util.Map.entry("trilobite", 50),
        java.util.Map.entry("golden_pumpkin", 2500),
        java.util.Map.entry("treasure_chest", 5000)
    );

    public static int getItemMoneyValue(Identifier itemId) {
        var item = Registries.ITEM.get(itemId);
        if (item instanceof CropItem cropItem) {
            return cropItem.getMoneyValue();
        }
        if (item instanceof ArtisanItem artisanItem) {
            return artisanItem.getSellPrice();
        }
        if (item instanceof stardewvalley.modid.item.ModItems.TargetedBaitItem baitItem) {
            return baitItem.getBaitSellPrice();
        }
        if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return 0;
        Integer seedPrice = SEED_SELL_PRICES.get(itemId.getPath());
        if (seedPrice != null) return seedPrice;
        Integer orePrice = ORE_SELL_PRICES.get(itemId.getPath());
        if (orePrice != null) return orePrice;
        return MISC_SELL_PRICES.getOrDefault(itemId.getPath(), 0);
    }

    public static int getItemMoneyValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return getItemMoneyValue(Registries.ITEM.getId(stack.getItem()));
    }

    /** 对外公开：获取物品价格（可用于客户端） */
    public static int getPublicItemPrice(Identifier itemId) {
        return getItemMoneyValue(itemId);
    }

    /** 判断是否为古物（与星露谷古物标签栏一致） */
    private static final Set<String> ARTIFACT_IDS = Set.of(
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
    );

    public static boolean isArtifactItem(Identifier itemId) {
        return itemId.getNamespace().equals(StardewValley.MOD_ID) && ARTIFACT_IDS.contains(itemId.getPath());
    }

    /** 获取古物售价倍率（古代珍宝鉴定指南：4倍） */
    public static float getArtifactPriceMultiplier(UUID playerUuid, ServerWorld world) {
        if (playerUuid != null && stardewvalley.modid.gui.BookDataManager.get(world)
            .hasUsedBook(playerUuid, "treasure_appraisal_guide")) {
            return 4.0f;
        }
        return 1.0f;
    }
}
