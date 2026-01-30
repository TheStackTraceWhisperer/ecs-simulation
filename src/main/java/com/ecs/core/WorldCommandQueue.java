package com.ecs.core;

import com.artemis.World;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Thread-safe command queue for deferred world operations.
 * Commands are queued and executed during the world processing phase.
 */
@Slf4j
@Singleton
public class WorldCommandQueue {
    private final ConcurrentLinkedQueue<Consumer<World>> queue = new ConcurrentLinkedQueue<>();

    /**
     * Enqueues a command to be executed on the world.
     *
     * @param command the command to enqueue
     */
    public void enqueue(Consumer<World> command) {
        queue.offer(command);
    }

    /**
     * Processes all queued commands and executes them on the given world.
     * Commands are drained from the queue and executed in FIFO order.
     * Exceptions are caught and logged to prevent the loop from crashing.
     *
     * @param world the world to execute commands on
     */
    public void process(World world) {
        Consumer<World> command;
        while ((command = queue.poll()) != null) {
            try {
                command.accept(world);
            } catch (Exception e) {
                log.error("Error executing command: {}", e.getMessage(), e);
            }
        }
    }
}
