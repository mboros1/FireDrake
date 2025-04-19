package mesh;

import ai.electric_dreams.fire_drake.gfx.mesh.ObjMesh;
import org.junit.jupiter.api.Test;

public class MeshTest {

    @Test
    public void testLoadObj() {
        var mesh = new ObjMesh("fire_drake.obj");

        mesh.reportObjStats();
    }
}
