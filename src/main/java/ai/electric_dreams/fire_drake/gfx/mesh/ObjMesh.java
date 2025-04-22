package ai.electric_dreams.fire_drake.gfx.mesh;

import ai.electric_dreams.fire_drake.gfx.Debug;
import ai.electric_dreams.fire_drake.gfx.Mesh;
import ai.electric_dreams.fire_drake.gfx.Shader;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengles.GLES30.glDeleteVertexArrays;

public class ObjMesh implements Mesh {

    private static Logger logger = LoggerFactory.getLogger(ObjMesh.class);

    record FaceVertex(int v, int vt, int vn) { }

    record Face(List<FaceVertex> vertices) { }

    private final List<float[]> vertices = new ArrayList<>();
    private final List<float[]> norms = new ArrayList<>();
    private final Set<String> unhandledCases = new HashSet<>();
    private final List<Face> faces = new ArrayList<>();
    private final List<String> materialFiles = new ArrayList<>();
    
    // OpenGL objects
    private int vao;
    private int vbo;
    private int ebo;
    private int indexCount;

    public ObjMesh(String resourceFilePath) {
        try (var objStream = ObjMesh.class.getClassLoader().getResourceAsStream(resourceFilePath);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(objStream)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank()) continue;

                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "v" -> vertices.add(new float[]{
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    });
                    case "vn" -> norms.add(new float[]{
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    });
                    case "vt" -> {
                        System.out.println("vts: " + Arrays.stream(tokens).skip(1).toList());
                    }
                    case "f" -> {
                        List<FaceVertex> faceVertices = getFaceVertices(tokens);
                        faces.add(new Face(faceVertices));
                    }
                    case "mtllib" -> {
                        materialFiles.add(tokens[1]);
                    }
                    case "usemtl", "o", "g", "s" -> {
                        logger.info("Ignored token: {}", List.of(tokens));
                    }
                    default -> {
                        if (unhandledCases.add(tokens[0])) {
                            logger.info("Unhandled token: {}", List.of(tokens));
                        }
                    }
                }
            }

            setupMesh();

        } catch (IOException | NullPointerException e) {
            logger.error("Failed to read OBJ file: {}", e.getMessage(), e);
        }
    }

    public void reportObjStats() {
        logger.info("OBJ Stats:");
        logger.info("  Vertices: {}", vertices.size());
        logger.info("  Normals: {}", norms.size());
        logger.info("  Faces: {}", faces.size());
        logger.info("  Material Files: {}", materialFiles.size());
        logger.info("  Unhandled Cases: {}", unhandledCases.size());
    }

    private static List<FaceVertex> getFaceVertices(String[] tokens) {
        List<FaceVertex> faceVertices = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            String[] parts = tokens[i].split("/");

            int v = Integer.parseInt(parts[0]) - 1; // Always present

            int vt = -1;
            int vn = -1;

            if (parts.length > 1 && !parts[1].isEmpty()) {
                vt = Integer.parseInt(parts[1]) - 1;
            }
            if (parts.length > 2 && !parts[2].isEmpty()) {
                vn = Integer.parseInt(parts[2]) - 1;
            }

            faceVertices.add(new FaceVertex(v, vt, vn));
        }
        return faceVertices;
    }

    private void setupMesh() {
        if (faces.isEmpty()) {
            logger.warn("No faces found, mesh will not be rendered");
            return;
        }

        // Create a map for index translation (we need to create unique vertices for OpenGL)
        Map<String, Integer> uniqueVertices = new HashMap<>();
        List<Float> vertexData = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        // Process each face
        for (Face face : faces) {
            // Triangulate face if needed (assuming face vertices are coplanar)
            for (int i = 0; i < face.vertices.size() - 2; i++) {
                processFaceTriangle(face.vertices.get(0), face.vertices.get(i + 1), face.vertices.get(i + 2),
                        uniqueVertices, vertexData, indices);
            }
        }

        // Create buffers
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();
        Debug.glCheckError("ObjMesh.setupMesh - create buffers");

        glBindVertexArray(vao);
        Debug.glCheckError("ObjMesh.setupMesh - bind VAO");

        // Create and fill vertex buffer
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexData.size());
        for (Float value : vertexData) {
            vertexBuffer.put(value);
        }
        vertexBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        Debug.glCheckError("ObjMesh.setupMesh - buffer vertex data");

        // Create and fill element buffer
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.size());
        for (Integer index : indices) {
            indexBuffer.put(index);
        }
        indexBuffer.flip();
        indexCount = indices.size();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        Debug.glCheckError("ObjMesh.setupMesh - buffer index data");

        // 6 floats per vertex: 3 for position, 3 for normal
        int stride = 6 * Float.BYTES;

        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        Debug.glCheckError("ObjMesh.setupMesh - position attribute");

        // Normal attribute (used for coloring)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        Debug.glCheckError("ObjMesh.setupMesh - normal attribute");

        // Unbind
        glBindVertexArray(0);
        Debug.glCheckError("ObjMesh.setupMesh - unbind");
    }

    private void processFaceTriangle(FaceVertex v1, FaceVertex v2, FaceVertex v3,
                                    Map<String, Integer> uniqueVertices, 
                                    List<Float> vertexData, 
                                    List<Integer> indices) {
        processFaceVertex(v1, uniqueVertices, vertexData, indices);
        processFaceVertex(v2, uniqueVertices, vertexData, indices);
        processFaceVertex(v3, uniqueVertices, vertexData, indices);
    }

    private void processFaceVertex(FaceVertex fv, Map<String, Integer> uniqueVertices, 
                                  List<Float> vertexData, List<Integer> indices) {
        // Create a unique key for this vertex
        String key = fv.v + ":" + fv.vn;

        // If we haven't seen this exact vertex before, add it to our buffer
        if (!uniqueVertices.containsKey(key)) {
            // Position
            float[] position = vertices.get(fv.v);
            vertexData.add(position[0]);
            vertexData.add(position[1]);
            vertexData.add(position[2]);

            // Normal (also used for coloring)
            float[] normal;
            if (fv.vn >= 0 && fv.vn < norms.size()) {
                normal = norms.get(fv.vn);
            } else {
                // Default normal if none specified
                normal = new float[]{0.0f, 0.0f, 1.0f};
            }
            vertexData.add(normal[0]);
            vertexData.add(normal[1]);
            vertexData.add(normal[2]);

            // Store the new vertex index
            uniqueVertices.put(key, uniqueVertices.size());
        }

        // Add the vertex index to our indices
        indices.add(uniqueVertices.get(key));
    }

    @Override
    public void draw(Shader shader) {
        if (vao == 0) {
            return; // No mesh data
        }
        shader.bind();

        glBindVertexArray(vao);
        Debug.glCheckError("ObjMesh.draw - bind VAO");
        
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        Debug.glCheckError("ObjMesh.draw - draw elements");
        
        glBindVertexArray(0);
        Debug.glCheckError("ObjMesh.draw - unbind VAO");
    }

    @Override
    public void cleanup() {
        if (vao != 0) {
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
            glDeleteBuffers(ebo);
            vao = 0;
            vbo = 0;
            ebo = 0;
            Debug.glCheckError("ObjMesh.cleanup");
        }
    }
}
