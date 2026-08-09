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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarnieShopScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int COLS = 5;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 30;
    private int invGridX;
    private int invGridY;

    private int currentTab = 0;
    private final String[] TAB_NAMES = {"商品", "牲畜"};
    private final int[] TAB_WIDTHS = {50, 50};
    private List<MarnieShopItem> currentItems = new ArrayList<>();
    private int scrollOffset = 0;

    public static class MarnieShopItem {
        public final String itemId;
        public final int price;
        public final ItemStack stack;
        public final boolean isTodo;

        public MarnieShopItem(String itemId, int price, boolean isTodo, boolean isVanilla) {
            this.itemId = isVanilla ? "minecraft:" + itemId : itemId;
            this.price = price;
            this.isTodo = isTodo;
            Identifier id = isVanilla ? Identifier.of("minecraft", itemId) : Identifier.of(StardewValley.MOD_ID, itemId);
            Item item = Registries.ITEM.get(id);
            this.stack = item != null ? new ItemStack(item) : ItemStack.EMPTY;
        }
    }

    private static final Map<String, Integer> GOODS_STOCK = new LinkedHashMap<>();
    private static final Map<String, Integer> LIVESTOCK_STOCK = new LinkedHashMap<>();
    private static final Map<String, Boolean> TODO_ITEMS = new LinkedHashMap<>();

    static {
        GOODS_STOCK.put("hay", 50);
        GOODS_STOCK.put("milk_pail", 1000);
        GOODS_STOCK.put("shears", 1000);
        GOODS_STOCK.put("auto_grafter", 25000);
        GOODS_STOCK.put("gold_egg", 100000);

        LIVESTOCK_STOCK.put("chicken_spawn_egg", 800);
        LIVESTOCK_STOCK.put("cow_spawn_egg", 1500);
        LIVESTOCK_STOCK.put("goat_spawn_egg", 4000);
        LIVESTOCK_STOCK.put("sheep_spawn_egg", 8000);
        LIVESTOCK_STOCK.put("rabbit_spawn_egg", 8000);
        LIVESTOCK_STOCK.put("pig_spawn_egg", 16000);

        TODO_ITEMS.put("auto_grafter", true);
    }

    public MarnieShopScreen() {
        super(Text.literal("玛妮的牧场"));
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
        updateTabItems();
    }

    private void updateTabItems() {
        currentItems.clear();
        if (currentTab == 0) {
            for (Map.Entry<String, Integer> entry : GOODS_STOCK.entrySet()) {
                boolean isTodo = TODO_ITEMS.containsKey(entry.getKey());
                currentItems.add(new MarnieShopItem(entry.getKey(), entry.getValue(), isTodo, false));
            }
        } else {
            boolean hasDiscount = ClockHudRenderer.hasAnimalCatalogueDiscount();
            for (Map.Entry<String, Integer> entry : LIVESTOCK_STOCK.entrySet()) {
                int price = hasDiscount ? Math.round(entry.getValue() * 0.8f) : entry.getValue();
                currentItems.add(new MarnieShopItem(entry.getKey(), price, false, true));
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
                MarnieShopItem shopItem = currentItems.get(i);
                if (shopItem.isTodo || shopItem.stack.isEmpty()) return true;

                int buyCount = 1;
                boolean shift = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                boolean ctrl = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (shift && ctrl) buyCount = 999;
                else if (ctrl) buyCount = 25;
                else if (shift) buyCount = 5;

                int currentGold = ClockHudRenderer.getClientGold();
                int totalCost = shopItem.price * buyCount;
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

        int tabPosX = MARGIN_X;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tw = TAB_WIDTHS[i];
            int color = i == currentTab ? 0xFF88FF88 : 0xFF888888;
            context.fill(tabPosX, 14, tabPosX + tw, 28, 0xFF333333);
            context.fill(tabPosX + 1, 15, tabPosX + tw - 1, 27, i == currentTab ? 0xFF446644 : 0xFF222222);
            context.drawCenteredTextWithShadow(textRenderer, TAB_NAMES[i], tabPosX + tw / 2, 17, color);
            tabPosX += tw + 6;
        }

        int gridStartY = MARGIN_Y;
        int currentGold = ClockHudRenderer.getClientGold();
        context.enableScissor(0, gridStartY, width, height - gridStartY);
        for (int i = 0; i < currentItems.size(); i++) {
            MarnieShopItem shopItem = currentItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            if (currentGold >= shopItem.price) {
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

            if (shopItem.isTodo) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x66000000);
                context.drawText(textRenderer, Text.literal("[TODO]"), x + 2, y + 22, 0xFF888888, false);
            } else {
                NumberRenderer.drawNumber(context, shopItem.price, x + 2, y + 22, 0.2f, true);
            }

            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                String name = !shopItem.stack.isEmpty() ? shopItem.stack.getName().getString() : shopItem.itemId;
                tooltip.add(Text.literal(name));
                tooltip.add(Text.literal(shopItem.price + "金"));
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
