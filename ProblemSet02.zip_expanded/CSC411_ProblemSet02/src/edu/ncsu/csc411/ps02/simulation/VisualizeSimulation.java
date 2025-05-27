package edu.ncsu.csc411.ps02.simulation;

import edu.ncsu.csc411.ps02.utils.ConfigurationLoader;
import edu.ncsu.csc411.ps02.utils.MapManager;
import edu.ncsu.csc411.ps02.agent.Robot;
import edu.ncsu.csc411.ps02.environment.Environment;
import edu.ncsu.csc411.ps02.environment.Position;
import edu.ncsu.csc411.ps02.environment.Tile;
import edu.ncsu.csc411.ps02.environment.TileStatus;
import edu.ncsu.csc411.ps02.utils.ColorPalette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A Visual Guide toward testing whether your robot
 * agent is operating correctly. This visualization
 * will run for 200 time steps. If the agent reaches
 * the target location before the 200th time step, the
 * simulation will end automatically.
 * You are free to modify the environment for test cases.
 * @author Adam Gaweda
 */
public class VisualizeSimulation extends JFrame {
	private static final long serialVersionUID = 1L;
	private EnvironmentPanel envPanel;
	private Environment env;
	private String mapFile = "maps/public/map01.txt";
	private String configFile = "config/configSmall.txt";
	
	/* Builds the environment; while not necessary for this problem set,
	 * this could be modified to allow for different types of environments,
	 * for example loading from a file, or creating multiple agents that
	 * can communicate/interact with each other.
	 * 
	 * The map variable allows you to customize the environment to any configuration.
	 * Each line in the list represents a row in the environment, and each character in
	 * a string represents a column. The Environment constructor that accepts a String list
	 * will review each character and set that tile's status to one of the following mappings.
	 *   'D': TileStatus.DIRTY
	 *   'C': TileStatus.CLEAN
	 *   'W': TileStatus.IMPASSABLE
	 *   'T': TileStatus.TARGET
	 */
	public VisualizeSimulation() {
		// Currently uses the first public test case
		Properties properties = ConfigurationLoader.loadConfiguration(configFile);
		int ITERATIONS = Integer.parseInt(properties.getProperty("ITERATIONS", "200"));
		int TILESIZE = Integer.parseInt(properties.getProperty("TILESIZE", "50"));
		int DELAY = Integer.parseInt(properties.getProperty("DELAY", "200"));
		boolean DEBUG = Boolean.parseBoolean(properties.getProperty("DEBUG", "true"));
		
		// Currently loads the first public test case, but you can change the map file
		// or make your own!
		String[] map = MapManager.loadMap(mapFile);
		this.env = new Environment(map);
    	envPanel = new EnvironmentPanel(this.env, ITERATIONS, TILESIZE, DELAY, DEBUG);
    	add(envPanel);
	}
	
	public static void main(String[] args) {
	    JFrame frame = new VisualizeSimulation();

	    frame.setTitle("CSC 411 - Problem Set 02");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);
    }
}

@SuppressWarnings("serial")
class EnvironmentPanel extends JPanel{
	private Timer timer;
	private Environment env;
	private ArrayList<Robot> robots;
	private static int ITERATIONS;
	public static int TILESIZE;
	public static int DELAY; // milliseconds
	public static boolean DEBUG;
	
