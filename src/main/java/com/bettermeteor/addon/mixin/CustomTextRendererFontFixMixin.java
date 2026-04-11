package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.utils.font.FontFix;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.ByteBuffer;

import static meteordevelopment.meteorclient.renderer.text.CustomTextRenderer.SHADOW_COLOR;

@Mixin(value = CustomTextRenderer.class, remap = false)
public abstract class CustomTextRendererFontFixMixin implements TextRenderer {
    @Shadow @Final private MeshBuilder mesh;
    @Shadow private boolean building;
    @Shadow private boolean scaleOnly;
    @Shadow private double fontScale;
    @Shadow private double scale;

    private FontFix[] bettermeteor$fontsFix;
    private FontFix bettermeteor$fontFix;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bettermeteor$init(FontFace fontFace, CallbackInfo ci) throws IOException {
        ByteBuffer buffer = fontFace.readToDirectByteBuffer();

        bettermeteor$fontsFix = new FontFix[5];
        for (int i = 0; i < bettermeteor$fontsFix.length; i++) {
            bettermeteor$fontsFix[i] = new FontFix(buffer, (int) Math.round(27 * ((i * 0.5) + 1)));
        }
    }

    /**
     * Credit for the original font-fix approach: Nippaku-Zanmu.
     */
    @Overwrite
    public void begin(double scale, boolean scaleOnly, boolean big) {
        if (building) throw new RuntimeException("CustomTextRenderer.begin() called twice");

        if (!scaleOnly) mesh.begin();

        if (big) {
            this.bettermeteor$fontFix = bettermeteor$fontsFix[bettermeteor$fontsFix.length - 1];
        } else {
            double scaleA = Math.floor(scale * 10) / 10;

            int scaleI;
            if (scaleA >= 3) scaleI = 5;
            else if (scaleA >= 2.5) scaleI = 4;
            else if (scaleA >= 2) scaleI = 3;
            else if (scaleA >= 1.5) scaleI = 2;
            else scaleI = 1;

            bettermeteor$fontFix = bettermeteor$fontsFix[scaleI - 1];
        }

        this.building = true;
        this.scaleOnly = scaleOnly;
        this.fontScale = bettermeteor$fontFix.getHeight() / 27.0;
        this.scale = 1 + (scale - fontScale) / fontScale;
    }

    /**
     * Credit for the original font-fix approach: Nippaku-Zanmu.
     */
    @Overwrite
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) return 0;

        FontFix font = building ? bettermeteor$fontFix : bettermeteor$fontsFix[0];
        return (font.getWidth(text, length) + (shadow ? 1 : 0)) * scale / 1.5;
    }

    /**
     * Credit for the original font-fix approach: Nippaku-Zanmu.
     */
    @Overwrite
    public double getHeight(boolean shadow) {
        FontFix font = building ? bettermeteor$fontFix : bettermeteor$fontsFix[0];
        return (font.getHeight() + 1 + (shadow ? 1 : 0)) * scale / 1.5;
    }

    /**
     * Credit for the original font-fix approach: Nippaku-Zanmu.
     */
    @Overwrite
    public double render(String text, double x, double y, Color color, boolean shadow) {
        boolean wasBuilding = building;
        if (!wasBuilding) begin();

        double width;
        if (shadow) {
            int preShadowA = SHADOW_COLOR.a;
            SHADOW_COLOR.a = (int) (color.a / 255.0 * preShadowA);

            width = bettermeteor$fontFix.render(mesh, text, x + fontScale * scale / 1.5, y + fontScale * scale / 1.5, SHADOW_COLOR, scale / 1.5);
            bettermeteor$fontFix.render(mesh, text, x, y, color, scale / 1.5);

            SHADOW_COLOR.a = preShadowA;
        } else {
            width = bettermeteor$fontFix.render(mesh, text, x, y, color, scale / 1.5);
        }

        if (!wasBuilding) end();
        return width;
    }

    /**
     * Credit for the original font-fix approach: Nippaku-Zanmu.
     */
    @Overwrite
    public void end() {
        if (!building) throw new RuntimeException("CustomTextRenderer.end() called without calling begin()");

        if (!scaleOnly) {
            mesh.end();

            MeshRenderer.begin()
                .attachments(MinecraftClient.getInstance().getFramebuffer())
                .pipeline(MeteorRenderPipelines.UI_TEXT)
                .mesh(mesh)
                .sampler("u_Texture", bettermeteor$fontFix.texture.getGlTextureView(), bettermeteor$fontFix.texture.getSampler())
                .end();
        }

        building = false;
        scale = 1;
    }
}
