package com.example.colormons;

import net.minecraft.client.gui.GuiGraphics;

/**
 * The paint grid. Draws the species texture's own pixels, each tinted with its group
 * colour, which is the same look the group map uses. Click maths shares one layout.
 */
public class PaintPanel {
    public static final int CELL = 3;

    public static int[] layout(String species, int box) {
        ScreenData.Size s = ScreenData.size(species);
        if (s == null) return new int[]{1, 1};
        int cols = Math.max(1, Math.min(s.width(), box / CELL));
        int rows = Math.max(1, Math.min(s.height(), box / CELL));
        return new int[]{cols, rows};
    }

    public static void draw(GuiGraphics g, String species, MonColors mc, int x, int y, int box) {
        int[] pixels = ScreenData.pixels(species);
        ScreenData.Size s = ScreenData.size(species);
        int[] effective = ScreenData.effectiveGroups(species, mc);
        if (pixels == null || s == null || effective == null) return;

        int[] l = layout(species, box);
        int cols = l[0], rows = l[1];
        int tw = s.width(), th = s.height();

        for (int cy = 0; cy < rows; cy++) for (int cx = 0; cx < cols; cx++) {
            int px = cx * tw / cols;
            int py = cy * th / rows;
            int idx = py * tw + px;
            int argb = pixels[idx];
            if (((argb >>> 24) & 0xFF) == 0) continue;

            // start from the texture's own colour, then blend the group tint over it,
            // so shading and shape stay readable instead of a flat block of colour
            int r = (argb >> 16) & 0xFF, gg = (argb >> 8) & 0xFF, b = argb & 0xFF;
            int grp = effective[idx];
            int out;
            if (grp >= 0 && grp < Palette.HIGHLIGHT.length) {
                int tint = Palette.HIGHLIGHT[grp];
                float lum = (r * 0.299f + gg * 0.587f + b * 0.114f) / 255f;
                float mix = 0.55f + 0.45f * lum;          // how much of the tint to apply
                int tr = (tint >> 16) & 0xFF, tg = (tint >> 8) & 0xFF, tb = tint & 0xFF;
                out = (blend(r, tr, mix) << 16) | (blend(gg, tg, mix) << 8) | blend(b, tb, mix);
            } else {
                out = argb & 0xFFFFFF;
            }
            g.fill(x + cx * CELL, y + cy * CELL, x + cx * CELL + CELL, y + cy * CELL + CELL,
                    0xFF000000 | out);
        }
    }

    private static int blend(int from, int to, float t) {
        int v = (int) (from * (1f - t) + to * t);
        return Math.max(0, Math.min(255, v));
    }

    public static int[] pixelAt(String species, double mx, double my, int x, int y, int box) {
        ScreenData.Size s = ScreenData.size(species);
        if (s == null) return null;
        int[] l = layout(species, box);
        int cols = l[0], rows = l[1];
        int cx = (int) Math.floor((mx - x) / CELL);
        int cy = (int) Math.floor((my - y) / CELL);
        if (cx < 0 || cy < 0 || cx >= cols || cy >= rows) return null;
        return new int[]{cx * s.width() / cols, cy * s.height() / rows};
    }
}
