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

public class MarlonTradeManager extends PersistentState {

    private static final String NAME = "stardewvalley_marlon_trade";
    private static final Codec<Set<UUID>> UUID_SET_CODEC = Uuids.CODEC.listOf().xmap(
        HashSet::new,
        List::copyOf
    );
    private static final Codec<MarlonTradeManager> CODEC = UUID_SET_CODEC.fieldOf("traded")
        .xmap(MarlonTradeManager::new, m -> m.tradedPlayers)
        .codec();

    private final Set<UUID> tradedPlayers;

    public static final PersistentStateType<MarlonTradeManager> TYPE = new PersistentStateType<>(
        NAME,
        MarlonTradeManager::new,
        SafeCodec.wrap(CODEC, MarlonTradeManager::new),
        DataFixTypes.LEVEL
    );

    public MarlonTradeManager() {
        this(new HashSet<>());
    }

    public MarlonTradeManager(Set<UUID> tradedPlayers) {
        this.tradedPlayers = tradedPlayers;
    }

    public static MarlonTradeManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public boolean hasTraded(UUID playerUuid) {
        return tradedPlayers.contains(playerUuid);
    }

    public void markTraded(UUID playerUuid) {
        tradedPlayers.add(playerUuid);
        setDirty(true);
    }
}
