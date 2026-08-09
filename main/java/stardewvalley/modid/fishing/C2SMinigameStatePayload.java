package stardewvalley.modid.fishing;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record C2SMinigameStatePayload(boolean open) implements CustomPayload {
    public static final CustomPayload.Id<C2SMinigameStatePayload> ID = new CustomPayload.Id<>(
        Identifier.of(FishingSounds.MODID, "minigame_state"));

    public static final PacketCodec<PacketByteBuf, C2SMinigameStatePayload> CODEC = PacketCodec.of(
        (value, buf) -> buf.writeBoolean(value.open),
        buf -> new C2SMinigameStatePayload(buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
