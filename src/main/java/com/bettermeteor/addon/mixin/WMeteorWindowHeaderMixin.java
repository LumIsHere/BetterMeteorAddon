package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.gui.MeteorGuiThemeRoundnessAccess;
import com.bettermeteor.addon.gui.RoundedGuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorWindow$WMeteorHeader")
public abstract class WMeteorWindowHeaderMixin extends WWidget {
    @Inject(method = "onRender", at = @At("HEAD"), cancellable = true)
    private void bettermeteor$renderRoundedHeader(GuiRenderer renderer, double mouseX, double mouseY, double delta, CallbackInfo ci) {
        MeteorGuiTheme meteorTheme = (MeteorGuiTheme) this.theme;
        double roundness = ((MeteorGuiThemeRoundnessAccess) meteorTheme).bettermeteor$getRoundness();
        RoundedGuiRenderer.roundedQuad(renderer, x, y, width, height, meteorTheme.scale(roundness), meteorTheme.accentColor.get());
        ci.cancel();
    }
}
