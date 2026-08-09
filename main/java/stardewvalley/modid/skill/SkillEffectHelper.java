package stardewvalley.modid.skill;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.crafting.CraftingMaterialSets;

import java.util.UUID;

public class SkillEffectHelper {

    // ===== 基础检查方法 =====

    public static boolean hasSkill(ServerWorld world, UUID playerUuid, String skillId) {
        SkillDataManager sdm = SkillDataManager.get(world);
        for (SkillRegistry.Category cat : SkillRegistry.Category.values()) {
            if (skillId.equals(sdm.getLevel5Skill(playerUuid, cat))) return true;
            if (skillId.equals(sdm.getLevel10Skill(playerUuid, cat))) return true;
        }
        return false;
    }

    public static boolean hasSkill(ServerPlayerEntity player, String skillId) {
        return hasSkill((ServerWorld) player.getEntityWorld(), player.getUuid(), skillId);
    }

    // ===== 耕种系效果 =====

    /** 农耕人：crop物品售卖价值 +10% */
    public static boolean hasTiller(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "tiller");
    }

    /** 畜牧人：动物标签物品价值 +20% */
    public static boolean hasRancher(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "rancher");
    }

    /** 农业学家：作物生长速度 +10%（乘法） */
    public static boolean hasAgriculturist(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "agriculturist");
    }

    /** 工匠：工匠制品价值 +40% */
    public static boolean hasArtisan(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "artisan");
    }

    /** 鸡舍大师：蛋类概率调整、每次下两个蛋 */
    public static boolean hasCoopmaster(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "coopmaster");
    }

    /** 牧羊人：羊毛翻倍 */
    public static boolean hasShepherd(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "shepherd");
    }

    // ===== 采集系效果 =====

    /** 护林人：砍原木 25% 额外掉一个原木 */
    public static boolean hasForester(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "forester");
    }

    /** 收集者：33% 概率双倍采集品 */
    public static boolean hasGatherer(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "gatherer");
    }

    /** 伐木工人：破坏原木 25% 掉硬木 */
    public static boolean hasLumberjack(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "lumberjack");
    }

    /** 植物学家：采集品必定铱星 */
    public static boolean hasBotanist(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "botanist");
    }

    /** 萃取者：树脂产品售卖 +30% */
    public static boolean hasTrapper(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "trapper");
    }

    /** 追踪者：采集品掉落概率翻倍 */
    public static boolean hasTracker(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "tracker");
    }

    // ===== 钓鱼系效果 =====

    /** 渔夫：鱼标签物品价值 +25% */
    public static boolean hasFisher(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "fisher");
    }

    /** 垂钓者：鱼标签物品价值 +50%（与渔夫叠加） */
    public static boolean hasAngler(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "angler");
    }

    /** 海盗：钓鱼宝藏概率加倍 */
    public static boolean hasPirate(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "pirate");
    }

    // ===== 采矿系效果 =====

    /** 矿工：矿脉掉落 +1 矿石 */
    public static boolean hasMiner(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "miner");
    }

    /** 地质学家：50% 概率宝石额外掉落两个 */
    public static boolean hasGeologist(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "geologist");
    }

    /** 铁匠：矿物标签物品价值 +40% */
    public static boolean hasBlacksmith(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "blacksmith");
    }

    /** 挖掘者：晶球掉落数量双倍 */
    public static boolean hasExcavator(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "excavator");
    }

    /** 勘探者：煤掉落几率/数量翻倍 */
    public static boolean hasProspector(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "prospector");
    }

    /** 宝石专家：矿石标签物品价值 +30% */
    public static boolean hasGemologist(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "gemologist");
    }

    // ===== 战斗系效果 =====

    /** 战士：伤害 +10%，最大生命 +3 */
    public static boolean hasFighter(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "fighter");
    }

    /** 侦查员：暴击几率 *1.5 */
    public static boolean hasScout(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "scout");
    }

    /** 野蛮人：伤害 +15%（与战士乘算叠加） */
    public static boolean hasBrute(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "brute");
    }

    /** 防御者：+5 最大生命 */
    public static boolean hasDefender(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "defender");
    }

    /** 特技者：武器技能冷却减半 */
    public static boolean hasAcrobat(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "acrobat");
    }

    /** 亡命徒：暴击伤害 *2 */
    public static boolean hasDesperado(ServerWorld world, UUID uuid) {
        return hasSkill(world, uuid, "desperado");
    }

    // ===== 伤害倍率计算 =====

    /** 获取总伤害倍率（战士 * 野蛮人，乘算） */
    public static float getDamageMultiplier(ServerWorld world, UUID uuid) {
        float mult = 1.0f;
        if (hasFighter(world, uuid)) mult *= 1.10f;
        if (hasBrute(world, uuid)) mult *= 1.15f;
        return mult;
    }

    /** 获取暴击几率倍率 */
    public static float getCritChanceMultiplier(ServerWorld world, UUID uuid) {
        if (hasScout(world, uuid)) return 1.5f;
        return 1.0f;
    }

    /** 获取暴击伤害倍率 */
    public static float getCritDamageMultiplier(ServerWorld world, UUID uuid) {
        if (hasDesperado(world, uuid)) return 2.0f;
        return 1.0f;
    }

    /** 获取额外最大生命值 */
    public static int getBonusMaxHealth(ServerWorld world, UUID uuid) {
        int bonus = 0;
        if (hasFighter(world, uuid)) bonus += 3;
        if (hasDefender(world, uuid)) bonus += 5;
        return bonus;
    }

    /** 武器技能冷却倍率 */
    public static float getSkillCooldownMultiplier(ServerWorld world, UUID uuid) {
        if (hasAcrobat(world, uuid)) return 0.5f;
        return 1.0f;
    }

    // ===== 标签辅助检查（全部基于 CraftingMaterialSets） =====

    /** 判断物品是否属于作物 */
    public static boolean isCropItem(Identifier itemId) {
        if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return false;
        String path = itemId.getPath();
        if (path.startsWith("caiji_")) return false;
        if (path.startsWith("fish_")) return false;
        return CraftingMaterialSets.ANY_CROP.contains(path);
    }

    /** 判断物品是否属于动物制品（蛋、奶、毛、鸭毛、兔子脚、松露，不含奶酪和蛋黄酱） */
    public static boolean isAnimalItem(Identifier itemId) {
        if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return false;
        return CraftingMaterialSets.ANY_ANIMAL_PRODUCT.contains(itemId.getPath());
    }

    /** 判断物品是否属于工匠制品 */
    public static boolean isArtisanItem(Identifier itemId) {
        if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return false;
        return CraftingMaterialSets.ANY_ARTISAN.contains(itemId.getPath());
    }

    /** 判断物品是否属于鱼类 */
    public static boolean isFishItem(Identifier itemId) {
        if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return false;
        return CraftingMaterialSets.ANY_FISH.contains(itemId.getPath());
    }

    /** 判断物品是否属于矿物（铁匠加成用） */
    public static boolean isMineralItem(Identifier itemId) {
        if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return false;
        return CraftingMaterialSets.ANY_MINERAL.contains(itemId.getPath());
    }

    /** 判断物品是否属于矿石/宝石（宝石专家加成用） */
    public static boolean isOreGemItem(Identifier itemId) {
        if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return false;
        return CraftingMaterialSets.ANY_GEMSTONE.contains(itemId.getPath());
    }

    /** 判断物品是否属于树脂产品（萃取者加成用） */
    public static boolean isResinItem(Identifier itemId) {
        if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return false;
        return CraftingMaterialSets.ANY_RESIN.contains(itemId.getPath());
    }

    /** 获取物品总售卖价值倍率 */
    public static float getSellPriceMultiplier(ServerWorld world, UUID uuid, Identifier itemId) {
        float mult = 1.0f;
        if (isCropItem(itemId) && hasTiller(world, uuid)) mult *= 1.10f;
        if (isAnimalItem(itemId) && hasRancher(world, uuid)) mult *= 1.20f;
        if (isArtisanItem(itemId) && hasArtisan(world, uuid)) mult *= 1.40f;
        if (isFishItem(itemId) && hasFisher(world, uuid)) mult *= 1.25f;
        if (isFishItem(itemId) && hasAngler(world, uuid)) mult *= 1.50f;
        if (isMineralItem(itemId) && hasBlacksmith(world, uuid)) mult *= 1.40f;
        if (isOreGemItem(itemId) && hasGemologist(world, uuid)) mult *= 1.30f;
        if (isResinItem(itemId) && hasTrapper(world, uuid)) mult *= 1.30f;
        return mult;
    }
}
