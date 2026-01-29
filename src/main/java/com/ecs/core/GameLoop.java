package com.ecs.core;

import com.artemis.World;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Main game loop that processes the world at regular intervals.
 */
@Singleton
public class GameLoop implements Runnable {

    private final World world;
    private final WorldCommandQueue commandQueue;
    private volatile boolean running = true;
    private long lastTime = System.nanoTime();

    @Inject
    public GameLoop(World world, WorldCommandQueue commandQueue) {
        this.world = world;
        this.commandQueue = commandQueue;
    }

    /**
     * Stops the game loop.
     */
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Calculate delta time in seconds
                long currentTime = System.nanoTime();
                float delta = (currentTime - lastTime) / 1_000_000_000.0f;
                lastTime = currentTime;

                // Process queued commands
                commandQueue.process(world);

                // Update world with delta time
                world.setDelta(delta);
                world.process();

                // Sleep to prevent CPU spinning
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            } catch (Exception e) {
                System.err.println("Error in game loop: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
