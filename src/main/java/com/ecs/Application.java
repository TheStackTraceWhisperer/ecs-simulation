package com.ecs;

import com.ecs.core.GameLoop;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Main application entry point.
 * Bootstraps the Micronaut context and starts the game loop.
 */
@Slf4j
@Singleton
public class Application {

    private final GameLoop gameLoop;
    private Thread gameThread;

    @Inject
    public Application(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    public void startGameLoop() {
        gameThread = new Thread(gameLoop, "GameLoop");
        gameThread.start();
        log.info("ECS Simulation started.");
    }

    @EventListener
    public void onShutdown(ShutdownEvent event) {
        log.info("Shutting down...");
        gameLoop.stop();
        if (gameThread != null) {
            try {
                gameThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for game thread to stop");
            }
        }
    }

    public static void main(String[] args) {
        ApplicationContext context = Micronaut.build(args)
                .mainClass(Application.class)
                .start();

        // Start the game loop
        Application app = context.getBean(Application.class);
        app.startGameLoop();
    }
}
