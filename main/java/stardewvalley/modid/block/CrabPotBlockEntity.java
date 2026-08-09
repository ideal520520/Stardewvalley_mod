package stardewvalley.modid.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class CrabPotBlockEntity extends BlockEntity {
    private String baitItemId = "";
    private int baitCount = 0;
    private String caughtItemId = "";
    private int caughtQuality = 0;
    private int caughtCount = 1;
    private long lastHarvestDay = -1;
    private UUID placerUuid = null;

    public CrabPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRAB_POT, pos, state);
    }

    public String getBaitItemId() { return baitItemId; }
    public void setBaitItemId(String id) {
        this.baitItemId = id != null ? id : "";
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public int getBaitCount() { return baitCount; }
    public void setBaitCount(int count) {
        this.baitCount = Math.max(0, Math.min(count, 999));
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public String getCaughtItemId() { return caughtItemId; }
    public void setCaughtItemId(String id) {
        this.caughtItemId = id != null ? id : "";
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public int getCaughtQuality() { return caughtQuality; }
    public void setCaughtQuality(int q) {
        this.caughtQuality = q;
        markDirty();
    }

    public int getCaughtCount() { return caughtCount; }
    public void setCaughtCount(int count) {
        this.caughtCount = Math.max(1, count);
        markDirty();
    }

    public long getLastHarvestDay() { return lastHarvestDay; }
    public void setLastHarvestDay(long d) {
        this.lastHarvestDay = d;
        markDirty();
    }

    public UUID getPlacerUuid() { return placerUuid; }
    public void setPlacerUuid(UUID uuid) {
        this.placerUuid = uuid;
        markDirty();
    }

    @Override
    public void writeDataWithoutId(WriteView buf) {
        buf.putString("baitItemId", baitItemId);
        buf.putInt("baitCount", baitCount);
        buf.putString("caughtItemId", caughtItemId);
        buf.putInt("caughtQuality", caughtQuality);
        buf.putInt("caughtCount", caughtCount);
        buf.putLong("lastHarvestDay", lastHarvestDay);
        if (placerUuid != null) {
            buf.putString("placerUuid", placerUuid.toString());
        }
    }

    @Override
    public void readData(ReadView buf) {
        this.baitItemId = buf.getString("baitItemId", "");
        this.baitCount = buf.getInt("baitCount", 0);
        this.caughtItemId = buf.getString("caughtItemId", "");
        this.caughtQuality = buf.getInt("caughtQuality", 0);
        this.caughtCount = buf.getInt("caughtCount", 1);
        this.lastHarvestDay = buf.getLong("lastHarvestDay", -1);
        String uuidStr = buf.getString("placerUuid", "");
        this.placerUuid = uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
        if (world != null && !world.isClient() && world instanceof ServerWorld sw) {
            CrabPotHandler.registerCrabPot(sw, pos);
        }
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
