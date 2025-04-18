package ai.electric_dreams.fire_drake.gfx;

import ai.electric_dreams.fire_drake.gfx.mesh.EasyMesh;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public final class PsxForwardRenderer implements Renderer {
    private Shader psxShader;
    private Shader postShader;
    private int lowResFbo, colorTex, depthRb;
    private Matrix4f view = new Matrix4f(), proj = new Matrix4f();
    private List<DrawCmd> drawQueue = new ArrayList<>();
    private long window;

    private static class DrawCmd {
        final Mesh mesh;
        final Material mat;
        final Matrix4f model;
        final int instanceCount;

        DrawCmd(Mesh mesh, Material mat, Matrix4f model) {
            this(mesh, mat, model, 1);
        }

        DrawCmd(Mesh mesh, Material mat, Matrix4f model, int instanceCount) {
            this.mesh = mesh;
            this.mat = mat;
            this.model = model;
            this.instanceCount = instanceCount;
        }

        void bind() {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, mat.textureId());
            Debug.glCheckError("DrawCmd.bind");
        }
    }

    @Override
    public void init(long win) {
        this.window = win;

        // Set up framebuffer resize callback
        glfwSetFramebufferSizeCallback(window, (_, width, height) -> {
            glViewport(0, 0, width, height);
        });


        psxShader  = Shader.builder("psx")
                .vertexFromResource("shaders/psx.vert")
                .fragmentFromResource("shaders/psx.frag")
                .build();
    }

    @Override
    public void beginFrame() {
        // TODO: update view and proj

        drawQueue.clear();
        glBindFramebuffer(GL_FRAMEBUFFER, lowResFbo);
        Debug.glCheckError("PsxForwardRenderer.beginFrame - bind framebuffer");
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Debug.glCheckError("PsxForwardRenderer.beginFrame - clear");

        psxShader.bind();
        Debug.glCheckError("PsxForwardRenderer.beginFrame - bind psx shader");
    }

    @Override
    public void draw(Mesh mesh, Material mat, Matrix4f model) {
        drawQueue.add(new DrawCmd(mesh, mat, new Matrix4f(model)));
    }

    @Override
    public void endFrame() {

        psxShader.setMatrix4f("view", view);
        psxShader.setMatrix4f("projection", proj);
        Debug.glCheckError("PsxForwardRenderer.endFrame - set matrices");
        
        for (DrawCmd cmd : drawQueue) {
            cmd.bind();
            psxShader.setMatrix4f("model", cmd.model);
            psxShader.setInt("texture_diffuse1", 0);
            cmd.mesh.draw(psxShader);
            Debug.glCheckError("PsxForwardRenderer.endFrame - draw mesh");
        }
        psxShader.unbind();
        Debug.glCheckError("PsxForwardRenderer.endFrame - unbind psx shader");

        // TODO: set up post-processing work
        // post‑process to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Debug.glCheckError("PsxForwardRenderer.endFrame - bind default framebuffer");
        
    }

    @Override 
    public void destroy() { 
        psxShader.cleanup(); 
//        postShader.cleanup();
        glDeleteFramebuffers(lowResFbo);
        glDeleteTextures(colorTex);
        glDeleteRenderbuffers(depthRb);
        Debug.glCheckError("PsxForwardRenderer.destroy");
    }

    @Override
    public void draw(Mesh testTriangle) {
        testTriangle.draw(psxShader);
    }
}
