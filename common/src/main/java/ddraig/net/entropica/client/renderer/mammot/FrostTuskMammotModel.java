package ddraig.net.entropica.client.renderer.mammot;

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
import net.minecraft.util.Mth;

/**
 * Fauna 20 — Frost-Tusk Mammot (Glacial Excavator).
 * Exported from Blockbench 5.2.2 with optimized 64x64 shared T-junction UVs and 8 animations:
 * idle, walk, charge, laydown, lay, stand_up, attack, and death.
 */
public class FrostTuskMammotModel extends EntityModel<FrostTuskMammotRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_tusk_mammot"), "main"
    );

    private final ModelPart root;
    private final ModelPart leg_front_left;
    private final ModelPart leg_front_left_lower;
    private final ModelPart leg_front_right;
    private final ModelPart leg_front_right_lower;
    private final ModelPart leg_back_left;
    private final ModelPart leg_back_left_lower;
    private final ModelPart leg_back_right;
    private final ModelPart leg_back_right_lower;
    private final ModelPart body;
    private final ModelPart woolFringe;
    private final ModelPart tail;
    private final ModelPart tail_tip;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart ear_left;
    private final ModelPart ear_right;
    private final ModelPart trunk_1;
    private final ModelPart trunk_2;
    private final ModelPart trunk_3;
    private final ModelPart trunk_4;
    private final ModelPart trunk_tip;
    private final ModelPart tusk_left;
    private final ModelPart tusk_sheath_left;
    private final ModelPart tusk_l_1;
    private final ModelPart tusk_l_2;
    private final ModelPart tusk_l_3;
    private final ModelPart tusk_l_4;
    private final ModelPart tusk_l_5;
    private final ModelPart tusk_l_6;
    private final ModelPart tusk_right;
    private final ModelPart tusk_sheath_right;
    private final ModelPart tusk_r_1;
    private final ModelPart tusk_r_2;
    private final ModelPart tusk_r_3;
    private final ModelPart tusk_r_4;
    private final ModelPart tusk_r_5;
    private final ModelPart tusk_r_6;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation chargeAnimation;
    private final KeyframeAnimation laydownAnimation;
    private final KeyframeAnimation layAnimation;
    private final KeyframeAnimation standUpAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation deathAnimation;

    public FrostTuskMammotModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.leg_front_left = this.root.getChild("leg_front_left");
        this.leg_front_left_lower = this.leg_front_left.getChild("leg_front_left_lower");
        this.leg_front_right = this.root.getChild("leg_front_right");
        this.leg_front_right_lower = this.leg_front_right.getChild("leg_front_right_lower");
        this.leg_back_left = this.root.getChild("leg_back_left");
        this.leg_back_left_lower = this.leg_back_left.getChild("leg_back_left_lower");
        this.leg_back_right = this.root.getChild("leg_back_right");
        this.leg_back_right_lower = this.leg_back_right.getChild("leg_back_right_lower");
        this.body = this.root.getChild("body");
        this.woolFringe = this.body.getChild("wool_fringe");
        this.tail = this.body.getChild("tail");
        this.tail_tip = this.tail.getChild("tail_tip");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.ear_left = this.head.getChild("ear_left");
        this.ear_right = this.head.getChild("ear_right");
        this.trunk_1 = this.head.getChild("trunk_1");
        this.trunk_2 = this.trunk_1.getChild("trunk_2");
        this.trunk_3 = this.trunk_2.getChild("trunk_3");
        this.trunk_4 = this.trunk_3.getChild("trunk_4");
        this.trunk_tip = this.trunk_4.getChild("trunk_tip");
        this.tusk_left = this.head.getChild("tusk_left");
        this.tusk_sheath_left = this.tusk_left.getChild("tusk_sheath_left");
        this.tusk_l_1 = this.tusk_left.getChild("tusk_l_1");
        this.tusk_l_2 = this.tusk_left.getChild("tusk_l_2");
        this.tusk_l_3 = this.tusk_left.getChild("tusk_l_3");
        this.tusk_l_4 = this.tusk_left.getChild("tusk_l_4");
        this.tusk_l_5 = this.tusk_left.getChild("tusk_l_5");
        this.tusk_l_6 = this.tusk_left.getChild("tusk_l_6");
        this.tusk_right = this.head.getChild("tusk_right");
        this.tusk_sheath_right = this.tusk_right.getChild("tusk_sheath_right");
        this.tusk_r_1 = this.tusk_right.getChild("tusk_r_1");
        this.tusk_r_2 = this.tusk_right.getChild("tusk_r_2");
        this.tusk_r_3 = this.tusk_right.getChild("tusk_r_3");
        this.tusk_r_4 = this.tusk_right.getChild("tusk_r_4");
        this.tusk_r_5 = this.tusk_right.getChild("tusk_r_5");
        this.tusk_r_6 = this.tusk_right.getChild("tusk_r_6");

        this.idleAnimation = FrostTuskMammotAnimation.IDLE.bake(modelRoot);
        this.walkAnimation = FrostTuskMammotAnimation.WALK.bake(modelRoot);
        this.chargeAnimation = FrostTuskMammotAnimation.CHARGE.bake(modelRoot);
        this.laydownAnimation = FrostTuskMammotAnimation.LAYDOWN.bake(modelRoot);
        this.layAnimation = FrostTuskMammotAnimation.LAY.bake(modelRoot);
        this.standUpAnimation = FrostTuskMammotAnimation.STAND_UP.bake(modelRoot);
        this.attackAnimation = FrostTuskMammotAnimation.ATTACK.bake(modelRoot);
        this.deathAnimation = FrostTuskMammotAnimation.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition leg_front_left = root.addOrReplaceChild("leg_front_left", CubeListBuilder.create()
                .texOffs(40, 33).addBox(-3.0F, 0.5F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.15F)),
                PartPose.offset(-5.5F, -18.0F, -10.5F));

        leg_front_left.addOrReplaceChild("leg_front_left_lower", CubeListBuilder.create()
                .texOffs(40, 33).addBox(-3.0F, -0.7F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.05F))
                .texOffs(40, 48).addBox(-3.0F, 6.05F, -3.3F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.45F)),
                PartPose.offset(0.0F, 9.5F, 0.0F));

        PartDefinition leg_front_right = root.addOrReplaceChild("leg_front_right", CubeListBuilder.create()
                .texOffs(40, 33).mirror().addBox(-3.0F, 0.5F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.15F)).mirror(false),
                PartPose.offset(5.5F, -18.0F, -10.5F));

        leg_front_right.addOrReplaceChild("leg_front_right_lower", CubeListBuilder.create()
                .texOffs(40, 33).mirror().addBox(-3.0F, -0.7F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(40, 48).mirror().addBox(-3.0F, 6.05F, -3.3F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.45F)).mirror(false),
                PartPose.offset(0.0F, 9.5F, 0.0F));

        PartDefinition leg_back_left = root.addOrReplaceChild("leg_back_left", CubeListBuilder.create()
                .texOffs(40, 33).addBox(-3.0F, 0.5F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.15F)),
                PartPose.offset(-5.0F, -18.0F, 8.5F));

        leg_back_left.addOrReplaceChild("leg_back_left_lower", CubeListBuilder.create()
                .texOffs(40, 33).addBox(-3.0F, -0.7F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.05F))
                .texOffs(40, 48).addBox(-3.0F, 6.05F, -3.3F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.45F)),
                PartPose.offset(0.0F, 9.5F, 0.0F));

        PartDefinition leg_back_right = root.addOrReplaceChild("leg_back_right", CubeListBuilder.create()
                .texOffs(40, 33).mirror().addBox(-3.0F, 0.5F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.15F)).mirror(false),
                PartPose.offset(5.0F, -18.0F, 8.5F));

        leg_back_right.addOrReplaceChild("leg_back_right_lower", CubeListBuilder.create()
                .texOffs(40, 33).mirror().addBox(-3.0F, -0.7F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.05F)).mirror(false)
                .texOffs(40, 48).mirror().addBox(-3.0F, 6.05F, -3.3F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.45F)).mirror(false),
                PartPose.offset(0.0F, 9.5F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 0).addBox(-10.0F, -15.0F, -16.0F, 20.0F, 22.0F, 11.0F, new CubeDeformation(0.35F))
                .texOffs(7, 2).addBox(-8.0F, -17.0F, -15.0F, 16.0F, 2.0F, 9.0F, new CubeDeformation(0.2F))
                .texOffs(4, 1).addBox(-9.0F, -13.0F, -6.0F, 18.0F, 20.0F, 10.0F, new CubeDeformation(0.25F))
                .texOffs(7, 2).addBox(-8.0F, -10.0F, 4.0F, 16.0F, 17.0F, 9.0F, new CubeDeformation(0.15F))
                .texOffs(20, 2).addBox(-9.6F, -7.0F, 3.5F, 3.0F, 14.0F, 9.0F, new CubeDeformation(0.2F))
                .texOffs(20, 2).mirror().addBox(6.6F, -7.0F, 3.5F, 3.0F, 14.0F, 9.0F, new CubeDeformation(0.2F)).mirror(false),
                PartPose.offset(0.0F, -18.0F, 0.0F));

        body.addOrReplaceChild("wool_fringe", CubeListBuilder.create()
                .texOffs(42, 46).addBox(-10.25F, 6.8F, -16.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(42, 46).addBox(-9.2F, 6.8F, -5.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(42, 46).addBox(-9.75F, 6.8F, 2.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(42, 46).mirror().addBox(10.25F, 6.8F, -16.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(42, 46).mirror().addBox(9.2F, 6.8F, -5.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(42, 46).mirror().addBox(9.75F, 6.8F, 2.0F, 0.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(42, 57).addBox(-10.5F, 6.8F, -16.3F, 11.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(42, 57).mirror().addBox(-0.5F, 6.8F, -16.3F, 11.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(42, 57).addBox(-9.5F, 6.8F, 13.1F, 11.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(42, 57).mirror().addBox(-1.5F, 6.8F, 13.1F, 11.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.ZERO);

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(48, 37).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, 13.0F, 0.3142F, 0.0F, 0.0F));

        tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(20, 26).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 8.5F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(9, 4).addBox(-8.0F, -7.0F, -6.0F, 16.0F, 13.0F, 7.0F, new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, -10.0F, -15.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-6.0F, -5.0F, -6.0F, 12.0F, 12.0F, 8.0F, new CubeDeformation(0.1F))
                .texOffs(6, 39).addBox(-6.0F, -4.2F, -7.3F, 12.0F, 3.0F, 2.0F, new CubeDeformation(0.12F))
                .texOffs(10, 2).addBox(-6.5F, -9.0F, -6.8F, 13.0F, 5.0F, 9.0F, new CubeDeformation(0.25F))
                .texOffs(15, 4).addBox(-5.0F, -11.0F, -5.5F, 10.0F, 2.0F, 7.0F, new CubeDeformation(0.15F))
                .texOffs(2, 27).addBox(-3.0F, 6.5F, -3.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.0F, -4.0F));

        head.addOrReplaceChild("ear_left", CubeListBuilder.create()
                .texOffs(53, 0).addBox(-2.8F, -0.5F, -0.8F, 3.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-6.0F, -2.5F, 0.0F, 0.0F, 0.2793F, 0.1047F));

        head.addOrReplaceChild("ear_right", CubeListBuilder.create()
                .texOffs(53, 0).mirror().addBox(-0.2F, -0.5F, -0.8F, 3.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(6.0F, -2.5F, 0.0F, 0.0F, -0.2793F, -0.1047F));

        PartDefinition trunk_1 = head.addOrReplaceChild("trunk_1", CubeListBuilder.create()
                .texOffs(0, 53).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(0.0F, 0.5F, -5.2F, -0.2443F, 0.0F, 0.0F));

        PartDefinition trunk_2 = trunk_1.addOrReplaceChild("trunk_2", CubeListBuilder.create()
                .texOffs(2, 54).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.02F)),
                PartPose.offsetAndRotation(0.0F, 4.1F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition trunk_3 = trunk_2.addOrReplaceChild("trunk_3", CubeListBuilder.create()
                .texOffs(4, 55).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.02F)),
                PartPose.offsetAndRotation(0.0F, 4.1F, 0.0F, 0.3142F, 0.0F, 0.0F));

        PartDefinition trunk_4 = trunk_3.addOrReplaceChild("trunk_4", CubeListBuilder.create()
                .texOffs(4, 55).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.15F)),
                PartPose.offsetAndRotation(0.0F, 4.1F, 0.0F, 0.5585F, 0.0F, 0.0F));

        trunk_4.addOrReplaceChild("trunk_tip", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 3.9F, 0.0F, 0.6632F, 0.0F, 0.0F));

        PartDefinition tusk_left = head.addOrReplaceChild("tusk_left", CubeListBuilder.create(),
                PartPose.offset(-5.6F, 4.6F, -3.4F));

        tusk_left.addOrReplaceChild("tusk_sheath_left", CubeListBuilder.create()
                .texOffs(23, 6).addBox(-2.0F, -2.5F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.3F)),
                PartPose.offsetAndRotation(-0.4F, 0.3F, 0.05F, 0.9812F, 0.5157F, 0.296F));

        tusk_left.addOrReplaceChild("tusk_l_1", CubeListBuilder.create()
                .texOffs(24, 55).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.32F)),
                PartPose.offsetAndRotation(-1.65F, 2.1F, -1.3F, 1.0559F, 0.4758F, 0.4124F));

        tusk_left.addOrReplaceChild("tusk_l_2", CubeListBuilder.create()
                .texOffs(24, 55).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.22F)),
                PartPose.offsetAndRotation(-5.05F, 6.0F, -4.3F, 0.8878F, 0.477F, 0.4138F));

        tusk_left.addOrReplaceChild("tusk_l_3", CubeListBuilder.create()
                .texOffs(24, 55).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(-7.8F, 7.9F, -8.4F, 0.1885F, 0.351F, 0.4067F));

        tusk_left.addOrReplaceChild("tusk_l_4", CubeListBuilder.create()
                .texOffs(24, 55).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(-0.05F)),
                PartPose.offsetAndRotation(-8.3F, 5.8F, -12.2F, -0.9833F, 0.3138F, 0.4262F));

        tusk_left.addOrReplaceChild("tusk_l_5", CubeListBuilder.create()
                .texOffs(25, 55).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.22F)),
                PartPose.offsetAndRotation(-6.55F, 1.5F, -13.1F, -1.7809F, 0.2805F, 0.4779F));

        tusk_left.addOrReplaceChild("tusk_l_6", CubeListBuilder.create()
                .texOffs(25, 55).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(-3.9F, -2.35F, -11.4F, -2.0839F, 0.2786F, 0.521F));

        PartDefinition tusk_right = head.addOrReplaceChild("tusk_right", CubeListBuilder.create(),
                PartPose.offset(5.6F, 4.6F, -3.4F));

        tusk_right.addOrReplaceChild("tusk_sheath_right", CubeListBuilder.create()
                .texOffs(23, 6).mirror().addBox(-2.0F, -2.5F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.3F)).mirror(false),
                PartPose.offsetAndRotation(0.4F, 0.3F, 0.05F, 0.9812F, -0.5157F, -0.296F));

        tusk_right.addOrReplaceChild("tusk_r_1", CubeListBuilder.create()
                .texOffs(24, 55).mirror().addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.32F)).mirror(false),
                PartPose.offsetAndRotation(1.65F, 2.1F, -1.3F, 1.0559F, -0.4758F, -0.4124F));

        tusk_right.addOrReplaceChild("tusk_r_2", CubeListBuilder.create()
                .texOffs(24, 55).mirror().addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.22F)).mirror(false),
                PartPose.offsetAndRotation(5.05F, 6.0F, -4.3F, 0.8878F, -0.477F, -0.4138F));

        tusk_right.addOrReplaceChild("tusk_r_3", CubeListBuilder.create()
                .texOffs(24, 55).mirror().addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.1F)).mirror(false),
                PartPose.offsetAndRotation(7.8F, 7.9F, -8.4F, 0.1885F, -0.351F, -0.4067F));

        tusk_right.addOrReplaceChild("tusk_r_4", CubeListBuilder.create()
                .texOffs(24, 55).mirror().addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(-0.05F)).mirror(false),
                PartPose.offsetAndRotation(8.3F, 5.8F, -12.2F, -0.9833F, -0.3138F, -0.4262F));

        tusk_right.addOrReplaceChild("tusk_r_5", CubeListBuilder.create()
                .texOffs(25, 55).mirror().addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.22F)).mirror(false),
                PartPose.offsetAndRotation(6.55F, 1.5F, -13.1F, -1.7809F, -0.2805F, -0.4779F));

        tusk_right.addOrReplaceChild("tusk_r_6", CubeListBuilder.create()
                .texOffs(25, 55).mirror().addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(-0.02F)).mirror(false),
                PartPose.offsetAndRotation(3.9F, -2.35F, -11.4F, -2.0839F, -0.2786F, -0.521F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(FrostTuskMammotRenderState state) {
        super.setupAnim(state);
        this.woolFringe.visible = !state.isSheared;

        if (state.deathAnimationState.isStarted() || state.deathTime > 0.0F) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        if (state.laydownAnimationState.isStarted()) {
            this.laydownAnimation.apply(state.laydownAnimationState, state.ageInTicks);
            return;
        }
        if (state.layAnimationState.isStarted()) {
            this.layAnimation.apply(state.layAnimationState, state.ageInTicks);
            return;
        }
        if (state.standUpAnimationState.isStarted()) {
            this.standUpAnimation.apply(state.standUpAnimationState, state.ageInTicks);
            return;
        }

        if (state.chargeAnimationState.isStarted()) {
            this.chargeAnimation.apply(state.chargeAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);

        if (!state.attackAnimationState.isStarted() && !state.chargeAnimationState.isStarted()) {
            float clampedYaw = Mth.clamp(state.yRot, -30.0F, 30.0F) * Mth.DEG_TO_RAD;
            float clampedPitch = Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD;
            this.head.yRot += clampedYaw * 0.35F;
            this.head.xRot += clampedPitch * 0.28F;
        }
    }
}
