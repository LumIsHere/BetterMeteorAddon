package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.utils.font.ConfigFontFallbackAccessor;
import com.bettermeteor.addon.utils.font.FontFix;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.renderer.text.CustomTextRenderer.SHADOW_COLOR;

@Mixin(value = CustomTextRenderer.class, remap = false)
public abstract class CustomTextRendererFontFixMixin implements TextRenderer {
    @Shadow @Final public FontFace fontFace;
    @Shadow private boolean building;
    @Shadow private boolean scaleOnly;
    @Shadow private double fontScale;
    @Shadow private double scale;

    @Unique private FontFix[][] bettermeteor$fontStacks;
    @Unique private FontFix[] bettermeteor$activeFontStack;
    @Unique private MeshBuilder[] bettermeteor$meshes;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bettermeteor$init(FontFace fontFace, CallbackInfo ci) throws IOException {
        bettermeteor$buildFontStacks(fontFace);
    }

    @Unique
    private void bettermeteor$buildFontStacks(FontFace primaryFontFace) throws IOException {
        List<FontFace> faces = new ArrayList<>();
        faces.add(primaryFontFace);
        Config config = Config.get();
        if (config instanceof ConfigFontFallbackAccessor accessor) {
            faces.addAll(accessor.bettermeteor$getFallbackFontFaces());
        }

        bettermeteor$fontStacks = new FontFix[5][faces.size()];
        bettermeteor$meshes = new MeshBuilder[faces.size()];

        List<ByteBuffer> buffers = new ArrayList<>(faces.size());
        for (FontFace face : faces) {
            buffers.add(face.readToDirectByteBuffer());
        }

        for (int fontIndex = 0; fontIndex < faces.size(); fontIndex++) {
            bettermeteor$meshes[fontIndex] = new MeshBuilder(MeteorRenderPipelines.UI_TEXT);

            ByteBuffer buffer = buffers.get(fontIndex);
            for (int sizeIndex = 0; sizeIndex < bettermeteor$fontStacks.length; sizeIndex++) {
                bettermeteor$fontStacks[sizeIndex][fontIndex] = new FontFix(buffer, (int) Math.round(27 * ((sizeIndex * 0.5) + 1)));
            }
        }
    }

    @Override
    @Overwrite
    public void setAlpha(double a) {
        if (bettermeteor$meshes == null) return;
        for (MeshBuilder mesh : bettermeteor$meshes) {
            mesh.alpha = a;
        }
    }

    /**
     * Ordered fallback font selection based on the original font-fix approach by Nippaku-Zanmu.
     */
    @Overwrite
    public void begin(double scale, boolean scaleOnly, boolean big) {
        if (building) throw new RuntimeException("CustomTextRenderer.begin() called twice");
        if (bettermeteor$fontStacks == null) throw new RuntimeException("CustomTextRenderer font stacks not initialized");

        if (!scaleOnly) {
            for (MeshBuilder mesh : bettermeteor$meshes) mesh.begin();
        }

        if (big) {
            this.bettermeteor$setActiveFontStack(bettermeteor$fontStacks.length - 1);
        } else {
            double scaleA = Math.floor(scale * 10) / 10;

            int scaleI;
            if (scaleA >= 3) scaleI = 5;
            else if (scaleA >= 2.5) scaleI = 4;
            else if (scaleA >= 2) scaleI = 3;
            else if (scaleA >= 1.5) scaleI = 2;
            else scaleI = 1;

            this.bettermeteor$setActiveFontStack(scaleI - 1);
        }

        this.building = true;
        this.scaleOnly = scaleOnly;

        this.fontScale = bettermeteor$activeFontStack[0].getHeight() / 27.0;
        this.scale = 1 + (scale - fontScale) / fontScale;
    }

    @Unique
    private void bettermeteor$setActiveFontStack(int stackIndex) {
        bettermeteor$activeFontStack = bettermeteor$fontStacks[stackIndex];
    }

    @Overwrite
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) return 0;

        FontFix[] fontStack = building ? bettermeteor$activeFontStack : bettermeteor$fontStacks[0];
        double width = 0;

        for (int i = 0; i < length; i++) {
            int cp = text.charAt(i);
            int fontIndex = bettermeteor$resolveFontIndex(fontStack, cp);
            width += fontStack[fontIndex].getAdvance(cp);
        }

        return (width + (shadow ? 1 : 0)) * scale / 1.5;
    }

    @Overwrite
    public double getHeight(boolean shadow) {
        FontFix[] fontStack = building ? bettermeteor$activeFontStack : bettermeteor$fontStacks[0];
        return (fontStack[0].getHeight() + 1 + (shadow ? 1 : 0)) * scale / 1.5;
    }

    @Overwrite
    public double render(String text, double x, double y, Color color, boolean shadow) {
        boolean wasBuilding = building;
        if (!wasBuilding) begin();

        double width;
        if (shadow) {
            int preShadowA = SHADOW_COLOR.a;
            SHADOW_COLOR.a = (int) (color.a / 255.0 * preShadowA);
            bettermeteor$renderPass(text, x + fontScale * scale / 1.5, y + fontScale * scale / 1.5, SHADOW_COLOR);
            SHADOW_COLOR.a = preShadowA;
            width = bettermeteor$renderPass(text, x, y, color);
        } else {
            width = bettermeteor$renderPass(text, x, y, color);
        }

        if (!wasBuilding) end();
        return width;
    }

    @Unique
    private double bettermeteor$renderPass(String text, double x, double y, Color color) {
        double cursorX = x;
        for (int i = 0; i < text.length(); i++) {
            int cp = text.charAt(i);
            int fontIndex = bettermeteor$resolveFontIndex(bettermeteor$activeFontStack, cp);
            cursorX = bettermeteor$activeFontStack[fontIndex].renderCodePoint(
                bettermeteor$meshes[fontIndex],
                cp,
                cursorX,
                y,
                color,
                scale / 1.5
            );
        }

        return cursorX;
    }

    @Unique
    private int bettermeteor$resolveFontIndex(FontFix[] fontStack, int codePoint) {
        for (int i = 0; i < fontStack.length; i++) {
            if (fontStack[i].hasGlyph(codePoint)) return i;
        }

        return 0;
    }

    @Overwrite
    public void end() {
        if (!building) throw new RuntimeException("CustomTextRenderer.end() called without calling begin()");

        if (!scaleOnly) {
            for (int i = 0; i < bettermeteor$meshes.length; i++) {
                MeshBuilder mesh = bettermeteor$meshes[i];
                mesh.end();

                MeshRenderer.begin()
                    .attachments(MinecraftClient.getInstance().getFramebuffer())
                    .pipeline(MeteorRenderPipelines.UI_TEXT)
                    .mesh(mesh)
                    .sampler("u_Texture", bettermeteor$activeFontStack[i].texture.getGlTextureView(), bettermeteor$activeFontStack[i].texture.getSampler())
                    .end();
            }
        }

        building = false;
        scale = 1;
    }

    @Overwrite
    public void destroy() {
        if (bettermeteor$fontStacks == null) return;

        for (FontFix[] stack : bettermeteor$fontStacks) {
            for (FontFix font : stack) {
                font.texture.close();
            }
        }
    }
}
