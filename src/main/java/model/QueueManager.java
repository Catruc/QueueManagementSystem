package model;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueManager {

    public ArrayList<Server> servers;
    private int numServers;
    private PriorityQueue<Task> waitingClients;
    private int maxSimulationTime;
    private CyclicBarrier barrier;
    public StringBuilder queueHistory;
    private Map<Integer, Integer> tasksPerTimeInterval;
    private AtomicInteger peakHour;


    public QueueManager(int numServers, int maxSimulationTime) {
        this.numServers = numServers;     // Number of servers
        this.maxSimulationTime = maxSimulationTime;  // Maximum simulation time
        servers = new ArrayList<>();  // List of servers
        waitingClients = new PriorityQueue<>(Comparator.comparingInt(Task::getArrivalTime));  // List of waiting clients
        barrier = new CyclicBarrier(numServers);   // Cyclic barrier for the servers
        this.queueHistory = new StringBuilder();    // String builder for the queue history
        tasksPerTimeInterval = new HashMap<>();    // Map for the tasks per time interval
        peakHour = new AtomicInteger(0);   // Atomic integer for the peak hour

        for (int i = 0; i < numServers; i++) {
            BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();  // Create a queue for each server
            String serverName = "QUEUE"+(i+1);   // Create a name for each server
            Server server = new Server(taskQueue, maxSimulationTime, barrier,serverName);  // Create a server with the queue
            servers.add(server);  // Add the server to the list of servers
            Thread serverThread = new Thread(server);  // Create a thread for the server
            serverThread.setName(serverName);  // Set the name of the thread
            serverThread.start();  // Start the thread
        }

    }


    public AtomicInteger getPeakHour() {
        return peakHour;
    }

    public void addClient(Task task) {
        waitingClients.add(task);
    }

    public String printWaitingClients() {
        return "Clients not yet in any queue: " + waitingClients;
    }


    public static int getTotalServiceTime(Server server) {
        return server.getTasks().stream().mapToInt(Task::getServiceTime).sum();
    }



    public synchronized void distributeClient(Task client) {   // Distribute the client to the server with the least tasks
        if (client.getArrivalTime() <= maxSimulationTime) {  // If the client's arrival time is less than the maximum simulation time
            int minTasks = Integer.MAX_VALUE;   // Minimum number of tasks
            int minServiceTime = Integer.MAX_VALUE;   // Minimum service time
            Server bestServer = null;   // Best server

            for (Server server : servers) {   // For each server
                int currentTasks = server.getTasks().size();  // Get the number of tasks
                int currentServiceTime = getTotalServiceTime(server);   // Get the total service time

                if (currentTasks < minTasks || (currentTasks == minTasks && currentServiceTime < minServiceTime)) {  // If the current number of tasks is less than the minimum number of tasks or if the current number of tasks is equal to the minimum number of tasks and the current service time is less than the minimum service time
                    minTasks = currentTasks;   // Set the minimum number of tasks to the current number of tasks
                    minServiceTime = currentServiceTime;   // Set the minimum service time to the current service time
                    bestServer = server;   // Set the best server to the current server
                }
            }

            if (bestServer != null) {   // If the best server is not null
                bestServer.addTask(client);   // Add the client to the best server
            }
        }
    }

    public void findPeakHour() {
        int peakHourValue = tasksPerTimeInterval.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow(RuntimeException::new).getKey();   // Find the peak hour
        peakHour.set(peakHourValue);   // Set the peak hour
    }


    public void printLiveStatus(int currentTime,ExecutorService executorService){
        int totalTasks = servers.stream().mapToInt(server -> server.getTasks().size()).sum();
        tasksPerTimeInterval.put(currentTime, totalTasks);
        System.out.println("Time: " + currentTime);
        queueHistory.append("Time: ").append(currentTime).append(System.lineSeparator());
        String waitingClientsStatus = printWaitingClients();
        System.out.println(waitingClientsStatus);
        queueHistory.append(waitingClientsStatus).append(System.lineSeparator());
        for (Server server : servers) {
            Future<?> future = executorService.submit(server::printQueueStatusForConsole);  // Print the queue status of each server
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        servers.forEach(server -> {
            queueHistory.append(server.printQueueStatus()).append(System.lineSeparator());
        });
    }

    public void processClients(List<Server> servers, int maxSimulationTime, List<Task> tasks) {
        ExecutorService executorService = Executors.newFixedThreadPool(numServers);  // Create a fixed thread pool with the number of servers
        int currentTime = 0;  // Current time
        int stop = 1;  // Stop variable
        double averageServingTime = calculateAverageServingTime(tasks);  // Calculate the average serving time
        while (!waitingClients.isEmpty() || currentTime < maxSimulationTime||stop==1) {   // While there are waiting clients or the current time is less than the maximum simulation time
            while (!waitingClients.isEmpty() && waitingClients.peek().getArrivalTime() == currentTime) {   // While there are waiting clients and the arrival time of the first client is equal to the current time
                Task client = waitingClients.poll();   // Get the first client
                distributeClient(client);   // Distribute the client
            }
            printLiveStatus(currentTime,executorService);

            try {
                Thread.sleep(1000); // Sleep for 1 second to simulate 1 second of simulation time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentTime++;   // Increment the current time
            stop = 0;   // Set the stop variable to 0
        }
        executorService.shutdown();   // Shutdown the executor service
        if (stop == 0) {
            queueHistory.append("Simulation finished").append(System.lineSeparator());
            findPeakHour();
            queueHistory.append("Peak hour: ").append(peakHour).append(System.lineSeparator());
            queueHistory.append("Average serving time: ").append(averageServingTime).append(System.lineSeparator());
            double averageWaitingTime = calculateAverageWaitingTime(); // Calculate the average waiting time
            queueHistory.append("Average waiting time: ").append(averageWaitingTime).append(System.lineSeparator()); // Append the average waiting time to the queue history
            System.out.println(averageWaitingTime);
        }
    }

    public void writeQueueHistoryToFile(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.print(queueHistory.toString());
        } catch (IOException e) {
            System.err.println("Error writing queue history to file: " + e.getMessage());
        }
    }

    public double calculateAverageServingTime(List<Task> tasks) {
        int totalServiceTime = 0;   // Total service time
        for (Task task : tasks) {
            totalServiceTime += task.getServiceTime();   // Add the service time of each task to the total service time
        }
        return (double) totalServiceTime / tasks.size();  // Return the average service time
    }

    public double calculateAverageWaitingTime() {
        int totalWaitingTime = 0;   // Total waiting time
        int totalProcessedTasks = 0;   // Total processed tasks

        for (Server server : servers) {   // For each server
            totalWaitingTime += server.getTotalWaitingTime();   // Add the total waiting time of each server to the total waiting time
            totalProcessedTasks += server.getTotalProcessedTasks();   // Add the total processed tasks of each server to the total processed tasks
        }

        return totalProcessedTasks == 0 ? 0.0 : (double) totalWaitingTime / totalProcessedTasks;  // Return the average waiting time
    }

}
