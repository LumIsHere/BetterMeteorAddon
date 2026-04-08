package com.bettermeteor.addon.gui;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public final class RoundedGuiRenderer {
    private RoundedGuiRenderer() {
    }

    public static void roundedQuad(GuiRenderer renderer, double x, double y, double width, double height, double radius, Color color) {
        double r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));

        if (r <= 0) {
            renderer.quad(x, y, width, height, color);
            return;
        }

        int steps = Math.max(3, (int) Math.ceil(r));
        double stepHeight = r / steps;

        renderer.quad(x, y + r, width, height - r * 2, color);

        for (int i = 0; i < steps; i++) {
            double yOffset = i * stepHeight;
            double centerY = yOffset + stepHeight / 2;
            double inset = r - Math.sqrt(Math.max(0, r * r - Math.pow(r - centerY, 2)));
            double stripWidth = width - inset * 2;

            renderer.quad(x + inset, y + yOffset, stripWidth, stepHeight, color);
            renderer.quad(x + inset, y + height - yOffset - stepHeight, stripWidth, stepHeight, color);
        }
    }
}
