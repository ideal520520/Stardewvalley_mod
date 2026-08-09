package stardewvalley.modid.skill;

import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

import java.util.*;

public class SkillRegistry {

    public enum Category {
        FARMING, FORAGING, FISHING, MINING, COMBAT
    }

    public static class SkillEntry {
        public final String id;
        public final String name;
        public final String description;
        public final Category category;
        public final int tier; // 1 = level 5, 2 = level 10
        public final String prerequisite; // null for tier 1
        public final Identifier texture;
        public final boolean enabled;

        public SkillEntry(String id, String name, String description, Category category, int tier, String prerequisite, boolean enabled) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.tier = tier;
            this.prerequisite = prerequisite;
            this.texture = Identifier.of(StardewValley.MOD_ID, "textures/gui/skill/36px-" + id + ".png");
            this.enabled = enabled;
        }

        public SkillEntry(String id, String name, String description, Category category, int tier, String prerequisite) {
            this(id, name, description, category, tier, prerequisite, true);
        }
    }

    private static final Map<String, SkillEntry> ALL_SKILLS = new LinkedHashMap<>();

    // ─── 耕种 (FARMING) ───
    // 一级 (level 5)
    public static final SkillEntry RANCHER = register("rancher", "畜牧人", "所有动物物品（蛋、奶、毛等）的售卖价值提升20%（乘法）", Category.FARMING, 1, null);
    public static final SkillEntry TILLER = register("tiller", "农耕人", "所有作物物品的售卖价值提升10%（乘法）", Category.FARMING, 1, null);
    // 二级 (level 10)
    public static final SkillEntry AGRICULTURIST = register("agriculturist", "农业学家", "由你种下的作物生长速度提高10%（乘法，与化肥叠加）（前置：农耕人）", Category.FARMING, 2, "tiller");
    public static final SkillEntry ARTISAN = register("artisan", "工匠", "所有工匠制品（果汁、腌菜、果酱、酒、奶酪等）的售卖价值上升40%（乘法）（前置：农耕人）", Category.FARMING, 2, "tiller");
    public static final SkillEntry COOPMASTER = register("coopmaster", "鸡舍大师", "被你喂食过的鸡每次下两个蛋，且蛋类概率调整为：鸡蛋25%、棕色鸡蛋25%、大鸡蛋10%、大棕色鸡蛋10%、虚空蛋8%、金蛋7%、恐龙蛋15%（前置：畜牧人）", Category.FARMING, 2, "rancher");
    public static final SkillEntry SHEPHERD = register("shepherd", "牧羊人", "你使用剪刀剪羊毛时，掉落的羊毛数量翻倍（前置：畜牧人）", Category.FARMING, 2, "rancher");

    // ─── 采集 (FORAGING) ───
    public static final SkillEntry FORESTER = register("forester", "护林人", "你砍伐原木时有额外25%概率再掉落一个原木（木头）", Category.FORAGING, 1, null);
    public static final SkillEntry GATHERER = register("gatherer", "收集者", "你采集物品时，有33%的概率获得双倍数量", Category.FORAGING, 1, null);
    public static final SkillEntry LUMBERJACK = register("lumberjack", "伐木工人", "你破坏原木时，有25%几率额外掉落一个硬木（前置：护林人）", Category.FORAGING, 2, "forester");
    public static final SkillEntry BOTANIST = register("botanist", "植物学家", "你掉落的采集品品质必定是铱星（前置：收集者）", Category.FORAGING, 2, "gatherer");
    public static final SkillEntry TRAPPER = register("trapper", "萃取者", "树脂产品（枫糖浆、橡树树脂、松焦油、树液、蘑菇类）的售卖价值增加30%（乘法）（前置：收集者）", Category.FORAGING, 2, "gatherer");
    public static final SkillEntry TRACKER = register("tracker", "追踪者", "你掉落采集品的概率翻倍（前置：护林人）", Category.FORAGING, 2, "forester");

    // ─── 钓鱼 (FISHING) ───
    public static final SkillEntry FISHER = register("fisher", "渔夫", "所有星露谷鱼类物品的售卖价值提高25%（乘法）", Category.FISHING, 1, null);
    public static final SkillEntry TAPPER = register("tapper", "捕猎者", "建造蟹笼所需的材料用量会减少", Category.FISHING, 1, null);
    public static final SkillEntry ANGLER = register("angler", "垂钓者", "所有星露谷鱼类物品的售卖价值额外增加50%（乘法，与渔夫乘算叠加）（前置：渔夫）", Category.FISHING, 2, "fisher");
    public static final SkillEntry PIRATE = register("pirate", "海盗", "钓鱼小游戏找到宝藏的概率加倍（前置：渔夫）", Category.FISHING, 2, "fisher");
    public static final SkillEntry MARINER = register("mariner", "水手", "蟹笼不再产生垃圾物品", Category.FISHING, 2, "tapper", true);
    public static final SkillEntry LUREMASTER = register("luremaster", "诱饵大师", "蟹笼不再需要诱饵", Category.FISHING, 2, "tapper", true);

    // ─── 挖矿 (MINING) ───
    public static final SkillEntry MINER = register("miner", "矿工", "你挖掘原版矿脉（煤矿、铜矿、铁矿、金矿、钻石矿）时，额外掉落1块对应的矿石", Category.MINING, 1, null);
    public static final SkillEntry GEOLOGIST = register("geologist", "地质学家", "你挖掘石头掉落宝石时，有50%的概率额外多掉落一个", Category.MINING, 1, null);
    public static final SkillEntry BLACKSMITH = register("blacksmith", "铁匠", "所有星露谷矿物（矿石、金属锭）的物品售卖价值增加40%（乘法）（前置：矿工）", Category.MINING, 2, "miner");
    public static final SkillEntry EXCAVATOR = register("excavator", "挖掘者", "你挖掘石头掉落晶球时，掉落数量变为双倍（前置：地质学家）", Category.MINING, 2, "geologist");
    public static final SkillEntry PROSPECTOR = register("prospector", "勘探者", "你挖掘煤矿时额外掉落1~6个煤炭（数量随机）（前置：矿工）", Category.MINING, 2, "miner");
    public static final SkillEntry GEMOLOGIST = register("gemologist", "宝石专家", "所有星露谷矿石与宝石类物品的售卖价值提高30%（乘法）（前置：地质学家）", Category.MINING, 2, "geologist");

    // ─── 战斗 (COMBAT) ───
    public static final SkillEntry FIGHTER = register("fighter", "战士", "你造成的所有伤害值增加10%（乘法），最大生命值+3", Category.COMBAT, 1, null);
    public static final SkillEntry SCOUT = register("scout", "侦查员", "你的武器暴击几率提高50%（乘法：暴击几率 = 暴击几率 × 1.5）", Category.COMBAT, 1, null);
    public static final SkillEntry BRUTE = register("brute", "野蛮人", "你造成的伤害值额外增加15%（乘法，与战士乘算叠加：1.10×1.15=1.265倍）（前置：战士）", Category.COMBAT, 2, "fighter");
    public static final SkillEntry DEFENDER = register("defender", "防御者", "最大生命值+5（与战士的+3累加，共+8）（前置：战士）", Category.COMBAT, 2, "fighter");
    public static final SkillEntry ACROBAT = register("acrobat", "特技者", "你的武器技能冷却时间减少一半（前置：侦查员）", Category.COMBAT, 2, "scout");
    public static final SkillEntry DESPERADO = register("desperado", "亡命徒", "你对敌人造成的暴击伤害变为2倍（前置：侦查员）", Category.COMBAT, 2, "scout");

    private static SkillEntry register(String id, String name, String description, Category category, int tier, String prerequisite, boolean enabled) {
        SkillEntry entry = new SkillEntry(id, name, description, category, tier, prerequisite, enabled);
        ALL_SKILLS.put(id, entry);
        return entry;
    }

    private static SkillEntry register(String id, String name, String description, Category category, int tier, String prerequisite) {
        return register(id, name, description, category, tier, prerequisite, true);
    }

    public static SkillEntry get(String id) {
        return ALL_SKILLS.get(id);
    }

    public static Collection<SkillEntry> getAll() {
        return ALL_SKILLS.values();
    }

    public static List<SkillEntry> getByCategory(Category category) {
        return ALL_SKILLS.values().stream()
                .filter(e -> e.category == category)
                .toList();
    }

    public static List<SkillEntry> getTier1ByCategory(Category category) {
        return ALL_SKILLS.values().stream()
                .filter(e -> e.category == category && e.tier == 1)
                .toList();
    }

    public static List<SkillEntry> getTier2ByCategory(Category category) {
        return ALL_SKILLS.values().stream()
                .filter(e -> e.category == category && e.tier == 2)
                .toList();
    }

    public static List<SkillEntry> getAvailableTier2(Category category, String tier1Choice) {
        if (tier1Choice == null) return List.of();
        return ALL_SKILLS.values().stream()
                .filter(e -> e.category == category && e.tier == 2 && tier1Choice.equals(e.prerequisite))
                .toList();
    }
}
