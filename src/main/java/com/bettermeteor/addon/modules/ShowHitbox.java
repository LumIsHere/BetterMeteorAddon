package com.bettermeteor.addon.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.entity.ItemEntity;

public class ShowHitbox extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> players = sgGeneral.add(new BoolSetting.Builder()
        .name("players")
        .description("Show player hitboxes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> crystals = sgGeneral.add(new BoolSetting.Builder()
        .name("crystals")
        .description("Show end crystal hitboxes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> items = sgGeneral.add(new BoolSetting.Builder()
        .name("items")
        .description("Show dropped item hitboxes.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> hostileMobs = sgGeneral.add(new BoolSetting.Builder()
        .name("hostile-mobs")
        .description("Show hostile mob hitboxes.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> passiveMobs = sgGeneral.add(new BoolSetting.Builder()
        .name("passive-mobs")
        .description("Show passive mob hitboxes.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> waterMobs = sgGeneral.add(new BoolSetting.Builder()
        .name("water-mobs")
        .description("Show water creature hitboxes.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ambientMobs = sgGeneral.add(new BoolSetting.Builder()
        .name("ambient-mobs")
        .description("Show ambient mob hitboxes.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> projectiles = sgGeneral.add(new BoolSetting.Builder()
        .name("projectiles")
        .description("Show projectile hitboxes.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> vehicles = sgGeneral.add(new BoolSetting.Builder()
        .name("vehicles")
        .description("Show vehicle hitboxes.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> misc = sgGeneral.add(new BoolSetting.Builder()
        .name("misc")
        .description("Show hitboxes for entities that do not match the categories above.")
        .defaultValue(false)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the hitboxes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Fill color for the hitboxes.")
        .defaultValue(new SettingColor(255, 0, 0, 25))
        .visible(() -> shapeMode.get() != ShapeMode.Lines)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Outline color for the hitboxes.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(() -> shapeMode.get() != ShapeMode.Sides)
        .build()
    );

    public ShowHitbox() {
        super(Categories.Render, "show-hitbox", "Renders hitboxes for the entity types you choose.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == null || entity == mc.player || !entity.isAlive()) continue;
            if (!shouldRender(entity)) continue;

            event.renderer.box(entity.getBoundingBox(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }

    private boolean shouldRender(Entity entity) {
        if (entity instanceof PlayerEntity) return players.get();
        if (entity instanceof EndCrystalEntity) return crystals.get();
        if (entity instanceof ItemEntity) return items.get();
        if (entity instanceof ProjectileEntity) return projectiles.get();
        if (entity instanceof VehicleEntity) return vehicles.get();
        if (entity instanceof Monster) return hostileMobs.get();
        if (entity instanceof LivingEntity living && living.getType().getSpawnGroup() == SpawnGroup.AMBIENT) return ambientMobs.get();
        if (entity instanceof PassiveEntity) return passiveMobs.get();
        if (entity instanceof LivingEntity living && living.getType().getSpawnGroup() == SpawnGroup.WATER_AMBIENT) return waterMobs.get();
        if (entity instanceof LivingEntity living && living.getType().getSpawnGroup() == SpawnGroup.WATER_CREATURE) return waterMobs.get();

        return misc.get();
    }
}
