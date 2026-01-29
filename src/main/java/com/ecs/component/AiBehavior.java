package com.ecs.component;

import com.artemis.Component;
import com.ecs.ai.BehaviorNode;

/**
 * AI behavior component holding a behavior tree root node.
 */
public class AiBehavior extends Component {
    public BehaviorNode rootNode;

    public AiBehavior() {
    }

    public AiBehavior(BehaviorNode rootNode) {
        this.rootNode = rootNode;
    }
}
