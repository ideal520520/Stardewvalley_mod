package stardewvalley.modid.fishing;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.gui.ClockHudRenderer;
import stardewvalley.modid.item.RodComponent;
import stardewvalley.modid.skill.SkillRegistry;

public class FishingScreen extends Screen {
    private static final Text TITLE = Text.literal("Fishing Minigame");
    private static final Identifier TEXTURE = Identifier.of(FishingSounds.MODID, "textures/minigame.png");
    private static final Identifier HIT_TEXTURE = Identifier.of(FishingSounds.MODID, "textures/hit.png");
    private static final Identifier GREENBAR = Identifier.of(FishingSounds.MODID, "textures/greenbar.png");
    private static final Identifier FISH_TEXTURE = Identifier.of(FishingSounds.MODID, "textures/fish.png");
    private static final Identifier FISHKING_TEXTURE = Identifier.of(FishingSounds.MODID, "textures/fishking.png");

    private static final int GUI_W = 38;
    private static final int GUI_H = 152;
    private static final int HIT_W = 73;
    private static final int HIT_H = 29;
    private static final int PERFECT_U = 144;
    private static final int PERFECT_V = 0;
    private static final int PERFECT_W = 40;
    private static final int PERFECT_H = 11;

    private int leftPos, topPos;
    private final FishingMinigame minigame;
    private Status status = Status.HIT_TEXT;
    public double accuracy = -1;
    private boolean mouseDown = false;
    private int animTimer = 0;

    private static final Identifier CHEST_TEXTURE = Identifier.of("stardewvalley", "textures/gui/fishing_treasure_chest.png");

    private final Animation textSize = new Animation(0);
    private final Animation progressBar;

    private final Shake shake = new Shake(0.75F, 1);
    public int reelSoundTimer = -1;
    private java.util.Random creakRandom = new java.util.Random();
    private boolean chestObtained = false;

    private int barHeight;

    // 渔具效果标记
    private boolean hasTreasureHunter = false;
    private boolean hasSonarBobber = false;

    // 挑战鱼饵相关
    private boolean isChallengeBait = false;
    private boolean isLegendaryFish = false;

    // 声呐浮漂：显示鱼的贴图
    private final Identifier fishItemId;
    private ItemStack fishStack = null;

    public FishingScreen(FishBehavior behavior, Identifier fishItemId) {
        super(TITLE);
        this.minigame = new FishingMinigame(this, behavior);
        this.progressBar = new Animation(minigame.getProgress());
        this.fishItemId = fishItemId;
    }

