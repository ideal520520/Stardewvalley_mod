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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DesertTraderScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int COLS = 9;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 30;
    private int invGridX;
    private int invGridY;

    private int currentTab = 0;
    private final String[] TAB_NAMES = {"固定商品", "滚动商品"};
    private final int[] TAB_WIDTHS = {80, 80};
    private List<TradeItem> currentItems = new ArrayList<>();
    private int scrollOffset = 0;

    private final List<TradeItem> fixedItems = new ArrayList<>();
    private final List<TradeItem> rotatingItems = new ArrayList<>();

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

        TradeItem(String resultItemId, String displayName, List<Ingredient> requiredItems) {
            this.resultItemId = resultItemId;
            this.displayName = displayName;
            this.requiredItems = requiredItems;
        }
    }

    public DesertTraderScreen() {
        super(Text.literal("沙漠商人"));
        initFixedItems();
        initRotatingItems();
        updateCurrentItems();
    }

    private void initFixedItems() {
        fixedItems.add(new TradeItem("ancient_treasure_decor", "古物宝藏", List.of(
            new Ingredient("omni_geode", 5)
        )));
        fixedItems.add(new TradeItem("warp_totem_desert", "传送图腾：沙漠", List.of(
            new Ingredient("omni_geode", 3)
        )));
        fixedItems.add(new TradeItem("triple_shot_espresso", "三倍浓缩咖啡", List.of(
            new Ingredient("diamond", 1)
        )));
        fixedItems.add(new TradeItem("spicy_eel", "香辣鳗鱼", List.of(
            new Ingredient("ruby", 1)
        )));
        fixedItems.add(new TradeItem("mega_bomb", "超级炸弹", List.of(
            new Ingredient("iridium_ore", 5)
        )));
        fixedItems.add(new TradeItem("bomb", "炸弹", List.of(
            new Ingredient("quartz", 5)
        )));
    }

    private void initRotatingItems() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                rotatingItems.add(new TradeItem("hay", "干草×3", List.of(
                    new Ingredient("omni_geode", 1)
                )));
                break;
            case Calendar.TUESDAY:
                rotatingItems.add(new TradeItem("fiber", "纤维", List.of(
                    new Ingredient("misc_stone", 5)
                )));
                break;
            case Calendar.WEDNESDAY:
                rotatingItems.add(new TradeItem("cloth", "布料", List.of(
                    new Ingredient("aquamarine", 3)
                )));
                break;
            case Calendar.THURSDAY:
                rotatingItems.add(new TradeItem("magic_rock_candy", "魔法糖冰棍", List.of(
                    new Ingredient("prismatic_shard", 3)
                )));
                break;
            case Calendar.FRIDAY:
                rotatingItems.add(new TradeItem("cheese", "奶酪", List.of(
                    new Ingredient("emerald", 1)
                )));
                break;
            case Calendar.SATURDAY:
                rotatingItems.add(new TradeItem("spring_seeds", "春季种子", List.of(
                    new Ingredient("summer_seeds", 2)
                )));
                rotatingItems.add(new TradeItem("summer_seeds", "夏季种子", List.of(
                    new Ingredient("fall_seeds", 2)
                )));
                rotatingItems.add(new TradeItem("fall_seeds", "秋季种子", List.of(
                    new Ingredient("winter_seeds", 2)
                )));
                rotatingItems.add(new TradeItem("winter_seeds", "冬季种子", List.of(
                    new Ingredient("spring_seeds", 2)
                )));
                break;
            case Calendar.SUNDAY:
                rotatingItems.add(new TradeItem("staircase", "楼梯", List.of(
                    new Ingredient("jade", 1)
                )));
                break;
        }
    }

    private void updateCurrentItems() {
        currentItems.clear();
        currentItems.addAll(currentTab == 0 ? fixedItems : rotatingItems);
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

            boolean canTrade = hasEnoughItems(item);

            context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, canTrade ? 0xFF333333 : 0xFF222222);
            context.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, canTrade ? 0xFF448844 : 0xFF333333);

            Item resultItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, item.resultItemId));
            if (resultItem != null) {
                context.drawItem(new ItemStack(resultItem), x + 8, y + 4);
            }

            if (!item.requiredItems.isEmpty()) {
                Ingredient ing = item.requiredItems.get(0);
                Item ingItem = CraftingMaterialSets.getItemForDisplay(ing.itemId);

                if (ingItem != null) {
                    var matrices = context.getMatrices();
                    matrices.pushMatrix();
                    matrices.translate(x + 20, y + 20);
                    matrices.scale(0.5f, 0.5f);
                    context.drawItem(new ItemStack(ingItem), -8, -8);
                    matrices.popMatrix();
                }

                int playerCount = CraftingMaterialSets.countItemInInventory(ing.itemId, client);
                int countColor = playerCount >= ing.count ? 0x88FF88 : 0xFF8888;
                context.drawText(textRenderer, Text.literal(String.valueOf(ing.count)), x + 24, y + 24, countColor, false);
            }

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
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
        context.disableScissor();

        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.desert_trader.your_inventory"), invGridX, invGridY - 12, 0xFFFFFF, false);

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

        int gridStartY = MARGIN_Y;
        for (int i = 0; i < currentItems.size(); i++) {
            TradeItem item = currentItems.get(i);
            if (!hasEnoughItems(item)) continue;

            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            if (mx >= x && mx < x + CELL_SIZE && my >= y && my < y + CELL_SIZE) {
                int tradeCount = 1;
                boolean shift = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                boolean ctrl = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (shift && ctrl) tradeCount = 999;
                else if (ctrl) tradeCount = 25;
                else if (shift) tradeCount = 5;

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

    private boolean hasEnoughItems(TradeItem item) {
        for (Ingredient ing : item.requiredItems) {
            if (CraftingMaterialSets.countItemInInventory(ing.itemId, client) < ing.count) return false;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
