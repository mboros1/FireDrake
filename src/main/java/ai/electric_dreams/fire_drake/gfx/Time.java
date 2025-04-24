package ai.electric_dreams.fire_drake.gfx;

public class Time {
    private static long lastTime = System.nanoTime();

    // Returns delta time in seconds
    public static float getDeltaTime() {
        long now = System.nanoTime();
        float delta = (now - lastTime) / 1_000_000_000.0f;
        lastTime = now;
        return delta;
    }

    public static void reset() {
        lastTime = System.nanoTime();
    }
}
