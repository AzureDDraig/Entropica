package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.renderer.BarrowCryptGlowOwlRenderState;
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
 * Fauna 9 — Barrow Crypt-Glow Owl (Silent Crypt Flyer)
 * Exact conversion of all 47 cubes, 16 bones, and 64x64 UVs from barrow_crypt_glow_owl.bbmodel.
 */
public class BarrowCryptGlowOwlModel extends EntityModel<BarrowCryptGlowOwlRenderState> {
	public static final ModelLayerLocation MODEL_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_crypt_glow_owl"), "main");
	public static final ModelLayerLocation LAYER_LOCATION = MODEL_LOCATION;

	private final ModelPart root;
	private final ModelPart body_root;
	private final ModelPart torso;
	private final ModelPart neck_pivot;
	private final ModelPart head_group;
	private final ModelPart tail_group;
	private final ModelPart wing_l;
	private final ModelPart wing_l_outer;
	private final ModelPart wing_r;
	private final ModelPart wing_r_outer;
	private final ModelPart leg_l;
	private final ModelPart leg_l_shin;
	private final ModelPart leg_l_foot;
	private final ModelPart leg_r;
	private final ModelPart leg_r_shin;
	private final ModelPart leg_r_foot;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation flyAnimation;
	private final KeyframeAnimation attackAnimation;

