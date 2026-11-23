package ddraig.net.entropica.client.renderer.silica_glass_scarab;

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
 * Fauna 13 — Silica Glass-Scarab (Solar Scarab):
 * Sculpted 16-cube armored desert-glacier scarab with a broad clay-terracotta abdomen & thorax,
 * dark-slate head with a 3-cube wide forked golden beetle horn, 6 articulated slate legs
 * (legFL, legML, legBL, legFR, legMR, legBR), and 4 angled translucent solar-quartz concentrating
 * wing-panes (paneFL, paneFR, paneBL, paneBR) across its back.
 */
public class SilicaGlassScarabModel extends EntityModel<SilicaGlassScarabRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silica_glass_scarab"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart abdomen;
    private final ModelPart thorax;
    private final ModelPart head;
    private final ModelPart horn;
    private final ModelPart hornStem;
    private final ModelPart hornTineLeft;
    private final ModelPart hornTineRight;
    private final ModelPart paneFL;
    private final ModelPart paneFR;
    private final ModelPart paneBL;
    private final ModelPart paneBR;
    private final ModelPart legFL;
    private final ModelPart legML;
    private final ModelPart legBL;
    private final ModelPart legFR;
    private final ModelPart legMR;
    private final ModelPart legBR;

    public SilicaGlassScarabModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.body = this.root.getChild("body");
        this.abdomen = this.body.getChild("abdomen");
        this.thorax = this.body.getChild("thorax");
        this.head = this.body.getChild("head");
        this.horn = this.head.getChild("horn");
        this.hornStem = this.horn.getChild("horn_stem");
        this.hornTineLeft = this.horn.getChild("horn_tine_left");
        this.hornTineRight = this.horn.getChild("horn_tine_right");
        this.paneFL = this.body.getChild("pane_fl");
        this.paneFR = this.body.getChild("pane_fr");
        this.paneBL = this.body.getChild("pane_bl");
        this.paneBR = this.body.getChild("pane_br");
        this.legFL = this.root.getChild("leg_fl");
        this.legML = this.root.getChild("leg_ml");
        this.legBL = this.root.getChild("leg_bl");
        this.legFR = this.root.getChild("leg_fr");
        this.legMR = this.root.getChild("leg_mr");
        this.legBR = this.root.getChild("leg_br");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0.0F, -5.0F, 0.0F));

        // 1. Broad clay-terracotta rear abdomen carapace (9x5x8 -> UV [0,0..34,13))
        body.addOrReplaceChild("abdomen",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -2.5F, -1.0F, 9.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.5F));

        // 2. Terracotta & gold-trimmed pronotum thorax shield (8x4x5 -> UV [34,0..60,9))
        body.addOrReplaceChild("thorax",
                CubeListBuilder.create()
                        .texOffs(34, 0).addBox(-4.0F, -2.0F, -2.5F, 8.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.2F, -3.0F));

        // 3. Dark-slate beetle head with glowing compound eyes (5x3x4 -> UV [0,13..18,20))
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 13).addBox(-2.5F, -1.5F, -3.5F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.6F, -5.2F));

        // 4, 5, 6. Three-cube wide forked golden scarab horn
        PartDefinition horn = head.addOrReplaceChild("horn", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -0.4F, -3.0F, -0.36F, 0.0F, 0.0F));

        // 4. Central golden horn shaft (2x2x4 -> UV [18,13..30,19))
        horn.addOrReplaceChild("horn_stem",
                CubeListBuilder.create()
                        .texOffs(18, 13).addBox(-1.0F, -1.0F, -4.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // 5. Left forked golden prong (2x1x3 -> UV [30,13..40,17))
        horn.addOrReplaceChild("horn_tine_left",
                CubeListBuilder.create()
                        .texOffs(30, 13).addBox(-1.0F, -0.5F, -3.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -0.6F, -3.6F, -0.18F, 0.42F, 0.0F));

        // 6. Right forked golden prong (2x1x3 -> UV [40,13..50,17))
        horn.addOrReplaceChild("horn_tine_right",
                CubeListBuilder.create()
                        .texOffs(40, 13).addBox(-1.0F, -0.5F, -3.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -0.6F, -3.6F, -0.18F, -0.42F, 0.0F));

        // 7..10. Four angled translucent solar-quartz concentrating wing-panes across the back
        // 7. Front-left wing-pane (5x1x6 -> UV [0,32..22,39))
        body.addOrReplaceChild("pane_fl",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-5.0F, -0.5F, 0.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.2F, -2.5F, -3.2F, 0.14F, -0.08F, -0.12F));

        // 8. Front-right wing-pane (5x1x6 -> UV [22,32..44,39))
        body.addOrReplaceChild("pane_fr",
                CubeListBuilder.create()
                        .texOffs(22, 32).addBox(0.0F, -0.5F, 0.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.2F, -2.5F, -3.2F, 0.14F, 0.08F, 0.12F));

        // 9. Back-left concentrating wing-pane (4x1x7 -> UV [0,41..22,49))
        body.addOrReplaceChild("pane_bl",
                CubeListBuilder.create()
                        .texOffs(0, 41).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.3F, -2.9F, 0.4F, 0.20F, -0.12F, -0.08F));

        // 10. Back-right concentrating wing-pane (4x1x7 -> UV [22,41..44,49))
        body.addOrReplaceChild("pane_br",
                CubeListBuilder.create()
                        .texOffs(22, 41).addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3F, -2.9F, 0.4F, 0.20F, 0.12F, 0.08F));

        // 11..16. Six articulated slate-chitin legs (2x5x2 -> UV y=[22..29))
        root.addOrReplaceChild("leg_fl",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.4F, -4.4F, -3.4F, -0.22F, 0.18F, 0.46F));

        root.addOrReplaceChild("leg_ml",
                CubeListBuilder.create()
                        .texOffs(8, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.8F, -4.4F, 0.4F, 0.0F, 0.0F, 0.50F));

        root.addOrReplaceChild("leg_bl",
                CubeListBuilder.create()
                        .texOffs(16, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.6F, -4.4F, 4.4F, 0.26F, -0.20F, 0.46F));

        root.addOrReplaceChild("leg_fr",
                CubeListBuilder.create()
                        .texOffs(24, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.4F, -4.4F, -3.4F, -0.22F, -0.18F, -0.46F));

        root.addOrReplaceChild("leg_mr",
                CubeListBuilder.create()
                        .texOffs(32, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.8F, -4.4F, 0.4F, 0.0F, 0.0F, -0.50F));

        root.addOrReplaceChild("leg_br",
                CubeListBuilder.create()
                        .texOffs(40, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.6F, -4.4F, 4.4F, 0.26F, 0.20F, -0.46F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(SilicaGlassScarabRenderState state) {
        super.setupAnim(state);

        float yawRad = Mth.clamp(state.yRot, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
        float pitchRad = Mth.clamp(state.xRot, -20.0F, 20.0F) * Mth.DEG_TO_RAD;
        this.head.yRot += yawRad * 0.65F;
        this.head.xRot += pitchRad * 0.55F;

        // 6-leg alternating tripod crawl on walk:
        // Tripod A = (legFL, legMR, legBL), Tripod B = (legFR, legML, legBR)
        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;
        float phaseA = walkPos * 0.90F;
        float phaseB = phaseA + Mth.PI;

        float sweepA = Mth.cos(phaseA) * 0.65F * walkSpeed;
        float sweepB = Mth.cos(phaseB) * 0.65F * walkSpeed;
        float liftA = Math.max(0.0F, Mth.sin(phaseA)) * 0.36F * walkSpeed;
        float liftB = Math.max(0.0F, Mth.sin(phaseB)) * 0.36F * walkSpeed;

        this.legFL.xRot += sweepA;
        this.legFL.yRot += sweepA * 0.45F;
        this.legFL.zRot += liftA;

        this.legMR.xRot -= sweepA;
        this.legMR.yRot -= sweepA * 0.50F;
        this.legMR.zRot -= liftA;

        this.legBL.xRot += sweepA;
        this.legBL.yRot += sweepA * 0.45F;
        this.legBL.zRot += liftA;

        this.legFR.xRot -= sweepB;
        this.legFR.yRot -= sweepB * 0.45F;
        this.legFR.zRot -= liftB;

        this.legML.xRot += sweepB;
        this.legML.yRot += sweepB * 0.50F;
        this.legML.zRot += liftB;

        this.legBR.xRot -= sweepB;
        this.legBR.yRot -= sweepB * 0.45F;
        this.legBR.zRot -= liftB;

        // Subtle body scuttle & idle breathing + gentle horn tilt & wing-pane shimmer
        float age = state.ageInTicks;
        this.body.y += Mth.sin(age * 0.10F) * 0.15F - Math.abs(Mth.sin(phaseA)) * 0.25F * walkSpeed;
        this.body.zRot += Mth.cos(phaseA) * 0.045F * walkSpeed;

        this.horn.xRot += Mth.sin(age * 0.085F) * 0.065F;
        this.horn.zRot += Mth.cos(age * 0.065F) * 0.035F;

        float shimmerFront = Mth.sin(age * 0.14F) * 0.045F;
        float shimmerBack = Mth.sin(age * 0.14F - 0.85F) * 0.055F;
        this.paneFL.zRot += shimmerFront;
        this.paneFR.zRot -= shimmerFront;
        this.paneBL.zRot += shimmerBack;
        this.paneBR.zRot -= shimmerBack;
        this.paneFL.xRot += shimmerFront * 0.5F;
        this.paneFR.xRot += shimmerFront * 0.5F;
        this.paneBL.xRot += shimmerBack * 0.6F;
        this.paneBR.xRot += shimmerBack * 0.6F;

        // Solar Reflection Defense flare when solarFlashTicks > 0
        if (state.solarFlashTicks > 0) {
            float flash = Mth.clamp(state.solarFlashTicks / 18.0F, 0.0F, 1.0F);
            float flutter = Mth.sin(age * 1.9F) * 0.11F * flash;

            this.horn.xRot -= 0.28F * flash;
            this.body.xRot -= 0.08F * flash;

            // Sweep all 4 quartz wing-panes upward & outward into a parabolic solar dish
            this.paneFL.xRot += 0.46F * flash;
            this.paneFR.xRot += 0.46F * flash;
            this.paneFL.zRot += 0.44F * flash + flutter;
            this.paneFR.zRot -= 0.44F * flash + flutter;
            this.paneFL.yRot -= 0.18F * flash;
            this.paneFR.yRot += 0.18F * flash;

            this.paneBL.xRot += 0.58F * flash;
            this.paneBR.xRot += 0.58F * flash;
            this.paneBL.zRot += 0.38F * flash - flutter;
            this.paneBR.zRot -= 0.38F * flash - flutter;
            this.paneBL.yRot -= 0.24F * flash;
            this.paneBR.yRot += 0.24F * flash;
        }
    }
}
