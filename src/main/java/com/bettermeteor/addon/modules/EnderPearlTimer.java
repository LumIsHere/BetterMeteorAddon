package com.bettermeteor.addon.modules;

import com.bettermeteor.addon.BetterMeteorAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.simulator.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.entity.simulator.SimulationStep;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.text.Text;

import java.util.Locale;

public class EnderPearlTimer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> maxSimulationTicks = sgGeneral.add(new IntSetting.Builder()
        .name("max-simulation-ticks")
        .description("Maximum number of ticks to simulate for a pearl landing.")
        .defaultValue(400)
        .range(20, 1200)
        .sliderRange(20, 1200)
        .build()
    );

    private final ProjectileEntitySimulator simulator = new ProjectileEntitySimulator();
    private String lastTimerText;

    public EnderPearlTimer() {
        super(BetterMeteorAddon.CATEGORY, "ender-pearl-timer", "Shows your active ender pearl landing time in Minecraft's vanilla subtitle slot above the action bar.");
    }

    @Override
    public void onActivate() {
        lastTimerText = null;
        clearTitle();
    }

    @Override
    public void onDeactivate() {
        lastTimerText = null;
        clearTitle();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) {
            lastTimerText = null;
            clearTitle();
            return;
        }

        EnderPearlEntity pearl = getOwnPearl();
        if (pearl == null) {
            lastTimerText = null;
            clearTitle();
            return;
        }

        int ticksLeft = simulateTicksToLanding(pearl);
        if (ticksLeft < 0) {
            lastTimerText = null;
            clearTitle();
            return;
        }

        String timerText = formatSeconds(ticksLeft);
        if (timerText.equals(lastTimerText)) return;

        mc.inGameHud.setTitleTicks(0, 2, 0);
        mc.inGameHud.setTitle(Text.empty());
        mc.inGameHud.setSubtitle(Text.literal(timerText));
        lastTimerText = timerText;
    }

    private EnderPearlEntity getOwnPearl() {
        EnderPearlEntity bestPearl = null;
        double bestDistance = Double.MAX_VALUE;

        for (EnderPearlEntity pearl : mc.world.getEntitiesByClass(EnderPearlEntity.class, mc.player.getBoundingBox().expand(256), entity -> true)) {
            if (pearl == null || pearl.isRemoved()) continue;
            if (pearl.getOwner() != mc.player) continue;

            double distance = pearl.squaredDistanceTo(mc.player);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPearl = pearl;
            }
        }

        return bestPearl;
    }

    private int simulateTicksToLanding(EnderPearlEntity pearl) {
        if (!simulator.set(pearl)) return -1;

        for (int ticks = 0; ticks <= maxSimulationTicks.get(); ticks++) {
            SimulationStep step = simulator.tick();
            if (step.shouldStop) return ticks + 1;
        }

        return -1;
    }

    private String formatSeconds(int ticks) {
        return String.format(Locale.US, "%.1fs", ticks / 20.0);
    }

    private void clearTitle() {
        if (mc.inGameHud == null) return;

        mc.inGameHud.setTitle(Text.empty());
        mc.inGameHud.setSubtitle(Text.empty());
    }
}
