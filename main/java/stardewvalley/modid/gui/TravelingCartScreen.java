package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import stardewvalley.modid.StardewValley;

import java.util.ArrayList;
import java.util.List;

public class TravelingCartScreen extends Screen {

    private static final Identifier CART_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Traveling_Cart.png");
    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int CART_WIDTH = 125;
    private static final int CART_HEIGHT = 69;
    private static final int COLS = 11;
    private static final int CELL = 28;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int GRID_Y = 105;
    private int invGridX;
    private int invGridY;

    private int scrollOffset = 0;

    public static List<TravelingCartEntry> CURRENT_STOCK = new ArrayList<>();

    public static class TravelingCartEntry {
        public String itemId;
        public int price;
        public int maxBuy;
        public int available;
        public boolean isSpecial;

        public TravelingCartEntry(String itemId, int price, int maxBuy, int available, boolean isSpecial) {
            this.itemId = itemId;
            this.price = price;
            this.maxBuy = maxBuy;
            this.available = available;
            this.isSpecial = isSpecial;
        }
    }

    public TravelingCartScreen() {
        super(Text.translatable("gui.stardewvalley.traveling_cart.title"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.TravelingCartStockRequestC2SPayload());
        invGridX = MARGIN_X;
        invGridY = this.height - 100;
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.traveling_cart.back"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        // 点击商品购买
        for (int i = 0; i < CURRENT_STOCK.size(); i++) {
            TravelingCartEntry entry = CURRENT_STOCK.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL;
            int y = GRID_Y + row * CELL - scrollOffset;

            if (y < GRID_Y - CELL || y > GRID_Y + 6 * CELL) continue;

            if (mx >= x && mx < x + CELL && my >= y && my < y + CELL) {
                if (entry.available <= 0) return true;
                int currentGold = ClockHudRenderer.getClientGold();
                if (currentGold >= entry.price) {
                    int count = getClickBuyCount(entry);
                    ClientPlayNetworking.send(new ModPayloads.TravelingCartBuyC2SPayload(entry.itemId, count, entry.price));
                }
                return true;
            }
        }

        // 点击背包格子
        for (int slot = 0; slot < 36; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int sx = invGridX + col * SLOT_SIZE;
            int sy = invGridY + row * SLOT_SIZE;
            if (mx >= sx && mx < sx + SLOT_SIZE && my >= sy && my < sy + SLOT_SIZE) {
                if (client == null || client.player == null) return true;
                return true;
            }
        }
        return false;
    }

    private int getClickBuyCount(TravelingCartEntry entry) {
        if (client == null) return 1;
        boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        return shift ? Math.min(entry.maxBuy, entry.available) : 1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int rows = (CURRENT_STOCK.size() + COLS - 1) / COLS;
        int maxScroll = Math.max(0, rows * CELL - 6 * CELL);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * CELL));
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        // 绘制旅行货车图片（顶部居中）
        int cartX = (width - CART_WIDTH) / 2;
        int cartY = 8;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, CART_TEXTURE, cartX, cartY, 0, 0, CART_WIDTH, CART_HEIGHT, CART_WIDTH, CART_HEIGHT);

        // "商品" 标题
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("商品"), width / 2, cartY + CART_HEIGHT + 5, 0xFFFFFF);

        // 营业时间
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("星期五和星期日：06:00至20:00"), width / 2, cartY + CART_HEIGHT + 22, 0x888888);

        // 渲染商品网格
        context.enableScissor(0, GRID_Y, width, height - GRID_Y);
        for (int i = 0; i < CURRENT_STOCK.size(); i++) {
            TravelingCartEntry entry = CURRENT_STOCK.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL;
            int y = GRID_Y + row * CELL - scrollOffset;

            if (y < GRID_Y - CELL || y > GRID_Y + 6 * CELL) continue;

            boolean soldOut = entry.available <= 0;
            int bgColor = soldOut ? 0xFF222222 : (entry.isSpecial ? 0xFF444444 : 0xFF333333);
            int bgInner = soldOut ? 0xFF111111 : (entry.isSpecial ? 0xFF333333 : 0xFF222222);
            context.fill(x, y, x + CELL, y + CELL, bgColor);
            context.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, bgInner);

            if (!soldOut) {
                Identifier itemId = Identifier.of(StardewValley.MOD_ID, entry.itemId);
                Item item = Registries.ITEM.get(itemId);
                if (item != null && item != net.minecraft.item.Items.AIR) {
                    context.drawItem(new ItemStack(item), x + 6, y + 2);
                }
                NumberRenderer.drawNumber(context, entry.price, x + 2, y + 20, 0.16f, true);
            }

            // 显示剩余可购买数量
            int xOff = soldOut ? x + CELL - 12 : x + CELL - 10;
            context.drawText(textRenderer, Text.literal("×" + entry.available), xOff, y + 1,
                soldOut ? 0xFF444444 : 0xFFFFAA, false);

            if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                context.fill(x, y, x + CELL, y + CELL, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                if (soldOut) {
                    tooltip.add(Text.literal("§c卖光了"));
                } else {
                    Identifier itemId = Identifier.of(StardewValley.MOD_ID, entry.itemId);
                    Item item = Registries.ITEM.get(itemId);
                    if (item != null && item != net.minecraft.item.Items.AIR) {
                        tooltip.add(Text.literal(item.getName().getString()));
                    } else {
                        tooltip.add(Text.literal(entry.itemId));
                    }
                    tooltip.add(Text.literal(entry.price + "g"));
                    if (entry.isSpecial) {
                        tooltip.add(Text.literal("§6特殊物品"));
                    }
                }
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
        context.disableScissor();

        // 背包
        renderInventory(context);

        // 金币显示
        int gold = ClockHudRenderer.getClientGold();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GOLD_TEXTURE, MARGIN_X, height - 20, 0.0f, 0.0f, 16, 16, 16, 16);
        NumberRenderer.drawNumber(context, gold, MARGIN_X + 18, height - 16, 0.255f, true);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderInventory(DrawContext context) {
        context.drawText(textRenderer, Text.literal("背包"), invGridX, invGridY - 12, 0xFFFFFF, false);
        for (int slot = 0; slot < 36; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int sx = invGridX + col * SLOT_SIZE;
            int sy = invGridY + row * SLOT_SIZE;
            context.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0xFF8B8B8B);
            context.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFFC6C6C6);
            int invIndex = slot < 9 ? slot + 27 : slot - 9;
            ItemStack stack = client != null && client.player != null ? client.player.getInventory().getStack(invIndex) : ItemStack.EMPTY;
            if (!stack.isEmpty()) {
                context.drawItem(stack, sx + 1, sy + 1);
                context.drawStackOverlay(textRenderer, stack, sx + 1, sy + 1);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
