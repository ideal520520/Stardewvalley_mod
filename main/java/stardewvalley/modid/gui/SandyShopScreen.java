package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SandyShopScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int COLS = 5;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 30;
    private int invGridX;
    private int invGridY;

    private int currentTab = 0;
    private final String[] TAB_NAMES = {"gui.stardewvalley.sandy_shop.fixed_items", "gui.stardewvalley.sandy_shop.rotating_items"};
    private final int[] TAB_WIDTHS = {90, 110};
    private List<SandyShopItem> currentItems = new ArrayList<>();
    private int scrollOffset = 0;

    // 服务端同步的滚动商品库存
    private ModPayloads.SandyShopStockItem rotatingStock;

    public static class SandyShopItem {
        public final String itemId;
        public final int price;
        public final int maxBuy;
        public int available;
        public final ItemStack stack;
        public final boolean isTodo;

        public SandyShopItem(String itemId, int price, int maxBuy, int available, boolean isTodo) {
            this.itemId = itemId;
            this.price = price;
            this.maxBuy = maxBuy;
            this.available = available;
            this.isTodo = isTodo;
            Item item = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, itemId));
            this.stack = item != null ? new ItemStack(item) : ItemStack.EMPTY;
        }
    }

    private static final Map<String, Integer> FIXED_STOCK = new LinkedHashMap<>();
    private static final Map<String, Boolean> FIXED_TODO = new LinkedHashMap<>();

    static {
        FIXED_STOCK.put("cactusfruit_seeds", 150);
        FIXED_TODO.put("cactusfruit_seeds", false);
        FIXED_STOCK.put("rhubarb_seeds", 100);
        FIXED_TODO.put("rhubarb_seeds", false);
        FIXED_STOCK.put("starfruit_seeds", 400);
        FIXED_TODO.put("starfruit_seeds", false);
        FIXED_STOCK.put("beet_seeds", 20);
        FIXED_TODO.put("beet_seeds", false);
    }

    public SandyShopScreen() {
        super(Text.translatable("gui.stardewvalley.sandy_shop.title"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.SandyShopStockRequestC2SPayload());
        invGridX = MARGIN_X;
        invGridY = MARGIN_Y + 3 * CELL_SIZE + 30;
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.sandy_shop.back"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
        updateTabItems();
    }

    public void onStockSync(List<ModPayloads.SandyShopStockItem> items) {
        if (!items.isEmpty()) {
            rotatingStock = items.get(0);
            if (currentTab == 1) {
                updateTabItems();
            }
        }
    }

    private void updateTabItems() {
        currentItems.clear();
        if (currentTab == 0) {
            for (Map.Entry<String, Integer> entry : FIXED_STOCK.entrySet()) {
                boolean todo = FIXED_TODO.getOrDefault(entry.getKey(), false);
                currentItems.add(new SandyShopItem(entry.getKey(), entry.getValue(), 0, 9999, todo));
            }
        } else {
            if (rotatingStock != null) {
                int available = rotatingStock.maxBuy > 0 ? rotatingStock.available : 9999;
                currentItems.add(new SandyShopItem(rotatingStock.itemId, rotatingStock.price, rotatingStock.maxBuy, available, false));
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        int clickTabPosX = MARGIN_X;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            if (mx >= clickTabPosX && mx < clickTabPosX + TAB_WIDTHS[i] && my >= 14 && my < 28) {
                currentTab = i;
                scrollOffset = 0;
                updateTabItems();
                return true;
            }
            clickTabPosX += TAB_WIDTHS[i] + 6;
        }

        int gridStartY = MARGIN_Y;
        for (int i = 0; i < currentItems.size(); i++) {
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            if (mx >= x && mx < x + CELL_SIZE && my >= y && my < y + CELL_SIZE) {
                SandyShopItem shopItem = currentItems.get(i);
                if (shopItem.isTodo || shopItem.stack.isEmpty() || shopItem.available <= 0) return true;

                int buyCount = 1;
                boolean shift = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                boolean ctrl = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (shift && ctrl) buyCount = Math.min(999, shopItem.available);
                else if (ctrl) buyCount = Math.min(25, shopItem.available);
                else if (shift) buyCount = Math.min(5, shopItem.available);
                if (shopItem.maxBuy > 0 && buyCount > shopItem.maxBuy) buyCount = shopItem.maxBuy;

                if (currentTab == 0) {
                    // 固定商品用通用购买数据包
                    int currentGold = ClockHudRenderer.getClientGold();
                    int totalCost = shopItem.price * buyCount;
                    if (currentGold >= totalCost) {
                        ClientPlayNetworking.send(new ModPayloads.ShopBuyC2SPayload(shopItem.itemId, buyCount));
                    }
                } else {
                    // 滚动商品用桑迪商店专用数据包（含库存检查）
                    int currentGold = ClockHudRenderer.getClientGold();
                    int totalCost = shopItem.price * buyCount;
                    if (currentGold >= totalCost) {
                        ClientPlayNetworking.send(new ModPayloads.SandyShopBuyC2SPayload(shopItem.itemId, buyCount, shopItem.price));
                        // 客户端立即扣减库存，避免等待服务端同步期间超买
                        if (shopItem.maxBuy > 0) {
                            shopItem.available = Math.max(0, shopItem.available - buyCount);
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int gridStartY = MARGIN_Y;
        int rows = (currentItems.size() + COLS - 1) / COLS;
        int visibleRows = 3;
        int maxScroll = Math.max(0, rows * CELL_SIZE - visibleRows * CELL_SIZE);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * CELL_SIZE));
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        int tabPosX = MARGIN_X;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tw = TAB_WIDTHS[i];
            int color = i == currentTab ? 0xFF88FF88 : 0xFF888888;
            context.fill(tabPosX, 14, tabPosX + tw, 28, 0xFF333333);
            context.fill(tabPosX + 1, 15, tabPosX + tw - 1, 27, i == currentTab ? 0xFF446644 : 0xFF222222);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable(TAB_NAMES[i]), tabPosX + tw / 2, 17, color);
            tabPosX += tw + 6;
        }

        int gridStartY = MARGIN_Y;
        context.enableScissor(0, gridStartY, width, height - gridStartY);
        for (int i = 0; i < currentItems.size(); i++) {
            SandyShopItem shopItem = currentItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            boolean soldOut = shopItem.available <= 0;
            boolean canAfford = !soldOut && ClockHudRenderer.getClientGold() >= shopItem.price;
            int bgColor = soldOut ? 0xFF222222 : (canAfford ? 0xFF333333 : 0xFF222222);
            int borderColor = soldOut ? 0xFF111111 : (canAfford ? 0xFF448844 : 0xFF333333);
            context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, bgColor);
            context.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, borderColor);

            if (!shopItem.stack.isEmpty()) {
                context.drawItem(shopItem.stack, x + 8, y + 4);
                context.drawStackOverlay(textRenderer, shopItem.stack, x + 8, y + 4);
            }

            NumberRenderer.drawNumber(context, shopItem.price, x + 2, y + 22, 0.2f, true);

            if (soldOut) {
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("§c售罄"), x + CELL_SIZE / 2, y + 6, 0xFF4444);
            } else if (shopItem.maxBuy > 0 && currentTab == 1) {
                // 滚动商品限购显示剩余数量
                String remainStr = "x" + shopItem.available;
                context.drawText(textRenderer, Text.literal(remainStr), x + 24, y + 24, 0xFFFFAA, false);
            }

            if (shopItem.isTodo) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x66000000);
            }

            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                String name = !shopItem.stack.isEmpty() ? shopItem.stack.getName().getString() : shopItem.itemId;
                tooltip.add(Text.literal(name));
                tooltip.add(Text.literal(shopItem.price + "g"));
                if (soldOut) {
                    tooltip.add(Text.literal("§c已售罄"));
                } else if (shopItem.maxBuy > 0 && currentTab == 1) {
                    tooltip.add(Text.literal("§e剩余: " + shopItem.available + "/" + shopItem.maxBuy));
                }
                if (shopItem.isTodo) {
                    tooltip.add(Text.literal("§c[TODO]"));
                }
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
        context.disableScissor();

        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.sandy_shop.your_inventory"), invGridX, invGridY - 12, 0xFFFFFF, false);

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
            }
        }

        int gold = ClockHudRenderer.getClientGold();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GOLD_TEXTURE, MARGIN_X, height - 20, 0.0f, 0.0f, 16, 16, 16, 16);
        NumberRenderer.drawNumber(context, gold, MARGIN_X + 18, height - 16, 0.255f, true);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
