package com.ecs.service;

import com.ecs.core.WorldCommandQueue;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Scheduled task for automatic game state persistence.
 */
@Singleton
public class AutoSaveTask {

    private final WorldCommandQueue commandQueue;
    private final PersistenceService persistenceService;
    private static final String SAVE_FILE = "autosave.yml";

    @Inject
    public AutoSaveTask(WorldCommandQueue commandQueue, PersistenceService persistenceService) {
        this.commandQueue = commandQueue;
        this.persistenceService = persistenceService;
    }

    /**
     * Enqueues a save command every 5 minutes.
     */
    @Scheduled(fixedDelay = "5m")
    public void autoSave() {
        commandQueue.enqueue(world -> {
            try {
                persistenceService.save(SAVE_FILE);
                System.out.println("Auto-save completed: " + SAVE_FILE);
            } catch (Exception e) {
                System.err.println("Auto-save failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
