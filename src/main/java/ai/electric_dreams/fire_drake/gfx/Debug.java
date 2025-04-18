package ai.electric_dreams.fire_drake.gfx;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public class Debug {

    public static void glCheckError(String where) {
        for (int err = glGetError(); err != GL_NO_ERROR; err = glGetError()) {
            System.err.printf("GL error 0x%X at %s%n", err, where);
        }
    }
}
