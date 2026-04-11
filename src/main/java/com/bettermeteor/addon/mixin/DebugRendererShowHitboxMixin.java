package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.modules.ShowHitbox;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.debug.DebugDataStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererShowHitboxMixin {
    @Unique
    private EntityHitboxDebugRenderer bettermeteor$entityHitboxRenderer;

    @Inject(method = "render", at = @At("TAIL"))
    private void bettermeteor$renderShowHitbox(Frustum frustum, double cameraX, double cameraY, double cameraZ, float tickDelta, CallbackInfo ci) {
        ShowHitbox module = Modules.get().get(ShowHitbox.class);
        if (module == null || !module.isActive()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        if (world == null || mc.getNetworkHandler() == null) return;

        DebugDataStore debugDataStore = mc.getNetworkHandler().getDebugDataStore();
        if (debugDataStore == null) return;

        if (bettermeteor$entityHitboxRenderer == null) {
            bettermeteor$entityHitboxRenderer = new EntityHitboxDebugRenderer(mc);
        }

        bettermeteor$entityHitboxRenderer.render(cameraX, cameraY, cameraZ, debugDataStore, frustum, tickDelta);
    }
}
