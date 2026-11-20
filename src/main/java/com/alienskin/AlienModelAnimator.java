package com.alienskin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

/** Shows/hides the alien parts and poses them every frame. */
public final class AlienModelAnimator {
    private AlienModelAnimator() {}

    public static void apply(PlayerEntityModel m, float mouth, boolean on) {
        BipedEntityModel<?> b = m;
        if (!b.head.hasChild("alien_skull")) return;   // some other model sharing this class

        // hide the vanilla boxes (children still draw) and the clothing layers
        ModelPart[] limbs = {b.head, b.body, b.leftArm, b.rightArm, b.leftLeg, b.rightLeg};
        for (ModelPart p : limbs) p.hidden = on;
        if (on) {
            b.hat.visible = false;
            m.jacket.visible = false;
            m.leftSleeve.visible = false;
            m.rightSleeve.visible = false;
            m.leftPants.visible = false;
            m.rightPants.visible = false;
        }

        vis(b.head, "alien_skull", on);
        vis(b.head, "alien_jaw", on);
        vis(b.head, "alien_mouth", on && mouth > 0.04f);
        vis(b.body, "alien_torso", on);
        vis(b.leftArm, "alien_arm", on);
        vis(b.rightArm, "alien_arm", on);
        vis(b.leftLeg, "alien_leg", on);
        vis(b.rightLeg, "alien_leg", on);
        if (!on) return;

        // jaw drops while talking
        ModelPart jaw = b.head.getChild("alien_jaw");
        jaw.setOrigin(0.0f, 2.3f * mouth, 0.2f * mouth);

        // thin shoulders, bent elbows, spread claws
        b.leftArm.originX = 4.4f;
        b.rightArm.originX = -4.4f;
        poseArm(b.leftArm);
        poseArm(b.rightArm);

        // bent knees, flat feet
        poseLeg(b.leftLeg);
        poseLeg(b.rightLeg);
    }

    private static void poseArm(ModelPart arm) {
        ModelPart fore = arm.getChild("alien_arm").getChild("alien_fore");
        fore.setOrigin(0.0f, 7.6f, 0.0f);
        fore.pitch = -0.2f;
        for (int i = 0; i < 3; i++) {
            ModelPart c = fore.getChild("alien_claw" + i);
            c.setOrigin(0.0f, 9.8f, (i - 1) * 0.85f);
            c.pitch = (i - 1) * 0.22f;
        }
    }

    private static void poseLeg(ModelPart leg) {
        ModelPart shin = leg.getChild("alien_leg").getChild("alien_shin");
        shin.setOrigin(0.0f, 6.5f, 0.0f);
        shin.pitch = 0.32f;
        ModelPart foot = shin.getChild("alien_foot");
        foot.setOrigin(0.0f, 5.8f, 0.0f);
        foot.pitch = -0.32f;
    }

    private static void vis(ModelPart parent, String name, boolean v) {
        if (parent.hasChild(name)) parent.getChild(name).visible = v;
    }
}
