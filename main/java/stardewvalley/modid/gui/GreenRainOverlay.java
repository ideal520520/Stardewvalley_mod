package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import stardewvalley.modid.weather.WeatherType;

public class GreenRainOverlay implements HudRenderCallback {

    private static final int GREEN_TINT = 0x2010FF20;

    public static void register() {
        HudRenderCallback.EVENT.register(new GreenRainOverlay());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        if (ClockHudRenderer.getClientWeather() == WeatherType.GREEN_RAIN) {
            int w = context.getScaledWindowWidth();
            int h = context.getScaledWindowHeight();
            context.fill(0, 0, w, h, GREEN_TINT);
        }
    }
}
