package com.bettermeteor.addon.modules;

import com.bettermeteor.addon.BetterMeteorAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class DigDown extends Module {
    public DigDown() {
        super(BetterMeteorAddon.MINIGAMES, "dig-down", "Shows a title based on the block two blocks below you.");
    }

    @Override
    public void onDeactivate() {
        if (mc.inGameHud != null) mc.inGameHud.clearTitle();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null || mc.inGameHud == null) return;

        BlockPos pos = mc.player.getBlockPos().down(2);
        BlockState state = mc.world.getBlockState(pos);

        String title = getTitle(state);
        if (title == null) {
            mc.inGameHud.clearTitle();
            return;
        }

        mc.inGameHud.setTitleTicks(0, 2, 0);
        mc.inGameHud.setTitle(Text.literal(title));
    }

    private String getTitle(BlockState state) {
        if (state.isOf(Blocks.DIRT)) return "1";
        if (state.isOf(Blocks.STONE)) return "2";
        if (state.isIn(BlockTags.PLANKS)) return "3";
        return null;
    }
}
