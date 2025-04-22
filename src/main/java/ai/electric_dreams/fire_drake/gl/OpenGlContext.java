package ai.electric_dreams.fire_drake.gl;

import ai.electric_dreams.fire_drake.gfx.Debug;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Initializes GL on the thread that owns the window and keeps the capabilities
 * so they can be queried later if needed.
 */
public final class OpenGlContext {
    private static final Logger log = LoggerFactory.getLogger(OpenGlContext.class);

    private Optional<GLCapabilities> caps = Optional.empty();
    private boolean vsyncEnabled = true;

    /**
     * Initializes the OpenGL context for the specified window.
     * 
     * @param window The window handle
     * @return The created GL capabilities
     * @throws IllegalStateException if the context has already been initialized
     */
    public GLCapabilities init(long window) {
        if (caps.isPresent()) {
            throw new IllegalStateException("OpenGL context already initialized");
        }
        
        glfwMakeContextCurrent(window);
        GLCapabilities capabilities = GL.createCapabilities();
        
        // Enable v-sync by default
        glfwSwapInterval(vsyncEnabled ? 1 : 0);
        
        caps = Optional.of(capabilities);
        
        // Check for OpenGL 3.3 support
        if (!capabilities.OpenGL33) {
            log.warn("OpenGL 3.3 not available on this machine, some features may not work");
        }
        
        // Set default clear color
        glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
        
        // Get OpenGL version string
        String versionString = glGetString(GL_VERSION);
        log.info("OpenGL context initialized: {}", versionString);

        // in init():
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);   // or GL_LESS, whichever you prefer

        Debug.glCheckError("OpenGlContext.init");
        return capabilities;
    }

    /**
     * Gets the current OpenGL capabilities.
     * 
     * @return The current capabilities
     * @throws IllegalStateException if the context has not been initialized
     */
    public GLCapabilities capabilities() {
        return caps.orElseThrow(
            () -> new IllegalStateException("OpenGL context not yet initialized"));
    }
    
    /**
     * Sets whether vertical synchronization is enabled.
     * 
     * @param enabled true to enable v-sync, false to disable
     */
    public void setVsync(boolean enabled) {
        if (caps.isPresent()) {
            glfwSwapInterval(enabled ? 1 : 0);
            log.info("V-sync {}", enabled ? "enabled" : "disabled");
        }
        this.vsyncEnabled = enabled;
    }
    
    /**
     * Checks if vertical synchronization is enabled.
     * 
     * @return true if v-sync is enabled
     */
    public boolean isVsyncEnabled() {
        return vsyncEnabled;
    }
    
    /**
     * Clears the color and depth buffers.
     */
    public void clearBuffers() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Debug.glCheckError("OpenGlContext.clearBuffers");
    }
} 