/*
 * Font atlas loading based on the original font-fix approach by Nippaku-Zanmu.
 */
package com.bettermeteor.addon.utils.font;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackRange;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class FontFix {
    public Texture texture;

    private static final int SIZE = 2048;

    private final int height;
    private final float scale;
    private final float ascent;
    private final ByteBuffer buffer;
    private final STBTTFontinfo fontInfo;
    private final ByteBuffer bitmap;
    private final STBTTPackContext packContext;
    private final Int2ObjectOpenHashMap<CharData> charMap = new Int2ObjectOpenHashMap<>();

    private long loadTimer;
    private int loadCount;
    private final int loadSpeedLimit = 7;

    public FontFix(ByteBuffer buffer, int height) {
        this.buffer = buffer;
        this.height = height;

        fontInfo = STBTTFontinfo.create();
        STBTruetype.stbtt_InitFont(fontInfo, buffer);

        bitmap = BufferUtils.createByteBuffer(SIZE * SIZE);

        packContext = STBTTPackContext.create();
        STBTruetype.stbtt_PackBegin(packContext, bitmap, SIZE, SIZE, 0, 1);

        texture = new Texture(SIZE, SIZE, TextureFormat.RED8, FilterMode.LINEAR, FilterMode.LINEAR);
        texture.upload(bitmap);
        scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ascent = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, null, null);
            this.ascent = ascent.get(0);
        }

        preloadAsciiCharacters();
    }

    private void preloadAsciiCharacters() {
        STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(128);
        STBTTPackRange.Buffer packRange = STBTTPackRange.create(1);
        packRange.put(STBTTPackRange.create().set(height, 32, null, 128, cdata, (byte) 2, (byte) 2));
        packRange.flip();

        STBTruetype.stbtt_PackFontRanges(packContext, buffer, 0, packRange);

        for (int i = 0; i < cdata.capacity(); i++) {
            putCharData(i + 32, cdata.get(i));
        }

        createTexture();
    }

    public boolean hasGlyph(int codePoint) {
        return getCharData(codePoint) != null;
    }

    public double getAdvance(int codePoint) {
        CharData charData = getCharData(codePoint);
        if (charData == null) charData = getCharData(32);
        return charData != null ? charData.xAdvance : 0;
    }

    public int getHeight() {
        return height;
    }

    public double renderCodePoint(MeshBuilder mesh, int codePoint, double x, double y, Color color, double scale) {
        CharData charData = getCharData(codePoint);
        if (charData == null) charData = getCharData(32);
        if (charData == null) return x;

        y += ascent * this.scale * scale;

        mesh.ensureCapacity(4, 6);
        mesh.quad(
            mesh.vec2(x + charData.x0 * scale, y + charData.y0 * scale).vec2(charData.u0, charData.v0).color(color).next(),
            mesh.vec2(x + charData.x0 * scale, y + charData.y1 * scale).vec2(charData.u0, charData.v1).color(color).next(),
            mesh.vec2(x + charData.x1 * scale, y + charData.y1 * scale).vec2(charData.u1, charData.v1).color(color).next(),
            mesh.vec2(x + charData.x1 * scale, y + charData.y0 * scale).vec2(charData.u1, charData.v0).color(color).next()
        );

        return x + charData.xAdvance * scale;
    }

    private CharData getCharData(int codePoint) {
        CharData cached = charMap.get(codePoint);
        if (cached != null) return cached;
        if (codePoint < 0) return null;
        if (STBTruetype.stbtt_FindGlyphIndex(fontInfo, codePoint) == 0 && codePoint != 0) return null;

        loadCharacter(List.of(codePoint));
        return charMap.get(codePoint);
    }

    private void loadCharacter(List<Integer> codePoints) {
        if (System.currentTimeMillis() - loadTimer > 100) {
            loadTimer = System.currentTimeMillis();
            loadCount = 0;
        }
        if (loadCount >= loadSpeedLimit) return;

        List<Integer> supported = new ArrayList<>();
        for (Integer codePoint : codePoints) {
            if (codePoint == null || charMap.containsKey(codePoint)) continue;
            if (STBTruetype.stbtt_FindGlyphIndex(fontInfo, codePoint) == 0 && codePoint != 0) continue;
            supported.add(codePoint);
        }
        if (supported.isEmpty()) return;

        STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(supported.size());
        STBTTPackRange.Buffer packRange = STBTTPackRange.create(supported.size());

        for (int i = 0; i < supported.size(); i++) {
            packRange.put(STBTTPackRange.create().set(height, supported.get(i), null, 1, cdata.position(i), (byte) 2, (byte) 2));
        }
        packRange.flip();

        STBTruetype.stbtt_PackFontRanges(packContext, buffer, 0, packRange);

        for (int i = 0; i < supported.size(); i++) {
            putCharData(supported.get(i), cdata.get(i));
        }

        createTexture();
        loadCount++;
    }

    private void putCharData(int codePoint, STBTTPackedchar packedChar) {
        float ipw = 1f / SIZE;
        float iph = 1f / SIZE;

        charMap.put(codePoint, new CharData(
            packedChar.xoff(),
            packedChar.yoff(),
            packedChar.xoff2(),
            packedChar.yoff2(),
            packedChar.x0() * ipw,
            packedChar.y0() * iph,
            packedChar.x1() * ipw,
            packedChar.y1() * iph,
            packedChar.xadvance()
        ));
    }

    private void createTexture() {
        texture = new Texture(SIZE, SIZE, TextureFormat.RED8, FilterMode.LINEAR, FilterMode.LINEAR);
        texture.upload(bitmap);
    }

    private record CharData(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float xAdvance) {
    }
}
