package ddraig.net.entropica.client.renderer.storm_kite;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class StormKiteModel extends EntityModel<StormKiteRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "storm_kite"), "main");

    private final ModelPart body_1;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation moveAnimation;
    private final KeyframeAnimation flapAnimation;
    private final KeyframeAnimation diveAnimation;
    private final KeyframeAnimation attackAnimation;

    public StormKiteModel(ModelPart root) {
        super(root);
        this.body_1 = root.getChild("body_1");
        this.idleAnimation = StormKiteAnimation.IDLE.bake(root);
        this.moveAnimation = StormKiteAnimation.MOVE.bake(root);
        this.flapAnimation = StormKiteAnimation.FLAP.bake(root);
        this.diveAnimation = StormKiteAnimation.DIVE.bake(root);
        this.attackAnimation = StormKiteAnimation.ATTACK.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body_1 = partdefinition.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(86, 43).addBox(-5.0F, -2.0F, -8.0F, 10.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, -16.0F));

        PartDefinition body_2 = body_1.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(86, 55).addBox(-6.0F, -2.5F, -8.0F, 12.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

        PartDefinition body_3 = body_2.addOrReplaceChild("body_3", CubeListBuilder.create().texOffs(86, 68).addBox(-6.5F, -3.0F, -8.0F, 13.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

        PartDefinition body_4 = body_3.addOrReplaceChild("body_4", CubeListBuilder.create().texOffs(86, 82).addBox(-6.0F, -2.5F, -8.0F, 12.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

        PartDefinition body_5 = body_4.addOrReplaceChild("body_5", CubeListBuilder.create().texOffs(86, 95).addBox(-5.0F, -2.0F, -8.0F, 10.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

        PartDefinition body_6 = body_5.addOrReplaceChild("body_6", CubeListBuilder.create().texOffs(86, 107).addBox(-3.5F, -1.5F, -8.0F, 7.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 8.0F));

        PartDefinition tail_1 = body_6.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(96, 0).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(96, 14).addBox(-1.0F, -0.8F, 0.0F, 2.0F, 1.6F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 12.0F));

        PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(96, 28).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 12.0F));

        tail_3.addOrReplaceChild("tail_tuft", CubeListBuilder.create().texOffs(52, 108).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 12.0F));

        PartDefinition wing_left_1 = body_3.addOrReplaceChild("wing_left_1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.5F, -20.0F, 8.0F, 3.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 0.0F, 0.0F));

        PartDefinition wing_left_2 = wing_left_1.addOrReplaceChild("wing_left_2", CubeListBuilder.create().texOffs(0, 43).addBox(0.0F, -1.5F, -16.0F, 10.0F, 3.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, 2.0F));

        PartDefinition wing_left_3 = wing_left_2.addOrReplaceChild("wing_left_3", CubeListBuilder.create().texOffs(0, 78).addBox(0.0F, -1.0F, -12.0F, 10.0F, 2.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 2.0F));

        PartDefinition wing_left_4 = wing_left_3.addOrReplaceChild("wing_left_4", CubeListBuilder.create().texOffs(0, 104).addBox(0.0F, -1.0F, -8.0F, 10.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 2.0F));

        PartDefinition wing_left_5 = wing_left_4.addOrReplaceChild("wing_left_5", CubeListBuilder.create().texOffs(86, 118).addBox(0.0F, -0.5F, -4.5F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 1.5F));

        wing_left_5.addOrReplaceChild("wing_left_6", CubeListBuilder.create().texOffs(52, 104).addBox(0.0F, -0.5F, -1.5F, 12.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 2.0F));

        PartDefinition wing_right_1 = body_3.addOrReplaceChild("wing_right_1", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -1.5F, -20.0F, 8.0F, 3.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 0.0F, 0.0F));

        PartDefinition wing_right_2 = wing_right_1.addOrReplaceChild("wing_right_2", CubeListBuilder.create().texOffs(0, 43).addBox(-10.0F, -1.5F, -16.0F, 10.0F, 3.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 2.0F));

        PartDefinition wing_right_3 = wing_right_2.addOrReplaceChild("wing_right_3", CubeListBuilder.create().texOffs(0, 78).addBox(-10.0F, -1.0F, -12.0F, 10.0F, 2.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 2.0F));

        PartDefinition wing_right_4 = wing_right_3.addOrReplaceChild("wing_right_4", CubeListBuilder.create().texOffs(0, 104).addBox(-10.0F, -1.0F, -8.0F, 10.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 2.0F));

        PartDefinition wing_right_5 = wing_right_4.addOrReplaceChild("wing_right_5", CubeListBuilder.create().texOffs(86, 118).addBox(-10.0F, -0.5F, -4.5F, 10.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 1.5F));

        wing_right_5.addOrReplaceChild("wing_right_6", CubeListBuilder.create().texOffs(52, 104).addBox(-12.0F, -0.5F, -1.5F, 12.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 2.0F));

        PartDefinition cephalic_left_1 = body_1.addOrReplaceChild("cephalic_left_1", CubeListBuilder.create().texOffs(68, 78).addBox(-0.5F, -0.5F, -8.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, -4.0F));

        PartDefinition cephalic_left_2 = cephalic_left_1.addOrReplaceChild("cephalic_left_2", CubeListBuilder.create().texOffs(68, 89).addBox(-0.8F, 0.0F, -6.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -8.0F));

        cephalic_left_2.addOrReplaceChild("cephalic_left_3", CubeListBuilder.create().texOffs(68, 98).addBox(-1.2F, 0.5F, -4.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -6.0F));

        PartDefinition cephalic_right_1 = body_1.addOrReplaceChild("cephalic_right_1", CubeListBuilder.create().texOffs(68, 78).addBox(-1.5F, -0.5F, -8.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, -4.0F));

        PartDefinition cephalic_right_2 = cephalic_right_1.addOrReplaceChild("cephalic_right_2", CubeListBuilder.create().texOffs(68, 89).addBox(-1.2F, 0.0F, -6.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -8.0F));

        cephalic_right_2.addOrReplaceChild("cephalic_right_3", CubeListBuilder.create().texOffs(68, 98).addBox(-0.8F, 0.5F, -4.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("hitbox", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, -16.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(StormKiteRenderState state) {
        super.setupAnim(state);
        this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        this.moveAnimation.apply(state.moveAnimationState, state.ageInTicks);
        this.flapAnimation.apply(state.flapAnimationState, state.ageInTicks);
        this.diveAnimation.apply(state.diveAnimationState, state.ageInTicks);
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
    }
}
