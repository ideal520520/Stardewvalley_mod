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

import java.util.*;

public class MuseumDonationScreen extends Screen {

    private static final Identifier GUNTHER_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/Gunther_Icon.png");
    private static final Identifier RUSTY_KEY_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/rusty_key.png");
    private static final Identifier DWARF_TRANSLATION_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/dwarvish_translation_guide.png");

    private static final int MARGIN_X = 8;
    private static final int SLOT_SIZE = 18;
    private static final int GUIDE_CELL = 22;
    // 右侧奖励面板
    private static final int REWARD_ICON = 26;  // 外框
    private static final int ICON_SIZE = 24;     // 贴图尺寸
    private static final int CLAIM_ROW_H = 26;   // 已领取历史每行高度
    private static final int REWARD_PANEL_W = 200;
    private static final int INV_GRID_Y_OFFSET = 100;

    private int invGridX;
    private int invGridY;
    private int guideCols;
    private int rewardPanelX;
    private int guideMaxRight;

    private List<String> donatedItems = new ArrayList<>();
    private Set<Integer> claimedRewardIndices = new HashSet<>();
    private Set<String> abilities = new HashSet<>();

    private int guideScrollOffset = 0;
    private boolean dataLoaded = false;

    public static Set<String> clientAbilities = new HashSet<>();

    private static final int TOTAL_REWARDS = MuseumManager.REWARDS.size();

