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
import org.lwjgl.glfw.GLFW;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.season.Season;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GusShopScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int COLS = 4;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 30;
    private int invGridX;
    private int invGridY;

    private final List<GusShopItem> allItems = new ArrayList<>();

    private static class GusShopItem {
        public final String itemId;
        public final int price;
        public final ItemStack stack;

        public GusShopItem(String itemId, int price) {
            this.itemId = itemId;
            this.price = price;
            Item item = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, itemId));
            this.stack = item != null ? new ItemStack(item) : ItemStack.EMPTY;
        }
    }

    public GusShopScreen() {
        super(Text.literal("格斯酒吧"));
        // 固定商品（全年供应）
        allItems.add(new GusShopItem("beer", 400));
        allItems.add(new GusShopItem("salad", 220));
        allItems.add(new GusShopItem("bread", 120));
        allItems.add(new GusShopItem("spaghetti", 240));
        allItems.add(new GusShopItem("pizza", 600));
        allItems.add(new GusShopItem("coffee", 300));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());

        if (client != null && client.world != null) {
            // 夏季额外商品（啤酒已在基础商品中，不重复添加）
            if (Season.fromTimeOfDay(client.world.getTimeOfDay()) == Season.SUMMER) {
                allItems.add(new GusShopItem("tropical_curry", 600));
                allItems.add(new GusShopItem("pale_ale", 1000));
                allItems.add(new GusShopItem("mead", 800));
                allItems.add(new GusShopItem("cranberry_candy", 400));
                allItems.add(new GusShopItem("mango_wine_gold", 5000));
            }
        }

        int totalRows = (allItems.size() + COLS - 1) / COLS;
        invGridX = MARGIN_X;
        invGridY = MARGIN_Y + totalRows * CELL_SIZE + 30;
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

        int gridStartY = MARGIN_Y;
        for (int i = 0; i < allItems.size(); i++) {
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE;

            if (mx >= x && mx < x + CELL_SIZE && my >= y && my < y + CELL_SIZE) {
                GusShopItem shopItem = allItems.get(i);
                if (shopItem.stack.isEmpty()) return true;

                int buyCount = 1;
                boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
                boolean ctrl = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);
        int currentGold = ClockHudRenderer.getClientGold();

        // 标题
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("格斯酒吧 - 星落酒吧"), width / 2, 10, 0xFFFFAA);

        // 夏季商品提示
        if (client != null && client.world != null && Season.fromTimeOfDay(client.world.getTimeOfDay()) == Season.SUMMER) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("§e夏季额外供应!"), width / 2, 22, 0xFFFF55);
        }

        int gridStartY = MARGIN_Y;
        for (int i = 0; i < allItems.size(); i++) {
            GusShopItem shopItem = allItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE;

            boolean canAfford = currentGold >= shopItem.price;
            int bgColor = canAfford ? 0xFF333333 : 0xFF222222;
            int borderColor = canAfford ? 0xFF448844 : 0xFF333333;
            context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, bgColor);
            context.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, borderColor);

            if (!shopItem.stack.isEmpty()) {
                context.drawItem(shopItem.stack, x + 8, y + 4);
            }

            NumberRenderer.drawNumber(context, shopItem.price, x + 2, y + 22, 0.2f, true);

            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal(shopItem.stack.getName().getString()));
                tooltip.add(Text.literal(shopItem.price + "金"));
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }

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
