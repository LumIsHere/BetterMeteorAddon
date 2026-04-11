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
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MurderMystery extends Module {
    private static final String MURDER_MYSTERY_TITLE = "MURDER MYSTERY";

    private static final Color GOLD_COLOR = new Color(255, 210, 60);
    private static final Color PLAYER_COLOR = new Color(255, 255, 255);
    private static final Color MURDERER_COLOR = new Color(255, 0, 0);
    private static final Color DETECTIVE_COLOR = new Color(255, 255, 0);
    private static final Set<Item> MURDERER_MAINHAND_ITEMS = new HashSet<>();

    static {
        MURDERER_MAINHAND_ITEMS.add(Items.IRON_SWORD);
        MURDERER_MAINHAND_ITEMS.add(Items.STONE_SWORD);
        MURDERER_MAINHAND_ITEMS.add(Items.IRON_SHOVEL);
        MURDERER_MAINHAND_ITEMS.add(Items.STICK);
        MURDERER_MAINHAND_ITEMS.add(Items.WOODEN_AXE);
        MURDERER_MAINHAND_ITEMS.add(Items.WOODEN_SWORD);
        MURDERER_MAINHAND_ITEMS.add(Items.DEAD_BUSH);
        MURDERER_MAINHAND_ITEMS.add(Items.SUGAR_CANE);
        MURDERER_MAINHAND_ITEMS.add(Items.STONE_SHOVEL);
        MURDERER_MAINHAND_ITEMS.add(Items.BLAZE_ROD);
        MURDERER_MAINHAND_ITEMS.add(Items.DIAMOND_SHOVEL);
        MURDERER_MAINHAND_ITEMS.add(Items.QUARTZ);
        MURDERER_MAINHAND_ITEMS.add(Items.PUMPKIN_PIE);
        MURDERER_MAINHAND_ITEMS.add(Items.GOLDEN_PICKAXE);
        MURDERER_MAINHAND_ITEMS.add(Items.LEATHER);
        MURDERER_MAINHAND_ITEMS.add(Items.NAME_TAG);
        MURDERER_MAINHAND_ITEMS.add(Items.CHARCOAL);
        MURDERER_MAINHAND_ITEMS.add(Items.FLINT);
        MURDERER_MAINHAND_ITEMS.add(Items.BONE);
        MURDERER_MAINHAND_ITEMS.add(Items.CARROT);
        MURDERER_MAINHAND_ITEMS.add(Items.GOLDEN_CARROT);
        MURDERER_MAINHAND_ITEMS.add(Items.COOKIE);
        MURDERER_MAINHAND_ITEMS.add(Items.DIAMOND_AXE);
        MURDERER_MAINHAND_ITEMS.add(Items.ROSE_BUSH);
        MURDERER_MAINHAND_ITEMS.add(Items.PRISMARINE_SHARD);
        MURDERER_MAINHAND_ITEMS.add(Items.COOKED_BEEF);
        MURDERER_MAINHAND_ITEMS.add(Items.NETHER_BRICK);
        MURDERER_MAINHAND_ITEMS.add(Items.COOKED_CHICKEN);
        MURDERER_MAINHAND_ITEMS.add(Items.MUSIC_DISC_BLOCKS);
        MURDERER_MAINHAND_ITEMS.add(Items.GOLDEN_HOE);
        MURDERER_MAINHAND_ITEMS.add(Items.LAPIS_LAZULI);
        MURDERER_MAINHAND_ITEMS.add(Items.GOLDEN_SWORD);
        MURDERER_MAINHAND_ITEMS.add(Items.DIAMOND_SWORD);
        MURDERER_MAINHAND_ITEMS.add(Items.DIAMOND_HOE);
        MURDERER_MAINHAND_ITEMS.add(Items.SHEARS);
        MURDERER_MAINHAND_ITEMS.add(Items.SALMON);
        MURDERER_MAINHAND_ITEMS.add(Items.RED_DYE);
        MURDERER_MAINHAND_ITEMS.add(Items.BREAD);
        MURDERER_MAINHAND_ITEMS.add(Items.OAK_BOAT);
        MURDERER_MAINHAND_ITEMS.add(Items.GLISTERING_MELON_SLICE);
        MURDERER_MAINHAND_ITEMS.add(Items.BOOK);
        MURDERER_MAINHAND_ITEMS.add(Items.JUNGLE_SAPLING);
        MURDERER_MAINHAND_ITEMS.add(Items.GOLDEN_AXE);
        MURDERER_MAINHAND_ITEMS.add(Items.DIAMOND_PICKAXE);
        MURDERER_MAINHAND_ITEMS.add(Items.GOLDEN_SHOVEL);
    }

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

        if (isWaitingScoreboard()) {
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
            Role previousRole = roles.getOrDefault(player.getUuid(), Role.NONE);
            if (previousRole == Role.MURDERER) role = Role.MURDERER;
            else if (previousRole == Role.DETECTIVE && role == Role.NONE) role = Role.DETECTIVE;
            nextRoles.put(player.getUuid(), role);

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

        if (MURDERER_MAINHAND_ITEMS.contains(mainHand.getItem()) || MURDERER_MAINHAND_ITEMS.contains(offHand.getItem())) return Role.MURDERER;
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

    private boolean isWaitingScoreboard() {
        ScoreboardObjective objective = mc.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;

        for (ScoreboardEntry score : mc.world.getScoreboard().getScoreboardEntries(objective)) {
            String line = Formatting.strip(score.display().getString());
            if (line != null && line.contains("Waiting...")) return true;
        }

        return false;
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
