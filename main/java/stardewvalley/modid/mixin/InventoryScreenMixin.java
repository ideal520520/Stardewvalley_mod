package stardewvalley.modid.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.equipment.ClientEquipmentData;
import stardewvalley.modid.mixin.RecipeBookScreenAccessor;
import stardewvalley.modid.equipment.ClientTrashCanData;
import stardewvalley.modid.equipment.EquipmentInventory;
import stardewvalley.modid.gui.ModPayloads;

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
    private static final int TRASH_SLOT_X = 155;
    private static final int TRASH_SLOT_Y = 62;
    private static final int TAB_SLOT_X = 137; // 垃圾桶左侧
    private static final int TAB_SLOT_Y = 62;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // 打开背包时请求垃圾桶数据
        if (ClientTrashCanData.getTrashLevel() > 0) {
            ClientPlayNetworking.send(new ModPayloads.TrashCanDataRequestC2SPayload());
        }
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void onDrawBg(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!(((Object) this) instanceof InventoryScreen)) return;
        var w = MinecraftClient.getInstance().getWindow();
        int guiLeft = (w.getScaledWidth() - 176) / 2;
        int guiTop = (w.getScaledHeight() - 166) / 2;

        int recipeBookOffset = 0;
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeBookScreen) {
            var rbw = ((RecipeBookScreenAccessor) MinecraftClient.getInstance().currentScreen).getRecipeBook();
            if (rbw.isOpen()) recipeBookOffset = 78;
        }

        // 装备栏
        for (int i = 0; i < EquipmentInventory.SLOT_COUNT; i++) {
            int sx = guiLeft + 77 + recipeBookOffset;
            int sy = guiTop + 7 + i * 18;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, sx - 1, sy, 0.0f, 0.0f, 17, 17, 17, 17);
            var stack = ClientEquipmentData.getEquipment().getSlot(i);
            if (!stack.isEmpty()) {
                context.drawItem(stack, sx, sy);
                context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, stack, sx, sy);
            }
        }

        // 扩展背包按钮（在垃圾桶左侧）
        int tx = guiLeft + TAB_SLOT_X + recipeBookOffset;
        int ty = guiTop + TAB_SLOT_Y;

        // 槽位框
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, tx - 1, ty - 1, 0.0f, 0.0f, 18, 18, 18, 18);
        // 扩展背包图标（缩放为16x16显示）
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TAB_TEXTURE, tx, ty, 0.0f, 0.0f, 16, 16, 16, 16);

        // 悬浮提示
        if (mouseX >= tx && mouseX < tx + 18 && mouseY >= ty && mouseY < ty + 18) {
            context.fill(tx - 1, ty - 1, tx + 17, ty + 17, 0x44FFFFFF);
        }

        // 垃圾桶（带装备栏框 + 半透明材质）
        int trashLevel = ClientTrashCanData.getTrashLevel();
        if (trashLevel > 0 && trashLevel <= TRASH_TEXTURES.length) {
            int trx = guiLeft + TRASH_SLOT_X + recipeBookOffset;
            int try_ = guiTop + TRASH_SLOT_Y;

            // 装备栏框
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, trx - 1, try_ - 1, 0.0f, 0.0f, 18, 18, 18, 18);

            // 垃圾桶材质（半透明覆盖层模拟透明效果）
            context.drawTexture(RenderPipelines.GUI_TEXTURED, TRASH_TEXTURES[trashLevel - 1], trx, try_, 0.0f, 0.0f, 16, 16, 16, 16);
            context.fill(trx, try_, trx + 16, try_ + 16, 0x80FFFFFF);

            var pending = ClientTrashCanData.getPendingItem();
            if (!pending.isEmpty()) {
                context.drawItem(pending, trx + 1, try_ + 1);
                context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, pending, trx + 1, try_ + 1);
            }
        }
    }
}
