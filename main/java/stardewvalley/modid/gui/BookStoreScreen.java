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

public class BookStoreScreen extends Screen {

    private static final Identifier BOOKSELLER_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Bookseller.png");
    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int COLS = 9;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 85;
    private int invGridX;
    private int invGridY;

    private int currentTab = 0;
    private final String[] TAB_NAMES = {"书籍", "交换"};
    private final int[] TAB_WIDTHS = {48, 48};
    private List<BookStoreEntry> currentItems = new ArrayList<>();
    private int scrollOffset = 0;

    public static List<BookStoreEntry> CURRENT_STOCK = new ArrayList<>();

    public static class BookStoreEntry {
        public String itemId;
        public int price;
        public boolean isBarter;
        public String requiredItemId;
        public int requiredCount;
        public String requiredItemId2;
        public int requiredCount2;
        public int resultCount;

        public BookStoreEntry(String itemId, int price) {
            this.itemId = itemId;
            this.price = price;
            this.isBarter = false;
            this.requiredItemId = null;
            this.requiredCount = 0;
            this.requiredItemId2 = null;
            this.requiredCount2 = 0;
            this.resultCount = 1;
        }

        public BookStoreEntry(String itemId, String requiredItemId, int requiredCount) {
            this(itemId, requiredItemId, requiredCount, 1);
        }

        public BookStoreEntry(String itemId, String requiredItemId, int requiredCount, int resultCount) {
            this.itemId = itemId;
            this.price = 0;
            this.isBarter = true;
            this.requiredItemId = requiredItemId;
            this.requiredCount = requiredCount;
            this.requiredItemId2 = null;
            this.requiredCount2 = 0;
            this.resultCount = resultCount;
        }

        public BookStoreEntry(String itemId, String requiredItemId, int requiredCount, String requiredItemId2, int requiredCount2) {
            this.itemId = itemId;
            this.price = 0;
            this.isBarter = true;
            this.requiredItemId = requiredItemId;
            this.requiredCount = requiredCount;
            this.requiredItemId2 = requiredItemId2;
            this.requiredCount2 = requiredCount2;
            this.resultCount = 1;
        }
    }

    public BookStoreScreen() {
        super(Text.literal("书摊老板"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.BookStoreStockRequestC2SPayload());
        invGridX = MARGIN_X;
        invGridY = this.height - 100;
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
            Text.literal("< 返回"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
        updateTabItems();
    }

    public void refreshDisplay() {
        updateTabItems();
    }

    private void updateTabItems() {
        currentItems.clear();
        for (BookStoreEntry entry : CURRENT_STOCK) {
            if (currentTab == 0 && !entry.isBarter && entry.price > 0) {
                currentItems.add(entry);
            } else if (currentTab == 1 && entry.isBarter) {
                currentItems.add(entry);
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
            BookStoreEntry entry = currentItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 4 * CELL_SIZE) continue;

            if (mx >= x && mx < x + CELL_SIZE && my >= y && my < y + CELL_SIZE) {
                Identifier itemId = Identifier.of(StardewValley.MOD_ID, entry.itemId);
                Item item = Registries.ITEM.get(itemId);
                if (item == null || item == net.minecraft.item.Items.AIR) return true;

                if (currentTab == 0) {
                    int buyCount = 1;
                    boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
                    boolean ctrl = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
                    if (shift && ctrl) buyCount = 999;
                    else if (ctrl) buyCount = 25;
                    else if (shift) buyCount = 5;

                    int currentGold = ClockHudRenderer.getClientGold();
                    if (currentGold >= entry.price * buyCount) {
                        ClientPlayNetworking.send(new ModPayloads.BookStoreBuyC2SPayload(entry.itemId, buyCount, entry.price));
                    }
                } else {
                    int tradeCount = 1;
                    boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
                    boolean ctrl = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
                    if (shift && ctrl) tradeCount = 999;
                    else if (ctrl) tradeCount = 25;
                    else if (shift) tradeCount = 5;

                    if (hasRequiredItems(entry, tradeCount)) {
                        ClientPlayNetworking.send(new ModPayloads.BookStoreBarterC2SPayload(
                            entry.itemId, tradeCount * entry.resultCount,
                            entry.requiredItemId, entry.requiredCount * tradeCount,
                            entry.requiredItemId2, entry.requiredCount2 * tradeCount
                        ));
                    }
                }
                return true;
            }
        }

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

    private boolean hasRequiredItems(BookStoreEntry entry, int tradeCount) {
        if (client == null || client.player == null) return false;

        int count1 = 0;
        int count2 = 0;

        if (entry.requiredItemId != null && entry.requiredCount > 0) {
            for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
                ItemStack stack = client.player.getInventory().getStack(slot);
                if (!stack.isEmpty()) {
                    Identifier id = Registries.ITEM.getId(stack.getItem());
                    if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals(entry.requiredItemId)) {
                        count1 += stack.getCount();
                    }
                }
            }
        }
        if (entry.requiredItemId2 != null && entry.requiredCount2 > 0) {
            for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
                ItemStack stack = client.player.getInventory().getStack(slot);
                if (!stack.isEmpty()) {
                    Identifier id = Registries.ITEM.getId(stack.getItem());
                    if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals(entry.requiredItemId2)) {
                        count2 += stack.getCount();
                    }
                }
            }
        }

