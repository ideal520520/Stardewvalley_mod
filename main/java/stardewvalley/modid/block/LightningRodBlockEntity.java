package stardewvalley.modid.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class LightningRodBlockEntity extends BlockEntity {
    private String chargedItemId = "";
    private UUID placerUuid = null;

    public LightningRodBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHTNING_ROD, pos, state);
    }

    public void setChargedItem(String itemId) {
        this.chargedItemId = itemId != null ? itemId : "";
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public String getChargedItemId() { return chargedItemId; }

    public UUID getPlacerUuid() { return placerUuid; }
    public void setPlacerUuid(UUID uuid) {
        this.placerUuid = uuid;
        markDirty();
    }

    @Override
    public void writeDataWithoutId(WriteView buf) {
        buf.putString("chargedItemId", chargedItemId);
        if (placerUuid != null) {
            buf.putString("placerUuid", placerUuid.toString());
        }
    }

    @Override
    public void readData(ReadView buf) {
        this.chargedItemId = buf.getString("chargedItemId", "");
        String uuidStr = buf.getString("placerUuid", "");
        this.placerUuid = uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup lookup) {
        return createNbt(lookup);
    }
}
