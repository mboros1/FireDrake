package ai.electric_dreams.fire_drake;

import ai.electric_dreams.fire_drake.gfx.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the game world containing all visible entities.
 */
public class World {
    private final List<Entity> entities = new ArrayList<>();

    /**
     * Adds an entity to the world.
     * 
     * @param entity The entity to add
     */
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    /**
     * Removes an entity from the world.
     * 
     * @param entity The entity to remove
     * @return true if the entity was removed, false otherwise
     */
    public boolean removeEntity(Entity entity) {
        return entities.remove(entity);
    }

    /**
     * Returns all visible entities in the world.
     * In a more complex implementation, this would filter entities based on camera frustum.
     * 
     * @return A list of all visible entities
     */
    public List<Entity> visible() {
        return Collections.unmodifiableList(entities);
    }

    /**
     * Updates the world state.
     * 
     * @param deltaTime Time since last update in seconds
     */
    public void update(float deltaTime) {
        // Update world state, physics, etc.
    }
} 