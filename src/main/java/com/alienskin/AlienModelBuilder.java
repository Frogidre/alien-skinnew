package com.alienskin;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;

/**
 * Builds the alien out of many small boxes, attached as children of the vanilla
 * player parts so they follow the normal animations. All boxes sample flat-colour
 * areas of alien.png (the vanilla boxes are hidden while the alien is on).
 *
 * Model space: +y is DOWN, -z is the front. 1 unit = 1/16 block.
 */
public final class AlienModelBuilder {
    // UV origins (top-left of a colour area) in the 64x64 texture
    private static final int[] SKIN  = {0, 0};    // big skin area  (0,0)-(63,31)
    private static final int[] DARK  = {0, 32};   // dark skin      (0,32)-(31,47)
    private static final int[] RIB   = {32, 32};  // light bone     (32,32)-(63,47)
    private static final int[] EYE   = {0, 48};   // black          (0,48)-(15,63)
    private static final int[] CLAW  = {16, 48};  // dark claw      (16,48)-(31,63)
    private static final int[] RED   = {32, 48};  // mouth inside   (32,48)-(47,63)
    private static final int[] WHITE = {48, 48};  // eye glint      (48,48)-(63,63)

    // Skull ellipsoid
    private static final float CY = -6.8f, RY = 6.4f, RX = 5.0f, RZ = 5.2f, ZC = 1.3f;

    private AlienModelBuilder() {}

    public static void build(ModelData data) {
        ModelPartData root = data.getRoot();
        buildHead(root.getChild("head"));
        buildTorso(root.getChild("body"));
        buildArm(root.getChild("left_arm"));
        buildArm(root.getChild("right_arm"));
        buildLeg(root.getChild("left_leg"));
        buildLeg(root.getChild("right_leg"));
    }

    // ---- helpers -------------------------------------------------------------------------

    private static ModelPartBuilder box(ModelPartBuilder bld, int[] uv,
                                        float x0, float y0, float z0, float x1, float y1, float z1) {
        return bld.uv(uv[0], uv[1]).cuboid(x0, y0, z0, x1 - x0, y1 - y0, z1 - z0);
    }

    private static float t(float y) {
        float t = (y - CY) / RY;
        return Math.max(-0.97f, Math.min(0.97f, t));
    }

    private static float shape(float y) {
        float t = t(y);
        return (float) Math.sqrt(1.0 - t * t) * (1.0f - 0.10f * t);   // egg: wider on top
    }

    private static float halfW(float y)   { return RX * shape(y); }
    private static float halfD(float y)   { return RZ * shape(y); }
    private static float zCenter(float y) { return ZC - 0.6f * t(y); } // skull sweeps backwards
    private static float frontZ(float y)  { return zCenter(y) - halfD(y); }

    // ---- head ----------------------------------------------------------------------------

    private static void buildHead(ModelPartData head) {
        ModelPartBuilder skull = ModelPartBuilder.create();

        // egg-shaped skull from 12 stacked slices
        for (int j = 0; j < 12; j++) {
            float y0 = -13.0f + j;
            float ym = y0 + 0.5f;
            float a = halfW(ym), zc = zCenter(ym), zh = halfD(ym);
            box(skull, SKIN, -a, y0, zc - zh, a, y0 + 1.02f, zc + zh);
        }

        // big slanted black eyes (outer corner high, inner corner low)
        float[][] rows = {
                {-9.0f, 0.9f, 2.6f, 4.6f},
                {-8.2f, 0.9f, 1.6f, 4.8f},
                {-7.4f, 0.9f, 1.0f, 4.7f},
                {-6.6f, 0.9f, 0.9f, 4.0f},
                {-5.8f, 0.9f, 0.9f, 3.0f},
                {-5.0f, 0.8f, 1.2f, 2.0f}
        };
        int k = 0;
        for (float[] r : rows) {
            float zf = frontZ(r[0] + r[1] / 2f) - 0.35f - 0.01f * k;
            k++;
            box(skull, EYE, r[2], r[0], zf, r[3], r[0] + r[1], zf + 0.4f);
            box(skull, EYE, -r[3], r[0], zf, -r[2], r[0] + r[1], zf + 0.4f);
        }
        // eye glints
        float zg = frontZ(-8.4f) - 0.45f;
        box(skull, WHITE, 3.7f, -8.7f, zg, 4.3f, -8.1f, zg + 0.12f);
        box(skull, WHITE, -4.3f, -8.7f, zg, -3.7f, -8.1f, zg + 0.12f);
        // nostrils
        float zn = frontZ(-3.8f) - 0.15f;
        box(skull, DARK, 0.3f, -3.9f, zn, 0.8f, -3.4f, zn + 0.2f);
        box(skull, DARK, -0.8f, -3.9f, zn, -0.3f, -3.4f, zn + 0.2f);
        head.addChild("alien_skull", skull, ModelTransform.NONE);

        // jaw (drops when the player talks) - sticks out in front of the face
        float zj = frontZ(-1.6f);
        ModelPartBuilder jaw = ModelPartBuilder.create();
        box(jaw, SKIN, -1.7f, -2.2f, zj - 1.2f, 1.7f, -0.5f, zj + 0.6f);
        box(jaw, DARK, -1.4f, -2.3f, zj - 1.3f, 1.4f, -1.9f, zj - 1.0f);   // lip line
        head.addChild("alien_jaw", jaw, ModelTransform.NONE);

        // red mouth inside, only visible when the jaw is open
        ModelPartBuilder mouth = ModelPartBuilder.create();
        box(mouth, RED, -1.5f, -2.4f, zj - 0.5f, 1.5f, -0.6f, zj + 0.1f);
        head.addChild("alien_mouth", mouth, ModelTransform.NONE);
    }

