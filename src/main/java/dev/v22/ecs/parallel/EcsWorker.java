package dev.v22.ecs.parallel;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class EcsWorker extends Thread {
    private final BlockingDeque<Runnable> taskQueue = new LinkedBlockingDeque<>();
    private volatile boolean running = true;

    @Override
    public void run() {
        try {
            while (running) {
                Runnable task = taskQueue.take();
                task.run();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void submitTask(Runnable task) {
        taskQueue.add(task);
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}