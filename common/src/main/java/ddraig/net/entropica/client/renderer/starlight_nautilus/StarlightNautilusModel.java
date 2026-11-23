package ddraig.net.entropica.client.renderer.starlight_nautilus;

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
 * Mojang 128x128 base NautilusModel geometry + dedicated 128x128 baby mesh + Saddle & Mount Armor layers
 * for the Flying Starlight Nautilus (entropica:starlight_nautilus).
 * In this layout, "shell" is at -Z (forward jet-propulsion direction) and "body"/tentacles pulse behind at +Z.
 */
public class StarlightNautilusModel extends EntityModel<StarlightNautilusRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus"), "main"
    );
    public static final ModelLayerLocation BABY_LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus"), "baby"
    );
    public static final ModelLayerLocation SADDLE_LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus"), "saddle"
    );
    public static final ModelLayerLocation ARMOR_LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus"), "armor"
    );

    private final ModelPart root;
    private final ModelPart shell;
    private final ModelPart body;
    private final ModelPart upperMouth;
    private final ModelPart innerMouth;
    private final ModelPart lowerMouth;

    public StarlightNautilusModel(ModelPart modelRoot) {
        super(modelRoot);
        this.root = modelRoot.getChild("root");
        this.shell = this.root.getChild("shell");
        this.body = this.root.getChild("body");
        this.upperMouth = this.body.getChild("upper_mouth");
        this.innerMouth = this.body.getChild("inner_mouth");
        this.lowerMouth = this.body.getChild("lower_mouth");
    }

    public static MeshDefinition createBodyMesh(CubeDeformation def) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        PartDefinition root = partRoot.addOrReplaceChild(
                "root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 29.0F, -6.0F)
        );

        root.addOrReplaceChild(
                "shell",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -10.0F, -7.0F, 14.0F, 10.0F, 16.0F, def)
                        .texOffs(0, 26).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 8.0F, 20.0F, def)
                        .texOffs(48, 26).addBox(-7.0F, 0.0F, 6.0F, 14.0F, 8.0F, 0.0F, def),
                PartPose.offset(0.0F, -13.0F, 5.0F)
        );

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 54).addBox(-5.0F, -4.51F, -3.0F, 10.0F, 8.0F, 8.0F, def),
                PartPose.offset(0.0F, -8.5F, 12.3F)
        );

        body.addOrReplaceChild(
                "upper_mouth",
                CubeListBuilder.create()
                        .texOffs(54, 54).addBox(-5.0F, -2.0F, 0.0F, 10.0F, 4.0F, 4.0F, def.extend(-0.001F)),
                PartPose.offset(0.0F, -2.51F, 7.0F)
        );

        body.addOrReplaceChild(
                "inner_mouth",
                CubeListBuilder.create()
                        .texOffs(54, 70).addBox(-3.0F, -2.0F, -0.5F, 6.0F, 4.0F, 4.0F, def),
                PartPose.offset(0.0F, -0.51F, 7.5F)
        );

        body.addOrReplaceChild(
                "lower_mouth",
                CubeListBuilder.create()
                        .texOffs(54, 62).addBox(-5.0F, -1.98F, 0.0F, 10.0F, 4.0F, 4.0F, def.extend(-0.001F)),
                PartPose.offset(0.0F, 1.49F, 7.0F)
        );

        return mesh;
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(createBodyMesh(CubeDeformation.NONE), 128, 128);
    }

    public static LayerDefinition createBabyBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        PartDefinition root = partRoot.addOrReplaceChild(
                "root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 29.0F, -3.5F)
        );

        root.addOrReplaceChild(
                "shell",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -6.0F, -4.5F, 8.0F, 6.0F, 9.0F, CubeDeformation.NONE)
                        .texOffs(0, 15).addBox(-4.0F, 0.0F, -4.5F, 8.0F, 5.0F, 11.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -10.0F, 3.0F)
        );

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-3.0F, -2.51F, -2.0F, 6.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -7.5F, 7.0F)
        );

        body.addOrReplaceChild(
                "upper_mouth",
                CubeListBuilder.create()
                        .texOffs(24, 32).addBox(-3.0F, -1.0F, 0.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(-0.001F)),
                PartPose.offset(0.0F, -1.51F, 3.0F)
        );

        body.addOrReplaceChild(
                "inner_mouth",
                CubeListBuilder.create()
                        .texOffs(24, 44).addBox(-2.0F, -1.0F, -0.2F, 4.0F, 2.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -0.51F, 3.2F)
        );

        body.addOrReplaceChild(
                "lower_mouth",
                CubeListBuilder.create()
                        .texOffs(24, 38).addBox(-3.0F, -0.98F, 0.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(-0.001F)),
                PartPose.offset(0.0F, 0.49F, 3.0F)
        );

        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition createSaddleLayer() {
        return LayerDefinition.create(createBodyMesh(new CubeDeformation(0.2F)), 128, 128);
    }

    public static LayerDefinition createArmorLayer() {
        return LayerDefinition.create(createBodyMesh(new CubeDeformation(0.45F)), 128, 128);
    }

    @Override
    public void setupAnim(StarlightNautilusRenderState state) {
        super.setupAnim(state);

        float age = state.ageInTicks;
        float moveFactor = Mth.clamp(state.walkAnimationSpeed * 1.65F, 0.0F, 1.0F);
        float dash = Mth.clamp(state.dashProgress, 0.0F, 1.0F);

        // Gentle buoyant hover bob + flight pitch alignment
        float bobAmp = state.isRidden ? 0.25F : (state.isSitting ? 0.35F : 0.70F);
        this.root.y += Mth.sin(age * 0.09F) * bobAmp;

        float flightPitch = Mth.clamp(state.xRot, -45.0F, 45.0F) * Mth.DEG_TO_RAD;
        this.root.xRot += flightPitch * (state.isRidden ? 0.65F : 0.35F);

        // Rhythmic cephalopod jet-propulsion cycle (faster while gliding/swimming, rapid during Star-Jet Dash)
        float jetPhase = age * Mth.lerp(moveFactor, 0.12F, 0.28F) + dash * age * 0.42F;
        float jetPulse = 0.5F + 0.5F * Mth.sin(jetPhase); // 0.0 = contracted jet, 1.0 = expanded intake

        // Shell pitches gently with each jet stroke while body counter-pulses along Z
        float shellRock = Mth.sin(jetPhase - 0.5F) * Mth.lerp(moveFactor, 0.035F, 0.085F) * (state.isRidden ? 0.45F : 1.0F);
        this.shell.xRot += shellRock - dash * 0.06F;
        this.shell.zRot += Mth.cos(age * 0.07F) * 0.025F * (state.isRidden ? 0.35F : 1.0F);

        float bodyPumpZ = (jetPulse - 0.5F) * Mth.lerp(moveFactor, 0.35F, 0.85F) - dash * 1.15F;
        this.body.z += bodyPumpZ;
        this.body.xRot += -shellRock * 0.65F;

        // Tentacle hood ("upper_mouth" & "lower_mouth" extend toward +Z):
        // Positive xRot on upper_mouth lifts it UP (opening); negative xRot on lower_mouth lowers it DOWN (opening).
        // During Star-Jet Dash (dash > 0), tentacles contract rapidly together into a tight propulsion nozzle!
        float baseOpen = Mth.lerp(moveFactor, 0.08F, 0.22F) * jetPulse;
        float rapidDashFlutter = dash > 0.0F ? (Mth.sin(age * 1.25F) * 0.045F * dash) : 0.0F;
        float nozzleAngle = Mth.lerp(dash, baseOpen, -0.10F) + rapidDashFlutter;

        this.upperMouth.xRot += nozzleAngle;
        this.lowerMouth.xRot -= nozzleAngle;
        this.innerMouth.z += (jetPulse - 0.5F) * 0.25F * (1.0F - dash * 0.7F);
    }
}
