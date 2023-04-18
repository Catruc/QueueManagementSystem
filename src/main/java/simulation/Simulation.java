package simulation;

import model.QueueManager;
import model.Server;
import model.Task;

import java.util.concurrent.BlockingQueue;

public class Simulation {

    public static void main(String[] args) {
        int numServers = 2;
        int numTasks = 4;
        int maxSimulationTime = 60;
        // Create the QueueManager
        QueueManager queueManager = new QueueManager(numServers,maxSimulationTime);

        // Generate random tasks
        int minArrivalTime = 2;
        int maxArrivalTime = 30;
        int minServiceTime = 2;
        int maxServiceTime = 4;
        BlockingQueue<Task> tasks = Task.generateRandomTask(numTasks, minArrivalTime, maxArrivalTime, minServiceTime, maxServiceTime);
        // Add tasks to the queue manager
        for (Task task : tasks) {
            queueManager.addClient(task);
        }

        // Process the clients
        queueManager.processClients(queueManager.servers, maxSimulationTime);
        queueManager.writeQueueHistoryToFile("queue_history.txt");
    }
}
