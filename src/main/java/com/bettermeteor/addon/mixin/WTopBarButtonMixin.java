package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.gui.MeteorGuiThemeRoundnessAccess;
import com.bettermeteor.addon.gui.RoundedGuiRenderer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "meteordevelopment.meteorclient.gui.widgets.WTopBar$WTopBarButton")
public abstract class WTopBarButtonMixin extends WPressable {
    @Shadow @Final private Tab tab;
    @Shadow @Final WTopBar this$0;

    @Inject(method = "onRender", at = @At("HEAD"), cancellable = true)
    private void bettermeteor$renderRoundedTab(GuiRenderer renderer, double mouseX, double mouseY, double delta, CallbackInfo ci) {
        double pad = pad();
        boolean selected = pressed || (MeteorClient.mc.currentScreen instanceof TabScreen tabScreen && tabScreen.tab == tab);
        Color color = ((WTopBarAccessor) this$0).bettermeteor$getButtonColor(selected, mouseOver);
        MeteorGuiTheme meteorTheme = (MeteorGuiTheme) this.theme;
        double roundness = ((MeteorGuiThemeRoundnessAccess) meteorTheme).bettermeteor$getRoundness();

        RoundedGuiRenderer.roundedQuad(renderer, x, y, width, height, meteorTheme.scale(roundness), color);
        renderer.text(tab.name, x + pad, y + pad, ((WTopBarAccessor) this$0).bettermeteor$getNameColor(), false);
        ci.cancel();
    }
}
