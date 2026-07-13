package com.benbenlaw.enderrf.client.render;

import codechicken.enderstorage.EnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.client.model.ButtonModelLibrary;
import codechicken.enderstorage.client.render.RenderCustomEndPortal;
import codechicken.lib.colour.Colour;
import codechicken.lib.colour.EnumColour;
import codechicken.lib.fluid.FluidUtils;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCModelLibrary;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.*;
import codechicken.lib.vec.uv.UVTranslation;
import com.benbenlaw.enderrf.EnderRF;
import com.benbenlaw.enderrf.block.BlockEnderBattery;
import com.benbenlaw.enderrf.block.entity.TileEnderBattery;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Map;

public class RenderTileEnderBattery implements BlockEntityRenderer<TileEnderBattery> {

    private static final RenderType baseType = RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(EnderRF.MOD_ID, "textures/ender_battery.png"));
    private static final RenderType buttonType = RenderType.entitySolid(ResourceLocation.fromNamespaceAndPath(EnderStorage.MOD_ID, "textures/buttons.png"));
    private static final RenderType pearlType = CCModelLibrary.getIcos4RenderType(ResourceLocation.fromNamespaceAndPath(EnderRF.MOD_ID, "textures/hedronmap.png"));

    public static final CCModel tankModel;
    public static final CCModel valveModel;
    public static final CCModel[] buttons;
    public static final RenderCustomEndPortal renderEndPortal = new RenderCustomEndPortal(0.1205, 0.24, 0.76, 0.24, 0.76);

    static {
        Map<String, CCModel> models = new OBJParser(ResourceLocation.fromNamespaceAndPath(EnderRF.MOD_ID, "models/endertank.obj"))
                .quads()
                .swapYZ()
                .parse();
        Transformation fix = new Translation(-0.0099 - 0.5, 0, -0.0027 - 0.5);
        valveModel = models.remove("Valve").apply(fix).computeNormals();
        tankModel = CCModel.combine(models.values()).apply(fix).computeNormals().shrinkUVs(0.004);

        buttons = new CCModel[3];
        for (int i = 0; i < 3; i++) {
            buttons[i] = ButtonModelLibrary.button.copy().apply(BlockEnderBattery.buttonT[i].with(new Translation(-0.5, 0, -0.5)));
        }
    }

    public RenderTileEnderBattery() {
    }

    public RenderTileEnderBattery(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEnderBattery enderBattery, float partialTicks, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        if (enderBattery == null) return;

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        float valveRot = (float) MathHelper.interpolate(
                enderBattery.pressure_state.b_rotate,
                enderBattery.pressure_state.a_rotate,
                partialTicks
        ) * 0.01745F;

        int pearlOffset = RenderUtils.getTimeOffset(enderBattery.getBlockPos());
        Matrix4 mat = new Matrix4(mStack);

        renderTank(ccrs, mat.copy(), source, enderBattery.rotation, valveRot, enderBattery.getFrequency(), pearlOffset);

        float ratio = (float) enderBattery.clientEnergy / (float) enderBattery.getStorage().getMaxEnergyStored();
        FluidStack stack = new FluidStack(Fluids.WATER, enderBattery.clientEnergy);
        renderFluid(ccrs, mat, source, stack, ratio);

        ccrs.reset();
    }

    public static void renderTank(CCRenderState ccrs, Matrix4 mat, MultiBufferSource buffers, int rotation, float valveRot, Frequency freq, int pearlOffset) {
        renderEndPortal.render(mat, buffers);
        ccrs.reset();
        mat.translate(0.5, 0, 0.5);
        mat.rotate((-90 * (rotation + 2)) * MathHelper.torad, Vector3.Y_POS);
        ccrs.bind(baseType, buffers);
        tankModel.render(ccrs, mat);
        Matrix4 valveMat = mat.copy().apply(new codechicken.lib.vec.Rotation(valveRot, Vector3.Z_POS).at(new Vector3(0, 0.4165, 0)));
        valveModel.render(ccrs, valveMat, new UVTranslation(0, freq.hasOwner() ? 13 / 64D : 0));

        ccrs.bind(buttonType, buffers);
        EnumColour[] colours = freq.toArray();
        for (int i = 0; i < 3; i++) {
            buttons[i].render(ccrs, mat, new UVTranslation(0.25 * (colours[i].getWoolMeta() % 4), 0.25 * (colours[i].getWoolMeta() / 4)));
        }

        double time = ClientUtils.getRenderTime() + pearlOffset;
        Matrix4 pearlMat = RenderUtils.getMatrix(mat.copy(), new Vector3(0, 0.45 + RenderUtils.getPearlBob(time) * 2, 0), new Rotation(time / 3, Vector3.Y_POS), 0.04);
        ccrs.brightness = 15728880;
        ccrs.bind(pearlType, buffers);
        CCModelLibrary.icosahedron4.render(ccrs, pearlMat);
        ccrs.reset();
    }

    public static void renderFluid(CCRenderState ccrs, Matrix4 mat, MultiBufferSource getter, FluidStack stack, float ratio) {
        if (ratio <= 0.001f) return;

        double minX = 0.22, minY = 0.12, minZ = 0.22;
        double maxX = 0.78, maxY = 0.75, maxZ = 0.78;
        double currentHeight = minY + (maxY - minY) * ratio;

        Cuboid6 box = new Cuboid6(minX, minY, minZ, maxX, currentHeight, maxZ);

        ccrs.reset();
        ccrs.bind(RenderType.entityTranslucent(ResourceLocation.withDefaultNamespace("textures/misc/white.png")), getter);
        ccrs.baseColour = 0x00FF0088;
        ccrs.brightness = 15728880;

        CCModel model = CCModel.quadModel(24);
        model.generateBlock(0, box);
        model.computeNormals();

        model.render(ccrs, mat);
        ccrs.reset();
    }

    /* red
    public static void renderFluid(CCRenderState ccrs, Matrix4 mat, MultiBufferSource getter, FluidStack stack, float ratio) {
    if (ratio <= 0.001f) return; // Don't render if empty

    double minX = 0.22, minY = 0.12, minZ = 0.22;
    double maxX = 0.78, maxY = 0.75, maxZ = 0.78;
    double currentHeight = minY + (maxY - minY) * ratio;

    Cuboid6 box = new Cuboid6(minX, minY, minZ, maxX, currentHeight, maxZ);

    ccrs.reset();

    ccrs.bind(RenderType.entityTranslucent(ResourceLocation.withDefaultNamespace("textures/misc/white.png")), getter);

    ccrs.baseColour = 0xFF000088;

    ccrs.brightness = 15728880;

    CCModel model = CCModel.quadModel(24);
    model.generateBlock(0, box);
    model.computeNormals();

    model.render(ccrs, mat);
    ccrs.reset();
}
     */
}