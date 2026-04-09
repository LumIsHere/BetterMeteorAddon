package com.bettermeteor.addon.modules;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;

public class HandChams extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> handTexture = sgGeneral.add(new BoolSetting.Builder()
        .name("texture")
        .description("Whether to render hand textures.")
        .defaultValue(false)
        .build()
    );

    public final Setting<SettingColor> handColor = sgGeneral.add(new ColorSetting.Builder()
        .name("hand-color")
        .description("The color of your hand.")
        .defaultValue(new SettingColor(198, 135, 254, 150))
        .build()
    );

    public static final Identifier BLANK = MeteorClient.identifier("textures/blank.png");

    public HandChams() {
        super(Categories.Render, "hand-chams", "Tweaks first-person hand rendering.");
    }
}
