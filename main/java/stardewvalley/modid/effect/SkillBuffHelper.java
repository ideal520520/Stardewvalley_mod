package stardewvalley.modid.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import stardewvalley.modid.season.FarmingLevelManager;
import stardewvalley.modid.season.ForagingLevelManager;
import stardewvalley.modid.season.FishingLevelManager;
import stardewvalley.modid.season.MiningLevelManager;

import java.util.UUID;

public class SkillBuffHelper {

    /** 获取有效耕种等级（基础 + 食物加成） */
    public static int getEffectiveFarmingLevel(ServerWorld world, UUID uuid, ServerPlayerEntity player) {
        int base = FarmingLevelManager.get(world).getLevel(uuid);
        int bonus = getBuffAmplifier(player, ModStatusEffects.FARMING_BUFF) + 1;
        return Math.max(0, base + bonus);
    }

    /** 获取有效采集等级（基础 + 食物加成） */
    public static int getEffectiveForagingLevel(ServerWorld world, UUID uuid, ServerPlayerEntity player) {
        int base = ForagingLevelManager.get(world).getLevel(uuid);
        int bonus = getBuffAmplifier(player, ModStatusEffects.FORAGING_BUFF) + 1;
        return Math.max(0, base + bonus);
    }

    /** 获取有效钓鱼等级（基础 + 食物加成）（服务端用） */
    public static int getEffectiveFishingLevel(ServerWorld world, UUID uuid, ServerPlayerEntity player) {
        int base = FishingLevelManager.get(world).getLevel(uuid);
        int bonus = getBuffAmplifier(player, ModStatusEffects.FISHING_BUFF) + 1;
        return Math.max(0, base + bonus);
    }

    /** 获取有效钓鱼等级（客户端用，从状态效果读取） */
    public static int getClientFishingBuffLevel(net.minecraft.client.network.ClientPlayerEntity player) {
        StatusEffectInstance effect = player.getStatusEffect(ModStatusEffects.FISHING_BUFF);
        if (effect != null) {
            return effect.getAmplifier() + 1;
        }
        return 0;
    }

    /** 获取有效采矿等级（基础 + 食物加成） */
    public static int getEffectiveMiningLevel(ServerWorld world, UUID uuid, ServerPlayerEntity player) {
        int base = MiningLevelManager.get(world).getLevel(uuid);
        int bonus = getBuffAmplifier(player, ModStatusEffects.MINING_BUFF) + 1;
        return Math.max(0, base + bonus);
    }

    /** 获取状态效果的放大器（0-based），没有效果则返回 -1 */
    private static int getBuffAmplifier(ServerPlayerEntity player, RegistryEntry<StatusEffect> effect) {
        if (player == null) return -1;
        StatusEffectInstance instance = player.getStatusEffect(effect);
        if (instance == null) return -1;
        return instance.getAmplifier();
    }
}
