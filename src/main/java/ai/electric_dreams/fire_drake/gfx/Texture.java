package ai.electric_dreams.fire_drake.gfx;

public class Texture {
    private int id;
    private String type;
    private String path;

    public Texture(int id, String type, String path) {
        this.id = id;
        this.type = type;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
} 