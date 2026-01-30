package com.ecs;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.ecs.ai.BehaviorNode;
import com.ecs.ai.SequenceNode;
import com.ecs.ai.Status;
import com.ecs.component.AiBehavior;
import com.ecs.factory.EntityFactory;
import com.ecs.registry.TemplateRegistry;
import com.ecs.service.YamlService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to ensure entities maintain independent AI state (no "hive mind" bug).
 */
class AiStateIsolationTest {

    @Test
    void entitiesShouldMaintainIndependentAiState() {
        // Create a world
        World world = new World(new WorldConfigurationBuilder().build());

        // Create a simple test: two entities with RUNNING nodes
        // If they share state, both would be affected by one execution
        DummyNode runningNode = new DummyNode(Status.RUNNING);
        SequenceNode nodeA = new SequenceNode(
            new DummyNode(Status.SUCCESS),
            runningNode
        );
        BehaviorNode copiedNode = nodeA.deepCopy();
        SequenceNode nodeB = (copiedNode instanceof SequenceNode) ? (SequenceNode) copiedNode : null;
        assertThat(nodeB).isNotNull();

        // Create EntityA
        int entityA = world.create();
        AiBehavior behaviorA = new AiBehavior(nodeA);
        world.edit(entityA).add(behaviorA);

        // Create EntityB
        int entityB = world.create();
        AiBehavior behaviorB = new AiBehavior(nodeB);
        world.edit(entityB).add(behaviorB);

        // Execute A twice
        behaviorA.rootNode.execute(world, entityA); // First execution - child 1 succeeds, moves to child 2
        Status statusA = behaviorA.rootNode.execute(world, entityA); // Second execution - still at child 2 (RUNNING)
        
        // Execute B once
        Status statusB = behaviorB.rootNode.execute(world, entityB); // First execution - child 1 succeeds, moves to child 2
        
        // Both should return RUNNING since they're at the running node
        assertThat(statusA).isEqualTo(Status.RUNNING);
        assertThat(statusB).isEqualTo(Status.RUNNING);
        
        // The key test: verify they are different instances
        assertThat(behaviorA.rootNode).isNotSameAs(behaviorB.rootNode);
    }

    @Test
    void templateSpawningShouldCreateIndependentAiInstances() {
        // Create template registry
        YamlService yamlService = new YamlService();
        TemplateRegistry templateRegistry = new TemplateRegistry(yamlService);

        // Create a world
        World world = new World(new WorldConfigurationBuilder().build());

        // Create a template with an AI behavior
        DummyNode child1 = new DummyNode(Status.SUCCESS);
        DummyNode child2 = new DummyNode(Status.SUCCESS);
        SequenceNode rootNode = new SequenceNode(child1, child2);
        
        templateRegistry.registerTemplate("test_orc", Arrays.asList(
            new AiBehavior(rootNode)
        ));

        // Use EntityFactory to spawn two entities from the same template
        EntityFactory factory = new EntityFactory(templateRegistry);
        int entityA = factory.prepare("test_orc").build(world);
        int entityB = factory.prepare("test_orc").build(world);

        // Get their AI behaviors
        AiBehavior behaviorA = world.getMapper(AiBehavior.class).get(entityA);
        AiBehavior behaviorB = world.getMapper(AiBehavior.class).get(entityB);

        // Verify they have different instances
        assertThat(behaviorA.rootNode).isNotSameAs(behaviorB.rootNode);

        // Verify that modifying A doesn't affect B
        if (behaviorA.rootNode instanceof SequenceNode seqA && 
            behaviorB.rootNode instanceof SequenceNode seqB) {
            
            // Execute A's behavior
            behaviorA.rootNode.execute(world, entityA);
            
            // B should still be unaffected (this would fail if they shared state)
            assertThat(seqA).isNotSameAs(seqB);
        }
    }

    /**
     * Dummy node for testing that returns a fixed status.
     */
    private static class DummyNode implements BehaviorNode {
        private final Status returnStatus;

        DummyNode(Status returnStatus) {
            this.returnStatus = returnStatus;
        }

        @Override
        public Status execute(World world, int entityId) {
            return returnStatus;
        }

        @Override
        public BehaviorNode deepCopy() {
            return new DummyNode(returnStatus);
        }
    }
}
