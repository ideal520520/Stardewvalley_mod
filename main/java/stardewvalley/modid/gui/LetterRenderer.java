package stardewvalley.modid.gui;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

public class LetterRenderer {

    private static final Identifier[] LETTER_TEXTURES = new Identifier[26];
    private static final Identifier[] LETTER_WHITE_TEXTURES = new Identifier[26];
    private static final int LETTER_W = 22;
    private static final int LETTER_H = 22;

    static {
        for (int i = 0; i < 26; i++) {
            char c = (char) ('A' + i);
            LETTER_TEXTURES[i] = Identifier.of(StardewValley.MOD_ID, "textures/gui/letter/" + c + ".png");
            LETTER_WHITE_TEXTURES[i] = Identifier.of(StardewValley.MOD_ID, "textures/gui/letter_white/" + c + ".png");
        }
    }

    public static void drawLetter(DrawContext context, char c, int x, int y, float scale) {
        int idx = Character.toUpperCase(c) - 'A';
        if (idx < 0 || idx >= 26) return;
        int w = Math.round(LETTER_W * scale);
        int h = Math.round(LETTER_H * scale);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, LETTER_TEXTURES[idx],
            x, y, 0.0f, 0.0f, w, h, LETTER_W, LETTER_H, LETTER_W, LETTER_H);
    }

    public static void drawLetterWhite(DrawContext context, char c, int x, int y, float scale) {
        int idx = Character.toUpperCase(c) - 'A';
        if (idx < 0 || idx >= 26) return;
        int w = Math.round(LETTER_W * scale);
        int h = Math.round(LETTER_H * scale);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, LETTER_WHITE_TEXTURES[idx],
            x, y, 0.0f, 0.0f, w, h, LETTER_W, LETTER_H, LETTER_W, LETTER_H);
    }

    public static void drawText(DrawContext context, String text, int x, int y, float scale) {
        int cursorX = x;
        int spacing = Math.round(LETTER_W * scale);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                cursorX += spacing;
                continue;
            }
            drawLetter(context, c, cursorX, y, scale);
            cursorX += spacing;
        }
    }

    public static void drawTextWhite(DrawContext context, String text, int x, int y, float scale) {
        int cursorX = x;
        int spacing = Math.round(LETTER_W * scale);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                cursorX += spacing;
                continue;
            }
            drawLetterWhite(context, c, cursorX, y, scale);
            cursorX += spacing;
        }
    }

    public static int charWidth(float scale) {
        return Math.round(LETTER_W * scale);
    }

    public static int textWidth(String text, float scale) {
        return text.length() * charWidth(scale);
    }
}