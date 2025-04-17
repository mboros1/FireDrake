package ai.electric_dreams.fire_drake.gfx;

import org.joml.Matrix4f;

public interface Renderer {
    void init(long windowHandle);
    void beginFrame();
    void draw(Mesh mesh, Material mat, Matrix4f model);
    void endFrame();
    void resize(int width, int height);
    void destroy();
}
