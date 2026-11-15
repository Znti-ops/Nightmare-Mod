package com.pesadilla.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.PartPose;
import net.minecraft.util.Mth;

public class WatcherModel extends EntityModel<WatcherRenderState> {
    private final ModelPart head, leftArm, rightArm, leftLeg, rightLeg;

    public WatcherModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3, 0, -3, 6, 6, 6), PartPose.offset(0, -14, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-3, 0, -1.5f, 6, 16, 3), PartPose.offset(0, -8, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1, 0, -1, 2, 18, 2), PartPose.offset(-4, -7, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1, 0, -1, 2, 18, 2), PartPose.offset(4, -7, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5f, 0, -1.5f, 3, 16, 3), PartPose.offset(-1.5f, 8, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5f, 0, -1.5f, 3, 16, 3), PartPose.offset(1.5f, 8, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(WatcherRenderState state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        float amp = state.walkAnimationSpeed;
        float pos = state.walkAnimationPos;
        this.leftLeg.xRot = Mth.cos(pos * 0.4f + Mth.PI) * 1.2f * amp;
        this.rightLeg.xRot = Mth.cos(pos * 0.4f) * 1.2f * amp;
        // brazos colgando, largos y torcidos hacia delante
        this.leftArm.xRot = -0.35f + Mth.cos(pos * 0.2f) * 0.3f * amp;
        this.rightArm.xRot = -0.35f - Mth.cos(pos * 0.2f) * 0.3f * amp;
        this.leftArm.zRot = -0.08f;
        this.rightArm.zRot = 0.08f;
    }
}
