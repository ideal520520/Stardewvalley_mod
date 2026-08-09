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
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MarlonShopScreen extends Screen {

    private static final Identifier GOLD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/gold.png");

    public static boolean hasBartered = false;

    private static final int COLS = 9;
    private static final int CELL_SIZE = 32;
    private static final int SLOT_SIZE = 18;
    private static final int MARGIN_X = 8;
    private static final int MARGIN_Y = 30;
    private int invGridX;
    private int invGridY;

    private int currentTab = 0;
    private final String[] TAB_NAMES = {"武器", "戒指&靴子", "兑换"};
    private final int[] TAB_WIDTHS = {48, 72, 48};
    private List<MarlonEntry> currentItems = new ArrayList<>();
    private int scrollOffset = 0;

    private static class MarlonEntry {
        final String itemId;
        final String label;
        final int price;
        final boolean isTodo;
        final ItemStack stack;

        MarlonEntry(String itemId, String label, int price, boolean isTodo) {
            this.itemId = itemId;
            this.label = label;
            this.price = price;
            this.isTodo = isTodo;
            if (!isTodo) {
                Item item = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, itemId));
                this.stack = item != null ? new ItemStack(item) : ItemStack.EMPTY;
            } else {
                this.stack = ItemStack.EMPTY;
            }
        }
    }

    private static final List<MarlonEntry> WEAPONS = new ArrayList<>();
    private static final List<MarlonEntry> RINGS = new ArrayList<>();
    private static final List<MarlonEntry> BOOTS = new ArrayList<>();
    private static final List<MarlonEntry> OTHER = new ArrayList<>();
    private static final List<MarlonEntry> BARTER = new ArrayList<>();

    static {
        // 武器
        WEAPONS.add(new MarlonEntry("rusty_sword", "生锈的剑", 250, false));
        WEAPONS.add(new MarlonEntry("wooden_blade", "木剑", 250, false));
        WEAPONS.add(new MarlonEntry("bone_sword", "股骨", 350, false));
        WEAPONS.add(new MarlonEntry("iron_dirk", "铁制短剑", 500, false));
        WEAPONS.add(new MarlonEntry("silver_saber", "镀银军刀", 750, false));
        WEAPONS.add(new MarlonEntry("elf_blade", "精灵之刃", 750, false));
        WEAPONS.add(new MarlonEntry("steel_smallsword", "钢制轻剑", 750, false));
        WEAPONS.add(new MarlonEntry("cutlass", "弯刀", 750, false));
        WEAPONS.add(new MarlonEntry("pirates_sword", "海盗剑", 850, false));
        WEAPONS.add(new MarlonEntry("wood_mallet", "木锤", 2000, false));
        WEAPONS.add(new MarlonEntry("claymore", "双刃大剑", 2000, false));
        WEAPONS.add(new MarlonEntry("templars_blade", "圣堂之刃", 4000, false));
        WEAPONS.add(new MarlonEntry("crystal_dagger", "水晶匕首", 4500, false));
        WEAPONS.add(new MarlonEntry("steel_falchion", "钢刀", 9000, false));
        WEAPONS.add(new MarlonEntry("obsidian_edge", "黑曜石之刃", 9000, false));
        WEAPONS.add(new MarlonEntry("lava_katana", "熔岩武士刀", 25000, false));
        WEAPONS.add(new MarlonEntry("galaxy_sword", "银河剑", 50000, false));
        WEAPONS.add(new MarlonEntry("galaxy_dagger", "银河匕首", 35000, false));
        WEAPONS.add(new MarlonEntry("galaxy_hammer", "银河之锤", 75000, false));
        WEAPONS.add(new MarlonEntry("insect_head", "昆虫头部", 10000, false));

        // 戒指
        RINGS.add(new MarlonEntry("amethyst_ring", "紫水晶戒指", 1000, false));
        RINGS.add(new MarlonEntry("topaz_ring", "黄水晶戒指", 1000, false));
        RINGS.add(new MarlonEntry("aquamarine_ring", "海蓝宝石戒指", 2500, false));
        RINGS.add(new MarlonEntry("jade_ring", "翡翠戒指", 2500, false));
        RINGS.add(new MarlonEntry("emerald_ring", "绿宝石戒指", 5000, false));
        RINGS.add(new MarlonEntry("ruby_ring", "红宝石戒指", 5000, false));
        RINGS.add(new MarlonEntry("slime_charmer_ring", "史莱姆克星戒指", 25000, false));
        RINGS.add(new MarlonEntry("savage_ring", "野蛮人戒指", 25000, false));
        RINGS.add(new MarlonEntry("burglars_ring", "窃贼戒指", 20000, false));
        RINGS.add(new MarlonEntry("vampire_ring", "吸血戒指", 15000, false));
        RINGS.add(new MarlonEntry("crabshell_ring", "蟹壳戒指", 15000, false));
        RINGS.add(new MarlonEntry("napalm_ring", "燃烧弹戒指", 30000, false));

        // 靴子
        BOOTS.add(new MarlonEntry("sneakers", "运动鞋", 500, false));
        BOOTS.add(new MarlonEntry("rubber_boots", "橡胶靴", 500, false));
        BOOTS.add(new MarlonEntry("leather_boots", "皮靴", 500, false));
        BOOTS.add(new MarlonEntry("work_boots", "工作靴", 700, false));
        BOOTS.add(new MarlonEntry("combat_boots", "战靴", 1250, false));
        BOOTS.add(new MarlonEntry("tundra_boots", "冻土靴", 750, false));
        BOOTS.add(new MarlonEntry("thermal_boots", "热能靴", 1000, false));
        BOOTS.add(new MarlonEntry("dark_boots", "黑暗之靴", 2500, false));
        BOOTS.add(new MarlonEntry("firewalker_boots", "蹈火者靴", 2000, false));
        BOOTS.add(new MarlonEntry("genie_shoes", "神怪之鞋", 3500, false));
        BOOTS.add(new MarlonEntry("space_boots", "太空之靴", 5000, false));
        BOOTS.add(new MarlonEntry("cowboy_boots", "牛仔之靴", 1500, false));
        BOOTS.add(new MarlonEntry("emilys_magic_boots", "艾米丽的魔法靴", 6000, false));
        BOOTS.add(new MarlonEntry("leprechaun_shoes", "矮精灵鞋子", 3000, false));
        BOOTS.add(new MarlonEntry("cinderclown_shoes", "灰烬小丑鞋", 8000, false));
        BOOTS.add(new MarlonEntry("mermaid_boots", "美人鱼靴", 10000, false));
        BOOTS.add(new MarlonEntry("dragonscale_boots", "龙鳞靴", 12000, false));
        BOOTS.add(new MarlonEntry("crystal_shoes", "水晶鞋", 7000, false));

        OTHER.add(new MarlonEntry("explosive_ammo", "爆炸弹丸", 300, false));
        OTHER.add(new MarlonEntry("slingshot", "弹弓", 500, false));
        OTHER.add(new MarlonEntry("master_slingshot", "高级弹弓", 1000, false));

        // 以物换物
        BARTER.add(new MarlonEntry("galaxy_sword", "银河剑", 0, false));
    }

    public MarlonShopScreen() {
        super(Text.literal("马龙的商店"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.MarlonTradeStateRequestC2SPayload());
        invGridX = MARGIN_X;
        invGridY = MARGIN_Y + 3 * CELL_SIZE + 30;
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
            Text.literal("< 返回"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
        updateTabItems();
    }

    private void updateTabItems() {
        currentItems.clear();
        if (currentTab == 0) {
            currentItems.addAll(WEAPONS);
        } else if (currentTab == 1) {
            currentItems.addAll(RINGS);
            currentItems.addAll(BOOTS);
            currentItems.addAll(OTHER);
        } else {
            currentItems.addAll(BARTER);
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
            MarlonEntry entry = currentItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            if (mx >= x && mx < x + CELL_SIZE && my >= y && my < y + CELL_SIZE) {
                if (entry.isTodo || entry.stack.isEmpty()) return true;

                if (currentTab == 2) {
                    // 已兑换则忽略点击
                    if (hasBartered) return true;

                    // 以物换物标签
                    int tradeCount = 1;
                    boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
                    boolean ctrl = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
                    if (shift && ctrl) tradeCount = 999;
                    else if (ctrl) tradeCount = 25;
                    else if (shift) tradeCount = 5;

                    // 检查玩家是否有足够五彩碎片
                    int prismaticCount = 0;
                    for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
                        ItemStack stack = client.player.getInventory().getStack(slot);
                        if (!stack.isEmpty()) {
                            Identifier id = Registries.ITEM.getId(stack.getItem());
                            if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals("prismatic_shard")) {
                                prismaticCount += stack.getCount();
                            }
                        }
                    }
                    if (prismaticCount >= tradeCount) {
                        ClientPlayNetworking.send(new ModPayloads.MarlonBarterC2SPayload(entry.itemId, tradeCount));
                    }
                    return true;
                }

                int buyCount = 1;
                boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
                boolean ctrl = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
                if (shift && ctrl) buyCount = 999;
                else if (ctrl) buyCount = 25;
                else if (shift) buyCount = 5;

                int currentGold = ClockHudRenderer.getClientGold();
                int totalCost = entry.price * buyCount;
                if (currentGold >= totalCost) {
                    ClientPlayNetworking.send(new ModPayloads.ShopBuyC2SPayload(entry.itemId, buyCount));
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
        context.enableScissor(0, gridStartY, width, height - gridStartY);
        for (int i = 0; i < currentItems.size(); i++) {
            MarlonEntry entry = currentItems.get(i);
            int row = i / COLS;
            int col = i % COLS;
            int x = MARGIN_X + col * CELL_SIZE;
            int y = gridStartY + row * CELL_SIZE - scrollOffset;

            if (y < gridStartY - CELL_SIZE || y > gridStartY + 3 * CELL_SIZE) continue;

            boolean isBarterTab = currentTab == 2;
            boolean isSoldOut = isBarterTab && hasBartered;
            int currentGold = ClockHudRenderer.getClientGold();
            boolean canAfford = !entry.isTodo && !isSoldOut && !entry.stack.isEmpty();
            if (isBarterTab) {
                int prismaticCount = 0;
                if (client != null && client.player != null) {
                    for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
                        ItemStack stack = client.player.getInventory().getStack(slot);
                        if (!stack.isEmpty()) {
                            Identifier id = Registries.ITEM.getId(stack.getItem());
                            if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals("prismatic_shard")) {
                                prismaticCount += stack.getCount();
                            }
                        }
                    }
                }
                canAfford = prismaticCount >= 1;
            } else {
                canAfford = currentGold >= entry.price;
            }

            int bgColor = entry.isTodo ? 0xFF444444 : (isSoldOut ? 0xFF222222 : (canAfford ? 0xFF333333 : 0xFF222222));
            int bgInner = entry.isTodo ? 0xFF333333 : (isSoldOut ? 0xFF111111 : (canAfford ? 0xFF448844 : 0xFF333333));
            context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, bgColor);
            context.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, bgInner);

            if (isSoldOut) {
                context.drawCenteredTextWithShadow(textRenderer, Text.literal("已卖光"), x + CELL_SIZE / 2, y + CELL_SIZE / 2 - 4, 0xFF4444);
            } else {
                if (!entry.isTodo && !entry.stack.isEmpty()) {
                    context.drawItem(entry.stack, x + 8, y + 4);
                    context.drawStackOverlay(textRenderer, entry.stack, x + 8, y + 4);
                }

                if (isBarterTab) {
                    // 兑换标签页显示所需材料
                    Item prismaticItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "prismatic_shard"));
                    if (prismaticItem != null) {
                        var matrices = context.getMatrices();
                        matrices.pushMatrix();
                        matrices.translate(x + 20, y + 20);
                        matrices.scale(0.5f, 0.5f);
                        context.drawItem(new ItemStack(prismaticItem), -8, -8);
                        matrices.popMatrix();
                    }
                    int ingCount = 0;
                    if (client != null && client.player != null) {
                        for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
                            ItemStack stack = client.player.getInventory().getStack(slot);
                            if (!stack.isEmpty()) {
                                Identifier id = Registries.ITEM.getId(stack.getItem());
                                if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals("prismatic_shard")) {
                                    ingCount += stack.getCount();
                                }
                            }
                        }
                    }
                    int countColor = ingCount >= 1 ? 0x88FF88 : 0xFF8888;
                    context.drawText(textRenderer, Text.literal("x1"), x + 24, y + 24, countColor, false);
                } else {
                    NumberRenderer.drawNumber(context, entry.price, x + 2, y + 22, 0.2f, true);
                }
            }

            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal(entry.label));
                if (isSoldOut) {
                    tooltip.add(Text.literal("§c已卖光"));
                } else if (isBarterTab) {
                    int ingCount = 0;
                    if (client != null && client.player != null) {
                        for (int slot = 0; slot < client.player.getInventory().size(); slot++) {
                            ItemStack stack = client.player.getInventory().getStack(slot);
                            if (!stack.isEmpty()) {
                                Identifier id = Registries.ITEM.getId(stack.getItem());
                                if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals("prismatic_shard")) {
                                    ingCount += stack.getCount();
                                }
                            }
                        }
                    }
                    tooltip.add(Text.literal("需要 五彩碎片 x1 (你有: " + ingCount + ")"));
                } else if (entry.isTodo) {
                    tooltip.add(Text.literal("§7[TODO]"));
                } else {
                    tooltip.add(Text.literal(entry.price + "g"));
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
