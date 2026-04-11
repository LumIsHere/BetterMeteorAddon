package com.bettermeteor.addon.utils.font;

import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;

import java.util.ArrayList;
import java.util.List;

public final class FontFaceParser {
    private FontFaceParser() {
    }

    public static FontFace parse(String value) {
        if (value == null) return null;

        String[] split = value.replace(" ", "").split("-");
        if (split.length != 2) return null;

        for (FontFamily family : Fonts.FONT_FAMILIES) {
            if (!family.getName().replace(" ", "").equals(split[0])) continue;

            try {
                return family.get(FontInfo.Type.valueOf(split[1]));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }

    public static List<FontFace> parseMany(List<String> values) {
        List<FontFace> faces = new ArrayList<>();
        if (values == null) return faces;

        for (String value : values) {
            FontFace face = parse(value);
            if (face != null) faces.add(face);
        }

        return faces;
    }
}
