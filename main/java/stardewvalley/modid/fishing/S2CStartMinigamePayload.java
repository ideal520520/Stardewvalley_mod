package stardewvalley.modid.fishing;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record S2CStartMinigamePayload(FishBehavior behavior, Identifier fishItemId) implements CustomPayload {
    public static final CustomPayload.Id<S2CStartMinigamePayload> ID = new CustomPayload.Id<>(
        Identifier.of(FishingSounds.MODID, "start_minigame"));

    public static final PacketCodec<PacketByteBuf, S2CStartMinigamePayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            value.behavior.writeToBuffer(buf);
            buf.writeIdentifier(value.fishItemId);
        },
        buf -> new S2CStartMinigamePayload(new FishBehavior(buf), buf.readIdentifier())
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
