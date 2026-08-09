package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.util.SafeCodec;

import java.util.*;

public class BundleManager extends PersistentState {

    private static final String NAME = "stardewvalley_bundles";

    // 每个收集包的提交数据: bundleId -> (itemId -> 已提交数量)
    private final Map<String, Map<String, Integer>> bundleProgress = new LinkedHashMap<>();
    // 已完成(集齐)的收集包
    private final Set<String> completedBundles = new HashSet<>();
    // 已领取奖励的玩家UUID -> 已领取的bundleId集合
    private final Map<UUID, Set<String>> rewardClaimedPlayers = new HashMap<>();
    // 金库已购买的收集包
    private final Set<String> vaultPurchased = new HashSet<>();

    private BundleManager() {}

    // ===== 数据类型 =====

    public record BundleItemDef(String itemId, int count) {}
    public record BundleDef(
        String id, String categoryId, String name,
        List<BundleItemDef> requiredItems,
        String rewardItemId, int rewardCount,
        int slotCount
    ) {}

    // ===== Bundle Slot Data for sync =====
    public static class BundleSlotData {
        public String bundleId;
        public Map<String, Integer> submittedItems;
        public boolean completed;

        public BundleSlotData(String bundleId, Map<String, Integer> submittedItems, boolean completed) {
            this.bundleId = bundleId;
            this.submittedItems = submittedItems;
            this.completed = completed;
        }
    }

    // ===== 持久化编解码 =====

    private record Data(Map<String, Map<String, Integer>> progress, Set<String> completed,
                        Map<String, Set<String>> claimed, Set<String> vaultPurchased) {}

