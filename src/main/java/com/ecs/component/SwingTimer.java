package com.ecs.component;

import com.artemis.Component;
import com.artemis.annotations.Transient;

/**
 * Swing timer component for attack cooldown tracking.
 * Transient component that is not serialized.
 */
@Transient
public class SwingTimer extends Component {
    public float cooldown;

    public SwingTimer() {
    }

    public SwingTimer(float cooldown) {
        this.cooldown = cooldown;
    }
}
