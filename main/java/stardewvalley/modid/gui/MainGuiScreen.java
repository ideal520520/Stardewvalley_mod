package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.fishing.C2SMinigameStatePayload;
import stardewvalley.modid.season.Season;
import stardewvalley.modid.skill.SkillRegistry;
import stardewvalley.modid.gui.TravelingCartScreen;
import stardewvalley.modid.gui.DesertTraderScreen;
import stardewvalley.modid.gui.DwarfShopScreen;
import stardewvalley.modid.gui.HarveyShopScreen;
import stardewvalley.modid.gui.IslandTraderScreen;
import stardewvalley.modid.gui.KrobusShopScreen;
import stardewvalley.modid.gui.MarlonShopScreen;
import stardewvalley.modid.gui.MarnieShopScreen;
import stardewvalley.modid.gui.RobinShopScreen;
import stardewvalley.modid.gui.SandyShopScreen;
import stardewvalley.modid.gui.GusShopScreen;
import stardewvalley.modid.gui.BookStoreScreen;

public class MainGuiScreen extends Screen {

    private static final Identifier SHOP_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/pierre_icon.png");
    private static final Identifier SHIPPING_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/shippingbox.png");
    private static final Identifier CLINT_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/clint_icon.png");
    private static final Identifier FARMING_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/farmingskillicon.png");
    private static final Identifier FORAGING_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/foragingskillicon.png");
    private static final Identifier FISHING_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/fishingskillicon.png");
    private static final Identifier MINING_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/miningskillicon.png");
    private static final Identifier COMBAT_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/combatskillicon.png");
    private static final Identifier WILLY_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/willy_icon.png");
    private static final Identifier WORKBENCH_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/workbench.png");
    private static final Identifier COOKING_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/Cooking_Icon.png");
    private static final Identifier JUNIMO_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/Junimo_Icon.png");
    private static final Identifier TRAVELING_CART_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Traveling_Cart.png");
    private static final Identifier BOOKSELLER_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Bookseller.png");
    private static final Identifier GUNTHER_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Gunther_Icon.png");
    private static final Identifier MASTERY_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/Mastery_Icon.png");

    private static final Identifier DESERT_TRADER_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Desert_Trader_Icon.png");
    private static final Identifier DWARF_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Dwarf.png");
    private static final Identifier GOLDEN_WALNUT_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Golden_Walnut.png");
    private static final Identifier HARVEY_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Harvey_Icon.png");
    private static final Identifier ISLAND_TRADER_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Island_Trader_Icon.png");
    private static final Identifier MARLON_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Marlon_Icon.png");
    private static final Identifier MARNIE_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Marnie_Icon.png");
    private static final Identifier ROBIN_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Robin_Icon.png");
    private static final Identifier SANDY_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Sandy_Icon.png");
    private static final Identifier WIZARD_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Wizard_Icon.png");
    private static final Identifier KROBUS_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Krobus_Icon.png");
    private static final Identifier GUS_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/Gus_Icon.png");

    // 第一列紧靠左边(8)，列间距加大避免文本重叠
    private static final int SKILL_COL1_X = 8;
    private static final int SKILL_COL2_X = 128;
    private static final int SKILL_COL3_X = 248;
    // 已选天赋小图标间距
    private static final int SKILL_ICON_GAP = 18;

    // NPC商店图标区域
    // 第1列(x=8)在4个左列商店下方再放2个，第2列(x=76)从y=50起最多6个
    private static final int NPC_ROW_GAP = 36;
    private static final int NPC_Y_START = 50;
    // 列1: x=8, 列2: x=8+32+36=76, 列3: x=76+32+36=144
    private static final int NPC_COL2_X = 76;
    private static final int NPC_COL3_X = 144;

    // NPC排列：第1列(2个)放在y=194,230；第2列(6个)从y=50起；第3列(5个)从y=50起
    private static final String[] NPC_COL1 = {"desert","dwarf"};
    private static final String[] NPC_COL2 = {"walnut","harvey","island","marlon","marnie","robin"};
    private static final String[] NPC_COL3 = {"sandy","krobus","wizard","gus","gunther"};

    // 缓存金库收集包完成状态（服务端同步）
    public static boolean vaultCompleted = false;

