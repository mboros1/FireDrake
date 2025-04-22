package ai.electric_dreams.fire_drake.window;

import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Creates and owns a GLFW window handle.
 */
public final class GlfwWindow {
    private static final Logger log = LoggerFactory.getLogger(GlfwWindow.class);

    private Optional<Long> handle = Optional.empty();
    private boolean maximized = false;

    /**
     * Initializes GLFW library. Must be called before any other GLFW operations.
     */
    public static void initGlfw() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        log.info("GLFW initialized");
    }

    /**
     * Terminates GLFW and frees the error callback.
     */
    public static void terminateGlfw() {
        glfwTerminate();
        glfwSetErrorCallback(null).free();
        log.info("GLFW terminated");
    }

    /**
     * Creates a GLFW window with the specified title and dimensions.
     * 
     * @param title Window title
     * @param width Initial width
     * @param height Initial height
     * @return Window handle
     */
    public long create(String title, int width, int height) {
        handle.ifPresent(hnd -> {
            throw new IllegalStateException("Window already created: " + hnd);
        });

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, maximized ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);

        // Create the window
        final long newHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (newHandle == NULL) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }
        handle = Optional.of(newHandle);
        
        // Center the window on screen
        centerOnScreen();
        
        // Add a default key callback for ESC key
        glfwSetKeyCallback(newHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        log.info("GLFW window created: {}", newHandle);
        return newHandle;
    }

    /**
     * Centers the window on the primary monitor.
     */
    public void centerOnScreen() {
        long windowHandle = handle();
        
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            // Get the window size
            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                windowHandle,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            );
        }
    }

    /**
     * Sets whether the window should be maximized when created.
     * Must be called before create().
     * 
     * @param maximized true to maximize the window
     */
    public void setMaximized(boolean maximized) {
        if (handle.isPresent()) {
            throw new IllegalStateException("Cannot set maximized after window is created");
        }
        this.maximized = maximized;
    }

    /**
     * Shows the window.
     */
    public void show() {
        glfwShowWindow(handle());
    }

    /**
     * Returns the window handle.
     * 
     * @return The window handle
     * @throws IllegalStateException if the window has not been created
     */
    public long handle() {
        return handle.orElseThrow(
            () -> new IllegalStateException("Window not yet created – call create() first"));
    }

    /**
     * Frees the window callbacks and destroys the window.
     */
    public void destroy() {
        handle.ifPresent(h -> {
            glfwFreeCallbacks(h);
            glfwDestroyWindow(h);
        });
        handle = Optional.empty();
        log.info("GLFW window destroyed");
    }

    /**
     * Checks if the window should close.
     * 
     * @return true if the window should close
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(handle());
    }

    /**
     * Swaps the front and back buffers.
     */
    public void swapBuffers() {
        glfwSwapBuffers(handle());
    }
} 