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

public class TotemPoleBlockEntity extends BlockEntity {
    private String displayItemId = "";

    public TotemPoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOTEM_POLE, pos, state);
    }

    public void setDisplayItemId(String itemId) {
        this.displayItemId = itemId != null ? itemId : "";
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public String getDisplayItemId() { return displayItemId; }

    @Override
    public void writeDataWithoutId(WriteView buf) {
        buf.putString("displayItemId", displayItemId);
    }

    @Override
    public void readData(ReadView buf) {
        this.displayItemId = buf.getString("displayItemId", "");
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
