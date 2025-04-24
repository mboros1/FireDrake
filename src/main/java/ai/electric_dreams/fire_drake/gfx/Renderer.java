package ai.electric_dreams.fire_drake.gfx;

import ai.electric_dreams.fire_drake.gfx.mesh.EasyMesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface Renderer {
    void init(long windowHandle);
    void beginFrame();
    void draw(Mesh mesh, Material mat, Matrix4f model);
    void endFrame();
    void destroy();

    void draw(Mesh testTriangle);

    Vector3f getCameraTarget();

    void setPlayer(Entity player);

    int getFbWidth();
    int getFbHeight();

    Matrix4f getView();

    Matrix4f getProj();

    void gameToCameraUpdates(float radius, float yawRad, float pitchRad);

    Vector3f getCameraPos();

    void setCameraTarget(Vector3f worldCenter);

    void setForward(boolean pressed);

    void setLeft(boolean pressed);

    void setBackward(boolean pressed);

    void setRight(boolean pressed);

    void keyboardMove(float deltaTime);
}
