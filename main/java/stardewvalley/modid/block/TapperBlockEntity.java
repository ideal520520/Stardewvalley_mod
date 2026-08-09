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

public class TapperBlockEntity extends BlockEntity {
    private String outputItemId = "";

    public TapperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TAPPER, pos, state);
    }

    public void setOutputItemId(String outputItemId) {
        this.outputItemId = outputItemId != null ? outputItemId : "";
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public String getOutputItemId() { return outputItemId; }

    @Override
    public void writeDataWithoutId(WriteView buf) {
        buf.putString("outputItemId", outputItemId);
    }

    @Override
    public void readData(ReadView buf) {
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
