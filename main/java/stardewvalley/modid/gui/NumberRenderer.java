package stardewvalley.modid.gui;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

public class NumberRenderer {

    private static final Identifier[] DIGIT_TEXTURES = new Identifier[10];
    private static final Identifier[] DIGIT_TEXTURES_WHITE = new Identifier[10];
    private static final int DIGIT_W = 16;
    private static final int DIGIT_H = 16;

    static {
        for (int i = 0; i <= 9; i++) {
            DIGIT_TEXTURES[i] = Identifier.of(StardewValley.MOD_ID, "textures/gui/number/" + i + ".png");
            DIGIT_TEXTURES_WHITE[i] = Identifier.of(StardewValley.MOD_ID, "textures/gui/number_white/" + i + ".png");
        }
    }

    public static void drawNumber(DrawContext context, int number, int x, int y, float scale) {
        drawNumber(context, number, x, y, scale, false);
    }

    public static void drawNumber(DrawContext context, int number, int x, int y, float scale, boolean white) {
        String s = String.valueOf(number);
        int spacing = Math.round(DIGIT_W * scale);
        Identifier[] textures = white ? DIGIT_TEXTURES_WHITE : DIGIT_TEXTURES;
        for (int i = 0; i < s.length(); i++) {
            int digit = s.charAt(i) - '0';
            int dx = x + i * spacing;
            int w = Math.round(DIGIT_W * scale);
            int h = Math.round(DIGIT_H * scale);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, textures[digit],
                dx, y, 0.0f, 0.0f, w, h, DIGIT_W, DIGIT_H, DIGIT_W, DIGIT_H);
        }
    }

    public static int charWidth(float scale) {
        return Math.round(DIGIT_W * scale);
    }

    public static int numberWidth(int number, float scale) {
        return String.valueOf(number).length() * charWidth(scale);
    }
}