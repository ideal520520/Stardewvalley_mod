package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

import java.util.*;

public class BundleDetailScreen extends Screen {

    private static final int SLOT_SIZE = 18;
    private static final int INV_GRID_X = 10;
    private static final int INV_GRID_Y_OFFSET = 150;
    private static final Identifier BUNDLE_SLOT = Identifier.of(StardewValley.MOD_ID, "textures/gui/Bundle_Slot.png");
    private static final Identifier PURCHASE_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/Bundle_Purchase_ZH.png");

    private final String bundleId;
    private final String categoryId;
    private final BundleManager.BundleDef bundleDef;

    private Map<String, Boolean> completedBundles;
    private Set<String> vaultPurchased;
    private Set<String> rewardClaimedBundleIds;
    private Map<String, Integer> submittedItems;
    private boolean dataLoaded = false;

    public BundleDetailScreen(String bundleId, String categoryId,
                               Map<String, Boolean> completedBundles,
                               Set<String> vaultPurchased,
                               Set<String> rewardClaimedBundleIds) {
        super(Text.literal(""));
        this.bundleId = bundleId;
        this.categoryId = categoryId;
        this.bundleDef = BundleManager.getBundleDef(bundleId);
        this.completedBundles = completedBundles;
        this.vaultPurchased = vaultPurchased;
        this.rewardClaimedBundleIds = rewardClaimedBundleIds;
        this.submittedItems = new LinkedHashMap<>();
        if (bundleDef != null) {
            for (var item : bundleDef.requiredItems()) {
                submittedItems.put(item.itemId(), 0);
            }
        }
        this.dataLoaded = true;
    }

