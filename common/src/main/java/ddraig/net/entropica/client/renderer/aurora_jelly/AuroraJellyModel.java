package ddraig.net.entropica.client.renderer.aurora_jelly;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Fauna 4 — Aurora Jelly (Magnetic Glow-Drifter)
 * 32x32 Translucent Blockbench Model (textures/entity/aurora_jelly/aurora_jelly.png):
 *  - "magnetic_core" (twin glowing mint-emerald dipole cubes), "manubrium", and 7 two-segment tendrils
 *    ("tendril_center" + 6 perimeter tendrils) are added to "body" BEFORE "bell" so the internal core
 *    renders cleanly inside the 4-tier semi-translucent aurora dome without depth occlusion.
 */
public class AuroraJellyModel extends EntityModel<AuroraJellyRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aurora_jelly"), "main"
    );

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart magnetic_core;
    private final ModelPart manubrium;
    private final ModelPart tendril_center;
    private final ModelPart tendril_center_lower;
    private final ModelPart tendril_fl;
    private final ModelPart tendril_fl_lower;
    private final ModelPart tendril_fr;
    private final ModelPart tendril_fr_lower;
    private final ModelPart tendril_bl;
    private final ModelPart tendril_bl_lower;
    private final ModelPart tendril_br;
    private final ModelPart tendril_br_lower;
    private final ModelPart tendril_ml;
    private final ModelPart tendril_ml_lower;
    private final ModelPart tendril_mr;
    private final ModelPart tendril_mr_lower;
    private final ModelPart bell;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation flyAnimation;
    private final KeyframeAnimation magnetizeAnimation;
    private final KeyframeAnimation startledFlashAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public AuroraJellyModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.body = this.root.getChild("body");
        this.magnetic_core = this.body.getChild("magnetic_core");
        this.manubrium = this.body.getChild("manubrium");
        this.tendril_center = this.body.getChild("tendril_center");
        this.tendril_center_lower = this.tendril_center.getChild("tendril_center_lower");
        this.tendril_fl = this.body.getChild("tendril_fl");
        this.tendril_fl_lower = this.tendril_fl.getChild("tendril_fl_lower");
        this.tendril_fr = this.body.getChild("tendril_fr");
        this.tendril_fr_lower = this.tendril_fr.getChild("tendril_fr_lower");
        this.tendril_bl = this.body.getChild("tendril_bl");
        this.tendril_bl_lower = this.tendril_bl.getChild("tendril_bl_lower");
        this.tendril_br = this.body.getChild("tendril_br");
        this.tendril_br_lower = this.tendril_br.getChild("tendril_br_lower");
        this.tendril_ml = this.body.getChild("tendril_ml");
        this.tendril_ml_lower = this.tendril_ml.getChild("tendril_ml_lower");
        this.tendril_mr = this.body.getChild("tendril_mr");
        this.tendril_mr_lower = this.tendril_mr.getChild("tendril_mr_lower");
        this.bell = this.body.getChild("bell");

        this.idleAnimation = AuroraJellyAnimation.IDLE.bake(modelRoot);
        this.flyAnimation = AuroraJellyAnimation.FLY.bake(modelRoot);
        this.magnetizeAnimation = AuroraJellyAnimation.MAGNETIZE.bake(modelRoot);
        this.startledFlashAnimation = AuroraJellyAnimation.STARTLED_FLASH.bake(modelRoot);
        this.attackAnimation = AuroraJellyAnimation.ATTACK.bake(modelRoot);
        this.deathAnimation = AuroraJellyAnimation.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild(
                "root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -16.0F, 0.0F)
        );

        body.addOrReplaceChild(
                "magnetic_core",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.8F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).mirror().addBox(0.8F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(0.0F, -2.2F, 0.0F)
        );

        body.addOrReplaceChild(
                "manubrium",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.5F, 0.0F)
        );

        PartDefinition tendril_center = body.addOrReplaceChild(
                "tendril_center",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.08F)),
                PartPose.offset(0.0F, 1.0F, 0.0F)
        );

        tendril_center.addOrReplaceChild(
                "tendril_center_lower",
                CubeListBuilder.create()
                        .texOffs(28, 12).addBox(-0.5F, -0.2F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.04F)),
                PartPose.offset(0.0F, 7.0F, 0.0F)
        );

        PartDefinition tendril_fl = body.addOrReplaceChild(
                "tendril_fl",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.1F, 0.8F, -2.6F)
        );

        tendril_fl.addOrReplaceChild(
                "tendril_fl_lower",
                CubeListBuilder.create()
                        .texOffs(28, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.0F, 0.0F)
        );

        PartDefinition tendril_fr = body.addOrReplaceChild(
                "tendril_fr",
                CubeListBuilder.create()
                        .texOffs(24, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(2.1F, 0.8F, -2.6F)
        );

        tendril_fr.addOrReplaceChild(
                "tendril_fr_lower",
                CubeListBuilder.create()
                        .texOffs(28, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(0.0F, 7.0F, 0.0F)
        );

        PartDefinition tendril_bl = body.addOrReplaceChild(
                "tendril_bl",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.1F, 0.8F, 2.6F)
        );

        tendril_bl.addOrReplaceChild(
                "tendril_bl_lower",
                CubeListBuilder.create()
                        .texOffs(28, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.0F, 0.0F)
        );

        PartDefinition tendril_br = body.addOrReplaceChild(
                "tendril_br",
                CubeListBuilder.create()
                        .texOffs(24, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(2.1F, 0.8F, 2.6F)
        );

        tendril_br.addOrReplaceChild(
                "tendril_br_lower",
                CubeListBuilder.create()
                        .texOffs(28, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(0.0F, 7.0F, 0.0F)
        );

        PartDefinition tendril_ml = body.addOrReplaceChild(
                "tendril_ml",
                CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.6F, 0.8F, 0.0F)
        );

        tendril_ml.addOrReplaceChild(
                "tendril_ml_lower",
                CubeListBuilder.create()
                        .texOffs(28, 0).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.0F, 0.0F)
        );

        PartDefinition tendril_mr = body.addOrReplaceChild(
                "tendril_mr",
                CubeListBuilder.create()
                        .texOffs(24, 0).mirror().addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(3.6F, 0.8F, 0.0F)
        );

        tendril_mr.addOrReplaceChild(
                "tendril_mr_lower",
                CubeListBuilder.create()
                        .texOffs(28, 0).mirror().addBox(-0.5F, -2.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(0.0F, 7.0F, 0.0F)
        );

        // Added LAST on "body" so inner "magnetic_core" renders before the translucent 4-tier "bell"
        body.addOrReplaceChild(
                "bell",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.15F))
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.7F))
                        .texOffs(0, 12).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.02F))
                        .texOffs(1, 21).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.08F)),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(AuroraJellyRenderState state) {
        super.setupAnim(state);

        if (state.deathAnimationState.isStarted() || state.deathTime > 0.0F) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.magnetizeAnimationState.isStarted()) {
            this.magnetizeAnimation.apply(state.magnetizeAnimationState, state.ageInTicks);
        } else if (state.flyAnimationState.isStarted()) {
            this.flyAnimation.apply(state.flyAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        this.startledFlashAnimation.apply(state.startledFlashAnimationState, state.ageInTicks);
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
    }
}
