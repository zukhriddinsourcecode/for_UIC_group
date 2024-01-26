package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CounterIncrementer {

    public static void main(String[] args) {
        // Number of threads to initiate
        int numberOfThreads = 100_000;

        // Thread pool with 10 threads
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // Atomic counters to ensure atomic operations
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);

        // Submit tasks to the thread pool
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                // Atomic increment of counters
                synchronized (CounterIncrementer.class) {
                    counter1.incrementAndGet();
                    counter2.incrementAndGet();
                }
            });
        }

        // Shutdown the executor service and wait for all tasks to complete
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Display the counter values
        System.out.println("Counter1: " + counter1.get());
        System.out.println("Counter2: " + counter2.get());
    }
}
