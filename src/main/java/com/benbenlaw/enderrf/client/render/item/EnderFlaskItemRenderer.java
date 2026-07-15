package com.benbenlaw.enderrf.client.render.item;

import com.benbenlaw.enderrf.client.render.RenderTileEnderFlask;
import com.mojang.blaze3d.vertex.PoseStack;

import codechicken.enderstorage.api.Frequency;
import codechicken.lib.math.MathHelper;
import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class EnderFlaskItemRenderer implements IItemRenderer {

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource source,
            int packedLight, int packedOverlay) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        Frequency freq = Frequency.readFromStack(stack);

        Matrix4 mat = new Matrix4(poseStack);
        RenderTileEnderFlask.renderTank(ccrs, mat, source, 2, (float) (MathHelper.torad * 90F), freq, 0);

        mat.translate(-0.5, 0, -0.5);

        ccrs.reset();
    }

    @Override
    public PerspectiveModelState getModelState() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }
}