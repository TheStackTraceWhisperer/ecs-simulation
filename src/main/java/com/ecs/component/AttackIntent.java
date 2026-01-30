package com.ecs.component;

import com.artemis.Component;
import com.artemis.annotations.Transient;

/**
 * Attack intent component indicating an entity wants to attack a target.
 * Transient component that is not serialized.
 */
@Transient
public class AttackIntent extends Component {
    public int targetId;

    public AttackIntent() {
    }

    public AttackIntent(int targetId) {
        this.targetId = targetId;
    }
}
