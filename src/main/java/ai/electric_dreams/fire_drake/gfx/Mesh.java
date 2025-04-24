package ai.electric_dreams.fire_drake.gfx;

import org.joml.Vector3f;

public interface Mesh {
    void draw(Shader shader);
    void cleanup();

    Vector3f getCenter();
}
