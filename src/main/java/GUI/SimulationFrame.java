package GUI;

import model.QueueManager;
import model.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.*;

public class SimulationFrame {


    public void SimulationFrameGenerate() {
        JFrame frame = new JFrame("Queues Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 800);
        frame.setLayout(null);

        JLabel numberOfClientsLabel = new JLabel("Number of clients: ");
        JTextField numberOfClientsTextField = new JTextField(10);
        JLabel numberOfQueuesLabel = new JLabel("Number of queues: ");
        JTextField numberOfQueuesTextField = new JTextField(10);
        JLabel simulationIntervalLabel = new JLabel("Simulation interval: ");
        JTextField simulationIntervalTextField = new JTextField(10);
        JLabel minimumArrivalTimeLabel = new JLabel("Minimum arrival time: ");
        JTextField minimumArrivalTimeTextField = new JTextField(10);
        JLabel maximumArrivalTimeLabel = new JLabel("Maximum arrival time: ");
        JTextField maximumArrivalTimeTextField = new JTextField(10);
        JLabel minimumServiceTimeLabel = new JLabel("Minimum service time: ");
        JTextField minimumServiceTimeTextField = new JTextField(10);
        JLabel maximumServiceTimeLabel = new JLabel("Maximum service time: ");
        JTextField maximumServiceTimeTextField = new JTextField(10);
        JButton startSimulationButton = new JButton("Start simulation");
        JTextArea simulationResultsTextArea = new JTextArea();
        JTextArea otherResultsTextArea = new JTextArea();

        numberOfClientsLabel.setBounds(10, 15, 150, 25);
        numberOfClientsTextField.setBounds(160, 15, 150, 25);
        numberOfQueuesLabel.setBounds(10, 45, 150, 25);
        numberOfQueuesTextField.setBounds(160, 45, 150, 25);
        simulationIntervalLabel.setBounds(10, 75, 150, 25);
        simulationIntervalTextField.setBounds(160, 75, 150, 25);
        minimumArrivalTimeLabel.setBounds(10, 105, 150, 25);
        minimumArrivalTimeTextField.setBounds(160, 105, 150, 25);
        maximumArrivalTimeLabel.setBounds(10, 135, 150, 25);
        maximumArrivalTimeTextField.setBounds(160, 135, 150, 25);
        minimumServiceTimeLabel.setBounds(10, 165, 150, 25);
        minimumServiceTimeTextField.setBounds(160, 165, 150, 25);
        maximumServiceTimeLabel.setBounds(10, 195, 150, 25);
        maximumServiceTimeTextField.setBounds(160, 195, 150, 25);
        startSimulationButton.setBounds(10, 225, 150, 25);
        simulationResultsTextArea.setBounds(10, 255, 550, 500);
        otherResultsTextArea.setBounds(320, 30, 260, 200);
        JScrollPane scrollPane = new JScrollPane(simulationResultsTextArea);
        scrollPane.setBounds(10, 255, 550, 500);

        startSimulationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int numberOfClients = Integer.parseInt(numberOfClientsTextField.getText());
                int numberOfQueues = Integer.parseInt(numberOfQueuesTextField.getText());
                int simulationInterval = Integer.parseInt(simulationIntervalTextField.getText());
                int minArrivalTime = Integer.parseInt(minimumArrivalTimeTextField.getText());
                int maxArrivalTime = Integer.parseInt(maximumArrivalTimeTextField.getText());
                int minServiceTime = Integer.parseInt(minimumServiceTimeTextField.getText());
                int maxServiceTime = Integer.parseInt(maximumServiceTimeTextField.getText());

                QueueManager queueManager = new QueueManager(numberOfQueues, simulationInterval);
                BlockingQueue<Task> tasks = Task.generateRandomTask(numberOfClients, minArrivalTime, maxArrivalTime, minServiceTime, maxServiceTime);
                // Add tasks to the queue manager
                for (Task task : tasks) {
                    queueManager.addClient(task);
                }
                double initialAverageServingTime = queueManager.calculateAverageServingTime(new ArrayList<>(tasks));
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                executorService.submit(() -> {
                    queueManager.processClients(queueManager.servers, simulationInterval,new ArrayList<>(tasks));
                    queueManager.findPeakHour(); // call findPeakHour after processClients
                    queueManager.calculateAverageWaitingTime();
                });

                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                simulationResultsTextArea.setText(queueManager.queueHistory.toString());
                                if (queueManager.getPeakHour().get() != -1) {
                                    otherResultsTextArea.setText("Peak hour: " + queueManager.getPeakHour().get()+"\n");
                                }
                                if (initialAverageServingTime != -1) {
                                    otherResultsTextArea.append("Average Serving Time: " + initialAverageServingTime + "\n");
                                }
                                double averageWaitingTime = queueManager.calculateAverageWaitingTime();
                                otherResultsTextArea.append("Average Waiting Time: " + averageWaitingTime + "\n");
                            }
                        });
                    }
                }, 0, 1, TimeUnit.SECONDS);
            }
        });


        frame.add(numberOfClientsLabel);
        frame.add(numberOfClientsTextField);
        frame.add(numberOfQueuesLabel);
        frame.add(numberOfQueuesTextField);
        frame.add(simulationIntervalLabel);
        frame.add(simulationIntervalTextField);
        frame.add(minimumArrivalTimeLabel);
        frame.add(minimumArrivalTimeTextField);
        frame.add(maximumArrivalTimeLabel);
        frame.add(maximumArrivalTimeTextField);
        frame.add(minimumServiceTimeLabel);
        frame.add(minimumServiceTimeTextField);
        frame.add(maximumServiceTimeLabel);
        frame.add(maximumServiceTimeTextField);
        frame.add(startSimulationButton);
        //frame.add(simulationResultsTextArea);
        frame.add(scrollPane);
        frame.add(otherResultsTextArea);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SimulationFrame simulationFrame = new SimulationFrame();
        simulationFrame.SimulationFrameGenerate();
    }

}
