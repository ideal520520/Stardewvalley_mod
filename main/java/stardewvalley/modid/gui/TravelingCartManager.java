package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TravelingCartManager extends PersistentState {

    private static final String NAME = "stardewvalley_traveling_cart";

    private long lastGeneratedDay = -1;
    private List<ModPayloads.TravelingCartItem> currentStock = new ArrayList<>();

    // 玩家购买记录: playerUUID -> (itemId -> 已购买数量)
    // 非持久化，每日重置
    private final Map<UUID, Map<String, Integer>> purchasedCounts = new HashMap<>();

    // ====== 购买跟踪 ======

    public int getAvailableCount(UUID playerId, String itemId) {
        int maxBuy = 0;
        for (ModPayloads.TravelingCartItem item : currentStock) {
            if (item.itemId.equals(itemId)) {
                maxBuy = item.maxBuy;
                break;
            }
        }
        if (maxBuy <= 0) return 0;
        Map<String, Integer> playerPurchases = purchasedCounts.get(playerId);
        int bought = playerPurchases != null ? playerPurchases.getOrDefault(itemId, 0) : 0;
        return Math.max(0, maxBuy - bought);
    }

    public boolean tryPurchase(UUID playerId, String itemId, int count) {
        int available = getAvailableCount(playerId, itemId);
        if (available < count) return false;
        purchasedCounts.computeIfAbsent(playerId, k -> new HashMap<>())
            .merge(itemId, count, Integer::sum);
        setDirty(true);
        return true;
    }

    private void resetPurchases(long currentDay) {
        if (lastGeneratedDay != currentDay) {
            purchasedCounts.clear();
        }
    }

    // ====== 序列化 ======

    private record ItemData(String itemId, int price, int maxBuy, boolean isSpecial) {}

    private static final Codec<ItemData> ITEM_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("itemId").forGetter(ItemData::itemId),
            Codec.INT.fieldOf("price").forGetter(ItemData::price),
            Codec.INT.fieldOf("maxBuy").forGetter(ItemData::maxBuy),
            Codec.BOOL.fieldOf("isSpecial").forGetter(ItemData::isSpecial)
        ).apply(instance, ItemData::new)
    );

    private record SaveData(long lastGeneratedDay, List<ItemData> items) {}

    private static final Codec<SaveData> SAVE_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.fieldOf("lastGeneratedDay").forGetter(SaveData::lastGeneratedDay),
            Codec.list(ITEM_DATA_CODEC).fieldOf("currentStock").forGetter(SaveData::items)
        ).apply(instance, SaveData::new)
    );

    public static final Codec<TravelingCartManager> CODEC = SAVE_DATA_CODEC.xmap(
        data -> {
            TravelingCartManager mgr = new TravelingCartManager();
            mgr.lastGeneratedDay = data.lastGeneratedDay;
            for (ItemData id : data.items) {
                mgr.currentStock.add(new ModPayloads.TravelingCartItem(id.itemId, id.price, id.maxBuy, id.maxBuy, id.isSpecial));
            }
            return mgr;
        },
        mgr -> {
            List<ItemData> itemList = new ArrayList<>();
            for (ModPayloads.TravelingCartItem item : mgr.currentStock) {
                itemList.add(new ItemData(item.itemId, item.price, item.maxBuy, item.isSpecial));
            }
            return new SaveData(mgr.lastGeneratedDay, itemList);
        }
    );

    private static final PersistentStateType<TravelingCartManager> TYPE = new PersistentStateType<>(
        NAME,
        TravelingCartManager::new,
        SafeCodec.wrap(CODEC, TravelingCartManager::new),
        DataFixTypes.LEVEL
    );

    public static TravelingCartManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public List<ModPayloads.TravelingCartItem> getCurrentStock() {
        return currentStock;
    }

    // ====== 普通商品池 ======

    private static final Map<String, int[]> REGULAR_STOCK = Map.ofEntries(
        // 食物
        Map.entry("algae_soup", new int[]{300, 1000}),
        Map.entry("artichoke_dip", new int[]{630, 1050}),
        Map.entry("autums_bounty", new int[]{1050, 1750}),
        Map.entry("baked_fish", new int[]{300, 1000}),
        Map.entry("bean_hotpot", new int[]{300, 1000}),
        Map.entry("blackberry_cobbler", new int[]{780, 1300}),
        Map.entry("blueberry_tart", new int[]{450, 1000}),
        Map.entry("bread", new int[]{180, 1000}),
        Map.entry("bruschetta", new int[]{630, 1050}),
        Map.entry("carp_surprise", new int[]{450, 1000}),
        Map.entry("cheese_cauliflower", new int[]{900, 1500}),
        Map.entry("chocolate_cake", new int[]{600, 1000}),
        Map.entry("chowder", new int[]{405, 1000}),
        Map.entry("coleslaw", new int[]{1035, 1725}),
        Map.entry("complete_breakfast", new int[]{1050, 1750}),
        Map.entry("cookie", new int[]{420, 1000}),
        Map.entry("crab_cakes", new int[]{825, 1375}),
        Map.entry("cranberry_candy", new int[]{525, 1000}),
        Map.entry("cranberry_sauce", new int[]{360, 1000}),
        Map.entry("crispy_bass", new int[]{450, 1000}),
        Map.entry("dish_of_the_sea", new int[]{660, 1100}),
        Map.entry("eggplant_parmesan", new int[]{600, 1000}),
        Map.entry("escargot", new int[]{375, 1000}),
        Map.entry("farmers_lunch", new int[]{450, 1000}),
        Map.entry("fiddlehead_risotto", new int[]{1050, 1750}),
        Map.entry("fish_stew", new int[]{525, 1000}),
        Map.entry("fish_taco", new int[]{1500, 2500}),
        Map.entry("fried_calamari", new int[]{450, 1000}),
        Map.entry("fried_eel", new int[]{360, 1000}),
        Map.entry("fried_egg", new int[]{105, 1000}),
        Map.entry("fried_mushroom", new int[]{600, 1000}),
        Map.entry("fruit_salad", new int[]{1350, 2250}),
        Map.entry("glazed_yams", new int[]{600, 1000}),
        Map.entry("hashbrowns", new int[]{360, 1000}),
        Map.entry("ice_cream", new int[]{360, 1000}),
        Map.entry("lobster_bisque", new int[]{615, 1025}),
        Map.entry("lucky_lunch", new int[]{750, 1250}),
        Map.entry("maki_roll", new int[]{660, 1100}),
        Map.entry("maple_bar", new int[]{900, 1500}),
        Map.entry("miners_treat", new int[]{600, 1000}),
        Map.entry("omelet", new int[]{375, 1000}),
        Map.entry("pale_broth", new int[]{450, 1000}),
        Map.entry("pancakes", new int[]{240, 1000}),
        Map.entry("parsnip_soup", new int[]{360, 1000}),
        Map.entry("pepper_poppers", new int[]{600, 1000}),
        Map.entry("pink_cake", new int[]{1440, 2400}),
        Map.entry("pizza", new int[]{900, 1500}),
        Map.entry("plum_pudding", new int[]{780, 1300}),
        Map.entry("poppyseed_muffin", new int[]{750, 1250}),
        Map.entry("pumpkin_pie", new int[]{1155, 1925}),
        Map.entry("pumpkin_soup", new int[]{900, 1500}),
        Map.entry("radish_salad", new int[]{900, 1500}),
        Map.entry("red_plate", new int[]{1200, 2000}),
        Map.entry("rhubarb_pie", new int[]{1200, 2000}),
        Map.entry("rice_pudding", new int[]{780, 1300}),
        Map.entry("roasted_hazelnuts", new int[]{810, 1350}),
        Map.entry("roots_platter", new int[]{300, 1000}),
        Map.entry("salad", new int[]{330, 1000}),
        Map.entry("salmon_dinner", new int[]{900, 1500}),
        Map.entry("sashimi", new int[]{225, 1000}),
        Map.entry("seafoam_pudding", new int[]{900, 1500}),
        Map.entry("shrimp_cocktail", new int[]{480, 1000}),
        Map.entry("spaghetti", new int[]{360, 1000}),
        Map.entry("spicy_eel", new int[]{525, 1000}),
        Map.entry("stir_fry", new int[]{1005, 1675}),
        Map.entry("strange_bun", new int[]{675, 1125}),
        Map.entry("stuffing", new int[]{495, 1000}),
        Map.entry("super_meal", new int[]{660, 1100}),
        Map.entry("survival_burger", new int[]{540, 1000}),
        Map.entry("tom_kha_soup", new int[]{750, 1250}),
        Map.entry("tortilla", new int[]{150, 1000}),
        Map.entry("triple_shot_espresso", new int[]{1350, 2250}),
        Map.entry("trout_soup", new int[]{300, 1000}),
        Map.entry("vegetable_medley", new int[]{360, 1000}),
        // 作物与采集
        Map.entry("amaranth", new int[]{450, 1000}),
        Map.entry("artichoke", new int[]{480, 1000}),
        Map.entry("beet", new int[]{300, 1000}),
        Map.entry("bokchoy", new int[]{240, 1000}),
        Map.entry("cauliflower", new int[]{525, 1000}),
        Map.entry("corn", new int[]{150, 1000}),
        Map.entry("eggplant", new int[]{180, 1000}),
        Map.entry("caiji_fiddleheadfern", new int[]{270, 1000}),
        Map.entry("garlic", new int[]{180, 1000}),
        Map.entry("greenbean", new int[]{120, 1000}),
        Map.entry("hops", new int[]{100, 1000}),
        Map.entry("kale", new int[]{330, 1000}),
        Map.entry("parsnip", new int[]{105, 1000}),
        Map.entry("potato", new int[]{240, 1000}),
        Map.entry("pumpkin", new int[]{960, 1600}),
        Map.entry("radish", new int[]{270, 1000}),
        Map.entry("redcabbage", new int[]{780, 1300}),
        Map.entry("tomato", new int[]{180, 1000}),
        Map.entry("unmilledrice", new int[]{100, 1000}),
        Map.entry("wheat", new int[]{100, 1000}),
        Map.entry("yam", new int[]{480, 1000}),
        Map.entry("apple", new int[]{300, 1000}),
        Map.entry("apricot", new int[]{150, 1000}),
        Map.entry("caiji_blackberry", new int[]{100, 1000}),
        Map.entry("blueberry", new int[]{150, 1000}),
        Map.entry("cactusfruit", new int[]{225, 1000}),
        Map.entry("cherry", new int[]{240, 1000}),
        Map.entry("caiji_coconut", new int[]{300, 1000}),
        Map.entry("cranberries", new int[]{225, 1000}),
        Map.entry("caiji_crystalfruit", new int[]{450, 1000}),
        Map.entry("grape", new int[]{240, 1000}),
        Map.entry("hotpepper", new int[]{120, 1000}),
        Map.entry("melon", new int[]{750, 1250}),
        Map.entry("orange", new int[]{300, 1000}),
        Map.entry("peach", new int[]{420, 1000}),
        Map.entry("pomegranate", new int[]{420, 1000}),
        Map.entry("rhubarb", new int[]{660, 1100}),
        Map.entry("caiji_salmonberry", new int[]{100, 1000}),
        Map.entry("caiji_spiceberry", new int[]{240, 1000}),
        Map.entry("starfruit", new int[]{2250, 3750}),
        Map.entry("strawberry", new int[]{360, 1000}),
        Map.entry("caiji_wildplum", new int[]{240, 1000}),
        Map.entry("bluejazz", new int[]{150, 1000}),
        Map.entry("caiji_crocus", new int[]{180, 1000}),
        Map.entry("fairyrose", new int[]{870, 1450}),
        Map.entry("poppy", new int[]{420, 1000}),
        Map.entry("summerspangle", new int[]{270, 1000}),
        Map.entry("sunflower", new int[]{240, 1000}),
        Map.entry("caiji_sweetpea", new int[]{150, 1000}),
        Map.entry("tulip", new int[]{100, 1000}),
        Map.entry("caiji_cavecarrot", new int[]{100, 1000}),
        Map.entry("caiji_chanterelle", new int[]{480, 1000}),
        Map.entry("caiji_commonmushroom", new int[]{120, 1000}),
        Map.entry("caiji_daffodil", new int[]{100, 1000}),
        Map.entry("caiji_dandelion", new int[]{120, 1000}),
        Map.entry("caiji_hazelnut", new int[]{270, 1000}),
        Map.entry("caiji_holly", new int[]{240, 1000}),
        Map.entry("caiji_leek", new int[]{180, 1000}),
        Map.entry("caiji_morel", new int[]{450, 1000}),
        Map.entry("caiji_purplemushroom", new int[]{750, 1250}),
        Map.entry("caiji_redmushroom", new int[]{225, 1000}),
        Map.entry("caiji_sap", new int[]{100, 1000}),
        Map.entry("caiji_snowyam", new int[]{300, 1000}),
        Map.entry("caiji_springonion", new int[]{100, 1000}),
        Map.entry("caiji_wildhorseradish", new int[]{150, 1000}),
        Map.entry("caiji_winterroot", new int[]{210, 1000}),
        // 种子与肥料
        Map.entry("basic_fertilizer", new int[]{100, 1000}),
        Map.entry("basic_retaining_soil", new int[]{100, 1000}),
        Map.entry("speed-gro", new int[]{100, 1000}),
        Map.entry("quality_fertilizer", new int[]{100, 1000}),
        Map.entry("quality_retaining_soil", new int[]{100, 1000}),
        Map.entry("hyper_speed-gro", new int[]{120, 1000}),
        Map.entry("deluxe_fertilizer", new int[]{100, 1000}),
        Map.entry("deluxe_retaining_soil", new int[]{100, 1000}),
        Map.entry("acorn", new int[]{100, 1000}),
        Map.entry("maple_seed", new int[]{100, 1000}),
        Map.entry("pine_cone", new int[]{100, 1000}),
        Map.entry("amaranth_seeds", new int[]{105, 1000}),
        Map.entry("ancient_seed", new int[]{100, 1000}),
        Map.entry("artichoke_seeds", new int[]{100, 1000}),
        Map.entry("beanstarter", new int[]{100, 1000}),
        Map.entry("beet_seeds", new int[]{100, 1000}),
        Map.entry("blueberry_seeds", new int[]{120, 1000}),
        Map.entry("bokchoy_seeds", new int[]{100, 1000}),
        Map.entry("cauliflower_seeds", new int[]{120, 1000}),
        Map.entry("coffeebean", new int[]{100, 1000}),
        Map.entry("corn_seeds", new int[]{225, 1000}),
        Map.entry("cranberries_seeds", new int[]{360, 1000}),
        Map.entry("eggplant_seeds", new int[]{100, 1000}),
        Map.entry("fairyrose_seeds", new int[]{300, 1000}),
        Map.entry("fall_seeds", new int[]{135, 1000}),
        Map.entry("garlic_seeds", new int[]{100, 1000}),
        Map.entry("grapestarter", new int[]{100, 1000}),
        Map.entry("hopsstarter", new int[]{100, 1000}),
        Map.entry("bluejazz_seeds", new int[]{100, 1000}),
        Map.entry("kale_seeds", new int[]{105, 1000}),
        Map.entry("melon_seeds", new int[]{120, 1000}),
        Map.entry("parsnip_seeds", new int[]{100, 1000}),
        Map.entry("hotpepper_seeds", new int[]{100, 1000}),
        Map.entry("poppy_seeds", new int[]{150, 1000}),
        Map.entry("potato_seeds", new int[]{100, 1000}),
        Map.entry("pumpkin_seeds", new int[]{150, 1000}),
        Map.entry("radish_seeds", new int[]{100, 1000}),
        Map.entry("redcabbage_seeds", new int[]{150, 1000}),
        Map.entry("rhubarb_seeds", new int[]{150, 1000}),
        Map.entry("unmilledrice_seeds", new int[]{100, 1000}),
        Map.entry("summerspangle_seeds", new int[]{100, 1000}),
        Map.entry("spring_seeds", new int[]{105, 1000}),
        Map.entry("starfruit_seeds", new int[]{600, 1000}),
        Map.entry("summer_seeds", new int[]{165, 1000}),
        Map.entry("sunflower_seeds", new int[]{100, 1000}),
        Map.entry("tomato_seeds", new int[]{100, 1000}),
        Map.entry("tulip_seeds", new int[]{100, 1000}),
        Map.entry("wheat_seeds", new int[]{100, 1000}),
        Map.entry("winter_seeds", new int[]{100, 1000}),
        Map.entry("yam_seeds", new int[]{100, 1000}),
        Map.entry("tealeaves_seeds", new int[]{750, 1250}),
        // 鱼类与海滩采集
        Map.entry("joja_cola", new int[]{100, 1000}),
        Map.entry("caiji_clam", new int[]{150, 1000}),
        Map.entry("caiji_coral", new int[]{240, 1000}),
        Map.entry("caiji_nautilusshell", new int[]{360, 1000}),
        Map.entry("caiji_rainbow_shell", new int[]{900, 1500}),
        Map.entry("caiji_seaurchin", new int[]{480, 1000}),
        Map.entry("fish_albacore", new int[]{225, 1000}),
        Map.entry("fish_anchovy", new int[]{100, 1000}),
        Map.entry("fish_bream", new int[]{135, 1000}),
        Map.entry("fish_bullhead", new int[]{225, 1000}),
        Map.entry("fish_carp", new int[]{100, 1000}),
        Map.entry("fish_catfish", new int[]{600, 1000}),
        Map.entry("fish_chub", new int[]{150, 1000}),
        Map.entry("caiji_cockle", new int[]{150, 1000}),
        Map.entry("fish_crab", new int[]{300, 1000}),
        Map.entry("fish_crayfish", new int[]{225, 1000}),
        Map.entry("fish_dorado", new int[]{300, 1000}),
        Map.entry("fish_eel", new int[]{255, 1000}),
        Map.entry("fish_flounder", new int[]{300, 1000}),
        Map.entry("fish_ghostfish", new int[]{135, 1000}),
        Map.entry("fish_halibut", new int[]{240, 1000}),
        Map.entry("fish_herring", new int[]{100, 1000}),
        Map.entry("fish_largemouth_bass", new int[]{300, 1000}),
        Map.entry("fish_lingcod", new int[]{360, 1000}),
        Map.entry("fish_lobster", new int[]{360, 1000}),
        Map.entry("fish_midnight_carp", new int[]{450, 1000}),
        Map.entry("caiji_mussel", new int[]{100, 1000}),
        Map.entry("fish_octopus", new int[]{450, 1000}),
        Map.entry("caiji_oyster", new int[]{120, 1000}),
        Map.entry("fish_perch", new int[]{165, 1000}),
        Map.entry("fish_periwinkle", new int[]{100, 1000}),
        Map.entry("fish_pike", new int[]{300, 1000}),
        Map.entry("fish_pufferfish", new int[]{600, 1000}),
        Map.entry("fish_rainbow_trout", new int[]{195, 1000}),
        Map.entry("fish_red_mullet", new int[]{225, 1000}),
        Map.entry("fish_red_snapper", new int[]{150, 1000}),
        Map.entry("fish_salmon", new int[]{225, 1000}),
        Map.entry("fish_sandfish", new int[]{225, 1000}),
        Map.entry("fish_sardine", new int[]{120, 1000}),
        Map.entry("fish_scorpion_carp", new int[]{450, 1000}),
        Map.entry("fish_sea_cucumber", new int[]{225, 1000}),
        Map.entry("fish_shad", new int[]{180, 1000}),
        Map.entry("fish_shrimp", new int[]{180, 1000}),
        Map.entry("fish_smallmouth_bass", new int[]{150, 1000}),
        Map.entry("fish_snail", new int[]{195, 1000}),
        Map.entry("fish_squid", new int[]{240, 1000}),
        Map.entry("fish_sturgeon", new int[]{600, 1000}),
        Map.entry("fish_sunfish", new int[]{100, 1000}),
        Map.entry("fish_super_cucumber", new int[]{750, 1250}),
        Map.entry("fish_tiger_trout", new int[]{450, 1000}),
        Map.entry("fish_tilapia", new int[]{225, 1000}),
        Map.entry("fish_tuna", new int[]{300, 1000}),
        Map.entry("fish_walleye", new int[]{315, 1000}),
        Map.entry("fish_woodskip", new int[]{225, 1000}),
        // 工匠制品
        Map.entry("beer", new int[]{600, 1000}),
        Map.entry("caviar", new int[]{1500, 2500}),
        Map.entry("cheese", new int[]{690, 1150}),
        Map.entry("cloth", new int[]{1410, 2350}),
        Map.entry("duck_mayonnaise", new int[]{1125, 1875}),
        Map.entry("goat_cheese", new int[]{1200, 2000}),
        Map.entry("green_tea", new int[]{300, 1000}),
        Map.entry("honey", new int[]{300, 1000}),
        Map.entry("ancient_fruit_jelly", new int[]{480, 1000}),
        Map.entry("tomato_juice", new int[]{450, 1000}),
        Map.entry("mayonnaise", new int[]{570, 1000}),
        Map.entry("mead", new int[]{600, 1000}),
        Map.entry("pale_ale", new int[]{900, 1500}),
        Map.entry("amaranth_pickles", new int[]{300, 1000}),
        Map.entry("truffle_oil", new int[]{3195, 5325}),
        Map.entry("ancient_fruit_wine", new int[]{1200, 2000}),
        // 动物产品
        Map.entry("duck_feather", new int[]{375, 1000}),
        Map.entry("rabbits_foot", new int[]{1695, 2825}),
        Map.entry("wool", new int[]{1020, 1700}),
        Map.entry("duck_egg", new int[]{285, 1000}),
        Map.entry("egg", new int[]{150, 1000}),
        Map.entry("brown_egg", new int[]{150, 1000}),
        Map.entry("large_egg", new int[]{285, 1000}),
        Map.entry("large_brown_egg", new int[]{285, 1000}),
        Map.entry("goat_milk", new int[]{675, 1125}),
        Map.entry("large_goat_milk", new int[]{1035, 1725}),
        Map.entry("milk", new int[]{375, 1000}),
        Map.entry("large_milk", new int[]{570, 1000}),
        Map.entry("truffle", new int[]{1875, 3125}),
        // 精炼物品
        Map.entry("bomb", new int[]{150, 1000}),
        Map.entry("cherry_bomb", new int[]{150, 1000}),
        Map.entry("life_elixir", new int[]{1500, 2500}),
        Map.entry("mega_bomb", new int[]{150, 1000}),
        Map.entry("oil_of_garlic", new int[]{3000, 5000}),
        Map.entry("sprinkler", new int[]{300, 1000}),
        Map.entry("quality_sprinkler", new int[]{1350, 2250}),
        // 资源
        Map.entry("coal", new int[]{100, 1000}),
        Map.entry("copper_bar", new int[]{180, 1000}),
        Map.entry("copper_ore", new int[]{100, 1000}),
        Map.entry("gold_bar", new int[]{750, 1250}),
        Map.entry("gold_ore", new int[]{100, 1000}),
        Map.entry("iridium_bar", new int[]{3000, 5000}),
        Map.entry("iridium_ore", new int[]{300, 1000}),
        Map.entry("iron_bar", new int[]{360, 1000}),
        Map.entry("iron_ore", new int[]{100, 1000}),
        Map.entry("refined_quartz", new int[]{150, 1000}),
        Map.entry("battery_pack", new int[]{1500, 2500}),
        Map.entry("clay", new int[]{100, 1000}),
        Map.entry("fiber", new int[]{100, 1000}),
        Map.entry("hardwood", new int[]{100, 1000}),
        Map.entry("misc_stone", new int[]{100, 1000}),
        Map.entry("wood", new int[]{100, 1000}),
        // 糖浆
        Map.entry("maple_syrup", new int[]{600, 1000}),
        Map.entry("oak_resin", new int[]{450, 1000}),
        Map.entry("pine_tar", new int[]{300, 1000}),
        // 鱼饵与渔具
        Map.entry("bait_bait", new int[]{100, 1000}),
        Map.entry("bait_magnet", new int[]{100, 1000}),
        Map.entry("fishtool_barbed_hook", new int[]{1500, 2500}),
        Map.entry("fishtool_cork_bobber", new int[]{750, 1250}),
        Map.entry("fishtool_dressed_spinner", new int[]{1500, 2500}),
        Map.entry("fishtool_lead_bobber", new int[]{450, 1000}),
        Map.entry("fishtool_spinner", new int[]{750, 1250}),
        Map.entry("fishtool_trap_bobber", new int[]{600, 1000}),
        Map.entry("fishtool_treasure_hunter", new int[]{750, 1250}),
        // 怪物掉落
        Map.entry("bat_wing", new int[]{100, 1000}),
        Map.entry("bug_meat", new int[]{100, 1000}),
        Map.entry("slime", new int[]{100, 1000}),
        Map.entry("solar_essence", new int[]{120, 1000}),
        Map.entry("void_essence", new int[]{150, 1000})
    );

    // ====== 特殊商品池 ======

    private record SpecialItem(String itemId, int fixedPrice, int weight) {}

    private static final List<SpecialItem> SPECIAL_ITEMS = List.of(
        new SpecialItem("sweetgemberry_seeds", 1000, 50),
        new SpecialItem("coffeebean", 2500, 25),
        new SpecialItem("stardew_valley_almanac", 6000, 5),
        new SpecialItem("woodcutters_weekly", 6000, 5),
        new SpecialItem("mining_monthly", 6000, 5),
        new SpecialItem("combat_quarterly", 6000, 5),
        new SpecialItem("bait_and_bobber", 6000, 5)
    );

    private static final int SPECIAL_TOTAL_WEIGHT = SPECIAL_ITEMS.stream().mapToInt(SpecialItem::weight).sum();

    // ====== 库存生成 ======

    public List<ModPayloads.TravelingCartItem> getOrGenerateStock(ServerWorld world, float luck) {
        long currentDay = world.getTimeOfDay() / 24000L;

        if (lastGeneratedDay == currentDay) {
            return currentStock;
        }

        resetPurchases(currentDay);
        Random random = new Random(currentDay);
        List<ModPayloads.TravelingCartItem> newStock = new ArrayList<>();

        // 1. 挑选10个普通商品
        List<String> regularKeys = new ArrayList<>(REGULAR_STOCK.keySet());
        for (int i = 0; i < 10; i++) {
            String itemId = regularKeys.get(random.nextInt(regularKeys.size()));
            int[] range = REGULAR_STOCK.get(itemId);
            int basePrice = range[0] + random.nextInt(range[1] - range[0] + 1);
            int price = Math.max(range[0], Math.min(range[1], (int) (basePrice * (1.0 + luck))));
            int maxBuy = random.nextFloat() < 0.5f ? 5 : 1;
            newStock.add(new ModPayloads.TravelingCartItem(itemId, price, maxBuy, maxBuy, false));
        }

        // 2. 25%概率添加1个特殊商品
        if (random.nextFloat() < 0.25f) {
            int roll = random.nextInt(SPECIAL_TOTAL_WEIGHT);
            int cumulative = 0;
            for (SpecialItem si : SPECIAL_ITEMS) {
                cumulative += si.weight;
                if (roll < cumulative) {
                    int maxBuy;
                    if ("sweetgemberry_seeds".equals(si.itemId)) {
                        maxBuy = random.nextFloat() < 0.1f ? 5 : 1;
                    } else {
                        maxBuy = 1;
                    }
                    newStock.add(new ModPayloads.TravelingCartItem(si.itemId, si.fixedPrice, maxBuy, maxBuy, true));
                    break;
                }
            }
        }

        lastGeneratedDay = currentDay;
        currentStock = newStock;
        setDirty(true);

        return currentStock;
    }

    /** 获取给玩家的库存（含剩余可购买数量） */
    public List<ModPayloads.TravelingCartItem> getStockForPlayer(ServerWorld world, float luck, UUID playerId) {
        getOrGenerateStock(world, luck);
        List<ModPayloads.TravelingCartItem> result = new ArrayList<>();
        for (ModPayloads.TravelingCartItem item : currentStock) {
            int available = getAvailableCount(playerId, item.itemId);
            result.add(new ModPayloads.TravelingCartItem(item.itemId, item.price, item.maxBuy, available, item.isSpecial));
        }
        return result;
    }
}
