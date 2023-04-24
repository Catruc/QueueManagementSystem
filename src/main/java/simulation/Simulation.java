package simulation;

import model.QueueManager;
import model.Server;
import model.Task;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class Simulation {

    public static void main(String[] args) {
        int numServers = 20;
        int numTasks = 1000;
        int maxSimulationTime = 200;
        // Create the QueueManager
        QueueManager queueManager = new QueueManager(numServers,maxSimulationTime);

        // Generate random tasks
        int minArrivalTime = 10;
        int maxArrivalTime = 100;
        int minServiceTime = 3;
        int maxServiceTime = 9;
        BlockingQueue<Task> tasks = Task.generateRandomTask(numTasks, minArrivalTime, maxArrivalTime, minServiceTime, maxServiceTime);
        double average=queueManager.calculateAverageServingTime(new ArrayList<>(tasks));
        // Add tasks to the queue manager
        for (Task task : tasks) {
            queueManager.addClient(task);
        }

        // Process the clients
        queueManager.processClients(queueManager.servers, maxSimulationTime,new ArrayList<>(tasks));
        queueManager.writeQueueHistoryToFile("queue_history3.txt");
    }
}
