package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.renderer.BarrowHoundRenderState;
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
 * Fauna 25 — Barrow Hound (Crypt Stalker)
 * Exact conversion of all 55 cubes, 22 bones, and 64x64 UVs from barrow_hound.bbmodel.
 */
public class BarrowHoundModel extends EntityModel<BarrowHoundRenderState> {
	public static final ModelLayerLocation MODEL_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_hound"), "main");
	public static final ModelLayerLocation LAYER_LOCATION = MODEL_LOCATION;

	private final ModelPart root;
	private final ModelPart body_root;
	private final ModelPart chest_group;
	private final ModelPart neck_group;
	private final ModelPart head_group;
	private final ModelPart jaw_group;
	private final ModelPart leg_fl;
	private final ModelPart shin_fl;
	private final ModelPart paw_fl;
	private final ModelPart leg_fr;
	private final ModelPart shin_fr;
	private final ModelPart paw_fr;
	private final ModelPart abdomen_group;
	private final ModelPart tail_1;
	private final ModelPart tail_2;
	private final ModelPart tail_3;
	private final ModelPart leg_bl;
	private final ModelPart shin_bl;
	private final ModelPart paw_bl;
	private final ModelPart leg_br;
	private final ModelPart shin_br;
	private final ModelPart paw_br;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation walkAnimation;
	private final KeyframeAnimation attackAnimation;
	private final KeyframeAnimation howlAnimation;

	public BarrowHoundModel(ModelPart modelRoot) {
		super(modelRoot);
		this.root = modelRoot.getChild("root");
		this.body_root = this.root.getChild("body_root");
		this.chest_group = this.body_root.getChild("chest_group");
		this.neck_group = this.chest_group.getChild("neck_group");
		this.head_group = this.neck_group.getChild("head_group");
		this.jaw_group = this.head_group.getChild("jaw_group");
		this.leg_fl = this.chest_group.getChild("leg_fl");
		this.shin_fl = this.leg_fl.getChild("shin_fl");
		this.paw_fl = this.shin_fl.getChild("paw_fl");
		this.leg_fr = this.chest_group.getChild("leg_fr");
		this.shin_fr = this.leg_fr.getChild("shin_fr");
		this.paw_fr = this.shin_fr.getChild("paw_fr");
		this.abdomen_group = this.body_root.getChild("abdomen_group");
		this.tail_1 = this.abdomen_group.getChild("tail_1");
		this.tail_2 = this.tail_1.getChild("tail_2");
		this.tail_3 = this.tail_2.getChild("tail_3");
		this.leg_bl = this.abdomen_group.getChild("leg_bl");
		this.shin_bl = this.leg_bl.getChild("shin_bl");
		this.paw_bl = this.shin_bl.getChild("paw_bl");
		this.leg_br = this.abdomen_group.getChild("leg_br");
		this.shin_br = this.leg_br.getChild("shin_br");
		this.paw_br = this.shin_br.getChild("paw_br");

		this.idleAnimation = BarrowHoundAnimations.IDLE.bake(modelRoot);
		this.walkAnimation = BarrowHoundAnimations.WALK.bake(modelRoot);
		this.attackAnimation = BarrowHoundAnimations.ATTACK.bake(modelRoot);
		this.howlAnimation = BarrowHoundAnimations.HOWL.bake(modelRoot);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body_root = root.addOrReplaceChild("body_root", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, 0.0F));

