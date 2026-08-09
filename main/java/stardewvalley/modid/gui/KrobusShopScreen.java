package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.gui.ClockHudRenderer;

import java.util.ArrayList;
import java.util.List;

public class KrobusShopScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int COLS = 9;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 30;
    private int invGridX;
    private int invGridY;

    public static List<ModPayloads.KrobusShopItem> CURRENT_STOCK = new ArrayList<>();

    private int currentTab = 0;
    private final String[] TAB_NAMES = {"固定商品", "滚动商品"};
    private final int[] TAB_WIDTHS = {80, 80};
    private List<ModPayloads.KrobusShopItem> tabItems = new ArrayList<>();
    private int scrollOffset = 0;

    // 固定商品数量（由服务端决定）
    private static final int FIXED_ITEM_COUNT = 4;

    public KrobusShopScreen() {
        super(Text.literal("科罗布斯的商店"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.KrobusDataRequestC2SPayload());
        invGridX = MARGIN_X;
        invGridY = MARGIN_Y + 3 * CELL_SIZE + 30;
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
            Text.literal("< 返回"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
        currentTab = 0;
        updateTabItems();
    }

    public void refreshStock() {
        updateTabItems();
    }

    private void updateTabItems() {
        tabItems.clear();
        if (currentTab == 0) {
            // 固定商品（前FIXED_ITEM_COUNT个）
            for (int i = 0; i < CURRENT_STOCK.size() && i < FIXED_ITEM_COUNT; i++) {
                tabItems.add(CURRENT_STOCK.get(i));
            }
        } else {
            // 滚动商品（第FIXED_ITEM_COUNT个及以后）
            for (int i = FIXED_ITEM_COUNT; i < CURRENT_STOCK.size(); i++) {
                tabItems.add(CURRENT_STOCK.get(i));
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        // Tab栏点击
        int tabPosX = MARGIN_X;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            if (mx >= tabPosX && mx < tabPosX + TAB_WIDTHS[i] && my >= 14 && my < 28) {
                currentTab = i;
                scrollOffset = 0;
                updateTabItems();
                return true;
            }
            tabPosX += TAB_WIDTHS[i] + 6;
        }

        // 点击商品购买
        int gridStartY = MARGIN_Y;
        for (int i = 0; i < tabItems.size(); i++) {
            ModPayloads.KrobusShopItem entry = tabItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            if (mx >= x && mx < x + CELL_SIZE && my >= y && my < y + CELL_SIZE) {
                if (entry.available <= 0) return true;
                int currentGold = ClockHudRenderer.getClientGold();
                if (currentGold >= entry.price) {
                    int count = getClickBuyCount(entry);
                    ClientPlayNetworking.send(new ModPayloads.KrobusBuyC2SPayload(entry.itemId, count, entry.price));
                }
                return true;
            }
        }
        return false;
    }

    private int getClickBuyCount(ModPayloads.KrobusShopItem entry) {
        if (client == null) return 1;
        boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        return shift ? Math.min(entry.maxBuy, entry.available) : 1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int gridStartY = MARGIN_Y;
        int rows = (tabItems.size() + COLS - 1) / COLS;
        int visibleRows = 3;
        int maxScroll = Math.max(0, rows * CELL_SIZE - visibleRows * CELL_SIZE);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * CELL_SIZE));
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        // 标题
        context.drawText(textRenderer, Text.literal("科罗布斯的商店"), MARGIN_X, 5, 0xFFFFFF, false);

        // Tab栏
        int tabPosX = MARGIN_X;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tw = TAB_WIDTHS[i];
            int color = i == currentTab ? 0xFF88FF88 : 0xFF888888;
            context.fill(tabPosX, 14, tabPosX + tw, 28, 0xFF333333);
            context.fill(tabPosX + 1, 15, tabPosX + tw - 1, 27, i == currentTab ? 0xFF446644 : 0xFF222222);
            context.drawCenteredTextWithShadow(textRenderer, TAB_NAMES[i], tabPosX + tw / 2, 17, color);
            tabPosX += tw + 6;
        }

        // 商品网格
        int gridStartY = MARGIN_Y;
        context.enableScissor(0, gridStartY, width, height - gridStartY);
        for (int i = 0; i < tabItems.size(); i++) {
            ModPayloads.KrobusShopItem entry = tabItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            boolean soldOut = entry.available <= 0;
            int currentGold = ClockHudRenderer.getClientGold();
            boolean canAfford = !soldOut && currentGold >= entry.price;
            int bgColor = soldOut ? 0xFF222222 : (canAfford ? 0xFF333333 : 0xFF222222);
            int borderColor = soldOut ? 0xFF111111 : (canAfford ? 0xFF448844 : 0xFF333333);
            context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, bgColor);
            context.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, borderColor);

            if (!soldOut || entry.available == 9999) {
                var stack = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, entry.itemId));
                if (stack != null) {
                    context.drawItem(new ItemStack(stack), x + 8, y + 4);
                    context.drawStackOverlay(textRenderer, new ItemStack(stack), x + 8, y + 4);
                }
                NumberRenderer.drawNumber(context, entry.price, x + 2, y + 22, 0.2f, true);
            }

            // 显示剩余可购买数量
            String availText;
            if (entry.available >= 9999) {
                availText = "×∞";
            } else if (soldOut) {
                availText = "×0";
            } else {
                availText = "×" + entry.available;
            }
            int xOff = soldOut ? x + CELL_SIZE - 12 : x + CELL_SIZE - 10;
            context.drawText(textRenderer, Text.literal(availText), xOff, y + 1,
                soldOut ? 0xFF444444 : 0xFFFFAA, false);

            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                if (soldOut && entry.available < 9999) {
                    tooltip.add(Text.literal("§c卖光了"));
                } else {
                    var stack = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, entry.itemId));
                    if (stack != null) {
                        tooltip.add(Text.literal(stack.getName().getString()));
                    } else {
                        tooltip.add(Text.literal(entry.itemId));
                    }
                    tooltip.add(Text.literal(entry.price + "g"));
                    if (entry.maxBuy >= 9999) {
                        tooltip.add(Text.literal("§7无限供应"));
                    } else if (entry.maxBuy == 1) {
                        tooltip.add(Text.literal("§7限购1次"));
                    } else {
                        tooltip.add(Text.literal("§7限购 " + entry.maxBuy + " 个/天"));
                    }
                    if (entry.available > 0 && entry.available < 9999) {
                        tooltip.add(Text.literal("§7剩余: " + entry.available));
                    }
                }
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
        context.disableScissor();

        // 背包
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

        // 金币
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
