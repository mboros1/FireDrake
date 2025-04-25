package ai.electric_dreams.fire_drake.gfx;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Represents a visible entity in the game world.
 */
public class Entity {
    private final Mesh mesh;
    private final Material material;
    private Matrix4f transform;
    private Vector3f meshCenter = null;
    private float speed = 25.0f;

    public Entity(Mesh mesh, Material material, Matrix4f transform) {
        this.mesh = mesh;
        this.material = material;
        this.transform = transform;
    }

    public Vector3f getWorldCenter() {
        if (meshCenter == null) {
            meshCenter = mesh.getCenter();
        }
        return transform.transformPosition(meshCenter);
    }


    public Mesh mesh() {
        return mesh;
    }

    public Material material() {
        return material;
    }

    public Matrix4f transform() {
        return transform;
    }

    public void setTransform(Matrix4f transform) {
        this.transform = transform;
    }

    public void move(Vector3f delta) {
        transform.translate(delta);
    }

    public float getSpeed() {
        return speed;
    }
}