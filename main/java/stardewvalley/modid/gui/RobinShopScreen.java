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
import stardewvalley.modid.gui.ClockHudRenderer;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RobinShopScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int COLS = 5;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 30;
    private int invGridX;
    private int invGridY;

    private List<RobinShopItem> currentItems = new ArrayList<>();
    private int scrollOffset = 0;

    public static class RobinShopItem {
        public final String itemId;
        public final boolean isTodo;
        public final ItemStack stack;

        public RobinShopItem(String itemId, boolean isTodo) {
            this.itemId = itemId;
            this.isTodo = isTodo;
            Item item = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, itemId));
            this.stack = item != null ? new ItemStack(item) : ItemStack.EMPTY;
        }
    }

    private static final List<String> ITEM_IDS = List.of("wood", "misc_stone", "wood_chipper");
    private static final Map<String, Boolean> TODO_MAP = new LinkedHashMap<>();

    static {
        TODO_MAP.put("wood", false);
        TODO_MAP.put("misc_stone", false);
        TODO_MAP.put("wood_chipper", true);
    }

    public RobinShopScreen() {
        super(Text.literal("罗宾的建筑商店"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
        invGridX = MARGIN_X;
        invGridY = MARGIN_Y + 3 * CELL_SIZE + 30;
        addDrawableChild(ButtonWidget.builder(
            Text.literal("< 返回"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
        updateItems();
    }

    private void updateItems() {
        currentItems.clear();
        for (String itemId : ITEM_IDS) {
            boolean isTodo = TODO_MAP.getOrDefault(itemId, true);
            currentItems.add(new RobinShopItem(itemId, isTodo));
        }
    }

    private int getCurrentYear() {
        if (client == null || client.world == null) return 1;
        long totalDays = client.world.getTimeOfDay() / 24000L;
        return (int)(totalDays / 112) + 1; // 112天一年(4季节×28天)
    }

    private int getItemActualPrice(String itemId) {
        int year = getCurrentYear();
        return switch (itemId) {
            case "wood" -> year <= 1 ? 10 : 50;
            case "misc_stone" -> year <= 1 ? 20 : 100;
            case "wood_chipper" -> 1000;
            default -> 0;
        };
    }

    private String getPriceLabel(String itemId) {
        int year = getCurrentYear();
        if ("wood".equals(itemId)) {
            return year <= 1 ? "10g" : "50g";
        }
        if ("misc_stone".equals(itemId)) {
            return year <= 1 ? "20g" : "100g";
        }
        return "1000g";
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        int gridStartY = MARGIN_Y;
        for (int i = 0; i < currentItems.size(); i++) {
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            if (mx >= x && mx < x + CELL_SIZE && my >= y && my < y + CELL_SIZE) {
                RobinShopItem shopItem = currentItems.get(i);
                if (shopItem.isTodo || shopItem.stack.isEmpty()) return true;

                int buyCount = 1;
                boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
                boolean ctrl = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
                if (shift && ctrl) buyCount = 999;
                else if (ctrl) buyCount = 25;
                else if (shift) buyCount = 5;

                int price = getItemActualPrice(shopItem.itemId);
                int currentGold = ClockHudRenderer.getClientGold();
                int totalCost = price * buyCount;
                if (currentGold >= totalCost) {
                    ClientPlayNetworking.send(new ModPayloads.ShopBuyC2SPayload(shopItem.itemId, buyCount));
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

        // 标题
        int year = getCurrentYear();
        String title = "罗宾的建筑商店 (第" + year + "年)";
        context.drawText(textRenderer, Text.literal(title), MARGIN_X, 14, 0xFFFFFF, false);

        int gridStartY = MARGIN_Y;
        int currentGold = ClockHudRenderer.getClientGold();
        context.enableScissor(0, gridStartY, width, height - gridStartY);
        for (int i = 0; i < currentItems.size(); i++) {
            RobinShopItem shopItem = currentItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            if (currentGold >= getItemActualPrice(shopItem.itemId)) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0xFF333333);
                context.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, 0xFF448844);
            } else {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0xFF222222);
                context.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, 0xFF333333);
            }

            if (!shopItem.stack.isEmpty()) {
                context.drawItem(shopItem.stack, x + 8, y + 4);
                context.drawStackOverlay(textRenderer, shopItem.stack, x + 8, y + 4);
            }

            int price = getItemActualPrice(shopItem.itemId);
            NumberRenderer.drawNumber(context, price, x + 2, y + 22, 0.2f, true);

            if (shopItem.isTodo) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x66000000);
            }

            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                String name = !shopItem.stack.isEmpty() ? shopItem.stack.getName().getString() : shopItem.itemId;
                tooltip.add(Text.literal(name));

                String priceLabel = getPriceLabel(shopItem.itemId);
                if ("wood".equals(shopItem.itemId) || "misc_stone".equals(shopItem.itemId)) {
                    String yearInfo = year <= 1 ? "第一年价格" : "第一年后价格";
                    tooltip.add(Text.literal(priceLabel + " (" + yearInfo + ")"));
                } else {
                    tooltip.add(Text.literal(priceLabel));
                }

                if (shopItem.isTodo) {
                    tooltip.add(Text.literal("§c[TODO]"));
                }
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
        context.disableScissor();

        context.drawText(textRenderer, Text.literal("你的背包"), invGridX, invGridY - 12, 0xFFFFFF, false);

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
