package ai.electric_dreams.fire_drake;

import ai.electric_dreams.fire_drake.gfx.*;
import ai.electric_dreams.fire_drake.gfx.mesh.EasyMesh;
import ai.electric_dreams.fire_drake.gfx.mesh.ObjMesh;
import ai.electric_dreams.fire_drake.gl.OpenGlContext;
import ai.electric_dreams.fire_drake.imgui.ImGuiLayer;
import ai.electric_dreams.fire_drake.window.GlfwWindow;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

public class FireDrakeGame {
	private static final Logger logger = LoggerFactory.getLogger(FireDrakeGame.class);

	// Window, OpenGL and ImGui subsystems
	private final GlfwWindow windowSystem = new GlfwWindow();
	private final OpenGlContext glContext = new OpenGlContext();
	private final ImGuiLayer imguiLayer = new ImGuiLayer();

	// Rendering and game state
	private Renderer renderer;
	private World world;
	private boolean showPopupWindow = false;
	private ObjMesh dragonMesh;

	// Camera controls
	private boolean leftMouseDown = false;
	private double lastMouseX = -1;
	private double lastMouseY = -1;
	private float yaw = 0.0f;
	private float pitch = 0.0f;
	private float zoom = 5.0f; // distance from camera to target

	public static void main(String[] args) {
		logger.info("Welcome to Fire Drake!");

		// Start the daemon in a separate thread
		new Thread(() -> {
			SpringApplication app = new SpringApplication(FireDaemon.class);
			app.setWebApplicationType(WebApplicationType.NONE);
			app.run(args);
		}).start();

		logger.info("Launching Game Loop");
		new FireDrakeGame().run();
	}

	public void run() {
		try {
			init();
			loop();
		} finally {
			// Clean up resources - regardless of how we exit
			cleanup();
		}
	}

	void init() {
		// Initialize GLFW
		GlfwWindow.initGlfw();

		// Configure and create the window
		windowSystem.setMaximized(true);
		long windowHandle = windowSystem.create("Fire Drake", 1280, 720);

		// Initialize OpenGL
		glContext.init(windowHandle);

		// Initialize ImGui
		imguiLayer.init(windowHandle);

		// Initialize game world
		world = new World();

		dragonMesh = new ObjMesh("fire_drake.obj");
		dragonMesh.reportObjStats();
		Matrix4f model = new Matrix4f()
				.translate(0, -1, -5)   // move it down 1 unit and back 5 units
				.scale(0.1f);           // shrink it to 10%
		var dragonEntity = new Entity(dragonMesh, null, model);
		world.addEntity(dragonEntity);

		var triangleMesh = DefaultDebugMeshes.defaultTriangle();
		var triangleEntity = new Entity(triangleMesh, null, new Matrix4f().identity());
		world.addEntity(triangleEntity);

		initMouseCallbacks(windowHandle);

		// Make the window visible
		windowSystem.show();
	}

	private void initMouseCallbacks(long windowHandle) {

		GLFW.glfwSetMouseButtonCallback(windowHandle, (win, button, action, mods) -> {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
				leftMouseDown = (action == GLFW.GLFW_PRESS);
			}
		});
		GLFW.glfwSetCursorPosCallback(windowHandle, (win, xpos, ypos) -> {
			if (!leftMouseDown) {
				lastMouseX = xpos;
				lastMouseY = ypos;
				return;
			}

			double dx = xpos - lastMouseX;
			double dy = ypos - lastMouseY;

			// Adjust sensitivity as needed
			yaw   += dx * 0.1f;
			pitch += dy * 0.1f;
			pitch = Math.max(-89.9f, Math.min(89.9f, pitch)); // Clamp pitch

			lastMouseX = xpos;
			lastMouseY = ypos;
		});

		GLFW.glfwSetScrollCallback(windowHandle, (win, xoffset, yoffset) -> {
			zoom -= yoffset * 0.5f;
			zoom = Math.max(1.0f, Math.min(zoom, 50.0f)); // Clamp zoom
		});

	}

	private void loop() {
		// Get window handle
		long windowHandle = windowSystem.handle();

		// Initialize renderer
		renderer = new PsxForwardRenderer();
		renderer.init(windowHandle);

		// Get a test mesh for rendering
		 var testTriangle = DefaultDebugMeshes.defaultTriangle();

		// Main loop
		while (!windowSystem.shouldClose()) {
			org.lwjgl.glfw.GLFW.glfwPollEvents();
			float radius = zoom;
			float yawRad = (float)Math.toRadians(yaw);
			float pitchRad = (float)Math.toRadians(pitch);
			renderer.gameToCameraUpdates(radius, yawRad, pitchRad);


			// Clear the backbuffer
			glContext.clearBuffers();

			// Render game world
			renderer.beginFrame();
			for (var e : world.visible()) {
				renderer.draw(e.mesh(), e.material(), e.transform());
			}
			// TEMPORARY: show test triangle
			 renderer.draw(testTriangle);
			renderer.endFrame();

			// ImGui rendering
			imguiLayer.newFrame();
			
			// Main HUD window
			ImGui.begin("Main HUD");
			if (ImGui.button("Open Window")) {
				showPopupWindow = true;
			}


// 3) pull whatever stats you want
			ImGui.text(String.format("FPS: %.1f", ImGui.getIO().getFramerate()));
			ImGui.text(String.format("Frame Time: %.2f ms", 1000.0f / ImGui.getIO().getFramerate()));
			ImGui.separator();

// your tracked framebuffer size
			ImGui.text("Framebuffer: " + renderer.getFbWidth() + "×" + renderer.getFbHeight());
			ImGui.text(String.format("Aspect: %.3f", (float)renderer.getFbWidth() / renderer.getFbHeight()));
			ImGui.separator();

// camera/view/proj
			float[] m = new float[16];
			renderer.getView().get(m);
			ImGui.text("View Matrix:");
			for (int row = 0; row < 4; row++) {
				ImGui.text(String.format(
						"%6.2f %6.2f %6.2f %6.2f",
						m[row*4+0], m[row*4+1], m[row*4+2], m[row*4+3]
				));
			}
			ImGui.separator();

			renderer.getProj().get(m);
			ImGui.text("Proj Matrix:");
			for (int row = 0; row < 4; row++) {
				ImGui.text(String.format(
						"%6.2f %6.2f %6.2f %6.2f",
						m[row*4+0], m[row*4+1], m[row*4+2], m[row*4+3]
				));
			}
			ImGui.separator();

			ImGui.end();

			// Popup window if enabled
			if (showPopupWindow) {
				ImGui.begin("Poppable Window", ImGuiWindowFlags.None);
				ImGui.text("Here's a floating ImGui window.");
				if (ImGui.button("Close")) {
					showPopupWindow = false;
				}
				ImGui.end();
			}

			// Render ImGui
			imguiLayer.render();

			// Swap buffers
			windowSystem.swapBuffers();
		}
	}

	private void cleanup() {
		if (renderer != null) {
			renderer.destroy();
		}
		
		imguiLayer.dispose();
		windowSystem.destroy();
		GlfwWindow.terminateGlfw();
		
		logger.info("Fire Drake shutdown complete");
	}
}

