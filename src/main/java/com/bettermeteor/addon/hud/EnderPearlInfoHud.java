package com.bettermeteor.addon.hud;

import com.bettermeteor.addon.BetterMeteorAddon;
import com.bettermeteor.addon.utils.EnderPearlInfoUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EnderPearlInfoHud extends HudElement {
    public static final HudElementInfo<EnderPearlInfoHud> INFO = new HudElementInfo<>(BetterMeteorAddon.HUD_GROUP, "ender-pearl-info", "Shows ender pearl landing time and danger info.", EnderPearlInfoHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgChecks = settings.createGroup("Checks");
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> maxSimulationTicks = sgGeneral.add(new IntSetting.Builder()
        .name("max-simulation-ticks")
        .description("Maximum number of ticks to simulate for a pearl landing.")
        .defaultValue(400)
        .range(20, 1200)
        .sliderRange(20, 1200)
        .build()
    );

    private final Setting<Unit> unit = sgGeneral.add(new EnumSetting.Builder<Unit>()
        .name("unit")
        .description("How to display the remaining pearl time.")
        .defaultValue(Unit.Seconds)
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Render shadow behind the text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> padding = sgGeneral.add(new IntSetting.Builder()
        .name("padding")
        .description("Space around the text.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );

    private final Setting<Double> nearbyRange = sgChecks.add(new DoubleSetting.Builder()
        .name("nearby-range")
        .description("Range around the pearl landing spot used for nearby danger checks.")
        .defaultValue(6.0)
        .min(1.0)
        .sliderRange(1.0, 16.0)
        .build()
    );

    private final Setting<Boolean> showPlayers = sgChecks.add(new BoolSetting.Builder()
        .name("show-other-players")
        .description("Show when other players are near the landing spot.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showCrystals = sgChecks.add(new BoolSetting.Builder()
        .name("show-crystals")
        .description("Show when end crystals are near the landing spot.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showAnchors = sgChecks.add(new BoolSetting.Builder()
        .name("show-anchors")
        .description("Show when respawn anchors are near the landing spot.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showTnt = sgChecks.add(new BoolSetting.Builder()
        .name("show-tnt")
        .description("Show when TNT blocks or primed TNT are near the landing spot.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showWater = sgChecks.add(new BoolSetting.Builder()
        .name("show-water")
        .description("Show when the pearl lands in water.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showLava = sgChecks.add(new BoolSetting.Builder()
        .name("show-lava")
        .description("Show when the pearl lands in lava.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Apply a custom text scale to this HUD element.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom text scale.")
        .defaultValue(1.0)
        .min(0.5)
        .sliderRange(0.5, 4.0)
        .visible(customScale::get)
        .build()
    );

    private final Setting<Boolean> customColor = sgRender.add(new BoolSetting.Builder()
        .name("custom-color")
        .description("Use a custom text color instead of Meteor HUD colors.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> textColor = sgRender.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Text color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(customColor::get)
        .build()
    );

    private final Setting<Boolean> background = sgRender.add(new BoolSetting.Builder()
        .name("background")
        .description("Render a background behind the text.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgRender.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Background color.")
        .defaultValue(new SettingColor(25, 25, 25, 120))
        .visible(background::get)
        .build()
    );

    public EnderPearlInfoHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        EnderPearlInfoUtils.PearlInfo info = EnderPearlInfoUtils.getInfo(MinecraftClient.getInstance(), maxSimulationTicks.get(), nearbyRange.get());
        boolean hasInfo = info != null;
        List<String> lines = hasInfo ? getLines(info) : getPreviewLines();

        double scale = getScale();
        double textHeight = renderer.textHeight(shadow.get(), scale);
        double width = 0;
        for (String line : lines) {
            width = Math.max(width, renderer.textWidth(line, shadow.get(), scale));
        }

        setSize(width + padding.get() * 2.0, lines.size() * textHeight + padding.get() * 2.0);

        if (!hasInfo && !isInEditor()) return;

        if (background.get()) {
            renderer.quad(x, y, getWidth(), getHeight(), backgroundColor.get());
        }

        Color color = customColor.get() ? textColor.get() : Hud.get().textColors.get().getFirst();
        double textY = y + padding.get();
        for (String line : lines) {
            double lineX = x + padding.get() + alignX(renderer.textWidth(line, shadow.get(), scale), alignment.get());
            renderer.text(line, lineX, textY, color, shadow.get(), scale);
            textY += textHeight;
        }
    }

    private List<String> getLines(EnderPearlInfoUtils.PearlInfo info) {
        List<String> lines = new ArrayList<>();
        lines.add("Time: " + formatTime(info.ticksLeft()));

        if (showPlayers.get() && !info.nearbyPlayers().isEmpty()) {
            lines.add("Players nearby: " + String.join(", ", info.nearbyPlayers()));
        }
        if (showCrystals.get() && info.crystalsNearby()) lines.add("Crystal nearby");
        if (showAnchors.get() && info.anchorStatus() == EnderPearlInfoUtils.AnchorStatus.Charged) lines.add("Charged Anchor nearby");
        if (showAnchors.get() && info.anchorStatus() == EnderPearlInfoUtils.AnchorStatus.Uncharged) lines.add("Anchor nearby");
        if (showTnt.get() && info.tntNearby()) lines.add("TNT nearby");
        if (showWater.get() && info.landingFluid() == EnderPearlInfoUtils.LandingFluid.Water) lines.add("Lands in water");
        if (showLava.get() && info.landingFluid() == EnderPearlInfoUtils.LandingFluid.Lava) lines.add("Lands in lava");

        if (lines.size() == 1) lines.add("Landing looks clear");
        return lines;
    }

    private List<String> getPreviewLines() {
        return List.of("Time: " + formatTime(30), "Players nearby: Steve, Alex", "Charged Anchor nearby");
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    private String formatTime(int ticksLeft) {
        return switch (unit.get()) {
            case Seconds -> String.format(Locale.US, "%.1fs", ticksLeft / 20.0);
            case Milliseconds -> ticksLeft * 50 + "ms";
            case Ticks -> ticksLeft + "t";
        };
    }

    public enum Unit {
        Seconds,
        Milliseconds,
        Ticks
    }
}
