package com.bettermeteor.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
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

    private final Setting<Boolean> clickSwap = sgGeneral.add(new BoolSetting.Builder()
        .name("click-swap")
        .description("When left clicking obsidian, swaps to a hotbar end crystal by simulating its hotbar key.")
        .defaultValue(true)
        .build()
    );

    private int pendingHotbarKeyRelease = -1;

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
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (event.action != KeyAction.Press || event.button() != GLFW_MOUSE_BUTTON_LEFT) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult hitResult)) return;
        if (hitResult.getType() != HitResult.Type.BLOCK) return;

        if (clickCrystal.get() && mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
            Utils.rightClick();
            event.cancel();
            return;
        }

        if (!clickSwap.get()) return;
        if (!mc.world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.OBSIDIAN)) return;

        FindItemResult crystal = InvUtils.findInHotbar(Items.END_CRYSTAL);
        if (!crystal.found() || !crystal.isHotbar()) return;
        if (crystal.slot() == mc.player.getInventory().getSelectedSlot()) return;

        pressHotbarKey(crystal.slot());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (pendingHotbarKeyRelease < 0) return;

        KeyBinding keyBinding = mc.options.hotbarKeys[pendingHotbarKeyRelease];
        keyBinding.setPressed(false);
        pendingHotbarKeyRelease = -1;
    }

    private void pressHotbarKey(int slot) {
        if (slot < 0 || slot >= mc.options.hotbarKeys.length) return;

        KeyBinding keyBinding = mc.options.hotbarKeys[slot];
        KeyBindingAccessor accessor = (KeyBindingAccessor) keyBinding;
        accessor.meteor$setTimesPressed(accessor.meteor$getTimesPressed() + 1);
        keyBinding.setPressed(true);
        pendingHotbarKeyRelease = slot;
    }
}
