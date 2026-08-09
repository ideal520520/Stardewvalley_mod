package stardewvalley.modid.fishing;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FishingDataStorage {
    private static final Map<ServerPlayerEntity, FishingBobberEntity> playerHookMap = new HashMap<>();
    private static final Map<ServerPlayerEntity, ItemStack> playerItemMap = new HashMap<>();
    private static final Map<ServerPlayerEntity, Integer> playerQualityBoostMap = new HashMap<>();
    private static final Map<ServerPlayerEntity, Integer> playerWaterDepthMap = new HashMap<>();
    private static final Map<ServerPlayerEntity, Integer> playerFishingLevelMap = new HashMap<>();

    public static void storeData(ServerPlayerEntity player, FishingBobberEntity hook, ItemStack items) {
        playerHookMap.put(player, hook);
        playerItemMap.put(player, items);
    }

    public static void storeFishingData(ServerPlayerEntity player, int waterDepth, int fishingLevel) {
        playerWaterDepthMap.put(player, waterDepth);
        playerFishingLevelMap.put(player, fishingLevel);
    }

    public static void storeQualityBoost(ServerPlayerEntity player, int boost) {
        playerQualityBoostMap.put(player, boost);
    }

    public static FishingBobberEntity getHookForPlayer(ServerPlayerEntity player) {
        return playerHookMap.get(player);
    }

    public static ItemStack getItemsForPlayer(ServerPlayerEntity player) {
        return playerItemMap.get(player);
    }

    public static int getQualityBoostForPlayer(ServerPlayerEntity player) {
        return playerQualityBoostMap.getOrDefault(player, 0);
    }

    public static int getWaterDepth(ServerPlayerEntity player) {
        return playerWaterDepthMap.getOrDefault(player, 0);
    }

    public static int getFishingLevel(ServerPlayerEntity player) {
        return playerFishingLevelMap.getOrDefault(player, 0);
    }

    public static void clearDataForPlayer(ServerPlayerEntity player) {
        playerHookMap.remove(player);
        playerItemMap.remove(player);
        playerQualityBoostMap.remove(player);
        playerWaterDepthMap.remove(player);
        playerFishingLevelMap.remove(player);
    }
}
