package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import stardewvalley.modid.StardewValley;

public class ModScreenHandlers {
    public static final ExtendedScreenHandlerType<BackpackScreenHandler, BackpackOpenData> BACKPACK =
            new ExtendedScreenHandlerType<>(
                    (syncId, inventory, data) -> new BackpackScreenHandler(syncId, inventory, data.level()),
                    BackpackOpenData.PACKET_CODEC
            );

    public static final ExtendedScreenHandlerType<SlingshotScreenHandler, SlingshotOpenData> SLINGSHOT =
            new ExtendedScreenHandlerType<>(
                    (syncId, inventory, data) -> new SlingshotScreenHandler(syncId, inventory),
                    SlingshotOpenData.PACKET_CODEC
            );

    public static final ExtendedScreenHandlerType<RodScreenHandler, RodOpenData> ROD =
            new ExtendedScreenHandlerType<>(
                    (syncId, inventory, data) -> new RodScreenHandler(syncId, inventory, data),
                    RodOpenData.PACKET_CODEC
            );

    public static final ExtendedScreenHandlerType<CrabPotBaitScreenHandler, CrabPotOpenData> CRAB_POT =
            new ExtendedScreenHandlerType<>(
                    (syncId, inventory, data) -> new CrabPotBaitScreenHandler(syncId, inventory, data.pos()),
                    CrabPotOpenData.PACKET_CODEC
            );

    public static void registerAll() {
        Registry.register(Registries.SCREEN_HANDLER,
                Identifier.of(StardewValley.MOD_ID, "backpack"),
                BACKPACK);
        Registry.register(Registries.SCREEN_HANDLER,
                Identifier.of(StardewValley.MOD_ID, "slingshot"),
                SLINGSHOT);
        Registry.register(Registries.SCREEN_HANDLER,
                Identifier.of(StardewValley.MOD_ID, "rod"),
                ROD);
        Registry.register(Registries.SCREEN_HANDLER,
                Identifier.of(StardewValley.MOD_ID, "crab_pot"),
                CRAB_POT);
    }

    /**
     * 打开背包时传递到客户端的数据（背包等级）
     */
    public record BackpackOpenData(int level) {
        public static final PacketCodec<PacketByteBuf, BackpackOpenData> PACKET_CODEC = PacketCodec.of(
                (value, buf) -> buf.writeInt(value.level),
                buf -> new BackpackOpenData(buf.readInt())
        );
    }

    /**
     * 打开弹弓时传递到客户端的数据（无额外信息）
     */
    public record SlingshotOpenData() {
        public static final PacketCodec<PacketByteBuf, SlingshotOpenData> PACKET_CODEC = PacketCodec.of(
                (value, buf) -> {},
                buf -> new SlingshotOpenData()
        );
    }

    /**
     * 打开钓竿界面时传递的数据
     */
    public record RodOpenData(int tackleSlotCount) {
        public static final PacketCodec<PacketByteBuf, RodOpenData> PACKET_CODEC = PacketCodec.of(
                (value, buf) -> buf.writeInt(value.tackleSlotCount),
                buf -> new RodOpenData(buf.readInt())
        );
    }

    /**
     * 打开蟹笼诱饵界面时传递的数据（蟹笼的 BlockPos）
     */
    public record CrabPotOpenData(BlockPos pos) {
        public static final PacketCodec<PacketByteBuf, CrabPotOpenData> PACKET_CODEC = PacketCodec.of(
                (value, buf) -> buf.writeBlockPos(value.pos),
                buf -> new CrabPotOpenData(buf.readBlockPos())
        );
    }
}
