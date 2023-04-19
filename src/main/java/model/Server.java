package model;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {

    private LinkedBlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod;
    private int maxSimulationTime;
    private CyclicBarrier barrier;
    private String serverName;


    public Server(BlockingQueue<Task> tasks, int maxSimulationTime,CyclicBarrier barrier,String serverName) {
        this.tasks = new LinkedBlockingQueue<>();
        this.maxSimulationTime = maxSimulationTime;
        this.waitingPeriod = new AtomicInteger(0);
        this.barrier=barrier;
        this.serverName=serverName;
    }

    public void addTask(Task task) {
        tasks.add(task);
        waitingPeriod.addAndGet(task.getServiceTime());
    }

    public BlockingQueue<Task> getTasks() {
        return tasks;
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }

    public void decrementServiceTime() {
        if (!tasks.isEmpty()) {
            Task currentTask = tasks.peek();
            if (currentTask.getServiceTime() > 0) {
                currentTask.setServiceTime(currentTask.getServiceTime() - 1);
            }
        }
    }

    @Override
    public void run() {

        int currentTime = 0;   // Current simulation time
        while (currentTime < maxSimulationTime) {   // Run until the maximum simulation time is reached
            try {
                Task currentTask = tasks.peek();   // Get the first task in the queue
                if (currentTask != null) {    // If there is a task in the queue
                    if (currentTask.getArrivalTime() <= currentTime) {
                        decrementServiceTime();   // Decrement the service time of the task
                        if (currentTask.getServiceTime() == 0) {    // If the service time of the task is 0, remove it from the queue
                            tasks.poll();   // Remove the task from the queue
                            waitingPeriod.addAndGet(-currentTask.getServiceTime());   // Decrement the waiting period of the server

                        }
                    }
                }
                //System.out.println("Current time: " + currentTime + "\n");

                barrier.await();  // Wait for all the servers to finish processing the current task
                //printQueueStatus();
                Thread.sleep(1000); // Sleep for 1 second to simulate 1 second of simulation time
                currentTime++;  // Increment the current simulation time
            } catch (Exception e) {
                e.printStackTrace();  // Print the stack trace if an exception is thrown
            }

        }
    }

    public String printQueueStatus() {
        return serverName + tasks;
    }

    public void printQueueStatusForConsole() {
        System.out.println(serverName + tasks);
    }

}