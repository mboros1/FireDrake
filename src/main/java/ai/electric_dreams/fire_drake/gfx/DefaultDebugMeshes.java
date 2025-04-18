package ai.electric_dreams.fire_drake.gfx;

import ai.electric_dreams.fire_drake.gfx.mesh.ArrayMesh;
import ai.electric_dreams.fire_drake.gfx.mesh.EasyMesh;
import org.joml.Vector2f;
import org.joml.Vector3f;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public final class DefaultDebugMeshes {

    private static Mesh FULLSCREEN;

    /** Lazy‑init singleton */
    public static Mesh fullscreenQuad() {
        if (FULLSCREEN == null) FULLSCREEN = createFullscreenQuad();
        return FULLSCREEN;
    }

    // ---------------------------------------------------------------------

    private static Mesh createFullscreenQuad() {

        /* A Vertex holds:
           - position (vec3)
           - normal    (vec3)
           - texCoord  (vec2)
           - tangent   (vec3)
           - bitangent (vec3)
        */

        Vector3f p0 = new Vector3f(-1f, -1f, 0f);
        Vector3f p1 = new Vector3f( 1f, -1f, 0f);
        Vector3f p2 = new Vector3f( 1f,  1f, 0f);
        Vector3f p3 = new Vector3f(-1f,  1f, 0f);

        Vector2f uv0 = new Vector2f(0f, 0f);
        Vector2f uv1 = new Vector2f(1f, 0f);
        Vector2f uv2 = new Vector2f(1f, 1f);
        Vector2f uv3 = new Vector2f(0f, 1f);

        // For a flat screen‑aligned quad the normal can be zero;
        // tangents/bitangents aren’t used by our PS1 shader.
        Vector3f zero = new Vector3f();

        List<Vertex> verts = Arrays.asList(
                new Vertex(p0, zero, uv0, zero, zero),
                new Vertex(p1, zero, uv1, zero, zero),
                new Vertex(p2, zero, uv2, zero, zero),
                new Vertex(p3, zero, uv3, zero, zero)
        );

        // Two triangles: 0‑1‑2, 2‑3‑0
        List<Integer> indices = Arrays.asList(0, 1, 2, 2, 3, 0);

        // No material/texture needed here; your post shader binds the scene tex.
        List<Texture> textures = Collections.emptyList();

        return new EasyMesh(verts, indices, textures);
    }

    public static Mesh defaultTriangle() {
        float[] vertices = {
                // positions         // colors
                0.5f, -0.5f, 0.0f,  1.0f, 0.0f, 0.0f,  // bottom right
                -0.5f, -0.5f, 0.0f,  0.0f, 1.0f, 0.0f,  // bottom left
                0.0f,  0.5f, 0.0f,  0.0f, 0.0f, 1.0f   // top
        };

        int[] indices = {
                0, 1, 2
        };

        return new ArrayMesh(vertices, indices);
    }

    private DefaultDebugMeshes() {}   // utility class
}