    @Override
    protected void init() {
        leftPos = (width - GUI_W) / 2;
        topPos = (height - GUI_H) / 2;
        int fishingLevel = ClockHudRenderer.getClientFishingLevel();
        if (client != null && client.player != null) {
            fishingLevel += stardewvalley.modid.effect.SkillBuffHelper.getClientFishingBuffLevel(client.player);
        }
        minigame.setFishingLevel(fishingLevel);

        // 读取玩家手竿上的渔具效果和鱼饵
        if (client != null && client.player != null) {
            ItemStack rod = client.player.getMainHandStack();
            boolean isSv = rod.getItem() instanceof stardewvalley.modid.item.ModFishingRodItem;
            if (!isSv) {
                rod = client.player.getOffHandStack();
                isSv = rod.getItem() instanceof stardewvalley.modid.item.ModFishingRodItem;
            }
            if (isSv) {
                RodComponent comp = rod.get(RodComponent.TYPE);
                if (comp != null) {
                    boolean leadBobber = false, trapBobber = false, barbedHook = false;
                    boolean deluxeBait = false;
                    int corkBobberCount = 0;
                    int treasureHunterCount = 0;
                    // 检查鱼饵
                    ItemStack bait = comp.getBait();
                    if (!bait.isEmpty()) {
                        String baitPath = Registries.ITEM.getId(bait.getItem()).getPath();
                        deluxeBait = "bait_deluxe_bait".equals(baitPath);
                        isChallengeBait = "bait_challenge_bait".equals(baitPath);
                        if ("bait_magnet".equals(baitPath)) {
                            minigame.setMagnetBait();
                        }
                    }
                    // 检查渔具
                    for (int i = 0; i < comp.getTackleCount(); i++) {
                        ItemStack tackle = comp.getTackle(i);
                        if (tackle.isEmpty()) continue;
                        String path = Registries.ITEM.getId(tackle.getItem()).getPath();
                        switch (path) {
                            case "fishtool_lead_bobber" -> leadBobber = true;
                            case "fishtool_trap_bobber" -> trapBobber = true;
                            case "fishtool_treasure_hunter" -> treasureHunterCount++;
                            case "fishtool_cork_bobber" -> corkBobberCount++;
                            case "fishtool_barbed_hook" -> barbedHook = true;
                            case "fishtool_sonar_bobber" -> hasSonarBobber = true;
                        }
                    }
                    this.hasTreasureHunter = treasureHunterCount > 0;
                    minigame.setTackleEffects(leadBobber, trapBobber, treasureHunterCount > 0, corkBobberCount, barbedHook, deluxeBait);
                    for (int t = 0; t < treasureHunterCount; t++) {
                        minigame.setChestChanceBonus(0.05f);
                    }
                }
            }
        }

        // 所有宝箱修饰累积完毕后统一判定一次
        // 海盗：钓鱼宝藏概率加倍
        String fishingSkill10 = ClockHudRenderer.getClientSkill(SkillRegistry.Category.FISHING, 2);
        if ("pirate".equals(fishingSkill10)) {
            minigame.setChestChanceBonus(FishingMinigame.CHEST_CHANCE);
        }
        minigame.finalizeChestRoll();

        // 声呐浮漂：准备鱼的ItemStack
        if (hasSonarBobber && fishItemId != null) {
            var item = Registries.ITEM.get(fishItemId);
            if (item != null && item != net.minecraft.item.Items.AIR) {
                fishStack = new ItemStack(item);
            }
        }

        // 判断是否为鱼王（移除品质后缀）
        if (fishItemId != null) {
            String fishPath = fishItemId.getPath();
            String baseName = fishPath;
            if (baseName.endsWith("_iridium")) baseName = baseName.substring(0, baseName.length() - 8);
            else if (baseName.endsWith("_gold")) baseName = baseName.substring(0, baseName.length() - 5);
            else if (baseName.endsWith("_silver")) baseName = baseName.substring(0, baseName.length() - 7);
            this.isLegendaryFish = "fish_crimsonfish".equals(baseName) || "fish_angler".equals(baseName)
                || "fish_legend".equals(baseName) || "fish_glacierfish".equals(baseName)
                || "fish_mutant_carp".equals(baseName);
        }

        // 挑战鱼饵：启用出栏计时（鱼王不适用）
        if (isChallengeBait && !isLegendaryFish) {
            minigame.setChallengeBaitActive(true);
        }

        this.barHeight = FishingMinigame.getBobberBarHeight(fishingLevel) + minigame.getExtraBarHeight();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (status == Status.HIT_TEXT) {
            float scale = textSize.getInterpolated(delta) * 1.5F;
            int x = (int) ((width - HIT_W * scale) / 2);
            int y = (int) ((height - HIT_H * scale) / 3);
            if (scale > 0.01f) {
                int dw = (int) (HIT_W * scale);
                int dh = (int) (HIT_H * scale);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, HIT_TEXTURE, x, y, 71.0f, 0.0f, dw, dh, 256, 256);
            }
        } else {
            context.fill(0, 0, width, height, 0x88000000);

            context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0f, 0.0f, GUI_W, GUI_H, 256, 256);

            // 挑战鱼饵：在UI左侧显示当前奖励等级的鱼图标
            if (isChallengeBait && !isLegendaryFish) {
                int fishIconX = leftPos - 18;
                int fishIconSize = 14;
                int fishSpacing = 16;
                int fishStartY = topPos + 4;
                int rewardTier = minigame.getChallengeBaitRewardTier();
                for (int i = 0; i < 3; i++) {
                    if (i < rewardTier) {
                        context.drawTexture(RenderPipelines.GUI_TEXTURED, FISH_TEXTURE,
                            fishIconX, fishStartY + i * fishSpacing,
                            0.0f, 0.0f, fishIconSize, fishIconSize, fishIconSize, fishIconSize);
                    }
                }
            }

            // 声呐浮漂：在左上角显示鱼贴图
            if (hasSonarBobber && fishStack != null) {
                context.drawItem(fishStack, leftPos + 2, topPos + 4);
            }

