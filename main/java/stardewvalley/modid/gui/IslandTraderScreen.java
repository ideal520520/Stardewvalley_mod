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
import stardewvalley.modid.crafting.CraftingMaterialSets;
import stardewvalley.modid.season.Season;

import java.util.ArrayList;
import java.util.List;

public class IslandTraderScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int COLS = 9;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 30;
    private int invGridX;
    private int invGridY;

    private int currentTab = 0;
    private final String[] TAB_NAMES = {"固定商品", "特定商品"};
    private final int[] TAB_WIDTHS = {80, 80};
    private List<TradeItem> currentItems = new ArrayList<>();
    private int scrollOffset = 0;

    private final List<TradeItem> fixedItems = new ArrayList<>();
    private final List<TradeItem> specialItems = new ArrayList<>();

    private static class Ingredient {
        final String itemId;
        final int count;
        Ingredient(String itemId, int count) {
            this.itemId = itemId;
            this.count = count;
        }
    }

    private static class TradeItem {
        final String resultItemId;
        final String displayName;
        final List<Ingredient> requiredItems;
        final boolean isTimed;
        final boolean hasTodo;

        TradeItem(String resultItemId, String displayName, List<Ingredient> requiredItems) {
            this(resultItemId, displayName, requiredItems, false, true);
        }

        TradeItem(String resultItemId, String displayName, List<Ingredient> requiredItems, boolean isTimed) {
            this(resultItemId, displayName, requiredItems, isTimed, true);
        }

        TradeItem(String resultItemId, String displayName, List<Ingredient> requiredItems, boolean isTimed, boolean hasTodo) {
            this.resultItemId = resultItemId;
            this.displayName = displayName;
            this.requiredItems = requiredItems;
            this.isTimed = isTimed;
            this.hasTodo = hasTodo;
        }
    }

    public IslandTraderScreen() {
        super(Text.literal("岛屿商人"));
        initFixedItems();
        initSpecialItems();
        updateCurrentItems();
    }

    private void initFixedItems() {
        fixedItems.add(new TradeItem("warp_totem_farm", "传送图腾：农场", List.of(new Ingredient("taroroot", 5)), false, false));
        fixedItems.add(new TradeItem("taroroot_seeds", "芋头块茎", List.of(new Ingredient("bone_fragment", 2)), false, false));
        fixedItems.add(new TradeItem("pineapple_seeds", "菠萝种子", List.of(new Ingredient("caiji_magmacap", 1)), false, false));
        fixedItems.add(new TradeItem("golden_coconut", "金色椰子", List.of(new Ingredient("caiji_coconut", 10)), false, false));
        fixedItems.add(new TradeItem("mahogany_seed", "桃花心木种子", List.of(new Ingredient("fish_stingray", 1)), false, false));
    }

    private void initSpecialItems() {
        specialItems.add(new TradeItem("galaxy_soul", "银河之魂", List.of(new Ingredient("radioactive_ore", 10)), true, false));
    }

    private void updateCurrentItems() {
        currentItems.clear();
        if (currentTab == 0) {
            currentItems.addAll(fixedItems);
        } else {
            boolean lastDay = isLastDayOfSeason();
            for (TradeItem item : specialItems) {
                if (!item.isTimed || lastDay) {
                    currentItems.add(item);
                }
            }
        }
    }

    private boolean isLastDayOfSeason() {
        if (client == null || client.world == null) return false;
        long timeOfDay = client.world.getTimeOfDay();
        int dayOfSeason = Season.getDayOfSeason(timeOfDay);
        return dayOfSeason == 28;
    }

    private boolean hasEnoughItems(TradeItem item) {
        for (Ingredient ing : item.requiredItems) {
            if (CraftingMaterialSets.countItemInInventory(ing.itemId, client) < ing.count) return false;
        }
        return true;
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
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        int tabPosX = MARGIN_X;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            if (mx >= tabPosX && mx < tabPosX + TAB_WIDTHS[i] && my >= 14 && my < 28) {
                currentTab = i;
                scrollOffset = 0;
                updateCurrentItems();
                return true;
            }
            tabPosX += TAB_WIDTHS[i] + 6;
        }

        // 点击商品交易
        for (int i = 0; i < currentItems.size(); i++) {
            TradeItem item = currentItems.get(i);
            if (item.hasTodo) continue;
            if (!hasEnoughItems(item)) continue;

            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = MARGIN_Y + row * CELL_SIZE - scrollOffset;

            if (y < MARGIN_Y - CELL_SIZE || y > MARGIN_Y + 3 * CELL_SIZE) continue;

            if (mx >= x && mx < x + CELL_SIZE && my >= y && my < y + CELL_SIZE) {
                int tradeCount = 1;
                boolean shift = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                boolean ctrl = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (shift && ctrl) tradeCount = 999;
                else if (ctrl) tradeCount = 25;
                else if (shift) tradeCount = 5;

                // TODO: 发送交易网络包（暂用DesertTraderBuyC2SPayload作为示例）
                ClientPlayNetworking.send(new ModPayloads.DesertTraderBuyC2SPayload(item.resultItemId, tradeCount));
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
        context.enableScissor(0, gridStartY, width, height - gridStartY);
        for (int i = 0; i < currentItems.size(); i++) {
            TradeItem item = currentItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            // 格子背景
            boolean canTrade = !item.hasTodo && hasEnoughItems(item);
            int bgColor = item.hasTodo ? 0xFF555555 : (canTrade ? 0xFF333333 : 0xFF222222);
            int borderColor = item.hasTodo ? 0xFF884444 : (canTrade ? 0xFF448844 : 0xFF333333);
            context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, bgColor);
            context.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, borderColor);

            // 结果物品图标（居中）
            Item resultItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, item.resultItemId));
            if (resultItem != null && resultItem != net.minecraft.item.Items.AIR) {
                context.drawItem(new ItemStack(resultItem), x + 8, y + 4);
            }

            // 所需物品小图标（左上角）+ 数量
            if (!item.requiredItems.isEmpty()) {
                Ingredient ing = item.requiredItems.get(0);
                Item ingItem = CraftingMaterialSets.getItemForDisplay(ing.itemId);
                if (ingItem != null) {
                    var matrices = context.getMatrices();
                    matrices.pushMatrix();
                    matrices.translate(x + 1, y + 1);
                    matrices.scale(0.5f, 0.5f);
                    context.drawItem(new ItemStack(ingItem), 0, 0);
                    matrices.popMatrix();
                }

                int playerCount = CraftingMaterialSets.countItemInInventory(ing.itemId, client);
                int countColor = playerCount >= ing.count ? 0x88FF88 : 0xFF8888;
                context.drawText(textRenderer, Text.literal(String.valueOf(ing.count)), x + 10, y + 1, countColor, false);
            }

            // TODO标记
            if (item.hasTodo) {
                context.drawText(textRenderer, Text.literal("[TODO]"), x + 2, y + 22, 0xFFAA44, false);
            }

            // Tooltip
            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal(item.displayName));
                for (Ingredient ing : item.requiredItems) {
                    Item ingItem = CraftingMaterialSets.getItemForDisplay(ing.itemId);
                    String ingName = ingItem != null ? ingItem.getName().getString() : ing.itemId;
                    int pc = CraftingMaterialSets.countItemInInventory(ing.itemId, client);
                    tooltip.add(Text.literal("需要 " + ingName + " x" + ing.count + " (你有: " + pc + ")"));
                }
                if (item.hasTodo) {
                    tooltip.add(Text.literal("§c此交易尚未实装"));
                }
                if (item.isTimed && !isLastDayOfSeason()) {
                    tooltip.add(Text.literal("§c仅在每个季节的最后一天可用"));
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
