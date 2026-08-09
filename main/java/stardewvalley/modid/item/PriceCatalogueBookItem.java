package stardewvalley.modid.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import stardewvalley.modid.gui.ModPayloads;

public class PriceCatalogueBookItem extends StardewBookItem {
    public PriceCatalogueBookItem(Settings settings) {
        super(settings, "price_catalogue", false);
    }

    @Override
    protected void applyEffect(ServerWorld world, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new ModPayloads.PriceCatalogueSyncS2CPayload(true));
        }
    }
}
