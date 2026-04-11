package com.bettermeteor.addon.modules;

import com.bettermeteor.addon.BetterMeteorAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class MurderMystery extends Module {
    private static final String MURDER_MYSTERY_TITLE = "MURDER MYSTERY";

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> goldEsp = sgGeneral.add(new BoolSetting.Builder()
        .name("gold-esp")
        .description("Highlights dropped gold ingots while you are in Murder Mystery.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> playerEsp = sgGeneral.add(new BoolSetting.Builder()
        .name("player-esp")
        .description("Highlights other players while you are in Murder Mystery.")
        .defaultValue(false)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the ESP boxes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> goldSideColor = sgRender.add(new ColorSetting.Builder()
        .name("gold-side-color")
        .description("Side color for dropped gold ingot ESP.")
        .defaultValue(new SettingColor(255, 210, 60, 35))
        .visible(goldEsp::get)
        .build()
    );

    private final Setting<SettingColor> goldLineColor = sgRender.add(new ColorSetting.Builder()
        .name("gold-line-color")
        .description("Line color for dropped gold ingot ESP.")
        .defaultValue(new SettingColor(255, 210, 60, 255))
        .visible(goldEsp::get)
        .build()
    );

    private final Setting<SettingColor> playerSideColor = sgRender.add(new ColorSetting.Builder()
        .name("player-side-color")
        .description("Side color for player ESP.")
        .defaultValue(new SettingColor(255, 80, 80, 25))
        .visible(playerEsp::get)
        .build()
    );

    private final Setting<SettingColor> playerLineColor = sgRender.add(new ColorSetting.Builder()
        .name("player-line-color")
        .description("Line color for player ESP.")
        .defaultValue(new SettingColor(255, 80, 80, 255))
        .visible(playerEsp::get)
        .build()
    );

    private boolean announced;

    public MurderMystery() {
        super(BetterMeteorAddon.MINIGAMES, "murder-mystery", "Detects Murder Mystery from the scoreboard title and alerts you in chat.");
    }

    @Override
    public void onDeactivate() {
        announced = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) {
            announced = false;
            return;
        }

        boolean inMurderMystery = isInMurderMystery();
        if (inMurderMystery) {
            if (!announced) {
                info("You are in Murder Mystery.");
                announced = true;
            }
        } else {
            announced = false;
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null || !isInMurderMystery()) return;

        for (Entity entity : mc.world.getEntities()) {
            if (goldEsp.get() && entity instanceof ItemEntity itemEntity && itemEntity.getStack().isOf(Items.GOLD_INGOT)) {
                renderBox(event, itemEntity, goldSideColor.get(), goldLineColor.get());
                continue;
            }

            if (playerEsp.get() && entity instanceof PlayerEntity player && player != mc.player) {
                renderBox(event, player, playerSideColor.get(), playerLineColor.get());
            }
        }
    }

    private boolean isInMurderMystery() {
        ScoreboardObjective objective = mc.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;

        String title = Formatting.strip(objective.getDisplayName().getString());
        if (title == null) return false;

        return MURDER_MYSTERY_TITLE.equals(title.trim());
    }

    private void renderBox(Render3DEvent event, Entity entity, SettingColor sideColor, SettingColor lineColor) {
        double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
        double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
        double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();

        Box box = entity.getBoundingBox();
        event.renderer.box(
            x + box.minX,
            y + box.minY,
            z + box.minZ,
            x + box.maxX,
            y + box.maxY,
            z + box.maxZ,
            sideColor,
            lineColor,
            shapeMode.get(),
            0
        );
    }
}
