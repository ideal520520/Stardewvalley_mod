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
import stardewvalley.modid.equipment.ClientTrashCanData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OreShopScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    private static final int CELL = 32;
    private static final int SLOT_SIZE = 18;
    private static final int INV_MARGIN_X = 8;
    private int invGridX;
    private int invGridY;

    private static final String[] BUY_ITEMS = {"coal", "copper_ore", "iron_ore", "gold_ore", "iridium_ore"};
    private static final String[] BUY_LABELS = {"gui.stardewvalley.ore_shop.buy_coal", "gui.stardewvalley.ore_shop.buy_copper", "gui.stardewvalley.ore_shop.buy_iron", "gui.stardewvalley.ore_shop.buy_gold", "gui.stardewvalley.ore_shop.buy_iridium"};

    // 加工服务可处理物品
    private static final String[] PROCESS_ITEMS = {"geode", "frozen_geode", "magma_geode", "omni_geode", "ancient_treasure_decor", "golden_coconut", "mystery_box", "golden_mystery_box"};
    private static final String[] PROCESS_LABELS = {"gui.stardewvalley.ore_shop.process_geode", "gui.stardewvalley.ore_shop.process_frozen_geode", "gui.stardewvalley.ore_shop.process_magma_geode", "gui.stardewvalley.ore_shop.process_omni_geode", "gui.stardewvalley.ore_shop.process_artifact", "gui.stardewvalley.ore_shop.process_golden_coconut", "gui.stardewvalley.ore_shop.process_mystery_box", "gui.stardewvalley.ore_shop.process_golden_mystery_box"};

    private int currentTab = 0;

    private boolean upgradeInProgress = false;
    private String upgradeToolType = "";
    private String upgradeTargetTier = "";
    private long upgradeCompleteTime = 0;
    private int guideScrollOffset = 0;

    private static final String[] TOOL_TYPES = {"hoe", "pickaxe", "axe", "watering_can"};

    // Cached tool upgrade info, scanned once on open
    private static class ToolUpgradeInfo {
        String nextTier;
        int costGold;
        Item nextItem;
        String barItemId;
        boolean hasTool;

        ToolUpgradeInfo(String nextTier, int costGold, Item nextItem, String barItemId, boolean hasTool) {
            this.nextTier = nextTier;
            this.costGold = costGold;
            this.nextItem = nextItem;
            this.barItemId = barItemId;
            this.hasTool = hasTool;
        }
    }

    private final Map<String, ToolUpgradeInfo> toolUpgradeCache = new LinkedHashMap<>();

    private static final java.util.Set<String> SELLABLE_ITEMS = java.util.Set.of(
        "coal", "copper_ore", "iron_ore", "gold_ore", "iridium_ore",
        "copper_bar", "iron_bar", "gold_bar", "iridium_bar",
        "amethyst", "aquamarine", "diamond", "emerald", "jade", "ruby", "topaz",
        "prismatic_shard", "geode", "frozen_geode", "magma_geode", "omni_geode"
    );

    private static final String[] TRASH_TIER_ITEMS = {"copper_bar", "iron_bar", "gold_bar", "iridium_bar"};
    private static final int[] TRASH_COSTS = {1000, 2500, 5000, 12500};
    private static final String[] TRASH_LABELS = {"gui.stardewvalley.ore_shop.tier_copper", "gui.stardewvalley.ore_shop.tier_steel", "gui.stardewvalley.ore_shop.tier_gold", "gui.stardewvalley.ore_shop.tier_iridium"};
    private static final Identifier[] TRASH_TEXTURES = {
        Identifier.of(StardewValley.MOD_ID, "textures/gui/Trash_Can_Copper.png"),
        Identifier.of(StardewValley.MOD_ID, "textures/gui/Trash_Can_Steel.png"),
        Identifier.of(StardewValley.MOD_ID, "textures/gui/Trash_Can_Gold.png"),
        Identifier.of(StardewValley.MOD_ID, "textures/gui/Trash_Can_Iridium.png"),
    };

    public OreShopScreen() {
        super(Text.translatable("gui.stardewvalley.ore_shop.title"));
    }

    @Override
    protected void init() {
        invGridX = INV_MARGIN_X;
        invGridY = this.height - 100;
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.ToolUpgradeStatusRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.TrashCanLevelRequestC2SPayload());
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.ore_shop.back"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());

        scanToolUpgrades();
    }

    private void scanToolUpgrades() {
        toolUpgradeCache.clear();
        if (client == null || client.player == null) return;

        String[] tierOrder = {"iridium", "gold", "steel", "copper", "normal"};
        for (String toolType : TOOL_TYPES) {
            String currentTier = "normal";
            for (String tier : tierOrder) {
                String name = tier.equals("normal") ? toolType : tier + "_" + toolType;
                Identifier id = Identifier.of(StardewValley.MOD_ID, name);
                Item item = Registries.ITEM.get(id);
                if (item == null) continue;
                boolean found = false;
                for (int si = 0; si < client.player.getInventory().size(); si++) {
                    if (client.player.getInventory().getStack(si).getItem() == item) {
                        currentTier = tier;
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }

            String nextTier = OreToolUpgradeManager.getNextTier(currentTier);
            if (nextTier == null) {
                toolUpgradeCache.put(toolType, new ToolUpgradeInfo(null, 0, null, null, false));
                continue;
            }

            String nextName = nextTier + "_" + toolType;
            Item nextItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, nextName));
            int costGold = OreToolUpgradeManager.getRequiredGold(nextTier);
            String barId = OreToolUpgradeManager.getRequiredBar(nextTier);
            toolUpgradeCache.put(toolType, new ToolUpgradeInfo(nextTier, costGold, nextItem, barId, true));
        }
    }

    public void setToolUpgradeStatus(boolean upgrading, String toolType, String targetTier, long completeTime) {
        this.upgradeInProgress = upgrading;
        this.upgradeToolType = toolType;
        this.upgradeTargetTier = targetTier;
        this.upgradeCompleteTime = completeTime;
        if (!upgrading) {
            scanToolUpgrades();
        }
    }

    private boolean isSecondYear() {
        if (client == null || client.world == null) return false;
        return client.world.getTimeOfDay() / 24000L > 72;
    }

    private int getBuyPrice(String itemId) {
        return switch (itemId) {
            case "coal" -> isSecondYear() ? 250 : 150;
            case "copper_ore" -> isSecondYear() ? 150 : 75;
            case "iron_ore" -> isSecondYear() ? 250 : 150;
            case "gold_ore" -> isSecondYear() ? 750 : 400;
            case "iridium_ore" -> isSecondYear() ? 1200 : 600;
            default -> 0;
        };
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        if (my >= 10 && my < 24) {
            if (mx >= INV_MARGIN_X && mx < INV_MARGIN_X + 60) { currentTab = 0; guideScrollOffset = 0; }
            if (mx >= INV_MARGIN_X + 62 && mx < INV_MARGIN_X + 122) { currentTab = 1; guideScrollOffset = 0; }
            if (mx >= INV_MARGIN_X + 124 && mx < INV_MARGIN_X + 184) { currentTab = 2; guideScrollOffset = 0; }
            if (mx >= INV_MARGIN_X + 186 && mx < INV_MARGIN_X + 246) { currentTab = 3; guideScrollOffset = 0; }
            return true;
        }

        if (currentTab == 0) {
            for (int i = 0; i < BUY_ITEMS.length; i++) {
                int x = INV_MARGIN_X + i * CELL;
                int y = 32;
                if (mx >= x && mx < x + CELL && my >= y && my < y + CELL) {
                    int count = getClickCount();
                    int price = getBuyPrice(BUY_ITEMS[i]);
                    if (ClockHudRenderer.getClientGold() >= price * count) {
                        ClientPlayNetworking.send(new ModPayloads.OreShopBuyC2SPayload(BUY_ITEMS[i], count));
                    }
                    return true;
                }
            }
        }

        if (currentTab == 3) {
            for (int i = 0; i < PROCESS_ITEMS.length; i++) {
                int col = i % 4;
                int row = i / 4;
                int x = INV_MARGIN_X + col * CELL;
                int y = 32 + row * (CELL + 4);
                if (mx >= x && mx < x + CELL && my >= y && my < y + CELL) {
                    ClientPlayNetworking.send(new ModPayloads.GeodeProcessC2SPayload(PROCESS_ITEMS[i]));
                    return true;
                }
            }
        }

        // Click inventory slot to sell (any tab)
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
                Identifier itemId = Registries.ITEM.getId(stack.getItem());
                if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return true;
                if (!SELLABLE_ITEMS.contains(itemId.getPath())) return true;
                int count = getClickSellCount(stack.getCount());
                ClientPlayNetworking.send(new ModPayloads.OreShopSellC2SPayload(slot, count));
                return true;
            }
        }

        if (currentTab == 1) {
            if (upgradeInProgress) {
                return true;
            }
            for (int ti = 0; ti < TOOL_TYPES.length; ti++) {
                int x = INV_MARGIN_X + ti * (CELL + 8);
                int y = 50;
                if (mx >= x && mx < x + CELL && my >= y && my < y + CELL) {
                    ToolUpgradeInfo info = toolUpgradeCache.get(TOOL_TYPES[ti]);
                    if (info != null && info.nextTier != null) {
                        ClientPlayNetworking.send(new ModPayloads.ToolUpgradeRequestC2SPayload(TOOL_TYPES[ti]));
                    }
                    return true;
                }
            }
            // 垃圾桶升级点击
            int trashCol = 0;
            for (String toolType : TOOL_TYPES) {
                ToolUpgradeInfo info = toolUpgradeCache.get(toolType);
                if (info == null || !info.hasTool || info.nextTier == null) { trashCol++; continue; }
                trashCol++;
            }
            int trashLevel = ClientTrashCanData.getTrashLevel();
            if (trashLevel < 4) {
                int bx = INV_MARGIN_X + trashCol * (CELL + 8);
                int by = 50;
                if (mx >= bx && mx < bx + CELL && my >= by && my < by + CELL) {
                    ClientPlayNetworking.send(new ModPayloads.TrashCanUpgradeC2SPayload());
                    return true;
                }
            }
        }
        return false;
    }

    private int getClickCount() {
        if (client == null) return 1;
        boolean shift = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        boolean ctrl = org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        if (shift && ctrl) return 999;
        if (ctrl) return 25;
        if (shift) return 5;
        return 1;
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
        if (currentTab == 2) {
            guideScrollOffset = (int) Math.max(0, guideScrollOffset - verticalAmount * 22);
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        int currentGold = ClockHudRenderer.getClientGold();

        int tc0 = currentTab == 0 ? 0xFF88FF88 : 0xFF888888;
        int tc1 = currentTab == 1 ? 0xFF88FF88 : 0xFF888888;
        int tc2 = currentTab == 2 ? 0xFF88FF88 : 0xFF888888;
        int tc3 = currentTab == 3 ? 0xFF88FF88 : 0xFF888888;
        context.fill(INV_MARGIN_X, 10, INV_MARGIN_X + 60, 24, 0xFF333333);
        context.fill(INV_MARGIN_X + 1, 11, INV_MARGIN_X + 59, 23, currentTab == 0 ? 0xFF446644 : 0xFF222222);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.ores"), INV_MARGIN_X + 30, 13, tc0);
        context.fill(INV_MARGIN_X + 62, 10, INV_MARGIN_X + 122, 24, 0xFF333333);
        context.fill(INV_MARGIN_X + 63, 11, INV_MARGIN_X + 121, 23, currentTab == 1 ? 0xFF446644 : 0xFF222222);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.upgrade"), INV_MARGIN_X + 92, 13, tc1);
        context.fill(INV_MARGIN_X + 124, 10, INV_MARGIN_X + 184, 24, 0xFF333333);
        context.fill(INV_MARGIN_X + 125, 11, INV_MARGIN_X + 183, 23, currentTab == 2 ? 0xFF446644 : 0xFF222222);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.guide"), INV_MARGIN_X + 154, 13, tc2);
        context.fill(INV_MARGIN_X + 186, 10, INV_MARGIN_X + 246, 24, 0xFF333333);
        context.fill(INV_MARGIN_X + 187, 11, INV_MARGIN_X + 245, 23, currentTab == 3 ? 0xFF446644 : 0xFF222222);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.process"), INV_MARGIN_X + 216, 13, tc3);

        if (currentTab == 0) {
            renderOres(context, mouseX, mouseY, currentGold);
        } else if (currentTab == 1) {
            renderUpgrades(context, mouseX, mouseY, currentGold);
        } else if (currentTab == 2) {
            renderGuide(context);
        } else if (currentTab == 3) {
            renderProcess(context, mouseX, mouseY, currentGold);
        }

        renderInventory(context);

        int gold = ClockHudRenderer.getClientGold();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GOLD_TEXTURE, INV_MARGIN_X, height - 20, 0.0f, 0.0f, 16, 16, 16, 16);
        NumberRenderer.drawNumber(context, gold, INV_MARGIN_X + 18, height - 16, 0.255f, true);
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderOres(DrawContext context, int mouseX, int mouseY, int currentGold) {
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.sell_hint"), INV_MARGIN_X, 70, 0xAAAAAA, false);

        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.buy"), INV_MARGIN_X, 24, 0xFFFFFF, false);
        for (int i = 0; i < BUY_ITEMS.length; i++) {
            int x = INV_MARGIN_X + i * CELL;
            int y = 32;
            int price = getBuyPrice(BUY_ITEMS[i]);
            drawCell(context, x, y, BUY_ITEMS[i], price, currentGold >= price);
            if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                context.fill(x, y, x + CELL, y + CELL, 0x44FFFFFF);
                context.drawTooltip(textRenderer, List.of(Text.translatable(BUY_LABELS[i]), Text.literal(getBuyPrice(BUY_ITEMS[i]) + "g")), mouseX, mouseY);
            }
        }
    }

    private void drawCell(DrawContext context, int x, int y, String itemId, int price, boolean canAfford) {
        if (canAfford) {
            context.fill(x, y, x + CELL, y + CELL, 0xFF333333);
            context.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF448844);
        } else {
            context.fill(x, y, x + CELL, y + CELL, 0xFF222222);
            context.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF333333);
        }
        Identifier id = Identifier.of(StardewValley.MOD_ID, itemId);
        var item = Registries.ITEM.get(id);
        if (item != null) {
            context.drawItem(new ItemStack(item), x + 8, y + 2);
        }
        NumberRenderer.drawNumber(context, price, x + 1, y + 20, 0.18f, true);
    }

    private static final String[] GUIDE_ITEMS = {
        "coal", "copper_ore", "iron_ore", "gold_ore", "iridium_ore",
        "copper_bar", "iron_bar", "gold_bar", "iridium_bar",
        "amethyst", "aquamarine", "diamond", "emerald", "jade", "ruby", "topaz",
        "prismatic_shard", "geode", "frozen_geode", "magma_geode", "omni_geode"
    };

    private void renderGuide(DrawContext context) {
        context.drawText(textRenderer, Text.literal("可在此出售的物品："), INV_MARGIN_X, 22, 0xFFFFFF, false);
        int cols = 18;
        int CELL_G = 22;
        for (int idx = 0; idx < GUIDE_ITEMS.length; idx++) {
            int ix = INV_MARGIN_X + (idx % cols) * CELL_G;
            int iy = 38 + (idx / cols) * CELL_G - guideScrollOffset;
            if (iy < 20 || iy > height) continue;
            Identifier id = Identifier.of(StardewValley.MOD_ID, GUIDE_ITEMS[idx]);
            var item = Registries.ITEM.get(id);
            if (item != null && item != net.minecraft.item.Items.AIR) {
                context.drawItem(new ItemStack(item), ix, iy);
            }
        }
    }

    private void renderProcess(DrawContext context, int mouseX, int mouseY, int currentGold) {
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.process_hint"), INV_MARGIN_X, 24, 0xFFFFFF, false);
        for (int i = 0; i < PROCESS_ITEMS.length; i++) {
            int col = i % 4;
            int row = i / 4;
            int x = INV_MARGIN_X + col * CELL;
            int y = 32 + row * (CELL + 4);
            boolean hasItem = hasItemInInventory(PROCESS_ITEMS[i]);
            boolean canAfford = currentGold >= 25 && hasItem;
            if (canAfford) {
                context.fill(x, y, x + CELL, y + CELL, 0xFF333333);
                context.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF448844);
            } else {
                context.fill(x, y, x + CELL, y + CELL, 0xFF222222);
                context.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF333333);
            }
            Identifier id = Identifier.of(StardewValley.MOD_ID, PROCESS_ITEMS[i]);
            var item = Registries.ITEM.get(id);
            if (item != null && item != net.minecraft.item.Items.AIR) {
                context.drawItem(new ItemStack(item), x + 8, y + 2);
            }
            NumberRenderer.drawNumber(context, 25, x + 1, y + 20, 0.18f, true);
            if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                context.fill(x, y, x + CELL, y + CELL, 0x44FFFFFF);
                context.drawTooltip(textRenderer, List.of(
                    Text.translatable(PROCESS_LABELS[i]),
                    Text.literal("25g")
                ), mouseX, mouseY);
            }
        }
    }

    private boolean hasItemInInventory(String itemId) {
        if (client == null || client.player == null) return false;
        Identifier id = Identifier.of(StardewValley.MOD_ID, itemId);
        net.minecraft.item.Item targetItem = Registries.ITEM.get(id);
        if (targetItem == null || targetItem == net.minecraft.item.Items.AIR) return false;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            if (client.player.getInventory().getStack(i).getItem() == targetItem) return true;
        }
        return false;
    }

    private void renderUpgrades(DrawContext context, int mouseX, int mouseY, int currentGold) {
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.tool_upgrades"), INV_MARGIN_X, 30, 0xFFFFFF, false);

        if (upgradeInProgress) {
            long currentTime = client.world != null ? client.world.getTimeOfDay() : 0;
            long remaining = upgradeCompleteTime - currentTime;
            int days = (int) Math.max(0, (remaining + 24000L - 1) / 24000L);
            String label;
            if ("trash_can".equals(upgradeToolType)) {
                String tierLabel = switch (upgradeTargetTier) {
                    case "1" -> "铜"; case "2" -> "钢"; case "3" -> "金"; case "4" -> "铱";
                    default -> upgradeTargetTier;
                };
                label = "垃圾桶 → " + tierLabel;
            } else {
                String toolLabel = switch (upgradeToolType) {
                    case "hoe" -> "锄头"; case "pickaxe" -> "镐"; case "axe" -> "斧头";
                    case "watering_can" -> "水壶"; default -> upgradeToolType;
                };
                String tierLabel = switch (upgradeTargetTier) {
                    case "copper" -> "铜"; case "steel" -> "钢"; case "gold" -> "金"; case "iridium" -> "铱";
                    default -> upgradeTargetTier;
                };
                label = toolLabel + " → " + tierLabel;
            }
            context.drawText(textRenderer, Text.literal(label), INV_MARGIN_X, 50, 0xFFFFAA, false);
            if (days <= 0) {
                context.drawText(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.ready"), INV_MARGIN_X, 70, 0x88FF88, false);
            } else {
                context.drawText(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.days_left", days), INV_MARGIN_X, 70, 0xFF8888, false);
            }
            return;
        }

        int col = 0;
        for (String toolType : TOOL_TYPES) {
            ToolUpgradeInfo info = toolUpgradeCache.get(toolType);
            if (info == null || !info.hasTool || info.nextTier == null) {
                col++;
                continue;
            }

            int x = INV_MARGIN_X + col * (CELL + 8);
            int y = 50;

            boolean hasBars = info.barItemId != null && countInventoryItem(info.barItemId) >= 5;
            boolean canAfford = currentGold >= info.costGold && hasBars;
            if (canAfford) {
                context.fill(x, y, x + CELL, y + CELL, 0xFF333333);
                context.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF448844);
            } else {
                context.fill(x, y, x + CELL, y + CELL, 0xFF222222);
                context.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xFF333333);
            }

            context.drawItem(new ItemStack(info.nextItem), x + 8, y + 2);

            context.drawText(textRenderer, Text.literal(info.costGold + "g"), x + 3, y + 22, 0x88FF88, false);

            if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                context.fill(x, y, x + CELL, y + CELL, 0x44FFFFFF);
                String barName = switch (info.barItemId) {
                    case "copper_bar" -> "铜锭"; case "iron_bar" -> "铁锭";
                    case "gold_bar" -> "金锭"; case "iridium_bar" -> "铱锭";
                    default -> info.barItemId;
                };
                context.drawTooltip(textRenderer, List.of(
                    Text.translatable("gui.stardewvalley.ore_shop.upgrade"),
                    Text.literal(info.costGold + "g + 5 " + barName)
                ), mouseX, mouseY);
            }

            col++;
        }

        // 垃圾桶升级（在工具同一行，最后一列）
        int trashLevel = ClientTrashCanData.getTrashLevel();
        if (trashLevel < 4) {
            int tx = INV_MARGIN_X + col * (CELL + 8);
            int ty = 50;
            boolean hasTrashBars = countInventoryItem(TRASH_TIER_ITEMS[trashLevel]) >= 5;
            boolean canAffordTrash = currentGold >= TRASH_COSTS[trashLevel] && hasTrashBars;
            if (canAffordTrash) {
                context.fill(tx, ty, tx + CELL, ty + CELL, 0xFF333333);
                context.fill(tx + 1, ty + 1, tx + CELL - 1, ty + CELL - 1, 0xFF448844);
            } else {
                context.fill(tx, ty, tx + CELL, ty + CELL, 0xFF222222);
                context.fill(tx + 1, ty + 1, tx + CELL - 1, ty + CELL - 1, 0xFF333333);
            }
            context.drawTexture(RenderPipelines.GUI_TEXTURED, TRASH_TEXTURES[trashLevel], tx + 8, ty + 2, 0.0f, 0.0f, 16, 16, 16, 16);
            NumberRenderer.drawNumber(context, TRASH_COSTS[trashLevel], tx + 1, ty + 20, 0.18f, true);
            if (mouseX >= tx && mouseX < tx + CELL && mouseY >= ty && mouseY < ty + CELL) {
                context.fill(tx, ty, tx + CELL, ty + CELL, 0x44FFFFFF);
                context.drawTooltip(textRenderer, List.of(
                    Text.translatable("gui.stardewvalley.ore_shop.upgrade_trash_can"),
                    Text.translatable("gui.stardewvalley.ore_shop.trash_upgrade_cost", TRASH_COSTS[trashLevel], Text.translatable(TRASH_LABELS[trashLevel]))
                ), mouseX, mouseY);
            }
        }
    }

    private void renderInventory(DrawContext context) {
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.ore_shop.inventory"), invGridX, invGridY - 12, 0xFFFFFF, false);
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
                if (id.getNamespace().equals(StardewValley.MOD_ID) && SELLABLE_ITEMS.contains(id.getPath())) {
                    context.fill(sx, sy, sx + SLOT_SIZE, sy + 1, 0xFF00DD00);
                }
            }
        }
    }

    private int countInventoryItem(String itemId) {
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
    public boolean shouldPause() {
        return false;
    }
}
