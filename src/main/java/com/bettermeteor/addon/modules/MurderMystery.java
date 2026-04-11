package com.bettermeteor.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import com.bettermeteor.addon.BetterMeteorAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.misc.text.TextUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MurderMystery extends Module {
    private static final String MURDER_MYSTERY_TITLE = "MURDER MYSTERY";

    private static final Color GOLD_COLOR = new Color(255, 210, 60);
    private static final Color PLAYER_COLOR = new Color(255, 255, 255);
    private static final Color MURDERER_COLOR = new Color(255, 0, 0);
    private static final Color DETECTIVE_COLOR = new Color(255, 255, 0);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> goldEsp = sgGeneral.add(new BoolSetting.Builder()
        .name("gold-esp")
        .description("Highlights dropped gold ingots with shader ESP while you are in Murder Mystery.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> playerEsp = sgGeneral.add(new BoolSetting.Builder()
        .name("player-esp")
        .description("Highlights other players with shader ESP while you are in Murder Mystery.")
        .defaultValue(true)
        .build()
    );

    private final Map<UUID, Role> roles = new HashMap<>();
    private boolean inMurderMystery;

    public MurderMystery() {
        super(BetterMeteorAddon.MINIGAMES, "murder-mystery", "Uses the scoreboard title to detect Murder Mystery and applies shader ESP for gold and players.");
    }

    @Override
    public void onDeactivate() {
        inMurderMystery = false;
        roles.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) {
            inMurderMystery = false;
            roles.clear();
            return;
        }

        inMurderMystery = isMurderMysterySidebar();
        if (!inMurderMystery) {
            roles.clear();
            return;
        }

        updateRoles();
    }

    public boolean shouldForceRender() {
        return isActive() && inMurderMystery && (goldEsp.get() || playerEsp.get());
    }

    public boolean shouldRenderEntity(Entity entity) {
        if (!shouldForceRender()) return false;

        if (goldEsp.get() && entity instanceof ItemEntity itemEntity) {
            return itemEntity.getStack().isOf(Items.GOLD_INGOT);
        }

        if (playerEsp.get() && entity instanceof PlayerEntity player) {
            return player != mc.player;
        }

        return false;
    }

    public Color getEntityColor(Entity entity) {
        if (!shouldRenderEntity(entity)) return null;

        if (entity instanceof ItemEntity) return GOLD_COLOR;
        if (entity instanceof PlayerEntity player) {
            return switch (roles.getOrDefault(player.getUuid(), Role.NONE)) {
                case MURDERER -> MURDERER_COLOR;
                case DETECTIVE -> DETECTIVE_COLOR;
                case NONE -> PLAYER_COLOR;
            };
        }

        return null;
    }

    private void updateRoles() {
        Map<UUID, Role> nextRoles = new HashMap<>();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            Role role = getRole(player);
            nextRoles.put(player.getUuid(), role);

            Role previousRole = roles.getOrDefault(player.getUuid(), Role.NONE);
            if (role != previousRole && role != Role.NONE) {
                info(buildRoleMessage(player, role));
            }
        }

        roles.clear();
        roles.putAll(nextRoles);
    }

    private Role getRole(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        if (mainHand.isOf(Items.IRON_SWORD) || offHand.isOf(Items.IRON_SWORD)) return Role.MURDERER;
        if (mainHand.isOf(Items.BOW) || offHand.isOf(Items.BOW)) return Role.DETECTIVE;
        return Role.NONE;
    }

    private MutableText buildRoleMessage(PlayerEntity player, Role role) {
        Color teamColor = TextUtils.getMostPopularColor(player.getDisplayName());

        MutableText playerName = Text.literal(player.getName().getString())
            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(teamColor.getPacked())));

        MutableText roleText = Text.literal(role.title + "!")
            .formatted(Formatting.BOLD, role.formatting);

        return Text.empty()
            .append(playerName)
            .append(Text.literal(" is ").formatted(Formatting.GRAY))
            .append(roleText);
    }

    private boolean isMurderMysterySidebar() {
        ScoreboardObjective objective = mc.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;

        String title = Formatting.strip(objective.getDisplayName().getString());
        return title != null && MURDER_MYSTERY_TITLE.equals(title.trim());
    }

    private enum Role {
        NONE("", Formatting.WHITE),
        MURDERER("MURDERER", Formatting.RED),
        DETECTIVE("DETECTIVE", Formatting.YELLOW);

        private final String title;
        private final Formatting formatting;

        Role(String title, Formatting formatting) {
            this.title = title;
            this.formatting = formatting;
        }
    }
}
