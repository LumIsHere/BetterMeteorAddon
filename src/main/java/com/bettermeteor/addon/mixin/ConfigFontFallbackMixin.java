package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.utils.font.ConfigFontFallbackAccessor;
import com.bettermeteor.addon.utils.font.FontFaceListSetting;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Config.class)
public abstract class ConfigFontFallbackMixin implements ConfigFontFallbackAccessor {
    @Shadow @Final private SettingGroup sgVisual;
    @Shadow public Setting<Boolean> customFont;
    @Shadow public Setting<FontFace> font;

    @Unique private Setting<List<FontFace>> bettermeteor$fallbackFonts;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bettermeteor$addFallbackFontSetting(CallbackInfo ci) {
        bettermeteor$fallbackFonts = sgVisual.add(new FontFaceListSetting.Builder()
            .name("fallback-fonts")
            .description("Ordered fallback custom fonts. Top to bottom: if the first font lacks a character, the next font is used.")
            .visible(customFont::get)
            .onChanged(value -> {
                if (Fonts.RENDERER != null) {
                    Fonts.RENDERER.destroy();
                    Fonts.RENDERER = null;
                }

                Fonts.load(font.get());
            })
            .build());
    }

    @Override
    public List<FontFace> bettermeteor$getFallbackFontFaces() {
        return bettermeteor$fallbackFonts != null ? bettermeteor$fallbackFonts.get() : List.of();
    }
}
