package stardewvalley.modid.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.util.SafeCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillDataManager extends PersistentState {

    private static final String NAME = "stardewvalley_skills";

    private final Map<UUID, PlayerSkillData> playerData = new HashMap<>();

    public static class PlayerSkillData {
        // 耕种
        public String farmingLevel5;  // ranchar or tiller
        public String farmingLevel10; // agriculturist, artisan, coopmaster, shepherd
        // 采集
        public String foragingLevel5;  // forester or gatherer
        public String foragingLevel10; // lumberjack, botanist, trapper, tracker
        // 钓鱼
        public String fishingLevel5;  // fisher or tapper
        public String fishingLevel10; // angler, pirate, mariner, luremaster
        // 挖矿
        public String miningLevel5;  // miner or geologist
        public String miningLevel10; // blacksmith, excavator, prospector, gemologist
        // 战斗
        public String combatLevel5;  // fighter or scout
        public String combatLevel10; // brute, defender, acrobat, desperado

        public PlayerSkillData() {
            this.farmingLevel5 = null;
            this.farmingLevel10 = null;
            this.foragingLevel5 = null;
            this.foragingLevel10 = null;
            this.fishingLevel5 = null;
            this.fishingLevel10 = null;
            this.miningLevel5 = null;
            this.miningLevel10 = null;
            this.combatLevel5 = null;
            this.combatLevel10 = null;
        }

        public String getLevel5(SkillRegistry.Category category) {
            return switch (category) {
                case FARMING -> farmingLevel5;
                case FORAGING -> foragingLevel5;
                case FISHING -> fishingLevel5;
                case MINING -> miningLevel5;
                case COMBAT -> combatLevel5;
            };
        }

        public void setLevel5(SkillRegistry.Category category, String skillId) {
            switch (category) {
                case FARMING -> farmingLevel5 = skillId;
                case FORAGING -> foragingLevel5 = skillId;
                case FISHING -> fishingLevel5 = skillId;
                case MINING -> miningLevel5 = skillId;
                case COMBAT -> combatLevel5 = skillId;
            }
        }

        public String getLevel10(SkillRegistry.Category category) {
            return switch (category) {
                case FARMING -> farmingLevel10;
                case FORAGING -> foragingLevel10;
                case FISHING -> fishingLevel10;
                case MINING -> miningLevel10;
                case COMBAT -> combatLevel10;
            };
        }

        public void setLevel10(SkillRegistry.Category category, String skillId) {
            switch (category) {
                case FARMING -> farmingLevel10 = skillId;
                case FORAGING -> foragingLevel10 = skillId;
                case FISHING -> fishingLevel10 = skillId;
                case MINING -> miningLevel10 = skillId;
                case COMBAT -> combatLevel10 = skillId;
            }
        }
    }

    public static final Codec<PlayerSkillData> SKILL_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("farmingLevel5", "").forGetter(d -> d.farmingLevel5 == null ? "" : d.farmingLevel5),
            Codec.STRING.optionalFieldOf("farmingLevel10", "").forGetter(d -> d.farmingLevel10 == null ? "" : d.farmingLevel10),
            Codec.STRING.optionalFieldOf("foragingLevel5", "").forGetter(d -> d.foragingLevel5 == null ? "" : d.foragingLevel5),
            Codec.STRING.optionalFieldOf("foragingLevel10", "").forGetter(d -> d.foragingLevel10 == null ? "" : d.foragingLevel10),
            Codec.STRING.optionalFieldOf("fishingLevel5", "").forGetter(d -> d.fishingLevel5 == null ? "" : d.fishingLevel5),
            Codec.STRING.optionalFieldOf("fishingLevel10", "").forGetter(d -> d.fishingLevel10 == null ? "" : d.fishingLevel10),
            Codec.STRING.optionalFieldOf("miningLevel5", "").forGetter(d -> d.miningLevel5 == null ? "" : d.miningLevel5),
            Codec.STRING.optionalFieldOf("miningLevel10", "").forGetter(d -> d.miningLevel10 == null ? "" : d.miningLevel10),
            Codec.STRING.optionalFieldOf("combatLevel5", "").forGetter(d -> d.combatLevel5 == null ? "" : d.combatLevel5),
            Codec.STRING.optionalFieldOf("combatLevel10", "").forGetter(d -> d.combatLevel10 == null ? "" : d.combatLevel10)
        ).apply(instance, (f5, f10, fo5, fo10, fi5, fi10, m5, m10, c5, c10) -> {
            PlayerSkillData data = new PlayerSkillData();
            if (!f5.isEmpty()) data.farmingLevel5 = f5;
            if (!f10.isEmpty()) data.farmingLevel10 = f10;
            if (!fo5.isEmpty()) data.foragingLevel5 = fo5;
            if (!fo10.isEmpty()) data.foragingLevel10 = fo10;
            if (!fi5.isEmpty()) data.fishingLevel5 = fi5;
            if (!fi10.isEmpty()) data.fishingLevel10 = fi10;
            if (!m5.isEmpty()) data.miningLevel5 = m5;
            if (!m10.isEmpty()) data.miningLevel10 = m10;
            if (!c5.isEmpty()) data.combatLevel5 = c5;
            if (!c10.isEmpty()) data.combatLevel10 = c10;
            return data;
        })
    );

    public static final Codec<SkillDataManager> CODEC = Codec.unboundedMap(
        Uuids.CODEC,
        SKILL_DATA_CODEC
    ).xmap(
        map -> {
            SkillDataManager manager = new SkillDataManager();
            manager.playerData.putAll(map);
            return manager;
        },
        manager -> manager.playerData
    );

    public static final PersistentStateType<SkillDataManager> TYPE = new PersistentStateType<>(
        NAME,
        SkillDataManager::new,
        SafeCodec.wrap(CODEC, SkillDataManager::new),
        DataFixTypes.LEVEL
    );

    public static SkillDataManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public PlayerSkillData getOrCreate(UUID playerUuid) {
        return playerData.computeIfAbsent(playerUuid, k -> new PlayerSkillData());
    }

    public String getLevel5Skill(UUID playerUuid, SkillRegistry.Category category) {
        return getOrCreate(playerUuid).getLevel5(category);
    }

    public String getLevel10Skill(UUID playerUuid, SkillRegistry.Category category) {
        return getOrCreate(playerUuid).getLevel10(category);
    }

    public void setLevel5Skill(UUID playerUuid, SkillRegistry.Category category, String skillId) {
        PlayerSkillData data = getOrCreate(playerUuid);
        data.setLevel5(category, skillId);
        setDirty(true);
    }

    public void setLevel10Skill(UUID playerUuid, SkillRegistry.Category category, String skillId) {
        PlayerSkillData data = getOrCreate(playerUuid);
        data.setLevel10(category, skillId);
        setDirty(true);
    }

    public void clearLevel10(UUID playerUuid, SkillRegistry.Category category) {
        PlayerSkillData data = getOrCreate(playerUuid);
        data.setLevel10(category, null);
        setDirty(true);
    }
}
