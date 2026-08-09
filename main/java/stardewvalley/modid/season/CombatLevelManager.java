package stardewvalley.modid.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.util.SafeCodec;
import stardewvalley.modid.skill.SkillEffectHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatLevelManager extends PersistentState {

    private static final String NAME = "stardewvalley_combat";

    private final Map<UUID, CombatData> playerData = new HashMap<>();

    public static class CombatData {
        public int level;
        public int xp;
        public int healthBonus;

        public CombatData(int level, int xp, int healthBonus) {
            this.level = level;
            this.xp = xp;
            this.healthBonus = healthBonus;
        }
    }

    public static final Codec<CombatData> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("level").forGetter(d -> d.level),
            Codec.INT.fieldOf("xp").forGetter(d -> d.xp),
            Codec.INT.fieldOf("healthBonus").forGetter(d -> d.healthBonus)
        ).apply(instance, CombatData::new)
    );

    public static final Codec<CombatLevelManager> CODEC = Codec.unboundedMap(
        Uuids.CODEC, DATA_CODEC
    ).xmap(
        map -> {
            CombatLevelManager manager = new CombatLevelManager();
            manager.playerData.putAll(map);
            return manager;
        },
        manager -> manager.playerData
    );

    public static final PersistentStateType<CombatLevelManager> TYPE = new PersistentStateType<>(
        NAME,
        CombatLevelManager::new,
        SafeCodec.wrap(CODEC, CombatLevelManager::new),
        DataFixTypes.LEVEL
    );

    public static CombatLevelManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public int getLevel(UUID playerUuid) {
        return playerData.getOrDefault(playerUuid, new CombatData(0, 0, 0)).level;
    }

    public int getXp(UUID playerUuid) {
        return playerData.getOrDefault(playerUuid, new CombatData(0, 0, 0)).xp;
    }

    public int getHealthBonus(UUID playerUuid) {
        return playerData.getOrDefault(playerUuid, new CombatData(0, 0, 0)).healthBonus;
    }

    private static final int[] XP_THRESHOLDS = {100, 380, 770, 1300, 2150, 3300, 4800, 6900, 10000, 15000};
    private static final int MAX_LEVEL = 10;

    public void addXp(ServerPlayerEntity player, int amount) {
        UUID playerUuid = player.getUuid();
        CombatData data = playerData.computeIfAbsent(playerUuid, k -> new CombatData(0, 0, 0));
        int oldLevel = data.level;

        data.xp += amount;
        data.level = 0;
        for (int lvl = MAX_LEVEL; lvl >= 1; lvl--) {
            if (data.xp >= XP_THRESHOLDS[lvl - 1]) {
                data.level = lvl;
                break;
            }
        }

        // 检查是否有升级，除5级和10级外每级+1生命上限
        if (data.level > oldLevel) {
            for (int lvl = oldLevel + 1; lvl <= data.level; lvl++) {
                if (lvl != 5 && lvl != 10) {
                    data.healthBonus++;
                }
            }
            applyHealthBonus(player, data.healthBonus);
        }

        setDirty(true);
    }

    public void applyHealthBonus(ServerPlayerEntity player) {
		UUID playerUuid = player.getUuid();
		CombatData data = playerData.get(playerUuid);
		int bonus = (data != null) ? data.healthBonus : 0;
		// 战士(+3) / 防御者(+5) 技能生命加成
		if (player.getEntityWorld() instanceof ServerWorld world) {
			bonus += SkillEffectHelper.getBonusMaxHealth(world, playerUuid);
			// 战斗精通：生命上限永远+4
			if (stardewvalley.modid.skill.MasteryManager.hasMasteredSkill(world, playerUuid, stardewvalley.modid.skill.SkillRegistry.Category.COMBAT)) {
				bonus += 4;
			}
		}
		applyHealthBonus(player, bonus);
	}

    private static void applyHealthBonus(ServerPlayerEntity player, int bonus) {
        double baseHealth = 20.0;
        double newMax = baseHealth + bonus;
        var attr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(newMax);
        }
    }
}