	public BarrowCryptGlowOwlModel(ModelPart modelRoot) {
		super(modelRoot);
		this.root = modelRoot.getChild("root");
		this.body_root = this.root.getChild("body_root");
		this.torso = this.body_root.getChild("torso");
		this.neck_pivot = this.torso.getChild("neck_pivot");
		this.head_group = this.neck_pivot.getChild("head_group");
		this.tail_group = this.torso.getChild("tail_group");
		this.wing_l = this.torso.getChild("wing_l");
		this.wing_l_outer = this.wing_l.getChild("wing_l_outer");
		this.wing_r = this.torso.getChild("wing_r");
		this.wing_r_outer = this.wing_r.getChild("wing_r_outer");
		this.leg_l = this.body_root.getChild("leg_l");
		this.leg_l_shin = this.leg_l.getChild("leg_l_shin");
		this.leg_l_foot = this.leg_l_shin.getChild("leg_l_foot");
		this.leg_r = this.body_root.getChild("leg_r");
		this.leg_r_shin = this.leg_r.getChild("leg_r_shin");
		this.leg_r_foot = this.leg_r_shin.getChild("leg_r_foot");

		this.idleAnimation = BarrowCryptGlowOwlAnimations.IDLE.bake(modelRoot);
		this.walkAnimation = BarrowCryptGlowOwlAnimations.WALK.bake(modelRoot);
		this.flyAnimation = BarrowCryptGlowOwlAnimations.FLY.bake(modelRoot);
		this.attackAnimation = BarrowCryptGlowOwlAnimations.ATTACK.bake(modelRoot);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, 0.0F));

		PartDefinition torso = body_root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.2F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(14, 26).addBox(-3.0F, -3.8F, -3.6F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.02F))
				.texOffs(50, 0).addBox(-3.0F, -0.8F, -3.4F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 21).addBox(-3.0F, -4.0F, 2.5F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition neck_pivot = torso.addOrReplaceChild("neck_pivot", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.1F, 0.0F, -0.0873F, 0.0F, 0.0F));

		PartDefinition head_group = neck_pivot.addOrReplaceChild("head_group", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0F, -6.9F, -3.0F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(27, 13).addBox(-4.0F, -7.4F, -4.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.02F))
				.texOffs(49, 29).addBox(-3.0F, -8.4F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.02F))
				.texOffs(15, 14).addBox(-2.0F, -9.4F, -4.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.02F))
				.texOffs(56, 10).addBox(-1.0F, -2.7F, -5.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.2F, 0.0F));

		PartDefinition tail_group = torso.addOrReplaceChild("tail_group", CubeListBuilder.create().texOffs(36, 26).addBox(-2.0F, -0.8F, -0.2F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.03F))
				.texOffs(45, 13).addBox(-1.0F, -0.4F, 0.0F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.8F, 2.6F, -0.8029F, 0.0F, 0.0F));
		PartDefinition tail_mid_l_r1 = tail_group.addOrReplaceChild("tail_mid_l_r1", CubeListBuilder.create().texOffs(14, 16).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(-1.8F, 0.0F, 0.0F, 0.0F, -0.1396F, 0.0F));
		PartDefinition tail_mid_r_r1 = tail_group.addOrReplaceChild("tail_mid_r_r1", CubeListBuilder.create().texOffs(14, 16).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(1.8F, 0.0F, 0.0F, 0.0F, 0.1396F, 0.0F));
		PartDefinition tail_outer_l_r1 = tail_group.addOrReplaceChild("tail_outer_l_r1", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(-0.04F)), PartPose.offsetAndRotation(-3.2F, -0.1F, 0.0F, 0.0F, -0.2793F, 0.0F));
		PartDefinition tail_outer_r_r1 = tail_group.addOrReplaceChild("tail_outer_r_r1", CubeListBuilder.create().texOffs(22, 0).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offsetAndRotation(3.2F, -0.1F, 0.0F, 0.0F, 0.2793F, 0.0F));

		PartDefinition wing_l = torso.addOrReplaceChild("wing_l", CubeListBuilder.create().texOffs(0, 14).addBox(-5.0F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(41, 21).addBox(-3.0F, -1.3F, -2.2F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.03F))
				.texOffs(14, 23).addBox(-5.5F, 0.2F, 2.3F, 4.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(-4.2F, -3.5F, -0.8F, 0.0F, 0.5585F, -1.4661F));

		PartDefinition wing_l_outer = wing_l.addOrReplaceChild("wing_l_outer", CubeListBuilder.create().texOffs(47, 26).addBox(-6.0F, -0.6F, -2.4F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 28).addBox(-5.0F, -0.5F, -0.4F, 5.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F))
				.texOffs(26, 28).addBox(-3.5F, -0.4F, 1.6F, 4.0F, 1.0F, 2.0F, new CubeDeformation(-0.04F)), PartPose.offset(-5.0F, 0.0F, 0.0F));

		PartDefinition wing_r = torso.addOrReplaceChild("wing_r", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(0.0F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(41, 21).mirror().addBox(0.0F, -1.3F, -2.2F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.03F)).mirror(false)
				.texOffs(14, 23).mirror().addBox(1.5F, 0.2F, 2.3F, 4.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false), PartPose.offsetAndRotation(4.2F, -3.5F, -0.8F, 0.0F, -0.5585F, 1.4661F));

		PartDefinition wing_r_outer = wing_r.addOrReplaceChild("wing_r_outer", CubeListBuilder.create().texOffs(47, 26).mirror().addBox(0.0F, -0.6F, -2.4F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 28).mirror().addBox(0.0F, -0.5F, -0.4F, 5.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false)
				.texOffs(26, 28).mirror().addBox(-0.5F, -0.4F, 1.6F, 4.0F, 1.0F, 2.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offset(5.0F, 0.0F, 0.0F));

		PartDefinition leg_l = body_root.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(27, 22).addBox(-1.5F, -0.4F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.2F, 0.2F, 0.0F));

		PartDefinition leg_l_shin = leg_l.addOrReplaceChild("leg_l_shin", CubeListBuilder.create().texOffs(56, 5).addBox(-1.0F, -0.3F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.3F, 0.0F));

		PartDefinition leg_l_foot = leg_l_shin.addOrReplaceChild("leg_l_foot", CubeListBuilder.create().texOffs(51, 21).addBox(-1.5F, -0.4F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(-0.02F))
				.texOffs(56, 14).addBox(0.6F, 0.0F, -3.6F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(56, 14).addBox(-0.55F, 0.0F, -3.9F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(56, 14).addBox(-1.7F, 0.0F, -3.6F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(22, 0).addBox(0.6F, 0.0F, -4.6F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(22, 0).addBox(-0.55F, 0.0F, -4.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(22, 0).addBox(-1.7F, 0.0F, -4.6F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.5F, 0.0F, 1.2F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.5F, 0.0F));

		PartDefinition leg_r = body_root.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(27, 22).mirror().addBox(-1.5F, -0.4F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.2F, 0.2F, 0.0F));

		PartDefinition leg_r_shin = leg_r.addOrReplaceChild("leg_r_shin", CubeListBuilder.create().texOffs(56, 5).mirror().addBox(-1.0F, -0.3F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 1.3F, 0.0F));

		PartDefinition leg_r_foot = leg_r_shin.addOrReplaceChild("leg_r_foot", CubeListBuilder.create().texOffs(51, 21).mirror().addBox(-1.5F, -0.4F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(-0.02F)).mirror(false)
				.texOffs(56, 14).mirror().addBox(-1.6F, 0.0F, -3.6F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(56, 14).mirror().addBox(-0.45F, 0.0F, -3.9F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(56, 14).mirror().addBox(0.7F, 0.0F, -3.6F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(22, 0).mirror().addBox(-1.6F, 0.0F, -4.6F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(22, 0).mirror().addBox(-0.45F, 0.0F, -4.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(22, 0).mirror().addBox(0.7F, 0.0F, -4.6F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 0).mirror().addBox(-0.5F, 0.0F, 1.2F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 2.5F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(BarrowCryptGlowOwlRenderState state) {
		super.setupAnim(state);
		
		float clampedYaw = Mth.clamp(state.yRot, -55.0F, 55.0F) * Mth.DEG_TO_RAD;
		float clampedPitch = Mth.clamp(state.xRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD;
		this.neck_pivot.yRot += clampedYaw * 0.3F;
		this.head_group.yRot += clampedYaw * 0.7F;
		this.head_group.xRot += clampedPitch * 0.75F;
		
		if (state.attackAnimationState.isStarted()) {
			this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
		} else if (state.isFlying) {
			this.flyAnimation.apply(state.flyAnimationState, state.ageInTicks);
		} else if (state.walkAnimationState.isStarted()) {
			this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
		} else {
			this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		}
	}
}
