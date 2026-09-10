package ddraig.net.entropica.client.renderer.gemini_goat_mage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.Entropica;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class GeminiGoatMageModel extends EntityModel<GeminiGoatMageRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "gemini_goat_mage"), "main");

    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation castSolarAnimation;
    private final KeyframeAnimation castUmbralAnimation;
    private final KeyframeAnimation castDualAnimation;

    public GeminiGoatMageModel(ModelPart root) {
        super(root);
        this.root = root;
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.left_arm = this.torso.getChild("left_arm");
        this.right_arm = this.torso.getChild("right_arm");
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");

        this.idleAnimation = GeminiGoatMageAnimation.idle.bake(root);
        this.walkAnimation = GeminiGoatMageAnimation.walk.bake(root);
        this.castSolarAnimation = GeminiGoatMageAnimation.cast_solar_flame.bake(root);
        this.castUmbralAnimation = GeminiGoatMageAnimation.cast_umbral_flame.bake(root);
        this.castDualAnimation = GeminiGoatMageAnimation.cast_dual_gemini.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5.0F, -14.0F, -3.0F, 10.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        torso.addOrReplaceChild("chest_robe", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-5.5F, -14.5F, -3.5F, 11.0F, 15.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        torso.addOrReplaceChild("collar_v", CubeListBuilder.create().texOffs(0, 48)
                .addBox(-4.0F, -14.2F, -3.7F, 8.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        torso.addOrReplaceChild("sash_ruby", CubeListBuilder.create().texOffs(18, 48)
                .addBox(-5.7F, -2.5F, -3.7F, 11.4F, 2.5F, 7.4F, new CubeDeformation(0.0F)), PartPose.ZERO);

        PartDefinition skirt_root = torso.addOrReplaceChild("skirt_root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        skirt_root.addOrReplaceChild("skirt_front", CubeListBuilder.create().texOffs(0, 20)
                .addBox(-6.0F, 0.0F, -4.0F, 12.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        skirt_root.addOrReplaceChild("skirt_back", CubeListBuilder.create().texOffs(26, 20)
                .addBox(-6.0F, 0.0F, 3.0F, 12.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        skirt_root.addOrReplaceChild("skirt_left", CubeListBuilder.create().texOffs(52, 20)
                .addBox(5.0F, 0.0F, -3.0F, 1.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        skirt_root.addOrReplaceChild("skirt_right", CubeListBuilder.create().texOffs(52, 38)
                .addBox(-6.0F, 0.0F, -3.0F, 1.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create().texOffs(68, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -14.0F, 0.0F));

        head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(68, 16)
                .addBox(-2.5F, -4.0F, -7.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(84, 16)
                .addBox(4.0F, -6.0F, 0.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(90, 16)
                .addBox(-6.0F, -6.0F, 0.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(96, 0)
                .addBox(2.5F, -12.0F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(106, 0)
                .addBox(-4.5F, -12.0F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        head.addOrReplaceChild("spectacles", CubeListBuilder.create().texOffs(68, 24)
                .addBox(-3.5F, -5.5F, -4.5F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        PartDefinition hat_root = head.addOrReplaceChild("hat_root", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, 0.0F));
        hat_root.addOrReplaceChild("hat_brim", CubeListBuilder.create().texOffs(0, 64)
                .addBox(-8.0F, -1.0F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        hat_root.addOrReplaceChild("hat_cone_base", CubeListBuilder.create().texOffs(64, 64)
                .addBox(-5.0F, -7.0F, -5.0F, 10.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        hat_root.addOrReplaceChild("hat_cone_mid", CubeListBuilder.create().texOffs(0, 81)
                .addBox(-3.5F, -13.0F, -3.5F, 7.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        hat_root.addOrReplaceChild("hat_tip", CubeListBuilder.create().texOffs(28, 81)
                .addBox(-2.0F, -19.0F, -1.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        // Arms & Sleeves
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 81)
                .addBox(0.0F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -13.0F, 0.0F));
        left_arm.addOrReplaceChild("left_bell_sleeve", CubeListBuilder.create().texOffs(64, 81)
                .addBox(-0.5F, 2.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(84, 81)
                .addBox(0.5F, 5.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        left_forearm.addOrReplaceChild("left_paw", CubeListBuilder.create().texOffs(96, 81)
                .addBox(0.5F, 11.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(48, 93)
                .addBox(-4.0F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -13.0F, 0.0F));
        right_arm.addOrReplaceChild("right_bell_sleeve", CubeListBuilder.create().texOffs(64, 93)
                .addBox(-4.5F, 2.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(84, 93)
                .addBox(-3.5F, 5.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        right_forearm.addOrReplaceChild("right_paw", CubeListBuilder.create().texOffs(96, 93)
                .addBox(-3.5F, 11.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        // Orbiting Flames
        torso.addOrReplaceChild("flame_solar_cluster", CubeListBuilder.create().texOffs(104, 64)
                .addBox(7.0F, -10.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        torso.addOrReplaceChild("flame_umbral_cluster", CubeListBuilder.create().texOffs(104, 72)
                .addBox(-11.0F, -10.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        // Legs
        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 100)
                .addBox(-1.5F, 0.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 12.0F, 0.0F));
        left_leg.addOrReplaceChild("left_hoof", CubeListBuilder.create().texOffs(14, 100)
                .addBox(-2.0F, 9.0F, -3.0F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 100).mirror()
                .addBox(-1.5F, 0.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 12.0F, 0.0F));
        right_leg.addOrReplaceChild("right_hoof", CubeListBuilder.create().texOffs(14, 100).mirror()
                .addBox(-2.0F, 9.0F, -3.0F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(GeminiGoatMageRenderState state) {
        super.setupAnim(state);

        this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        this.castSolarAnimation.apply(state.castSolarAnimationState, state.ageInTicks);
        this.castUmbralAnimation.apply(state.castUmbralAnimationState, state.ageInTicks);
        this.castDualAnimation.apply(state.castDualAnimationState, state.ageInTicks);
    }
}
