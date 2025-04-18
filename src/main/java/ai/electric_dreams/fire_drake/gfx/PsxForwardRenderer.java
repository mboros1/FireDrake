package ai.electric_dreams.fire_drake.gfx;

import ai.electric_dreams.fire_drake.gfx.mesh.EasyMesh;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

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

    private static class Triangle {
        int vaoId;
        int vboId;
        int eboId;

        public static Triangle generateDefault() {
            Triangle triangle = new Triangle();
            float[] vertices = {
                    // x, y, z
                    0.5f,  0.5f, 0.0f, // top right
                    0.5f, -0.5f, 0.0f, // bottom right
                    -0.5f, -0.5f, 0.0f, // bottom left
                    -0.5f,  0.5f, 0.0f  // top left
            };

            int[] indices = {
                    0, 1, 3, // first triangle
                    1, 2, 3  // second triangle
            };

// --- VAO ---
            triangle.vaoId = glGenVertexArrays();
            glBindVertexArray(triangle.vaoId);

// --- VBO ---
            triangle.vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, triangle.vboId);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

// --- EBO (optional, only if using indices) ---
            triangle.eboId = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triangle.eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

// --- Attribute Pointer for aPos at location 0 ---
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

// --- Unbind (optional safety) ---
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

            return triangle;
        }

    }

    private static class FullscreenQuad {
        private int vao;
        private int vbo;

        public FullscreenQuad() {
            // Create a VAO for a fullscreen quad (two triangles)
            vao = glGenVertexArrays();
            vbo = glGenBuffers();
            Debug.glCheckError("FullscreenQuad.init - gen buffers");

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            Debug.glCheckError("FullscreenQuad.init - bind buffers");

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
            Debug.glCheckError("FullscreenQuad.init - buffer data");

            // Position attribute
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);
            // Texture coordinate attribute
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
            glEnableVertexAttribArray(1);
            Debug.glCheckError("FullscreenQuad.init - set attributes");

            glBindVertexArray(0);
            Debug.glCheckError("FullscreenQuad.init - unbind VAO");
        }

        public void draw() {
            glBindVertexArray(vao);
            Debug.glCheckError("FullscreenQuad.draw - bind VAO");
            glDrawArrays(GL_TRIANGLES, 0, 6);
            Debug.glCheckError("FullscreenQuad.draw - draw arrays");
            glBindVertexArray(0);
            Debug.glCheckError("FullscreenQuad.draw - unbind VAO");
        }

        public void cleanup() {
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
            Debug.glCheckError("FullscreenQuad.cleanup");
        }
    }

    @Override
    public void init(long win) {
        this.window = win;
        // --- low‑res FBO ---
        colorTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, colorTex);
        Debug.glCheckError("PsxForwardRenderer.init - gen and bind texture");
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 320, 240, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, 0);
        Debug.glCheckError("PsxForwardRenderer.init - texImage2D");

        // Add proper texture parameters for the framebuffer texture
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        Debug.glCheckError("PsxForwardRenderer.init - texture parameters");

        depthRb = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthRb);
        Debug.glCheckError("PsxForwardRenderer.init - gen and bind renderbuffer");
        
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, 320, 240);
        Debug.glCheckError("PsxForwardRenderer.init - renderbuffer storage");

        lowResFbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, lowResFbo);
        Debug.glCheckError("PsxForwardRenderer.init - gen and bind framebuffer");
        
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D, colorTex, 0);
        Debug.glCheckError("PsxForwardRenderer.init - framebuffer texture");
        
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER, depthRb);
        Debug.glCheckError("PsxForwardRenderer.init - framebuffer renderbuffer");
        
        // Check if framebuffer is complete
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is not complete! Status: 0x" + Integer.toHexString(status));
        }
        Debug.glCheckError("PsxForwardRenderer.init - check framebuffer status");

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Debug.glCheckError("PsxForwardRenderer.init - unbind framebuffer");

        psxShader  = Shader.builder("psx")
                .vertexFromResource("shaders/psx.vert")
                .fragmentFromResource("shaders/psx.frag")
                .build();
//        postShader = Shader.builder("post")
//                .vertexFromResource("shaders/fullscreen.vert")
//                .fragmentFromResource("shaders/post_dither.frag")
//                .build();
//        psxShader  = Shader.builder("debug1")
//                .vertexFromResource("shaders/debug.vert")
//                .fragmentFromResource("shaders/debug.frag")
//                .build();
//        postShader = Shader.builder("debug2")
//                .vertexFromResource("shaders/psx.vert")
//                .fragmentFromResource("shaders/psx.frag")
//                .build();

    }

    @Override
    public void beginFrame() {
        drawQueue.clear();
        glBindFramebuffer(GL_FRAMEBUFFER, lowResFbo);
        Debug.glCheckError("PsxForwardRenderer.beginFrame - bind framebuffer");
        
        glViewport(0, 0, 320, 240);
        Debug.glCheckError("PsxForwardRenderer.beginFrame - viewport");
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Debug.glCheckError("PsxForwardRenderer.beginFrame - clear");
    }

    @Override
    public void draw(Mesh mesh, Material mat, Matrix4f model) {
        drawQueue.add(new DrawCmd(mesh, mat, new Matrix4f(model)));
    }

    @Override
    public void endFrame() {
        psxShader.bind();
        Debug.glCheckError("PsxForwardRenderer.endFrame - bind psx shader");
        
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

        // post‑process to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Debug.glCheckError("PsxForwardRenderer.endFrame - bind default framebuffer");
        
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(window, width, height);
        glViewport(0, 0, width[0], height[0]);
        Debug.glCheckError("PsxForwardRenderer.endFrame - viewport");
        
        glDisable(GL_DEPTH_TEST);
        Debug.glCheckError("PsxForwardRenderer.endFrame - disable depth test");

//        postShader.bind();
//        Debug.glCheckError("PsxForwardRenderer.endFrame - bind post shader");
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, colorTex);
        Debug.glCheckError("PsxForwardRenderer.endFrame - bind texture");
        
//        postShader.setInt("uScene", 0);
//        fullscreenVao.draw();
//        postShader.unbind();
//        Debug.glCheckError("PsxForwardRenderer.endFrame - post process");
        
        glEnable(GL_DEPTH_TEST);
        Debug.glCheckError("PsxForwardRenderer.endFrame - enable depth test");
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
//        postShader.cleanup();
        glDeleteFramebuffers(lowResFbo);
        glDeleteTextures(colorTex);
        glDeleteRenderbuffers(depthRb);
        Debug.glCheckError("PsxForwardRenderer.destroy");
    }
}
