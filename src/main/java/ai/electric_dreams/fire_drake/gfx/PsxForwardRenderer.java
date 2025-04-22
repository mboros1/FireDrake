package ai.electric_dreams.fire_drake.gfx;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public final class PsxForwardRenderer implements Renderer {
    private Shader psxShader;
    private Shader debugShader;
    private int lowResFbo = 0; // Initialize to 0 (default framebuffer)
    private int colorTex = 0;
    private int depthRb = 0;
    private Matrix4f view = new Matrix4f(), proj = new Matrix4f();
    private List<DrawCmd> drawQueue = new ArrayList<>();
    private long window;
    private int fbWidth, fbHeight;

    private static class DrawCmd {
        final Mesh mesh;
        final Matrix4f model;
        final int instanceCount;

        DrawCmd(Mesh mesh, Matrix4f model) {
            this(mesh, model, 1);
        }

        DrawCmd(Mesh mesh, Matrix4f model, int instanceCount) {
            this.mesh = mesh;
            this.model = model;
            this.instanceCount = instanceCount;
        }
    }

    @Override
    public void init(long win) {
        this.window = win;

        // Set up framebuffer resize callback
        glfwSetFramebufferSizeCallback(window, (_, width, height) -> {
            fbWidth  = width;
            fbHeight = height;
            glViewport(0, 0, width, height);
        });

        // Load shaders
        psxShader = Shader.builder("psx")
                .vertexFromResource("shaders/psx.vert")
                .fragmentFromResource("shaders/psx.frag")
                .build();
                
        // Load debug shader for simple colored primitives
        debugShader = Shader.builder("debug")
                .vertexFromResource("shaders/debug.vert")
                .fragmentFromResource("shaders/debug.frag")
                .build();

        initView();
        updateProjection();
    }

    private void initView() {
        // camera at (0,2,5) looking at origin
        view.identity()
                .lookAt(new Vector3f(0,2,5), new Vector3f(0,0,0), new Vector3f(0,1,0));
    }

    private void updateProjection() {
        float aspect = (float)fbWidth / fbHeight;
        proj.identity()
                .perspective((float)Math.toRadians(60), aspect, 0.1f, 100f);
    }

    @Override
    public void beginFrame() {
        glViewport(0, 0, fbWidth, fbHeight);

        drawQueue.clear();
        
        // We're rendering directly to the default framebuffer now
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Debug.glCheckError("PsxForwardRenderer.beginFrame - bind default framebuffer");
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Debug.glCheckError("PsxForwardRenderer.beginFrame - clear");
    }

    @Override
    public void draw(Mesh mesh, Material mat, Matrix4f model) {
        drawQueue.add(new DrawCmd(mesh, new Matrix4f(model)));
    }

    @Override
    public void endFrame() {
        psxShader.bind();
        psxShader.setMatrix4f("view", view);
        psxShader.setMatrix4f("projection", proj);
        Debug.glCheckError("PsxForwardRenderer.endFrame - set matrices");
        
        for (DrawCmd cmd : drawQueue) {
            psxShader.setMatrix4f("model", cmd.model);
            cmd.mesh.draw(psxShader);
            Debug.glCheckError("PsxForwardRenderer.endFrame - draw mesh");
        }
        psxShader.unbind();
        Debug.glCheckError("PsxForwardRenderer.endFrame - unbind psx shader");
        
        // We're already on the default framebuffer
    }

    @Override
    public void draw(Mesh testTriangle) {
        // Use the debug shader for the test triangle
        debugShader.bind();
        debugShader.setMatrix4f("model", new Matrix4f().identity());
        debugShader.setMatrix4f("view", view);
        debugShader.setMatrix4f("projection", proj);
        testTriangle.draw(debugShader);
        debugShader.unbind();
        Debug.glCheckError("PsxForwardRenderer.draw - triangle");
    }

    @Override
    public void destroy() { 
        psxShader.cleanup();
        if (debugShader != null) {
            debugShader.cleanup();
        }
        
        // Clean up framebuffer resources if they were created
        if (lowResFbo != 0) {
            glDeleteFramebuffers(lowResFbo);
        }
        if (colorTex != 0) {
            glDeleteTextures(colorTex);
        }
        if (depthRb != 0) {
            glDeleteRenderbuffers(depthRb);
        }
        Debug.glCheckError("PsxForwardRenderer.destroy");
    }
}
