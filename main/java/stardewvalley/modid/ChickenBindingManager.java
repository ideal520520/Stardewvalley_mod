package stardewvalley.modid;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.skill.SkillEffectHelper;
import stardewvalley.modid.util.SafeCodec;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChickenBindingManager extends PersistentState {

    private static final String NAME = "stardewvalley_chicken_bindings";

    private final Set<UUID> coopmasterFedChickens = new HashSet<>();

    private static final Codec<Set<UUID>> SET_CODEC = Uuids.CODEC.listOf().xmap(
        HashSet::new, java.util.ArrayList::new
    );

    private static final Codec<ChickenBindingManager> CODEC = SET_CODEC.xmap(
        data -> {
            ChickenBindingManager mgr = new ChickenBindingManager();
            mgr.coopmasterFedChickens.addAll(data);
            return mgr;
        },
        mgr -> new HashSet<>(mgr.coopmasterFedChickens)
    );

    private static final PersistentStateType<ChickenBindingManager> TYPE = new PersistentStateType<>(
        NAME,
        ChickenBindingManager::new,
        SafeCodec.wrap(CODEC, ChickenBindingManager::new),
        DataFixTypes.LEVEL
    );

    public static ChickenBindingManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    /** 如果喂食者有鸡舍大师天赋，标记这只鸡为受鸡舍大师喂食 */
    public void bind(ServerWorld world, UUID chickenUuid, UUID playerUuid) {
        if (SkillEffectHelper.hasCoopmaster(world, playerUuid)) {
            if (coopmasterFedChickens.add(chickenUuid)) {
                setDirty(true);
            }
        }
    }

    /** 这只鸡是否被鸡舍大师喂食过（一直有效） */
    public boolean isCoopmasterFed(UUID chickenUuid) {
        return coopmasterFedChickens.contains(chickenUuid);
    }

    /** 鸡死亡时移除绑定 */
    public void unbind(UUID chickenUuid) {
        if (coopmasterFedChickens.remove(chickenUuid)) {
            setDirty(true);
        }
    }
}
