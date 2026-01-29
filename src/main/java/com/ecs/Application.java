package com.ecs;

import com.ecs.core.GameLoop;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

/**
 * Main application entry point.
 * Bootstraps the Micronaut context and starts the game loop.
 */
public class Application {

    public static void main(String[] args) {
        ApplicationContext context = Micronaut.build(args)
                .mainClass(Application.class)
                .start();

        // Retrieve the game loop bean and start it in a new thread
        GameLoop gameLoop = context.getBean(GameLoop.class);
        Thread gameThread = new Thread(gameLoop, "GameLoop");
        gameThread.start();

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            gameLoop.stop();
            try {
                gameThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            context.close();
        }));

        System.out.println("ECS Simulation started.");
    }
}
