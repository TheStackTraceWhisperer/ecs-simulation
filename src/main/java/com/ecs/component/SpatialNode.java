package com.ecs.component;

import com.artemis.Component;
import com.artemis.annotations.Transient;

/**
 * Spatial node component for tracking entity position in the spatial grid.
 * This is a transient component that is not serialized.
 */
@Transient
public class SpatialNode extends Component {
    public float lastX;
    public float lastY;

    public SpatialNode() {
    }

    public SpatialNode(float lastX, float lastY) {
        this.lastX = lastX;
        this.lastY = lastY;
    }
}
