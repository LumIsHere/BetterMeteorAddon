package com.bettermeteor.addon.mixin;

import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WindowScreen.class)
public interface WindowScreenAccessor {
    @Accessor("window")
    WWindow bettermeteor$getWindow();
}
