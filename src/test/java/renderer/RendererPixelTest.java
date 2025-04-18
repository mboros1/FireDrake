package renderer;

import ai.electric_dreams.fire_drake.gfx.Material;
import ai.electric_dreams.fire_drake.gfx.Mesh;
import ai.electric_dreams.fire_drake.gfx.MeshFactory;
import ai.electric_dreams.fire_drake.gfx.PsxForwardRenderer;
import org.joml.Matrix4f;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import util.GpuTestUtil;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

@DisabledOnOs(OS.MAC)   // macOS needs context on thread‑0; skip if troublesome
@TestInstance(PER_CLASS)
class RendererPixelTest {

    private PsxForwardRenderer renderer;
    private Mesh quad;
    private Material redMat;

    @BeforeAll
    void setup() {
        GpuTestUtil.initGlfw();

        renderer = new PsxForwardRenderer();
        renderer.init(GLFW.glfwGetCurrentContext());   // passes window handle

        // 1×1 red texture
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        ByteBuffer red = BufferUtils.createByteBuffer(4).put(new byte[]{ (byte)255,0,0,(byte)255 });
        red.flip();
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,1,1,0,GL_RGBA,GL_UNSIGNED_BYTE,red);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        redMat = new Material(tex);

        quad = MeshFactory.fullscreenQuad();   // two triangles covering NDC
    }

    @AfterAll
    void tearDown() {
        renderer.destroy();
        GpuTestUtil.shutdown();
    }

    @Test
    void redQuadProducesRedPixel() {
        renderer.beginFrame();
        renderer.draw(quad, redMat, new Matrix4f().identity());
        renderer.endFrame();

        // read centre pixel of low‑res FBO (320×240) → redMat is solid red
        ByteBuffer pixel = BufferUtils.createByteBuffer(4);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);           // default FB after endFrame up‑scale
        glReadPixels(2, 2, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
        int r = pixel.get(0) & 0xFF;
        int g = pixel.get(1) & 0xFF;

        assertAll(
                () -> assertTrue(r >= 250, "R channel should be ~255"),
                () -> assertTrue(g <=   5, "G channel near 0")
        );
    }
}