        if (entry.requiredItemId2 != null && entry.requiredCount2 > 0) {
            // 需要两种材料都满足
            return count1 >= entry.requiredCount * tradeCount && count2 >= entry.requiredCount2 * tradeCount;
        }
        if (entry.requiredItemId != null && entry.requiredCount > 0) {
            return count1 >= entry.requiredCount * tradeCount;
        }
        return false;
    }

    private int countRequiredItem(String itemId) {
        if (client == null || client.player == null) return 0;
        int count = 0;
        for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
            ItemStack stack = client.player.getInventory().getStack(slot);
            if (!stack.isEmpty()) {
                Identifier id = Registries.ITEM.getId(stack.getItem());
                if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals(itemId)) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int gridStartY = MARGIN_Y;
        int rows = (currentItems.size() + COLS - 1) / COLS;
        int visibleRows = 4;
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
        context.enableScissor(0, gridStartY, width, height - gridStartY);
        for (int i = 0; i < currentItems.size(); i++) {
            BookStoreEntry entry = currentItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 4 * CELL_SIZE) continue;

            boolean canAfford = false;
            if (currentTab == 0) {
                int currentGold = ClockHudRenderer.getClientGold();
                canAfford = currentGold >= entry.price;
            } else {
                canAfford = hasRequiredItems(entry, 1);
            }

            int bgColor = canAfford ? 0xFF333333 : 0xFF222222;
            int bgInner = canAfford ? 0xFF448844 : 0xFF333333;
            context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, bgColor);
            context.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, bgInner);

            Identifier itemId = Identifier.of(StardewValley.MOD_ID, entry.itemId);
            Item item = Registries.ITEM.get(itemId);
            if (item != null && item != net.minecraft.item.Items.AIR) {
                int displayCount = currentTab == 0 ? 1 : entry.resultCount;
                context.drawItem(new ItemStack(item, displayCount), x + 8, y + 4);
                context.drawStackOverlay(textRenderer, new ItemStack(item, displayCount), x + 8, y + 4);
            }

            if (currentTab == 0) {
                NumberRenderer.drawNumber(context, entry.price, x + 2, y + 22, 0.2f, true);
            } else {
                // 显示所需材料1的小图标
                if (entry.requiredItemId != null && entry.requiredCount > 0) {
                    Identifier reqId = Identifier.of(StardewValley.MOD_ID, entry.requiredItemId);
                    Item reqItem = Registries.ITEM.get(reqId);
                    if (reqItem != null && reqItem != net.minecraft.item.Items.AIR) {
                        var matrices = context.getMatrices();
                        matrices.pushMatrix();
                        matrices.translate(x + 20, y + 20);
                        matrices.scale(0.5f, 0.5f);
                        context.drawItem(new ItemStack(reqItem), -8, -8);
                        matrices.popMatrix();
                    }
                    int ingCount = countRequiredItem(entry.requiredItemId);
                    int reqCount = entry.requiredCount;
                    int countColor = ingCount >= reqCount ? 0x88FF88 : 0xFF8888;
                    context.drawText(textRenderer, Text.literal("x" + reqCount), x + 24, y + 24, countColor, false);
                }
            }

            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                if (item != null && item != net.minecraft.item.Items.AIR) {
                    tooltip.add(Text.literal(item.getName().getString()));
                } else {
                    tooltip.add(Text.literal(entry.itemId));
                }
                if (currentTab == 0) {
                    tooltip.add(Text.literal(entry.price + "g"));
                } else {
                    if (entry.requiredItemId != null && entry.requiredCount > 0) {
                        int ingCount = countRequiredItem(entry.requiredItemId);
                        Item reqItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, entry.requiredItemId));
                        String reqName = reqItem != null ? reqItem.getName().getString() : entry.requiredItemId;
                        tooltip.add(Text.literal("需要 " + reqName + " x" + entry.requiredCount + " (你有: " + ingCount + ")"));
                    }
                    if (entry.requiredItemId2 != null && entry.requiredCount2 > 0) {
                        int ingCount2 = countRequiredItem(entry.requiredItemId2);
                        Item reqItem2 = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, entry.requiredItemId2));
                        String reqName2 = reqItem2 != null ? reqItem2.getName().getString() : entry.requiredItemId2;
                        tooltip.add(Text.literal("需要 " + reqName2 + " x" + entry.requiredCount2 + " (你有: " + ingCount2 + ")"));
                    }
                }
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
        context.disableScissor();

        int booksellerX = (width - 110) / 2;
        int booksellerY = 4;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BOOKSELLER_TEXTURE, booksellerX, booksellerY, 0, 0, 110, 79, 110, 79);

        renderInventory(context);

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
            ItemStack stack = client != null && client.player != null
                ? client.player.getInventory().getStack(invIndex) : ItemStack.EMPTY;
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
