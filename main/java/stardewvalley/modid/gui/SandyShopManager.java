package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.*;

public class SandyShopManager extends PersistentState {

    private static final String NAME = "stardewvalley_sandy_shop";

    // 玩家购买记录: playerUUID -> (itemId -> 已购买数量)
    private final Map<UUID, Map<String, Integer>> purchasedCounts = new HashMap<>();

    public static class SandyRotatingItem {
        public final String itemId;
        public final int price;
        public final int maxBuy;

        public SandyRotatingItem(String itemId, int price, int maxBuy) {
            this.itemId = itemId;
            this.price = price;
            this.maxBuy = maxBuy;
        }
    }

    public int getAvailableCount(UUID playerId, String itemId) {
        int maxBuy = getMaxBuy(itemId);
        if (maxBuy <= 0) return 9999;
        Map<String, Integer> playerPurchases = purchasedCounts.get(playerId);
        int bought = playerPurchases != null ? playerPurchases.getOrDefault(itemId, 0) : 0;
        return Math.max(0, maxBuy - bought);
    }

    public boolean tryPurchase(UUID playerId, String itemId, int count) {
        if (count <= 0) return false;
        int maxBuy = getMaxBuy(itemId);
        if (maxBuy <= 0) return true;
        int available = getAvailableCount(playerId, itemId);
        if (available < count) return false;
        purchasedCounts.computeIfAbsent(playerId, k -> new HashMap<>())
            .merge(itemId, count, Integer::sum);
        setDirty(true);
        return true;
    }

    private int getMaxBuy(String itemId) {
        SandyRotatingItem today = getTodayItem();
        if (today != null && today.itemId.equals(itemId)) {
            return today.maxBuy;
        }
        return 0;
    }

    public static SandyRotatingItem getTodayItem() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return switch (dayOfWeek) {
            case Calendar.MONDAY -> new SandyRotatingItem("caiji_coconut", 200, 10);
            case Calendar.TUESDAY -> new SandyRotatingItem("cactusfruit", 150, 0);
            case Calendar.WEDNESDAY -> new SandyRotatingItem("omni_geode", 500, 3);
            case Calendar.THURSDAY -> new SandyRotatingItem("deluxe_speed-gro", 80, 0);
            case Calendar.FRIDAY -> new SandyRotatingItem("honey", 200, 0);
            case Calendar.SATURDAY -> new SandyRotatingItem("deluxe_retaining_soil", 200, 0);
            case Calendar.SUNDAY -> new SandyRotatingItem("ice_cream", 240, 0);
            default -> null;
        };
    }

    public ModPayloads.SandyShopStockItem getStockForPlayer(UUID playerId) {
        SandyRotatingItem today = getTodayItem();
        if (today == null) return null;
        int available = today.maxBuy > 0 ? getAvailableCount(playerId, today.itemId) : 9999;
        return new ModPayloads.SandyShopStockItem(today.itemId, today.price, today.maxBuy, available);
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

    private record SaveData(List<PurchaseEntry> purchases) {}

    private static final Codec<SaveData> SAVE_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(PURCHASE_ENTRY_CODEC).fieldOf("purchases").forGetter(SaveData::purchases)
        ).apply(instance, SaveData::new)
    );

    public static final Codec<SandyShopManager> CODEC = SAVE_DATA_CODEC.xmap(
        data -> {
            SandyShopManager mgr = new SandyShopManager();
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
            return new SaveData(purchaseList);
        }
    );

    private static final PersistentStateType<SandyShopManager> TYPE = new PersistentStateType<>(
        NAME,
        SandyShopManager::new,
        SafeCodec.wrap(CODEC, SandyShopManager::new),
        DataFixTypes.LEVEL
    );

    public static SandyShopManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }
}
