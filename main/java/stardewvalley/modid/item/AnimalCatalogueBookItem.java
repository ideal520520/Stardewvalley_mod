package stardewvalley.modid.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import stardewvalley.modid.gui.ModPayloads;

public class AnimalCatalogueBookItem extends StardewBookItem {
    public AnimalCatalogueBookItem(Settings settings) {
        super(settings, "animal_catalogue", false);
    }

    @Override
    protected void applyEffect(ServerWorld world, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new ModPayloads.AnimalCatalogueDiscountS2CPayload(true));
        }
    }
}
