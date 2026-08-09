package stardewvalley.modid.fishing;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import stardewvalley.modid.season.FishingLevelManager;

import java.util.List;

public class FishingNetworking {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(S2CStartMinigamePayload.ID, S2CStartMinigamePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(S2CFishBitePayload.ID, S2CFishBitePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(C2SCompleteMinigamePayload.ID, C2SCompleteMinigamePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(C2SMinigameStatePayload.ID, C2SMinigameStatePayload.CODEC);

        FishingSounds.register();
        FishingTimeManager.register();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FishBehaviorReloadListener());

        // 注册网络处理器
        registerHandlers();
    }

    @SuppressWarnings("deprecation")
    private static void registerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(C2SCompleteMinigamePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var items = FishingDataStorage.getItemsForPlayer(player);
                if (items == null) return;
                boolean fishCaught = FishingHookLogic.endMinigame(player, payload.success(), payload.accuracy(), payload.outOfBarTicks(), items);
                // 宝箱奖励：鱼成功钓上来且宝箱也获得时，生成随机战利品
                if (fishCaught && payload.chestObtained()) {
                    ServerWorld world = (ServerWorld) player.getEntityWorld();
                    // 增加50钓鱼经验
                    FishingLevelManager fishingManager = FishingLevelManager.get(world);
                    fishingManager.addXp(player.getUuid(), 50);
                    // 生成宝箱战利品（最多6种不同物品）
                    int fishingLevel = FishingDataStorage.getFishingLevel(player);
                    float luckMult = stardewvalley.modid.StardewValley.getFinalLuckMultiplier(world, player);
                    // 钓鱼精通：33%×宝箱最终出现概率 将普通宝箱升级为金色宝箱
                    boolean golden = stardewvalley.modid.skill.MasteryManager.hasMasteredSkill(world, player.getUuid(),
                        stardewvalley.modid.skill.SkillRegistry.Category.FISHING)
                        && world.random.nextFloat() < 0.33f * payload.chestChance();
                    List<ItemStack> treasures = golden
                        ? FishingTreasureGenerator.generateGoldenTreasure(world, world.random, fishingLevel, items, player.getUuid(), luckMult)
                        : FishingTreasureGenerator.generateTreasure(world, world.random, fishingLevel, items, player.getUuid(), luckMult);
                    for (ItemStack treasure : treasures) {
                        ItemEntity itemEntity = new ItemEntity(
                            world, player.getX(), player.getY() + 1, player.getZ(), treasure
                        );
                        world.spawnEntity(itemEntity);
                    }
                }
                FishingDataStorage.clearDataForPlayer(player);
                FishingTimeManager.unfreeze();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(C2SMinigameStatePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (payload.open()) {
                    FishingTimeManager.freeze((net.minecraft.server.world.ServerWorld) context.player().getEntityWorld());
                } else {
                    FishingTimeManager.unfreeze();
                }
            });
        });
    }
}
