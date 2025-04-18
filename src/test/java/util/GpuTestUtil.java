package util;

import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

// src/test/java/util/GpuTestUtil.java
public final class GpuTestUtil {
    private static long window;

    public static void initGlfw() {
        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        window = glfwCreateWindow(2, 2, "", NULL, NULL);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
    }
    public static void shutdown() {
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}
