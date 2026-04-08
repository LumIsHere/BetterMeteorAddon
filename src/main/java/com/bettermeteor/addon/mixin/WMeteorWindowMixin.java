package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.gui.MeteorGuiThemeRoundnessAccess;
import com.bettermeteor.addon.gui.RoundedGuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorWindow;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WMeteorWindow.class)
public abstract class WMeteorWindowMixin extends WWindow {
    private WMeteorWindowMixin(WWidget icon, String title) {
        super(icon, title);
    }

    @Inject(method = "onRender", at = @At("HEAD"), cancellable = true)
    private void bettermeteor$renderRoundedBody(GuiRenderer renderer, double mouseX, double mouseY, double delta, CallbackInfo ci) {
        WWindowAccessor accessor = (WWindowAccessor) this;
        if (accessor.bettermeteor$isExpanded() || accessor.bettermeteor$getAnimProgress() > 0) {
            MeteorGuiTheme meteorTheme = (MeteorGuiTheme) this.theme;
            double roundness = ((MeteorGuiThemeRoundnessAccess) meteorTheme).bettermeteor$getRoundness();
            RoundedGuiRenderer.roundedQuad(renderer, x, y, width, height, meteorTheme.scale(roundness), meteorTheme.backgroundColor.get());
        }

        ci.cancel();
    }
}
