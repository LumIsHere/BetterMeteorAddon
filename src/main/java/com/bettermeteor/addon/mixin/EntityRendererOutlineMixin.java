package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.modules.MurderMystery;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererOutlineMixin<T extends Entity, S extends EntityRenderState> {
    @Unique
    private MurderMystery bettermeteor$murderMystery;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bettermeteor$onInit(EntityRendererFactory.Context context, CallbackInfo ci) {
        bettermeteor$murderMystery = Modules.get().get(MurderMystery.class);
    }

    @Inject(method = "canBeCulled", at = @At("HEAD"), cancellable = true)
    private void bettermeteor$canBeCulled(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (bettermeteor$murderMystery != null && bettermeteor$murderMystery.shouldRenderEntity(entity)) {
            cir.setReturnValue(false);
        }
    }
}
