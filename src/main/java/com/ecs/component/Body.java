package com.ecs.component;

import com.artemis.Component;

/**
 * Body component representing physical size in space.
 */
public class Body extends Component {
    public float radius;

    public Body() {
    }

    public Body(float radius) {
        this.radius = radius;
    }
}
