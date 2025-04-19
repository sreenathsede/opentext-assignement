package org.opentext.async;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.*;

public class SimpleTaskExecutor implements Main.TaskExecutor {

    private final ExecutorService workerPool;
    private final BlockingQueue<InternalTask<?>> taskQueue = new LinkedBlockingQueue<>();
    private final Map<UUID, Object> groupLocks = new ConcurrentHashMap<>();
    private final Thread dispatcher;
    private volatile boolean running = true;

    public SimpleTaskExecutor(int maxThreads) {
        this.workerPool = Executors.newFixedThreadPool(maxThreads);
        this.dispatcher = new Thread(this::dispatchLoop, "dispatcher-thread");
        this.dispatcher.start();
        System.out.println("[SYSTEM] SimpleTaskExecutor started with pool size " + maxThreads);
    }

    @Override
    public <T> Future<T> submitTask(Main.Task<T> task) {
        if (task == null) throw new IllegalArgumentException("Task cannot be null");
        CompletableFuture<T> future = new CompletableFuture<>();
        InternalTask<T> internal = new InternalTask<>(task, future);
        taskQueue.offer(internal);
        System.out.println("[SUBMIT] Task submitted: " + task.taskUUID());
        return future;
    }

    private void dispatchLoop() {
        System.out.println("[DISPATCHER] Dispatcher thread running");
        try {
            while (running || !taskQueue.isEmpty()) {
                InternalTask<?> internal = taskQueue.take();
                System.out.println("[DISPATCHER] Dispatching: " + internal.task.taskUUID());
                workerPool.submit(() -> executeTask(internal));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[DISPATCHER] Interrupted and stopping");
        }
    }

    private <T> void executeTask(InternalTask<T> internal) {
        Main.Task<T> task = internal.task;
        UUID groupId = task.taskGroup().groupUUID();
        Object lock = groupLocks.computeIfAbsent(groupId, id -> new Object());

        System.out.println("[EXECUTOR] Waiting for lock on group " + groupId + " for task " + task.taskUUID());
        synchronized (lock) {
            System.out.println("[EXECUTOR] Acquired lock, running task " + task.taskUUID());
            try {
                T result = task.taskAction().call();
                internal.future.complete(result);
                System.out.println("[EXECUTOR] Completed task " + task.taskUUID());
            } catch (Exception e) {
                internal.future.completeExceptionally(e);
                System.out.println("[EXECUTOR] Task " + task.taskUUID() + " failed: " + e.getMessage());
            }
        }
        System.out.println("[EXECUTOR] Released lock for group " + groupId + " on task " + task.taskUUID());
    }

    public void shutdown() {
        running = false;
        dispatcher.interrupt();
        workerPool.shutdown();
        System.out.println("[SYSTEM] Shutdown initiated");
    }

    private static class InternalTask<T> {
        final Main.Task<T> task;
        final CompletableFuture<T> future;

        InternalTask(Main.Task<T> task, CompletableFuture<T> future) {
            this.task = task;
            this.future = future;
        }
    }
}