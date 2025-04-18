package ai.electric_dreams.fire_drake.gfx;

import static org.lwjgl.opengl.GL11.glDeleteTextures;

public class Material {
    private final int textureId;

    public Material(int textureId) {
        this.textureId = textureId;
    }

    public int textureId() {
        return textureId;
    }

    public void cleanup() {
        glDeleteTextures(textureId);
        Debug.glCheckError("Material.cleanup");
    }
}
