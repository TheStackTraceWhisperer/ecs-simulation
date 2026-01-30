package com.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.ecs.component.Position;
import com.ecs.component.SpatialNode;
import com.ecs.spatial.SpatialHashGrid;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * System for maintaining entity positions in the spatial grid.
 */
@Singleton
@Order(2)
public class SpatialSystem extends IteratingSystem {

    private final SpatialHashGrid grid;
    private ComponentMapper<Position> positionMapper;
    private ComponentMapper<SpatialNode> spatialNodeMapper;

    @Inject
    public SpatialSystem(SpatialHashGrid grid) {
        super(Aspect.all(Position.class, SpatialNode.class));
        this.grid = grid;
    }

    @Override
    protected void inserted(int entityId) {
        Position position = positionMapper.get(entityId);
        SpatialNode node = spatialNodeMapper.get(entityId);

        // Initialize the node with current position
        node.lastX = position.x;
        node.lastY = position.y;

        // Insert into grid
        grid.insert(entityId, position.x, position.y);
    }

    @Override
    protected void removed(int entityId) {
        SpatialNode node = spatialNodeMapper.get(entityId);
        if (node != null) {
            // Use the last known position from SpatialNode
            grid.remove(entityId, node.lastX, node.lastY);
        }
    }

    @Override
    protected void process(int entityId) {
        Position position = positionMapper.get(entityId);
        SpatialNode node = spatialNodeMapper.get(entityId);

        // Check if position has changed
        if (position.x != node.lastX || position.y != node.lastY) {
            // Remove from old position
            grid.remove(entityId, node.lastX, node.lastY);

            // Insert at new position
            grid.insert(entityId, position.x, position.y);

            // Update last known position
            node.lastX = position.x;
            node.lastY = position.y;
        }
    }
}