    public void updateSyncData(Map<String, Integer> submittedItems,
                                Map<String, Boolean> completedBundles,
                                Set<String> vaultPurchased,
                                Set<String> rewardClaimedBundleIds) {
        this.submittedItems = new LinkedHashMap<>(submittedItems);
        this.completedBundles = completedBundles;
        this.vaultPurchased = vaultPurchased;
        this.rewardClaimedBundleIds = rewardClaimedBundleIds;
        this.dataLoaded = true;
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.BundleDataRequestC2SPayload());
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.bundle_detail.back"),
            button -> { if (client != null) client.setScreen(new BundleCategoryScreen(categoryId, getCategoryDisplayName(),
                completedBundles, vaultPurchased, rewardClaimedBundleIds)); }
        ).dimensions(width - 60, 5, 55, 20).build());
    }

    // ===== 品质匹配逻辑 =====

    private static final Set<String> QUALITY_SUFFIXES = Set.of("_iridium", "_gold", "_silver");

    // 所有银星品质果酒ID集合（用于匹配 _any_wine_silver）
    private static final Set<String> SILVER_WINE_ITEMS = createSilverWineSet();

    private static Set<String> createSilverWineSet() {
        java.util.Set<String> set = new java.util.HashSet<>();
        for (String itemId : stardewvalley.modid.crafting.CraftingMaterialSets.ANY_WINE) {
            if (itemId.endsWith("_wine_silver")) {
                set.add(itemId);
            }
        }
        return java.util.Collections.unmodifiableSet(set);
    }

    private static String stripQuality(String itemId) {
        for (String suffix : QUALITY_SUFFIXES) {
            if (itemId.endsWith(suffix)) {
                return itemId.substring(0, itemId.length() - suffix.length());
            }
        }
        return itemId;
    }

    private static boolean itemMatchesRequired(String inventoryItemId, String requiredItemId) {
        if (inventoryItemId.equals(requiredItemId)) return true;
        // 特殊处理: _any_wine_silver 匹配任何银星品质果酒
        if (requiredItemId.equals("_any_wine_silver") && SILVER_WINE_ITEMS.contains(inventoryItemId)) {
            return true;
        }
        String stripped = stripQuality(inventoryItemId);
        return stripped.equals(requiredItemId);
    }

    /** 特殊物品ID的显示映射（_any_wine_silver等不是真实物品ID） */
    private static String getDisplayItemId(String itemId) {
        return switch (itemId) {
            case "_any_wine_silver" -> "ancient_fruit_wine_silver";
            default -> itemId;
        };
    }

    // ===== 金库 =====

    private static boolean isVaultBundleDef(BundleManager.BundleDef def) {
        return def != null && def.requiredItems().size() == 1 &&
            (def.requiredItems().get(0).itemId().startsWith("_gold_"));
    }

    private static int getVaultGoldCost(BundleManager.BundleDef def) {
        if (def == null || def.requiredItems().isEmpty()) return 0;
        String id = def.requiredItems().get(0).itemId();
        return switch (id) {
            case "_gold_2500" -> 2500;
            case "_gold_5000" -> 5000;
            case "_gold_10000" -> 10000;
            case "_gold_25000" -> 25000;
            default -> 0;
        };
    }

    // ===== 交互 =====

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        if (bundleDef == null) return false;
        boolean isCompleted = completedBundles.getOrDefault(bundleId, false);

        if (isVaultBundleDef(bundleDef)) {
            boolean purchased = vaultPurchased.contains(bundleId);
            if (!purchased) {
                int bx = (width - 64) / 2, by = 80;
                if (mx >= bx && mx < bx + 64 && my >= by && my < by + 32) {
                    if (ClockHudRenderer.getClientGold() >= getVaultGoldCost(bundleDef) && client != null) {
                        ClientPlayNetworking.send(new ModPayloads.BundleVaultBuyC2SPayload(bundleId));
                        ClientPlayNetworking.send(new ModPayloads.BundleDataRequestC2SPayload());
                    }
                    return true;
                }
            }
        } else {
            if (isCompleted && !rewardClaimedBundleIds.contains(bundleId)) {
                int detailX = (width - 34) / 2, detailY = 32;
                int rewardX = detailX + 44;
                if (mx >= rewardX && mx < rewardX + 16 && my >= detailY && my < detailY + 16) {
                    ClientPlayNetworking.send(new ModPayloads.BundleClaimRewardC2SPayload(bundleId));
                    ClientPlayNetworking.send(new ModPayloads.BundleDataRequestC2SPayload());
                    return true;
                }
            }
            for (int slot = 0; slot < 36; slot++) {
                int col = slot % 9, row = slot / 9;
                int sx = INV_GRID_X + col * SLOT_SIZE, sy = INV_GRID_Y_OFFSET + row * SLOT_SIZE;
                if (mx >= sx && mx < sx + SLOT_SIZE && my >= sy && my < sy + SLOT_SIZE) {
                    handleSlotClick(slot);
                    return true;
                }
            }
        }
        return false;
    }

    private void handleSlotClick(int slot) {
        if (client == null || client.player == null || bundleDef == null) return;
        if (completedBundles.getOrDefault(bundleId, false)) return;

        int invIndex = slot < 9 ? slot + 27 : slot - 9;
        ItemStack stack = client.player.getInventory().getStack(invIndex);
        if (stack.isEmpty()) return;

        String path = Registries.ITEM.getId(stack.getItem()).getPath();
        if (!Registries.ITEM.getId(stack.getItem()).getNamespace().equals(StardewValley.MOD_ID)) return;

        for (BundleManager.BundleItemDef req : bundleDef.requiredItems()) {
            if (itemMatchesRequired(path, req.itemId())) {
                int submitted = submittedItems.getOrDefault(req.itemId(), 0);
                if (submitted < req.count()) {
                    ClientPlayNetworking.send(new ModPayloads.BundleSubmitItemC2SPayload(bundleId, slot));
                    ClientPlayNetworking.send(new ModPayloads.BundleDataRequestC2SPayload());
                }
                return;
            }
        }
    }

    // ===== 渲染 =====

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        if (bundleDef != null) {
            context.drawText(textRenderer, Text.literal(bundleDef.name()),
                width / 2 - textRenderer.getWidth(bundleDef.name()) / 2, 8, 0xFFFFAA, false);
        }

        if (!dataLoaded || bundleDef == null) {
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.bundle_detail.loading"), width / 2 - 20, height / 2, 0x808080, false);
            super.render(context, mouseX, mouseY, delta);
            return;
        }

        boolean isCompleted = completedBundles.getOrDefault(bundleId, false);
        boolean rewardClaimed = rewardClaimedBundleIds.contains(bundleId);

        if (isVaultBundleDef(bundleDef)) {
            renderVaultBundle(context, mouseX, mouseY, isCompleted, rewardClaimed);
        } else {
            renderRegularBundle(context, isCompleted, rewardClaimed);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderVaultBundle(DrawContext context, int mouseX, int mouseY, boolean completed, boolean claimed) {
        // 1. 绘制详情图（34x34）
        int detailX = (width - 34) / 2, detailY = 32;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, getBundleDetailTexture(), detailX, detailY, 0, 0, 34, 34, 34, 34);

        // 2. 奖励物品
        int rewardX = detailX + 44;
        Identifier rewardId = Identifier.of(StardewValley.MOD_ID, bundleDef.rewardItemId());
        ItemStack rewardStack = new ItemStack(Registries.ITEM.get(rewardId), 1);
        boolean purchased = vaultPurchased.contains(bundleId);
        if (!purchased) {
            context.fill(rewardX, detailY, rewardX + 18, detailY + 18, 0xFF333333);
            context.fill(rewardX + 1, detailY + 1, rewardX + 17, detailY + 17, 0xFF448844);
        } else {
            context.fill(rewardX, detailY, rewardX + 18, detailY + 18, 0xFF8B8B8B);
            context.fill(rewardX + 1, detailY + 1, rewardX + 17, detailY + 17, 0xFFC6C6C6);
        }
        if (!rewardStack.isEmpty() && !purchased) {
            context.drawItem(rewardStack, rewardX + 1, detailY + 1);
            drawQuantity(context, bundleDef.rewardCount(), rewardX + 2, detailY + 2);
        }

        // 3. 购买按钮
        int bx = (width - 64) / 2, by = 80;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, PURCHASE_ICON, bx, by, 0, 0, 64, 32, 64, 32);

        if (!purchased) {
            context.drawText(textRenderer, Text.literal(getVaultGoldCost(bundleDef) + "g"), bx + 5, by + 34, 0xFFFF55, false);
            context.drawText(textRenderer, ClockHudRenderer.getClientGold() >= getVaultGoldCost(bundleDef)
                    ? Text.translatable("gui.stardewvalley.bundle_detail.click_to_buy")
                    : Text.translatable("gui.stardewvalley.bundle_detail.not_enough"),
                bx + 5, by + 46, ClockHudRenderer.getClientGold() >= getVaultGoldCost(bundleDef) ? 0x00FF00 : 0xFF5555, false);
        } else {
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.bundle_detail.purchased"), bx + 5, by + 34, 0x00AA00, false);
        }
    }

    private void renderRegularBundle(DrawContext context, boolean isCompleted, boolean rewardClaimed) {
        int detailX = (width - 34) / 2;
        int detailY = 32;

        // 1. 34x34 详情图
        context.drawTexture(RenderPipelines.GUI_TEXTURED, getBundleDetailTexture(), detailX, detailY, 0, 0, 34, 34, 34, 34);

        // 2. 奖励物品
        int rewardX = detailX + 44;
        Identifier rewardId = Identifier.of(StardewValley.MOD_ID, bundleDef.rewardItemId());
        ItemStack rewardStack = new ItemStack(Registries.ITEM.get(rewardId), 1);

        boolean canClaim = isCompleted && !rewardClaimed;
        if (canClaim) {
            context.fill(rewardX, detailY, rewardX + 18, detailY + 18, 0xFF333333);
            context.fill(rewardX + 1, detailY + 1, rewardX + 17, detailY + 17, 0xFF448844);
        } else {
            context.fill(rewardX, detailY, rewardX + 18, detailY + 18, 0xFF8B8B8B);
            context.fill(rewardX + 1, detailY + 1, rewardX + 17, detailY + 17, 0xFFC6C6C6);
        }
        if (!rewardStack.isEmpty() && !rewardClaimed) {
            context.drawItem(rewardStack, rewardX + 1, detailY + 1);
            drawQuantity(context, bundleDef.rewardCount(), rewardX + 2, detailY + 2);
        }

        if (canClaim) {
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.bundle_detail.claim"), rewardX, detailY + 19, 0x00FF00, false);
        } else if (rewardClaimed) {
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.bundle_detail.claimed"), rewardX, detailY + 19, 0xAAAAAA, false);
        }

        // 3. 所需物品列表（水平排列，每行最多6个）
        int listY = detailY + 42;
        int perRow = 6;
        int itemSize = 18;
        int gap = 4;

        for (int i = 0; i < bundleDef.requiredItems().size(); i++) {
            BundleManager.BundleItemDef req = bundleDef.requiredItems().get(i);
            int submitted = submittedItems.getOrDefault(req.itemId(), 0);
            boolean fulfilled = submitted >= req.count();

            int col = i % perRow;
            int row = i / perRow;
            int ix = 10 + col * (itemSize + gap + 50);
            int iy = listY + row * 24;

            // 物品图标背景
            context.fill(ix, iy, ix + itemSize, iy + itemSize, fulfilled ? 0xFF335533 : 0xFF555555);
            context.fill(ix + 1, iy + 1, ix + itemSize - 1, iy + itemSize - 1, fulfilled ? 0xFF446644 : 0xFF6B6B6B);

            String displayId = getDisplayItemId(req.itemId());
            Identifier reqId = Identifier.of(StardewValley.MOD_ID, displayId);
            ItemStack reqStack = new ItemStack(Registries.ITEM.get(reqId), 1);
            if (!reqStack.isEmpty()) {
                context.drawItem(reqStack, ix + 1, iy + 1);
            }

            // 数量标签在图标右下侧: "X 数量"
            drawQuantity(context, req.count(), ix + 2, iy + 2);

            // 右侧文字: 已提交/需要 和 短名
            String countStr = submitted + "/" + req.count();
            context.drawText(textRenderer, Text.literal(countStr), ix + itemSize + 2, iy, fulfilled ? 0x00DD00 : 0xFFFFFF, false);
            String shortName = req.itemId().replace('_', ' ');
            if (shortName.length() > 8) shortName = shortName.substring(0, 8);
            context.drawText(textRenderer, Text.literal(shortName), ix + itemSize + 2, iy + 10, 0xAAAAAA, false);
        }

        // 4. Bundle_Slot行
        int slotCount = bundleDef.slotCount();
        if (slotCount > 0) {
            int slotY = listY + ((bundleDef.requiredItems().size() + perRow - 1) / perRow) * 24 + 5;
            if (slotY < 80) slotY = 80;
            int slotStartX = Math.max(10, (width - slotCount * 20) / 2);

            // 对"任选N"类收集包，按提交顺序显示物品
            boolean isAnyOfN = bundleId.equals("craft_exotic") || bundleId.equals("missing_joja") || bundleId.equals("fish_crab");

            for (int si = 0; si < slotCount; si++) {
                int sx = slotStartX + si * 20;
                context.drawTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT, sx, slotY, 0, 0, 18, 18, 18, 18);

                BundleManager.BundleItemDef req = null;
                boolean fulfilled = false;
                if (isAnyOfN) {
                    // 任选N：按提交顺序取第si个已完成的物品
                    int found = 0;
                    for (BundleManager.BundleItemDef r : bundleDef.requiredItems()) {
                        int sub = submittedItems.getOrDefault(r.itemId(), 0);
                        if (sub >= r.count()) {
                            if (found == si) { req = r; fulfilled = true; break; }
                            found++;
                        }
                    }
                } else if (si < bundleDef.requiredItems().size()) {
                    req = bundleDef.requiredItems().get(si);
                    fulfilled = submittedItems.getOrDefault(req.itemId(), 0) >= req.count();
                }

                if (req != null && fulfilled) {
                    Identifier itemId = Identifier.of(StardewValley.MOD_ID, req.itemId());
                    ItemStack itemStack = new ItemStack(Registries.ITEM.get(itemId), 1);
                    if (!itemStack.isEmpty()) {
                        context.drawItem(itemStack, sx, slotY);
                        drawQuantity(context, req.count(), sx + 2, slotY + 2);
                    }
                }
            }

            if (isCompleted) {
                context.drawText(textRenderer, Text.translatable("gui.stardewvalley.bundle_detail.complete"),
                    slotStartX + (slotCount * 20) / 2 - 20, slotY + 20, 0x00FF00, false);
            }
        }

        // 5. 背包
        renderInventory(context);
    }

    private void renderInventory(DrawContext context) {
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.bundle_detail.inventory"), INV_GRID_X, INV_GRID_Y_OFFSET - 10, 0xFFFFFF, false);

        for (int slot = 0; slot < 36; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int sx = INV_GRID_X + col * SLOT_SIZE;
            int sy = INV_GRID_Y_OFFSET + row * SLOT_SIZE;

            context.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0xFF8B8B8B);
            context.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFFC6C6C6);

            int invIndex = slot < 9 ? slot + 27 : slot - 9;
            ItemStack stack = client != null && client.player != null
                ? client.player.getInventory().getStack(invIndex) : ItemStack.EMPTY;

            if (!stack.isEmpty()) {
                context.drawItem(stack, sx + 1, sy + 1);
                context.drawStackOverlay(textRenderer, stack, sx + 1, sy + 1);

                if (bundleDef != null && !completedBundles.getOrDefault(bundleId, false)) {
                    String path = Registries.ITEM.getId(stack.getItem()).getPath();
                    boolean canSubmit = false;
                    if (Registries.ITEM.getId(stack.getItem()).getNamespace().equals(StardewValley.MOD_ID)) {
                        for (BundleManager.BundleItemDef req : bundleDef.requiredItems()) {
                            if (itemMatchesRequired(path, req.itemId())) {
                                if (submittedItems.getOrDefault(req.itemId(), 0) < req.count()) {
                                    canSubmit = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (canSubmit) {
                        context.fill(sx, sy, sx + SLOT_SIZE, sy + 2, 0xFF00DD00);
                    }
                }
            }
        }
    }

    private String getCategoryDisplayName() {
        return switch (categoryId) {
            case "craft" -> "工艺室";
            case "pantry" -> "茶水间";
            case "fish" -> "鱼缸";
            case "boiler" -> "锅炉房";
            case "bulletin" -> "布告栏";
            case "vault" -> "金库";
            default -> "";
        };
    }

    private Identifier getBundleDetailTexture() {
        return Identifier.of(StardewValley.MOD_ID, "textures/bundles/" + getBundleDetailFileName());
    }

    private String getBundleDetailFileName() {
        return switch (bundleId) {
            case "craft_spring" -> "spring_foraging_bundle.png";
            case "craft_summer" -> "summer_foraging_bundle.png";
            case "craft_fall" -> "fall_foraging_bundle.png";
            case "craft_winter" -> "winter_foraging_bundle.png";
            case "craft_construction" -> "construction_bundle.png";
            case "craft_exotic" -> "exotic_foraging_bundle.png";
            case "pantry_spring" -> "spring_crops_bundle.png";
            case "pantry_summer" -> "summer_crops_bundle.png";
            case "pantry_fall" -> "fall_crops_bundle.png";
            case "pantry_quality" -> "quality_crops_bundle.png";
            case "pantry_animal" -> "animal_bundle.png";
            case "pantry_artisan" -> "artisan_bundle.png";
            case "fish_river" -> "river_fish_bundle.png";
            case "fish_lake" -> "lake_fish_bundle.png";
            case "fish_ocean" -> "ocean_fish_bundle.png";
            case "fish_night" -> "night_fishing_bundle.png";
            case "fish_crab" -> "crab_pot_bundle.png";
            case "fish_specialty" -> "specialty_fish_bundle.png";
            case "boiler_blacksmith" -> "blacksmith_bundle.png";
            case "boiler_geologist" -> "geologists_bundle.png";
            case "boiler_adventurer" -> "adventurers_bundle.png";
            case "bulletin_chef" -> "chefs_bundle.png";
            case "bulletin_dye" -> "dye_bundle.png";
            case "bulletin_field" -> "field_research_bundle.png";
            case "bulletin_fodder" -> "fodder_bundle.png";
            case "bulletin_enchanter" -> "enchanters_bundle.png";
            case "vault_2500" -> "2500_bundle.png";
            case "vault_5000" -> "5000_bundle.png";
            case "vault_10000" -> "10000_bundle.png";
            case "vault_25000" -> "25000_bundle.png";
            case "missing_joja" -> "the_missing_bundle.png";
            default -> "spring_foraging_bundle.png";
        };
    }

    /** 使用LetterRenderer和NumberRenderer在图标右下侧绘制 "X数量" */
    private void drawQuantity(DrawContext context, int amount, int iconRight, int iconBottom) {
        float scale = 0.2f;
        LetterRenderer.drawLetter(context, 'X', iconRight, iconBottom, scale);
        int xOff = LetterRenderer.charWidth(scale);
        NumberRenderer.drawNumber(context, amount, iconRight + xOff, iconBottom, scale);
    }

    public String getCurrentBundleId() {
        return bundleId;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
