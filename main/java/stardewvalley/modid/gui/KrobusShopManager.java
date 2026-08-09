package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class KrobusShopManager extends PersistentState {

    private static final String NAME = "stardewvalley_krobus_shop";

    private long lastGeneratedDay = -1;

    // 玩家购买记录: playerUUID -> (itemId -> 已购买数量)
    // stardrop等永久一次性物品也存储在这里，不会在每日重置时清除
    private final Map<UUID, Map<String, Integer>> purchasedCounts = new HashMap<>();

    // ====== 固定商品定义 ======
    private static final List<KrobusItemDef> FIXED_ITEMS = List.of(
        new KrobusItemDef("void_egg", 5000, 9999),   // 无限
        new KrobusItemDef("void_essence", 100, 10),
        new KrobusItemDef("solar_essence", 80, 10),
        new KrobusItemDef("stardrop", 20000, 1)      // 永久一次
    );

    // 周三鱼池
    private static final String[] WEDNESDAY_FISH = {
        "fish_albacore", "fish_bullhead", "fish_chub", "fish_dorado", "fish_halibut",
        "fish_lingcod", "fish_shad", "fish_sturgeon", "fish_tiger_trout", "fish_tilapia", "bait_magnet"
    };

    // 周六菜肴池
    private static final String[] SATURDAY_DISHES = {
        "autums_bounty", "baked_fish", "bean_hotpot", "blueberry_tart", "bread",
        "carp_surprise", "cheese_cauliflower", "chocolate_cake", "complete_breakfast", "cookie",
        "cranberry_sauce", "crispy_bass", "dish_of_the_sea", "eggplant_parmesan", "farmers_lunch",
        "fish_taco", "fried_calamari", "fried_eel", "fried_egg", "fried_mushroom",
        "glazed_yams", "hashbrowns", "ice_cream", "lucky_lunch", "maki_roll",
        "miners_treat", "omelet", "pancakes", "parsnip_soup", "pepper_poppers",
        "pink_cake", "pizza", "pumpkin_soup", "red_plate", "rhubarb_pie",
        "rice_pudding", "roots_platter", "salad", "salmon_dinner", "sashimi",
        "spaghetti", "spicy_eel", "strange_bun", "stuffing", "super_meal",
        "survival_burger", "tropical_curry", "tortilla", "trout_soup", "vegetable_medley"
    };

    private record KrobusItemDef(String itemId, int price, int maxBuy) {}

    // ====== 滚动商品定义 ======
    private record KrobusDailyItem(String itemId, int price, int maxBuy) {}

    // ====== 购买跟踪 ======

    /**
     * 获取某个物品对某个玩家的剩余可购买数量。
     * 对于 void_egg（无限），返回9999。
     * 对于 stardrop（永久一次），从持久化记录中检查。
     * 对于其他物品，从每日购买记录中计算。
     */
    public int getAvailableCount(UUID playerId, String itemId) {
        if ("void_egg".equals(itemId)) {
            return 9999;
        }
        if ("stardrop".equals(itemId)) {
            Map<String, Integer> playerPurchases = purchasedCounts.get(playerId);
            int bought = playerPurchases != null ? playerPurchases.getOrDefault("stardrop", 0) : 0;
            return Math.max(0, 1 - bought);
        }
        int maxBuy = getMaxBuyForItem(itemId);
        if (maxBuy <= 0) return 0;
        Map<String, Integer> playerPurchases = purchasedCounts.get(playerId);
        int bought = playerPurchases != null ? playerPurchases.getOrDefault(itemId, 0) : 0;
        return Math.max(0, maxBuy - bought);
    }

    /**
     * 尝试让玩家购买指定数量的物品。
     * 返回true如果购买成功，false如果超过限制。
     */
    public boolean tryPurchase(UUID playerId, String itemId, int count) {
        if (count <= 0) return false;
        // stardrop永久一次
        if ("stardrop".equals(itemId)) {
            Map<String, Integer> playerPurchases = purchasedCounts.computeIfAbsent(playerId, k -> new HashMap<>());
            int bought = playerPurchases.getOrDefault(itemId, 0);
            if (bought + count > 1) return false;
            playerPurchases.merge(itemId, count, Integer::sum);
            setDirty(true);
            return true;
        }
        // void_egg无限，允许购买
        if ("void_egg".equals(itemId)) {
            // 不限购，直接允许
            return true;
        }
        // 其他限购物品
        int available = getAvailableCount(playerId, itemId);
        if (available < count) return false;
        purchasedCounts.computeIfAbsent(playerId, k -> new HashMap<>())
            .merge(itemId, count, Integer::sum);
        setDirty(true);
        return true;
    }

    /** 每日重置购买记录（保留stardrop的永久记录） */
    private void resetDailyPurchases(long currentDay) {
        if (lastGeneratedDay != currentDay) {
            Map<UUID, Map<String, Integer>> stardropOnly = new HashMap<>();
            for (Map.Entry<UUID, Map<String, Integer>> entry : purchasedCounts.entrySet()) {
                UUID uuid = entry.getKey();
                Map<String, Integer> playerMap = entry.getValue();
                if (playerMap.containsKey("stardrop")) {
                    Map<String, Integer> stardropMap = new HashMap<>();
                    stardropMap.put("stardrop", playerMap.get("stardrop"));
                    stardropOnly.put(uuid, stardropMap);
                }
            }
            purchasedCounts.clear();
            purchasedCounts.putAll(stardropOnly);
        }
    }

    /**
     * 获取某个物品的每日最大可购买数量。
     * 先检查固定商品，再检查当天滚动商品。
     */
    private int getMaxBuyForItem(String itemId) {
        for (KrobusItemDef def : FIXED_ITEMS) {
            if (def.itemId.equals(itemId)) {
                return def.maxBuy;
            }
        }
        KrobusDailyItem daily = getTodayDailyItem();
        if (daily != null && daily.itemId.equals(itemId)) {
            return daily.maxBuy;
        }
        return 0;
    }

    // ====== 库存生成 ======

    /**
     * 获取给玩家的完整商品列表（含剩余可购买数量）。
     * 固定商品在前，滚动商品在后。
     */
    public List<ModPayloads.KrobusShopItem> getStockData(UUID playerId) {
        List<ModPayloads.KrobusShopItem> result = new ArrayList<>();
        // 固定商品
        for (KrobusItemDef def : FIXED_ITEMS) {
            int available = getAvailableCount(playerId, def.itemId);
            result.add(new ModPayloads.KrobusShopItem(def.itemId, def.price, def.maxBuy, available));
        }
        // 滚动商品
        KrobusDailyItem daily = getTodayDailyItem();
        if (daily != null) {
            int available = getAvailableCount(playerId, daily.itemId);
            result.add(new ModPayloads.KrobusShopItem(daily.itemId, daily.price, daily.maxBuy, available));
        }
        return result;
    }

    /**
     * 获取今天的滚动商品（根据Calendar的星期几确定）。
     * 使用 Calendar.DAY_OF_WEEK 的数值：
     *   1=周日, 2=周一, 3=周二, 4=周三, 5=周四, 6=周五, 7=周六
     */
    private KrobusDailyItem getTodayDailyItem() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        // 使用当天日期作为随机种子，确保同一天内一致
        long daySeed = cal.get(Calendar.YEAR) * 1000L + cal.get(Calendar.DAY_OF_YEAR);
        Random random = new Random(daySeed);
        return switch (dayOfWeek) {
            case Calendar.MONDAY -> new KrobusDailyItem("slime", 10, 50);
            case Calendar.TUESDAY -> new KrobusDailyItem("omni_geode", 300, 1);
            case Calendar.WEDNESDAY -> {
                String fish = WEDNESDAY_FISH[random.nextInt(WEDNESDAY_FISH.length)];
                yield new KrobusDailyItem(fish, 200, 5);
            }
            case Calendar.THURSDAY -> new KrobusDailyItem("mixedseeds", 30, 10);
            case Calendar.FRIDAY -> new KrobusDailyItem("iridium_sprinkler", 10000, 1);
            case Calendar.SATURDAY -> {
                String dish = SATURDAY_DISHES[random.nextInt(SATURDAY_DISHES.length)];
                int dishPrice = 50 + random.nextInt(451);
                yield new KrobusDailyItem(dish, dishPrice, 5);
            }
            case Calendar.SUNDAY -> new KrobusDailyItem("bat_wing", 30, 10);
            default -> null;
        };
    }

    /** 获取带可用数量的商品列表（服务端调用，别名） */
    public List<ModPayloads.KrobusShopItem> getStockForPlayer(UUID playerId) {
        return getStockData(playerId);
    }

    // ====== 序列化 ======

    private record PurchaseEntry(UUID playerId, Map<String, Integer> items) {}

    private static final Codec<Map<String, Integer>> ITEM_MAP_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);

    private static final Codec<PurchaseEntry> PURCHASE_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Uuids.CODEC.fieldOf("playerId").forGetter(PurchaseEntry::playerId),
            ITEM_MAP_CODEC.fieldOf("items").forGetter(PurchaseEntry::items)
        ).apply(instance, PurchaseEntry::new)
    );

    private record SaveData(long lastGeneratedDay, List<PurchaseEntry> purchases) {}

    private static final Codec<SaveData> SAVE_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.fieldOf("lastGeneratedDay").forGetter(SaveData::lastGeneratedDay),
            Codec.list(PURCHASE_ENTRY_CODEC).fieldOf("purchases").forGetter(SaveData::purchases)
        ).apply(instance, SaveData::new)
    );

    public static final Codec<KrobusShopManager> CODEC = SAVE_DATA_CODEC.xmap(
        data -> {
            KrobusShopManager mgr = new KrobusShopManager();
            mgr.lastGeneratedDay = data.lastGeneratedDay;
            for (PurchaseEntry entry : data.purchases) {
                mgr.purchasedCounts.put(entry.playerId, new HashMap<>(entry.items));
            }
            return mgr;
        },
        mgr -> {
            List<PurchaseEntry> purchaseList = new ArrayList<>();
            for (Map.Entry<UUID, Map<String, Integer>> entry : mgr.purchasedCounts.entrySet()) {
                purchaseList.add(new PurchaseEntry(entry.getKey(), new HashMap<>(entry.getValue())));
            }
            return new SaveData(mgr.lastGeneratedDay, purchaseList);
        }
    );

    private static final PersistentStateType<KrobusShopManager> TYPE = new PersistentStateType<>(
        NAME,
        KrobusShopManager::new,
        SafeCodec.wrap(CODEC, KrobusShopManager::new),
        DataFixTypes.LEVEL
    );

    public static KrobusShopManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    /** 每天调用，重置购买记录 */
    public void onNewDay(long currentDay) {
        if (lastGeneratedDay != currentDay) {
            resetDailyPurchases(currentDay);
            lastGeneratedDay = currentDay;
            setDirty(true);
        }
    }
}
