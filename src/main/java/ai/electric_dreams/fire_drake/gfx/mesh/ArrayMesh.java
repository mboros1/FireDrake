package ai.electric_dreams.fire_drake.gfx.mesh;

import ai.electric_dreams.fire_drake.gfx.Mesh;
import ai.electric_dreams.fire_drake.gfx.Shader;

public class ArrayMesh implements Mesh {

    private final float[] vertices;
    private final int[] indices;

    private int vao;
    private int vbo;
    private int ebo;


    public ArrayMesh(float[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;

    }

    @Override
    public void draw(Shader shader) {

    }

    @Override
    public void cleanup() {

    }
}
