package com.bettermeteor.addon.gui;

import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

public interface WidgetScreenModuleAnimationAccess {
    boolean bettermeteor$isModuleAnimationActive();

    double bettermeteor$getModuleAnimationOffsetX();

    boolean bettermeteor$isAnimatedModuleWindow(WWindow window);
}
