package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.*;

public class MuseumManager extends PersistentState {

    private static final String NAME = "stardewvalley_museum";

    // 全服共享的已捐赠物品ID列表（按捐赠顺序）
    private final List<String> donatedItems = new ArrayList<>();
    // 每个UUID领取过的奖励索引集合
    private final Map<UUID, Set<Integer>> claimedRewards = new HashMap<>();
    // UUID -> 已获得的特殊能力
    private final Map<UUID, Set<String>> playerAbilities = new HashMap<>();

    private MuseumManager() {}

    // ===== 可捐赠的95种物品定义 =====

    public static final Set<String> DONATABLE_ITEMS = new HashSet<>();

    static {
        // 42种古物
        String[] artifacts = {
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
        // 41种晶球矿物
        String[] minerals = {
            "tigerseye", "opal", "fire_opal", "alamite", "bixite",
            "baryte", "aerinite", "calcite", "dolomite", "esperite",
            "fluorapatite", "geminite", "helvite", "jamborite", "jagoite",
            "kyanite", "lunarite", "malachite", "neptunite", "lemon_stone",
            "nekoite", "orpiment", "petrified_slime", "thunder_egg", "pyrite",
            "ocean_stone", "ghost_crystal", "jasper", "celestine", "marble",
            "sandstone", "granite", "basalt", "limestone", "soapstone",
            "hematite", "mudstone", "obsidian", "slate", "fairy_stone", "star_shards"
        };
        // 12种宝石
        String[] gems = {
            "quartz", "earth_crystal", "fire_quartz", "tear_crystal",
            "emerald", "ruby", "aquamarine", "amethyst", "topaz",
            "jade", "diamond", "prismatic_shard"
        };
        DONATABLE_ITEMS.addAll(Arrays.asList(artifacts));
        DONATABLE_ITEMS.addAll(Arrays.asList(minerals));
        DONATABLE_ITEMS.addAll(Arrays.asList(gems));
    }

    public static boolean isDonatable(String itemId) {
        return DONATABLE_ITEMS.contains(itemId);
    }

    // ===== 奖励定义 =====

    public record RewardDef(int requiredCount, String rewardItemId, int rewardCount, boolean isAbility, String abilityId) {}

    public static final List<RewardDef> REWARDS = createRewards();

    private static List<RewardDef> createRewards() {
        List<RewardDef> list = new ArrayList<>();
        list.add(new RewardDef(5, "cauliflower_seeds", 9, false, null));
        list.add(new RewardDef(10, "melon_seeds", 9, false, null));
        list.add(new RewardDef(15, "starfruit_seeds", 1, false, null));
        list.add(new RewardDef(20, "ancientfruit_seeds", 1, false, null));
        list.add(new RewardDef(25, "keg", 2, false, null));
        list.add(new RewardDef(30, "battery_pack", 5, false, null));
        list.add(new RewardDef(35, "pumpkin_seeds", 9, false, null));
        list.add(new RewardDef(40, "garden_pot", 5, false, null));
        list.add(new RewardDef(50, "crystalarium", 2, false, null));
        list.add(new RewardDef(60, null, 0, true, "rusty_key"));
        list.add(new RewardDef(70, "triple_shot_espresso", 3, false, null));
        list.add(new RewardDef(80, "warp_totem_farm", 5, false, null));
        list.add(new RewardDef(90, "magic_rock_candy", 1, false, null));
        list.add(new RewardDef(95, "stardrop", 1, false, null));
        return Collections.unmodifiableList(list);
    }

    // 特定物品捐赠奖励：捐赠指定物品后触发
    public record SpecialRewardDef(String triggerItemId, String rewardItemId, int rewardCount, boolean isAbility, String abilityId) {}

    public static final List<SpecialRewardDef> SPECIAL_REWARDS = createSpecialRewards();

    private static List<SpecialRewardDef> createSpecialRewards() {
        List<SpecialRewardDef> list = new ArrayList<>();
        list.add(new SpecialRewardDef("ancient_seed", "ancientfruit_seeds", 1, false, null));
        return Collections.unmodifiableList(list);
    }

    // 矮人卷轴全套捐赠后可获能力（需要I+II+III+IV全部捐赠）
    public static final List<String> DWARF_SCROLLS = List.of("dwarf_scroll_i", "dwarf_scroll_ii", "dwarf_scroll_iii", "dwarf_scroll_iv");

    // ===== 数据操作 =====

    /**
     * @return 全服累计捐赠总数
     */
    public int getTotalDonationCount() {
        return donatedItems.size();
    }

    /**
     * @return 全服已捐赠物品列表（按捐赠顺序）
     */
    public List<String> getDonatedItems() {
        return Collections.unmodifiableList(donatedItems);
    }

    /**
     * 捐赠物品。返回该物品是否首次捐赠（全服首次）。
     * 如果全服已捐赠过该物品则不重复添加。
     */
    public boolean donateItem(String itemId) {
        if (donatedItems.contains(itemId)) return false;
        donatedItems.add(itemId);
        setDirty(true);
        return true;
    }

    public boolean hasClaimedReward(UUID playerUuid, int rewardIndex) {
        Set<Integer> claimed = claimedRewards.get(playerUuid);
        return claimed != null && claimed.contains(rewardIndex);
    }

    public void claimReward(UUID playerUuid, int rewardIndex) {
        claimedRewards.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(rewardIndex);
        setDirty(true);
    }

    public boolean hasAbility(UUID playerUuid, String abilityId) {
        Set<String> abilities = playerAbilities.get(playerUuid);
        return abilities != null && abilities.contains(abilityId);
    }

    public void addAbility(UUID playerUuid, String abilityId) {
        playerAbilities.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(abilityId);
        setDirty(true);
    }

    /**
     * 检查全服是否已集齐矮人卷轴I-IV
     */
    public boolean areAllDwarfScrollsDonatedGlobally() {
        return donatedItems.containsAll(DWARF_SCROLLS);
    }

    // ===== 持久化编解码 =====

    private record Data(List<String> donated, Map<String, Set<Integer>> claimed, Map<String, Set<String>> abilities) {}

    private static final Codec<Map<String, Set<Integer>>> CLAIMED_CODEC =
        Codec.unboundedMap(Codec.STRING, Codec.list(Codec.INT).xmap(
            l -> new HashSet<>(l), s -> new ArrayList<>(s)));

    private static final Codec<Map<String, Set<String>>> ABILITIES_CODEC =
        Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf().xmap(
            l -> new HashSet<>(l), s -> new ArrayList<>(s)));

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.listOf().fieldOf("donated").forGetter(d -> d.donated),
            CLAIMED_CODEC.fieldOf("claimed").forGetter(d -> d.claimed),
            ABILITIES_CODEC.fieldOf("abilities").forGetter(d -> d.abilities)
        ).apply(instance, Data::new)
    );

    public static final Codec<MuseumManager> CODEC = DATA_CODEC.xmap(
        MuseumManager::createCodec,
        MuseumManager::getData
    );

    public static final PersistentStateType<MuseumManager> TYPE = new PersistentStateType<>(
        NAME,
        MuseumManager::new,
        SafeCodec.wrap(CODEC, MuseumManager::new),
        DataFixTypes.LEVEL
    );

    private static Data getData(MuseumManager m) {
        Map<String, Set<Integer>> claimedStr = new HashMap<>();
        for (var e : m.claimedRewards.entrySet()) {
            claimedStr.put(e.getKey().toString(), new HashSet<>(e.getValue()));
        }
        Map<String, Set<String>> abilitiesStr = new HashMap<>();
        for (var e : m.playerAbilities.entrySet()) {
            abilitiesStr.put(e.getKey().toString(), new HashSet<>(e.getValue()));
        }
        return new Data(new ArrayList<>(m.donatedItems), claimedStr, abilitiesStr);
    }

    private static MuseumManager createCodec(Data data) {
        MuseumManager m = new MuseumManager();
        m.donatedItems.addAll(data.donated);
        for (var e : data.claimed.entrySet()) {
            try {
                UUID uuid = UUID.fromString(e.getKey());
                m.claimedRewards.put(uuid, new HashSet<>(e.getValue()));
            } catch (Exception ignored) {}
        }
        for (var e : data.abilities.entrySet()) {
            try {
                UUID uuid = UUID.fromString(e.getKey());
                m.playerAbilities.put(uuid, new HashSet<>(e.getValue()));
            } catch (Exception ignored) {}
        }
        return m;
    }

    // ===== 静态获取实例 =====

    public static MuseumManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    // ===== 为同步构造数据 =====

    public MuseumSyncData createSyncData(UUID playerUuid) {
        return new MuseumSyncData(
            new ArrayList<>(donatedItems),
            claimedRewards.getOrDefault(playerUuid, Collections.emptySet()),
            playerAbilities.getOrDefault(playerUuid, Collections.emptySet())
        );
    }

    // ===== 同步数据结构 =====
    public record MuseumSyncData(List<String> donatedItems, Set<Integer> claimedRewardIndices, Set<String> abilities) {}
}