    private static final Codec<Map<String, Map<String, Integer>>> PROGRESS_CODEC =
        Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.INT));

    private static final Codec<Map<String, Set<String>>> CLAIMED_CODEC =
        Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf().xmap(
            l -> new HashSet<>(l), s -> new ArrayList<>(s)
        ));

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            PROGRESS_CODEC.fieldOf("progress").forGetter(d -> d.progress),
            Codec.STRING.listOf().xmap(
                (java.util.function.Function<java.util.List<String>, java.util.Set<String>>) (l) -> new HashSet<>(l),
                (java.util.function.Function<java.util.Set<String>, java.util.List<String>>) (s) -> new ArrayList<>(s)
            ).fieldOf("completed").forGetter(d -> d.completed),
            CLAIMED_CODEC.fieldOf("claimed").forGetter(d -> d.claimed),
            Codec.STRING.listOf().xmap(
                (java.util.function.Function<java.util.List<String>, java.util.Set<String>>) (l) -> new HashSet<>(l),
                (java.util.function.Function<java.util.Set<String>, java.util.List<String>>) (s) -> new ArrayList<>(s)
            ).fieldOf("vault_purchased").forGetter(d -> d.vaultPurchased)
        ).apply(instance, Data::new)
    );

    public static final Codec<BundleManager> CODEC = DATA_CODEC.xmap(
        BundleManager::createCodec,
        BundleManager::getData
    );

    public static final PersistentStateType<BundleManager> TYPE = new PersistentStateType<>(
        NAME,
        BundleManager::new,
        SafeCodec.wrap(CODEC, BundleManager::new),
        DataFixTypes.LEVEL
    );

    private static Data getData(BundleManager m) {
        Map<String, Set<String>> claimedStr = new HashMap<>();
        for (var e : m.rewardClaimedPlayers.entrySet()) {
            claimedStr.put(e.getKey().toString(), e.getValue());
        }
        return new Data(m.bundleProgress, m.completedBundles, claimedStr, m.vaultPurchased);
    }

    private static BundleManager createCodec(Data data) {
        BundleManager m = new BundleManager();
        m.bundleProgress.putAll(data.progress);
        m.completedBundles.addAll(data.completed);
        for (var e : data.claimed.entrySet()) {
            try {
                UUID uuid = UUID.fromString(e.getKey());
                m.rewardClaimedPlayers.put(uuid, new HashSet<>(e.getValue()));
            } catch (Exception ignored) {}
        }
        m.vaultPurchased.addAll(data.vaultPurchased);
        return m;
    }

    // ===== 静态获取实例 =====

    public static BundleManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    // ===== Bundle 数据定义 =====

    private static final List<BundleDef> ALL_BUNDLES = createBundleDefs();

    private static List<BundleDef> createBundleDefs() {
        List<BundleDef> list = new ArrayList<>();

        // ===== 工艺室 =====
        String CRAFT = "craft";
        list.add(new BundleDef("craft_spring", CRAFT, "春季采集收集包",
            List.of(
                new BundleItemDef("caiji_wildhorseradish", 1),
                new BundleItemDef("caiji_daffodil", 1),
                new BundleItemDef("caiji_leek", 1),
                new BundleItemDef("caiji_dandelion", 1)
            ), "spring_seeds", 30, 4));
        list.add(new BundleDef("craft_summer", CRAFT, "夏季采集收集包",
            List.of(
                new BundleItemDef("grape", 1),
                new BundleItemDef("caiji_spiceberry", 1),
                new BundleItemDef("caiji_sweetpea", 1)
            ), "summer_seeds", 30, 3));
        list.add(new BundleDef("craft_fall", CRAFT, "秋季采集收集包",
            List.of(
                new BundleItemDef("caiji_commonmushroom", 1),
                new BundleItemDef("caiji_wildplum", 1),
                new BundleItemDef("caiji_hazelnut", 1),
                new BundleItemDef("caiji_blackberry", 1)
            ), "fall_seeds", 30, 4));
        list.add(new BundleDef("craft_winter", CRAFT, "冬季采集收集包",
            List.of(
                new BundleItemDef("caiji_winterroot", 1),
                new BundleItemDef("caiji_crystalfruit", 1),
                new BundleItemDef("caiji_snowyam", 1),
                new BundleItemDef("caiji_crocus", 1)
            ), "winter_seeds", 30, 4));
        list.add(new BundleDef("craft_construction", CRAFT, "建筑收集包",
            List.of(
                new BundleItemDef("wood", 99),
                new BundleItemDef("wood", 99),
                new BundleItemDef("misc_stone", 99),
                new BundleItemDef("hardwood", 10)
            ), "charcoal_kiln", 1, 4));
        list.add(new BundleDef("craft_exotic", CRAFT, "异国情调采集收集包",
            List.of(
                new BundleItemDef("caiji_coconut", 1),
                new BundleItemDef("cactusfruit", 1),
                new BundleItemDef("caiji_cavecarrot", 1),
                new BundleItemDef("caiji_redmushroom", 1),
                new BundleItemDef("caiji_purplemushroom", 1),
                new BundleItemDef("maple_syrup", 1),
                new BundleItemDef("oak_resin", 1),
                new BundleItemDef("pine_tar", 1),
                new BundleItemDef("caiji_morel", 1)
            ), "autums_bounty", 5, 5));

        // ===== 茶水间 =====
        String PANTRY = "pantry";
        list.add(new BundleDef("pantry_spring", PANTRY, "春季作物收集包",
            List.of(
                new BundleItemDef("parsnip", 1),
                new BundleItemDef("greenbean", 1),
                new BundleItemDef("cauliflower", 1),
                new BundleItemDef("potato", 1)
            ), "speed-gro", 20, 4));
        list.add(new BundleDef("pantry_summer", PANTRY, "夏季作物收集包",
            List.of(
                new BundleItemDef("tomato", 1),
                new BundleItemDef("hotpepper", 1),
                new BundleItemDef("blueberry", 1),
                new BundleItemDef("melon", 1)
            ), "quality_sprinkler", 2, 4));
        list.add(new BundleDef("pantry_fall", PANTRY, "秋季作物收集包",
            List.of(
                new BundleItemDef("corn", 1),
                new BundleItemDef("eggplant", 1),
                new BundleItemDef("pumpkin", 1),
                new BundleItemDef("yam", 1)
            ), "bee_house", 1, 4));
        list.add(new BundleDef("pantry_quality", PANTRY, "高品质作物收集包",
            List.of(
                new BundleItemDef("parsnip_gold", 5),
                new BundleItemDef("melon_gold", 5),
                new BundleItemDef("pumpkin_gold", 5),
                new BundleItemDef("corn_gold", 5)
            ), "preserves_jar", 1, 4));
        list.add(new BundleDef("pantry_animal", PANTRY, "动物收集包",
            List.of(
                new BundleItemDef("large_milk", 1),
                new BundleItemDef("large_brown_egg", 1),
                new BundleItemDef("large_egg", 1),
                new BundleItemDef("large_goat_milk", 1),
                new BundleItemDef("wool", 1),
                new BundleItemDef("duck_egg", 1)
            ), "cheese_press", 1, 5));
        list.add(new BundleDef("pantry_artisan", PANTRY, "工匠收集包",
            List.of(
                new BundleItemDef("truffle_oil", 1),
                new BundleItemDef("cloth", 1),
                new BundleItemDef("goat_cheese", 1),
                new BundleItemDef("cheese", 1),
                new BundleItemDef("honey", 1),
                new BundleItemDef("ancient_fruit_jelly", 1),
                new BundleItemDef("apple", 1),
                new BundleItemDef("apricot", 1),
                new BundleItemDef("orange", 1),
                new BundleItemDef("peach", 1),
                new BundleItemDef("pomegranate", 1),
                new BundleItemDef("cherry", 1)
            ), "keg", 1, 6));

        // ===== 鱼缸 =====
        String FISH = "fish";
        list.add(new BundleDef("fish_river", FISH, "河鱼收集包",
            List.of(
                new BundleItemDef("fish_sunfish", 1),
                new BundleItemDef("fish_catfish", 1),
                new BundleItemDef("fish_shad", 1),
                new BundleItemDef("fish_tiger_trout", 1)
            ), "bait_deluxe_bait", 30, 4));
        list.add(new BundleDef("fish_lake", FISH, "湖鱼收集包",
            List.of(
                new BundleItemDef("fish_largemouth_bass", 1),
                new BundleItemDef("fish_carp", 1),
                new BundleItemDef("fish_bullhead", 1),
                new BundleItemDef("fish_sturgeon", 1)
            ), "fishtool_dressed_spinner", 1, 4));
        list.add(new BundleDef("fish_ocean", FISH, "海鱼收集包",
            List.of(
                new BundleItemDef("fish_sardine", 1),
                new BundleItemDef("fish_tuna", 1),
                new BundleItemDef("fish_red_snapper", 1),
                new BundleItemDef("fish_tilapia", 1)
            ), "warp_totem_beach", 5, 4));
        list.add(new BundleDef("fish_night", FISH, "夜间垂钓收集包",
            List.of(
                new BundleItemDef("fish_walleye", 1),
                new BundleItemDef("fish_bream", 1),
                new BundleItemDef("fish_eel", 1)
            ), "glow_ring", 1, 3));
        list.add(new BundleDef("fish_crab", FISH, "蟹笼收集包",
            List.of(
                new BundleItemDef("fish_lobster", 1),
                new BundleItemDef("fish_crayfish", 1),
                new BundleItemDef("fish_crab", 1),
                new BundleItemDef("caiji_cockle", 1),
                new BundleItemDef("caiji_mussel", 1),
                new BundleItemDef("fish_shrimp", 1),
                new BundleItemDef("fish_snail", 1),
                new BundleItemDef("fish_periwinkle", 1),
                new BundleItemDef("caiji_oyster", 1),
                new BundleItemDef("caiji_clam", 1)
            ), "crab_pot", 3, 5));
        list.add(new BundleDef("fish_specialty", FISH, "特色鱼类收集包",
            List.of(
                new BundleItemDef("fish_pufferfish", 1),
                new BundleItemDef("fish_ghostfish", 1),
                new BundleItemDef("fish_sandfish", 1),
                new BundleItemDef("fish_woodskip", 1)
            ), "dish_of_the_sea", 5, 4));

        // ===== 锅炉房 =====
        String BOILER = "boiler";
        list.add(new BundleDef("boiler_blacksmith", BOILER, "铁匠的收集包",
            List.of(
                new BundleItemDef("copper_bar", 1),
                new BundleItemDef("iron_bar", 1),
                new BundleItemDef("gold_bar", 1)
            ), "furnace", 1, 3));
        list.add(new BundleDef("boiler_geologist", BOILER, "地理学家的收集包",
            List.of(
                new BundleItemDef("quartz", 1),
                new BundleItemDef("earth_crystal", 1),
                new BundleItemDef("tear_crystal", 1),
                new BundleItemDef("fire_quartz", 1)
            ), "omni_geode", 5, 4));
        list.add(new BundleDef("boiler_adventurer", BOILER, "冒险家的收集包",
            List.of(
                new BundleItemDef("slime", 99),
                new BundleItemDef("bat_wing", 10),
                new BundleItemDef("solar_essence", 1),
                new BundleItemDef("void_essence", 1)
            ), "small_magnet_ring", 1, 4));

        // ===== 布告栏 =====
        String BULLETIN = "bulletin";
        list.add(new BundleDef("bulletin_chef", BULLETIN, "厨师收集包",
            List.of(
                new BundleItemDef("maple_syrup", 1),
                new BundleItemDef("caiji_fiddleheadfern", 1),
                new BundleItemDef("truffle", 1),
                new BundleItemDef("poppy", 1),
                new BundleItemDef("maki_roll", 1),
                new BundleItemDef("fried_egg", 1)
            ), "pink_cake", 3, 6));
        list.add(new BundleDef("bulletin_dye", BULLETIN, "染料收集包",
            List.of(
                new BundleItemDef("caiji_redmushroom", 1),
                new BundleItemDef("caiji_seaurchin", 1),
                new BundleItemDef("sunflower", 1),
                new BundleItemDef("duck_feather", 1),
                new BundleItemDef("aquamarine", 1),
                new BundleItemDef("redcabbage", 1)
            ), "_todo_seed_maker", 1, 6));
        list.add(new BundleDef("bulletin_field", BULLETIN, "土地研究收集包",
            List.of(
                new BundleItemDef("caiji_purplemushroom", 1),
                new BundleItemDef("caiji_nautilusshell", 1),
                new BundleItemDef("fish_lingcod", 1),
                new BundleItemDef("frozen_geode", 1)
            ), "_todo_recycling_machine", 1, 4));
        list.add(new BundleDef("bulletin_fodder", BULLETIN, "饲料收集包",
            List.of(
                new BundleItemDef("wheat", 10),
                new BundleItemDef("hay", 10),
                new BundleItemDef("apple", 3)
            ), "apple", 20, 3));
        list.add(new BundleDef("bulletin_enchanter", BULLETIN, "魔法师收集包",
            List.of(
                new BundleItemDef("oak_resin", 1),
                new BundleItemDef("ancient_fruit_wine", 1),
                new BundleItemDef("rabbits_foot", 1),
                new BundleItemDef("pomegranate", 1)
            ), "gold_bar", 5, 4));

        // ===== 金库 (特殊: 不需要Bundle_Slot, 而是Bundle_Purchase) =====
        String VAULT = "vault";
        list.add(new BundleDef("vault_2500", VAULT, "2,500金收集包",
            List.of(new BundleItemDef("_gold_2500", 1)), "chocolate_cake", 3, 0));
        list.add(new BundleDef("vault_5000", VAULT, "5,000金收集包",
            List.of(new BundleItemDef("_gold_5000", 1)), "quality_fertilizer", 30, 0));
        list.add(new BundleDef("vault_10000", VAULT, "10,000金收集包",
            List.of(new BundleItemDef("_gold_10000", 1)), "lightning_rod", 1, 0));
        list.add(new BundleDef("vault_25000", VAULT, "25,000金收集包",
            List.of(new BundleItemDef("_gold_25000", 1)), "crystalarium", 1, 0));

        // ===== 废弃Joja超市（遗失的收集包） =====
        String MISSING = "missing";
        list.add(new BundleDef("missing_joja", MISSING, "废弃Joja超市收集包",
            List.of(
                new BundleItemDef("_any_wine_silver", 1),
                new BundleItemDef("dinosaur_mayonnaise", 1),
                new BundleItemDef("prismatic_shard", 1),
                new BundleItemDef("ancientfruit_gold", 5),
                new BundleItemDef("fish_void_salmon_gold", 1),
                new BundleItemDef("caviar", 1)
            ), "stardrop", 1, 5));

        return Collections.unmodifiableList(list);
    }

    public static List<BundleDef> getAllBundles() { return ALL_BUNDLES; }

    public static List<BundleDef> getBundlesByCategory(String categoryId) {
        return ALL_BUNDLES.stream().filter(b -> b.categoryId().equals(categoryId)).toList();
    }

    public static BundleDef getBundleDef(String bundleId) {
        return ALL_BUNDLES.stream().filter(b -> b.id().equals(bundleId)).findFirst().orElse(null);
    }

    // ===== 状态操作 =====

    @SuppressWarnings("unchecked")
    public void submitItem(String bundleId, String itemId, int count) {
        bundleProgress.computeIfAbsent(bundleId, k -> new LinkedHashMap<>());
        Map<String, Integer> items = bundleProgress.get(bundleId);
        items.merge(itemId, count, (java.util.function.BinaryOperator<Integer>) (Integer a, Integer b) -> a + b);
        setDirty(true);
    }

    public int getSubmittedCount(String bundleId, String itemId) {
        Map<String, Integer> items = bundleProgress.get(bundleId);
        if (items == null) return 0;
        return items.getOrDefault(itemId, 0);
    }

    public Map<String, Integer> getSubmittedItems(String bundleId) {
        return bundleProgress.getOrDefault(bundleId, Collections.emptyMap());
    }

    public boolean isBundleCompleted(String bundleId) {
        return completedBundles.contains(bundleId);
    }

    public void checkAndCompleteBundle(String bundleId) {
        if (completedBundles.contains(bundleId)) return;
        BundleDef def = getBundleDef(bundleId);
        if (def == null) return;

        Map<String, Integer> submitted = bundleProgress.get(bundleId);
        if (submitted == null) return;

        // 异国情调采集收集包: 从收集物品里集齐任意5个就算完成
        // 废弃Joja超市收集包: 从收集物品里集齐任意5个就算完成
        boolean allDone;
        if (bundleId.equals("craft_exotic") || bundleId.equals("missing_joja") || bundleId.equals("fish_crab")) {
            int collected = 0;
            for (BundleItemDef item : def.requiredItems()) {
                int have = submitted.getOrDefault(item.itemId(), 0);
                if (have >= item.count()) collected++;
            }
            allDone = collected >= 5;
        } else {
            allDone = true;
            for (BundleItemDef item : def.requiredItems()) {
                int have = submitted.getOrDefault(item.itemId(), 0);
                if (have < item.count()) { allDone = false; break; }
            }
        }

        if (allDone) {
            completedBundles.add(bundleId);
            setDirty(true);
        }
    }

    public boolean hasClaimedReward(UUID playerUuid) {
        Set<String> claimed = rewardClaimedPlayers.get(playerUuid);
        return claimed != null && !claimed.isEmpty() &&
            BundleManager.getAllBundles().stream().anyMatch(b -> claimed.contains(b.id()));
    }

    public boolean hasClaimedBundleReward(UUID playerUuid, String bundleId) {
        Set<String> claimed = rewardClaimedPlayers.get(playerUuid);
        return claimed != null && claimed.contains(bundleId);
    }

    public void markBundleRewardClaimed(UUID playerUuid, String bundleId) {
        rewardClaimedPlayers.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(bundleId);
        setDirty(true);
    }

    public Set<String> getClaimedBundleIds(UUID playerUuid) {
        return rewardClaimedPlayers.getOrDefault(playerUuid, Collections.emptySet());
    }

    public boolean isVaultPurchased(String bundleId) {
        return vaultPurchased.contains(bundleId);
    }

    public void markVaultPurchased(String bundleId) {
        vaultPurchased.add(bundleId);
        completedBundles.add(bundleId);
        setDirty(true);
    }

    public Set<String> getCompletedBundles() {
        return Collections.unmodifiableSet(completedBundles);
    }

    public Set<String> getVaultPurchased() {
        return Collections.unmodifiableSet(vaultPurchased);
    }

    // ===== 全服同步数据 =====

    public List<BundleSlotData> getAllSlotData() {
        List<BundleSlotData> list = new ArrayList<>();
        for (BundleDef def : ALL_BUNDLES) {
            boolean completed = completedBundles.contains(def.id());
            Map<String, Integer> submitted = bundleProgress.getOrDefault(def.id(), Collections.emptyMap());
            list.add(new BundleSlotData(def.id(), submitted, completed));
        }
        return list;
    }
}
