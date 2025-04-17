package ai.electric_dreams.fire_drake.gfx;

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

    private Shader(String name, ShaderSource vertexSrc, ShaderSource fragmentSrc) {
        this.name = name;
        this.programId   = glCreateProgram();
        this.matrixBuffer = BufferUtils.createFloatBuffer(16);

        try {
            int vId = compileShader("vertex",   vertexSrc,   GL_VERTEX_SHADER);
            int fId = compileShader("fragment", fragmentSrc, GL_FRAGMENT_SHADER);

            glAttachShader(programId, vId);
            glAttachShader(programId, fId);
            glLinkProgram(programId);
            checkProgramLinkStatus();

            // Shaders no longer needed after linking
            glDeleteShader(vId);
            glDeleteShader(fId);

        } catch (Exception e) {
            throw new RuntimeException("Shader [" + name + "] compilation failed: " + e.getMessage());
        }
    }

    private int compileShader(String stageLabel, ShaderSource src, int glType) {
        int id = glCreateShader(glType);
        glShaderSource(id, src.getSource());
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(id);
            throw new RuntimeException(
                    "Stage '" + stageLabel + "' failed to compile:\n" + log);
        }
        return id;
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

        public ShaderBuilder vertexFromResource(String path) {
            this.vertexShader = Optional.of(ShaderSource.fromResource(path));
            return this;
        }

        public ShaderBuilder fragmentFromResource(String path) {
            this.fragmentShader = Optional.of(ShaderSource.fromResource(path));
            return this;
        }

        public Shader build() {
            ShaderSource vertex = vertexShader.orElseThrow(() -> 
                new IllegalStateException("Vertex shader source not provided, Shader: [" + name + "]"));
            ShaderSource fragment = fragmentShader.orElseThrow(() -> 
                new IllegalStateException("Fragment shader source not provided, Shader: [" + name + "]"));
            return new Shader(name, vertex, fragment);
        }
    }
}