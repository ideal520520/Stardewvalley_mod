package stardewvalley.modid.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.season.CombatLevelManager;
import stardewvalley.modid.season.FarmingLevelManager;
import stardewvalley.modid.season.FishingLevelManager;
import stardewvalley.modid.season.ForagingLevelManager;
import stardewvalley.modid.season.MiningLevelManager;
import stardewvalley.modid.util.SafeCodec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MasteryManager extends PersistentState {

    private static final String NAME = "stardewvalley_mastery";

    /** 满级10级所需经验（与各等级管理器的 XP_THRESHOLDS[9] 一致） */
    public static final int MAX_LEVEL_XP = 15000;

    /** 精通等级所需累计精通点数：1级1万，2级2.5万，3级4.5万，4级7万，5级10万 */
    private static final int[] MASTERY_LEVEL_THRESHOLDS = {10000, 25000, 45000, 70000, 100000};

    private final Map<UUID, Set<String>> masteredSkills = new HashMap<>();

    private record Data(Map<UUID, Set<String>> masteredSkills) {}

    private static final Codec<Set<String>> STRING_SET_CODEC = Codec.list(Codec.STRING).xmap(
        list -> new HashSet<>(list),
        set -> java.util.List.copyOf(set)
    );

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(Uuids.CODEC, STRING_SET_CODEC)
                .optionalFieldOf("mastered_skills", new HashMap<>())
                .forGetter(Data::masteredSkills)
        ).apply(instance, map -> new Data(new HashMap<>(map)))
    );

    private static final Codec<MasteryManager> CODEC = DATA_CODEC.xmap(
        data -> {
            MasteryManager mgr = new MasteryManager();
            mgr.masteredSkills.putAll(data.masteredSkills());
            return mgr;
        },
        mgr -> new Data(mgr.masteredSkills)
    );

    public static final PersistentStateType<MasteryManager> TYPE = new PersistentStateType<>(
        NAME,
        MasteryManager::new,
        SafeCodec.wrap(CODEC, MasteryManager::new),
        DataFixTypes.LEVEL
    );

    public static MasteryManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    // ===== 精通点数计算 =====

    /** 获取五系技能当前经验 */
    private static int[] getSkillXps(ServerWorld world, UUID uuid) {
        return new int[]{
            FarmingLevelManager.get(world).getXp(uuid),
            ForagingLevelManager.get(world).getXp(uuid),
            FishingLevelManager.get(world).getXp(uuid),
            MiningLevelManager.get(world).getXp(uuid),
            CombatLevelManager.get(world).getXp(uuid)
        };
    }

    /** 总精通点数：五个技能都达到满级10级所需经验后，超出部分累加 */
    public static int getMasteryPoints(ServerWorld world, UUID uuid) {
        int[] xps = getSkillXps(world, uuid);
        for (int xp : xps) {
            if (xp < MAX_LEVEL_XP) return 0;
        }
        int points = 0;
        for (int xp : xps) points += xp - MAX_LEVEL_XP;
        return points;
    }

    /** 精通等级：0~5，由总精通点数决定 */
    public static int getMasteryLevel(ServerWorld world, UUID uuid) {
        int points = getMasteryPoints(world, uuid);
        for (int lvl = MASTERY_LEVEL_THRESHOLDS.length - 1; lvl >= 0; lvl--) {
            if (points >= MASTERY_LEVEL_THRESHOLDS[lvl]) return lvl + 1;
        }
        return 0;
    }

    // ===== 已精通技能 =====

    public boolean isMastered(UUID playerUuid, String categoryName) {
        Set<String> set = masteredSkills.get(playerUuid);
        return set != null && set.contains(categoryName);
    }

    public static boolean hasMasteredSkill(ServerWorld world, UUID playerUuid, SkillRegistry.Category category) {
        return get(world).isMastered(playerUuid, category.name());
    }

    public int getMasteredCount(UUID playerUuid) {
        Set<String> set = masteredSkills.get(playerUuid);
        return set == null ? 0 : set.size();
    }

    /** 尝试精通指定技能，校验精通点数门槛与未重复精通，成功则记录并返回true */
    public boolean tryMaster(ServerWorld world, ServerPlayerEntity player, String categoryName) {
        UUID uuid = player.getUuid();
        int masteryLevel = getMasteryLevel(world, uuid);
        if (masteryLevel <= 0) return false;
        if (getMasteredCount(uuid) >= masteryLevel) return false;
        if (isMastered(uuid, categoryName)) return false;
        masteredSkills.computeIfAbsent(uuid, k -> new HashSet<>()).add(categoryName);
        setDirty(true);
        return true;
    }
}
