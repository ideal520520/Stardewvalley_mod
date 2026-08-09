package stardewvalley.modid.fishing;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record C2SCompleteMinigamePayload(boolean success, double accuracy, boolean chestObtained, int outOfBarTicks, float chestChance) implements CustomPayload {
    public static final CustomPayload.Id<C2SCompleteMinigamePayload> ID = new CustomPayload.Id<>(
        Identifier.of(FishingSounds.MODID, "complete_minigame"));

    public static final PacketCodec<PacketByteBuf, C2SCompleteMinigamePayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeBoolean(value.success);
            if (value.success) buf.writeDouble(value.accuracy);
            buf.writeBoolean(value.chestObtained);
            buf.writeVarInt(value.outOfBarTicks);
            buf.writeFloat(value.chestChance);
        },
        buf -> {
            boolean success = buf.readBoolean();
            double accuracy = success ? buf.readDouble() : -1;
            boolean chestObtained = buf.readBoolean();
            int outOfBarTicks = buf.readVarInt();
            float chestChance = buf.readFloat();
            return new C2SCompleteMinigamePayload(success, accuracy, chestObtained, outOfBarTicks, chestChance);
        }
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
