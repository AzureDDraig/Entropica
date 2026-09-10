package ddraig.net.entropica.client.renderer.amber_weeping_stag;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class AmberWeepingStagModel extends EntityModel<AmberWeepingStagRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_weeping_stag"), "main");

    private final ModelPart root;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation attackAnimation;

    public AmberWeepingStagModel(ModelPart root) {
        super(root);
        this.root = root.getChild("root");
        this.walkAnimation = AmberWeepingStagAnimation.WALK.bake(root);
        this.idleAnimation = AmberWeepingStagAnimation.IDLE.bake(root);
        this.attackAnimation = AmberWeepingStagAnimation.ATTACK.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.0F, -8.0F, 8.0F, 9.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(-3.5F, -4.5F, 2.0F, 7.0F, 7.5F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -18.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -3.0F, -6.0F, -0.2618F, 0.0F, 0.0F));
        neck.addOrReplaceChild("neck_mesh_r1", CubeListBuilder.create().texOffs(38, 0).addBox(-2.5F, -5.0F, -2.0F, 5.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -0.5F, 0.6109F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(38, 16).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(38, 29).addBox(-2.0F, -2.5F, -7.0F, 4.0F, 3.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, -4.0F, 0.1745F, 0.0F, 0.0F));

        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(38, 39).addBox(-1.5F, 0.0F, -5.5F, 3.0F, 1.5F, 3.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -1.0F));

        PartDefinition ear_left = head.addOrReplaceChild("ear_left", CubeListBuilder.create(), PartPose.offsetAndRotation(2.5F, -3.0F, 1.0F, -0.1745F, 0.4363F, -0.2618F));
        ear_left.addOrReplaceChild("ear_left_mesh_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-1.4798F, -1.064F, -1.375F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.6276F, -0.6532F, -0.8735F));

        PartDefinition ear_right = head.addOrReplaceChild("ear_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.5F, -3.0F, 1.0F, -0.1745F, -0.4363F, 0.2618F));
        ear_right.addOrReplaceChild("ear_right_mesh_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-1.5202F, -1.064F, -1.375F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.6276F, 0.6532F, 0.8735F));

        PartDefinition antler_left = head.addOrReplaceChild("antler_left", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0F, -3.5237F, 1.2926F, -0.3927F, 0.2618F, -0.1745F));
        antler_left.addOrReplaceChild("antler_l_crown_back_r1", CubeListBuilder.create().texOffs(36, 46).addBox(0.9339F, -7.1755F, -1.0293F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2751F, -9.6064F, 3.0383F, 0.0F, 0.0F, -0.48F));
        antler_left.addOrReplaceChild("antler_l_crown_back_r2", CubeListBuilder.create().texOffs(36, 46).addBox(-0.0761F, -4.4531F, -1.034F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2751F, -9.6064F, 3.0383F, 0.0F, 0.0F, -0.2182F));
        antler_left.addOrReplaceChild("antler_l_crown_main_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.006F, -7.6534F, 2.6541F, -0.4659F, -0.1198F, -0.2333F));
        antler_left.addOrReplaceChild("antler_l_mid_tine_tip_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9411F, -13.6639F, -1.3571F, 1.5708F, 0.0F, 0.4363F));
        antler_left.addOrReplaceChild("antler_l_crown_back_r3", CubeListBuilder.create().texOffs(36, 46).addBox(-0.3087F, -1.9014F, -0.8387F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2751F, -9.6064F, 3.0383F, 0.0F, 0.0F, -0.0873F));
        antler_left.addOrReplaceChild("antler_l_mid_tine_tip_r2", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5482F, -1.8511F, 0.0795F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9411F, -13.6639F, -1.3571F, 0.8727F, 0.0F, 0.0F));
        antler_left.addOrReplaceChild("antler_l_beam_upper_r1", CubeListBuilder.create().texOffs(36, 46).addBox(2.2177F, -0.5817F, 2.1864F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -11.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
        antler_left.addOrReplaceChild("antler_l_brow_tip_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, 0.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -7.5F, -5.5F, 0.48F, 0.0F, 0.0F));
        antler_left.addOrReplaceChild("antler_l_crown_back_r4", CubeListBuilder.create().texOffs(36, 46).addBox(-0.9177F, -1.2459F, -0.3954F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4031F, -17.4748F, 1.5282F, 0.5672F, 0.0F, -0.829F));
        antler_left.addOrReplaceChild("antler_l_crown_main_r2", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5983F, -2.4172F, -0.3829F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.006F, -7.6534F, 2.6541F, 0.0577F, -0.1198F, -0.2333F));
        antler_left.addOrReplaceChild("antler_l_beam_upper_r2", CubeListBuilder.create().texOffs(36, 46).addBox(2.3284F, -1.4389F, 2.263F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -11.0F, 0.0F, 0.3491F, 0.0F, 0.3927F));
        antler_left.addOrReplaceChild("antler_l_mid_tine_tip_r3", CubeListBuilder.create().texOffs(36, 46).addBox(-1.1446F, -0.7065F, -0.7466F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4275F, -13.1593F, -3.2303F, 2.0134F, -0.0978F, 0.692F));
        antler_left.addOrReplaceChild("antler_l_mid_tine_fwd_r1", CubeListBuilder.create().texOffs(36, 46).addBox(3.6326F, -5.9532F, -1.3866F, 1.0F, 1.0F, 3.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -8.5F, -2.5F, -0.6104F, -0.025F, -0.0357F));
        antler_left.addOrReplaceChild("antler_l_beam_mid_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.6075F, -3.4333F, 0.9902F, 1.0F, 5.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -6.0F, 0.0F, -0.5713F, 0.4259F, 0.5713F));
        antler_left.addOrReplaceChild("antler_l_brow_tip_r2", CubeListBuilder.create().texOffs(36, 46).addBox(-0.7733F, -1.4464F, -0.4284F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -7.5F, -5.5F, 0.0436F, 0.0F, 0.0F));
        antler_left.addOrReplaceChild("antler_l_brow_tine_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, -1.0F, -2.5F, 1.0F, 1.0F, 4.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -4.0F, -2.5F, -0.48F, 0.0F, 0.0F));
        antler_left.addOrReplaceChild("antler_l_beam_lower_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.9364F, -1.5271F, -0.6749F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -3.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
        antler_left.addOrReplaceChild("antler_l_pedicle_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition antler_right = head.addOrReplaceChild("antler_right", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, -3.5237F, 1.2926F, -0.3927F, -0.2618F, 0.1745F));
        antler_right.addOrReplaceChild("antler_r_crown_back_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.0823F, -1.2459F, -0.3954F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4031F, -17.4748F, 1.5282F, 0.5672F, 0.0F, 0.829F));
        antler_right.addOrReplaceChild("antler_r_crown_back_r2", CubeListBuilder.create().texOffs(36, 46).addBox(-1.9339F, -7.1755F, -1.0293F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.2751F, -9.6064F, 3.0383F, 0.0F, 0.0F, 0.48F));
        antler_right.addOrReplaceChild("antler_r_crown_back_r3", CubeListBuilder.create().texOffs(36, 46).addBox(-0.9239F, -4.4531F, -1.034F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.2751F, -9.6064F, 3.0383F, 0.0F, 0.0F, 0.2182F));
        antler_right.addOrReplaceChild("antler_r_crown_back_r4", CubeListBuilder.create().texOffs(36, 46).addBox(-0.6913F, -1.9014F, -0.8387F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.2751F, -9.6064F, 3.0383F, 0.0F, 0.0F, 0.0873F));
        antler_right.addOrReplaceChild("antler_r_crown_main_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.4017F, -2.4172F, -0.3829F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.006F, -7.6534F, 2.6541F, 0.0577F, 0.1198F, 0.2333F));
        antler_right.addOrReplaceChild("antler_r_crown_main_r2", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.006F, -7.6534F, 2.6541F, -0.4659F, 0.1198F, 0.2333F));
        antler_right.addOrReplaceChild("antler_r_beam_upper_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-3.3284F, -1.4389F, 2.263F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -11.0F, 0.0F, 0.3491F, 0.0F, -0.3927F));
        antler_right.addOrReplaceChild("antler_r_beam_upper_r2", CubeListBuilder.create().texOffs(36, 46).addBox(-3.2177F, -0.5817F, 2.1864F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -11.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
        antler_right.addOrReplaceChild("antler_r_mid_tine_tip_r1", CubeListBuilder.create().texOffs(36, 46).addBox(0.1446F, -0.7065F, -0.7466F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.4275F, -13.1593F, -3.2303F, 2.0134F, 0.0978F, -0.692F));
        antler_right.addOrReplaceChild("antler_r_mid_tine_tip_r2", CubeListBuilder.create().texOffs(36, 46).addBox(-0.4518F, -1.8511F, 0.0795F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.9411F, -13.6639F, -1.3571F, 0.8727F, 0.0F, 0.0F));
        antler_right.addOrReplaceChild("antler_r_mid_tine_tip_r3", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.9411F, -13.6639F, -1.3571F, 1.5708F, 0.0F, -0.4363F));
        antler_right.addOrReplaceChild("antler_r_mid_tine_fwd_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-4.6326F, -5.9532F, -1.3866F, 1.0F, 1.0F, 3.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -8.5F, -2.5F, -0.6104F, 0.025F, 0.0357F));
        antler_right.addOrReplaceChild("antler_r_beam_mid_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.3925F, -3.4333F, 0.9902F, 1.0F, 5.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -6.0F, 0.0F, -0.5713F, -0.4259F, -0.5713F));
        antler_right.addOrReplaceChild("antler_r_brow_tip_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.2267F, -1.4464F, -0.4284F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -7.5F, -5.5F, 0.0436F, 0.0F, 0.0F));
        antler_right.addOrReplaceChild("antler_r_brow_tip_r2", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, 0.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -7.5F, -5.5F, 0.48F, 0.0F, 0.0F));
        antler_right.addOrReplaceChild("antler_r_brow_tine_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, -1.0F, -2.5F, 1.0F, 1.0F, 4.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -4.0F, -2.5F, -0.48F, 0.0F, 0.0F));
        antler_right.addOrReplaceChild("antler_r_beam_lower_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-1.0636F, -1.5271F, -0.6749F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -3.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
        antler_right.addOrReplaceChild("antler_r_pedicle_r1", CubeListBuilder.create().texOffs(36, 46).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.0F, 9.0F, 0.4363F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tail_mesh_r1", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, 0.5551F, -1.6067F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.9063F, -0.4226F, 1.6581F, 0.0F, 0.0F));

        root.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(14, 38).addBox(-1.2F, 0.0F, -1.5F, 2.4F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(26, 38).addBox(-0.8F, 9.0F, -1.0F, 1.6F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -17.0F, -5.0F));

        root.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(14, 38).addBox(-1.2F, 0.0F, -1.5F, 2.4F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(26, 38).addBox(-0.8F, 9.0F, -1.0F, 1.6F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -17.0F, -5.0F));

        PartDefinition leg_hind_left = root.addOrReplaceChild("leg_hind_left", CubeListBuilder.create().texOffs(0, 50).addBox(-1.3F, 0.0F, -2.0F, 2.6F, 9.0F, 4.5F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -18.0F, 7.0F));
        leg_hind_left.addOrReplaceChild("hind_l_lower_r1", CubeListBuilder.create().texOffs(26, 52).addBox(-0.8F, -2.5F, -1.0F, 1.6F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 15.5F, 3.5F, -0.1745F, 0.0F, 0.0F));
        leg_hind_left.addOrReplaceChild("hind_l_shank_r1", CubeListBuilder.create().texOffs(16, 52).addBox(-0.8F, -3.0F, -1.0F, 1.6F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition leg_hind_right = root.addOrReplaceChild("leg_hind_right", CubeListBuilder.create().texOffs(0, 50).addBox(-1.3F, 0.0F, -2.0F, 2.6F, 9.0F, 4.5F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -18.0F, 7.0F));
        leg_hind_right.addOrReplaceChild("hind_r_lower_r1", CubeListBuilder.create().texOffs(26, 52).addBox(-0.8F, -2.5F, -1.0F, 1.6F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 15.5F, 3.5F, -0.1745F, 0.0F, 0.0F));
        leg_hind_right.addOrReplaceChild("hind_r_shank_r1", CubeListBuilder.create().texOffs(16, 52).addBox(-0.8F, -3.0F, -1.0F, 1.6F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, 0.5236F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(AmberWeepingStagRenderState state) {
        super.setupAnim(state);
        this.walkAnimation.apply(state.walkAnimationState, state.ageInTicks);
        this.idleAnimation.apply(state.idleAnimationState, state.ageInTicks);
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
    }
}
