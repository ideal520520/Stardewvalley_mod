package stardewvalley.modid.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import stardewvalley.modid.gui.ModPayloads;
import stardewvalley.modid.season.*;

import java.util.UUID;

public class BookOfStarsItem extends StardewBookItem {

    private static final int[] XP_THRESHOLDS = {100, 380, 770, 1300, 2150, 3300, 4800, 6900, 10000, 15000};

    public BookOfStarsItem(Settings settings) {
        super(settings, "book_of_stars", true);
    }

    @Override
    protected void applyEffect(ServerWorld world, PlayerEntity player) {
        UUID uuid = player.getUuid();

        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        FarmingLevelManager farming = FarmingLevelManager.get(world);
        farming.addXp(uuid, 250);
        int fLevel = farming.getLevel(uuid);
        int fXp = farming.getXp(uuid);
        int fNext = fLevel < 10 ? XP_THRESHOLDS[fLevel] : 15000;
        ServerPlayNetworking.send(serverPlayer, new ModPayloads.FarmingSyncS2CPayload(fLevel, fXp, fNext));

        ForagingLevelManager foraging = ForagingLevelManager.get(world);
        foraging.addXp(uuid, 250);
        int foLevel = foraging.getLevel(uuid);
        int foXp = foraging.getXp(uuid);
        int foNext = foLevel < 10 ? XP_THRESHOLDS[foLevel] : 15000;
        ServerPlayNetworking.send(serverPlayer, new ModPayloads.ForagingSyncS2CPayload(foLevel, foXp, foNext));

        FishingLevelManager fishing = FishingLevelManager.get(world);
        fishing.addXp(uuid, 250);
        int fiLevel = fishing.getLevel(uuid);
        int fiXp = fishing.getXp(uuid);
        int fiNext = fiLevel < 10 ? XP_THRESHOLDS[fiLevel] : 15000;
        ServerPlayNetworking.send(serverPlayer, new ModPayloads.FishingSyncS2CPayload(fiLevel, fiXp, fiNext));

        MiningLevelManager mining = MiningLevelManager.get(world);
        mining.addXp(uuid, 250);
        int mLevel = mining.getLevel(uuid);
        int mXp = mining.getXp(uuid);
        int mNext = mLevel < 10 ? XP_THRESHOLDS[mLevel] : 15000;
        ServerPlayNetworking.send(serverPlayer, new ModPayloads.MiningSyncS2CPayload(mLevel, mXp, mNext));

        CombatLevelManager combat = CombatLevelManager.get(world);
        combat.addXp(serverPlayer, 250);
        int cLevel = combat.getLevel(uuid);
        int cXp = combat.getXp(uuid);
        int cNext = cLevel < 10 ? XP_THRESHOLDS[cLevel] : 15000;
        ServerPlayNetworking.send(serverPlayer, new ModPayloads.CombatSyncS2CPayload(cLevel, cXp, cNext));
    }
}
