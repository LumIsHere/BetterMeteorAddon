package com.bettermeteor.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import org.lwjgl.glfw.GLFW;

public class SignCommand extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private AutomationStage automationStage = AutomationStage.NONE;
    private String pendingCommand;

    private final Setting<SignLine> line = sgGeneral.add(new EnumSetting.Builder<SignLine>()
        .name("line")
        .description("Which sign line to read the player name from.")
        .defaultValue(SignLine.LINE_4)
        .build()
    );

    private final Setting<String> commandTemplate = sgGeneral.add(new StringSetting.Builder()
        .name("command")
        .description("Command template to run. Use %s where the player name should go. A leading slash is optional.")
        .defaultValue("")
        .placeholder("%s")
        .build()
    );

    public SignCommand() {
        super(Categories.Misc, "sign-command", "Copies and runs a customizable command using the selected line of a clicked sign.");
    }

    @Override
    public void onDeactivate() {
        automationStage = AutomationStage.NONE;
        pendingCommand = null;
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (mc.world == null || mc.player == null) return;

        BlockEntity blockEntity = mc.world.getBlockEntity(event.blockPos);
        if (!(blockEntity instanceof SignBlockEntity sign)) return;

        String target = getSelectedLine(sign).trim();
        if (target.isEmpty()) return;

        String command = buildCommand(target);
        if (command == null) return;

        mc.keyboard.setClipboard(command);
        pendingCommand = command;
        automationStage = AutomationStage.OPEN_CHAT;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || pendingCommand == null || automationStage == AutomationStage.NONE) return;

        switch (automationStage) {
            case OPEN_CHAT -> {
                if (mc.currentScreen == null) {
                    mc.setScreen(new ChatScreen("", true));
                    automationStage = AutomationStage.PASTE_COMMAND;
                }
            }
            case PASTE_COMMAND -> {
                if (!(mc.currentScreen instanceof ChatScreen chatScreen)) return;

                chatScreen.keyPressed(new KeyInput(GLFW.GLFW_KEY_V, 0, getPasteModifier()));
                automationStage = AutomationStage.SEND_COMMAND;
            }
            case SEND_COMMAND -> {
                if (!(mc.currentScreen instanceof ChatScreen chatScreen)) return;

                chatScreen.keyPressed(new KeyInput(GLFW.GLFW_KEY_ENTER, 0, 0));
                automationStage = AutomationStage.NONE;
                pendingCommand = null;
            }
            case NONE -> {
            }
        }
    }

    private String getSelectedLine(SignBlockEntity sign) {
        SignText text = sign.getFrontText();
        if (mc.player != null && sign.isPlayerFacingFront(mc.player)) text = sign.getFrontText();
        else text = sign.getBackText();

        return text.getMessage(line.get().index, false).getString();
    }

    private String buildCommand(String playerName) {
        String template = commandTemplate.get().trim();
        if (template.isEmpty()) {
            error("Command template is empty.");
            return null;
        }

        if (!template.contains("%s")) {
            error("Command template must contain %%s for the player name.");
            return null;
        }

        String normalizedTemplate = template.startsWith("/") ? template : "/" + template;
        return normalizedTemplate.formatted(playerName);
    }

    private int getPasteModifier() {
        return MacWindowUtil.IS_MAC ? GLFW.GLFW_MOD_SUPER : GLFW.GLFW_MOD_CONTROL;
    }

    private enum AutomationStage {
        NONE,
        OPEN_CHAT,
        PASTE_COMMAND,
        SEND_COMMAND
    }

    private enum SignLine {
        LINE_1(0, "Line 1"),
        LINE_2(1, "Line 2"),
        LINE_3(2, "Line 3"),
        LINE_4(3, "Line 4");

        private final int index;
        private final String title;

        SignLine(int index, String title) {
            this.index = index;
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
