package model;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

public class QueueManager {

    public List<Server> servers;
    private int numServers;
    private PriorityQueue<Task> waitingClients;
    private int maxSimulationTime;
    private CyclicBarrier barrier;
    private StringBuilder queueHistory;

    public QueueManager(int numServers, int maxSimulationTime) {
        this.numServers = numServers;     // Number of servers
        this.maxSimulationTime = maxSimulationTime;  // Maximum simulation time
        servers = new ArrayList<>();  // List of servers
        waitingClients = new PriorityQueue<>(Comparator.comparingInt(Task::getArrivalTime));  // List of waiting clients
        barrier = new CyclicBarrier(numServers);   // Cyclic barrier for the servers
        this.queueHistory = new StringBuilder();

        for (int i = 0; i < numServers; i++) {
            BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();  // Create a queue for each server
            String serverName = "QUEUE"+(i+1);
            Server server = new Server(taskQueue, maxSimulationTime, barrier,serverName);  // Create a server with the queue
            servers.add(server);  // Add the server to the list of servers
            Thread serverThread = new Thread(server);  // Create a thread for the server
            serverThread.setName(serverName);  // Set the name of the thread
            serverThread.start();  // Start the thread
        }

    }

    public void addClient(Task task) {
        waitingClients.add(task);
    }

    public String printWaitingClients() {
        return "Clients not yet in any queue: " + waitingClients;
    }

    public void distributeClient(Task client) {
        if (client.getArrivalTime() <= maxSimulationTime) {
            Server bestServer = getServerWithMinimumWaitingTime(servers);
            bestServer.addTask(client);
        }
    }

    public void processClients(List<Server> servers, int maxSimulationTime) {
        ExecutorService executorService = Executors.newFixedThreadPool(numServers);
        int currentTime = 0;
        int stop = 1;
        while (!waitingClients.isEmpty() || currentTime < maxSimulationTime) {
            while (!waitingClients.isEmpty() && waitingClients.peek().getArrivalTime() == currentTime) {
                Task client = waitingClients.poll();
                distributeClient(client);
            }
            System.out.println("Time: " + currentTime);
            queueHistory.append("Time: ").append(currentTime).append(System.lineSeparator());
            String waitingClientsStatus = printWaitingClients();
            System.out.println(waitingClientsStatus);
            queueHistory.append(waitingClientsStatus).append(System.lineSeparator());
            for (Server server : servers) {
                Future<?> future = executorService.submit(server::printQueueStatusForConsole);  // Print the queue status of each server
                try {
                    future.get();  // Wait for the task to complete before proceeding to the next server
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            servers.forEach(server -> {
                //server.printQueueStatus();
                queueHistory.append(server.printQueueStatus()).append(System.lineSeparator());
            });
            try {
                Thread.sleep(1000); // Sleep for 1 second to simulate 1 second of simulation time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentTime++;
            stop = 0;
        }
        executorService.shutdown();
        if (stop == 0) {
            System.out.println("Simulation finished");
        }
    }

    public void writeQueueHistoryToFile(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.print(queueHistory.toString());
        } catch (IOException e) {
            System.err.println("Error writing queue history to file: " + e.getMessage());
        }
    }

    public static Server getServerWithMinimumWaitingTime(List<Server> servers) {
        return servers.stream().min(Comparator.comparingInt(server -> server.getWaitingPeriod().get())).orElseThrow(RuntimeException::new);
    }


}
