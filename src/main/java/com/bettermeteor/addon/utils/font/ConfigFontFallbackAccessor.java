package com.bettermeteor.addon.utils.font;

import meteordevelopment.meteorclient.renderer.text.FontFace;

import java.util.List;

public interface ConfigFontFallbackAccessor {
    List<String> bettermeteor$getFallbackFontEntries();

    List<FontFace> bettermeteor$getFallbackFontFaces();
}
