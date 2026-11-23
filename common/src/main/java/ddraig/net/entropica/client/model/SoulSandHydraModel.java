package ddraig.net.entropica.client.model;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.animation.SoulSandHydraAnimations;
import ddraig.net.entropica.client.renderer.SoulSandHydraRenderState;
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
 * Fauna 50 — Soul-Sand Hydra (Multi-Headed Serpentine).
 * Exact conversion of all 86 cubes, 48 bone groups, and 128x128 UVs from soul_sand_hydra.bbmodel.
 */
public class SoulSandHydraModel extends EntityModel<SoulSandHydraRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "soul_sand_hydra"), "main");
    public static final ModelLayerLocation MODEL_LOCATION = LAYER_LOCATION;

    private final ModelPart body_grp;
    private final ModelPart head_c_grp;
    private final ModelPart head_l_grp;
    private final ModelPart head_r_grp;

    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation runAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation submergeAmbushAnimation;
    private final KeyframeAnimation witherRoarAnimation;
    private final KeyframeAnimation spellChargeAnimation;
    private final KeyframeAnimation spellReleaseAnimation;
    private final KeyframeAnimation breathAttackAnimation;
    private final KeyframeAnimation hurtAnimation;
    private final KeyframeAnimation deathAnimation;

    public SoulSandHydraModel(ModelPart modelRoot) {
        super(modelRoot);
        ModelPart root = modelRoot.getChild("root");
        this.body_grp = root.getChild("body_grp");
        ModelPart chestGrp = this.body_grp.getChild("chest_grp");
        this.head_c_grp = chestGrp.getChild("neck_1_c_grp").getChild("neck_2_c_grp").getChild("neck_3_c_grp").getChild("neck_4_c_grp").getChild("head_c_grp");
        this.head_l_grp = chestGrp.getChild("neck_1_l_grp").getChild("neck_2_l_grp").getChild("neck_3_l_grp").getChild("neck_4_l_grp").getChild("head_l_grp");
        this.head_r_grp = chestGrp.getChild("neck_1_r_grp").getChild("neck_2_r_grp").getChild("neck_3_r_grp").getChild("neck_4_r_grp").getChild("head_r_grp");

        this.idleAnimation = SoulSandHydraAnimations.IDLE.bake(modelRoot);
        this.walkAnimation = SoulSandHydraAnimations.WALK.bake(modelRoot);
        this.runAnimation = SoulSandHydraAnimations.RUN.bake(modelRoot);
        this.attackAnimation = SoulSandHydraAnimations.ATTACK.bake(modelRoot);
        this.submergeAmbushAnimation = SoulSandHydraAnimations.SUBMERGE_AMBUSH.bake(modelRoot);
        this.witherRoarAnimation = SoulSandHydraAnimations.WITHER_ROAR.bake(modelRoot);
        this.spellChargeAnimation = SoulSandHydraAnimations.SPELL_CHARGE.bake(modelRoot);
        this.spellReleaseAnimation = SoulSandHydraAnimations.SPELL_RELEASE.bake(modelRoot);
        this.breathAttackAnimation = SoulSandHydraAnimations.BREATH_ATTACK.bake(modelRoot);
        this.hurtAnimation = SoulSandHydraAnimations.HURT.bake(modelRoot);
        this.deathAnimation = SoulSandHydraAnimations.DEATH.bake(modelRoot);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body_grp = root.addOrReplaceChild("body_grp", CubeListBuilder.create(), PartPose.offset(0.0F, -17.0F, 0.0F));

        PartDefinition chest_grp = body_grp.addOrReplaceChild("chest_grp", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, -7.0F, -13.0F, 16.0F, 13.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(50, 26).addBox(-6.0F, 5.3F, -11.5F, 12.0F, 3.0F, 11.0F, new CubeDeformation(-0.05F))
                .texOffs(0, 0).addBox(-1.0F, -9.5F, -6.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.05F))
                .texOffs(0, 0).addBox(-1.0F, -9.5F, -2.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.05F)),
                PartPose.offsetAndRotation(0.0F, -0.5F, -4.5F, -0.1745F, 0.0F, 0.0F));

        PartDefinition neck_1_c_grp = chest_grp.addOrReplaceChild("neck_1_c_grp", CubeListBuilder.create().texOffs(40, 73).addBox(-2.5F, -7.2F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -6.1F, -9.9F, -0.0698F, 0.0F, 0.0F));
        PartDefinition neck_2_c_grp = neck_1_c_grp.addOrReplaceChild("neck_2_c_grp", CubeListBuilder.create()
                .texOffs(102, 59).addBox(-2.5F, -7.2F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(-0.15F))
                .texOffs(86, 26).addBox(-1.0F, -4.8F, 2.2F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.0F, -6.4F, 0.0F, -0.2094F, 0.0F, 0.0F));
        PartDefinition neck_3_c_grp = neck_2_c_grp.addOrReplaceChild("neck_3_c_grp", CubeListBuilder.create()
                .texOffs(0, 74).addBox(-2.5F, -7.2F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-0.18F))
                .texOffs(86, 26).addBox(-1.0F, -4.8F, 1.8F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.0F, -6.4F, 0.0F, 0.4887F, 0.0F, 0.0F));
        PartDefinition neck_4_c_grp = neck_3_c_grp.addOrReplaceChild("neck_4_c_grp", CubeListBuilder.create()
                .texOffs(105, 74).addBox(-2.5F, -6.2F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.22F))
                .texOffs(86, 26).addBox(-1.0F, -4.2F, 1.8F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.12F)),
                PartPose.offsetAndRotation(0.0F, -6.2F, 0.0F, 0.4538F, 0.0F, 0.0F));

        PartDefinition head_c_grp = neck_4_c_grp.addOrReplaceChild("head_c_grp", CubeListBuilder.create()
                .texOffs(93, 44).addBox(-3.5F, -3.5F, -5.5F, 7.0F, 6.0F, 8.0F, new CubeDeformation(-0.08F))
                .texOffs(56, 87).addBox(-2.5F, -1.8F, -12.0F, 5.0F, 3.0F, 7.0F, new CubeDeformation(-0.05F))
                .texOffs(29, 28).addBox(-2.5F, 1.0F, -11.6F, 5.0F, 1.0F, 6.0F, new CubeDeformation(-0.18F)),
                PartPose.offsetAndRotation(0.0F, -5.2F, -0.5F, -0.384F, 0.0F, 0.0F));
        head_c_grp.addOrReplaceChild("jaw_c_grp", CubeListBuilder.create().texOffs(0, 92).addBox(-2.5F, -0.5F, -6.8F, 5.0F, 2.0F, 7.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(0.0F, 1.6F, -5.0F, 0.0873F, 0.0F, 0.0F));
        head_c_grp.addOrReplaceChild("horn_c_r_grp", CubeListBuilder.create().texOffs(113, 21).mirror().addBox(-1.0F, -1.4F, -0.4F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.08F)).mirror(false), PartPose.offsetAndRotation(2.5F, -2.8F, 0.2F, 0.5934F, 0.2618F, 0.2094F));
        head_c_grp.addOrReplaceChild("horn_c_l_grp", CubeListBuilder.create().texOffs(113, 21).addBox(-1.0F, -1.4F, -0.4F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(-2.5F, -2.8F, 0.2F, 0.5934F, -0.2618F, -0.2094F));

        PartDefinition neck_1_l_grp = chest_grp.addOrReplaceChild("neck_1_l_grp", CubeListBuilder.create().texOffs(79, 59).addBox(-2.5F, -7.2F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-5.8F, -5.3F, -8.1F, -0.0349F, 0.384F, -0.3142F));
        PartDefinition neck_2_l_grp = neck_1_l_grp.addOrReplaceChild("neck_2_l_grp", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-2.5F, -7.2F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(-0.15F))
                .texOffs(86, 26).addBox(-1.0F, -4.8F, 2.2F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.0F, -6.4F, 0.0F, -0.2094F, 0.0873F, -0.0873F));
        PartDefinition neck_3_l_grp = neck_2_l_grp.addOrReplaceChild("neck_3_l_grp", CubeListBuilder.create()
                .texOffs(63, 73).addBox(-2.5F, -7.2F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-0.18F))
                .texOffs(86, 26).addBox(-1.0F, -4.8F, 1.8F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.0F, -6.4F, 0.0F, 0.4538F, 0.0349F, 0.1396F));
        PartDefinition neck_4_l_grp = neck_3_l_grp.addOrReplaceChild("neck_4_l_grp", CubeListBuilder.create()
                .texOffs(84, 74).addBox(-2.5F, -6.2F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.22F))
                .texOffs(86, 26).addBox(-1.0F, -4.2F, 1.8F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.12F)),
                PartPose.offsetAndRotation(0.0F, -6.2F, 0.0F, 0.4538F, 0.0F, 0.1396F));

        PartDefinition head_l_grp = neck_4_l_grp.addOrReplaceChild("head_l_grp", CubeListBuilder.create()
                .texOffs(62, 41).addBox(-3.5F, -3.5F, -5.5F, 7.0F, 6.0F, 8.0F, new CubeDeformation(-0.08F))
                .texOffs(14, 81).addBox(-2.5F, -1.8F, -12.0F, 5.0F, 3.0F, 7.0F, new CubeDeformation(-0.05F))
                .texOffs(29, 28).addBox(-2.5F, 1.0F, -11.6F, 5.0F, 1.0F, 6.0F, new CubeDeformation(-0.18F)),
                PartPose.offsetAndRotation(0.0F, -5.2F, -0.5F, -0.384F, 0.0F, 0.1222F));
        head_l_grp.addOrReplaceChild("jaw_l_grp", CubeListBuilder.create().texOffs(32, 91).addBox(-2.5F, -0.5F, -6.8F, 5.0F, 2.0F, 7.0F, new CubeDeformation(-0.12F)), PartPose.offsetAndRotation(0.0F, 1.6F, -5.0F, 0.0873F, 0.0F, 0.0F));
        head_l_grp.addOrReplaceChild("horn_l_r_grp", CubeListBuilder.create().texOffs(113, 21).mirror().addBox(-1.0F, -1.4F, -0.4F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.08F)).mirror(false), PartPose.offsetAndRotation(2.5F, -2.8F, 0.2F, 0.5934F, 0.2618F, 0.2094F));
        head_l_grp.addOrReplaceChild("horn_l_l_grp", CubeListBuilder.create().texOffs(113, 21).addBox(-1.0F, -1.4F, -0.4F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(-2.5F, -2.8F, 0.2F, 0.5934F, -0.2618F, -0.2094F));

        PartDefinition neck_1_r_grp = chest_grp.addOrReplaceChild("neck_1_r_grp", CubeListBuilder.create().texOffs(79, 59).mirror().addBox(-2.5F, -7.2F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(-0.1F)).mirror(false), PartPose.offsetAndRotation(5.8F, -5.3F, -8.1F, -0.0349F, -0.384F, 0.3142F));
        PartDefinition neck_2_r_grp = neck_1_r_grp.addOrReplaceChild("neck_2_r_grp", CubeListBuilder.create()
                .texOffs(0, 59).mirror().addBox(-2.5F, -7.2F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(-0.15F)).mirror(false)
                .texOffs(86, 26).mirror().addBox(-1.0F, -4.8F, 2.2F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.1F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, -6.4F, 0.0F, -0.2094F, -0.0873F, 0.0873F));
        PartDefinition neck_3_r_grp = neck_2_r_grp.addOrReplaceChild("neck_3_r_grp", CubeListBuilder.create()
                .texOffs(63, 73).mirror().addBox(-2.5F, -7.2F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(-0.18F)).mirror(false)
                .texOffs(86, 26).mirror().addBox(-1.0F, -4.8F, 1.8F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.1F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, -6.4F, 0.0F, 0.4538F, -0.0349F, -0.1396F));
        PartDefinition neck_4_r_grp = neck_3_r_grp.addOrReplaceChild("neck_4_r_grp", CubeListBuilder.create()
                .texOffs(84, 74).mirror().addBox(-2.5F, -6.2F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.22F)).mirror(false)
                .texOffs(86, 26).mirror().addBox(-1.0F, -4.2F, 1.8F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.12F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, -6.2F, 0.0F, 0.4538F, 0.0F, -0.1396F));

        PartDefinition head_r_grp = neck_4_r_grp.addOrReplaceChild("head_r_grp", CubeListBuilder.create()
                .texOffs(62, 41).mirror().addBox(-3.5F, -3.5F, -5.5F, 7.0F, 6.0F, 8.0F, new CubeDeformation(-0.08F)).mirror(false)
                .texOffs(14, 81).mirror().addBox(-2.5F, -1.8F, -12.0F, 5.0F, 3.0F, 7.0F, new CubeDeformation(-0.05F)).mirror(false)
                .texOffs(29, 28).mirror().addBox(-2.5F, 1.0F, -11.6F, 5.0F, 1.0F, 6.0F, new CubeDeformation(-0.18F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, -5.2F, -0.5F, -0.384F, 0.0F, -0.1222F));
        head_r_grp.addOrReplaceChild("jaw_r_grp", CubeListBuilder.create().texOffs(32, 91).mirror().addBox(-2.5F, -0.5F, -6.8F, 5.0F, 2.0F, 7.0F, new CubeDeformation(-0.12F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.6F, -5.0F, 0.0873F, 0.0F, 0.0F));
        head_r_grp.addOrReplaceChild("horn_r_r_grp", CubeListBuilder.create().texOffs(113, 21).mirror().addBox(-1.0F, -1.4F, -0.4F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.08F)).mirror(false), PartPose.offsetAndRotation(2.5F, -2.8F, 0.2F, 0.5934F, 0.2618F, 0.2094F));
        head_r_grp.addOrReplaceChild("horn_r_l_grp", CubeListBuilder.create().texOffs(113, 21).addBox(-1.0F, -1.4F, -0.4F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(-2.5F, -2.8F, 0.2F, 0.5934F, -0.2618F, -0.2094F));

        PartDefinition hips_grp = body_grp.addOrReplaceChild("hips_grp", CubeListBuilder.create()
                .texOffs(61, 0).addBox(-7.0F, -5.5F, -0.5F, 14.0F, 11.0F, 14.0F, new CubeDeformation(-0.05F))
                .texOffs(86, 30).addBox(-5.0F, 5.0F, 1.0F, 10.0F, 2.0F, 11.0F, new CubeDeformation(-0.05F))
                .texOffs(0, 0).addBox(-1.0F, -8.0F, 2.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.05F))
                .texOffs(0, 0).addBox(-1.0F, -8.0F, 7.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.05F)),
                PartPose.offsetAndRotation(0.0F, 0.5F, -3.5F, -0.0698F, 0.0F, 0.0F));

        PartDefinition tail_1_grp = hips_grp.addOrReplaceChild("tail_1_grp", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-5.0F, -4.0F, -0.6F, 10.0F, 8.0F, 8.0F, new CubeDeformation(-0.06F))
                .texOffs(0, 0).addBox(-1.0F, -6.6F, 1.3F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.08F)),
                PartPose.offsetAndRotation(0.0F, 0.7F, 12.7F, -0.2793F, 0.0F, 0.0F));
        PartDefinition tail_2_grp = tail_1_grp.addOrReplaceChild("tail_2_grp", CubeListBuilder.create()
                .texOffs(29, 41).addBox(-4.0F, -3.5F, -0.6F, 8.0F, 7.0F, 8.0F, new CubeDeformation(-0.06F))
                .texOffs(0, 0).addBox(-1.0F, -6.1F, 1.3F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.08F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 7.1F, -0.0698F, 0.0F, 0.0F));
        PartDefinition tail_3_grp = tail_2_grp.addOrReplaceChild("tail_3_grp", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-3.5F, -3.0F, -0.6F, 7.0F, 6.0F, 7.0F, new CubeDeformation(-0.06F))
                .texOffs(118, 12).addBox(-1.0F, -4.6F, 0.95F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.08F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 7.1F, 0.0873F, 0.0F, 0.0F));
        PartDefinition tail_4_grp = tail_3_grp.addOrReplaceChild("tail_4_grp", CubeListBuilder.create()
                .texOffs(47, 0).addBox(-3.0F, -2.5F, -0.6F, 6.0F, 5.0F, 7.0F, new CubeDeformation(-0.06F))
                .texOffs(118, 12).addBox(-1.0F, -4.1F, 0.95F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.08F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.1F, 0.1047F, 0.0F, 0.0F));
        PartDefinition tail_5_grp = tail_4_grp.addOrReplaceChild("tail_5_grp", CubeListBuilder.create()
                .texOffs(104, 0).addBox(-2.5F, -2.0F, -0.6F, 5.0F, 4.0F, 7.0F, new CubeDeformation(-0.06F))
                .texOffs(118, 12).addBox(-1.0F, -3.6F, 0.95F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.08F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.1F, 0.0873F, 0.0F, 0.0F));
        PartDefinition tail_6_grp = tail_5_grp.addOrReplaceChild("tail_6_grp", CubeListBuilder.create()
                .texOffs(75, 101).addBox(-2.0F, -1.5F, -0.6F, 4.0F, 3.0F, 6.0F, new CubeDeformation(-0.06F))
                .texOffs(118, 12).addBox(-1.0F, -3.1F, 0.6F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.08F))
                .texOffs(0, 7).addBox(-1.0F, -1.0F, 5.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.15F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.1F, 0.0698F, 0.0F, 0.0F));
        tail_6_grp.addOrReplaceChild("tail_barb_r_grp", CubeListBuilder.create().texOffs(46, 28).mirror().addBox(-0.5F, -0.5F, -0.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.05F)).mirror(false), PartPose.offsetAndRotation(2.0F, 0.0F, 3.2F, 0.0F, 0.4538F, 0.0F));
        tail_6_grp.addOrReplaceChild("tail_barb_l_grp", CubeListBuilder.create().texOffs(46, 28).addBox(-0.5F, -0.5F, -0.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 3.2F, 0.0F, -0.4538F, 0.0F));

        PartDefinition leg_fl_grp = body_grp.addOrReplaceChild("leg_fl_grp", CubeListBuilder.create().texOffs(23, 57).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(-7.5F, -0.307F, -12.0F, 0.2094F, 0.0F, 0.0F));
        PartDefinition foreleg_fl_grp = leg_fl_grp.addOrReplaceChild("foreleg_fl_grp", CubeListBuilder.create().texOffs(100, 87).addBox(-2.0F, -0.6F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 8.4F, 0.0F, -0.384F, 0.0F, 0.0F));
        foreleg_fl_grp.addOrReplaceChild("foot_fl_grp", CubeListBuilder.create()
                .texOffs(18, 101).addBox(-2.5F, 0.0F, -4.6F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(47, 0).addBox(1.2F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F))
                .texOffs(47, 0).addBox(-0.5F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F))
                .texOffs(47, 0).addBox(-2.2F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(0.0F, 7.2F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition leg_fr_grp = body_grp.addOrReplaceChild("leg_fr_grp", CubeListBuilder.create().texOffs(23, 57).mirror().addBox(-2.5F, -1.0F, -3.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(-0.08F)).mirror(false), PartPose.offsetAndRotation(7.5F, -0.307F, -12.0F, 0.2094F, 0.0F, 0.0F));
        PartDefinition foreleg_fr_grp = leg_fr_grp.addOrReplaceChild("foreleg_fr_grp", CubeListBuilder.create().texOffs(100, 87).mirror().addBox(-2.0F, -0.6F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(-0.08F)).mirror(false), PartPose.offsetAndRotation(0.0F, 8.4F, 0.0F, -0.384F, 0.0F, 0.0F));
        foreleg_fr_grp.addOrReplaceChild("foot_fr_grp", CubeListBuilder.create()
                .texOffs(18, 101).mirror().addBox(-2.5F, 0.0F, -4.6F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(47, 0).mirror().addBox(1.2F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(47, 0).mirror().addBox(-0.5F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(47, 0).mirror().addBox(-2.2F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 7.2F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition leg_bl_grp = body_grp.addOrReplaceChild("leg_bl_grp", CubeListBuilder.create().texOffs(56, 56).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(-6.8F, 0.637F, 5.2F, 0.3491F, 0.0F, 0.0F));
        PartDefinition shank_bl_grp = leg_bl_grp.addOrReplaceChild("shank_bl_grp", CubeListBuilder.create().texOffs(81, 87).addBox(-2.0F, -0.6F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(0.0F, 8.2F, 0.0F, -0.6632F, 0.0F, 0.0F));
        shank_bl_grp.addOrReplaceChild("foot_bl_grp", CubeListBuilder.create()
                .texOffs(50, 98).addBox(-2.5F, 0.0F, -4.6F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(47, 0).addBox(1.2F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F))
                .texOffs(47, 0).addBox(-0.5F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F))
                .texOffs(47, 0).addBox(-2.2F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.3142F, 0.0F, 0.0F));

        PartDefinition leg_br_grp = body_grp.addOrReplaceChild("leg_br_grp", CubeListBuilder.create().texOffs(56, 56).mirror().addBox(-2.5F, -1.0F, -3.0F, 5.0F, 10.0F, 6.0F, new CubeDeformation(-0.08F)).mirror(false), PartPose.offsetAndRotation(6.8F, 0.637F, 5.2F, 0.3491F, 0.0F, 0.0F));
        PartDefinition shank_br_grp = leg_br_grp.addOrReplaceChild("shank_br_grp", CubeListBuilder.create().texOffs(81, 87).mirror().addBox(-2.0F, -0.6F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(-0.08F)).mirror(false), PartPose.offsetAndRotation(0.0F, 8.2F, 0.0F, -0.6632F, 0.0F, 0.0F));
        shank_br_grp.addOrReplaceChild("foot_br_grp", CubeListBuilder.create()
                .texOffs(50, 98).mirror().addBox(-2.5F, 0.0F, -4.6F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(47, 0).mirror().addBox(1.2F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(47, 0).mirror().addBox(-0.5F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false)
                .texOffs(47, 0).mirror().addBox(-2.2F, 1.0F, -6.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.02F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.3142F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(SoulSandHydraRenderState state) {
        super.setupAnim(state);

        if (state.deathTime > 0.0F || state.deathAnimationState.isStarted()) {
            this.deathAnimation.apply(state.deathAnimationState, state.ageInTicks);
            return;
        }

        // Hold splayed-leg submerged ambush crouch sunk low into soft soul sand!
        if (state.isSubmergedAmbush) {
            net.minecraft.world.entity.AnimationState pinnedAmbush = new net.minecraft.world.entity.AnimationState();
            pinnedAmbush.start(0);
            this.submergeAmbushAnimation.apply(pinnedAmbush, 20.0F); // t = 1.0s crouched splay hold
            this.body_grp.y += 4.5F;
            return;
        }

        if (state.runAnimationState.isStarted()) {
            this.runAnimation.apply(state.runAnimationState, state.ageInTicks);
        } else if (state.walkAnimationState.isStarted()) {
            this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        } else {
            this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        }

        this.submergeAmbushAnimation.apply(state.submergeAmbushAnimationState, state.ageInTicks);
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
        this.witherRoarAnimation.apply(state.witherRoarAnimationState, state.ageInTicks);
        this.spellChargeAnimation.apply(state.spellChargeAnimationState, state.ageInTicks);
        this.spellReleaseAnimation.apply(state.spellReleaseAnimationState, state.ageInTicks);
        this.breathAttackAnimation.apply(state.breathAttackAnimationState, state.ageInTicks);
        this.hurtAnimation.apply(state.hurtAnimationState, state.ageInTicks);

        float headPitch = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD * 0.55F;
        float headYaw = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD * 0.55F;
        this.head_c_grp.xRot += headPitch;
        this.head_c_grp.yRot += headYaw;
        this.head_l_grp.xRot += headPitch * 0.85F;
        this.head_l_grp.yRot += headYaw * 0.85F;
        this.head_r_grp.xRot += headPitch * 0.85F;
        this.head_r_grp.yRot += headYaw * 0.85F;
    }
}
