package renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.opengl.GL41.*;

public class Shader {
    private final int programId;
    private final FloatBuffer matrixBuffer;
    private final String name;
    private final Map<String, Integer> uniformLocationCache = new HashMap<>();

    private Shader(String name, ShaderSource vertexShader, ShaderSource fragmentShader) {
        this.name = name;
        programId = glCreateProgram();
        matrixBuffer = BufferUtils.createFloatBuffer(16);

        try {
            int vertexShaderId = compileShader(vertexShader, GL_VERTEX_SHADER);
            int fragmentShaderId = compileShader(fragmentShader, GL_FRAGMENT_SHADER);

            glAttachShader(programId, vertexShaderId);
            glAttachShader(programId, fragmentShaderId);
            glLinkProgram(programId);

            checkProgramLinkStatus();

            // Delete shaders as they're linked into the program and no longer necessary
            glDeleteShader(vertexShaderId);
            glDeleteShader(fragmentShaderId);
        } catch (Exception e) {
            throw new RuntimeException("Shader [" + name + "] compilation failed: " + e.getMessage());
        }
    }

    private int compileShader(ShaderSource source, int type) {
        int shaderId = glCreateShader(type);
        
        glShaderSource(shaderId, source.getSource());
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shaderId);
            throw new RuntimeException("Shader compilation failed:\n" + log);
        }

        return shaderId;
    }

    private void checkProgramLinkStatus() {
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(programId);
            throw new RuntimeException("Program linking failed:\n" + log);
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public int getProgramId() {
        return programId;
    }

    public void setFloat(String name, float value) {
        int location = getUniformLocation(name);
        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    public void setInt(String name, int value) {
        int location = getUniformLocation(name);
        if (location != -1) {
            glUniform1i(location, value);
        }
    }

    public void setVector3f(String name, Vector3f value) {
        int location = getUniformLocation(name);
        if (location != -1) {
            glUniform3f(location, value.x, value.y, value.z);
        }
    }

    public void setMatrix4f(String name, Matrix4f matrix) {
        int location = getUniformLocation(name);
        if (location != -1) {
            matrix.get(matrixBuffer);
            glUniformMatrix4fv(location, false, matrixBuffer);
        }
    }

    private int getUniformLocation(String name) {
        return uniformLocationCache.computeIfAbsent(name, n -> glGetUniformLocation(programId, n));
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    public static ShaderBuilder builder(String name) {
        return new ShaderBuilder(name);
    }

    public static class ShaderBuilder {
        private final String name;
        private Optional<ShaderSource> vertexShader = Optional.empty();
        private Optional<ShaderSource> fragmentShader = Optional.empty();

        private ShaderBuilder(String name) {
            this.name = name;
        }

        public ShaderBuilder vertexFromFile(String path) {
            this.vertexShader = Optional.of(ShaderSource.fromFile(path));
            return this;
        }

        public ShaderBuilder vertexFromMemory(String source) {
            this.vertexShader = Optional.of(ShaderSource.fromMemory(source));
            return this;
        }

        public ShaderBuilder fragmentFromFile(String path) {
            this.fragmentShader = Optional.of(ShaderSource.fromFile(path));
            return this;
        }

        public ShaderBuilder fragmentFromMemory(String source) {
            this.fragmentShader = Optional.of(ShaderSource.fromMemory(source));
            return this;
        }

        public Shader build() {
            ShaderSource vertex = vertexShader.orElseThrow(() -> 
                new IllegalStateException("Vertex shader source not provided"));
            ShaderSource fragment = fragmentShader.orElseThrow(() -> 
                new IllegalStateException("Fragment shader source not provided"));
            return new Shader(name, vertex, fragment);
        }
    }
}