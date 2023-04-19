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
        this.queueHistory = new StringBuilder();
        tasksPerTimeInterval = new HashMap<>();
        peakHour = new AtomicInteger(0);

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



    public synchronized void distributeClient(Task client) {
        if (client.getArrivalTime() <= maxSimulationTime) {
            int minTasks = Integer.MAX_VALUE;
            int minServiceTime = Integer.MAX_VALUE;
            Server bestServer = null;

            for (Server server : servers) {
                int currentTasks = server.getTasks().size();
                int currentServiceTime = getTotalServiceTime(server);

                if (currentTasks < minTasks || (currentTasks == minTasks && currentServiceTime < minServiceTime)) {
                    minTasks = currentTasks;
                    minServiceTime = currentServiceTime;
                    bestServer = server;
                }
            }

            if (bestServer != null) {
                bestServer.addTask(client);
            }
        }
    }

    public void findPeakHour() {
        int peakHourValue = tasksPerTimeInterval.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow(RuntimeException::new).getKey();
        peakHour.set(peakHourValue);
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

    public void processClients(List<Server> servers, int maxSimulationTime) {
        ExecutorService executorService = Executors.newFixedThreadPool(numServers);
        int currentTime = 0;
        int stop = 1;
        while (!waitingClients.isEmpty() || currentTime < maxSimulationTime||stop==1) {
            while (!waitingClients.isEmpty() && waitingClients.peek().getArrivalTime() == currentTime) {
                Task client = waitingClients.poll();
                distributeClient(client);
            }
            printLiveStatus(currentTime,executorService);
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
            try {
                Thread.sleep(1000); // Sleep for 1 second before calculating the average waiting time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Simulation finished");
            queueHistory.append("Simulation finished").append(System.lineSeparator());
            findPeakHour();
            queueHistory.append("Peak hour: ").append(peakHour).append(System.lineSeparator());
        }
    }

    public void writeQueueHistoryToFile(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.print(queueHistory.toString());
        } catch (IOException e) {
            System.err.println("Error writing queue history to file: " + e.getMessage());
        }
    }


}
