package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.modules.ShowHitbox;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererShowHitboxMixin {
    @Inject(method = "drawHitbox", at = @At("HEAD"), cancellable = true)
    private void bettermeteor$filterHitboxes(Entity entity, float tickProgress, boolean inLocalServer, CallbackInfo ci) {
        ShowHitbox module = Modules.get().get(ShowHitbox.class);
        if (module == null || !module.isActive()) return;

        if (!module.shouldRender(entity)) ci.cancel();
    }
}
