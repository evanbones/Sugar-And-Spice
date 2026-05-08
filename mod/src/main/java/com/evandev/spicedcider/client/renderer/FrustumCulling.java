package com.evandev.spicedcider.client.renderer;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FrustumCulling {
    private static final float TO_RADIANS = (float) (Math.PI / 180);
    private static final Vector3f[] NORMALS = new Vector3f[]{
            new Vector3f(1, 0, 0),
            new Vector3f(-1, 0, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(0, -1, 0)
    };

    private final Quaternionf rotation = new Quaternionf();
    private final Quaternionf rotation2 = new Quaternionf();
    private final Vector3f[] defaultNormals = new Vector3f[4];
    private final Vector3f[] planes = new Vector3f[4];

    public FrustumCulling() {
        for (byte i = 0; i < 4; i++) {
            defaultNormals[i] = new Vector3f(NORMALS[i]);
            planes[i] = new Vector3f(NORMALS[i]);
        }
    }

    public void setFOV(float angle) {
        for (byte i = 0; i < 4; i++) {
            Vector3f original = NORMALS[i];
            Vector3f normal = defaultNormals[i];
            normal.set(original);

            if (normal.x() != 0) {
                rotation.rotationY(normal.x() > 0 ? angle : -angle);
            } else {
                rotation.rotationX(normal.y() > 0 ? -angle : angle);
            }
            normal.rotate(rotation);
        }
    }

    public void rotate(float yaw, float pitch) {
        rotation2.rotationX(pitch * TO_RADIANS);
        rotation.rotationY(-yaw * TO_RADIANS);
        rotation.mul(rotation2);

        for (byte i = 0; i < 4; i++) {
            Vector3f normal = defaultNormals[i];
            planes[i].set(normal);
            planes[i].rotate(rotation);
        }
    }

    public boolean isOutside(Vector3f pos, float distance) {
        for (byte i = 0; i < 4; i++) {
            if (planes[i].dot(pos) > distance) return true;
        }
        return false;
    }
}