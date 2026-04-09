package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.modules.HandChams;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Unique
    private HandChams bettermeteor$handChams() {
        return Modules.get().get(HandChams.class);
    }

    @ModifyExpressionValue(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayers;entityTranslucent(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer bettermeteor$modifyHandTexture(RenderLayer original, MatrixStack matrixStack, OrderedRenderCommandQueue entityRenderCommandQueue, int light, Identifier skinTexture, ModelPart modelPart, boolean sleeveVisible) {
        HandChams module = bettermeteor$handChams();
        if (module != null && module.isActive()) {
            Identifier texture = module.handTexture.get() ? skinTexture : HandChams.BLANK;
            return RenderLayers.entityTranslucent(texture);
        }

        return original;
    }

    @WrapWithCondition(method = "renderArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitModelPart(Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IILnet/minecraft/client/texture/Sprite;)V"))
    private boolean bettermeteor$modifyHandColor(OrderedRenderCommandQueue instance, ModelPart modelPart, MatrixStack matrixStack, RenderLayer renderLayer, int light, int uv, Sprite sprite) {
        HandChams module = bettermeteor$handChams();
        if (module != null && module.isActive()) {
            instance.submitModelPart(modelPart, matrixStack, renderLayer, light, uv, null, module.handColor.get().getPacked(), null);
            return false;
        }

        return true;
    }
}
