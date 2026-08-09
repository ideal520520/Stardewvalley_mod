package stardewvalley.modid.fishing;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record S2CFishBitePayload() implements CustomPayload {
    public static final CustomPayload.Id<S2CFishBitePayload> ID = new CustomPayload.Id<>(
        Identifier.of(FishingSounds.MODID, "fish_bite"));

    public static final PacketCodec<PacketByteBuf, S2CFishBitePayload> CODEC = PacketCodec.of(
        (value, buf) -> {},
        buf -> new S2CFishBitePayload()
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
