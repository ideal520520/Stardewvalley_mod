package stardewvalley.modid.equipment;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

import java.util.HashMap;
import java.util.Map;

/** 宝石戒指属性加成辅助 */
public class GemRingHelper {

    /** 获取玩家装备的宝石戒指信息 */
    public static Map<String, Integer> getGemRings(ServerPlayerEntity player) {
        Map<String, Integer> rings = new HashMap<>();
        EquipmentInventory inv = EquipmentStateManager.get((ServerWorld) player.getEntityWorld()).getPlayerData(player.getUuid());
        for (int i = 0; i < 2; i++) {
            ItemStack s = inv.getSlot(i);
            if (s.isEmpty()) continue;
            Identifier id = Registries.ITEM.getId(s.getItem());
            if (!StardewValley.MOD_ID.equals(id.getNamespace())) continue;
            String path = id.getPath();
            // 宝石戒指
            if ("amethyst_ring".equals(path) || "aquamarine_ring".equals(path) ||
                "jade_ring".equals(path) || "emerald_ring".equals(path) ||
                "ruby_ring".equals(path) || "topaz_ring".equals(path)) {
                rings.put(path, rings.getOrDefault(path, 0) + 1);
            }
            // 铱环 = 红宝石效果
            if ("iridium_band".equals(path)) {
                rings.put("ruby_ring", rings.getOrDefault("ruby_ring", 0) + 1);
            }
        }
        return rings;
    }

    /** 总计伤害倍率 */
    public static float getDamageMultiplier(Map<String, Integer> rings) {
        int rubyCount = rings.getOrDefault("ruby_ring", 0);
        return 1.0f + rubyCount * 0.10f;
    }

    /** 总计暴击率加成（加法） */
    public static float getCritChanceBonus(Map<String, Integer> rings) {
        int aquaCount = rings.getOrDefault("aquamarine_ring", 0);
        return aquaCount * 0.10f;
    }

    /** 总计暴击力量加成（原始值） */
    public static int getCritPowerBonus(Map<String, Integer> rings) {
        int jadeCount = rings.getOrDefault("jade_ring", 0);
        return jadeCount * 5; // critPower/50 = 1% per point, 5点=10%
    }

    /** 总计攻击速度倍率 */
    public static float getSpeedMultiplier(Map<String, Integer> rings) {
        int emeraldCount = rings.getOrDefault("emerald_ring", 0);
        return 1.0f + emeraldCount * 0.10f;
    }

    /** 总计击退加成（权重值） */
    public static int getWeightBonus(Map<String, Integer> rings) {
        int amethystCount = rings.getOrDefault("amethyst_ring", 0);
        return amethystCount * 1; // 权重每级+1, 原版每1击退距离增加约10%
    }
}
