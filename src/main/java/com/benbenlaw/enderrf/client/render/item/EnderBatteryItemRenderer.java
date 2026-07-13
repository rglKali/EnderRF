package com.benbenlaw.enderrf.client.render.item;

import codechicken.enderstorage.api.Frequency;
import codechicken.lib.math.MathHelper;
import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Matrix4;
import com.benbenlaw.enderrf.block.EnderRFBlocks;
import com.benbenlaw.enderrf.client.render.RenderTileEnderBattery;
import com.benbenlaw.enderrf.item.EnderRFItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class EnderBatteryItemRenderer implements IItemRenderer {

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        Frequency freq = Frequency.readFromStack(stack);
        float ratio = 1.0f;

        Matrix4 mat = new Matrix4(poseStack);
        RenderTileEnderBattery.renderTank(ccrs, mat, source, 2, (float) (MathHelper.torad * 90F), freq, 0);

        mat.translate(-0.5, 0, -0.5);

        RenderTileEnderBattery.renderFluid(ccrs, mat, source, null, ratio);

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