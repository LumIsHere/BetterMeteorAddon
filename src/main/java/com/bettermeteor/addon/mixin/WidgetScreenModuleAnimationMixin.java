package com.bettermeteor.addon.mixin;

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
public abstract class WidgetScreenModuleAnimationMixin {
    @Unique private double bettermeteor$moduleTransitionProgress;
    @Unique private boolean bettermeteor$moduleClosingAnimation;
    @Unique private boolean bettermeteor$moduleAllowClose;
    @Unique private double bettermeteor$moduleAnchorX = Double.NaN;

    @Inject(method = "renderCustom", at = @At("HEAD"))
    private void bettermeteor$animateModuleScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!((Object) this instanceof ModuleScreen)) return;

        WWindow window = ((WindowScreenAccessor) this).bettermeteor$getWindow();
        if (Double.isNaN(bettermeteor$moduleAnchorX)) {
            if (window.width <= 0) return;

            bettermeteor$moduleAnchorX = window.x;
        }

        boolean animating = bettermeteor$moduleClosingAnimation || bettermeteor$moduleTransitionProgress < 1;
        if (!animating) return;

        double speed = Math.max(delta / 15.0, 0.03);
        bettermeteor$moduleTransitionProgress = Math.min(1, bettermeteor$moduleTransitionProgress + speed);

        double eased = bettermeteor$moduleClosingAnimation
            ? easeInBack(bettermeteor$moduleTransitionProgress)
            : easeOutBack(bettermeteor$moduleTransitionProgress);

        double offsetX = Math.round(window.width * 1.35 * (bettermeteor$moduleClosingAnimation ? eased : eased - 1));
        double desiredX = bettermeteor$moduleAnchorX + offsetX;
        if (window.x != desiredX) window.move(desiredX - window.x, 0);

        if (bettermeteor$moduleClosingAnimation && bettermeteor$moduleTransitionProgress >= 1) {
            bettermeteor$moduleAllowClose = true;
            ((WidgetScreen) (Object) this).close();
            bettermeteor$moduleAllowClose = false;
        }
    }

    @Inject(method = "renderCustom", at = @At("TAIL"))
    private void bettermeteor$cleanupModuleScreenMatrix(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!((Object) this instanceof ModuleScreen)) return;

        boolean animating = bettermeteor$moduleClosingAnimation || bettermeteor$moduleTransitionProgress < 1;
        if (!animating) return;

        WWindow window = ((WindowScreenAccessor) this).bettermeteor$getWindow();
        if (!Double.isNaN(bettermeteor$moduleAnchorX) && window.x != bettermeteor$moduleAnchorX) {
            window.move(bettermeteor$moduleAnchorX - window.x, 0);
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
        bettermeteor$moduleAnchorX = Double.NaN;
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
