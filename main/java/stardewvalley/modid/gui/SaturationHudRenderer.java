package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.world.GameMode;
import stardewvalley.modid.mixin.HungerManagerAccessor;

import java.text.DecimalFormat;

/** 生存/冒险模式下在快捷栏右侧显示饱和值与疲劳值占比 */
public class SaturationHudRenderer implements HudRenderCallback {

    private static final float MAX_EXHAUSTION = 4.0f;
    private static final DecimalFormat SATURATION_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat EXHAUSTION_FORMAT = new DecimalFormat("0%");

    public static void register() {
        HudRenderCallback.EVENT.register(new SaturationHudRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        GameMode gameMode = client.player.getGameMode();
        if (gameMode != GameMode.SURVIVAL && gameMode != GameMode.ADVENTURE) return;

        float saturation = client.player.getHungerManager().getSaturationLevel();
        float exhaustion = ((HungerManagerAccessor) client.player.getHungerManager()).getExhaustionLevel();

        int x = context.getScaledWindowWidth() / 2;
        int y = context.getScaledWindowHeight();
        context.drawTextWithShadow(client.textRenderer, SATURATION_FORMAT.format(saturation), x + 92, y - 38, 0xFFFFFF00);
        context.drawTextWithShadow(client.textRenderer, EXHAUSTION_FORMAT.format(exhaustion / MAX_EXHAUSTION), x + 92, y - 28, 0xFF808080);
    }
}
