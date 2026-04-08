package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.gui.WidgetScreenModuleAnimationAccess;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.screens.ModuleScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WidgetScreen.class)
public abstract class WidgetScreenModuleAnimationMixin implements WidgetScreenModuleAnimationAccess {
    @Unique private double bettermeteor$moduleTransitionProgress;
    @Unique private boolean bettermeteor$moduleClosingAnimation;
    @Unique private boolean bettermeteor$moduleAllowClose;
    @Unique private WWindow bettermeteor$moduleWindow;

    @Inject(method = "renderCustom", at = @At("HEAD"))
    private void bettermeteor$animateModuleScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!((Object) this instanceof ModuleScreen)) return;

        bettermeteor$moduleWindow = ((WindowScreenAccessor) this).bettermeteor$getWindow();
        double speed = Math.max(delta / 15.0, 0.03);
        bettermeteor$moduleTransitionProgress = Math.min(1, bettermeteor$moduleTransitionProgress + speed);

        if (bettermeteor$moduleClosingAnimation && bettermeteor$moduleTransitionProgress >= 1) {
            bettermeteor$moduleAllowClose = true;
            ((WidgetScreen) (Object) this).close();
            bettermeteor$moduleAllowClose = false;
        }
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    private void bettermeteor$animateModuleClose(CallbackInfo ci) {
        if (!((Object) this instanceof ModuleScreen)) return;
        if (bettermeteor$moduleAllowClose || bettermeteor$moduleClosingAnimation) return;

        bettermeteor$moduleClosingAnimation = true;
        bettermeteor$moduleTransitionProgress = 0;
        ci.cancel();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void bettermeteor$resetModuleAnimationState(CallbackInfo ci) {
        if (!((Object) this instanceof ModuleScreen)) return;

        bettermeteor$moduleTransitionProgress = 0;
        bettermeteor$moduleClosingAnimation = false;
        bettermeteor$moduleAllowClose = false;
        bettermeteor$moduleWindow = ((WindowScreenAccessor) this).bettermeteor$getWindow();
    }

    @Override
    public boolean bettermeteor$isModuleAnimationActive() {
        return !bettermeteor$moduleClosingAnimation ? bettermeteor$moduleTransitionProgress < 1 : bettermeteor$moduleTransitionProgress < 1;
    }

    @Override
    public double bettermeteor$getModuleAnimationOffsetX() {
        if (bettermeteor$moduleWindow == null || bettermeteor$moduleWindow.width <= 0) return 0;

        double eased = bettermeteor$moduleClosingAnimation
            ? easeInBack(bettermeteor$moduleTransitionProgress)
            : easeOutBack(bettermeteor$moduleTransitionProgress);

        return Math.round(bettermeteor$moduleWindow.width * 1.35 * (bettermeteor$moduleClosingAnimation ? eased : eased - 1));
    }

    @Override
    public boolean bettermeteor$isAnimatedModuleWindow(WWindow window) {
        return window == bettermeteor$moduleWindow;
    }

    @Unique
    private static double easeOutBack(double t) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        double x = t - 1;
        return 1 + c3 * x * x * x + c1 * x * x;
    }

    @Unique
    private static double easeInBack(double t) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return c3 * t * t * t - c1 * t * t;
    }
}
