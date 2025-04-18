package ai.electric_dreams.fire_drake.gfx;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex {
    private Vector3f position;
    private Vector3f normal;
    private Vector2f texCoords;
    private Vector3f tangent;
    private Vector3f bitangent;

    public Vertex() {
        this.position = new Vector3f();
        this.normal = new Vector3f();
        this.texCoords = new Vector2f();
        this.tangent = new Vector3f();
        this.bitangent = new Vector3f();
    }

    public Vertex(Vector3f position,
                  Vector3f normal,
                  Vector2f texCoords,
                  Vector3f tangent,
                  Vector3f bitangent) {
        this.position  = new Vector3f(position);
        this.normal    = new Vector3f(normal);
        this.texCoords = new Vector2f(texCoords);
        this.tangent   = new Vector3f(tangent);
        this.bitangent = new Vector3f(bitangent);
    }


    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public void setNormal(Vector3f normal) {
        this.normal = normal;
    }

    public Vector2f getTexCoords() {
        return texCoords;
    }

    public void setTexCoords(Vector2f texCoords) {
        this.texCoords = texCoords;
    }

    public Vector3f getTangent() {
        return tangent;
    }

    public void setTangent(Vector3f tangent) {
        this.tangent = tangent;
    }

    public Vector3f getBitangent() {
        return bitangent;
    }

    public void setBitangent(Vector3f bitangent) {
        this.bitangent = bitangent;
    }
} 