package com.ecs.ai;

import com.artemis.World;

import java.util.Arrays;
import java.util.List;

/**
 * Behavior node that executes child nodes in sequence.
 * Returns SUCCESS only if all children succeed.
 * Returns FAILURE if any child fails.
 * Returns RUNNING if any child is still running.
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
}
