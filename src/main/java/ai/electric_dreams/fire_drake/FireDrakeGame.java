package ai.electric_dreams.fire_drake;

import ai.electric_dreams.fire_drake.gfx.*;
import ai.electric_dreams.fire_drake.gfx.mesh.EasyMesh;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class FireDrakeGame {
	private static final Logger logger = LoggerFactory.getLogger(FireDrakeGame.class);

	private ImGuiImplGlfw imGuiGlfw;
	private ImGuiImplGl3 imGuiGl3;
	private boolean showPopupWindow = false;
	private Renderer renderer;
	private World world;

    private Material redMat;



	public static void main(String[] args) {
		logger.info("Welcome to Fire Drake!");

		new Thread(() -> {
			SpringApplication app = new SpringApplication(FireDaemon.class);
			app.setWebApplicationType(WebApplicationType.NONE);
			app.run(args);
		}).start();

		logger.info("Launching Game Loop");
		new FireDrakeGame().run();
	}

	// The window handle
	private long window;

	public void run() {
        init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		ImGui.destroyContext();
	}

	void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
		glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);

		// Create the window
		window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
					window,
					(vidmode.width() - pWidth.get(0)) / 2,
					(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);

		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}


	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		Debug.glCheckError("FireDrakeGame.loop - create capabilities");
		
		renderer = new PsxForwardRenderer();
		renderer.init(window);
		
		// Initialize the game world
		world = new World();

		// -- Test red material setup (copied from your test) --
		int tex = glGenTextures();
		Debug.glCheckError("FireDrakeGame.loop - gen texture");
		
		glBindTexture(GL_TEXTURE_2D, tex);
		Debug.glCheckError("FireDrakeGame.loop - bind texture");
		
		ByteBuffer red = BufferUtils.createByteBuffer(4).put(new byte[]{ (byte)255, 0, 0, (byte)255 });
		red.flip();
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, red);
		Debug.glCheckError("FireDrakeGame.loop - texImage2D");
		
		// Add all necessary texture parameters
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		Debug.glCheckError("FireDrakeGame.loop - texParameteri");
		
		redMat = new Material(tex);

		// Fullscreen quad
        Mesh testQuad = DefaultDebugMeshes.fullscreenQuad();

		glEnable(GL_DEPTH_TEST);
		Debug.glCheckError("FireDrakeGame.loop - enable depth test");

		// Set the clear color
		glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
		Debug.glCheckError("FireDrakeGame.loop - clear color");

		// Init ImGui context
		ImGui.createContext();
		ImGuiIO io = ImGui.getIO();
		io.setConfigFlags(ImGuiConfigFlags.ViewportsEnable | ImGuiConfigFlags.DockingEnable);

		imGuiGlfw = new ImGuiImplGlfw();
		imGuiGlfw.init(window, true);
		imGuiGl3  = new ImGuiImplGl3();
		imGuiGl3.init("#version 330");


		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			glfwPollEvents();

			renderer.beginFrame();

			/* === game world draw calls === */
			for (var e : world.visible()) {
				renderer.draw(e.mesh(), e.material(), e.transform());
			}
			// TEMPORARY: show red quad
			renderer.draw(testQuad, redMat, new Matrix4f().identity());

			/* === end draw === */

			renderer.endFrame();

			imGuiGlfw.newFrame();
			imGuiGl3.newFrame();
			ImGui.newFrame();

			ImGui.begin("Main HUD");
			if (ImGui.button("Open Window")) {
				showPopupWindow = true;
			}
			ImGui.end();

			if (showPopupWindow) {
				ImGui.begin("Poppable Window", ImGuiWindowFlags.None);
				ImGui.text("Here's a floating ImGui window.");
				if (ImGui.button("Close")) {
					showPopupWindow = false;
				}
				ImGui.end();
			}


			// Render everything
			ImGui.render();
			// glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			imGuiGl3.renderDrawData(ImGui.getDrawData());
			Debug.glCheckError("FireDrakeGame.loop - render ImGui");

			// Render ImGui platform windows (multi-monitor)
			if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
				final long backupWindow = glfwGetCurrentContext();
				ImGui.updatePlatformWindows();
				ImGui.renderPlatformWindowsDefault();
				glfwMakeContextCurrent(backupWindow);
				Debug.glCheckError("FireDrakeGame.loop - render ImGui viewports");
			}

			glfwSwapBuffers(window);
			Debug.glCheckError("FireDrakeGame.loop - swap buffers");
		}
	}
}

