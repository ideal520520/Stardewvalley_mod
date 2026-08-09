package stardewvalley.modid.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

public class DeluxeWormBinBlockEntity extends BlockEntity {
    private boolean hasOutput = false;

    public DeluxeWormBinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DELUXE_WORM_BIN, pos, state);
    }

    public boolean hasOutput() { return hasOutput; }

    public void setHasOutput(boolean has) {
        this.hasOutput = has;
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public void writeDataWithoutId(WriteView buf) {
        buf.putBoolean("hasOutput", hasOutput);
    }

    @Override
    public void readData(ReadView buf) {
        this.hasOutput = buf.getBoolean("hasOutput", false);
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
