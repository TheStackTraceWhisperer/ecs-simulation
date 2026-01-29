package com.ecs.component;

import com.artemis.Component;

/**
 * Combat stats component for damage, range, and attack speed.
 */
public class CombatStats extends Component {
    public float damage;
    public float range;
    public float attackSpeed; // Attacks per second

    public CombatStats() {
    }

    public CombatStats(float damage, float range, float attackSpeed) {
        this.damage = damage;
        this.range = range;
        this.attackSpeed = attackSpeed;
    }
}
