package stardewvalley.modid.fishing;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class RenderUtil {

    public static void blitF(DrawContext context, Identifier texture, int x, int y, float uOffset, float vOffset, int uWidth, int vHeight) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, uOffset, vOffset, uWidth, vHeight, 256, 256);
    }

    public static void fillF(DrawContext context, int minX, int minY, int maxX, int maxY, int color) {
        context.fill(minX, minY, maxX, maxY, color);
    }
}
