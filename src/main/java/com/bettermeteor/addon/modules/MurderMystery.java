package com.bettermeteor.addon.modules;

import com.bettermeteor.addon.BetterMeteorAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Formatting;

public class MurderMystery extends Module {
    private static final String MURDER_MYSTERY_TITLE = "MURDER MYSTERY";

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

    private boolean isInMurderMystery() {
        ScoreboardObjective objective = mc.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;

        String title = Formatting.strip(objective.getDisplayName().getString());
        if (title == null) return false;

        return MURDER_MYSTERY_TITLE.equals(title.trim());
    }
}
