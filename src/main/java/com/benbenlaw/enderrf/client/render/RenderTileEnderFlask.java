package com.benbenlaw.enderrf.client.render;

import java.util.Map;

import com.benbenlaw.enderrf.EnderRF;
import com.benbenlaw.enderrf.block.BlockEnderFlask;
import com.benbenlaw.enderrf.tile.TileEnderFlask;
import com.mojang.blaze3d.vertex.PoseStack;

import codechicken.enderstorage.EnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.client.model.ButtonModelLibrary;
import codechicken.enderstorage.client.render.RenderCustomEndPortal;
import codechicken.lib.colour.EnumColour;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCModelLibrary;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.UVTranslation;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderTileEnderFlask implements BlockEntityRenderer<TileEnderFlask> {

    private static final RenderType baseType = RenderType
            .entityCutout(ResourceLocation.fromNamespaceAndPath(EnderRF.MOD_ID, "textures/ender_flask.png"));
    private static final RenderType buttonType = RenderType
            .entitySolid(ResourceLocation.fromNamespaceAndPath(EnderStorage.MOD_ID, "textures/buttons.png"));
    private static final RenderType pearlType = CCModelLibrary
            .getIcos4RenderType(ResourceLocation.fromNamespaceAndPath(EnderRF.MOD_ID, "textures/pearl_chemical.png"));

    public static final CCModel tankModel;
    public static final CCModel valveModel;
    public static final CCModel[] buttons;
    public static final RenderCustomEndPortal renderEndPortal = new RenderCustomEndPortal(0.1205, 0.24, 0.76, 0.24,
            0.76);

    static {
        Map<String, CCModel> models = new OBJParser(
                ResourceLocation.fromNamespaceAndPath(EnderRF.MOD_ID, "models/endertank.obj")).quads().swapYZ().parse();
        Transformation fix = new Translation(-0.0099 - 0.5, 0, -0.0027 - 0.5);
        valveModel = models.remove("Valve").apply(fix).computeNormals();
        tankModel = CCModel.combine(models.values()).apply(fix).computeNormals().shrinkUVs(0.004);

        buttons = new CCModel[3];
        for (int i = 0; i < 3; i++) {
            buttons[i] = ButtonModelLibrary.button.copy()
                    .apply(BlockEnderFlask.buttonT[i].with(new Translation(-0.5, 0, -0.5)));
        }
    }

    public RenderTileEnderFlask() {
    }

    public RenderTileEnderFlask(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEnderFlask enderFlask, float partialTicks, PoseStack mStack, MultiBufferSource source,
            int packedLight, int packedOverlay) {
        if (enderFlask == null)
            return;

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        float valveRot = (float) MathHelper.interpolate(enderFlask.pressure_state.b_rotate,
                enderFlask.pressure_state.a_rotate, partialTicks) * 0.01745F;

        int pearlOffset = RenderUtils.getTimeOffset(enderFlask.getBlockPos());
        Matrix4 mat = new Matrix4(mStack);

        renderTank(ccrs, mat.copy(), source, enderFlask.rotation, valveRot, enderFlask.getFrequency(), pearlOffset);

        renderChemical(ccrs, mat, source, enderFlask.chemical_state, enderFlask.getStorage().getCapacity());

        ccrs.reset();
    }

    public static void renderTank(CCRenderState ccrs, Matrix4 mat, MultiBufferSource buffers, int rotation,
            float valveRot, Frequency freq, int pearlOffset) {
        renderEndPortal.render(mat, buffers);
        ccrs.reset();
        mat.translate(0.5, 0, 0.5);
        mat.rotate((-90 * (rotation + 2)) * MathHelper.torad, Vector3.Y_POS);
        ccrs.bind(baseType, buffers);
        tankModel.render(ccrs, mat);
        Matrix4 valveMat = mat.copy()
                .apply(new codechicken.lib.vec.Rotation(valveRot, Vector3.Z_POS).at(new Vector3(0, 0.4165, 0)));
        valveModel.render(ccrs, valveMat, new UVTranslation(0, freq.hasOwner() ? 13 / 64D : 0));

        ccrs.bind(buttonType, buffers);
        EnumColour[] colours = freq.toArray();
        for (int i = 0; i < 3; i++) {
            buttons[i].render(ccrs, mat,
                    new UVTranslation(0.25 * (colours[i].getWoolMeta() % 4), 0.25 * (colours[i].getWoolMeta() / 4)));
        }

        double time = ClientUtils.getRenderTime() + pearlOffset;
        Matrix4 pearlMat = RenderUtils.getMatrix(mat.copy(),
                new Vector3(0, 0.45 + RenderUtils.getPearlBob(time) * 2, 0), new Rotation(time / 3, Vector3.Y_POS),
                0.04);
        ccrs.brightness = 15728880;
        ccrs.bind(pearlType, buffers);
        CCModelLibrary.icosahedron4.render(ccrs, pearlMat);
        ccrs.reset();
    }

    public static void renderChemical(CCRenderState ccrs, Matrix4 mat, MultiBufferSource getter, ChemicalStack stack,
            long capacity) {

        double ratio = (double) stack.getAmount() / (double) capacity;

        double minX = 0.22, minY = 0.12, minZ = 0.22;
        double maxX = 0.78, maxY = 0.75, maxZ = 0.78;
        double currentHeight = maxY - (maxY - minY) * ratio;

        Cuboid6 box = new Cuboid6(minX, currentHeight, minZ, maxX, maxY, maxZ);

        ccrs.reset();
        ccrs.bind(RenderType.entityTranslucent(ResourceLocation.withDefaultNamespace("textures/misc/white.png")),
                getter);

        // Mekanism returns 0x00RRGGBB; CCL's baseColour is 0xRRGGBBAA, so shift up and add full alpha.
        ccrs.baseColour = (stack.getChemicalColorRepresentation() << 8) | 0x8F;
        ccrs.brightness = 15728880;

        CCModel model = CCModel.quadModel(24);
        model.generateBlock(0, box);
        model.computeNormals();

        model.render(ccrs, mat);
        ccrs.reset();
    }

    /*
     * red public static void renderFluid(CCRenderState ccrs, Matrix4 mat,
     * MultiBufferSource getter, FluidStack stack, float ratio) { if (ratio <=
     * 0.001f) return; // Don't render if empty
     * 
     * double minX = 0.22, minY = 0.12, minZ = 0.22; double maxX = 0.78, maxY =
     * 0.75, maxZ = 0.78; double currentHeight = minY + (maxY - minY) * ratio;
     * 
     * Cuboid6 box = new Cuboid6(minX, minY, minZ, maxX, currentHeight, maxZ);
     * 
     * ccrs.reset();
     * 
     * ccrs.bind(RenderType.entityTranslucent(ResourceLocation.withDefaultNamespace(
     * "textures/misc/white.png")), getter);
     * 
     * ccrs.baseColour = 0xFF000088;
     * 
     * ccrs.brightness = 15728880;
     * 
     * CCModel model = CCModel.quadModel(24); model.generateBlock(0, box);
     * model.computeNormals();
     * 
     * model.render(ccrs, mat); ccrs.reset(); }
     */
}