    public MuseumDonationScreen() {
        super(Text.translatable("gui.stardewvalley.museum.title"));
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.MuseumDataRequestC2SPayload());
        invGridX = MARGIN_X;
        invGridY = this.height - INV_GRID_Y_OFFSET;
        rewardPanelX = this.width - REWARD_PANEL_W;
        guideMaxRight = rewardPanelX - 10;
        guideCols = Math.max(4, (guideMaxRight - MARGIN_X) / GUIDE_CELL);
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.museum.back"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
    }

    public void onDataSync(List<String> donated, Set<Integer> claimed, Set<String> abilities) {
        this.donatedItems = new ArrayList<>(donated);
        this.claimedRewardIndices = new HashSet<>(claimed);
        this.abilities = new HashSet<>(abilities);
        clientAbilities = new HashSet<>(abilities);
        this.dataLoaded = true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        // 主奖励槽点击（当前可领取的奖励）
        int totalDonated = donatedItems.size();
        boolean dwarfAvailable = MuseumManager.DWARF_SCROLLS.stream().allMatch(s -> donatedItems.contains(s))
            && !abilities.contains("dwarf_translation");
        int firstClaimable = -1;
        if (!dwarfAvailable) {
            for (int i = 0; i < TOTAL_REWARDS; i++) {
                if (totalDonated >= MuseumManager.REWARDS.get(i).requiredCount() && !claimedRewardIndices.contains(i)) {
                    firstClaimable = i;
                    break;
                }
            }
        }
        int slotX = rewardPanelX + 10;
        int slotY = 42;
        if (mx >= slotX && mx < slotX + REWARD_ICON + 100 && my >= slotY && my < slotY + REWARD_ICON) {
            if (dwarfAvailable) {
                ClientPlayNetworking.send(new ModPayloads.MuseumClaimRewardC2SPayload(-1));
                return true;
            }
            if (firstClaimable >= 0) {
                ClientPlayNetworking.send(new ModPayloads.MuseumClaimRewardC2SPayload(firstClaimable));
                return true;
            }
        }

        // 背包格子点击
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
                Identifier id = Registries.ITEM.getId(stack.getItem());
                if (!id.getNamespace().equals(StardewValley.MOD_ID)) return true;
                if (!MuseumManager.isDonatable(id.getPath())) return true;
                ClientPlayNetworking.send(new ModPayloads.MuseumDonateC2SPayload(slot));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseY < invGridY) {
            guideScrollOffset = (int) Math.max(0, guideScrollOffset - verticalAmount * GUIDE_CELL);
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        int totalDonated = donatedItems.size();

        // 标题
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GUNTHER_ICON, MARGIN_X, 5, 0, 0, 20, 22, 20, 22);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.museum.title"), MARGIN_X + 24, 8, 0xFFFFAA, false);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.museum.donated", totalDonated), MARGIN_X + 24, 20, 0xAAAAAA, false);

        if (!dataLoaded) {
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.museum.loading"), width / 2 - 20, height / 2, 0x808080, false);
            super.render(context, mouseX, mouseY, delta);
            return;
        }

        // ===== 右侧面板 =====
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.museum.rewards"), rewardPanelX + 4, 30, 0x88FF88, false);

        // 特殊奖励：矮人语教程（全服集齐4卷轴且玩家未拥有时显示）
        boolean dwarfAvailable = MuseumManager.DWARF_SCROLLS.stream().allMatch(s -> donatedItems.contains(s))
            && !abilities.contains("dwarf_translation");

        // 找当前应显示的奖励（特殊奖励优先，其次是里程碑奖励）
        int firstClaimable = -1;
        int firstUnclaimed = -1;
        // 先检查特殊奖励
        if (!dwarfAvailable) {
            for (int i = 0; i < TOTAL_REWARDS; i++) {
                boolean claimed = claimedRewardIndices.contains(i);
                if (!claimed && firstUnclaimed < 0) firstUnclaimed = i;
                if (totalDonated >= MuseumManager.REWARDS.get(i).requiredCount() && !claimed) {
                    firstClaimable = i;
                    break;
                }
            }
        }
        int displayIdx = dwarfAvailable ? -2 : (firstClaimable >= 0 ? firstClaimable : firstUnclaimed);

        // 1. 主奖励槽 — 仅26x26小框 + 24x24贴图
        int slotX = rewardPanelX + 10;
        int slotY = 42;

        // 处理矮人语教程特殊奖励
        if (displayIdx == -2) {
            // 可领取
            context.fill(slotX, slotY, slotX + REWARD_ICON, slotY + REWARD_ICON, 0xFF333333);
            context.fill(slotX + 1, slotY + 1, slotX + REWARD_ICON - 1, slotY + REWARD_ICON - 1, 0xFF448844);
            Identifier tex = getAbilityTexture("dwarf_translation");
            if (tex != null) {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, tex,
                    slotX + 1, slotY + 1, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.museum.claim"), slotX + REWARD_ICON + 6, slotY + 6, 0x88FF88, false);
            if (mouseX >= slotX && mouseX < slotX + REWARD_ICON + 100 && mouseY >= slotY && mouseY < slotY + REWARD_ICON) {
                java.util.List<Text> tip = new ArrayList<>();
                tip.add(Text.translatable("gui.stardewvalley.museum.donate_scrolls"));
                tip.add(Text.literal("能力：" + getAbilityName("dwarf_translation")));
                tip.add(Text.translatable("gui.stardewvalley.museum.click_to_claim"));
                context.drawTooltip(textRenderer, tip, mouseX, mouseY);
            }
        } else if (displayIdx >= 0) {
            MuseumManager.RewardDef def = MuseumManager.REWARDS.get(displayIdx);
            boolean canClaim = displayIdx == firstClaimable;
            if (canClaim) {
                // 像商店可购买物品一样渲染绿色
                context.fill(slotX, slotY, slotX + REWARD_ICON, slotY + REWARD_ICON, 0xFF333333);
                context.fill(slotX + 1, slotY + 1, slotX + REWARD_ICON - 1, slotY + REWARD_ICON - 1, 0xFF448844);
            } else {
                context.fill(slotX, slotY, slotX + REWARD_ICON, slotY + REWARD_ICON, 0xFF444444);
                context.fill(slotX + 1, slotY + 1, slotX + REWARD_ICON - 1, slotY + REWARD_ICON - 1, 0xFF222222);
            }
            // 贴图：能力用drawTexture，物品用drawItem（支持方块物品）
            if (def.isAbility()) {
                Identifier tex = getAbilityTexture(def.abilityId());
                if (tex != null) {
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, tex,
                        slotX + 1, slotY + 1, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
                }
            } else if (def.rewardItemId() != null) {
                Identifier itemId = Identifier.of(StardewValley.MOD_ID, def.rewardItemId());
                Item rItem = Registries.ITEM.get(itemId);
                if (rItem != null && rItem != net.minecraft.item.Items.AIR) {
                    context.drawItem(new ItemStack(rItem, def.rewardCount()), slotX + 4, slotY + 4);
                }
            }
            Text txt = canClaim
                ? Text.translatable("gui.stardewvalley.museum.claim")
                : Text.translatable("gui.stardewvalley.museum.donated_count", def.requiredCount());
            int txtColor = canClaim ? 0x88FF88 : 0x888888;
            context.drawText(textRenderer, txt, slotX + REWARD_ICON + 6, slotY + 6, txtColor, false);

            if (mouseX >= slotX && mouseX < slotX + REWARD_ICON + 100 && mouseY >= slotY && mouseY < slotY + REWARD_ICON) {
                context.fill(slotX, slotY, slotX + REWARD_ICON, slotY + REWARD_ICON, 0x44FFFFFF);
                java.util.List<Text> tip = new ArrayList<>();
                tip.add(Text.translatable("gui.stardewvalley.museum.donations", def.requiredCount()));
                if (def.isAbility()) tip.add(Text.literal("能力：" + getAbilityName(def.abilityId())));
                else if (def.rewardItemId() != null) tip.add(Text.literal(getRewardItemName(def.rewardItemId()) + " x" + def.rewardCount()));
                if (canClaim) tip.add(Text.translatable("gui.stardewvalley.museum.click_to_claim"));
                else tip.add(Text.translatable("gui.stardewvalley.museum.donate_more", def.requiredCount() - totalDonated));
                context.drawTooltip(textRenderer, tip, mouseX, mouseY);
            }
        } else {
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.museum.all_done"),
                slotX + REWARD_ICON + 6, slotY + 6, 0x88FF88, false);
        }

        // ===== 左侧已捐赠物品展示 =====
        int guideY = 40;
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.museum.donated_items", totalDonated), MARGIN_X, guideY - 10, 0xFFFFFF, false);

        if (!donatedItems.isEmpty()) {
            context.enableScissor(MARGIN_X, guideY, guideMaxRight - MARGIN_X, invGridY - guideY);
            for (int idx = 0; idx < donatedItems.size(); idx++) {
                String itemId = donatedItems.get(idx);
                int ix = MARGIN_X + (idx % guideCols) * GUIDE_CELL;
                int iy = guideY + (idx / guideCols) * GUIDE_CELL - guideScrollOffset;
                if (iy < guideY || iy + GUIDE_CELL > invGridY) continue;
                Identifier itemIdr = Identifier.of(StardewValley.MOD_ID, itemId);
                Item item = Registries.ITEM.get(itemIdr);
                if (item != null && item != net.minecraft.item.Items.AIR) {
                    context.drawItem(new ItemStack(item), ix + 3, iy + 3);
                }
            }
            context.disableScissor();
        } else {
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.museum.no_items"), MARGIN_X, guideY + 20, 0x808080, false);
        }

        renderInventory(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private String getAbilityName(String abilityId) {
        return switch (abilityId) {
            case "rusty_key" -> "生锈的钥匙";
            case "dwarf_translation" -> "矮人语翻译";
            default -> abilityId;
        };
    }

    private String getRewardItemName(String itemId) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, itemId);
        Item item = Registries.ITEM.get(id);
        if (item != null && item != net.minecraft.item.Items.AIR) {
            return new ItemStack(item).getName().getString();
        }
        return itemId;
    }

    private Identifier getAbilityTexture(String abilityId) {
        return switch (abilityId) {
            case "rusty_key" -> RUSTY_KEY_TEXTURE;
            case "dwarf_translation" -> DWARF_TRANSLATION_TEXTURE;
            default -> null;
        };
    }

    private void renderInventory(DrawContext context) {
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.museum.inventory"), invGridX, invGridY - 12, 0xFFFFFF, false);
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
                if (id.getNamespace().equals(StardewValley.MOD_ID) && MuseumManager.isDonatable(id.getPath())
                    && !donatedItems.contains(id.getPath())) {
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
