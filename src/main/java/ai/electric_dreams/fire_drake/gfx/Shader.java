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
        Debug.glCheckError("Shader.constructor - create program: " + name);
        this.matrixBuffer = BufferUtils.createFloatBuffer(16);

        try {
            int vId = compileShader("vertex",   vertexSrc,   GL_VERTEX_SHADER);
            int fId = compileShader("fragment", fragmentSrc, GL_FRAGMENT_SHADER);

            glAttachShader(programId, vId);
            Debug.glCheckError("Shader.constructor - attach vertex shader: " + name);
            glAttachShader(programId, fId);
            Debug.glCheckError("Shader.constructor - attach fragment shader: " + name);
            glLinkProgram(programId);
            Debug.glCheckError("Shader.constructor - link program: " + name);
            checkProgramLinkStatus();

            // Shaders no longer needed after linking
            glDeleteShader(vId);
            Debug.glCheckError("Shader.constructor - delete vertex shader: " + name);
            glDeleteShader(fId);
            Debug.glCheckError("Shader.constructor - delete fragment shader: " + name);

        } catch (Exception e) {
            throw new RuntimeException("Shader [" + name + "] compilation failed: " + e.getMessage());
        }
    }

    private int compileShader(String stageLabel, ShaderSource src, int glType) {
        int id = glCreateShader(glType);
        Debug.glCheckError("Shader.compileShader - create shader: " + stageLabel);
        glShaderSource(id, src.getSource());
        Debug.glCheckError("Shader.compileShader - set source: " + stageLabel);
        glCompileShader(id);
        Debug.glCheckError("Shader.compileShader - compile: " + stageLabel);

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
        Debug.glCheckError("Shader.bind: " + name);
    }

    public void unbind() {
        glUseProgram(0);
        Debug.glCheckError("Shader.unbind");
    }

    public int getProgramId() {
        return programId;
    }

    public void setFloat(String name, float value) {
        int location = getUniformLocation(name);
        if (location != -1) {
            glUniform1f(location, value);
            Debug.glCheckError("Shader.setFloat: " + name);
        }
    }

    public void setInt(String name, int value) {
        int location = getUniformLocation(name);
        if (location != -1) {
            glUniform1i(location, value);
            Debug.glCheckError("Shader.setInt: " + name);
        }
    }

    public void setVector3f(String name, Vector3f value) {
        int location = getUniformLocation(name);
        if (location != -1) {
            glUniform3f(location, value.x, value.y, value.z);
            Debug.glCheckError("Shader.setVector3f: " + name);
        }
    }

    public void setMatrix4f(String name, Matrix4f matrix) {
        int location = getUniformLocation(name);
        if (location != -1) {
            matrix.get(matrixBuffer);
            glUniformMatrix4fv(location, false, matrixBuffer);
            Debug.glCheckError("Shader.setMatrix4f: " + name);
        }
    }

    private int getUniformLocation(String name) {
        return uniformLocationCache.computeIfAbsent(name, n -> glGetUniformLocation(programId, n));
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
            Debug.glCheckError("Shader.cleanup: " + name);
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