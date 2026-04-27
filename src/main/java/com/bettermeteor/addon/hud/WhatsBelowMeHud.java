package com.bettermeteor.addon.hud;

import com.bettermeteor.addon.BetterMeteorAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

public class WhatsBelowMeHud extends HudElement {
    public static final HudElementInfo<WhatsBelowMeHud> INFO = new HudElementInfo<>(BetterMeteorAddon.HUD_GROUP, "whats-below-me", "Shows a 2D column of every block below you.", WhatsBelowMeHud::new);

    private static final Color AIR_COLOR = new Color(30, 32, 38, 90);
    private static final Color OUTLINE_COLOR = new Color(0, 0, 0, 180);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> columnWidth = sgGeneral.add(new IntSetting.Builder()
        .name("column-width")
        .description("Width of the 2D block column.")
        .defaultValue(18)
        .range(4, 120)
        .sliderRange(4, 80)
        .build()
    );

    private final Setting<Integer> blockHeight = sgGeneral.add(new IntSetting.Builder()
        .name("block-height")
        .description("Height of each block row.")
        .defaultValue(12)
        .range(4, 32)
        .sliderRange(4, 24)
        .build()
    );

    private final Setting<Integer> padding = sgGeneral.add(new IntSetting.Builder()
        .name("padding")
        .description("Space around the HUD element.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Boolean> showText = sgGeneral.add(new BoolSetting.Builder()
        .name("show-text")
        .description("Show player coordinates and column range.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> background = sgRender.add(new BoolSetting.Builder()
        .name("background")
        .description("Render a background behind the column.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgRender.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Background color.")
        .defaultValue(new SettingColor(20, 20, 24, 120))
        .visible(background::get)
        .build()
    );

    public WhatsBelowMeHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean hasWorld = mc.world != null && mc.player != null;

        int pad = padding.get();
        int width = columnWidth.get();
        int rowHeight = blockHeight.get();
        int playerMarkerHeight = Math.max(5, rowHeight * 2);
        int gap = showText.get() ? 6 : 0;

        ColumnInfo info = hasWorld ? getColumnInfo(mc) : getPreviewInfo();
        double stripHeight = Math.max(rowHeight, info.blockCount * rowHeight);
        double textWidth = showText.get() ? getTextWidth(renderer, info) : 0;
        double textHeight = showText.get() ? renderer.textHeight(true) * 4 : 0;

        setSize(pad * 2.0 + width + gap + textWidth, pad * 2.0 + Math.max(playerMarkerHeight + stripHeight, textHeight));

        if (!hasWorld && !isInEditor()) return;
        if (background.get()) renderer.quad(x, y, getWidth(), getHeight(), backgroundColor.get());

        int elementX = (int) Math.round(x);
        int elementY = (int) Math.round(y);
        int columnX = elementX + pad;
        int columnY = elementY + pad;
        int blocksY = columnY + playerMarkerHeight;

        renderer.quad(columnX, columnY, width, playerMarkerHeight, AIR_COLOR);
        renderer.line(columnX, columnY, columnX + width, columnY, OUTLINE_COLOR);
        renderer.line(columnX, columnY + playerMarkerHeight, columnX + width, columnY + playerMarkerHeight, OUTLINE_COLOR);
        renderer.post(() -> drawPlayerHead(renderer, mc, columnX, columnY, width, playerMarkerHeight));

        if (hasWorld) renderWorldColumn(renderer, mc, info, columnX, blocksY, width, rowHeight);
        else renderPreviewColumn(renderer, info, columnX, blocksY, width, rowHeight);

        renderer.line(columnX, blocksY, columnX, blocksY + stripHeight, OUTLINE_COLOR);
        renderer.line(columnX + width, blocksY, columnX + width, blocksY + stripHeight, OUTLINE_COLOR);

        if (showText.get()) renderText(renderer, info, columnX + width + gap, columnY);
    }

    private ColumnInfo getColumnInfo(MinecraftClient mc) {
        BlockPos playerPos = mc.player.getBlockPos();
        int bottomY = mc.world.getBottomY();
        int blockCount = Math.max(0, playerPos.getY() - bottomY);

        return new ColumnInfo(
            mc.player.getX(),
            mc.player.getY(),
            mc.player.getZ(),
            playerPos.getX(),
            playerPos.getY(),
            playerPos.getZ(),
            bottomY,
            blockCount
        );
    }

    private ColumnInfo getPreviewInfo() {
        return new ColumnInfo(12.5, 72.0, -8.5, 12, 72, -9, -64, 136);
    }

    private void renderWorldColumn(HudRenderer renderer, MinecraftClient mc, ColumnInfo info, int columnX, int blocksY, int width, int rowHeight) {
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int i = 0; i < info.blockCount; i++) {
            int blockY = info.blockY - 1 - i;
            pos.set(info.blockX, blockY, info.blockZ);

            BlockState state = mc.world.getBlockState(pos);
            if (state.isAir()) {
                renderer.quad(columnX, blocksY + i * rowHeight, width, rowHeight, AIR_COLOR);
            }
        }

        renderer.post(() -> renderWorldTextures(renderer, mc, info, columnX, blocksY, width, rowHeight));
    }

