package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DwarvishSafetyManager extends PersistentState {

    private static final String NAME = "stardewvalley_safety_manual";
    private static final Codec<Set<UUID>> UUID_SET_CODEC = Uuids.CODEC.listOf().xmap(
        HashSet::new,
        List::copyOf
    );
    private static final Codec<DwarvishSafetyManager> CODEC = UUID_SET_CODEC.fieldOf("activated")
        .xmap(DwarvishSafetyManager::new, m -> m.activatedPlayers)
        .codec();

    private final Set<UUID> activatedPlayers;

    public static final PersistentStateType<DwarvishSafetyManager> TYPE = new PersistentStateType<>(
        NAME,
        DwarvishSafetyManager::new,
        SafeCodec.wrap(CODEC, DwarvishSafetyManager::new),
        DataFixTypes.LEVEL
    );

    public DwarvishSafetyManager() {
        this(new HashSet<>());
    }

    public DwarvishSafetyManager(Set<UUID> activatedPlayers) {
        this.activatedPlayers = activatedPlayers;
    }

    public static DwarvishSafetyManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public boolean isActivated(UUID playerUuid) {
        return activatedPlayers.contains(playerUuid);
    }

    public void activate(UUID playerUuid) {
        activatedPlayers.add(playerUuid);
        setDirty(true);
    }
}
