package ai.electric_dreams.fire_drake.gfx;

import ai.electric_dreams.fire_drake.gfx.mesh.EasyMesh;
import org.joml.Matrix4f;

public interface Renderer {
    void init(long windowHandle);
    void beginFrame();
    void draw(Mesh mesh, Material mat, Matrix4f model);
    void endFrame();
    void destroy();

    void draw(Mesh testTriangle);

    int getFbWidth();
    int getFbHeight();

    Matrix4f getView();

    Matrix4f getProj();
}
