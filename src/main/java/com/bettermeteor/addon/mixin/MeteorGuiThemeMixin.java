package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.gui.MeteorGuiThemeRoundnessAccess;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(MeteorGuiTheme.class)
public abstract class MeteorGuiThemeMixin implements MeteorGuiThemeRoundnessAccess {
    @Shadow @Final private SettingGroup sgGeneral;

    @Unique private Setting<Double> bettermeteor$roundness;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bettermeteor$addRoundnessSetting(CallbackInfo ci) {
        bettermeteor$roundness = sgGeneral.add(new DoubleSetting.Builder()
            .name("roundness")
            .description("Corner roundness for Meteor tabs and windows.")
            .defaultValue(4)
            .min(0)
            .sliderRange(0, 16)
            .onSliderRelease()
            .onChanged(value -> {
                if (mc.currentScreen instanceof WidgetScreen widgetScreen) widgetScreen.invalidate();
            })
            .build());
    }

    @Override
    public double bettermeteor$getRoundness() {
        return bettermeteor$roundness != null ? bettermeteor$roundness.get() : 4;
    }
}
