package com.ecs.ai;

import com.artemis.World;

import java.util.Arrays;
import java.util.List;

/**
 * Behavior node that executes child nodes in sequence.
 * Returns SUCCESS only if all children succeed.
 * Returns FAILURE if any child fails.
 * Returns RUNNING if any child is still running.
 * 
 * <p><strong>Important:</strong> SequenceNode instances maintain state (currentIndex) 
 * and should NOT be shared between multiple entities. Each entity should have its own 
 * SequenceNode instance or the behavior should be recreated per entity.
 */
public class SequenceNode implements BehaviorNode {

    private final List<BehaviorNode> children;
    private int currentIndex = 0;

    public SequenceNode(BehaviorNode... children) {
        this.children = Arrays.asList(children);
    }

    public SequenceNode(List<BehaviorNode> children) {
        this.children = children;
    }

    @Override
    public Status execute(World world, int entityId) {
        if (children.isEmpty()) {
            return Status.SUCCESS;
        }

        while (currentIndex < children.size()) {
            BehaviorNode child = children.get(currentIndex);
            Status status = child.execute(world, entityId);

            if (status == Status.FAILURE) {
                currentIndex = 0; // Reset for next execution
                return Status.FAILURE;
            }

            if (status == Status.RUNNING) {
                return Status.RUNNING;
            }

            // SUCCESS - move to next child
            currentIndex++;
        }

        // All children succeeded
        currentIndex = 0; // Reset for next execution
        return Status.SUCCESS;
    }

    @Override
    public BehaviorNode deepCopy() {
        // Deep copy all children
        BehaviorNode[] copiedChildren = new BehaviorNode[children.size()];
        for (int i = 0; i < children.size(); i++) {
            copiedChildren[i] = children.get(i).deepCopy();
        }
        return new SequenceNode(copiedChildren);
    }
}
