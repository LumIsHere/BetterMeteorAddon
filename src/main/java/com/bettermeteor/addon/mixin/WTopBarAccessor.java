package com.bettermeteor.addon.mixin;

import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WTopBar.class)
public interface WTopBarAccessor {
    @Invoker("getButtonColor")
    Color bettermeteor$getButtonColor(boolean pressed, boolean hovered);

    @Invoker("getNameColor")
    Color bettermeteor$getNameColor();
}
