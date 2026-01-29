package com.ecs.spatial;

import com.artemis.utils.IntBag;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import jakarta.inject.Singleton;

/**
 * Spatial hash grid for efficient proximity queries.
 * Entities are bucketed into grid cells based on their position.
 */
@Singleton
public class SpatialHashGrid {

    private static final int CELL_SIZE = 100;
    private final Long2ObjectOpenHashMap<IntBag> grid = new Long2ObjectOpenHashMap<>();

    /**
     * Computes the hash key for a grid cell.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the hash key
     */
    private long hash(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    /**
     * Gets the grid cell coordinates for a position.
     *
     * @param x the x coordinate
     * @return the cell x coordinate
     */
    private int getCellX(float x) {
        return (int) Math.floor(x / CELL_SIZE);
    }

    /**
     * Gets the grid cell coordinates for a position.
     *
     * @param y the y coordinate
     * @return the cell y coordinate
     */
    private int getCellY(float y) {
        return (int) Math.floor(y / CELL_SIZE);
    }

    /**
     * Inserts an entity into the grid.
     *
     * @param id the entity ID
     * @param x  the x position
     * @param y  the y position
     */
    public void insert(int id, float x, float y) {
        int cellX = getCellX(x);
        int cellY = getCellY(y);
        long key = hash(cellX, cellY);

        IntBag cell = grid.get(key);
        if (cell == null) {
            cell = new IntBag();
            grid.put(key, cell);
        }
        cell.add(id);
    }

    /**
     * Removes an entity from the grid.
     *
     * @param id the entity ID
     * @param x  the x position
     * @param y  the y position
     */
    public void remove(int id, float x, float y) {
        int cellX = getCellX(x);
        int cellY = getCellY(y);
        long key = hash(cellX, cellY);

        IntBag cell = grid.get(key);
        if (cell != null) {
            cell.removeValue(id);
            if (cell.isEmpty()) {
                grid.remove(key);
            }
        }
    }

    /**
     * Gets all entities near a position (including the center cell and its 8 neighbors).
     *
     * @param x the x position
     * @param y the y position
     * @return a bag of entity IDs
     */
    public IntBag getNearby(float x, float y) {
        IntBag result = new IntBag();
        int cellX = getCellX(x);
        int cellY = getCellY(y);

        // Check 3x3 grid around the position
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                long key = hash(cellX + dx, cellY + dy);
                IntBag cell = grid.get(key);
                if (cell != null) {
                    for (int i = 0; i < cell.size(); i++) {
                        result.add(cell.get(i));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Clears all entities from the grid.
     */
    public void clear() {
        grid.clear();
    }
}
