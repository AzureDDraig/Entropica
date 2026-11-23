package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.VoidArachneAnimations;
import ddraig.net.entropica.client.renderer.VoidArachneRenderState;
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
import net.minecraft.util.Mth;

/**
 * Fauna 46 — Void Arachne (Nether Spinner).
 * Exact conversion of all 49 cubes, 38 bone groups, and 64x64 UVs from void_arachne.bbmodel.
 */
public class VoidArachneModel extends EntityModel<VoidArachneRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_arachne"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart neck_grp;
    private final ModelPart head_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation biteAnimation;
    private final KeyframeAnimation blinkOutAnimation;
    private final KeyframeAnimation blinkInAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public VoidArachneModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        ModelPart bodyGrp = root.getChild("body_grp");
        ModelPart waistGrp = bodyGrp.getChild("waist_grp");
        ModelPart chestGrp = waistGrp.getChild("chest_grp");
        this.neck_grp = chestGrp.getChild("neck_grp");
        this.head_grp = this.neck_grp.getChild("head_grp");

        this.idleAnimation = VoidArachneAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = VoidArachneAnimations.WALK.bake(modelRoot);
        this.attackAnimation = VoidArachneAnimations.ATTACK.bake(modelRoot);
        this.biteAnimation = VoidArachneAnimations.BITE.bake(modelRoot);
        this.blinkOutAnimation = VoidArachneAnimations.BLINK_OUT.bake(modelRoot);
        this.blinkInAnimation = VoidArachneAnimations.BLINK_IN.bake(modelRoot);
        this.hurtAnimation = VoidArachneAnimations.HURT.bake(modelRoot);
        this.deathAnimation = VoidArachneAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create().texOffs(0, 19).addBox(-4.5F, -4.5F, -4.5F, 9.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(14, 44).addBox(-2.5F, -3.0F, 4.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, -8.5F, -1.0F));

        body_grp.addOrReplaceChild("abdomen_grp", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -4.5F, 0.0F, 10.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(37, 28).addBox(-4.0F, -3.5F, 9.5F, 8.0F, 6.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, -1.0F, 5.5F));

        PartDefinition fang_l_grp = body_grp.addOrReplaceChild("fang_l_grp", CubeListBuilder.create(), PartPose.offset(-2.1F, 1.1F, -4.2F));
        fang_l_grp.addOrReplaceChild("fang_tip_l_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.14F)), PartPose.offsetAndRotation(0.0F, 0.9F, -2.9F, 0.384F, 0.0F, 0.0F));
        fang_l_grp.addOrReplaceChild("fang_upper_l_r1", CubeListBuilder.create().texOffs(42, 48).addBox(-1.0F, -1.0F, -3.4F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3142F, 0.0F, 0.0F));

        PartDefinition fang_r_grp = body_grp.addOrReplaceChild("fang_r_grp", CubeListBuilder.create(), PartPose.offset(2.1F, 1.1F, -4.2F));
        fang_r_grp.addOrReplaceChild("fang_tip_r_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.14F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.9F, -2.9F, 0.384F, 0.0F, 0.0F));
        fang_r_grp.addOrReplaceChild("fang_upper_r_r1", CubeListBuilder.create().texOffs(42, 48).mirror().addBox(-1.0F, -1.0F, -3.4F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3142F, 0.0F, 0.0F));

        PartDefinition leg_l1_grp = body_grp.addOrReplaceChild("leg_l1_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, 0.3F, -3.2F, 0.0F, -0.7679F, 0.0F));
        leg_l1_grp.addOrReplaceChild("femur_l1_r1", CubeListBuilder.create().texOffs(0, 52).addBox(-6.6F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.06F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6632F));
        PartDefinition tibia_l1_grp = leg_l1_grp.addOrReplaceChild("tibia_l1_grp", CubeListBuilder.create(), PartPose.offset(-4.9F, -3.8F, 0.0F));
        tibia_l1_grp.addOrReplaceChild("tibia_l1_r1", CubeListBuilder.create().texOffs(19, 52).addBox(-5.3F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6981F));
        tibia_l1_grp.addOrReplaceChild("tarsus_l1_grp", CubeListBuilder.create().texOffs(56, 0).addBox(-1.1F, -0.4F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.1F, 3.4F, 0.0F));

        PartDefinition leg_r1_grp = body_grp.addOrReplaceChild("leg_r1_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, 0.3F, -3.2F, 0.0F, 0.7679F, 0.0F));
        leg_r1_grp.addOrReplaceChild("femur_r1_r1", CubeListBuilder.create().texOffs(0, 52).mirror().addBox(-0.4F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.06F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6632F));
        PartDefinition tibia_r1_grp = leg_r1_grp.addOrReplaceChild("tibia_r1_grp", CubeListBuilder.create(), PartPose.offset(4.9F, -3.8F, 0.0F));
        tibia_r1_grp.addOrReplaceChild("tibia_r1_r1", CubeListBuilder.create().texOffs(19, 52).mirror().addBox(-0.7F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.03F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6981F));
        tibia_r1_grp.addOrReplaceChild("tarsus_r1_grp", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-0.9F, -0.4F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.1F, 3.4F, 0.0F));

        PartDefinition leg_l2_grp = body_grp.addOrReplaceChild("leg_l2_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, 0.3F, -0.8F, 0.0F, -0.2618F, 0.0F));
        leg_l2_grp.addOrReplaceChild("femur_l2_r1", CubeListBuilder.create().texOffs(0, 52).addBox(-6.6F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.06F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6632F));
        PartDefinition tibia_l2_grp = leg_l2_grp.addOrReplaceChild("tibia_l2_grp", CubeListBuilder.create(), PartPose.offset(-4.9F, -3.8F, 0.0F));
        tibia_l2_grp.addOrReplaceChild("tibia_l2_r1", CubeListBuilder.create().texOffs(19, 52).addBox(-5.3F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6981F));
        tibia_l2_grp.addOrReplaceChild("tarsus_l2_grp", CubeListBuilder.create().texOffs(56, 0).addBox(-1.1F, -0.4F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.1F, 3.4F, 0.0F));

        PartDefinition leg_r2_grp = body_grp.addOrReplaceChild("leg_r2_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, 0.3F, -0.8F, 0.0F, 0.2618F, 0.0F));
        leg_r2_grp.addOrReplaceChild("femur_r2_r1", CubeListBuilder.create().texOffs(0, 52).mirror().addBox(-0.4F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.06F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6632F));
        PartDefinition tibia_r2_grp = leg_r2_grp.addOrReplaceChild("tibia_r2_grp", CubeListBuilder.create(), PartPose.offset(4.9F, -3.8F, 0.0F));
        tibia_r2_grp.addOrReplaceChild("tibia_r2_r1", CubeListBuilder.create().texOffs(19, 52).mirror().addBox(-0.7F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.03F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6981F));
        tibia_r2_grp.addOrReplaceChild("tarsus_r2_grp", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-0.9F, -0.4F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.1F, 3.4F, 0.0F));

        PartDefinition leg_l3_grp = body_grp.addOrReplaceChild("leg_l3_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, 0.3F, 1.6F, 0.0F, 0.2793F, 0.0F));
        leg_l3_grp.addOrReplaceChild("femur_l3_r1", CubeListBuilder.create().texOffs(0, 52).addBox(-6.6F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.06F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6632F));
        PartDefinition tibia_l3_grp = leg_l3_grp.addOrReplaceChild("tibia_l3_grp", CubeListBuilder.create(), PartPose.offset(-4.9F, -3.8F, 0.0F));
        tibia_l3_grp.addOrReplaceChild("tibia_l3_r1", CubeListBuilder.create().texOffs(19, 52).addBox(-5.3F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6981F));
        tibia_l3_grp.addOrReplaceChild("tarsus_l3_grp", CubeListBuilder.create().texOffs(56, 0).addBox(-1.1F, -0.4F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.1F, 3.4F, 0.0F));

        PartDefinition leg_r3_grp = body_grp.addOrReplaceChild("leg_r3_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, 0.3F, 1.6F, 0.0F, -0.2793F, 0.0F));
        leg_r3_grp.addOrReplaceChild("femur_r3_r1", CubeListBuilder.create().texOffs(0, 52).mirror().addBox(-0.4F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.06F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6632F));
        PartDefinition tibia_r3_grp = leg_r3_grp.addOrReplaceChild("tibia_r3_grp", CubeListBuilder.create(), PartPose.offset(4.9F, -3.8F, 0.0F));
        tibia_r3_grp.addOrReplaceChild("tibia_r3_r1", CubeListBuilder.create().texOffs(19, 52).mirror().addBox(-0.7F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.03F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6981F));
        tibia_r3_grp.addOrReplaceChild("tarsus_r3_grp", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-0.9F, -0.4F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.1F, 3.4F, 0.0F));

        PartDefinition leg_l4_grp = body_grp.addOrReplaceChild("leg_l4_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, 0.3F, 4.0F, 0.0F, 0.8029F, 0.0F));
        leg_l4_grp.addOrReplaceChild("femur_l4_r1", CubeListBuilder.create().texOffs(0, 52).addBox(-6.6F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.06F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6632F));
        PartDefinition tibia_l4_grp = leg_l4_grp.addOrReplaceChild("tibia_l4_grp", CubeListBuilder.create(), PartPose.offset(-4.9F, -3.8F, 0.0F));
        tibia_l4_grp.addOrReplaceChild("tibia_l4_r1", CubeListBuilder.create().texOffs(19, 52).addBox(-5.3F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6981F));
        tibia_l4_grp.addOrReplaceChild("tarsus_l4_grp", CubeListBuilder.create().texOffs(56, 0).addBox(-1.1F, -0.4F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.1F, 3.4F, 0.0F));

        PartDefinition leg_r4_grp = body_grp.addOrReplaceChild("leg_r4_grp", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, 0.3F, 4.0F, 0.0F, -0.8029F, 0.0F));
        leg_r4_grp.addOrReplaceChild("femur_r4_r1", CubeListBuilder.create().texOffs(0, 52).mirror().addBox(-0.4F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.06F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6632F));
        PartDefinition tibia_r4_grp = leg_r4_grp.addOrReplaceChild("tibia_r4_grp", CubeListBuilder.create(), PartPose.offset(4.9F, -3.8F, 0.0F));
        tibia_r4_grp.addOrReplaceChild("tibia_r4_r1", CubeListBuilder.create().texOffs(19, 52).mirror().addBox(-0.7F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.03F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6981F));
        tibia_r4_grp.addOrReplaceChild("tarsus_r4_grp", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-0.9F, -0.4F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.1F, 3.4F, 0.0F));

        PartDefinition waist_grp = body_grp.addOrReplaceChild("waist_grp", CubeListBuilder.create().texOffs(0, 36).addBox(-2.5F, -4.8F, -1.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(36, 37).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.08F)), PartPose.offset(0.0F, -4.5F, -1.6F));

        PartDefinition chest_grp = waist_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create().texOffs(17, 36).addBox(-3.0F, -4.0F, -1.5F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.02F))
                .texOffs(41, 8).addBox(-3.0F, -3.1F, -2.4F, 6.0F, 2.0F, 1.0F, new CubeDeformation(-0.02F)), PartPose.offset(0.0F, -4.5F, 0.0F));

        PartDefinition arm_l_grp = chest_grp.addOrReplaceChild("arm_l_grp", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, -0.4F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8F, -3.4F, 0.0F, -0.1047F, 0.0F, 0.4887F));
        arm_l_grp.addOrReplaceChild("forearm_l_grp", CubeListBuilder.create().texOffs(55, 51).addBox(-1.0F, -0.2F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.04F))
                .texOffs(0, 45).addBox(-1.5F, 0.1F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.22F)), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, -0.1396F, 0.0F, -0.0524F));

        PartDefinition arm_r_grp = chest_grp.addOrReplaceChild("arm_r_grp", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-1.0F, -0.4F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.8F, -3.4F, 0.0F, -0.1047F, 0.0F, -0.4887F));
        arm_r_grp.addOrReplaceChild("forearm_r_grp", CubeListBuilder.create().texOffs(55, 51).mirror().addBox(-1.0F, -0.2F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.04F)).mirror(false)
                .texOffs(0, 45).mirror().addBox(-1.5F, 0.1F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.22F)).mirror(false), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, -0.1396F, 0.0F, 0.0524F));

        PartDefinition neck_grp = chest_grp.addOrReplaceChild("neck_grp", CubeListBuilder.create().texOffs(56, 35).addBox(-1.0F, -1.7F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        neck_grp.addOrReplaceChild("head_grp", CubeListBuilder.create().texOffs(34, 12).addBox(-4.0F, -6.9F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(-0.9F))
                .texOffs(31, 0).addBox(-3.0F, -6.0F, -2.7F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.14F))
                .texOffs(31, 44).addBox(-3.0F, -5.7F, 2.3F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.12F))
                .texOffs(53, 40).addBox(-3.2F, -5.7F, -1.7F, 1.0F, 6.0F, 4.0F, new CubeDeformation(0.1F))
                .texOffs(53, 40).mirror().addBox(2.2F, -5.7F, -1.7F, 1.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(0.0F, -1.1F, 0.0F));

        body_grp.addOrReplaceChild("tabard_grp", CubeListBuilder.create().texOffs(36, 55).addBox(-1.5F, 0.0F, -0.75F, 3.0F, 4.0F, 1.0F, new CubeDeformation(-0.16F)), PartPose.offset(0.0F, -4.5F, -4.6F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(VoidArachneRenderState state) {
        super.setupAnim(state);

        if (state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }
        if (state.blinkOutAnimationState.isStarted()) {
            this.blinkOutAnimation.apply(state.blinkOutAnimationState, state.ageInTicks);
            return;
        }
        if (state.blinkInAnimationState.isStarted()) {
            this.blinkInAnimation.apply(state.blinkInAnimationState, state.ageInTicks);
            return;
        }

        if (state.attackAnimationState.isStarted()) {
            this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        } else if (state.biteAnimationState.isStarted()) {
            this.biteAnimation.apply(state.biteAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        if (state.hurtAnimationState.isStarted()) {
            this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);
        }

        float yawRad = Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD;
        float pitchRad = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
        this.neck_grp.yRot += yawRad * 0.30F;
        this.head_grp.yRot += yawRad * 0.70F;
        this.head_grp.xRot += pitchRad * 0.75F;
    }
}
