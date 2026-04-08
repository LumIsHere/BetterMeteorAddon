package com.bettermeteor.addon.mixin;

import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WWindow.class)
public interface WWindowAccessor {
    @Accessor("expanded")
    boolean bettermeteor$isExpanded();

    @Accessor("animProgress")
    double bettermeteor$getAnimProgress();
}
