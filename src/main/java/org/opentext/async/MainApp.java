package org.opentext.async;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MainApp {
    public static void main(String[] args) throws Exception {
        SimpleTaskExecutor executor = new SimpleTaskExecutor(3);

        Main.TaskGroup g1 = new Main.TaskGroup(UUID.randomUUID());
        Main.TaskGroup g2 = new Main.TaskGroup(UUID.randomUUID());

        // Submit sample tasks
        for (int i = 1; i <= 6; i++) {
            int num = i;
            Main.Task<String> task = new Main.Task<>(
                    UUID.randomUUID(),
                    (i % 2 == 0 ? g1 : g2),
                    Main.TaskType.READ,
                    () -> {
                        System.out.println("[TASK] Executing work in task " + num);
                        Thread.sleep(500);
                        return "Result-" + num;
                    }
            );
            @SuppressWarnings("unchecked")
            CompletableFuture<String> future = (CompletableFuture<String>) executor.submitTask(task);
            future.whenComplete((res, ex) -> {
                if (ex == null) {
                    System.out.println("[MAIN] Got result for task " + num + ": " + res);
                } else {
                    System.out.println("[MAIN] Task " + num + " error: " + ex.getMessage());
                }
            });
        }

        // Allow tasks to complete
        Thread.sleep(4000);
        executor.shutdown();
    }
}
