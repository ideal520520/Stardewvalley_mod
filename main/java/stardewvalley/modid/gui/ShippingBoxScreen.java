package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ShippingBoxScreen extends Screen {

    private static final int SLOT_SIZE = 18;

    private List<GoldManager.ShippingEntry> entries = new ArrayList<>();
    private boolean dataLoaded = false;
    private int scrollOffset = 0;
    private int invGridX;
    private int invGridY;

    public ShippingBoxScreen() {
        super(Text.translatable("gui.stardewvalley.shipping_box.title"));
    }

    public void setShippingData(List<GoldManager.ShippingEntry> entries) {
        this.entries = entries;
        this.dataLoaded = true;
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.ShippingDataRequestC2SPayload());
        invGridX = 10;
        invGridY = 30;

        addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.shipping_box.back"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(5, 5, 50, 20).build());
    }

    private void scrollUp() {
        if (scrollOffset > 0) scrollOffset--;
    }

    private void scrollDown() {
        int maxOff = Math.max(0, (entries.size() + COLUMNS - 1) / COLUMNS - VISIBLE_ROWS);
        if (scrollOffset < maxOff) scrollOffset++;
    }

    private static final int VISIBLE_ROWS = 5;
    private static final int COLUMNS = 4;
    private static final int COLUMN_GAP = 55;

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        for (int slot = 0; slot < 36; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int sx = invGridX + col * SLOT_SIZE;
            int sy = invGridY + row * SLOT_SIZE;
            if (mx >= sx && mx < sx + SLOT_SIZE && my >= sy && my < sy + SLOT_SIZE) {
                handleSlotClick(slot);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (vertical > 0) scrollUp();
        if (vertical < 0) scrollDown();
        return true;
    }

    private void handleSlotClick(int slot) {
        if (client == null || client.player == null) return;
        int invIndex = slot < 9 ? slot + 27 : slot - 9;
        ItemStack stack = client.player.getInventory().getStack(invIndex);
        if (stack.isEmpty()) return;
        if (GoldManager.getItemMoneyValue(stack) <= 0) return;
        int count = getClickSellCount(stack.getCount());
        ClientPlayNetworking.send(new ModPayloads.AddToShippingC2SPayload(slot, count));
        ClientPlayNetworking.send(new ModPayloads.ShippingDataRequestC2SPayload());
    }

    private int getClickSellCount(int stackSize) {
        if (client == null) return 1;
        boolean shift = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        boolean ctrl = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        if (shift && ctrl) return stackSize;
        if (ctrl) return Math.min(25, stackSize);
        if (shift) return Math.min(5, stackSize);
        return 1;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.shipping_box.your_inventory"), invGridX, invGridY - 12, 0xFFFFFF, false);

        for (int slot = 0; slot < 36; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int sx = invGridX + col * SLOT_SIZE;
            int sy = invGridY + row * SLOT_SIZE;

            context.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0xFF8B8B8B);
            context.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFFC6C6C6);

            int invIndex = slot < 9 ? slot + 27 : slot - 9;
            ItemStack stack = client != null && client.player != null
                ? client.player.getInventory().getStack(invIndex) : ItemStack.EMPTY;

            if (!stack.isEmpty()) {
                context.drawItem(stack, sx + 1, sy + 1);
                context.drawStackOverlay(textRenderer, stack, sx + 1, sy + 1);
                if (GoldManager.getItemMoneyValue(stack) > 0)
                    context.fill(sx, sy, sx + SLOT_SIZE, sy + 1, 0xFF00DD00);
            }
        }

        int shipX = 220;
        int shipY = invGridY;
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.shipping_box.title"), shipX, shipY - 12, 0xFFFFFF, false);

        if (dataLoaded) {
            if (entries.isEmpty()) {
                context.drawText(textRenderer, Text.translatable("gui.stardewvalley.shipping_box.empty"), shipX, shipY + 4, 0x808080, false);
            } else {
                int startIdx = scrollOffset * COLUMNS;
                for (int i = startIdx; i < Math.min(entries.size(), startIdx + COLUMNS * VISIBLE_ROWS); i++) {
                    GoldManager.ShippingEntry e = entries.get(i);
                    int idx = i - startIdx;
                    int c = idx % COLUMNS;
                    int r = idx / COLUMNS;
                    int ix = shipX + c * COLUMN_GAP;
                    int iy = shipY + r * 38;

                    ItemStack icon = new ItemStack(Registries.ITEM.get(e.itemId), 1);
                    context.drawItem(icon, ix, iy);
                    context.drawStackOverlay(textRenderer, icon, ix, iy);

                    String shortName = e.itemId.getPath().replace('_', ' ');
                    if (shortName.length() > 12) shortName = shortName.substring(0, 12);

                    context.drawText(textRenderer, Text.literal(shortName), ix + 18, iy, 0xFFFFFF, false);
                    context.drawText(textRenderer, Text.literal("x"), ix + 18, iy + 10, 0xFFFF55, false);
                    NumberRenderer.drawNumber(context, e.count, ix + 18 + textRenderer.getWidth("x"), iy + 10, 0.33f, true);
                }
            }
        } else {
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.shipping_box.loading"), shipX, shipY + 4, 0x808080, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
