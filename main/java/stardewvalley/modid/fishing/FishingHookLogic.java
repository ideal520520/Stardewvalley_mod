package stardewvalley.modid.fishing;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import stardewvalley.modid.item.ModFishingRodItem;
import stardewvalley.modid.item.RodComponent;
import stardewvalley.modid.season.FishingLevelManager;
import stardewvalley.modid.season.FishingLootManager;

public class FishingHookLogic {

    public static void startMinigame(ServerPlayerEntity player, ItemStack item) {
        FishBehavior behavior = FishBehaviorReloadListener.getBehavior(item);
        if (behavior != null) {
            net.minecraft.util.Identifier fishId = net.minecraft.registry.Registries.ITEM.getId(item.getItem());
            ServerPlayNetworking.send(player, new S2CStartMinigamePayload(behavior, fishId));
        }
    }

    public static boolean endMinigame(ServerPlayerEntity player, boolean success, double accuracy, int outOfBarTicks, ItemStack items) {
        if (success) {
            // 检查鱼竿上的鱼饵
            ItemStack rod = player.getMainHandStack();
            if (!FishingLootManager.isSvRod(rod)) rod = player.getOffHandStack();
            boolean isChallengeBait = false;
            if (FishingLootManager.isSvRod(rod)) {
                RodComponent comp = rod.get(RodComponent.TYPE);
                if (comp != null) {
                    ItemStack bait = comp.getBait();
                    if (!bait.isEmpty()) {
                        String path = Registries.ITEM.getId(bait.getItem()).getPath();
                        isChallengeBait = "bait_challenge_bait".equals(path);
                    }
                }
            }

            boolean isPerfect = accuracy >= 1.0;

            // 挑战鱼饵：根据出栏时间决定鱼数量（鱼王不适用此效果）
            if (isChallengeBait) {
                String fishName = Registries.ITEM.getId(items.getItem()).getPath();
                if (!FishingLootManager.isLegendaryFish(fishName)) {
                    items = items.copy();
                    if (outOfBarTicks <= 10) {
                        items.setCount(3);
                    } else if (outOfBarTicks <= 20) {
                        items.setCount(2);
                    } else {
                        items.setCount(1);
                    }
                }
            }

            // 小游戏成功后根据存储的水深和钓鱼等级判定品质
            int wDepth = FishingDataStorage.getWaterDepth(player);
            int fishLevel = FishingDataStorage.getFishingLevel(player);
            if (wDepth > 0) {
                ServerWorld sw = (ServerWorld) player.getEntityWorld();
                items = FishingLootManager.applyFishQuality(sw, player, wDepth, items);
            }

            // 完美钓鱼时品质提升一级：普通→银→金→铱
            if (isPerfect) {
                String itemName = Registries.ITEM.getId(items.getItem()).getPath();
                String upgradedName = upgradeQuality(itemName);
                if (upgradedName != null) {
                    net.minecraft.util.Identifier upgradedId = net.minecraft.util.Identifier.of(stardewvalley.modid.StardewValley.MOD_ID, upgradedName);
                    net.minecraft.item.Item upgradedItem = Registries.ITEM.get(upgradedId);
                    if (upgradedItem != null && upgradedItem != net.minecraft.item.Items.AIR) {
                        items = items.copy();
                        items = new ItemStack(upgradedItem, items.getCount());
                    }
                }
            }

            player.getInventory().offerOrDrop(items);
            FishingDataStorage.storeQualityBoost(player, (int) Math.round(accuracy));

            ServerWorld world = player.getEntityWorld() instanceof ServerWorld sw ? sw : null;
            if (world != null) {
                player.incrementStat(Stats.FISH_CAUGHT);

                String itemName = Registries.ITEM.getId(items.getItem()).getPath();
                if (itemName.startsWith("fish_") && !FishingLootManager.isRealFishItem(itemName)) {
                } else {
                    int xpAmount = 5;
                    if (isPerfect) {
                        xpAmount = (int) Math.round(xpAmount * 2.4);
                    }

                    // 使用新的XP计算公式
                    if (FishingLootManager.isRealFishItem(itemName)) {
                        int quality = 0;
                        String baseName = itemName;
                        if (itemName.endsWith("_iridium")) { quality = 4; baseName = itemName.substring(0, itemName.length() - 8); }
                        else if (itemName.endsWith("_gold")) { quality = 2; baseName = itemName.substring(0, itemName.length() - 5); }
                        else if (itemName.endsWith("_silver")) { quality = 1; baseName = itemName.substring(0, itemName.length() - 7); }
                        int difficulty = FishingLootManager.getFishDifficulty(baseName);
                        xpAmount = FishingLootManager.calcFishingXp(quality, difficulty);
                        if (isPerfect) {
                            xpAmount = (int) Math.round(xpAmount * 2.4);
                        }
                    }

                    FishingLevelManager flm = FishingLevelManager.get(world);
                    flm.addXp(player.getUuid(), xpAmount);
                }
            }
        }

        if (player.fishHook != null) {
            player.fishHook.discard();
        }
        return success;
    }

    /** 完美钓鱼时品质提升一级：普通→银→金→铱；非鱼/已是最高品质时返回 null */
    private static String upgradeQuality(String itemName) {
        if (!itemName.startsWith("fish_")) return null;
        // 非鱼（垃圾/藻类/凝胶）不升级
        if (itemName.contains("algae") || itemName.contains("jelly") || itemName.equals("fish_seaweed")) return null;
        if (itemName.endsWith("_iridium")) return null; // 已是最高品质
        if (itemName.endsWith("_gold")) return itemName.substring(0, itemName.length() - 5) + "_iridium";
        if (itemName.endsWith("_silver")) return itemName.substring(0, itemName.length() - 7) + "_gold";
        // 普通品质 → 银
        return itemName + "_silver";
    }
}