    // 书店营业状态（服务端同步）
    public static boolean bookStoreOpenToday = false;

    private SkillRegistry.SkillEntry hoveredSkillEntry = null;

    public MainGuiScreen() {
        super(Text.translatable("gui.stardewvalley.main.title"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.FarmingRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.ForagingRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.FishingRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.MiningRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.CombatRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.SkillRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.MasteryRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.BundleDataRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.MuseumDataRequestC2SPayload());
        ClientPlayNetworking.send(new ModPayloads.BookStoreOpenRequestC2SPayload());
        ClientPlayNetworking.send(new C2SMinigameStatePayload(true));
    }

    private boolean isTravelingCartOpen() {
        if (client == null || client.world == null) return false;
        long timeOfDay = client.world.getTimeOfDay();
        int dayOfSeason = Season.getDayOfSeason(timeOfDay);
        // 星期五: day 5,12,19,26; 星期天: day 7,14,21,28
        return dayOfSeason == 5 || dayOfSeason == 12 || dayOfSeason == 19 || dayOfSeason == 26
            || dayOfSeason == 7 || dayOfSeason == 14 || dayOfSeason == 21 || dayOfSeason == 28;
    }

    private boolean isYear2() {
        if (client == null || client.world == null) return false;
        return Season.getTotalDays(client.world.getTimeOfDay()) >= 112;
    }

    private boolean isYear3() {
        if (client == null || client.world == null) return false;
        return Season.getTotalDays(client.world.getTimeOfDay()) >= 224;
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        int wbX = width - 40;
        int wbY = 8;
        if (mx >= wbX && mx < wbX + 24 && my >= wbY && my < wbY + 32) {
            if (client != null) client.setScreen(new WorkbenchScreen());
            return true;
        }

        int cookX = width - 40;
        int cookY = 48;
        if (mx >= cookX && mx < cookX + 16 && my >= cookY && my < cookY + 16) {
            if (client != null) client.setScreen(new CookingScreen());
            return true;
        }

        int bundleX = width - 40;
        int bundleY = 68;
        if (mx >= bundleX && mx < bundleX + 16 && my >= bundleY && my < bundleY + 16) {
            if (client != null) client.setScreen(new BundleRoomScreen());
            return true;
        }

        if (tryOpenSkillCategory(mx, my, SKILL_COL1_X, 8, SkillRegistry.Category.FARMING)) return true;
        if (tryOpenSkillCategory(mx, my, SKILL_COL1_X, 28, SkillRegistry.Category.FORAGING)) return true;
        if (tryOpenSkillCategory(mx, my, SKILL_COL2_X, 8, SkillRegistry.Category.FISHING)) return true;
        if (tryOpenSkillCategory(mx, my, SKILL_COL2_X, 28, SkillRegistry.Category.MINING)) return true;
        if (tryOpenSkillCategory(mx, my, SKILL_COL3_X, 8, SkillRegistry.Category.COMBAT)) return true;

        if (mx >= 8 && mx < 40 && my >= 50 && my < 82) {
            if (client != null) client.setScreen(new ShippingBoxScreen());
            return true;
        }
        if (mx >= 8 && mx < 40 && my >= 86 && my < 118) {
            if (client != null) client.setScreen(new ShopScreen());
            return true;
        }
        if (mx >= 8 && mx < 40 && my >= 122 && my < 154) {
            if (client != null) client.setScreen(new OreShopScreen());
            return true;
        }
        if (mx >= 8 && mx < 40 && my >= 158 && my < 190) {
            if (client != null) client.setScreen(new FishShopScreen());
            return true;
        }

        // NPC商店 - 第1列(x=8)，放在鱼店下方 y=194,230
        if (mx >= 8 && mx < 40 && my >= 194 && my < 226) {
            if (client != null && vaultCompleted) client.setScreen(new DesertTraderScreen());
            return true;
        }
        if (mx >= 8 && mx < 40 && my >= 230 && my < 262) {
            if (client != null && MuseumDonationScreen.clientAbilities.contains("dwarf_translation")) {
                client.setScreen(new DwarfShopScreen());
            }
            return true;
        }

        // NPC商店 - 第2列(x=76)，等间距排列，从y=50起
        for (int i = 0; i < NPC_COL2.length; i++) {
            int ny = NPC_Y_START + i * NPC_ROW_GAP;
            if (mx >= NPC_COL2_X && mx < NPC_COL2_X + 32 && my >= ny && my < ny + 32) {
                if (client != null) {
                    Screen screen = null;
                    switch (NPC_COL2[i]) {
                        case "harvey" -> screen = new HarveyShopScreen();
                        case "island" -> { if (isYear3()) screen = new IslandTraderScreen(); }
                        case "marlon" -> screen = new MarlonShopScreen();
                        case "marnie" -> screen = new MarnieShopScreen();
                        case "robin" -> screen = new RobinShopScreen();
                    }
                    if (screen != null) client.setScreen(screen);
                }
                return true;
            }
        }

        // NPC商店 - 第3列(x=144)，等间距排列，从y=50起
        for (int i = 0; i < NPC_COL3.length; i++) {
            int ny = NPC_Y_START + i * NPC_ROW_GAP;
            if (mx >= NPC_COL3_X && mx < NPC_COL3_X + 32 && my >= ny && my < ny + 32) {
                if (client != null) {
                    Screen screen = null;
                    switch (NPC_COL3[i]) {
                        case "sandy" -> { if (vaultCompleted) screen = new SandyShopScreen(); }
                        case "krobus" -> { if (MuseumDonationScreen.clientAbilities.contains("rusty_key")) screen = new KrobusShopScreen(); }
                        case "gus" -> screen = new GusShopScreen();
                        case "gunther" -> screen = new MuseumDonationScreen();
                    }
                    if (screen != null) client.setScreen(screen);
                }
                return true;
            }
        }

        // Traveling Cart button (bottom-right, only when open)
        int tcX = this.width - 125;
        int tcY = this.height - 69;
        if (isTravelingCartOpen() && mx >= tcX && mx < tcX + 125 && my >= tcY && my < tcY + 69) {
            if (client != null) client.setScreen(new TravelingCartScreen());
            return true;
        }

        // Book Store button (bottom-right, only when open)
        int bsX = this.width - 110;
        int bsY = this.height - 79;
        if (bookStoreOpenToday && mx >= bsX && mx < bsX + 110 && my >= bsY && my < bsY + 79) {
            if (client != null) client.setScreen(new BookStoreScreen());
            return true;
        }

        return false;
    }

    private boolean isBookStoreOpenToday() {
        return bookStoreOpenToday;
    }

    private int getClientCategoryLevel(SkillRegistry.Category category) {
        return switch (category) {
            case FARMING -> ClockHudRenderer.getClientFarmingLevel();
            case FORAGING -> ClockHudRenderer.getClientForagingLevel();
            case FISHING -> ClockHudRenderer.getClientFishingLevel();
            case MINING -> ClockHudRenderer.getClientMiningLevel();
            case COMBAT -> ClockHudRenderer.getClientCombatLevel();
        };
    }

    private boolean tryOpenSkillCategory(double mx, double my, int iconX, int iconY, SkillRegistry.Category category) {
        if (mx >= iconX && mx < iconX + 16 && my >= iconY && my < iconY + 16) {
            // 精通：未精通且精通等级高于已精通数时，点击该技能即可精通
            if (!ClockHudRenderer.isClientMastered(category)
                && ClockHudRenderer.getClientMasteryLevel() > ClockHudRenderer.getClientMasteredCount()) {
                if (client != null && client.player != null) {
                    ClientPlayNetworking.send(new ModPayloads.MasterySelectC2SPayload(category.ordinal()));
                    client.player.sendMessage(Text.literal("§a已请求精通该技能"), false);
                }
                return true;
            }
            // 已精通：点击无反应
            if (ClockHudRenderer.isClientMastered(category)) {
                return true;
            }
            String tier2 = ClockHudRenderer.getClientSkill(category, 2);
            if (tier2 != null) return true;
            String tier1 = ClockHudRenderer.getClientSkill(category, 1);
            int level = getClientCategoryLevel(category);
            if (tier1 != null && level < 10) return true;
            if (client != null) client.setScreen(new SkillSelectionScreen(category));
            return true;
        }
        return false;
    }

    private void drawNpcIcon(DrawContext context, Identifier texture, int x, int y, String labelKey) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0f, 0.0f, 32, 32, 32, 32);
        context.drawText(textRenderer, Text.translatable(labelKey), x + 36, y + 8, 0xFFFFFF, false);
    }

    private void drawSkillIcon(DrawContext context, int x, int y, SkillRegistry.Category category, int mouseX, int mouseY) {
        String tier1 = ClockHudRenderer.getClientSkill(category, 1);
        String tier2 = ClockHudRenderer.getClientSkill(category, 2);
        int iconX = x + 16 + 2;

        // 精通：在技能类别图标左下角渲染 8x8 精通图标（16x16纹理缩放到8x8）
        if (ClockHudRenderer.isClientMastered(category)) {
            Matrix3x2fStack m = context.getMatrices();
            m.pushMatrix();
            m.translate(x, y + 8);
            m.scale(0.5f, 0.5f);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, MASTERY_ICON, 0, 0, 0.0f, 0.0f, 16, 16, 16, 16);
            m.popMatrix();
        }

        Matrix3x2fStack matrices = context.getMatrices();
        float scale = 16f / 36f;

        if (tier1 != null) {
            SkillRegistry.SkillEntry entry = SkillRegistry.get(tier1);
            if (entry != null) {
                if (mouseX >= iconX && mouseX < iconX + 16 && mouseY >= y && mouseY < y + 16) {
                    hoveredSkillEntry = entry;
                }
                matrices.pushMatrix();
                matrices.translate(iconX, y);
                matrices.scale(scale, scale);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, entry.texture, 0, 0, 0, 0, 36, 36, 36, 36);
                matrices.popMatrix();
            }
            iconX += SKILL_ICON_GAP;
        }
        if (tier2 != null) {
            SkillRegistry.SkillEntry entry = SkillRegistry.get(tier2);
            if (entry != null) {
                if (mouseX >= iconX && mouseX < iconX + 16 && mouseY >= y && mouseY < y + 16) {
                    hoveredSkillEntry = entry;
                }
                matrices.pushMatrix();
                matrices.translate(iconX, y);
                matrices.scale(scale, scale);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, entry.texture, 0, 0, 0, 0, 36, 36, 36, 36);
                matrices.popMatrix();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);
        hoveredSkillEntry = null;

        float letterScale = 0.17f;
        float numScale = 0.255f;

        // ─── 耕种 ───
        context.drawTexture(RenderPipelines.GUI_TEXTURED, FARMING_ICON, SKILL_COL1_X, 8, 0.0f, 0.0f, 16, 16, 16, 16);
        drawSkillIcon(context, SKILL_COL1_X, 8, SkillRegistry.Category.FARMING, mouseX, mouseY);
        int textX = SKILL_COL1_X + 56;
        LetterRenderer.drawText(context, "LV", textX, 8, letterScale);
        textX += LetterRenderer.textWidth("LV", letterScale);
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientFarmingLevel(), textX, 8, numScale, true);
        textX = SKILL_COL1_X + 56;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientFarmingXp(), textX, 20, numScale, true);
        textX += NumberRenderer.numberWidth(ClockHudRenderer.getClientFarmingXp(), numScale) + 2;
        LetterRenderer.drawLetter(context, 'S', textX, 20, letterScale);
        textX += LetterRenderer.charWidth(letterScale) + 2;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientFarmingNextXp(), textX, 20, numScale, true);

        // ─── 采集 ───
        context.drawTexture(RenderPipelines.GUI_TEXTURED, FORAGING_ICON, SKILL_COL1_X, 28, 0.0f, 0.0f, 16, 16, 16, 16);
        drawSkillIcon(context, SKILL_COL1_X, 28, SkillRegistry.Category.FORAGING, mouseX, mouseY);
        textX = SKILL_COL1_X + 56;
        LetterRenderer.drawText(context, "LV", textX, 28, letterScale);
        textX += LetterRenderer.textWidth("LV", letterScale);
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientForagingLevel(), textX, 28, numScale, true);
        textX = SKILL_COL1_X + 56;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientForagingXp(), textX, 40, numScale, true);
        textX += NumberRenderer.numberWidth(ClockHudRenderer.getClientForagingXp(), numScale) + 2;
        LetterRenderer.drawLetter(context, 'S', textX, 40, letterScale);
        textX += LetterRenderer.charWidth(letterScale) + 2;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientForagingNextXp(), textX, 40, numScale, true);

        // ─── 钓鱼 ───
        context.drawTexture(RenderPipelines.GUI_TEXTURED, FISHING_ICON, SKILL_COL2_X, 8, 0.0f, 0.0f, 16, 16, 16, 16);
        drawSkillIcon(context, SKILL_COL2_X, 8, SkillRegistry.Category.FISHING, mouseX, mouseY);
        textX = SKILL_COL2_X + 56;
        LetterRenderer.drawText(context, "LV", textX, 8, letterScale);
        textX += LetterRenderer.textWidth("LV", letterScale);
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientFishingLevel(), textX, 8, numScale, true);
        textX = SKILL_COL2_X + 56;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientFishingXp(), textX, 20, numScale, true);
        textX += NumberRenderer.numberWidth(ClockHudRenderer.getClientFishingXp(), numScale) + 2;
        LetterRenderer.drawLetter(context, 'S', textX, 20, letterScale);
        textX += LetterRenderer.charWidth(letterScale) + 2;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientFishingNextXp(), textX, 20, numScale, true);

        // ─── 挖矿 ───
        context.drawTexture(RenderPipelines.GUI_TEXTURED, MINING_ICON, SKILL_COL2_X, 28, 0.0f, 0.0f, 16, 16, 16, 16);
        drawSkillIcon(context, SKILL_COL2_X, 28, SkillRegistry.Category.MINING, mouseX, mouseY);
        textX = SKILL_COL2_X + 56;
        LetterRenderer.drawText(context, "LV", textX, 28, letterScale);
        textX += LetterRenderer.textWidth("LV", letterScale);
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientMiningLevel(), textX, 28, numScale, true);
        textX = SKILL_COL2_X + 56;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientMiningXp(), textX, 40, numScale, true);
        textX += NumberRenderer.numberWidth(ClockHudRenderer.getClientMiningXp(), numScale) + 2;
        LetterRenderer.drawLetter(context, 'S', textX, 40, letterScale);
        textX += LetterRenderer.charWidth(letterScale) + 2;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientMiningNextXp(), textX, 40, numScale, true);

        // ─── 战斗 ───
        context.drawTexture(RenderPipelines.GUI_TEXTURED, COMBAT_ICON, SKILL_COL3_X, 8, 0.0f, 0.0f, 16, 16, 16, 16);
        drawSkillIcon(context, SKILL_COL3_X, 8, SkillRegistry.Category.COMBAT, mouseX, mouseY);
        textX = SKILL_COL3_X + 56;
        LetterRenderer.drawText(context, "LV", textX, 8, letterScale);
        textX += LetterRenderer.textWidth("LV", letterScale);
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientCombatLevel(), textX, 8, numScale, true);
        textX = SKILL_COL3_X + 56;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientCombatXp(), textX, 20, numScale, true);
        textX += NumberRenderer.numberWidth(ClockHudRenderer.getClientCombatXp(), numScale) + 2;
        LetterRenderer.drawLetter(context, 'S', textX, 20, letterScale);
        textX += LetterRenderer.charWidth(letterScale) + 2;
        NumberRenderer.drawNumber(context, ClockHudRenderer.getClientCombatNextXp(), textX, 20, numScale, true);

        // ─── 其他功能图标 ───
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SHIPPING_TEXTURE, 8, 50, 0.0f, 0.0f, 32, 32, 32, 32);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.main.shipping_box"), 44, 54, 0xFFFFFF, false);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, SHOP_TEXTURE, 8, 86, 0.0f, 0.0f, 32, 32, 32, 32);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.main.shop"), 44, 94, 0xFFFFFF, false);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, CLINT_TEXTURE, 8, 122, 0.0f, 0.0f, 32, 32, 32, 32);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.main.ore_shop"), 44, 126, 0xFFFFFF, false);

        context.drawTexture(RenderPipelines.GUI_TEXTURED, WILLY_TEXTURE, 8, 158, 0.0f, 0.0f, 32, 32, 32, 32);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.main.fish_shop"), 44, 164, 0xFFFFFF, false);

        // ─── NPC商店图标（第1列x=8，放在鱼店下方y=194,230） ───
        drawNpcIcon(context, DESERT_TRADER_TEXTURE, 8, 194, "gui.stardewvalley.main.desert_trader");
        drawNpcIcon(context, DWARF_TEXTURE, 8, 230, "gui.stardewvalley.main.dwarf");

        // ─── NPC商店图标（第2列x=76，从y=50起6个） ───
        drawNpcIcon(context, GOLDEN_WALNUT_TEXTURE, NPC_COL2_X, NPC_Y_START + 0 * NPC_ROW_GAP, "gui.stardewvalley.main.golden_walnut");
        drawNpcIcon(context, HARVEY_TEXTURE, NPC_COL2_X, NPC_Y_START + 1 * NPC_ROW_GAP, "gui.stardewvalley.main.harvey");
        drawNpcIcon(context, ISLAND_TRADER_TEXTURE, NPC_COL2_X, NPC_Y_START + 2 * NPC_ROW_GAP, "gui.stardewvalley.main.island_trader");
        drawNpcIcon(context, MARLON_TEXTURE, NPC_COL2_X, NPC_Y_START + 3 * NPC_ROW_GAP, "gui.stardewvalley.main.marlon");
        drawNpcIcon(context, MARNIE_TEXTURE, NPC_COL2_X, NPC_Y_START + 4 * NPC_ROW_GAP, "gui.stardewvalley.main.marnie");
        drawNpcIcon(context, ROBIN_TEXTURE, NPC_COL2_X, NPC_Y_START + 5 * NPC_ROW_GAP, "gui.stardewvalley.main.robin");

        // ─── NPC商店图标（第3列x=144，从y=50起） ───
        drawNpcIcon(context, SANDY_TEXTURE, NPC_COL3_X, NPC_Y_START + 0 * NPC_ROW_GAP, "gui.stardewvalley.main.sandy");
        drawNpcIcon(context, KROBUS_TEXTURE, NPC_COL3_X, NPC_Y_START + 1 * NPC_ROW_GAP, "gui.stardewvalley.main.krobus");
        drawNpcIcon(context, WIZARD_TEXTURE, NPC_COL3_X, NPC_Y_START + 2 * NPC_ROW_GAP, "gui.stardewvalley.main.wizard");
        drawNpcIcon(context, GUS_TEXTURE, NPC_COL3_X, NPC_Y_START + 3 * NPC_ROW_GAP, "gui.stardewvalley.main.gus");
        drawNpcIcon(context, GUNTHER_TEXTURE, NPC_COL3_X, NPC_Y_START + 4 * NPC_ROW_GAP, "gui.stardewvalley.main.museum");

        int wbX = width - 40;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WORKBENCH_TEXTURE, wbX, 8, 0.0f, 0.0f, 16, 32, 16, 32);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.main.craft"), wbX, 40, 0xFFFFFF, false);

        int cookX = width - 40;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, COOKING_ICON, cookX, 48, 0.0f, 0.0f, 16, 16, 16, 16);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.main.cook"), cookX, 64, 0xFFFFFF, false);

        int bundleX = width - 40;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, JUNIMO_ICON, bundleX, 68, 0.0f, 0.0f, 16, 16, 16, 16);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.main.bundle"), bundleX, 84, 0xFFFFFF, false);

        // Traveling Cart / Book Store (bottom-right, show whichever is open today)
        int bsX = this.width - 125;
        int bsY = this.height - 79;
        if (isTravelingCartOpen()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, TRAVELING_CART_TEXTURE, bsX, bsY, 0, 0, 125, 69, 125, 69);
        } else if (isBookStoreOpenToday()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, BOOKSELLER_TEXTURE, bsX, bsY, 0, 0, 110, 79, 110, 79);
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.main.book_store"), bsX, bsY + 81, 0xFFFFFF, false);
        }

        // 悬浮在天赋小图标上显示详情
        if (hoveredSkillEntry != null) {
            java.util.List<Text> lines = new java.util.ArrayList<>();
            lines.add(Text.literal("§e" + hoveredSkillEntry.name));
            lines.add(Text.literal("§7" + hoveredSkillEntry.description));
            context.drawTooltip(textRenderer, lines, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        ClientPlayNetworking.send(new C2SMinigameStatePayload(false));
    }
}
