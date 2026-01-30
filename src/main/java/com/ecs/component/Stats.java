package com.ecs.component;

import com.artemis.Component;

/**
 * Stats component for health tracking.
 */
public class Stats extends Component {
    public float health;
    public float maxHealth;

    public Stats() {
    }

    public Stats(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }
}
