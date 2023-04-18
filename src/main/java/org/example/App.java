package org.example;

import GUI.SimulationFrame;
import model.Task;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        SimulationFrame simulationFrame = new SimulationFrame();
        simulationFrame.SimulationFrameGenerate();
    }
}
