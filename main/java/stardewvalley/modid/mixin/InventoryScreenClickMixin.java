package stardewvalley.modid.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.equipment.ClientEquipmentData;
import stardewvalley.modid.equipment.EquipmentActionType;
import stardewvalley.modid.equipment.EquipmentInventory;
import stardewvalley.modid.gui.ModPayloads;

@Mixin(HandledScreen.class)
public abstract class InventoryScreenClickMixin {

    private static final int SLOT_SIZE = 18;
    private static final int SLOT_START_X = 77;
    private static final int SLOT_START_Y = 7;
    private static final int SLOT_SPACING = 18;
    private static final int TAB_SLOT_X = 137;
    private static final int TAB_SLOT_Y = 62;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void onMouseClicked(Click click, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof InventoryScreen)) return;

        HandledScreenAccessor origin = (HandledScreenAccessor) (Object) this;
        int guiLeft = origin.sv$getX();
        int guiTop = origin.sv$getY();

        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button == 0) {
            // 扩展背包按钮（在垃圾桶左侧）
            int tabx = guiLeft + TAB_SLOT_X;
            int taby = guiTop + TAB_SLOT_Y;
            if (mouseX >= tabx && mouseX < tabx + SLOT_SIZE && mouseY >= taby && mouseY < taby + SLOT_SIZE) {
                // 发送打开背包的请求到服务端（ScreenHandler 自动处理）
                ClientPlayNetworking.send(new ModPayloads.BackpackOpenC2SPayload());
                cir.setReturnValue(true);
                return;
            }

            // 装备栏点击
            for (int i = 0; i < EquipmentInventory.SLOT_COUNT; i++) {
                int sx = guiLeft + SLOT_START_X;
                int sy = guiTop + SLOT_START_Y + i * SLOT_SPACING;
                if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                    handleSlotClick(i);
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Unique
    private void handleSlotClick(int slotIndex) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemStack cursor = client.player.currentScreenHandler.getCursorStack();
        ItemStack slotStack = ClientEquipmentData.getEquipment().getSlot(slotIndex);

        if (cursor.isEmpty()) {
            if (!slotStack.isEmpty()) {
                client.player.currentScreenHandler.setCursorStack(slotStack.copy());
                ClientEquipmentData.getEquipment().setSlot(slotIndex, ItemStack.EMPTY);
                ClientPlayNetworking.send(new ModPayloads.EquipmentActionC2SPayload(
                    EquipmentActionType.TAKE.ordinal(), slotIndex, "", 0));
            }
        } else {
            if (slotStack.isEmpty()) {
                if (EquipmentInventory.canPlaceInSlot(slotIndex, cursor)) {
                    ClientEquipmentData.getEquipment().setSlot(slotIndex, cursor.copy());
                    client.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                    ClientPlayNetworking.send(new ModPayloads.EquipmentActionC2SPayload(
                        EquipmentActionType.PUT.ordinal(), slotIndex,
                        Registries.ITEM.getId(cursor.getItem()).toString(), cursor.getCount()));
                }
            } else {
                if (EquipmentInventory.canPlaceInSlot(slotIndex, cursor)) {
                    ClientEquipmentData.getEquipment().setSlot(slotIndex, cursor.copy());
                    client.player.currentScreenHandler.setCursorStack(slotStack.copy());
                    ClientPlayNetworking.send(new ModPayloads.EquipmentActionC2SPayload(
                        EquipmentActionType.SWAP.ordinal(), slotIndex,
                        Registries.ITEM.getId(cursor.getItem()).toString(), cursor.getCount()));
                }
            }
        }
    }
}
