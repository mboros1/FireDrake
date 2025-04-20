package mesh;

import ai.electric_dreams.fire_drake.gfx.Shader;
import ai.electric_dreams.fire_drake.gfx.mesh.ObjMesh;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

public class MeshTest {

    @Test
    public void testLoadObj() {
        // Create a shader for rendering
        Shader shader = Shader.builder("test")
                .vertexFromResource("shaders/psx.vert")
                .fragmentFromResource("shaders/psx.frag")
                .build();
        
        // Load the mesh
        var mesh = new ObjMesh("fire_drake.obj");

        // Report stats
        mesh.reportObjStats();
        
        // Setup view and projection matrices (if you want to test rendering)
        Matrix4f view = new Matrix4f().lookAt(
                0, 0, 5,    // Camera position
                0, 0, 0,    // Look at target
                0, 1, 0     // Up vector
        );
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(45.0f),  // FOV
                16.0f / 9.0f,                   // Aspect ratio
                0.1f,                           // Near plane
                100.0f                          // Far plane
        );
        
        // For a real render test, you would:
        // 1. Initialize OpenGL context
        // 2. Clear buffers
        // shader.bind();
        // shader.setMatrix4f("view", view);
        // shader.setMatrix4f("projection", projection);
        // mesh.draw(shader);
        // shader.unbind();
    }
}
