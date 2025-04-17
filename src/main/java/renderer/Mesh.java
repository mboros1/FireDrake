package renderer;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;


public class Mesh {
    private final List<Vertex> vertices;
    private final List<Integer> indices;
    private final List<Texture> textures;
    
    private int vao;
    private int vbo;
    private int ebo;

    public Mesh(List<Vertex> vertices, List<Integer> indices, List<Texture> textures) {
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

        glBindVertexArray(vao);
        
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
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        // Load index buffer
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.size());
        indices.forEach(indicesBuffer::put);
        indicesBuffer.flip();
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        // Set vertex attribute pointers
        int stride = 14 * Float.BYTES;
        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        // Normal attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        // Texture coordinate attribute
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
        // Tangent attribute
        glVertexAttribPointer(3, 3, GL_FLOAT, false, stride, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);
        // Bitangent attribute
        glVertexAttribPointer(4, 3, GL_FLOAT, false, stride, 11 * Float.BYTES);
        glEnableVertexAttribArray(4);

        glBindVertexArray(0);
    }

    public void draw(Shader shader) {
        int diffuseNr = 1;
        int specularNr = 1;
        int normalNr = 1;
        int heightNr = 1;

        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            
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
        }

        // Draw mesh
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        glActiveTexture(GL_TEXTURE0);
    }

    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
    }
} 