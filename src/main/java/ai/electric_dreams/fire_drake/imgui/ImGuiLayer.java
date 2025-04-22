package ai.electric_dreams.fire_drake.imgui;

import ai.electric_dreams.fire_drake.gfx.Debug;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Manages ImGui initialization, frame setup, and rendering.
 */
public final class ImGuiLayer {
    private static final Logger log = LoggerFactory.getLogger(ImGuiLayer.class);

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private boolean initialized = false;
    private Optional<Long> windowHandle = Optional.empty();

    /**
     * Initializes ImGui for the specified window.
     * 
     * @param window The window handle
     * @throws IllegalStateException if ImGui has already been initialized
     */
    public void init(long window) {
        if (initialized) {
            throw new IllegalStateException("ImGui already initialized");
        }

        windowHandle = Optional.of(window);
        
        // Create ImGui context
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setConfigFlags(ImGuiConfigFlags.ViewportsEnable | ImGuiConfigFlags.DockingEnable);

        // Initialize ImGui GLFW and GL3 implementations
        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 330");

        initialized = true;
        log.info("ImGui context initialized");
    }

    /**
     * Prepares ImGui for a new frame.
     * 
     * @throws IllegalStateException if ImGui has not been initialized
     */
    public void newFrame() {
        ensureInitialized();
        
        imGuiGlfw.newFrame();
        imGuiGl3.newFrame();
        ImGui.newFrame();
    }

    /**
     * Renders ImGui draw data.
     * 
     * @throws IllegalStateException if ImGui has not been initialized
     */
    public void render() {
        ensureInitialized();
        
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
        Debug.glCheckError("ImGuiLayer.render - render draw data");

        // Update and render platform windows (multi-viewport support)
        ImGuiIO io = ImGui.getIO();
        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            long backupWindowPtr = windowHandle.get();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            org.lwjgl.glfw.GLFW.glfwMakeContextCurrent(backupWindowPtr);
            Debug.glCheckError("ImGuiLayer.render - render platform windows");
        }
    }

    /**
     * Disposes of ImGui resources.
     */
    public void dispose() {
        if (initialized) {
            // No explicit disposal method for ImGui implementations
            // Just destroy the ImGui context
            ImGui.destroyContext();
            initialized = false;
            windowHandle = Optional.empty();
            log.info("ImGui context disposed");
        }
    }

    /**
     * Ensures that ImGui has been initialized.
     * 
     * @throws IllegalStateException if ImGui has not been initialized
     */
    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("ImGui not initialized - call init() first");
        }
    }
} 