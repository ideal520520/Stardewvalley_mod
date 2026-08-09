package stardewvalley.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WelcomeMessageState extends PersistentState {

    private static final String NAME = "stardewvalley_welcome_msg";

    private Set<UUID> disabledPlayers = new HashSet<>();
    private Set<UUID> receivedGuideBook = new HashSet<>();

    public static final Codec<WelcomeMessageState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(Codec.STRING).fieldOf("disabled_players").forGetter(s ->
                s.disabledPlayers.stream().map(UUID::toString).toList()
            ),
            Codec.list(Codec.STRING).fieldOf("received_guide_book").forGetter(s ->
                s.receivedGuideBook.stream().map(UUID::toString).toList()
            )
        ).apply(instance, (uuidStrings, guideUuidStrings) -> {
            WelcomeMessageState state = new WelcomeMessageState();
            state.disabledPlayers = new HashSet<>();
            for (String s : uuidStrings) {
                try {
                    state.disabledPlayers.add(UUID.fromString(s));
                } catch (IllegalArgumentException ignored) {
                }
            }
            state.receivedGuideBook = new HashSet<>();
            for (String s : guideUuidStrings) {
                try {
                    state.receivedGuideBook.add(UUID.fromString(s));
                } catch (IllegalArgumentException ignored) {
                }
            }
            return state;
        })
    );

    public static final PersistentStateType<WelcomeMessageState> TYPE = new PersistentStateType<>(
        NAME,
        WelcomeMessageState::new,
        SafeCodec.wrap(CODEC, WelcomeMessageState::new),
        DataFixTypes.LEVEL
    );

    public static WelcomeMessageState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public boolean isDisabled(UUID playerUuid) {
        return disabledPlayers.contains(playerUuid);
    }

    public void setDisabled(UUID playerUuid, boolean disabled) {
        if (disabled) {
            disabledPlayers.add(playerUuid);
        } else {
            disabledPlayers.remove(playerUuid);
        }
        markDirty();
    }

    public boolean hasReceivedGuideBook(UUID playerUuid) {
        return receivedGuideBook.contains(playerUuid);
    }

    public void markGuideBookReceived(UUID playerUuid) {
        receivedGuideBook.add(playerUuid);
        markDirty();
    }
}
