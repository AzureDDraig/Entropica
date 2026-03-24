package ddraig.net.entropica.client.model;

import ddraig.net.entropica.client.renderer.GrotRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GrotModel extends EntityModel<GrotRenderState> {
    private final ModelPart innerBody; // The core + eyes + mouth
    private final ModelPart outerBody; // The outer jelly

    public GrotModel(ModelPart root) {
        super(root);
        this.innerBody = root.getChild("inner_body");
        this.outerBody = root.getChild("outer_body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 1. Exact Vanilla Slime Inner Body (Core, Eyes, and Mouth)
        PartDefinition inner = partdefinition.addOrReplaceChild("inner_body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
        inner.addOrReplaceChild("right_eye", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        inner.addOrReplaceChild("left_eye", CubeListBuilder.create()
                .texOffs(32, 4).addBox(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        inner.addOrReplaceChild("mouth", CubeListBuilder.create()
                .texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);

        // 2. Exact Vanilla Slime Outer Jelly
        partdefinition.addOrReplaceChild("outer_body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);

        // Vanilla Slime texture map size is 64x32
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(GrotRenderState state) {
        super.setupAnim(state);
        // Hide BOTH from the base renderer. We will draw them manually in the Layer!
        this.innerBody.visible = false;
        this.outerBody.visible = false;
    }

    public ModelPart getInnerBody() { return this.innerBody; }
    public ModelPart getOuterBody() { return this.outerBody; }
}