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
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.item.ModItems;
import stardewvalley.modid.season.FishingLootManager;
import stardewvalley.modid.season.Season;

import java.util.ArrayList;
import java.util.List;

public class FishShopScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int COLS = 8;
    private static final int CELL = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int GRID_Y = 35;
    private int invGridX;
    private int invGridY;

    private int scrollOffset = 0;
    private int guideScrollOffset = 0;
    private int currentTab = 0;

    private static class FishShopEntry {
        String itemId;
        String label;
        int price;
        int requiredLevel;
        boolean isTodo;

        FishShopEntry(String itemId, String label, int price, int requiredLevel, boolean isTodo) {
            this.itemId = itemId;
            this.label = label;
            this.price = price;
            this.requiredLevel = requiredLevel;
            this.isTodo = isTodo;
        }
    }

    private static final List<FishShopEntry> ALL_ITEMS = new ArrayList<>();

    static {
        ALL_ITEMS.add(new FishShopEntry("trout_soup", "鳟鱼汤", 250, 0, false));
        ALL_ITEMS.add(new FishShopEntry("bait_bait", "鱼饵", 5, 2, false));
        ALL_ITEMS.add(new FishShopEntry("bait_deluxe_bait", "高级鱼饵", 100, 4, false));
        ALL_ITEMS.add(new FishShopEntry("crab_pot", "蟹笼", 1500, 3, false));
        ALL_ITEMS.add(new FishShopEntry("fish_smoker", "熏鱼机", 5000, 0, false));
        ALL_ITEMS.add(new FishShopEntry("fishtool_sonar_bobber", "声纳浮漂", 500, 6, false));
        ALL_ITEMS.add(new FishShopEntry("fishtool_spinner", "旋式鱼饵", 500, 6, false));
        ALL_ITEMS.add(new FishShopEntry("fishtool_trap_bobber", "陷阱浮标", 500, 6, false));
        ALL_ITEMS.add(new FishShopEntry("fishtool_lead_bobber", "铅制浮标", 200, 6, false));
        ALL_ITEMS.add(new FishShopEntry("fishtool_treasure_hunter", "寻宝者", 750, 7, false));
        ALL_ITEMS.add(new FishShopEntry("fishtool_cork_bobber", "软木塞浮标", 750, 7, false));
        ALL_ITEMS.add(new FishShopEntry("fishtool_barbed_hook", "倒刺钩", 1000, 8, false));
        ALL_ITEMS.add(new FishShopEntry("fishtool_dressed_spinner", "精装旋式鱼饵", 1000, 8, false));
        ALL_ITEMS.add(new FishShopEntry("bait_magnet", "磁铁", 1000, 9, false));
        ALL_ITEMS.add(new FishShopEntry("bamboo_pole", "竹鱼竿", 500, 0, false));
        ALL_ITEMS.add(new FishShopEntry("fiberglass_rod", "玻璃纤维鱼竿", 1800, 2, false));
        ALL_ITEMS.add(new FishShopEntry("iridium_rod", "铱金鱼竿", 7500, 6, false));
        ALL_ITEMS.add(new FishShopEntry("advanced_iridium_rod", "高级铱金鱼竿", 25000, 0, false));
    }

    /** 存储当日选中的鱼饵信息 */
    private String dailyBaitId = null;
    private int dailyBaitStock = 0;
    private long dailyBaitSeed = -1;

    private List<FishShopEntry> visibleItems = new ArrayList<>();

    public FishShopScreen() {
        super(Text.translatable("gui.stardewvalley.fish_shop.title"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
        invGridX = MARGIN_X;
        invGridY = this.height - 100;
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.fish_shop.back"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());

        refreshVisibleItems();
    }

    private void refreshVisibleItems() {
        visibleItems.clear();
        int fishingLevel = ClockHudRenderer.getClientFishingLevel();
        for (FishShopEntry entry : ALL_ITEMS) {
            if (fishingLevel >= entry.requiredLevel) {
                visibleItems.add(entry);
            }
        }

        // 每天只卖一种鱼的针对性鱼饵（根据当前游戏天数固定选择）
        Season currentSeason = Season.getCachedSeason();
        List<String> seasonalFish = new ArrayList<>();
        for (FishingLootManager.FishEntry fish : FishingLootManager.ALL_FISH) {
            if (fish.disabled() || !fish.isRealFish()) continue;
            if (fish.seasons() != null && fish.seasons().contains(currentSeason)) {
                String itemName = fish.itemName();
                String fishName = itemName.startsWith("fish_") ? itemName.substring(5) : itemName;
                String baitId = "bait_" + fishName + "_bait";
                if (ModItems.ITEMS.containsKey(baitId)) {
                    seasonalFish.add(fishName);
                }
            }
        }

        if (!seasonalFish.isEmpty()) {
            // 用当前游戏天数作为种子，确保同一天固定一种鱼
            long daySeed = 0;
            if (client != null && client.world != null) {
                daySeed = client.world.getTime() / 24000L;
            }
            if (daySeed != dailyBaitSeed) {
                dailyBaitSeed = daySeed;
                java.util.Random dailyRand = new java.util.Random(daySeed);
                String fishName = seasonalFish.get(dailyRand.nextInt(seasonalFish.size()));
                dailyBaitId = "bait_" + fishName + "_bait";
                dailyBaitStock = 8 + dailyRand.nextInt(5); // 8-12
            }

            if (dailyBaitId != null && dailyBaitStock > 0) {
                Item baitItem = ModItems.ITEMS.get(dailyBaitId);
                if (baitItem != null) {
                    int basePrice = stardewvalley.modid.gui.GoldManager.getItemMoneyValue(Identifier.of(StardewValley.MOD_ID, dailyBaitId));
                    int shopPrice = basePrice * 2;
                    Text displayName = Text.translatable("item.stardewvalley." + dailyBaitId);
                    visibleItems.add(new FishShopEntry(dailyBaitId, displayName.getString(), shopPrice, 0, false));
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        // 标签栏
        if (my >= 10 && my < 28) {
            if (mx >= MARGIN_X && mx < MARGIN_X + 60) currentTab = 0;
            if (mx >= MARGIN_X + 66 && mx < MARGIN_X + 116) currentTab = 1;
            scrollOffset = 0;
            guideScrollOffset = 0;
            refreshVisibleItems();
            return true;
        }

        if (currentTab == 0) {
            // 点击商品购买
            for (int i = 0; i < visibleItems.size(); i++) {
                FishShopEntry entry = visibleItems.get(i);
                int row = i / COLS;
                int col = i % COLS;
                int x = MARGIN_X + col * CELL;
                int y = GRID_Y + row * CELL - scrollOffset;

                if (y < GRID_Y - CELL || y > GRID_Y + 6 * CELL) continue;

                if (mx >= x && mx < x + CELL && my >= y && my < y + CELL) {
                    if (entry.isTodo) return true;
                    int currentGold = ClockHudRenderer.getClientGold();
                    if (currentGold >= entry.price) {
                        // 针对性鱼饵：判断库存
                        if (entry.itemId.equals(dailyBaitId)) {
                            if (dailyBaitStock <= 0) return true;
                            dailyBaitStock--;
                        }
                        ClientPlayNetworking.send(new ModPayloads.FishShopBuyC2SPayload(entry.itemId, 1));
                    }
                    return true;
                }
            }
        }

        // 点击背包格子出售（任何标签页下都可用）
        for (int slot = 0; slot < 36; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int sx = invGridX + col * SLOT_SIZE;
            int sy = invGridY + row * SLOT_SIZE;
            if (mx >= sx && mx < sx + SLOT_SIZE && my >= sy && my < sy + SLOT_SIZE) {
                int invIndex = slot < 9 ? slot + 27 : slot - 9;
                if (client == null || client.player == null) return true;
                ItemStack stack = client.player.getInventory().getStack(invIndex);
                if (stack.isEmpty()) return true;
                if (!stardewvalley.modid.skill.SkillEffectHelper.isFishItem(Registries.ITEM.getId(stack.getItem()))) return true;
                int count = getClickSellCount(stack.getCount());
                ClientPlayNetworking.send(new ModPayloads.FishShopSellC2SPayload(slot, count));
                return true;
            }
        }
        return false;
    }

    private int getClickSellCount(int stackSize) {
        if (client == null) return 1;
        boolean shift = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        boolean ctrl = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        if (shift && ctrl) return stackSize;
        if (ctrl) return Math.min(25, stackSize);
        if (shift) return Math.min(5, stackSize);
        return 1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentTab == 0) {
            int rows = (visibleItems.size() + COLS - 1) / COLS;
            int maxScroll = Math.max(0, rows * CELL - 6 * CELL);
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * CELL));
        } else {
            guideScrollOffset = (int) Math.max(0, guideScrollOffset - verticalAmount * 22);
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        int currentGold = ClockHudRenderer.getClientGold();

        // 标签栏
        int tc0 = currentTab == 0 ? 0xFF88FF88 : 0xFF888888;
        int tc1 = currentTab == 1 ? 0xFF88FF88 : 0xFF888888;
        context.fill(MARGIN_X, 10, MARGIN_X + 60, 28, 0xFF333333);
        context.fill(MARGIN_X + 1, 11, MARGIN_X + 59, 27, currentTab == 0 ? 0xFF446644 : 0xFF222222);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.stardewvalley.fish_shop.buy"), MARGIN_X + 30, 14, tc0);
        context.fill(MARGIN_X + 66, 10, MARGIN_X + 116, 28, 0xFF333333);
        context.fill(MARGIN_X + 67, 11, MARGIN_X + 115, 27, currentTab == 1 ? 0xFF446644 : 0xFF222222);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.stardewvalley.fish_shop.guide"), MARGIN_X + 91, 14, tc1);

        // 钓鱼等级
        int fishLevel = ClockHudRenderer.getClientFishingLevel();
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.fish_shop.fishing_level", fishLevel), MARGIN_X, 31, 0xFFFFAA, false);

        if (currentTab == 0) {
            // 渲染商品
            context.enableScissor(0, GRID_Y, width, height - GRID_Y);
            for (int i = 0; i < visibleItems.size(); i++) {
                FishShopEntry entry = visibleItems.get(i);
                int row = i / COLS;
                int col = i % COLS;
                int x = MARGIN_X + col * CELL;
                int y = GRID_Y + row * CELL - scrollOffset;

                if (y < GRID_Y - CELL || y > GRID_Y + 6 * CELL) continue;

                boolean canAfford = !entry.isTodo && currentGold >= entry.price;
                boolean isBaitItem = entry.itemId.equals(dailyBaitId);
                boolean soldOut = isBaitItem && dailyBaitStock <= 0;

                int bgColor = soldOut ? 0xFF222222 : (canAfford ? 0xFF333333 : 0xFF222222);
                int borderColor = soldOut ? 0xFF111111 : (canAfford ? 0xFF448844 : 0xFF333333);
                context.fill(x, y, x + CELL, y + CELL, bgColor);
                context.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, borderColor);

                if (!soldOut) {
                    Identifier itemId = Identifier.of(StardewValley.MOD_ID, entry.itemId);
                    Item item = Registries.ITEM.get(itemId);
                    if (item != null && item != net.minecraft.item.Items.AIR) {
                        context.drawItem(new ItemStack(item), x + 8, y + 1);
                    }
                    if (!entry.isTodo) {
                        NumberRenderer.drawNumber(context, entry.price, x + 1, y + 20, 0.16f, true);
                        if (isBaitItem) {
                            context.drawText(textRenderer, Text.literal("x" + dailyBaitStock), x + 17, y + 21, 0xFFFFAA, false);
                        }
                    }
                }

                if (soldOut) {
                    context.drawCenteredTextWithShadow(textRenderer, Text.literal("已卖光"), x + CELL / 2, y + CELL / 2 - 4, 0xFF4444);
                }

                if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                    context.fill(x, y, x + CELL, y + CELL, 0x44FFFFFF);
                    List<Text> tooltip = new ArrayList<>();
                    tooltip.add(Text.literal(entry.label));
                    if (entry.isTodo) {
                        tooltip.add(Text.literal("§7（待添加）"));
                    } else if (soldOut) {
                        tooltip.add(Text.literal("§c已卖光"));
                    } else {
                        String extra = isBaitItem ? ("剩余: " + dailyBaitStock + "个") : "";
                        tooltip.add(Text.literal(entry.price + "g" + (extra.isEmpty() ? "" : "  " + extra)));
                        if (entry.requiredLevel > 0) {
                            tooltip.add(Text.literal("需要钓鱼等级: " + entry.requiredLevel));
                        }
                    }
                    context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                }
            }
            context.disableScissor();
        } else {
            // Guide 页：显示所有可出售鱼类物品的贴图（排序后排列）
            context.drawText(textRenderer, Text.literal("可在鱼店出售的鱼类物品："), MARGIN_X, 35, 0xFFFFFF, false);
            java.util.List<net.minecraft.item.Item> fishItems = new java.util.ArrayList<>();
            for (var entry : net.minecraft.registry.Registries.ITEM.getEntrySet()) {
                if (entry.getKey().getValue().getNamespace().equals(StardewValley.MOD_ID)
                    && stardewvalley.modid.skill.SkillEffectHelper.isFishItem(entry.getKey().getValue())) {
                    fishItems.add(entry.getValue());
                }
            }
            fishItems.sort((a, b) -> {
                String pa = net.minecraft.registry.Registries.ITEM.getId(a).getPath();
                String pb = net.minecraft.registry.Registries.ITEM.getId(b).getPath();
                return pa.compareTo(pb);
            });
            int cols = 18;
            int CELL_G = 22;
            context.enableScissor(0, 40, width, height - 40);
            for (int idx = 0; idx < fishItems.size(); idx++) {
                int ix = MARGIN_X + (idx % cols) * CELL_G;
                int iy = 50 + (idx / cols) * CELL_G - guideScrollOffset;
                if (iy < 40 || iy > height) continue;
                context.drawItem(new ItemStack(fishItems.get(idx)), ix, iy);
            }
            context.disableScissor();
        }

        // 背包
        renderInventory(context);

        // 金币显示
        int gold = ClockHudRenderer.getClientGold();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GOLD_TEXTURE, MARGIN_X, height - 20, 0.0f, 0.0f, 16, 16, 16, 16);
        NumberRenderer.drawNumber(context, gold, MARGIN_X + 18, height - 16, 0.255f, true);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderInventory(DrawContext context) {
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.fish_shop.inventory"), invGridX, invGridY - 12, 0xFFFFFF, false);
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
                Identifier id = Registries.ITEM.getId(stack.getItem());
                if (id.getNamespace().equals(StardewValley.MOD_ID) && stardewvalley.modid.skill.SkillEffectHelper.isFishItem(id)) {
                    context.fill(sx, sy, sx + SLOT_SIZE, sy + 1, 0xFF00DD00);
                }
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