    private void renderPreviewColumn(HudRenderer renderer, ColumnInfo info, int columnX, int blocksY, int width, int rowHeight) {
        BlockState[] states = {
            Blocks.GRASS_BLOCK.getDefaultState(),
            Blocks.DIRT.getDefaultState(),
            Blocks.STONE.getDefaultState(),
            Blocks.DEEPSLATE.getDefaultState(),
            Blocks.BEDROCK.getDefaultState()
        };

        renderer.post(() -> {
            for (int i = 0; i < info.blockCount; i++) {
                drawBlockTexture(renderer, states[(i / 12) % states.length], columnX, blocksY + i * rowHeight, width, rowHeight);
            }
        });
    }

    private void renderWorldTextures(HudRenderer renderer, MinecraftClient mc, ColumnInfo info, int columnX, int blocksY, int width, int rowHeight) {
        if (mc.world == null) return;

        BlockPos.Mutable texturePos = new BlockPos.Mutable();
        for (int i = 0; i < info.blockCount; i++) {
            int blockY = info.blockY - 1 - i;
            texturePos.set(info.blockX, blockY, info.blockZ);

            BlockState state = mc.world.getBlockState(texturePos);
            drawBlockTexture(renderer, state, columnX, blocksY + i * rowHeight, width, rowHeight);
        }
    }

    private void drawBlockTexture(HudRenderer renderer, BlockState state, int tileX, int tileY, int width, int height) {
        if (state.isAir()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Sprite sprite = mc.getBlockRenderManager().getModel(state).particleSprite();
        renderer.drawContext.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, sprite, tileX, tileY, width, height);
    }

    private void drawPlayerHead(HudRenderer renderer, MinecraftClient mc, int markerX, int markerY, int markerWidth, int markerHeight) {
        if (mc.player == null) return;

        int size = Math.min(markerWidth, markerHeight);
        int headX = markerX + (markerWidth - size) / 2;
        int headY = markerY + (markerHeight - size) / 2;

        PlayerSkinDrawer.draw(renderer.drawContext, getPlayerSkinTextures(mc), headX, headY, size, 0xFFFFFFFF);
    }

    private SkinTextures getPlayerSkinTextures(MinecraftClient mc) {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) return entry.getSkinTextures();
        }

        return DefaultSkinHelper.getSkinTextures(mc.getGameProfile());
    }

    private double getTextWidth(HudRenderer renderer, ColumnInfo info) {
        return Math.max(
            Math.max(renderer.textWidth(getPositionLine(info), true), renderer.textWidth(getBlockLine(info), true)),
            Math.max(renderer.textWidth(getRangeLine(info), true), renderer.textWidth(getCountLine(info), true))
        );
    }

    private void renderText(HudRenderer renderer, ColumnInfo info, double textX, double textY) {
        Color color = Hud.get().textColors.get().getFirst();
        double lineHeight = renderer.textHeight(true);

        renderer.text(getPositionLine(info), textX, textY, color, true);
        renderer.text(getBlockLine(info), textX, textY + lineHeight, color, true);
        renderer.text(getRangeLine(info), textX, textY + lineHeight * 2, color, true);
        renderer.text(getCountLine(info), textX, textY + lineHeight * 3, color, true);
    }

    private String getPositionLine(ColumnInfo info) {
        return String.format(Locale.US, "Player: %.1f %.1f %.1f", info.playerX, info.playerY, info.playerZ);
    }

    private String getBlockLine(ColumnInfo info) {
        return "Block: " + info.blockX + " " + info.blockY + " " + info.blockZ;
    }

    private String getRangeLine(ColumnInfo info) {
        return "Below: y " + (info.blockY - 1) + " to " + info.bottomY;
    }

    private String getCountLine(ColumnInfo info) {
        return "Blocks: " + info.blockCount;
    }

    private record ColumnInfo(double playerX, double playerY, double playerZ, int blockX, int blockY, int blockZ, int bottomY, int blockCount) {}
}
