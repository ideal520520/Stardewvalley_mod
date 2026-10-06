package stardewvalley.modid.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.equipment.ClientEquipmentData;
import stardewvalley.modid.equipment.ClientTrashCanData;
import stardewvalley.modid.equipment.EquipmentInventory;
import stardewvalley.modid.gui.ModPayloads;
import stardewvalley.modid.gui.PlayerScreenHandlerAccessor;
import stardewvalley.modid.gui.TrashSlot;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    private static final Identifier SLOT_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/zhuangbei.png");
    private static final Identifier[] TRASH_TEXTURES = {
        Identifier.of(StardewValley.MOD_ID, "textures/gui/Trash_Can_Copper.png"),
        Identifier.of(StardewValley.MOD_ID, "textures/gui/Trash_Can_Steel.png"),
        Identifier.of(StardewValley.MOD_ID, "textures/gui/Trash_Can_Gold.png"),
        Identifier.of(StardewValley.MOD_ID, "textures/gui/Trash_Can_Iridium.png"),
    };
    private static final Identifier TAB_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Inventory_Tab.png");
    private static final int TAB_SLOT_X = 137; // 垃圾桶左侧
    private static final int TAB_SLOT_Y = 62;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // 打开背包时请求垃圾桶等级，并刷新垃圾桶槽位开关状态
        ClientPlayNetworking.send(new ModPayloads.TrashCanLevelRequestC2SPayload());
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.playerScreenHandler instanceof PlayerScreenHandlerAccessor acc) {
            acc.sv$getTrashSlot().setEnabled(ClientTrashCanData.getTrashLevel() > 0);
        }
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void onDrawBg(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        HandledScreenAccessor origin = (HandledScreenAccessor) (Object) this;
        int guiLeft = origin.sv$getX();
        int guiTop = origin.sv$getY();

        // 装备栏
        for (int i = 0; i < EquipmentInventory.SLOT_COUNT; i++) {
            int sx = guiLeft + 77;
            int sy = guiTop + 7 + i * 18;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, sx - 1, sy, 0.0f, 0.0f, 17, 17, 17, 17);
            var stack = ClientEquipmentData.getEquipment().getSlot(i);
            if (!stack.isEmpty()) {
                context.drawItem(stack, sx, sy);
                context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, stack, sx, sy);
            }
        }

        // 扩展背包按钮（在垃圾桶左侧）
        int tx = guiLeft + TAB_SLOT_X;
        int ty = guiTop + TAB_SLOT_Y;

        // 槽位框
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, tx - 1, ty - 1, 0.0f, 0.0f, 18, 18, 18, 18);
        // 扩展背包图标（缩放为16x16显示）
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TAB_TEXTURE, tx, ty, 0.0f, 0.0f, 16, 16, 16, 16);

        // 悬浮提示
        if (mouseX >= tx && mouseX < tx + 18 && mouseY >= ty && mouseY < ty + 18) {
            context.fill(tx - 1, ty - 1, tx + 17, ty + 17, 0x44FFFFFF);
        }

        // 垃圾桶（外框 + 半透明材质；物品本身由真实槽位渲染）
        int trashLevel = ClientTrashCanData.getTrashLevel();
        if (trashLevel > 0 && trashLevel <= TRASH_TEXTURES.length) {
            int texX = guiLeft + TrashSlot.TEX_X;
            int texY = guiTop + TrashSlot.TEX_Y;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, texX - 1, texY - 1, 0.0f, 0.0f, 18, 18, 18, 18);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, TRASH_TEXTURES[trashLevel - 1], texX, texY, 0.0f, 0.0f, 16, 16, 16, 16);
            context.fill(texX, texY, texX + 16, texY + 16, 0x80FFFFFF);
        }
    }
}
