package stardewvalley.modid.fishing;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.sound.SoundCategory;

public class FishingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(S2CStartMinigamePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new FishingScreen(payload.behavior(), payload.fishItemId()));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(S2CFishBitePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var player = context.client().player;
                if (player != null) {
                    player.getEntityWorld().playSound(player, player.getBlockPos(),
                        FishingSounds.FISH_BITE, SoundCategory.MASTER, 2.0F, 1.0F);
                }
            });
        });
    }
}
