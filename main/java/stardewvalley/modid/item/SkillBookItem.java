package stardewvalley.modid.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import stardewvalley.modid.gui.ModPayloads;
import stardewvalley.modid.season.*;

import java.util.UUID;

public class SkillBookItem extends Item {

    public enum SkillType {
        FARMING, FORAGING, FISHING, MINING, COMBAT
    }

    private static final int[] XP_THRESHOLDS = {100, 380, 770, 1300, 2150, 3300, 4800, 6900, 10000, 15000};

    private final SkillType skillType;

    public SkillBookItem(Settings settings, SkillType skillType) {
        super(settings);
        this.skillType = skillType;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        UUID uuid = user.getUuid();

        switch (skillType) {
            case FARMING -> {
                FarmingLevelManager manager = FarmingLevelManager.get(serverWorld);
                manager.addXp(uuid, 250);
                syncFarming(serverWorld, user, manager);
            }
            case FORAGING -> {
                ForagingLevelManager manager = ForagingLevelManager.get(serverWorld);
                manager.addXp(uuid, 250);
                syncForaging(serverWorld, user, manager);
            }
            case FISHING -> {
                FishingLevelManager manager = FishingLevelManager.get(serverWorld);
                manager.addXp(uuid, 250);
                syncFishing(serverWorld, user, manager);
            }
            case MINING -> {
                MiningLevelManager manager = MiningLevelManager.get(serverWorld);
                manager.addXp(uuid, 250);
                syncMining(serverWorld, user, manager);
            }
            case COMBAT -> {
                if (user instanceof ServerPlayerEntity serverPlayer) {
                    CombatLevelManager manager = CombatLevelManager.get(serverWorld);
                    manager.addXp(serverPlayer, 250);
                    syncCombat(serverWorld, user, manager);
                }
            }
        }

        if (!user.isCreative()) {
            stack.decrement(1);
        }

        return ActionResult.SUCCESS;
    }

    private void syncFarming(ServerWorld world, PlayerEntity player, FarmingLevelManager manager) {
        UUID uuid = player.getUuid();
        int level = manager.getLevel(uuid);
        int xp = manager.getXp(uuid);
        int nextXp = level < 10 ? XP_THRESHOLDS[level] : 15000;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new ModPayloads.FarmingSyncS2CPayload(level, xp, nextXp));
        }
    }

    private void syncForaging(ServerWorld world, PlayerEntity player, ForagingLevelManager manager) {
        UUID uuid = player.getUuid();
        int level = manager.getLevel(uuid);
        int xp = manager.getXp(uuid);
        int nextXp = level < 10 ? XP_THRESHOLDS[level] : 15000;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new ModPayloads.ForagingSyncS2CPayload(level, xp, nextXp));
        }
    }

    private void syncFishing(ServerWorld world, PlayerEntity player, FishingLevelManager manager) {
        UUID uuid = player.getUuid();
        int level = manager.getLevel(uuid);
        int xp = manager.getXp(uuid);
        int nextXp = level < 10 ? XP_THRESHOLDS[level] : 15000;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new ModPayloads.FishingSyncS2CPayload(level, xp, nextXp));
        }
    }

    private void syncMining(ServerWorld world, PlayerEntity player, MiningLevelManager manager) {
        UUID uuid = player.getUuid();
        int level = manager.getLevel(uuid);
        int xp = manager.getXp(uuid);
        int nextXp = level < 10 ? XP_THRESHOLDS[level] : 15000;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new ModPayloads.MiningSyncS2CPayload(level, xp, nextXp));
        }
    }

    private void syncCombat(ServerWorld world, PlayerEntity player, CombatLevelManager manager) {
        UUID uuid = player.getUuid();
        int level = manager.getLevel(uuid);
        int xp = manager.getXp(uuid);
        int nextXp = level < 10 ? XP_THRESHOLDS[level] : 15000;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new ModPayloads.CombatSyncS2CPayload(level, xp, nextXp));
        }
    }
}
