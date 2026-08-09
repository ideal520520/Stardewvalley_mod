package stardewvalley.modid.fishing;

import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class FishingMinigame {
    public static final int POINTS_TO_FINISH = 120;
    public static final float CHEST_PROGRESS_MAX = POINTS_TO_FINISH * 0.67F;
    public static final float CHEST_CHANCE = 0.15F;

    private static final float UP_ACCELERATION = 0.58F;
    private static final float GRAVITY = -0.7F;
    private static final int MAX_BOBBER_HEIGHT = 106;
    private static final int MAX_FISH_HEIGHT = FishBehavior.MAX_HEIGHT;

    private final Random random = new Random();
    private final FishingScreen screen;
    private final FishBehavior behavior;

    private double bobberPos = 0;
    private double bobberVelocity = 0;

    private double fishPos = 0;
    private double fishVelocity = 0;
    private int fishTarget = -1;
    private boolean fishIsIdle = false;
    private int fishIdleTicks = 0;

    private boolean bobberOnFish = true;
    private float points = POINTS_TO_FINISH / 5F;
    private int successTicks = 0;
    private int totalTicks = 0;

    private int bobberMinOffset = -2;
    private int bobberMaxOffset = 24;
    private int fishingLevel = 0;

    // 宝箱相关
    private boolean hasChest = false;
    private double chestPos = 0;
    private float chestProgress = 0;
    private boolean chestObtained = false;

    // 渔具效果
    private boolean hasLeadBobber = false;
    private boolean hasTrapBobber = false;
    private boolean hasTreasureHunter = false;
    private boolean hasBarbedHook = false;
    private int extraBarHeight = 0;

    /** 累积宝箱概率附加（每个寻宝者 +5%，累加） */
    private float chestChanceBonus = 0f;
    /** 磁铁鱼饵倍数（默认 1，有磁铁则 2） */
    private float magnetMultiplier = 1f;

    // 挑战鱼饵相关
    private boolean challengeBaitActive = false;
    private int outOfBarTicks = 0;

    public static int getBobberBarHeight(int level) {
        return 24 + level * 2;
    }

    public FishingMinigame(FishingScreen screen, FishBehavior behavior) {
        this.screen = screen;
        this.behavior = behavior;
        applyFishingLevel(0);
        // 宝箱判定延迟到所有修饰累积完毕后再执行
    }

    /** 磁铁鱼饵：宝箱概率 ×2 */
    public void setMagnetBait() {
        magnetMultiplier = 2f;
    }

    /** 挑战鱼饵：启用出栏计时 */
    public void setChallengeBaitActive(boolean active) {
        this.challengeBaitActive = active;
    }

    /** 寻宝者：额外加 bonus 概率（每个 +0.05） */
    public void setChestChanceBonus(float bonus) {
        chestChanceBonus += bonus;
    }

    /** 所有修饰累积完毕后，统一执行一次宝箱判定 */
    private float finalChestChance = 0f;

    public void finalizeChestRoll() {
        finalChestChance = (CHEST_CHANCE + chestChanceBonus) * magnetMultiplier;
        if (random.nextFloat() < finalChestChance) {
            hasChest = true;
            chestPos = random.nextDouble() * MAX_FISH_HEIGHT;
        }
    }

    /** 宝箱最终出现概率（用于服务端金色宝箱升级判定） */
    public float getFinalChestChance() { return finalChestChance; }

    public void setFishingLevel(int level) {
        this.fishingLevel = level;
        applyFishingLevel(level);
    }

    public void setTackleEffects(boolean leadBobber, boolean trapBobber, boolean treasureHunter, int corkBobberCount, boolean barbedHook, boolean deluxeBait) {
        this.hasLeadBobber = leadBobber;
        this.hasTrapBobber = trapBobber;
        this.hasTreasureHunter = treasureHunter;
        this.hasBarbedHook = barbedHook;
        this.extraBarHeight = (corkBobberCount * 6) + (deluxeBait ? 4 : 0);
        applyFishingLevel(fishingLevel);
    }

    private void applyFishingLevel(int level) {
        int barHeight = getBobberBarHeight(level) + extraBarHeight;
        this.bobberMinOffset = -2;
        this.bobberMaxOffset = barHeight - 2;
    }

    // 倒刺钩：鱼在绿色条内时，使绿色条跟随鱼移动（滞后200ms ≈ 4 ticks）
    private double barbedHookTargetOffset = 0;
    private double barbedHookCurrentOffset = 0;

    public void tick(boolean mouseDown) {
        if (mouseDown) {
            if (bobberVelocity < 0) {
                bobberVelocity *= 0.9;
            }
            bobberVelocity += UP_ACCELERATION;
        } else if (bobberPos > 0) {
            bobberVelocity += GRAVITY;
        }

        bobberPos += bobberVelocity;
        if (bobberPos > MAX_BOBBER_HEIGHT) {
            bobberVelocity = 0;
            bobberPos = MAX_BOBBER_HEIGHT;
        } else if (bobberPos <= 0) {
            bobberPos = 0;
            if (hasLeadBobber) {
                bobberVelocity = 0;
            } else {
                if (bobberVelocity < 2 * GRAVITY) {
                    bobberVelocity *= -0.4;
                } else {
                    bobberVelocity = 0;
                }
            }
        }

        if (fishTarget == -1 || behavior.shouldMoveNow(fishIdleTicks, random)) {
            fishTarget = behavior.pickNextTargetPos((int) fishPos, random);
            fishIsIdle = false;
            fishIdleTicks = 0;
        }

        if (fishIsIdle) {
            fishIdleTicks++;
            if (Math.abs(fishVelocity) > 0) {
                boolean up = fishVelocity > 0;
                fishVelocity -= (up ? behavior.upAcceleration() : behavior.downAcceleration()) * Math.signum(fishVelocity);
                if (fishVelocity == 0 || up && fishVelocity < 0 || !up && fishVelocity > 0) {
                    fishVelocity = 0;
                }
            }
        } else {
            double distanceLeft = fishTarget - fishPos;
            double acceleration = (distanceLeft > 0 ? behavior.upAcceleration() : behavior.downAcceleration()) * Math.signum(distanceLeft);
            fishVelocity = MathHelper.clamp(fishVelocity + acceleration, -behavior.topSpeed(), behavior.topSpeed());
        }

        fishPos += fishVelocity;
        if (Math.abs(fishTarget - fishPos) < fishVelocity) {
            fishIsIdle = true;
        } else if (fishPos > MAX_FISH_HEIGHT) {
            fishVelocity = 0;
            fishPos = MAX_FISH_HEIGHT;
            fishIsIdle = true;
        } else if (fishPos < 0) {
            fishVelocity = 0;
            fishPos = 0;
            fishIsIdle = true;
        }

        // 倒刺钩：计算绿色条偏移跟随鱼
        if (hasBarbedHook) {
            int barHeight = getBobberBarHeight(fishingLevel) + extraBarHeight;
            int min = MathHelper.floor(bobberPos) - 2;
            int max = MathHelper.ceil(bobberPos) + barHeight - 2;
            boolean fishInBar = fishPos >= min && fishPos <= max;
            if (fishInBar) {
                // 鱼在条内：目标是让条的中心对准鱼的位置
                double barCenter = bobberPos + (double) barHeight / 2;
                double targetOffset = fishPos - barCenter;
                barbedHookTargetOffset = targetOffset;
            }
            // 平滑跟随（滞后约4tick ≈ 200ms）
            barbedHookCurrentOffset += (barbedHookTargetOffset - barbedHookCurrentOffset) * 0.2;
        }

        int barHeight = getBobberBarHeight(fishingLevel) + extraBarHeight;
        int effectiveMin = MathHelper.floor(bobberPos) - 2;
        int effectiveMax = MathHelper.ceil(bobberPos) + barHeight - 2;

        if (hasBarbedHook) {
            double shift = barbedHookCurrentOffset;
            effectiveMin += (int) Math.round(shift);
            effectiveMax += (int) Math.round(shift);
        }

        boolean wasOnFish = bobberOnFish;
        bobberOnFish = fishPos >= effectiveMin && fishPos <= effectiveMax;

        totalTicks++;
        if (bobberOnFish) {
            successTicks++;
        }

        // 挑战鱼饵：累积鱼出栏时间（ticks），20ticks = 1秒
        if (challengeBaitActive) {
            if (!bobberOnFish) {
                outOfBarTicks++;
            }
            // 出栏超过1.5秒（30 ticks）强制进度归零，走正常失败路径
            if (outOfBarTicks >= 30) {
                points = 0;
            }
        }

        if (wasOnFish != bobberOnFish) {
            screen.playSound(FishingSounds.DWOP);
            screen.reelSoundTimer = 1;
        }

        if (bobberOnFish) {
            points += 1.0F;
        } else {
            float drop = 1.0F;
            if (hasTrapBobber) {
                drop *= 0.67f;
            }
            points -= drop;
        }

        points = Math.min(points, POINTS_TO_FINISH);
        points = Math.max(points, 0);

        // 宝箱进度
        if (hasChest && !chestObtained) {
            int chestMin = effectiveMin;
            int chestMax = effectiveMax;
            boolean chestInBar = chestPos >= chestMin && chestPos <= chestMax;

            if (chestInBar) {
                chestProgress += 4.0F / 3.0F;
            } else if (!hasTreasureHunter) {
                chestProgress -= 2.0F / 3.0F;
                if (chestProgress < 0) chestProgress = 0;
            }

            chestProgress = Math.min(chestProgress, CHEST_PROGRESS_MAX);

            if (chestProgress >= CHEST_PROGRESS_MAX) {
                chestObtained = true;
            }
        }

        if (points >= POINTS_TO_FINISH) {
            screen.setResult(true, (double) successTicks / totalTicks);
        }
        if (points <= 0) {
            screen.setResult(false, 0);
        }
    }

    public float getBobberPos() { return (float) bobberPos; }
    public float getFishPos() { return (float) fishPos; }
    public boolean isBobberOnFish() { return bobberOnFish; }
    public float getProgress() { return points / POINTS_TO_FINISH; }
    public int getSuccessTicks() { return successTicks; }
    public int getTotalTicks() { return totalTicks; }

    public boolean hasChest() { return hasChest; }
    public boolean isChestObtained() { return chestObtained; }
    public float getChestPos() { return (float) chestPos; }
    public float getChestProgress() { return chestProgress / CHEST_PROGRESS_MAX; }
    public float getChestRawProgress() { return chestProgress; }

    public boolean hasTreasureHunter() { return hasTreasureHunter; }
    public int getExtraBarHeight() { return extraBarHeight; }
    public boolean hasLeadBobber() { return hasLeadBobber; }

    // 挑战鱼饵
    public int getOutOfBarTicks() { return outOfBarTicks; }

    /** 挑战鱼饵奖励等级：0~10ticks=3条, 10~20=2条, 20~30=1条, >=30=0条 */
    public int getChallengeBaitRewardTier() {
        if (outOfBarTicks <= 10) return 3;
        if (outOfBarTicks <= 20) return 2;
        if (outOfBarTicks < 30) return 1;
        return 0;
    }

    public boolean isChallengeBaitActive() { return challengeBaitActive; }
}
