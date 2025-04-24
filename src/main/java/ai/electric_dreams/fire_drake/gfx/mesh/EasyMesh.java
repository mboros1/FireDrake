package ai.electric_dreams.fire_drake.gfx.mesh;

import ai.electric_dreams.fire_drake.gfx.*;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;


public class EasyMesh implements Mesh {
    private final List<Vertex> vertices;
    private final List<Integer> indices;
    private final List<Texture> textures;
    
    private int vao;
    private int vbo;
    private int ebo;

    public EasyMesh(List<Vertex> vertices, List<Integer> indices, List<Texture> textures) {
        this.vertices = vertices;
        this.indices = indices;
        this.textures = textures;
        
        setupMesh();
    }

    private void setupMesh() {
        // Create buffers/arrays
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();
        Debug.glCheckError("Mesh.setupMesh - create buffers");

        glBindVertexArray(vao);
        Debug.glCheckError("Mesh.setupMesh - bind VAO");
        
        // Load vertex data
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.size() * 14); // 3 pos + 3 normal + 2 tex + 3 tangent + 3 bitangent
        for (Vertex vertex : vertices) {
            // Position
            verticesBuffer.put(vertex.getPosition().x());
            verticesBuffer.put(vertex.getPosition().y());
            verticesBuffer.put(vertex.getPosition().z());
            // Normal
            verticesBuffer.put(vertex.getNormal().x());
            verticesBuffer.put(vertex.getNormal().y());
            verticesBuffer.put(vertex.getNormal().z());
            // TexCoords
            verticesBuffer.put(vertex.getTexCoords().x());
            verticesBuffer.put(vertex.getTexCoords().y());
            // Tangent
            verticesBuffer.put(vertex.getTangent().x());
            verticesBuffer.put(vertex.getTangent().y());
            verticesBuffer.put(vertex.getTangent().z());
            // Bitangent
            verticesBuffer.put(vertex.getBitangent().x());
            verticesBuffer.put(vertex.getBitangent().y());
            verticesBuffer.put(vertex.getBitangent().z());
        }
        verticesBuffer.flip();

        // Load vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        Debug.glCheckError("Mesh.setupMesh - bind VBO");
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        Debug.glCheckError("Mesh.setupMesh - buffer vertex data");

        // Load index buffer
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.size());
        indices.forEach(indicesBuffer::put);
        indicesBuffer.flip();
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        Debug.glCheckError("Mesh.setupMesh - bind EBO");
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        Debug.glCheckError("Mesh.setupMesh - buffer index data");

        // Set vertex attribute pointers
        int stride = 14 * Float.BYTES;
        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        Debug.glCheckError("Mesh.setupMesh - position attribute");
        // Normal attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        Debug.glCheckError("Mesh.setupMesh - normal attribute");
        // Texture coordinate attribute
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
        Debug.glCheckError("Mesh.setupMesh - texcoord attribute");
        // Tangent attribute
        glVertexAttribPointer(3, 3, GL_FLOAT, false, stride, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);
        Debug.glCheckError("Mesh.setupMesh - tangent attribute");
        // Bitangent attribute
        glVertexAttribPointer(4, 3, GL_FLOAT, false, stride, 11 * Float.BYTES);
        glEnableVertexAttribArray(4);
        Debug.glCheckError("Mesh.setupMesh - bitangent attribute");

        glBindVertexArray(0);
        Debug.glCheckError("Mesh.setupMesh - unbind VAO");
    }

    public void draw(Shader shader) {
        int diffuseNr = 1;
        int specularNr = 1;
        int normalNr = 1;
        int heightNr = 1;

        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            Debug.glCheckError("Mesh.draw - active texture " + i);
            
            String number;
            String name = textures.get(i).getType();
            
            if (name.equals("texture_diffuse")) {
                number = String.valueOf(diffuseNr++);
            } else if (name.equals("texture_specular")) {
                number = String.valueOf(specularNr++);
            } else if (name.equals("texture_normal")) {
                number = String.valueOf(normalNr++);
            } else if (name.equals("texture_height")) {
                number = String.valueOf(heightNr++);
            } else {
                continue;
            }

            shader.setInt(name + number, i);
            glBindTexture(GL_TEXTURE_2D, textures.get(i).getId());
            Debug.glCheckError("Mesh.draw - bind texture " + i);
        }

        // Draw mesh
        glBindVertexArray(vao);
        Debug.glCheckError("Mesh.draw - bind VAO");
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        Debug.glCheckError("Mesh.draw - draw elements");
        glBindVertexArray(0);
        Debug.glCheckError("Mesh.draw - unbind VAO");

        glActiveTexture(GL_TEXTURE0);
        Debug.glCheckError("Mesh.draw - reset active texture");
    }

    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        Debug.glCheckError("Mesh.cleanup");
    }

    @Override
    public Vector3f getCenter() {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);

        for (Vertex v : vertices) {
            min.min(v.getPosition());
            max.max(v.getPosition());
        }
        return min.add(max).mul(0.5f); // Center of bounding box
    }

} 