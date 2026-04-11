package com.bettermeteor.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Crystal extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> clientCrystal = sgGeneral.add(new BoolSetting.Builder()
        .name("client-crystal")
        .description("Removes attacked end crystals client-side immediately instead of waiting for the server destroy packet.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> clickCrystal = sgGeneral.add(new BoolSetting.Builder()
        .name("click-crystal")
        .description("When holding an end crystal, left click places crystals on valid blocks so you can spam place and break with LMB.")
        .defaultValue(true)
        .build()
    );

    public Crystal() {
        super(Categories.Combat, "crystal", "Adds client-side crystal breaking and left-click crystal placement.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onAttackEntity(AttackEntityEvent event) {
        if (!clientCrystal.get() || mc.world == null) return;
        if (!(event.entity instanceof EndCrystalEntity crystal)) return;

        if (!crystal.isRemoved()) {
            mc.world.removeEntity(crystal.getId(), Entity.RemovalReason.KILLED);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMouseClick(MouseClickEvent event) {
        if (!clickCrystal.get() || mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (event.action != KeyAction.Press || event.button() != GLFW_MOUSE_BUTTON_LEFT) return;

        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult hitResult)) return;
        if (hitResult.getType() != HitResult.Type.BLOCK) return;

        Utils.rightClick();
        event.cancel();
    }
}
