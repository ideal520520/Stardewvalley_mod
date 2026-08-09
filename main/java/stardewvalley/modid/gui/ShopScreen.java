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
import stardewvalley.modid.equipment.ClientBackpackData;
import stardewvalley.modid.season.Season;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShopScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");
    private static final Identifier BACKPACK_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/backpack.png");
    private static final Identifier BACKPACK36_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/36_backpack.png");

    private static final int COLS = 9;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 30;
    private int invGridX;
    private int invGridY;

    private int currentTab = 0;
    private final String[] TAB_NAMES = {"gui.stardewvalley.shop.tab_fertilizer_materials", "gui.stardewvalley.shop.tab_crop", "gui.stardewvalley.shop.tab_backpack"};
    private final int[] TAB_WIDTHS = {120, 58, 80};
    private List<ShopItem> currentItems = new ArrayList<>();
    private int scrollOffset = 0;

    public static class ShopItem {
        public final String itemId;
        public final int price;
        public final ItemStack stack;

        public ShopItem(String itemId, int price) {
            this.itemId = itemId;
            this.price = price;
            Item item = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, itemId));
            this.stack = item != null ? new ItemStack(item) : ItemStack.EMPTY;
        }
    }

    private static final Map<String, Integer> FERTILIZER_STOCK = new LinkedHashMap<>();
    private static final Map<String, Integer> CROP_STOCK = new LinkedHashMap<>();

    private static final Map<String, Season[]> CROP_SEASONS = new LinkedHashMap<>();

    static {
        FERTILIZER_STOCK.put("basic_fertilizer", 100);
        FERTILIZER_STOCK.put("quality_fertilizer", 150);
        FERTILIZER_STOCK.put("basic_retaining_soil", 100);
        FERTILIZER_STOCK.put("quality_retaining_soil", 150);
        FERTILIZER_STOCK.put("speed-gro", 100);
        FERTILIZER_STOCK.put("hyper_speed-gro", 150);
        FERTILIZER_STOCK.put("deluxe_speed-gro", 200);
        // 工匠制品
        FERTILIZER_STOCK.put("vinegar", 100);
        FERTILIZER_STOCK.put("wheat_flour", 100);
        FERTILIZER_STOCK.put("oil", 200);
        FERTILIZER_STOCK.put("sugar", 200);
        FERTILIZER_STOCK.put("rice", 200);

        // Spring
        CROP_STOCK.put("parsnip_seeds", 20);
        CROP_STOCK.put("beanstarter", 60);
        CROP_STOCK.put("cauliflower_seeds", 80);
        CROP_STOCK.put("potato_seeds", 50);
        CROP_STOCK.put("tulip_seeds", 20);
        CROP_STOCK.put("kale_seeds", 70);
        CROP_STOCK.put("bluejazz_seeds", 30);
        CROP_STOCK.put("garlic_seeds", 40);
        CROP_STOCK.put("unmilledrice_seeds", 40);
        // Summer
        CROP_STOCK.put("melon_seeds", 80);
        CROP_STOCK.put("tomato_seeds", 50);
        CROP_STOCK.put("blueberry_seeds", 80);
        CROP_STOCK.put("hotpepper_seeds", 40);
        CROP_STOCK.put("wheat_seeds", 10);
        CROP_STOCK.put("radish_seeds", 40);
        CROP_STOCK.put("poppy_seeds", 100);
        CROP_STOCK.put("summerspangle_seeds", 50);
        CROP_STOCK.put("hopsstarter", 60);
        CROP_STOCK.put("corn_seeds", 150);
        CROP_STOCK.put("sunflower_seeds", 200);
        CROP_STOCK.put("redcabbage_seeds", 100);
        // Fall
        CROP_STOCK.put("eggplant_seeds", 20);
        CROP_STOCK.put("pumpkin_seeds", 100);
        CROP_STOCK.put("bokchoy_seeds", 50);
        CROP_STOCK.put("yam_seeds", 60);
        CROP_STOCK.put("cranberries_seeds", 240);
        CROP_STOCK.put("fairyrose_seeds", 200);
        CROP_STOCK.put("amaranth_seeds", 70);
        CROP_STOCK.put("grapestarter", 60);
        CROP_STOCK.put("artichoke_seeds", 30);

        // Season mapping
        CROP_SEASONS.put("parsnip_seeds", new Season[]{Season.SPRING});
        CROP_SEASONS.put("beanstarter", new Season[]{Season.SPRING});
        CROP_SEASONS.put("cauliflower_seeds", new Season[]{Season.SPRING});
        CROP_SEASONS.put("potato_seeds", new Season[]{Season.SPRING});
        CROP_SEASONS.put("tulip_seeds", new Season[]{Season.SPRING});
        CROP_SEASONS.put("kale_seeds", new Season[]{Season.SPRING});
        CROP_SEASONS.put("bluejazz_seeds", new Season[]{Season.SPRING});
        CROP_SEASONS.put("garlic_seeds", new Season[]{Season.SPRING});
        CROP_SEASONS.put("unmilledrice_seeds", new Season[]{Season.SPRING});

        CROP_SEASONS.put("melon_seeds", new Season[]{Season.SUMMER});
        CROP_SEASONS.put("tomato_seeds", new Season[]{Season.SUMMER});
        CROP_SEASONS.put("blueberry_seeds", new Season[]{Season.SUMMER});
        CROP_SEASONS.put("hotpepper_seeds", new Season[]{Season.SUMMER});
        CROP_SEASONS.put("wheat_seeds", new Season[]{Season.SUMMER, Season.FALL});
        CROP_SEASONS.put("radish_seeds", new Season[]{Season.SUMMER});
        CROP_SEASONS.put("poppy_seeds", new Season[]{Season.SUMMER});
        CROP_SEASONS.put("summerspangle_seeds", new Season[]{Season.SUMMER});
        CROP_SEASONS.put("hopsstarter", new Season[]{Season.SUMMER});
        CROP_SEASONS.put("corn_seeds", new Season[]{Season.SUMMER, Season.FALL});
        CROP_SEASONS.put("sunflower_seeds", new Season[]{Season.SUMMER, Season.FALL});
        CROP_SEASONS.put("redcabbage_seeds", new Season[]{Season.SUMMER});

        CROP_SEASONS.put("eggplant_seeds", new Season[]{Season.FALL});
        CROP_SEASONS.put("pumpkin_seeds", new Season[]{Season.FALL});
        CROP_SEASONS.put("bokchoy_seeds", new Season[]{Season.FALL});
        CROP_SEASONS.put("yam_seeds", new Season[]{Season.FALL});
        CROP_SEASONS.put("cranberries_seeds", new Season[]{Season.FALL});
        CROP_SEASONS.put("fairyrose_seeds", new Season[]{Season.FALL});
        CROP_SEASONS.put("amaranth_seeds", new Season[]{Season.FALL});
        CROP_SEASONS.put("grapestarter", new Season[]{Season.FALL});
        CROP_SEASONS.put("artichoke_seeds", new Season[]{Season.FALL});
    }

    public static int getItemPrice(String itemId) {
        Integer price = FERTILIZER_STOCK.get(itemId);
        if (price != null) return price;
        price = CROP_STOCK.get(itemId);
        if (price != null) return price;
        return 0;
    }

    public ShopScreen() {
        super(Text.translatable("gui.stardewvalley.shop.title"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
        invGridX = MARGIN_X;
        invGridY = MARGIN_Y + 3 * CELL_SIZE + 30;
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.shop.back"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
        updateTabItems();
    }

    private void updateTabItems() {
        currentItems.clear();
        if (currentTab == 2) return; // Backpack tab uses custom rendering
        Map<String, Integer> stock = currentTab == 0 ? FERTILIZER_STOCK : CROP_STOCK;
        Season currentSeason = null;
        if (currentTab == 1 && client != null && client.world != null) {
            currentSeason = Season.fromTimeOfDay(client.world.getTimeOfDay());
        }
        for (Map.Entry<String, Integer> entry : stock.entrySet()) {
            if (currentTab == 1 && currentSeason != null) {
                Season[] seasons = CROP_SEASONS.get(entry.getKey());
                if (seasons == null) continue;
                boolean inSeason = false;
                for (Season s : seasons) {
                    if (s == currentSeason) { inSeason = true; break; }
                }
                if (!inSeason) continue;
            }
            currentItems.add(new ShopItem(entry.getKey(), entry.getValue()));
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

        if (currentTab == 2) {
            // Backpack tab - click on the upgrade slot
            int cellX = MARGIN_X;
            int cellY = MARGIN_Y;
            if (mx >= cellX && mx < cellX + CELL_SIZE && my >= cellY && my < cellY + CELL_SIZE) {
                int currentGold = ClockHudRenderer.getClientGold();
                int backpackLevel = ClientBackpackData.getBackpackLevel();
                if (backpackLevel < 1) {
                    // 购买第一个背包
                    if (currentGold >= 2000) {
                        ClientPlayNetworking.send(new ModPayloads.BackpackBuyC2SPayload("backpack"));
                    }
                } else if (backpackLevel < 2) {
                    // 购买第二个背包
                    if (currentGold >= 10000) {
                        ClientPlayNetworking.send(new ModPayloads.BackpackBuyC2SPayload("36_backpack"));
                    }
                }
                return true;
            }
            return false;
        }

        int gridStartY = MARGIN_Y;
        for (int i = 0; i < currentItems.size(); i++) {
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            if (mx >= x && mx < x + CELL_SIZE && my >= y && my < y + CELL_SIZE) {
                ShopItem shopItem = currentItems.get(i);
                if (shopItem.stack.isEmpty()) return true;

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
        if (currentTab == 2) return false;
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

        int currentGold = ClockHudRenderer.getClientGold();

        int tabPosX = MARGIN_X;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tw = TAB_WIDTHS[i];
            int color = i == currentTab ? 0xFF88FF88 : 0xFF888888;
            context.fill(tabPosX, 14, tabPosX + tw, 28, 0xFF333333);
            context.fill(tabPosX + 1, 15, tabPosX + tw - 1, 27, i == currentTab ? 0xFF446644 : 0xFF222222);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable(TAB_NAMES[i]), tabPosX + tw / 2, 17, color);
            tabPosX += tw + 6;
        }

        if (currentTab == 2) {
            // ─── 背包升级页签 ───
            int backpackLevel = ClientBackpackData.getBackpackLevel();
            boolean hasBackpack = backpackLevel >= 1;
            boolean hasBigBackpack = backpackLevel >= 2;

            int cellX = MARGIN_X;
            int cellY = MARGIN_Y;

            // 绘制物品格子
            if (hasBigBackpack) {
                // 已购买全部 - 显示已满
                context.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, 0xFF333333);
                context.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, 0xFF444444);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKPACK36_TEXTURE, cellX + 4, cellY + 2, 0.0f, 0.0f, 24, 24, 24, 24);
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("MAX"), cellX + CELL_SIZE / 2, cellY + CELL_SIZE - 10, 0xFFFFAA00);
            } else if (hasBackpack) {
                // 显示第二个背包 (36_backpack, 10000g)
                boolean canAfford = currentGold >= 10000;
                context.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, 0xFF333333);
                context.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, canAfford ? 0xFF448844 : 0xFF333333);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKPACK36_TEXTURE, cellX + 4, cellY + 2, 0.0f, 0.0f, 24, 24, 24, 24);
                NumberRenderer.drawNumber(context, 10000, cellX + 2, cellY + 22, 0.2f, true);
                // 第一行提示已购买
                context.drawText(textRenderer, Text.literal("背包(3行)"), cellX + CELL_SIZE + 4, cellY + 4, 0xFF88FF88, false);
                context.drawText(textRenderer, Text.literal("已拥有"), cellX + CELL_SIZE + 4, cellY + 14, 0xFFAAAAAA, false);
            } else {
                // 显示第一个背包 (backpack, 2000g)
                boolean canAfford = currentGold >= 2000;
                context.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, 0xFF333333);
                context.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, canAfford ? 0xFF448844 : 0xFF333333);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKPACK_TEXTURE, cellX + 4, cellY + 2, 0.0f, 0.0f, 24, 24, 24, 24);
                NumberRenderer.drawNumber(context, 2000, cellX + 2, cellY + 22, 0.2f, true);
                context.drawText(textRenderer, Text.literal("初始2行→3行"), cellX + CELL_SIZE + 4, cellY + 4, 0xFFFFFF, false);
            }

            // 悬浮提示
            if (mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE) {
                context.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                if (hasBigBackpack) {
                    tooltip.add(Text.literal("背包已满级 (4行)"));
                } else if (hasBackpack) {
                    tooltip.add(Text.literal("大型背包 - 10000g"));
                    tooltip.add(Text.literal("将扩展背包升级为4行"));
                } else {
                    tooltip.add(Text.literal("背包 - 2000g"));
                    tooltip.add(Text.literal("将扩展背包升级为3行"));
                }
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }

            // 说明文字
            context.drawText(textRenderer, Text.literal("当前背包等级: " + (backpackLevel + 2) + "行"), cellX, cellY + CELL_SIZE + 8, 0xFFFFFF, false);
        } else {
            // ─── 普通商品页签 ───
            int gridStartY = MARGIN_Y;
            context.enableScissor(0, gridStartY, width, height - gridStartY);
            for (int i = 0; i < currentItems.size(); i++) {
                ShopItem shopItem = currentItems.get(i);
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

                NumberRenderer.drawNumber(context, shopItem.price, x + 2, y + 22, 0.2f, true);

                if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                    context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                    List<Text> tooltip = new ArrayList<>();
                    tooltip.add(Text.literal(shopItem.stack.getName().getString()));
                    tooltip.add(Text.literal(shopItem.price + "g"));
                    context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                }
            }
            context.disableScissor();
        }

        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.shop.your_inventory"), invGridX, invGridY - 12, 0xFFFFFF, false);

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