    // ---- torso ---------------------------------------------------------------------------

    private static void buildTorso(ModelPartData body) {
        ModelPartBuilder t = ModelPartBuilder.create();
        box(t, SKIN, -1.1f, -2.0f, -0.5f, 1.1f, 0.8f, 1.5f);              // thin neck
        box(t, SKIN, -5.0f, 0.0f, -0.9f, 5.0f, 1.0f, 0.9f);               // collar bones / shoulders
        box(t, SKIN, -2.7f, 1.0f, -1.7f, 2.7f, 6.0f, 1.5f);               // chest
        for (int i = 0; i < 5; i++) {                                      // ribs
            float y = 1.4f + i * 0.95f;
            box(t, RIB, -2.4f, y, -2.1f, 2.4f, y + 0.5f, -1.65f);
        }
        box(t, DARK, -0.3f, 1.0f, -2.15f, 0.3f, 6.0f, -1.8f);             // breastbone line
        box(t, SKIN, -1.9f, 6.0f, -1.2f, 1.9f, 9.6f, 1.2f);               // sunken belly
        box(t, SKIN, -2.5f, 9.6f, -1.5f, 2.5f, 12.3f, 1.7f);              // hips
        box(t, SKIN, -2.0f, 10.0f, 1.7f, 2.0f, 12.3f, 2.5f);              // rear
        body.addChild("alien_torso", t, ModelTransform.NONE);
    }

    // ---- arms ----------------------------------------------------------------------------

    private static void buildArm(ModelPartData arm) {
        ModelPartBuilder up = ModelPartBuilder.create();
        box(up, SKIN, -1.2f, -1.2f, -1.2f, 1.2f, 1.2f, 1.2f);             // shoulder
        box(up, SKIN, -0.9f, 0.8f, -0.9f, 0.9f, 7.8f, 0.9f);              // upper arm
        box(up, DARK, -1.05f, 7.2f, -1.05f, 1.05f, 8.1f, 1.05f);          // elbow
        ModelPartData a = arm.addChild("alien_arm", up, ModelTransform.NONE);

        ModelPartBuilder fb = ModelPartBuilder.create();
        box(fb, SKIN, -0.75f, 0.0f, -0.75f, 0.75f, 8.2f, 0.75f);          // forearm
        box(fb, DARK, -0.95f, 7.5f, -0.95f, 0.95f, 8.3f, 0.95f);          // wrist
        box(fb, SKIN, -1.0f, 8.2f, -0.7f, 1.0f, 9.8f, 0.7f);              // palm
        ModelPartData fore = a.addChild("alien_fore", fb, ModelTransform.NONE);

        for (int i = 0; i < 3; i++) {                                      // three long claws
            ModelPartBuilder c = ModelPartBuilder.create();
            box(c, CLAW, -0.25f, 0.0f, -0.25f, 0.25f, 5.0f, 0.25f);
            fore.addChild("alien_claw" + i, c, ModelTransform.NONE);
        }
    }

    // ---- legs ----------------------------------------------------------------------------

    private static void buildLeg(ModelPartData leg) {
        ModelPartBuilder th = ModelPartBuilder.create();
        box(th, SKIN, -1.2f, -0.8f, -1.2f, 1.2f, 1.0f, 1.4f);             // hip
        box(th, SKIN, -1.0f, 0.5f, -0.9f, 1.0f, 6.6f, 1.1f);              // thigh
        box(th, DARK, -1.1f, 6.0f, -1.0f, 1.1f, 7.0f, 0.8f);              // knee
        ModelPartData l = leg.addChild("alien_leg", th, ModelTransform.NONE);

        ModelPartBuilder sh = ModelPartBuilder.create();
        box(sh, SKIN, -0.8f, 0.0f, -0.75f, 0.8f, 5.8f, 0.75f);            // shin
        ModelPartData shin = l.addChild("alien_shin", sh, ModelTransform.NONE);

        ModelPartBuilder ft = ModelPartBuilder.create();
        box(ft, SKIN, -0.7f, -0.7f, -0.6f, 0.7f, 0.0f, 1.2f);             // heel
        for (int i = 0; i < 3; i++) {                                      // three long toes
            float c = -1.0f + i * 1.0f;
            box(ft, SKIN, c - 0.25f, -0.5f, -4.2f, c + 0.25f, 0.0f, -0.4f);
            box(ft, CLAW, c - 0.25f, -0.45f, -4.9f, c + 0.25f, 0.0f, -4.2f);
        }
        shin.addChild("alien_foot", ft, ModelTransform.NONE);
    }
}
