package stardewvalley.modid;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import stardewvalley.modid.season.LuckManager;

/**
 * 动物品质产出系统
 * 当动物好感度>150时，可产出银/金/铱品质的动物制品
 */
public final class AnimalQualityHelper {

    private AnimalQualityHelper() {}

    /** 品质枚举 */
    public enum Quality {
        NORMAL(0, ""),
        SILVER(1, "_silver"),
        GOLD(2, "_gold"),
        IRIDIUM(3, "_iridium");

        public final int tier;
        public final String suffix;

        Quality(int tier, String suffix) {
            this.tier = tier;
            this.suffix = suffix;
        }
    }

    /**
     * 根据动物好感度和每日运气决定产出品质
     * 好感度>150才可产出高品质
     * 
     * 概率公式：
     *   gold = (friendship / 1000) * dailyLuck
     *   silver = 2 × gold
     *   iridium = 0.5 × gold
     * 
     * 判定顺序：先判定铱星，失败再判定金星，再失败再判定银星，再失败则普通品质
     */
    public static Quality determineQuality(int friendship, float dailyLuck, Random random) {
        if (friendship <= 150 || dailyLuck <= 0) {
            return Quality.NORMAL;
        }

        float goldChance = (friendship / 1000.0f) * dailyLuck;
        float iridiumChance = goldChance * 0.5f;
        float silverChance = goldChance * 2.0f;

        float roll = random.nextFloat();

        // 先判定铱星
        if (roll < iridiumChance) {
            return Quality.IRIDIUM;
        }
        // 再判定金星
        if (roll < iridiumChance + goldChance) {
            return Quality.GOLD;
        }
        // 再判定银星
        if (roll < iridiumChance + goldChance + silverChance) {
            return Quality.SILVER;
        }

        return Quality.NORMAL;
    }

    /**
     * 根据动物和世界获取品质，方便直接调用
     */
    public static Quality getQualityForAnimal(AnimalEntity animal, ServerWorld world) {
        Random random = world.random;
        int friendship = AnimalFriendshipManager.get(world).getFriendship(animal.getUuid());
        long currentDay = world.getTimeOfDay() / 24000L;
        float dailyLuck = LuckManager.get(world).getDailyLuck(currentDay);
        return determineQuality(friendship, dailyLuck, random);
    }

    /**
     * 获取带品质后缀的物品（如果源物品不在列表中则返回源物品）
     */
    public static Identifier getQualityItemId(String baseItemId, Quality quality) {
        if (quality == Quality.NORMAL) {
            return Identifier.of(StardewValley.MOD_ID, baseItemId);
        }
        return Identifier.of(StardewValley.MOD_ID, baseItemId + quality.suffix);
    }
}
