package stardewvalley.modid.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.equipment.ClientEquipmentData;
import stardewvalley.modid.equipment.ClientTrashCanData;
import stardewvalley.modid.mixin.RecipeBookScreenAccessor;
import stardewvalley.modid.equipment.EquipmentActionType;
import stardewvalley.modid.equipment.EquipmentInventory;
import stardewvalley.modid.gui.ModPayloads;

@Mixin(HandledScreen.class)
public abstract class InventoryScreenClickMixin {

    private static final int SLOT_SIZE = 18;
    private static final int SLOT_START_X = 77;
    private static final int SLOT_START_Y = 7;
    private static final int SLOT_SPACING = 18;
    private static final int TRASH_SLOT_X = 155;
    private static final int TRASH_SLOT_Y = 62;
    private static final int TAB_SLOT_X = 137;
    private static final int TAB_SLOT_Y = 62;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false)
    private void onMouseClicked(Click click, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof InventoryScreen)) return;

        var w = MinecraftClient.getInstance().getWindow();
        int guiLeft = (w.getScaledWidth() - 176) / 2;
        int guiTop = (w.getScaledHeight() - 166) / 2;

        int recipeBookOffset = 0;
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeBookScreen) {
            var rbw = ((RecipeBookScreenAccessor) MinecraftClient.getInstance().currentScreen).getRecipeBook();
            if (rbw.isOpen()) recipeBookOffset = 78;
        }

        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button == 0) {
            // 扩展背包按钮（在垃圾桶左侧）
            int tabx = guiLeft + TAB_SLOT_X + recipeBookOffset;
            int taby = guiTop + TAB_SLOT_Y;
            if (mouseX >= tabx && mouseX < tabx + SLOT_SIZE && mouseY >= taby && mouseY < taby + SLOT_SIZE) {
                // 发送打开背包的请求到服务端（ScreenHandler 自动处理）
                ClientPlayNetworking.send(new ModPayloads.BackpackOpenC2SPayload());
                cir.setReturnValue(true);
                return;
            }

            // 装备栏点击
            for (int i = 0; i < EquipmentInventory.SLOT_COUNT; i++) {
                int sx = guiLeft + SLOT_START_X + recipeBookOffset;
                int sy = guiTop + SLOT_START_Y + i * SLOT_SPACING;
                if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                    handleSlotClick(i);
                    cir.setReturnValue(true);
                    return;
                }
            }
            // 垃圾桶点击（由服务端管理状态，操作后通过TrashCanDataSyncS2CPayload同步）
            if (ClientTrashCanData.getTrashLevel() > 0) {
                int tx = guiLeft + TRASH_SLOT_X + recipeBookOffset;
                int ty = guiTop + TRASH_SLOT_Y;
                if (mouseX >= tx && mouseX < tx + SLOT_SIZE && mouseY >= ty && mouseY < ty + SLOT_SIZE) {
                    handleTrashClick();
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Unique
    private void handleTrashClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemStack cursor = client.player.currentScreenHandler.getCursorStack();

        if (cursor.isEmpty()) {
            // 空光标 → 取出暂存物品（乐观更新：立即清空显示）
            ClientTrashCanData.clear();
            ClientPlayNetworking.send(new ModPayloads.TrashCanActionC2SPayload(1, "", 0, 0, 0));
        } else {
            // 有物品 → 放入（乐观更新：立即显示物品，服务端稍后同步修正数量/替换逻辑）
            String itemId = Registries.ITEM.getId(cursor.getItem()).toString();
            int count = cursor.getCount();
            ClientTrashCanData.setPendingItem(cursor.copy());
            client.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            ClientPlayNetworking.send(new ModPayloads.TrashCanActionC2SPayload(0, itemId, count, 0, 0));
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