            float bobberY = 146 - this.barHeight + (float) minigame.getBobberPos() / 106.0F * (this.barHeight - 142);
            int barX = leftPos + 18;
            int barTopY = (int) (topPos + bobberY);
            int midTiles = (this.barHeight - 24) / 2;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, GREENBAR, barX, barTopY, 38.0f, 0.0f, 8, 12, 256, 256);
            for (int i = 0; i < midTiles; i++) {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, GREENBAR, barX, barTopY + 12 + i * 2, 0.0f, 0.0f, 8, 2, 256, 256);
            }
            context.drawTexture(RenderPipelines.GUI_TEXTURED, GREENBAR, barX, barTopY + 12 + midTiles * 2, 38.0f, 12.0f, 8, 12, 256, 256);

            float fishY = 4 - 16 + (142 - (float) minigame.getFishPos());
            if (isLegendaryFish) {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, FISHKING_TEXTURE, leftPos + 14, (int) (topPos + fishY),
                    0.0f, 0.0f, 16, 16, 16, 16);
            } else {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 14, (int) (topPos + fishY), 55.0f, 0.0f, 16, 15, 256, 256);
            }

            // 宝箱渲染
            if (minigame.hasChest() && !minigame.isChestObtained()) {
                float chestY = 4 - 16 + (142 - minigame.getChestPos());
                int chestScreenY = (int) (topPos + chestY);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, CHEST_TEXTURE, leftPos + 14, chestScreenY, 0.0f, 0.0f, 16, 16, 16, 16);

                float chestProgress = minigame.getChestProgress();
                int chestBarColor = MathHelper.hsvToRgb(chestProgress / 3.0F, 1.0F, 1.0F) | 0xFF000000;
                context.fill(leftPos + 14, chestScreenY - 3, leftPos + 14 + (int)(16 * chestProgress), chestScreenY - 1, chestBarColor);
            }

            float progress = progressBar.getInterpolated(delta);
            int color = MathHelper.hsvToRgb(progress / 3.0F, 1.0F, 1.0F) | 0xFF000000;
            context.fill(leftPos + 33, (int) (topPos + 148), leftPos + 37,
                (int) (topPos + 148 - progress * 145), color);

            context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 5, topPos + 129, 47.0f, 0.0f, 8, 3, 256, 256);

            boolean isPerfect = minigame.getSuccessTicks() == minigame.getTotalTicks() || hasTreasureHunter;
            if (status == Status.SUCCESS && isPerfect) {
                float s = textSize.getInterpolated(delta);
                int px = (int) (leftPos + 2 + (PERFECT_W - PERFECT_W * s) / 2);
                int py = (int) (topPos - PERFECT_H * s);
                int pw = (int) (PERFECT_W * s);
                int ph = (int) (PERFECT_H * s);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, px, py, (float) PERFECT_U, (float) PERFECT_V, pw, ph, 256, 256);
            }
        }
    }

    @Override
    public void tick() {
        shake.tick();

        boolean actuallyDown = false;
        if (client != null && client.getWindow() != null) {
            long handle = client.getWindow().getHandle();
            actuallyDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;
        }
        if (actuallyDown && !mouseDown) {
            playSound(FishingSounds.REEL_CREAK);
        }
        mouseDown = actuallyDown;

        switch (status) {
            case HIT_TEXT -> {
                if (animTimer < 20) {
                    if (++animTimer == 20) {
                        status = Status.MINIGAME;
                        ClientPlayNetworking.send(new C2SMinigameStatePayload(true));
                    } else if (animTimer <= 5) {
                        textSize.addValue(0.2F);
                    } else if (animTimer <= 15) {
                        textSize.addValue(-0.013F);
                    } else {
                        textSize.addValue(-0.16F);
                    }
                }
            }
            case MINIGAME -> {
                minigame.tick(mouseDown);
                progressBar.setValue(minigame.getProgress());

                if (reelSoundTimer == -1 || --reelSoundTimer == 0) {
                    reelSoundTimer = mouseDown ? 30 : 20;
                    playSound(mouseDown ? FishingSounds.REEL_FAST : FishingSounds.REEL_SLOW);
                }
            }
            case SUCCESS, FAILURE -> {
                if (--animTimer == 0) {
                    boolean wasPerfect = minigame.getSuccessTicks() == minigame.getTotalTicks() || hasTreasureHunter;
                    double finalAccuracy = wasPerfect ? 1.0 : accuracy;
                    ClientPlayNetworking.send(new C2SCompleteMinigamePayload(status == Status.SUCCESS, finalAccuracy, chestObtained, minigame.getOutOfBarTicks(), minigame.getFinalChestChance()));
                    client.setScreen(null);
                } else if (animTimer >= 15) {
                    textSize.addValue(0.2F);
                } else if (animTimer >= 5) {
                    textSize.addValue(-0.013F);
                } else {
                    textSize.addValue(-0.16F);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        ClientPlayNetworking.send(new C2SMinigameStatePayload(false));
        // 如果按ESC中途退出（小游戏未正常结束），按失败处理
        if (status == Status.HIT_TEXT || status == Status.MINIGAME) {
            ClientPlayNetworking.send(new C2SCompleteMinigamePayload(false, 0, false, 0, 0f));
        }
    }

    public void setResult(boolean success, double accuracy) {
        status = success ? Status.SUCCESS : Status.FAILURE;
        this.accuracy = accuracy;
        this.chestObtained = minigame.isChestObtained();
        animTimer = 20;
        textSize.reset(0.0F);
        progressBar.freeze();
        if (success) {
            playSound(FishingSounds.COMPLETE);
        } else {
            int idx = creakRandom.nextInt(3);
            SoundEvent creak = switch (idx) {
                case 0 -> SoundEvent.of(Identifier.of(FishingSounds.MODID, "reel_creak1"));
                case 1 -> SoundEvent.of(Identifier.of(FishingSounds.MODID, "reel_creak2"));
                default -> SoundEvent.of(Identifier.of(FishingSounds.MODID, "reel_creak3"));
            };
            playSound(creak);
        }
        shake.setValues(2.0F, 1);
    }

    public void playSound(SoundEvent event) {
        if (client != null && client.world != null) {
            client.world.playSound(client.player, client.player.getBlockPos(),
                event, SoundCategory.MASTER, 1.0F, 1.0F);
        }
    }

    public enum Status {
        HIT_TEXT, MINIGAME, SUCCESS, FAILURE
    }
}
