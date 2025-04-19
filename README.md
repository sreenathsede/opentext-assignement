# Simple Task Executor Assignment - OpenText

This is a minimal implementation of the `TaskExecutor` interface for the OpenText home assignment. It demonstrates how to build an asynchronous, ordered task processor in pure Java 17, without any external libraries.

---

##  Core Requirements

1. **Non‑blocking submission**: tasks are queued immediately.
2. **FIFO execution order**: first submitted → first started.
3. **Asynchronous execution**: tasks run on a fixed thread pool.
4. **Mutual exclusion by group**: tasks in the same `TaskGroup` never overlap.
5. **Result retrieval**: callers get a `Future<T>` for each task.
6. **Graceful shutdown**: dispatcher and executor stop cleanly.

---

##  Project Structure

```
src/
├── Main.java             # Provided: Task, TaskGroup, TaskType, TaskExecutor
├── SimpleTaskExecutor.java  # Your simplified executor implementation
└── MainApp.java          # Demo runner with sample tasks and output
```

---

You should see console logs tracing the entire lifecycle:

```
[SYSTEM] SimpleTaskExecutor started with pool size 3
[SUBMIT] Task submitted: 9f1a...  
[DISPATCHER] Dispatcher thread running
[DISPATCHER] Dispatching: 9f1a...
[EXECUTOR] Waiting for lock on group ... for task 9f1a...
[EXECUTOR] Acquired lock, running task 9f1a...
[TASK] Executing work in task 1
[EXECUTOR] Completed task 9f1a...
[MAIN] Got result for task 1: Result-1
...
[SYSTEM] Shutdown initiated
``` 

---

##  Implementation Highlights

- **BlockingQueue**: preserves FIFO order for submissions.
- **FixedThreadPool**: controls concurrency (configurable pool size).
- **`synchronized` on per‑group lock objects**: guarantees no two tasks in the same `TaskGroup` run together.
- **CompletableFuture**: returned as `Future<T>` to allow non‑blocking callbacks via `whenComplete(...)`.
- **Dispatcher thread**: continuously pulls tasks from the queue and submits them to the worker pool.

---

##  Assumptions & Notes

- FIFO constraint applies to the **start** of task execution.
- Group locking only serializes execution, not submission.
- No modifications were made to the provided `Main` types.
- All code is pure Java 17 standard library — no third‑party dependencies.

---
##  Author

Sreenath - Java Developer | Spring Boot | Microservices | Concurrent Programming Enthusiast

