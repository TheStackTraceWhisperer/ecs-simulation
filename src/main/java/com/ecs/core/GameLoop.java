package com.ecs.core;

import com.artemis.World;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Main game loop that processes the world at regular intervals.
 * Handles clock skips gracefully by capping maximum delta time.
 */
@Slf4j
@Singleton
public class GameLoop implements Runnable {

    private static final float MAX_DELTA_TIME = 0.25f; // Cap at 250ms to prevent spiral of death
    private static final long SLEEP_MILLIS = 1L;

    private final World world;
    private final WorldCommandQueue commandQueue;
    private volatile boolean running = true;
    private long lastTime;

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
        // Initialize lastTime when thread actually starts
        lastTime = System.nanoTime();
        
        while (running) {
            try {
                // Calculate delta time in seconds
                long currentTime = System.nanoTime();
                float delta = (currentTime - lastTime) / 1_000_000_000.0f;
                lastTime = currentTime;

                // Cap delta to prevent instability from clock skips or long pauses
                // This prevents the "spiral of death" where updates take longer than real time
                if (delta > MAX_DELTA_TIME) {
                    log.warn("Large delta time detected: {}s, capping to {}s", delta, MAX_DELTA_TIME);
                    delta = MAX_DELTA_TIME;
                }

                // Process queued commands
                commandQueue.process(world);

                // Update world with delta time
                world.setDelta(delta);
                world.process();

                // Sleep to prevent CPU spinning
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            } catch (Exception e) {
                log.error("Error in game loop: {}", e.getMessage(), e);
            }
        }
    }
}
