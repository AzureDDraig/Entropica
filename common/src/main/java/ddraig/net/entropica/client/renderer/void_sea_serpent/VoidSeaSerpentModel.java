package ddraig.net.entropica.client.renderer.void_sea_serpent;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class VoidSeaSerpentModel extends EntityModel<VoidSeaSerpentRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sea_serpent"), "main");

    private final ModelPart root;
    private final KeyframeAnimation swimAnimation;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation biteAnimation;
    private final KeyframeAnimation roarAnimation;

    public VoidSeaSerpentModel(ModelPart root) {
        super(root);
        this.root = root.getChild("root");
        this.swimAnimation = VoidSeaSerpentAnimation.SWIM.bake(root);
        this.idleAnimation = VoidSeaSerpentAnimation.IDLE.bake(root);
        this.biteAnimation = VoidSeaSerpentAnimation.BITE_ATTACK.bake(root);
        this.roarAnimation = VoidSeaSerpentAnimation.ROAR.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition segment_1 = root.addOrReplaceChild("segment_1", CubeListBuilder.create().texOffs(96, 0).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -15.0F, 0.0F));

        PartDefinition head = segment_1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 56).addBox(-3.0F, -3.0F, -8.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(70, 56).addBox(-2.0F, -3.0F, -16.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("crown_spine_r1", CubeListBuilder.create().texOffs(183, 56).mirror().addBox(0.0F, -8.0F, -2.0F, 0.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, -2.0F, -0.6922F, 0.056F, 0.211F));
        head.addOrReplaceChild("cheek_spike_r_r1", CubeListBuilder.create().texOffs(230, 58).mirror().addBox(-4.0F, -1.0F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, 1.0F, -1.0F, -0.2021F, 0.9903F, -0.2403F));
        head.addOrReplaceChild("cheek_spike_r_r2", CubeListBuilder.create().texOffs(230, 58).mirror().addBox(-3.0F, -1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, 3.0F, -1.0F, -0.5409F, 0.8934F, -0.6568F));
        head.addOrReplaceChild("cheek_spike_r_r3", CubeListBuilder.create().texOffs(230, 58).addBox(-4.0F, -1.0F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, -1.0F, 0.3275F, 0.9674F, 0.3913F));
        head.addOrReplaceChild("cheek_spike_l_r1", CubeListBuilder.create().texOffs(230, 58).addBox(0.0F, -1.0F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -1.0F, -1.0F, 0.3275F, -0.9674F, -0.3913F));
        head.addOrReplaceChild("cheek_spike_l_r2", CubeListBuilder.create().texOffs(230, 58).addBox(0.0F, -1.0F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 1.0F, -1.0F, -0.2021F, -0.9903F, 0.2403F));
        head.addOrReplaceChild("cheek_spike_l_r3", CubeListBuilder.create().texOffs(230, 58).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 3.0F, -1.0F, -0.5409F, -0.8934F, 0.6568F));
        head.addOrReplaceChild("horn_right_r1", CubeListBuilder.create().texOffs(209, 56).addBox(-1.0F, -4.0F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -3.0F, -1.0F, -0.5672F, -0.2618F, -0.1745F));
        head.addOrReplaceChild("horn_left_r1", CubeListBuilder.create().texOffs(209, 56).addBox(0.0F, -4.0F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -3.0F, -1.0F, -0.5672F, 0.2618F, 0.1745F));
        head.addOrReplaceChild("crown_spine_r2", CubeListBuilder.create().texOffs(184, 56).addBox(0.0F, -8.0F, -2.0F, 0.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, -2.0F, -0.6922F, -0.056F, -0.211F));
        head.addOrReplaceChild("jaw_lower", CubeListBuilder.create().texOffs(126, 56).addBox(-2.0F, -2.0F, -8.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -8.0F, -0.0436F, 0.0F, 0.0F));

        PartDefinition segment_2 = segment_1.addOrReplaceChild("segment_2", CubeListBuilder.create().texOffs(94, 0).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));
        segment_2.addOrReplaceChild("seg2_dorsal_fin_r1", CubeListBuilder.create().texOffs(4, 132).addBox(0.0F, -10.0F, -3.0F, 0.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 3.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition segment_3 = segment_2.addOrReplaceChild("segment_3", CubeListBuilder.create().texOffs(4, 0).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));
        segment_3.addOrReplaceChild("seg3_grand_sail_r1", CubeListBuilder.create().texOffs(34, 132).addBox(0.0F, -14.0F, -4.0F, 0.0F, 14.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 4.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition wing_pectoral_left = segment_3.addOrReplaceChild("wing_pectoral_left", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 0.0F, 5.0F, 0.5236F, 0.7854F, -0.5236F));
        wing_pectoral_left.addOrReplaceChild("seg3_wing_l_r1", CubeListBuilder.create().texOffs(4, 190).addBox(-1.1412F, 0.8364F, -4.9733F, 9.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(15, 201).addBox(-1.1412F, -0.1636F, -4.9733F, 9.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0742F, 5.346F, 1.4805F, -0.9995F, -0.4259F, 1.8367F));
        wing_pectoral_left.addOrReplaceChild("seg3_wing_l_r2", CubeListBuilder.create().texOffs(0, 186).addBox(0.0F, 0.0F, -4.0F, 9.0F, 0.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(13, 199).addBox(0.0F, -1.0F, -4.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -1.0F, 0.0F, -0.8727F, 0.0F, 1.2654F));

        PartDefinition wing_pectoral_right = segment_3.addOrReplaceChild("wing_pectoral_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, 0.0F, 5.0F, 0.5236F, -0.7854F, 0.5236F));
        wing_pectoral_right.addOrReplaceChild("seg3_wing_r_r1", CubeListBuilder.create().texOffs(4, 190).mirror().addBox(-7.8588F, -0.1636F, -4.9733F, 9.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(13, 199).addBox(-7.8588F, 0.8364F, -4.9733F, 9.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0742F, 5.346F, 1.4805F, -0.9995F, 0.4259F, -1.8367F));
        wing_pectoral_right.addOrReplaceChild("seg3_wing_r_r2", CubeListBuilder.create().texOffs(0, 186).mirror().addBox(-9.0F, -1.0F, -4.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(15, 201).mirror().addBox(-9.0F, 0.0F, -4.0F, 9.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -1.0F, 0.0F, -0.8727F, 0.0F, -1.2654F));

        PartDefinition segment_4 = segment_3.addOrReplaceChild("segment_4", CubeListBuilder.create().texOffs(2, 0).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));
        segment_4.addOrReplaceChild("seg4_dorsal_sail_r1", CubeListBuilder.create().texOffs(71, 132).addBox(0.0F, -12.0F, -4.0F, 0.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 5.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition segment_5 = segment_4.addOrReplaceChild("segment_5", CubeListBuilder.create().texOffs(2, 0).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));
        segment_5.addOrReplaceChild("seg5_dorsal_sail_r1", CubeListBuilder.create().texOffs(109, 132).addBox(0.0F, -10.0F, -4.0F, 0.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 5.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition segment_6 = segment_5.addOrReplaceChild("segment_6", CubeListBuilder.create().texOffs(2, 0).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));
        segment_6.addOrReplaceChild("seg6_dorsal_sail_r1", CubeListBuilder.create().texOffs(147, 132).addBox(0.0F, -8.0F, -4.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 5.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition segment_7 = segment_6.addOrReplaceChild("segment_7", CubeListBuilder.create().texOffs(2, 0).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));
        segment_7.addOrReplaceChild("seg7_dorsal_sail_r1", CubeListBuilder.create().texOffs(147, 132).addBox(0.0F, -8.0F, -4.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 5.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition wing_pelvic_left = segment_7.addOrReplaceChild("wing_pelvic_left", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 0.0F, 5.0F, 0.4363F, 0.6981F, -0.4363F));
        wing_pelvic_left.addOrReplaceChild("seg7_pelvic_l_r1", CubeListBuilder.create().texOffs(88, 188).addBox(6.5896F, 0.6952F, -6.0871F, 9.0F, 0.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(102, 201).addBox(6.5896F, -0.3048F, -6.0871F, 9.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6719F, -0.7166F, 1.4629F));
        wing_pelvic_left.addOrReplaceChild("seg7_pelvic_l_r2", CubeListBuilder.create().texOffs(88, 188).addBox(-0.897F, 0.3648F, -3.7504F, 9.0F, 0.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(100, 199).addBox(-0.897F, -0.6352F, -3.7504F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5422F, -0.4284F, 1.2267F));

        PartDefinition wing_pelvic_right = segment_7.addOrReplaceChild("wing_pelvic_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, 0.0F, 5.0F, 0.4363F, -0.6981F, 0.4363F));
        wing_pelvic_right.addOrReplaceChild("seg7_pelvic_r_r1", CubeListBuilder.create().texOffs(88, 188).mirror().addBox(-15.5896F, -0.3048F, -6.0871F, 9.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(100, 199).addBox(-15.5896F, 0.6952F, -6.0871F, 9.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6719F, 0.7166F, -1.4629F));
        wing_pelvic_right.addOrReplaceChild("seg7_pelvic_r_r2", CubeListBuilder.create().texOffs(88, 188).mirror().addBox(-8.103F, -0.6352F, -3.7504F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(102, 201).mirror().addBox(-8.103F, 0.3648F, -3.7504F, 9.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5422F, 0.4284F, -1.2267F));

        PartDefinition segment_8 = segment_7.addOrReplaceChild("segment_8", CubeListBuilder.create().texOffs(2, 0).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));
        segment_8.addOrReplaceChild("seg8_dorsal_sail_r1", CubeListBuilder.create().texOffs(147, 132).addBox(0.0F, -8.0F, -4.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 5.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition segment_9 = segment_8.addOrReplaceChild("segment_9", CubeListBuilder.create().texOffs(92, 0).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 10.0F));
        segment_9.addOrReplaceChild("seg9_dorsal_sail_r1", CubeListBuilder.create().texOffs(184, 132).addBox(0.0F, -8.0F, -3.0F, 0.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 4.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition segment_10 = segment_9.addOrReplaceChild("segment_10", CubeListBuilder.create().texOffs(92, 0).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));
        segment_10.addOrReplaceChild("seg10_dorsal_sail_r1", CubeListBuilder.create().texOffs(214, 132).addBox(0.0F, -7.0F, -3.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 4.0F, -0.4363F, 0.0F, 0.0F));

        PartDefinition segment_11 = segment_10.addOrReplaceChild("segment_11", CubeListBuilder.create().texOffs(92, 0).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));
        PartDefinition segment_12 = segment_11.addOrReplaceChild("segment_12", CubeListBuilder.create().texOffs(2, 90).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));
        PartDefinition segment_13 = segment_12.addOrReplaceChild("segment_13", CubeListBuilder.create().texOffs(2, 90).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));
        PartDefinition segment_14 = segment_13.addOrReplaceChild("segment_14", CubeListBuilder.create().texOffs(2, 90).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));
        PartDefinition segment_15 = segment_14.addOrReplaceChild("segment_15", CubeListBuilder.create().texOffs(70, 90).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

        PartDefinition segment_16 = segment_15.addOrReplaceChild("segment_16", CubeListBuilder.create().texOffs(70, 90).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(139, 226).addBox(0.0F, -1.0F, 6.0F, 0.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(94, 226).addBox(0.0F, -4.0F, 6.0F, 0.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 6.0F));

        segment_16.addOrReplaceChild("tail_fin_lower_l_r1", CubeListBuilder.create().texOffs(94, 226).addBox(-1.0F, -2.0F, 1.0F, 0.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -1.0F, 5.0F, 0.0F, 0.0F, -1.5708F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(VoidSeaSerpentRenderState state) {
        super.setupAnim(state);
        this.swimAnimation.apply(state.swimAnimationState, state.ageInTicks);
        this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        this.biteAnimation.apply(state.biteAnimationState, state.ageInTicks);
        this.roarAnimation.apply(state.roarAnimationState, state.ageInTicks);
    }
}
