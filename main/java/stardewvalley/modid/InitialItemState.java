package stardewvalley.modid;

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

public class InitialItemState extends PersistentState {

    private static final String NAME = "stardewvalley_initial_items";

    private final Set<UUID> claimedPlayers = new HashSet<>();

    private static final Codec<Set<UUID>> SET_CODEC = Codec.list(Uuids.CODEC).xmap(
        list -> new HashSet<>(list),
        set -> List.copyOf(set)
    );

    public static final Codec<InitialItemState> CODEC = SET_CODEC.xmap(
        set -> {
            InitialItemState state = new InitialItemState();
            state.claimedPlayers.addAll(set);
            return state;
        },
        state -> state.claimedPlayers
    );

    public static final PersistentStateType<InitialItemState> TYPE = new PersistentStateType<>(
        NAME,
        InitialItemState::new,
        SafeCodec.wrap(CODEC, InitialItemState::new),
        DataFixTypes.LEVEL
    );

    public static InitialItemState get(ServerWorld world) {
        ServerWorld overworld = world.getServer().getOverworld();
        return overworld.getPersistentStateManager().getOrCreate(TYPE);
    }

    public boolean hasClaimed(UUID uuid) {
        return claimedPlayers.contains(uuid);
    }

    public void markClaimed(UUID uuid) {
        claimedPlayers.add(uuid);
        setDirty(true);
    }
}
