package renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

class ShaderTest {
    private static long window;
    
    private static final String BASIC_VERTEX_SHADER = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            
            uniform mat4 model;
            uniform mat4 view;
            uniform mat4 projection;
            
            void main() {
                gl_Position = projection * view * model * vec4(aPos, 1.0);
            }
            """;
            
    private static final String BASIC_FRAGMENT_SHADER = """
            #version 330 core
            out vec4 FragColor;
            
            uniform vec3 color;
            uniform float alpha;
            
            void main() {
                FragColor = vec4(color, alpha);
            }
            """;

    @BeforeAll
    static void setUp() {
        assumeFalse(System.getProperty("os.name").toLowerCase().contains("mac"),
                "Skipping ShaderTest on macOS because of context-on-thread-0 requirement");



        // Initialize GLFW and create a window
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }
        
        // Configure GLFW
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        }
        
        // Create window
        window = glfwCreateWindow(100, 100, "Test Window", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create GLFW window");
        }
        
        // Make OpenGL context current
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
    }
    
    @AfterAll
    static void tearDown() {
        glfwDestroyWindow(window);
        glfwTerminate();
    }
    
    @Test
    void testShaderCompilation() {
        assertDoesNotThrow(() -> {
            Shader shader = Shader.builder("test")
                .vertexFromMemory(BASIC_VERTEX_SHADER)
                .fragmentFromMemory(BASIC_FRAGMENT_SHADER)
                .build();
            shader.cleanup();
        });
    }
    
    @Test
    void testUniformSetters() {
        Shader shader = Shader.builder("test")
            .vertexFromMemory(BASIC_VERTEX_SHADER)
            .fragmentFromMemory(BASIC_FRAGMENT_SHADER)
            .build();
            
        shader.bind();
        
        // Test setting uniforms
        assertDoesNotThrow(() -> {
            shader.setVector3f("color", new Vector3f(1.0f, 0.0f, 0.0f));
            shader.setFloat("alpha", 0.5f);
            shader.setMatrix4f("model", new Matrix4f().identity());
            shader.setMatrix4f("view", new Matrix4f().identity());
            shader.setMatrix4f("projection", new Matrix4f().identity());
        });
        
        // Verify uniform locations are cached
        int colorLocation = glGetUniformLocation(shader.getProgramId(), "color");
        assertTrue(colorLocation >= 0, "Color uniform location should be valid");
        
        shader.cleanup();
    }
    
    @Test
    void testInvalidShaderCompilation() {
        String invalidShader = "#version 330 core\nvoid main() { invalid code }";
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            Shader shader = Shader.builder("test")
                .vertexFromMemory(invalidShader)
                .fragmentFromMemory(BASIC_FRAGMENT_SHADER)
                .build();
        });
        
        assertTrue(exception.getMessage().contains("compilation failed"));
    }
    
    @Test
    void testMissingUniforms() {
        Shader shader = Shader.builder("test")
            .vertexFromMemory(BASIC_VERTEX_SHADER)
            .fragmentFromMemory(BASIC_FRAGMENT_SHADER)
            .build();
            
        shader.bind();
        
        // Setting a non-existent uniform should not throw but should return -1 for location
        shader.setFloat("nonexistent", 1.0f);
        assertEquals(-1, glGetUniformLocation(shader.getProgramId(), "nonexistent"));
        
        shader.cleanup();
    }
} 