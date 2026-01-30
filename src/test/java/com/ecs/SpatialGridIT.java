package com.ecs;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.utils.IntBag;
import com.ecs.component.Position;
import com.ecs.component.SpatialNode;
import com.ecs.spatial.SpatialHashGrid;
import com.ecs.system.SpatialSystem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for spatial grid integrity.
 * Ensures entities don't "teleport" or vanish from the grid.
 */
class SpatialGridIT {

    @Test
    void entityShouldMoveAcrossGridBucketsCorrectly() {
        // Create spatial grid
        SpatialHashGrid grid = new SpatialHashGrid();
        
        // Create world with spatial system
        World world = new World(new WorldConfigurationBuilder()
                .with(new SpatialSystem(grid))
                .build());

        // Spawn entity at (0, 0)
        int entity = world.create();
        world.edit(entity)
                .add(new Position(0, 0))
                .add(new SpatialNode(0, 0));

        // Process world to insert entity into grid
        world.setDelta(0.016f);
        world.process();

        // Verify entity is at (0, 0)
        IntBag nearOrigin = grid.getNearby(0, 0);
        assertThat(nearOrigin.size()).isGreaterThan(0);
        assertThat(nearOrigin.contains(entity)).isTrue();

        // Move entity to (500, 500) - this is far enough to be in a different bucket
        // Cell size is 100, so (0,0) is cell (0,0) and (500,500) is cell (5,5)
        // getNearby checks 3x3, so they won't overlap
        Position position = world.getMapper(Position.class).get(entity);
        position.x = 500;
        position.y = 500;

        // Process world to update spatial grid
        world.process();

        // Query at (0, 0) - should not contain our entity
        IntBag stillAtOrigin = grid.getNearby(0, 0);
        assertThat(stillAtOrigin.contains(entity))
                .as("Entity should no longer be at origin after moving")
                .isFalse();

        // Query at (500, 500) - should contain entity
        IntBag atNewPosition = grid.getNearby(500, 500);
        assertThat(atNewPosition.contains(entity))
                .as("Entity should be at new position (500, 500)")
                .isTrue();
    }

    @Test
    void multipleEntitiesShouldMaintainIndependentPositions() {
        // Create spatial grid
        SpatialHashGrid grid = new SpatialHashGrid();
        
        // Create world with spatial system
        World world = new World(new WorldConfigurationBuilder()
                .with(new SpatialSystem(grid))
                .build());

        // Spawn entity A at (0, 0)
        int entityA = world.create();
        world.edit(entityA)
                .add(new Position(0, 0))
                .add(new SpatialNode(0, 0));

        // Spawn entity B at (200, 200)
        int entityB = world.create();
        world.edit(entityB)
                .add(new Position(200, 200))
                .add(new SpatialNode(200, 200));

        // Process world
        world.setDelta(0.016f);
        world.process();

        // Verify A is at origin
        IntBag nearOrigin = grid.getNearby(0, 0);
        assertThat(nearOrigin.contains(entityA)).isTrue();
        assertThat(nearOrigin.contains(entityB)).isFalse();

        // Verify B is at (200, 200)
        IntBag nearB = grid.getNearby(200, 200);
        assertThat(nearB.contains(entityB)).isTrue();
        assertThat(nearB.contains(entityA)).isFalse();

        // Move A to (50, 50)
        Position posA = world.getMapper(Position.class).get(entityA);
        posA.x = 50;
        posA.y = 50;
        world.process();

        // Verify A moved and B didn't
        IntBag nearA = grid.getNearby(50, 50);
        assertThat(nearA.contains(entityA)).isTrue();
        assertThat(nearA.contains(entityB)).isFalse();

        IntBag stillNearB = grid.getNearby(200, 200);
        assertThat(stillNearB.contains(entityB)).isTrue();
        assertThat(stillNearB.contains(entityA)).isFalse();
    }

    @Test
    void entityDeletionShouldRemoveFromGrid() {
        // Create spatial grid
        SpatialHashGrid grid = new SpatialHashGrid();
        
        // Create world with spatial system
        World world = new World(new WorldConfigurationBuilder()
                .with(new SpatialSystem(grid))
                .build());

        // Spawn entity
        int entity = world.create();
        world.edit(entity)
                .add(new Position(0, 0))
                .add(new SpatialNode(0, 0));

        world.setDelta(0.016f);
        world.process();

        // Verify entity is in grid
        IntBag nearby = grid.getNearby(0, 0);
        assertThat(nearby.contains(entity)).isTrue();

        // Delete entity
        world.delete(entity);
        world.process();

        // Verify entity is removed from grid
        IntBag afterDeletion = grid.getNearby(0, 0);
        assertThat(afterDeletion.contains(entity))
                .as("Deleted entity should be removed from grid")
                .isFalse();
    }
}
