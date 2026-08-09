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

public class ArtisanEquipmentBlockEntity extends BlockEntity {
    private String inputItemId = "";
    private String outputItemId = "";

    public ArtisanEquipmentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARTISAN_EQUIPMENT, pos, state);
    }

    public void setItems(String inputItemId, String outputItemId) {
        this.inputItemId = inputItemId != null ? inputItemId : "";
        this.outputItemId = outputItemId != null ? outputItemId : "";
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public String getInputItemId() { return inputItemId; }
    public String getOutputItemId() { return outputItemId; }

    @Override
    public void writeDataWithoutId(WriteView buf) {
        buf.putString("inputItemId", inputItemId);
        buf.putString("outputItemId", outputItemId);
    }

    @Override
    public void readData(ReadView buf) {
        this.inputItemId = buf.getString("inputItemId", "");
        this.outputItemId = buf.getString("outputItemId", "");
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
