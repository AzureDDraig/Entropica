package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GeyserWiggleWormModel extends EntityModel<GeyserWiggleWormRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "geyser_wiggle_worm"), "main");
	private final ModelPart root;
	private final ModelPart body_1;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart magma_ring;
	private final ModelPart body_2;
	private final ModelPart body_3;
	private final ModelPart body_4;
	private final ModelPart body_5;
	private final ModelPart body_6;
	private final ModelPart body_7;
	private final ModelPart body_8;
	private final ModelPart body_9;
	private final ModelPart body_10;
	private final ModelPart tail_fin_base;

	private final KeyframeAnimation idleAnimation;
	private final KeyframeAnimation slitherAnimation;
	private final KeyframeAnimation rearUpAnimation;
	private final KeyframeAnimation attackAnimation;
	private final KeyframeAnimation ventSteamAnimation;

	public GeyserWiggleWormModel(ModelPart root) {
		super(root);
		this.root = root.getChild("root");
		this.body_1 = this.root.getChild("body_1");
		this.head = this.body_1.getChild("head");
		this.jaw = this.head.getChild("jaw");
		this.magma_ring = this.head.getChild("magma_ring");
		this.body_2 = this.body_1.getChild("body_2");
		this.body_3 = this.body_2.getChild("body_3");
		this.body_4 = this.body_3.getChild("body_4");
		this.body_5 = this.body_4.getChild("body_5");
		this.body_6 = this.body_5.getChild("body_6");
		this.body_7 = this.body_6.getChild("body_7");
		this.body_8 = this.body_7.getChild("body_8");
		this.body_9 = this.body_8.getChild("body_9");
		this.body_10 = this.body_9.getChild("body_10");
		this.tail_fin_base = this.body_10.getChild("tail_fin_base");

		this.idleAnimation = GeyserWiggleWormAnimation.idle.bake(root);
		this.slitherAnimation = GeyserWiggleWormAnimation.slither.bake(root);
		this.rearUpAnimation = GeyserWiggleWormAnimation.rear_up.bake(root);
		this.attackAnimation = GeyserWiggleWormAnimation.attack.bake(root);
		this.ventSteamAnimation = GeyserWiggleWormAnimation.vent_steam.bake(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body_1 = root.addOrReplaceChild("body_1", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-5.0F, -5.0F, -3.0F, 10.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -5.0F, 0.0F));

		PartDefinition head = body_1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.8F, -4.8F, -8.0F, 9.6F, 8.4F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(38, 0).mirror().addBox(-4.8F, -4.8F, -9.0F, 9.6F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(38, 4).addBox(-2.2F, -2.2F, -9.6F, 4.4F, 3.8F, 1.6F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -3.0F));

		PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 18).addBox(-4.4F, -0.6F, -8.4F, 8.8F, 2.2F, 8.4F, new CubeDeformation(0.0F))
		.texOffs(36, 18).addBox(-3.2F, -0.6F, -7.8F, 6.4F, 1.0F, 7.3F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.8F, -1.0F));

		PartDefinition magma_ring = head.addOrReplaceChild("magma_ring", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -7.4F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -0.2F));

		PartDefinition magma_seg_15_r1 = magma_ring.addOrReplaceChild("magma_seg_15_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4492F, -5.9128F, 0.0F, 0.0F, 0.0F, -5.8905F));

		PartDefinition magma_seg_14_r1 = magma_ring.addOrReplaceChild("magma_seg_14_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5255F, -4.5255F, 0.0F, 0.0F, 0.0F, -5.4978F));

		PartDefinition magma_seg_13_r1 = magma_ring.addOrReplaceChild("magma_seg_13_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9128F, -2.4492F, 0.0F, 0.0F, 0.0F, -5.1051F));

		PartDefinition magma_seg_12_r1 = magma_ring.addOrReplaceChild("magma_seg_12_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4F, 0.0F, 0.0F, 0.0F, 0.0F, -4.7124F));

		PartDefinition magma_seg_11_r1 = magma_ring.addOrReplaceChild("magma_seg_11_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.9128F, 2.4492F, 0.0F, 0.0F, 0.0F, -4.3197F));

		PartDefinition magma_seg_10_r1 = magma_ring.addOrReplaceChild("magma_seg_10_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5255F, 4.5255F, 0.0F, 0.0F, 0.0F, -3.927F));

		PartDefinition magma_seg_9_r1 = magma_ring.addOrReplaceChild("magma_seg_9_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.4492F, 5.9128F, 0.0F, 0.0F, 0.0F, -3.5343F));

		PartDefinition magma_seg_8_r1 = magma_ring.addOrReplaceChild("magma_seg_8_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.4F, 0.0F, 0.0F, 0.0F, -3.1416F));

		PartDefinition magma_seg_7_r1 = magma_ring.addOrReplaceChild("magma_seg_7_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4492F, 5.9128F, 0.0F, 0.0F, 0.0F, -2.7489F));

		PartDefinition magma_seg_6_r1 = magma_ring.addOrReplaceChild("magma_seg_6_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5255F, 4.5255F, 0.0F, 0.0F, 0.0F, -2.3562F));

		PartDefinition magma_seg_5_r1 = magma_ring.addOrReplaceChild("magma_seg_5_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.9128F, 2.4492F, 0.0F, 0.0F, 0.0F, -1.9635F));

		PartDefinition magma_seg_4_r1 = magma_ring.addOrReplaceChild("magma_seg_4_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.4F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition magma_seg_3_r1 = magma_ring.addOrReplaceChild("magma_seg_3_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.9128F, -2.4492F, 0.0F, 0.0F, 0.0F, -1.1781F));

		PartDefinition magma_seg_2_r1 = magma_ring.addOrReplaceChild("magma_seg_2_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5255F, -4.5255F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition magma_seg_1_r1 = magma_ring.addOrReplaceChild("magma_seg_1_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-1.4F, -1.0F, -1.2F, 2.8F, 2.0F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.4492F, -5.9128F, 0.0F, 0.0F, 0.0F, -0.3927F));

		PartDefinition body_2 = body_1.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(34, 30).mirror().addBox(-4.9F, -4.9F, -0.4F, 9.8F, 9.8F, 5.6F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 3.0F));

		PartDefinition body_3 = body_2.addOrReplaceChild("body_3", CubeListBuilder.create().texOffs(68, 30).mirror().addBox(-4.8F, -4.8F, -0.4F, 9.6F, 9.6F, 5.6F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.2F));

		PartDefinition body_4 = body_3.addOrReplaceChild("body_4", CubeListBuilder.create().texOffs(0, 48).mirror().addBox(-4.6F, -4.6F, -0.4F, 9.2F, 9.2F, 5.6F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.2F));

		PartDefinition body_5 = body_4.addOrReplaceChild("body_5", CubeListBuilder.create().texOffs(32, 48).mirror().addBox(-4.4F, -4.4F, -0.4F, 8.8F, 8.8F, 5.6F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.2F));

		PartDefinition body_6 = body_5.addOrReplaceChild("body_6", CubeListBuilder.create().texOffs(64, 48).mirror().addBox(-4.2F, -4.2F, -0.4F, 8.4F, 8.4F, 5.6F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.2F));

		PartDefinition body_7 = body_6.addOrReplaceChild("body_7", CubeListBuilder.create().texOffs(0, 65).mirror().addBox(-4.0F, -4.0F, -0.4F, 8.0F, 8.0F, 5.6F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.2F));

		PartDefinition body_8 = body_7.addOrReplaceChild("body_8", CubeListBuilder.create().texOffs(30, 65).mirror().addBox(-3.75F, -3.75F, -0.4F, 7.5F, 7.5F, 5.6F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.2F));

		PartDefinition body_9 = body_8.addOrReplaceChild("body_9", CubeListBuilder.create().texOffs(60, 65).mirror().addBox(-3.5F, -3.5F, -0.4F, 7.0F, 7.0F, 5.6F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.2F));

		PartDefinition body_10 = body_9.addOrReplaceChild("body_10", CubeListBuilder.create().texOffs(88, 65).mirror().addBox(-3.2F, -3.2F, -0.4F, 6.4F, 6.4F, 5.6F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.2F));

		PartDefinition tail_fin_base = body_10.addOrReplaceChild("tail_fin_base", CubeListBuilder.create().texOffs(0, 82).addBox(-1.5F, -1.5F, -0.2F, 3.0F, 3.0F, 7.7F, new CubeDeformation(0.0F))
		.texOffs(24, 82).addBox(-0.4F, -4.8F, 3.5F, 0.8F, 3.3F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(24, 82).addBox(-0.4F, 1.5F, 3.5F, 0.8F, 3.3F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(42, 82).addBox(-4.8F, -0.4F, 3.5F, 3.3F, 0.8F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(42, 82).addBox(1.5F, -0.4F, 3.5F, 3.3F, 0.8F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 5.2F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(GeyserWiggleWormRenderState state) {
		super.setupAnim(state);

		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

		this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
		this.slitherAnimation.apply(state.slitherAnimationState, state.ageInTicks);
		this.rearUpAnimation.apply(state.rearUpAnimationState, state.ageInTicks);
		this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
		this.ventSteamAnimation.apply(state.ventSteamAnimationState, state.ageInTicks);
	}
}
