package ai.electric_dreams.fire_drake.gfx;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public final class PsxForwardRenderer implements Renderer {
    private Shader psxShader;
    private Shader postShader;
    private int lowResFbo, colorTex, depthRb;
    private Matrix4f view = new Matrix4f(), proj = new Matrix4f();
    private List<DrawCmd> drawQueue = new ArrayList<>();
    private long window;
    private FullscreenQuad fullscreenVao;

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
        }
    }

    private static class FullscreenQuad {
        private int vao;
        private int vbo;

        public FullscreenQuad() {
            // Create a VAO for a fullscreen quad (two triangles)
            vao = glGenVertexArrays();
            vbo = glGenBuffers();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);

            // A single quad made of two triangles (6 vertices)
            float[] vertices = {
                -1.0f, -1.0f, 0.0f, 0.0f,
                 1.0f, -1.0f, 1.0f, 0.0f,
                 1.0f,  1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 0.0f, 0.0f,
                 1.0f,  1.0f, 1.0f, 1.0f,
                -1.0f,  1.0f, 0.0f, 1.0f
            };

            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

            // Position attribute
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);
            // Texture coordinate attribute
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
            glEnableVertexAttribArray(1);

            glBindVertexArray(0);
        }

        public void draw() {
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);
        }

        public void cleanup() {
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
        }
    }

    private String fullscreenVert() {
        return "#version 330 core\n" +
               "layout (location = 0) in vec2 aPos;\n" +
               "layout (location = 1) in vec2 aTexCoord;\n" +
               "out vec2 TexCoord;\n" +
               "void main() {\n" +
               "    gl_Position = vec4(aPos, 0.0, 1.0);\n" +
               "    TexCoord = aTexCoord;\n" +
               "}\n";
    }

    @Override
    public void init(long win) {
        this.window = win;
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
                
        fullscreenVao = new FullscreenQuad();
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
        psxShader.setMatrix4f("view", view);
        psxShader.setMatrix4f("projection", proj);
        
        for (DrawCmd cmd : drawQueue) {
            cmd.bind();
            psxShader.setMatrix4f("model", cmd.model);
            psxShader.setInt("texture_diffuse1", 0);
            cmd.mesh.draw(psxShader);
        }
        psxShader.unbind();

        // post‑process to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(window, width, height);
        glViewport(0, 0, width[0], height[0]);
        glDisable(GL_DEPTH_TEST);

        postShader.bind();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, colorTex);
        postShader.setInt("screenTexture", 0);
        fullscreenVao.draw();
        postShader.unbind();
        
        glEnable(GL_DEPTH_TEST);
    }

    @Override 
    public void resize(int w, int h) {
        /* nothing; low‑res stays fixed */
        // Update projection matrix if needed
        proj.identity().perspective((float) Math.toRadians(70.0f), 
                                   (float) w / (float) h, 
                                   0.1f, 1000.0f);
    }
    
    @Override 
    public void destroy() { 
        psxShader.cleanup(); 
        postShader.cleanup();
        fullscreenVao.cleanup();
        glDeleteFramebuffers(lowResFbo);
        glDeleteTextures(colorTex);
        glDeleteRenderbuffers(depthRb);
    }
}
