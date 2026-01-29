package com.ecs.component;

import com.artemis.Component;

/**
 * Position component storing x, y coordinates in 2D space.
 */
public class Position extends Component {
    public float x;
    public float y;

    public Position() {
    }

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
