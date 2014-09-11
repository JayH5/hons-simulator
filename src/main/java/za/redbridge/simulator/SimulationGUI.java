package za.redbridge.simulator;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;

import javax.swing.*;
import java.awt.*;

/**
 * Created by jamie on 2014/07/24.
 */

//this should be ExperimentGUI
public class SimulationGUI extends GUIState {

    private Display2D display;
    private JFrame displayFrame;
    private ContinuousPortrayal2D environmentPortrayal = new ContinuousPortrayal2D();

    public SimulationGUI(SimState state) {
        super(state);
    }

    @Override
    public void init (Controller controller) {
        super.init(controller);

        display = new Display2D(600, 600, this);

        display.setClipping(false);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Redbridge Bot Display");

        controller.registerFrame(displayFrame);

        displayFrame.setVisible(true);
        display.attach(environmentPortrayal, "Forage Area");
    }

    @Override
    public void start() {
        super.start();

        // Set the portrayal to display the environment
        final Simulation simulation = (Simulation) state;
        environmentPortrayal.setField(simulation.getEnvironment());

        // Set up the display
        display.reset();
        display.setBackdrop(Color.white);
        display.repaint();
    }

    @Override
    public void quit() {
        super.quit();

        if (displayFrame != null) {
            displayFrame.dispose();
        }

        displayFrame = null;

        display = null;
    }

}