		PartDefinition chest_group = body_root.addOrReplaceChild("chest_group", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -9.0F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(25, 0).addBox(-3.0F, -1.6F, -10.3F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(47, 19).addBox(-5.3F, -3.5F, -8.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(47, 19).mirror().addBox(3.3F, -3.5F, -8.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(48, 14).addBox(-1.5F, -5.6F, -8.2F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(48, 14).addBox(-1.5F, -5.4F, -4.4F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 17).addBox(-4.2F, 1.4F, -7.2F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.03F))
				.texOffs(0, 17).addBox(-4.2F, 1.4F, -5.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.03F))
				.texOffs(0, 17).addBox(-4.2F, 1.2F, -2.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.03F))
				.texOffs(0, 17).mirror().addBox(3.2F, 1.4F, -7.2F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.03F)).mirror(false)
				.texOffs(0, 17).mirror().addBox(3.2F, 1.4F, -5.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.03F)).mirror(false)
				.texOffs(0, 17).mirror().addBox(3.2F, 1.2F, -2.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.03F)).mirror(false), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition neck_group = chest_group.addOrReplaceChild("neck_group", CubeListBuilder.create().texOffs(19, 25).addBox(-3.0F, -2.9F, -4.5F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(41, 31).addBox(-3.5F, -3.3F, -2.7F, 7.0F, 5.0F, 3.0F, new CubeDeformation(0.02F))
				.texOffs(48, 14).addBox(-1.5F, -4.7F, -3.7F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, -8.5F));

		PartDefinition head_group = neck_group.addOrReplaceChild("head_group", CubeListBuilder.create().texOffs(0, 17).addBox(-3.5F, -3.9F, -5.0F, 7.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(23, 46).addBox(-4.1F, -0.3F, -5.5F, 2.0F, 3.0F, 4.0F, new CubeDeformation(-0.05F))
				.texOffs(23, 46).mirror().addBox(2.1F, -0.3F, -5.5F, 2.0F, 3.0F, 4.0F, new CubeDeformation(-0.05F)).mirror(false)
				.texOffs(0, 29).addBox(-2.0F, -0.8F, -11.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(-0.04F))
				.texOffs(0, 48).addBox(-1.0F, -1.3F, -10.3F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.02F))
				.texOffs(9, 43).addBox(-2.0F, 2.0F, -10.5F, 4.0F, 1.0F, 5.0F, new CubeDeformation(-0.1F))
				.texOffs(19, 17).addBox(-2.0F, 1.7F, -10.7F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.04F))
				.texOffs(19, 17).mirror().addBox(1.0F, 1.7F, -10.7F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.04F)).mirror(false), PartPose.offset(0.0F, -1.0F, -4.0F));
		PartDefinition ear_l_r1 = head_group.addOrReplaceChild("ear_l_r1", CubeListBuilder.create().texOffs(49, 50).addBox(-1.0F, -3.7F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7F, -3.6F, -2.0F, 0.1745F, 0.0F, 0.1745F));
		PartDefinition ear_r_r1 = head_group.addOrReplaceChild("ear_r_r1", CubeListBuilder.create().texOffs(49, 50).mirror().addBox(-1.0F, -3.7F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.7F, -3.6F, -2.0F, 0.1745F, 0.0F, -0.1745F));

		PartDefinition jaw_group = head_group.addOrReplaceChild("jaw_group", CubeListBuilder.create().texOffs(14, 35).addBox(-1.5F, -0.1F, -6.0F, 3.0F, 2.0F, 6.0F, new CubeDeformation(-0.06F))
				.texOffs(19, 17).addBox(-1.6F, -1.6F, -5.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.08F))
				.texOffs(19, 17).mirror().addBox(0.6F, -1.6F, -5.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.08F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.0F, -4.7F, 0.0873F, 0.0F, 0.0F));

		PartDefinition leg_fl = chest_group.addOrReplaceChild("leg_fl", CubeListBuilder.create().texOffs(43, 39).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.4F, 1.5F, -6.0F));

		PartDefinition shin_fl = leg_fl.addOrReplaceChild("shin_fl", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition paw_fl = shin_fl.addOrReplaceChild("paw_fl", CubeListBuilder.create().texOffs(35, 48).addBox(-1.5F, -0.5F, -2.3F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(57, 14).addBox(0.4F, 0.5F, -3.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(57, 14).addBox(-1.4F, 0.5F, -3.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, -0.2F));

		PartDefinition leg_fr = chest_group.addOrReplaceChild("leg_fr", CubeListBuilder.create().texOffs(43, 39).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.4F, 1.5F, -6.0F));

		PartDefinition shin_fr = leg_fr.addOrReplaceChild("shin_fr", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition paw_fr = shin_fr.addOrReplaceChild("paw_fr", CubeListBuilder.create().texOffs(35, 48).mirror().addBox(-1.5F, -0.5F, -2.3F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(57, 14).mirror().addBox(-1.4F, 0.5F, -3.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(57, 14).mirror().addBox(0.4F, 0.5F, -3.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 4.0F, -0.2F));

		PartDefinition abdomen_group = body_root.addOrReplaceChild("abdomen_group", CubeListBuilder.create().texOffs(34, 0).addBox(-3.0F, -3.0F, -0.5F, 6.0F, 6.0F, 8.0F, new CubeDeformation(-0.02F))
				.texOffs(29, 14).addBox(-3.5F, -3.5F, 4.8F, 7.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(48, 14).addBox(-1.5F, -4.6F, 0.6F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(48, 14).addBox(-1.5F, -5.0F, 5.2F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition tail_1 = abdomen_group.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(27, 38).addBox(-1.5F, -1.5F, -0.3F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.8F, 9.5F, -0.4887F, 0.0F, 0.0F));

		PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(50, 43).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, -0.1745F, 0.0F, 0.0F));

		PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(14, 49).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.02F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.6F, 0.1745F, 0.0F, 0.0F));

		PartDefinition leg_bl = abdomen_group.addOrReplaceChild("leg_bl", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, -0.5F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.2F, 0.5F, 7.2F));

		PartDefinition shin_bl = leg_bl.addOrReplaceChild("shin_bl", CubeListBuilder.create().texOffs(54, 0).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, 0.6F));

		PartDefinition paw_bl = shin_bl.addOrReplaceChild("paw_bl", CubeListBuilder.create().texOffs(35, 48).addBox(-1.5F, -0.5F, -2.5F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(57, 14).addBox(0.4F, 0.5F, -3.8F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(57, 14).addBox(-1.4F, 0.5F, -3.8F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.5F, -0.4F));

		PartDefinition leg_br = abdomen_group.addOrReplaceChild("leg_br", CubeListBuilder.create().texOffs(0, 38).mirror().addBox(-1.5F, -0.5F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.2F, 0.5F, 7.2F));

		PartDefinition shin_br = leg_br.addOrReplaceChild("shin_br", CubeListBuilder.create().texOffs(54, 0).mirror().addBox(-1.0F, -0.5F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 5.5F, 0.6F));

		PartDefinition paw_br = shin_br.addOrReplaceChild("paw_br", CubeListBuilder.create().texOffs(35, 48).mirror().addBox(-1.5F, -0.5F, -2.5F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(57, 14).mirror().addBox(-1.4F, 0.5F, -3.8F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(57, 14).mirror().addBox(0.4F, 0.5F, -3.8F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 4.5F, -0.4F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(BarrowHoundRenderState state) {
		super.setupAnim(state);
		
		if (!state.howlAnimationState.isStarted()) {
			float clampedYaw = Mth.clamp(state.yRot, -40.0F, 40.0F) * Mth.DEG_TO_RAD;
			float clampedPitch = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
			this.neck_group.yRot += clampedYaw * 0.35F;
			this.head_group.yRot += clampedYaw * 0.65F;
			this.head_group.xRot += clampedPitch * 0.75F;
		}
		
		if (state.attackAnimationState.isStarted()) {
			this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
		} else {
			if (state.walkAnimationState.isStarted()) {
				this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks, 1.15F);
			} else {
				this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
			}
			if (state.howlAnimationState.isStarted()) {
				this.howlAnimation.apply(state.howlAnimationState, state.ageInTicks);
			}
		}
	}
}
