package ai.electric_dreams.fire_drake.gfx.mesh;

import ai.electric_dreams.fire_drake.gfx.Debug;
import ai.electric_dreams.fire_drake.gfx.Mesh;
import ai.electric_dreams.fire_drake.gfx.Shader;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengles.GLES30.glDeleteVertexArrays;

public class ArrayMesh implements Mesh {

    private final float[] vertices;
    private final int[] indices;

    private int vao;
    private int vbo;


    public ArrayMesh(float[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;

        setupMesh();
    }

    @Override
    public void draw(Shader shader) {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    @Override
    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        Debug.glCheckError("Mesh.draw - draw mesh");

    }

    private void setupMesh() {
        // Create buffers/arrays
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        Debug.glCheckError("Mesh.setupMesh - create buffers");

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Upload data once
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Vertex positions
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        // Color attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3L);
        glEnableVertexAttribArray(1);

        // Unbind for safety (optional but common)
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
}
