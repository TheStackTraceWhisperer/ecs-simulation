package com.ecs.component;

import com.artemis.Component;

/**
 * Velocity component storing movement delta per second.
 */
public class Velocity extends Component {
    public float dx;
    public float dy;

    public Velocity() {
    }

    public Velocity(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
