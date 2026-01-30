package com.ecs.component;

import com.artemis.Component;

/**
 * Identity component for named entities.
 */
public class Identity extends Component {
    public String id;

    public Identity() {
    }

    public Identity(String id) {
        this.id = id;
    }
}
