package model;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Task {

    private static AtomicInteger ID = new AtomicInteger(0);

    private int id;
    private int arrivalTime;
    private int serviceTime;

    public Task(int id, int arrivalTime, int serviceTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
    }

    public int getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }


    public static void setID(AtomicInteger ID) {
        Task.ID = ID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public static BlockingQueue<Task> generateRandomTask(int numberOfTasks, int minArrivalTime, int maxArrivalTime, int minServiceTime, int maxServiceTime)
    {
        Random generateRandom = new Random();
        BlockingQueue<Task> tasks = new LinkedBlockingQueue<>();
        for(int i = 0; i < numberOfTasks; i++) {
            int arrivalTime = generateRandom.nextInt(maxArrivalTime - minArrivalTime) + minArrivalTime;
            int serviceTime = generateRandom.nextInt(maxServiceTime - minServiceTime) + minServiceTime;
            tasks.add(new Task(ID.incrementAndGet(), arrivalTime, serviceTime));
        }
        return tasks;
    }

    @Override
    public String toString() {
        return "{" + id + "," + arrivalTime + "," + serviceTime + "}";
    }
}
