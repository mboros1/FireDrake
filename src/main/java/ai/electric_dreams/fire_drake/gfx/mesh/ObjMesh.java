package ai.electric_dreams.fire_drake.gfx.mesh;

import ai.electric_dreams.fire_drake.gfx.Mesh;
import ai.electric_dreams.fire_drake.gfx.Shader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class ObjMesh implements Mesh {

    private static Logger logger = LoggerFactory.getLogger(ObjMesh.class);

    record FaceVertex(int v, int vt, int vn) {
    }

    record Face(List<FaceVertex> vertices) {
    }

    private final List<float[]> vertices = new ArrayList<>();
    private final List<float[]> norms = new ArrayList<>();
    private final Set<String> unhandledCases = new HashSet<>();
    private final List<Face> faces = new ArrayList<>();
    private final List<String> materialFiles = new ArrayList<>();


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

    @Override
    public void draw(Shader shader) {

    }

    @Override
    public void cleanup() {

    }
}
