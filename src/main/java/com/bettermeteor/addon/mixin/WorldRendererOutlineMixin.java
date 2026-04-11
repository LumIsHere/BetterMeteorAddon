package com.bettermeteor.addon.mixin;

import com.bettermeteor.addon.modules.MurderMystery;
import com.bettermeteor.addon.utils.render.postprocess.MurderMysteryOutlineShader;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import meteordevelopment.meteorclient.mixininterface.IEntityRenderState;
import meteordevelopment.meteorclient.mixininterface.IWorldRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.OutlineRenderCommandQueue;
import meteordevelopment.meteorclient.utils.render.NoopImmediateVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.NoopOutlineVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.WrapperImmediateVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererOutlineMixin {
    @Unique
    private static final MurderMysteryOutlineShader bettermeteor$shader = new MurderMysteryOutlineShader();

    @Unique
    private final OutlineRenderCommandQueue bettermeteor$outlineRenderCommandQueue = new OutlineRenderCommandQueue();

    @Unique
    private VertexConsumerProvider bettermeteor$provider;

    @Unique
    private RenderDispatcher bettermeteor$renderDispatcher;

    @Unique
    private MurderMystery bettermeteor$murderMystery;

    @Shadow
    @Final
    private EntityRenderManager entityRenderManager;

    @Inject(method = "render", at = @At("HEAD"))
    private void bettermeteor$onRenderHead(ObjectAllocator allocator,
                                           RenderTickCounter tickCounter,
                                           boolean renderBlockOutline,
                                           Camera camera,
                                           Matrix4f positionMatrix,
                                           Matrix4f projectionMatrix,
                                           Matrix4f matrix4f2,
                                           GpuBufferSlice fog,
                                           Vector4f fogColor,
                                           boolean shouldRenderSky,
                                           CallbackInfo ci) {
        if (bettermeteor$murderMystery == null) bettermeteor$murderMystery = Modules.get().get(MurderMystery.class);
        bettermeteor$shader.clearTexture();
    }

    @Inject(method = "pushEntityRenders", at = @At("TAIL"))
    private void bettermeteor$onPushEntityRenders(MatrixStack matrices, WorldRenderState worldState, OrderedRenderCommandQueue queue, CallbackInfo ci) {
        if (bettermeteor$murderMystery == null) bettermeteor$murderMystery = Modules.get().get(MurderMystery.class);
        if (bettermeteor$murderMystery == null || !bettermeteor$murderMystery.shouldForceRender()) return;

        if (bettermeteor$renderDispatcher == null) {
            bettermeteor$renderDispatcher = new RenderDispatcher(
                bettermeteor$outlineRenderCommandQueue,
                mc.getBlockRenderManager(),
                new WrapperImmediateVertexConsumerProvider(() -> bettermeteor$provider),
                mc.getAtlasManager(),
                NoopOutlineVertexConsumerProvider.INSTANCE,
                NoopImmediateVertexConsumerProvider.INSTANCE,
                mc.textRenderer
            );
        }

        bettermeteor$draw(worldState, matrices, entity -> bettermeteor$murderMystery.getEntityColor(entity));
    }

    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V"))
    private void bettermeteor$submitVertices(CallbackInfo ci) {
        bettermeteor$shader.submitVertices();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void bettermeteor$onRenderTail(ObjectAllocator allocator,
                                           RenderTickCounter tickCounter,
                                           boolean renderBlockOutline,
                                           Camera camera,
                                           Matrix4f positionMatrix,
                                           Matrix4f projectionMatrix,
                                           Matrix4f matrix4f2,
                                           GpuBufferSlice fog,
                                           Vector4f fogColor,
                                           boolean shouldRenderSky,
                                           CallbackInfo ci) {
        bettermeteor$shader.render();
    }

    @Inject(method = "onResized", at = @At("HEAD"))
    private void bettermeteor$onResized(int width, int height, CallbackInfo ci) {
        bettermeteor$shader.onResized(width, height);
    }

    @ModifyExpressionValue(method = "fillEntityRenderStates", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;isRenderingReady(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean bettermeteor$fillEntityRenderStatesIsRenderingReady(boolean original) {
        if (bettermeteor$murderMystery == null) bettermeteor$murderMystery = Modules.get().get(MurderMystery.class);
        if (bettermeteor$murderMystery != null && bettermeteor$murderMystery.shouldForceRender()) return true;
        return original;
    }

    @Unique
    private void bettermeteor$draw(WorldRenderState worldState, MatrixStack matrices, Function<Entity, Color> colorGetter) {
        var camera = worldState.cameraRenderState.pos;
        boolean empty = true;

        for (var state : worldState.entityRenderStates) {
            Entity entity = ((IEntityRenderState) state).meteor$getEntity();
            if (entity == null || !bettermeteor$shader.shouldDraw(entity)) continue;

            Color color = colorGetter.apply(entity);
            if (color == null) continue;
            bettermeteor$outlineRenderCommandQueue.setColor(color);

            var renderer = entityRenderManager.getRenderer(state);
            var offset = renderer.getPositionOffset(state);

            matrices.push();
            matrices.translate(state.x - camera.x + offset.x, state.y - camera.y + offset.y, state.z - camera.z + offset.z);
            renderer.render(state, matrices, bettermeteor$outlineRenderCommandQueue, worldState.cameraRenderState);
            matrices.pop();

            empty = false;
        }

        if (empty) return;

        ((IWorldRenderer) this).meteor$pushEntityOutlineFramebuffer(bettermeteor$shader.framebuffer);
        bettermeteor$provider = bettermeteor$shader.vertexConsumerProvider;

        bettermeteor$renderDispatcher.render();
        bettermeteor$outlineRenderCommandQueue.onNextFrame();

        bettermeteor$provider = null;
        ((IWorldRenderer) this).meteor$popEntityOutlineFramebuffer();
    }
}
