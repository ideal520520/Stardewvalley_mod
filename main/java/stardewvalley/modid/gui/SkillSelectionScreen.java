package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import stardewvalley.modid.skill.SkillRegistry;

import java.util.List;

public class SkillSelectionScreen extends Screen {

    private final SkillRegistry.Category category;
    private boolean showingTier1 = true;

    private static final int ICON_SIZE = 36;
    private static final int ICON_GAP = 20;

    private SkillRegistry.SkillEntry pendingSkill = null;
    private long lastClickTime = 0;

    public SkillSelectionScreen(SkillRegistry.Category category) {
        super(Text.literal(getCategoryName(category) + " 技能"));
        this.category = category;
    }

    private static String getCategoryName(SkillRegistry.Category cat) {
        return switch (cat) {
            case FARMING -> "耕种";
            case FORAGING -> "采集";
            case FISHING -> "钓鱼";
            case MINING -> "挖矿";
            case COMBAT -> "战斗";
        };
    }

    @Override
    protected void init() {
        String tier1Choice = getClientTier1();
        int level = getClientLevel();
        if (tier1Choice != null && level >= 10) {
            showingTier1 = false;
        } else {
            showingTier1 = true;
        }

        addDrawableChild(ButtonWidget.builder(
            Text.literal("< 返回"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
    }

    private String getClientTier1() {
        return ClockHudRenderer.getClientSkill(category, 1);
    }

    private int getClientLevel() {
        return switch (category) {
            case FARMING -> ClockHudRenderer.getClientFarmingLevel();
            case FORAGING -> ClockHudRenderer.getClientForagingLevel();
            case FISHING -> ClockHudRenderer.getClientFishingLevel();
            case MINING -> ClockHudRenderer.getClientMiningLevel();
            case COMBAT -> ClockHudRenderer.getClientCombatLevel();
        };
    }

    private List<SkillRegistry.SkillEntry> getSkills() {
        if (showingTier1) {
            return SkillRegistry.getTier1ByCategory(category);
        } else {
            return SkillRegistry.getAvailableTier2(category, getClientTier1());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xAA000000);

        String title = getCategoryName(category) + "技能 - " + (showingTier1 ? "5级" : "10级");
        context.drawText(textRenderer, Text.literal(title), (width - textRenderer.getWidth(title)) / 2, 10, 0xFFD700, false);

        List<SkillRegistry.SkillEntry> skills = getSkills();
        if (skills.isEmpty()) return;

        int totalW = skills.size() * ICON_SIZE + (skills.size() - 1) * ICON_GAP;
        int startX = (width - totalW) / 2;
        int iconY = (height - ICON_SIZE) / 2;

        String selectedTier1 = getClientTier1();
        String selectedTier2 = ClockHudRenderer.getClientSkill(category, 2);

        SkillRegistry.SkillEntry hoveredSkill = null;

        for (int i = 0; i < skills.size(); i++) {
            SkillRegistry.SkillEntry skill = skills.get(i);
            int ix = startX + i * (ICON_SIZE + ICON_GAP);
            boolean isSelected = showingTier1 ? skill.id.equals(selectedTier1) : skill.id.equals(selectedTier2);
            boolean isHovered = mouseX >= ix && mouseX < ix + ICON_SIZE && mouseY >= iconY && mouseY < iconY + ICON_SIZE;

            if (isHovered) hoveredSkill = skill;

            if (isSelected) {
                context.fill(ix - 2, iconY - 2, ix + ICON_SIZE + 2, iconY + ICON_SIZE + 2, 0xFF44AA44);
            }

            context.drawTexture(RenderPipelines.GUI_TEXTURED, skill.texture, ix, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            if (!skill.enabled) {
                context.fill(ix, iconY, ix + ICON_SIZE, iconY + ICON_SIZE, 0x88000000);
            }
        }

        // 悬浮显示详情（tooltip风格）
        if (hoveredSkill != null) {
            java.util.List<Text> lines = new java.util.ArrayList<>();
            lines.add(Text.literal("§e" + hoveredSkill.name + " §7(" + (showingTier1 ? "5级" : "10级") + ")"));
            lines.add(Text.literal("§7" + hoveredSkill.description));
            if (!showingTier1 && hoveredSkill.prerequisite != null) {
                SkillRegistry.SkillEntry preq = SkillRegistry.get(hoveredSkill.prerequisite);
                if (preq != null) {
                    lines.add(Text.literal("§8前置: " + preq.name));
                }
            }
            context.drawTooltip(textRenderer, lines, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mouseX = click.x();
        double mouseY = click.y();

        List<SkillRegistry.SkillEntry> skills = getSkills();
        if (skills.isEmpty()) return true;

        int totalW = skills.size() * ICON_SIZE + (skills.size() - 1) * ICON_GAP;
        int startX = (width - totalW) / 2;
        int iconY = (height - ICON_SIZE) / 2;

        for (int i = 0; i < skills.size(); i++) {
            SkillRegistry.SkillEntry skill = skills.get(i);
            int ix = startX + i * (ICON_SIZE + ICON_GAP);

            if (mouseX >= ix && mouseX < ix + ICON_SIZE && mouseY >= iconY && mouseY < iconY + ICON_SIZE) {
                if (!skill.enabled) {
                    if (client != null && client.player != null) {
                        client.player.sendMessage(Text.literal("§c该技能尚未实现"), false);
                    }
                    return true;
                }

                int level = getClientLevel();
                if (showingTier1 && level < 5) return true;
                if (!showingTier1 && level < 10) return true;

                long now = System.currentTimeMillis();
                if (skill == pendingSkill && (now - lastClickTime) < 500) {
                    ClientPlayNetworking.send(new ModPayloads.SkillSelectC2SPayload(
                        category.ordinal(), showingTier1 ? 1 : 2, skill.id));
                    if (client != null) {
                        client.player.sendMessage(Text.literal("§a已选择技能: " + skill.name), false);
                        client.setScreen(null);
                    }
                } else {
                    pendingSkill = skill;
                    lastClickTime = now;
                }
                return true;
            }
        }

        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
