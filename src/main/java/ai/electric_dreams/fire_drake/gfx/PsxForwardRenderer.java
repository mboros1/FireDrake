package ai.electric_dreams.fire_drake.gfx;

import org.joml.Matrix4f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public final class PsxForwardRenderer implements Renderer {
    private Shader psxShader;
    private Shader postShader;
    private int lowResFbo, colorTex, depthRb;
    private Matrix4f view = new Matrix4f(), proj = new Matrix4f();
    private List<DrawCmd> drawQueue = new ArrayList<>();

    @Override
    public void init(long win) {
        // --- low‑res FBO ---
        colorTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, colorTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 320, 240, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, 0);

        depthRb = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthRb);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, 320, 240);

        lowResFbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, lowResFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D, colorTex, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER, depthRb);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        psxShader  = Shader.builder("psx")
                .vertexFromFile("shaders/psx.vert")
                .fragmentFromFile("shaders/psx.frag")
                .build();
        postShader = Shader.builder("post")
                .vertexFromMemory(fullscreenVert())
                .fragmentFromFile("shaders/post_dither.frag")
                .build();
    }

    @Override
    public void beginFrame() {
        drawQueue.clear();
        glBindFramebuffer(GL_FRAMEBUFFER, lowResFbo);
        glViewport(0, 0, 320, 240);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void draw(Mesh mesh, Material mat, Matrix4f model) {
        drawQueue.add(new DrawCmd(mesh, mat, new Matrix4f(model)));
    }

    @Override
    public void endFrame() {
        psxShader.bind();
        for (DrawCmd cmd : drawQueue) {
            cmd.bind();
            psxShader.setMatrix4f("model", cmd.model);
            cmd.mesh.drawInstanced(cmd.instanceCount);
        }
        psxShader.unbind();

        // post‑process to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        int w = GLFW.glfwGetFramebufferSizeX(window);
        int h = GLFW.glfwGetFramebufferSizeY(window);
        glViewport(0, 0, w, h);
        glDisable(GL_DEPTH_TEST);

        postShader.bind();
        glBindTexture(GL_TEXTURE_2D, colorTex);
        fullscreenVao.draw();   // a single triangle or quad
        postShader.unbind();
    }

    @Override public void resize(int w, int h) {/* nothing; low‑res stays fixed */}
    @Override public void destroy() { psxShader.cleanup(); postShader.cleanup(); }
}
