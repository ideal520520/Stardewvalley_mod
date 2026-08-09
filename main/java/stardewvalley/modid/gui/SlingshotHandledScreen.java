package stardewvalley.modid.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class SlingshotHandledScreen extends HandledScreen<SlingshotScreenHandler> {
    private static final int SLOT_SIZE = 18;

    public SlingshotHandledScreen(SlingshotScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 140;
        this.playerInventoryTitleY = 44;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        for (int i = 0; i < handler.slots.size(); i++) {
            var slot = handler.getSlot(i);
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;
            context.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0xFF8B8B8B);
            context.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFFC6C6C6);
            if (slot.hasStack()) {
                context.drawItem(slot.getStack(), sx + 1, sy + 1);
                context.drawStackOverlay(this.textRenderer, slot.getStack(), sx + 1, sy + 1);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawText(this.textRenderer, this.title, x + 8, y + 6, 0xFFFFFFFF, true);
        context.drawText(this.textRenderer, Text.translatable("gui.stardewvalley.slingshot.ammo"), x + 8, y + 22, 0xFFFFFFFF, true);
        context.drawText(this.textRenderer, Text.translatable("container.inventory"), x + 8, y + 44, 0xFFFFFFFF, true);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
