package ai.electric_dreams.fire_drake.gfx;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Represents a visible entity in the game world.
 */
public class Entity {
    private Mesh mesh;
    private Material material;
    private Matrix4f transform;

    public Entity(Mesh mesh, Material material, Matrix4f transform) {
        this.mesh = mesh;
        this.material = material;
        this.transform = transform;
    }

    public Vector3f getWorldCenter() {
        Vector3f localCenter = mesh.getCenter();
        return transform.transformPosition(localCenter);
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
} 