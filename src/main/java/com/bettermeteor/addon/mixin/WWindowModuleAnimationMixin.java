package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.gui.WidgetScreenModuleAnimationAccess;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(WWindow.class)
public abstract class WWindowModuleAnimationMixin {
    @Unique private boolean bettermeteor$moduleAnimationApplied;

    @Inject(method = "render", at = @At("HEAD"))
    private void bettermeteor$applyModuleAnimationOffset(GuiRenderer renderer, double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        if (!(mc.currentScreen instanceof WidgetScreenModuleAnimationAccess access)) return;
        if (!access.bettermeteor$isModuleAnimationActive()) return;
        if (!access.bettermeteor$isAnimatedModuleWindow((WWindow) (Object) this)) return;

        double offsetX = access.bettermeteor$getModuleAnimationOffsetX();
        if (offsetX == 0) return;

        ((WWindow) (Object) this).move(offsetX, 0);
        bettermeteor$moduleAnimationApplied = true;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void bettermeteor$resetModuleAnimationOffset(GuiRenderer renderer, double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        if (!bettermeteor$moduleAnimationApplied) return;

        bettermeteor$moduleAnimationApplied = false;
        if (!(mc.currentScreen instanceof WidgetScreenModuleAnimationAccess access)) return;

        double offsetX = access.bettermeteor$getModuleAnimationOffsetX();
        if (offsetX != 0) ((WWindow) (Object) this).move(-offsetX, 0);
    }
}
