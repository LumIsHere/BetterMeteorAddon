package com.bettermeteor.addon.utils.render.postprocess;

import com.bettermeteor.addon.modules.MurderMystery;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.postprocess.EntityShader;
import meteordevelopment.meteorclient.utils.render.postprocess.OutlineUniforms;
import net.minecraft.entity.Entity;

public class MurderMysteryOutlineShader extends EntityShader {
    private static MurderMystery murderMystery;

    public MurderMysteryOutlineShader() {
        super(MeteorRenderPipelines.POST_OUTLINE);
    }

    @Override
    protected boolean shouldDraw() {
        if (murderMystery == null) murderMystery = Modules.get().get(MurderMystery.class);
        return murderMystery != null && murderMystery.shouldForceRender();
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        return shouldDraw() && murderMystery.shouldRenderEntity(entity);
    }

    @Override
    protected void setupPass(MeshRenderer renderer) {
        renderer.uniform("OutlineData", OutlineUniforms.write(2, 0.3f, ShapeMode.Both.ordinal(), 3.5f));
    }
}