	// Designs a GUI Panel based on the dimensions of the Environment and implements 
	// a Timer object to run the simulation. This timer will iterate through time-steps
	// with a 200ms delay (or wait 200ms before updating again).
	public EnvironmentPanel(Environment env, int iterations, int tilesize, int delay, boolean debug) {
		ITERATIONS = iterations;
		TILESIZE = tilesize;
		DELAY = delay;
		DEBUG = debug;
		
	    setPreferredSize(new Dimension(env.getCols()*TILESIZE, env.getRows()*TILESIZE));
		this.env = env;
		this.robots = env.getRobots();
		
		this.timer = new Timer(DELAY, new ActionListener() {
			int timeStepCount = 0;
			public void actionPerformed(ActionEvent e) {
				try {
					// Wrapped in try/catch in case the Robot's decision results
					// in a crash; we'll treat that the same as Action.DO_NOTHING
					env.updateEnvironment();
				} catch (Exception ex) {
					if (DEBUG) {
						String error = "[ERROR AGENT CRASH AT TIME STEP %03d] %s\n";
						System.out.printf(error, timeStepCount, ex);
					}
				}
				// Redraw the Visualization
				repaint();
				// Increment how many steps have been taken
				timeStepCount++;
				
				// Stop the simulation if either of the following conditions occur
				// 1) The Agent has reached the TARGET position
				if (env.goalConditionMet()) {
					String line = "Goal Condition was met in %02d steps!";
					String msg = String.format(line, timeStepCount);
					System.out.println(msg);
					timer.stop();
				}
				// 2) The simulation has iterated through the passed number of iterations
				if (timeStepCount == ITERATIONS) {
					String line = "Goal Condition was not met after %02d steps...";
					String msg = String.format(line, timeStepCount);
					System.out.println(msg);
					timer.stop();
				}
			}
		});
		this.timer.start();
	}
	
	/*
	 * The paintComponent method draws all of the objects onto the
	 * panel. This is updated at each time step when we call repaint().
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

	    // Draw a border around the entire environment
	    g.setColor(ColorPalette.BLACK);
	    g.drawRect(0, 0, env.getCols() * TILESIZE, env.getRows() * TILESIZE);

	    // Paint Environment Tiles with a gradient effect for cleaner visuals
	    Map<Position, Tile> tiles = env.getTiles();
	    for (Map.Entry<Position, Tile> entry : tiles.entrySet()) {
	        Position pos = entry.getKey();
	        Tile tile = entry.getValue();

	        // Apply a gradient effect for tiles
	        Graphics2D g2d = (Graphics2D) g;
	        int x = pos.getCol() * TILESIZE;
	        int y = pos.getRow() * TILESIZE;

	        Color startColor;
	        Color endColor;

	        if (tile.getStatus() == TileStatus.CLEAN) {
	            startColor = ColorPalette.RED;
	            endColor = ColorPalette.SILVER;
	        } else if (tile.getStatus() == TileStatus.DIRTY) {
	            startColor = ColorPalette.ORANGE;
	            endColor = ColorPalette.BROWN;
	        } else if (tile.getStatus() == TileStatus.IMPASSABLE) {
	            startColor = ColorPalette.CONCRETE;
	            endColor = ColorPalette.BLACK;
	        } else {
	            startColor = ColorPalette.WHITE;
	            endColor = ColorPalette.LIGHTYELLOW;
	        }

	        // Create and apply the gradient
	        GradientPaint gradient = new GradientPaint(x, y, startColor, x + TILESIZE, y + TILESIZE, endColor);
	        g2d.setPaint(gradient);
	        g2d.fillRect(x, y, TILESIZE, TILESIZE);

	        // Draw the gridlines
	        g.setColor(ColorPalette.BLACK);
	        g.drawRect(x, y, TILESIZE, TILESIZE);
	    }

	    // Paint Robots with a more vibrant appearance
	    for (Robot robot : robots) {
	        Position robotPos = env.getRobotPosition(robot);

	        // Draw the robot as a filled circle with a glowing effect
	        int x = robotPos.getCol() * TILESIZE + (TILESIZE / 4);
	        int y = robotPos.getRow() * TILESIZE + (TILESIZE / 4);
	        int size = TILESIZE / 2;

	        // Outer glow effect
	        g.setColor(new Color(0, 255, 0, 128)); // Semi-transparent green
	        g.fillOval(x - size / 4, y - size / 4, size + size / 2, size + size / 2);

	        // Main robot color
	        g.setColor(ColorPalette.GREEN);
	        g.fillOval(x, y, size, size);

	        // Robot outline
	        g.setColor(ColorPalette.BLACK);
	        g.drawOval(x, y, size, size);
	    }
	}
}