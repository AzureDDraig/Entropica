package ddraig.net.entropica.client.renderer.mycelial_spore_weaver;

import ddraig.net.entropica.Entropica;
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
 * Fauna 23 — Mycelial Spore-Weaver:
 * Sculpted 15-cube frost-adapted fungal-bark quadruped with a gnarled mossy body, shoulder hump,
 * overhanging fungal brow visor, hanging mycelial whisker tendrils, 4 crooked root-bark legs
 * with mossy hooves, 2 side fungal shelf brackets, short mossy tail, and a 3-tiered golden-orange
 * glazeCap umbrella dome (capStem, capMain, capCrown) that hides cleanly when sheared.
 */
public class MycelialSporeWeaverModel extends EntityModel<MycelialSporeWeaverRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mycelial_spore_weaver"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart frontHump;
    private final ModelPart head;
    private final ModelPart browVisor;
    private final ModelPart whiskerTendrils;
    private final ModelPart shelfLeft;
    private final ModelPart shelfRight;
    private final ModelPart tail;
    private final ModelPart glazeCap;
    private final ModelPart capStem;
    private final ModelPart capMain;
    private final ModelPart capCrown;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legHindLeft;
    private final ModelPart legHindRight;

    public MycelialSporeWeaverModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.body = this.root.getChild("body");
        this.frontHump = this.body.getChild("front_hump");
        this.head = this.body.getChild("head");
        this.browVisor = this.head.getChild("brow_visor");
        this.whiskerTendrils = this.head.getChild("whisker_tendrils");
        this.shelfLeft = this.body.getChild("shelf_left");
        this.shelfRight = this.body.getChild("shelf_right");
        this.tail = this.body.getChild("tail");
        this.glazeCap = this.body.getChild("glaze_cap");
        this.capStem = this.glazeCap.getChild("cap_stem");
        this.capMain = this.glazeCap.getChild("cap_main");
        this.capCrown = this.glazeCap.getChild("cap_crown");
        this.legFrontLeft = this.root.getChild("leg_front_left");
        this.legFrontRight = this.root.getChild("leg_front_right");
        this.legHindLeft = this.root.getChild("leg_hind_left");
        this.legHindRight = this.root.getChild("leg_hind_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 1. Gnarled mossy fungal-bark body (8x6x11 -> UV [0,0..38,17))
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -3.0F, -5.5F, 8.0F, 6.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -10.0F, 0.0F));

        // 2. Mossy front withers hump (7x2x6 -> UV [38,0..64,8))
        body.addOrReplaceChild("front_hump",
                CubeListBuilder.create()
                        .texOffs(38, 0).addBox(-3.5F, -1.8F, -3.0F, 7.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.0F, -2.0F));

        // 3. Fungal-bark head with glowing eyes (6x5x5 -> UV [0,17..22,27))
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-3.0F, -2.5F, -4.5F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.2F, -5.2F));

        // 4. Overhanging fungal shelf brow visor (7x1x3 -> UV [38,8..58,12))
        head.addOrReplaceChild("brow_visor",
                CubeListBuilder.create()
                        .texOffs(38, 8).addBox(-3.5F, -0.5F, -2.2F, 7.0F, 1.0F, 3.0F, new CubeDeformation(0.02F)),
                PartPose.offsetAndRotation(0.0F, -2.2F, -3.2F, 0.14F, 0.0F, 0.0F));

        // 5. Hanging mycelial luminescence whisker tendrils (5x3x1 -> UV [38,12..50,16))
        head.addOrReplaceChild("whisker_tendrils",
                CubeListBuilder.create()
                        .texOffs(38, 12).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 2.3F, -3.8F, 0.08F, 0.0F, 0.0F));

        // 6 & 7. Side fungal shelf brackets (2x1x5 -> UV [38,18..52,24) and [22,24..36,30))
        body.addOrReplaceChild("shelf_left",
                CubeListBuilder.create()
                        .texOffs(38, 18).addBox(-1.8F, -0.5F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, -0.4F, 0.5F, 0.0F, 0.0F, -0.18F));

        body.addOrReplaceChild("shelf_right",
                CubeListBuilder.create()
                        .texOffs(22, 24).addBox(-0.2F, -0.5F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, -0.4F, 0.5F, 0.0F, 0.0F, 0.18F));

        // 8. Short mossy bark tail (2x2x4 -> UV [50,12..62,18))
        body.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(50, 12).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.4F, 5.3F, -0.32F, 0.0F, 0.0F));

        // 9, 10, 11. Multi-tiered golden-orange glazeCap umbrella dome (capStem, capMain, capCrown)
        PartDefinition glazeCap = body.addOrReplaceChild("glaze_cap", CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, 0.6F));

        // capStem (4x3x4 -> UV [22,17..38,24))
        glazeCap.addOrReplaceChild("cap_stem",
                CubeListBuilder.create()
                        .texOffs(22, 17).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.2F, 0.0F));

        // capMain (11x2x11 -> UV [0,30..44,43))
        glazeCap.addOrReplaceChild("cap_main",
                CubeListBuilder.create()
                        .texOffs(0, 30).addBox(-5.5F, -2.0F, -5.5F, 11.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -2.6F, 0.0F));

        // capCrown (7x2x7 -> UV [0,43..28,52))
        glazeCap.addOrReplaceChild("cap_crown",
                CubeListBuilder.create()
                        .texOffs(0, 43).addBox(-3.5F, -2.0F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -4.4F, 0.0F));

        // 12..15. Four crooked root-bark legs with mossy frost hooves (3x7x3 -> UV y=52..62)
        root.addOrReplaceChild("leg_front_left",
                CubeListBuilder.create()
                        .texOffs(0, 52).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.8F, -7.0F, -3.6F, 0.0F, 0.0F, 0.04F));

        root.addOrReplaceChild("leg_front_right",
                CubeListBuilder.create()
                        .texOffs(12, 52).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.8F, -7.0F, -3.6F, 0.0F, 0.0F, -0.04F));

        root.addOrReplaceChild("leg_hind_left",
                CubeListBuilder.create()
                        .texOffs(24, 52).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.8F, -7.0F, 3.6F, 0.0F, 0.0F, 0.05F));

        root.addOrReplaceChild("leg_hind_right",
                CubeListBuilder.create()
                        .texOffs(36, 52).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.8F, -7.0F, 3.6F, 0.0F, 0.0F, -0.05F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(MycelialSporeWeaverRenderState state) {
        super.setupAnim(state);

        this.glazeCap.visible = state.hasCap;

        float yawRad = Mth.clamp(state.yRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
        float pitchRad = Mth.clamp(state.xRot, -25.0F, 25.0F) * Mth.DEG_TO_RAD;
        this.head.yRot += yawRad;
        this.head.xRot += pitchRad;

        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;
        float legSwing = Mth.cos(walkPos * 0.6662F) * 1.25F * walkSpeed;
        float legSwingOpp = Mth.cos(walkPos * 0.6662F + (float) Math.PI) * 1.25F * walkSpeed;

        this.legFrontLeft.xRot += legSwing;
        this.legHindRight.xRot += legSwing;
        this.legFrontRight.xRot += legSwingOpp;
        this.legHindLeft.xRot += legSwingOpp;

        // Gentle idle breathing, mycelial whisker drift, and fungal cap pulse
        float age = state.ageInTicks;
        float breath = Mth.sin(age * 0.09F);
        this.body.y += breath * 0.22F - Math.abs(Mth.cos(walkPos * 0.6662F)) * 0.35F * walkSpeed;
        this.body.zRot += Mth.sin(walkPos * 0.6662F) * 0.04F * walkSpeed;
        this.head.xRot += Mth.cos(age * 0.07F) * 0.035F;
        this.whiskerTendrils.xRot += 0.06F + Mth.sin(age * 0.14F) * 0.09F + walkSpeed * 0.18F;
        this.tail.yRot += Mth.sin(age * 0.11F + walkPos * 0.5F) * (0.08F + 0.16F * walkSpeed);
        this.shelfLeft.zRot += Mth.sin(age * 0.09F) * 0.03F;
        this.shelfRight.zRot -= Mth.sin(age * 0.09F) * 0.03F;

        if (state.hasCap) {
            this.glazeCap.y += Mth.cos(age * 0.09F) * 0.14F;
            this.glazeCap.zRot += Mth.sin(walkPos * 0.6662F + 0.6F) * 0.05F * walkSpeed;
        }

        // Defensive Spore Puff shudder when puffTicks > 0
        if (state.puffTicks > 0) {
            float intensity = Mth.clamp(state.puffTicks / 20.0F, 0.0F, 1.0F);
            float shudder = Mth.sin((age + state.puffTicks) * 2.1F) * intensity;
            this.body.zRot += shudder * 0.12F;
            this.body.x += shudder * 0.45F;
            this.head.zRot -= shudder * 0.10F;
            if (state.hasCap) {
                this.glazeCap.zRot -= shudder * 0.20F;
                this.glazeCap.xRot += Mth.cos(age * 2.6F) * 0.14F * intensity;
                this.glazeCap.y -= 0.45F * intensity;
            }
        }
    }
}
