package renderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a shader source that can be loaded either from a file or from memory.
 */
public record ShaderSource(String source) {
    
    /**
     * Creates a ShaderSource from a file path.
     *
     * @param path the path to the shader file
     * @return a new ShaderSource instance
     * @throws RuntimeException if the file cannot be read
     */
    public static ShaderSource fromFile(String path) {
        try {
            return new ShaderSource(Files.readString(Path.of(path)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader file: " + path, e);
        }
    }

    /**
     * Creates a ShaderSource from a string in memory.
     *
     * @param source the shader source code
     * @return a new ShaderSource instance
     */
    public static ShaderSource fromMemory(String source) {
        return new ShaderSource(source);
    }

    /**
     * Gets the shader source code.
     *
     * @return the shader source code
     */
    public String getSource() {
        return source;
    }
} 