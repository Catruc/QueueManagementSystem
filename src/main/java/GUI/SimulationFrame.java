package GUI;

import model.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;

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
        JTextField simulationResultsTextField = new JTextField();

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
        simulationResultsTextField.setBounds(10, 255, 550, 500);

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

                if(simulationInterval<maxArrivalTime && simulationInterval<maxServiceTime && simulationInterval<minArrivalTime && simulationInterval<minServiceTime && minArrivalTime>maxArrivalTime && minServiceTime>maxServiceTime){
                    JOptionPane.showMessageDialog(frame, "Simulation interval must be greater than maximum arrival time and maximum service time");
                    return;
                }

                BlockingQueue<Task> tasks = Task.generateRandomTask(numberOfClients, minArrivalTime, maxArrivalTime, minServiceTime, maxServiceTime);

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
        frame.add(simulationResultsTextField);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SimulationFrame simulationFrame = new SimulationFrame();
        simulationFrame.SimulationFrameGenerate();
    }

}
