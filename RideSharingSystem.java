import java.util.*;
import java.util.concurrent.*;

public class RideSharingSystem {
    
    // Defining a Task class to hold a task ID
    static class Task {
        private final int id;

        // Creating a new Task with an ID
        public Task(int id) {
            this.id = id;
        }

        // Returning the task ID
        public int getId() {
            return id;
        }
    }

    // Creating a shared queue to store tasks safely
    static class TaskQueue {
        private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

        // Adding a task to the queue
        public void addTask(Task task) {
            queue.add(task);
        }

        // Getting a task from the queue, waiting if it's empty
        public Task getTask() throws InterruptedException {
            return queue.take();
        }
    }

    // Defining a Worker class that performs tasks
    static class Worker implements Runnable {
        private final int workerId;
        private final TaskQueue taskQueue;
        private final List<String> results;

        // Initializing the worker with ID, task queue, and result list
        public Worker(int id, TaskQueue queue, List<String> results) {
            this.workerId = id;
            this.taskQueue = queue;
            this.results = results;
        }

        @Override
        public void run() {
            try {
                // Repeating while tasks are available
                while (true) {
                    // Taking a task from the queue
                    Task task = taskQueue.getTask();

                    // Printing start of the task
                    System.out.println("Worker " + workerId + " started Task " + task.getId());

                    // Simulating task processing time
                    Thread.sleep(500);

                    // Creating the result string
                    String result = "Task " + task.getId() + " processed by Worker " + workerId;

                    // Adding result to the shared result list
                    synchronized (results) {
                        results.add(result);
                    }

                    // Printing completion of the task
                    System.out.println("Worker " + workerId + " finished Task " + task.getId());
                }
            } catch (InterruptedException e) {
                // Printing interruption message
                System.err.println("Worker " + workerId + " interrupted.");
            } catch (Exception e) {
                // Printing any other error messages
                System.err.println("Worker " + workerId + " error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        // Setting number of workers and tasks
        int numWorkers = 3;
        int numTasks = 10;

        // Creating the shared task queue and result list
        TaskQueue queue = new TaskQueue();
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // Adding tasks to the queue
        for (int i = 1; i <= numTasks; i++) {
            queue.addTask(new Task(i));
        }

        // Starting worker threads using a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(numWorkers);
        for (int i = 1; i <= numWorkers; i++) {
            executor.submit(new Worker(i, queue, results));
        }

        // Waiting enough time for all tasks to finish
        try {
            Thread.sleep(numTasks * 600L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stopping all workers after work is done
        executor.shutdownNow();

        // Waiting for all threads to stop
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Printing final results
        System.out.println("\nResults");
        for (String res : results) {
            System.out.println(res);
        }
    }
}
