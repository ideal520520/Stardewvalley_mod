package stardewvalley.modid.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

public class BackpackHandledScreen extends HandledScreen<BackpackScreenHandler> {

    private static final Identifier SLOT_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/zhuangbei.png");
    private static final Identifier TAB_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Inventory_Tab.png");
    private static final int SLOT_SIZE = 18;
    private static final int BACK_BTN_X = 137;

    public BackpackHandledScreen(BackpackScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = BackpackScreenHandler.getGuiHeight(handler.getBackpackLevel());
        this.playerInventoryTitleY = BackpackScreenHandler.getInventoryTitleY(handler.getBackpackLevel());
        this.playerInventoryTitleX = 8;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // 绘制标题（紧贴背包格子上方）
        context.drawText(this.textRenderer, this.title, x + 8, y + 10, 0xFFFFFF, false);

        // 绘制所有槽位
        for (int i = 0; i < handler.slots.size(); i++) {
            var slot = handler.getSlot(i);
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;
            drawSlotBg(context, sx, sy);

            if (slot.hasStack()) {
                context.drawItem(slot.getStack(), sx + 1, sy + 1);
                context.drawStackOverlay(this.textRenderer, slot.getStack(), sx + 1, sy + 1);
            }
        }

        // 返回原版背包按钮（与"背包"标题同排，右侧）
        int backBtnY = BackpackScreenHandler.getBackButtonY(handler.getBackpackLevel());
        int bx = x + BACK_BTN_X;
        int by = y + backBtnY;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, bx - 1, by - 1, 0.0f, 0.0f, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TAB_TEXTURE, bx, by, 0.0f, 0.0f, 16, 16, 16, 16);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (click.button() == 0) {
            double mx = click.x();
            double my = click.y();
            int x = (this.width - this.backgroundWidth) / 2;
            int y = (this.height - this.backgroundHeight) / 2;
            int backBtnY = BackpackScreenHandler.getBackButtonY(handler.getBackpackLevel());
            int bx = x + BACK_BTN_X;
            int by = y + backBtnY;
            if (mx >= bx && mx < bx + SLOT_SIZE && my >= by && my < by + SLOT_SIZE) {
                if (this.client != null) {
                    this.client.setScreen(new InventoryScreen(this.client.player));
                }
                return true;
            }
        }
        return super.mouseClicked(click, bl);
    }

    private void drawSlotBg(DrawContext context, int x, int y) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF8B8B8B);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFFC6C6C6);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
