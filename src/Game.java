/*
 * Game.java	1.0	27/11/12
 * 
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;

/**
 * 
 * 
 * @author Niklas Larsson
 * @version 1.0
 */
public class Game {
	/*
	 * VARIABLE DECLARATION
	 */
	//TODO Javadoc-comments on all variables
	private JFrame frame;
	private JButton restart;
	private int[][] grid;
	private Coordinate[] mineLocation;
	private JPanel[][] panelGrid;
	private JLabel information;
	private JLabel timeDisplay;
	private GridLayout layout;
	private boolean dead;
	private boolean started;
	private int gameMode;
	private JPanel buttonPanel;
	private int amountFlaggedMines;
	private long startTime;
	private long endTime;
	private Thread timeThread;
	final public static int SMALL = 10;
	final public static int MEDIUM = 20;
	final public static int LARGE = 30;
	
	/*
	 * CONSTRUCTORS
	 */
	public Game() {
		frame = new JFrame("MineSweeper");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dead = false;
		started = false;
		amountFlaggedMines = 0;
		layout = new GridLayout(Game.SMALL, Game.SMALL);
		gameMode = Game.SMALL;
	}
	public Game(int mode) {
		frame = new JFrame("MineSweeper");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dead = false;
		started = false;
		amountFlaggedMines = 0;
		layout = new GridLayout(mode, mode);
		gameMode = mode;
	}
	
	/*
	 * METHODS
	 */
	/**
	 * Launches the GUI
	 */
	public void go() {
		restart = new JButton("Restart");
		information = new JLabel("Mines Left: ");
		timeDisplay = new JLabel("0");
		JPanel rightAlign = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
		rightAlign.add(timeDisplay);
		JPanel informationPanel = new JPanel(new GridLayout(1, 3));
		informationPanel.add(information);
		informationPanel.add(restart);
		informationPanel.add(rightAlign);
		restart.addActionListener(new RestartButtonListener());
		frame.getContentPane().add(BorderLayout.NORTH, informationPanel);
		fillGrid(gameMode);
		information.setText("Mines Left: " + (mineLocation.length - amountFlaggedMines));
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
		frame.setLocation(100, 50);
	}//End go method
	
	/**
	 * Fills the grid with with all the buttons and places mines at random locations.
	 * Mines are 10% of the size of the game times itself.
	 * 
	 * @param in int The size of the game. 
	 */
	private void fillGrid(int in) {
		grid = new int[in][in];
		int numberOfMines = (int)((in * in) / 10);
		mineLocation = new Coordinate[numberOfMines];
		for (int i = 0; i < numberOfMines; i++) {
			int xPos = (int) (Math.random() * in);
			int yPos = (int) (Math.random() * in);
			if (grid[xPos][yPos] != 1) {
				grid[xPos][yPos] = 1;
				mineLocation[i] = new Coordinate(xPos, yPos);
			} else {
				i--;
			}
		}
		panelGrid = new JPanel[in][in];
		buttonPanel = new JPanel(layout);
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				panelGrid[i][j] = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); 
				GameButton button = new GameButton(i,j);
				button.setPreferredSize(new Dimension(20, 20));
				button.addActionListener(new GameButtonListener());
				button.addMouseListener(new RightClickListener());
				panelGrid[i][j].add(button);
				buttonPanel.add(panelGrid[i][j]);
			}
		}
		frame.getContentPane().add(BorderLayout.CENTER, buttonPanel);
	}//End fillGrid method
	
	/**
	 * checks if how many mines there is around a specific button, if there are none
	 * then check the areas of the surrounding buttons as well.
	 * 
	 * @param buttonPressed GameButton Which button that was pressed.
	 */
	public void checkArea(GameButton buttonPressed) {
		//Stores all the coordinates around the selected coordinate.
		// 8 is used for there are only max 8 squares around the selected coordinate.
		int xPos = buttonPressed.getXPos();
		int yPos = buttonPressed.getYPos();
		Coordinate[] coordinates = new Coordinate[8];
		int nearbyMines = 0;
		for (int i = 0; i < 8; i++) {
			switch(i) {
				case 0:
					if (xPos > 0 && yPos > 0) { // upper left corner
						coordinates[i] = new Coordinate((xPos - 1), (yPos - 1));
					}
					break;
				case 1:
					if (xPos > 0) { // straight up
						coordinates[i] = new Coordinate((xPos - 1), yPos);
					}
					break;
				case 2:
					if (xPos > 0 && yPos < (grid.length - 1)) {// upper right corner
						coordinates[i] = new Coordinate((xPos -1), (yPos + 1));
					}
					break;
				case 3:
					if (yPos > 0) { // straight left
						coordinates[i] = new Coordinate(xPos, (yPos -1));
					}
					break;
				case 4:
					if (yPos < (grid.length - 1)) { // straight right
						coordinates[i] = new Coordinate(xPos, (yPos + 1));
					}
					break;
				case 5:
					if (yPos > 0 && xPos < (grid.length - 1)) { // bottom left corner
						coordinates[i] = new Coordinate((xPos + 1), (yPos - 1));
					}
					break;
				case 6: 
					if (xPos < (grid.length - 1)) { // straight down
						coordinates[i] = new Coordinate((xPos + 1), yPos);
					}
					break;
				case 7:
					if (xPos < (grid.length - 1) && yPos < (grid.length - 1)) { // bottom right corner
						coordinates[i] = new Coordinate((xPos + 1), (yPos + 1));
					}
					break;
				}
			}
		for (int i = 0; i < coordinates.length; i++) {
			if (coordinates[i] != null && grid[coordinates[i].getXPos()][coordinates[i].getYPos()] == 1) {
				nearbyMines++;
			}
		}
		panelGrid[buttonPressed.getXPos()][buttonPressed.getYPos()].remove(buttonPressed);
		panelGrid[buttonPressed.getXPos()][buttonPressed.getYPos()].add(new JLabel("" + ((nearbyMines > 0) ? nearbyMines : "")));
		/*
		 * If there are no nearby mines then check the surrounding buttons. This is was creates the effect of many buttons
		 * disappear at the same time. 
		 */
		if (nearbyMines == 0) {
			for(int i = 0; i < coordinates.length; i++) {
				if (coordinates[i] != null) {
					try {
						//If casting is successful then there is a button in the particular JPanel
						GameButton temp = (GameButton) panelGrid[coordinates[i].getXPos()][coordinates[i].getYPos()].getComponent(0);
						if (temp.getState() != GameButton.NORMAL) {
							amountFlaggedMines--;
						}
						checkArea(temp);
					} catch(ClassCastException cce) { }
				}
			}
		}
		panelGrid[buttonPressed.getXPos()][buttonPressed.getYPos()].setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
		panelGrid[buttonPressed.getXPos()][buttonPressed.getYPos()].updateUI();
	}//End checkArea method
	
	/**
	 * Displays all mines.
	 */
	public void showAllMines() {
		for (Coordinate temp : mineLocation) {
			panelGrid[temp.getXPos()][temp.getYPos()].removeAll();
			JLabel mine = new JLabel(new ImageIcon("mine.png"));
			mine.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
			panelGrid[temp.getXPos()][temp.getYPos()].add(mine);
			panelGrid[temp.getXPos()][temp.getYPos()].updateUI();
			dead = true;
			endTime = setTime();
		}
	}//End showAllMines method
	
	/**
	 * Checks how many buttons there are left on the grid.
	 * 
	 * @return int The number of buttons.
	 */
	public int numberOfButtonsLeft() {
		int num = 0;
		for (int i = 0; i < panelGrid.length; i++) {
			for (int j = 0; j < panelGrid[i].length; j++) {
				try {
					GameButton temp = (GameButton) panelGrid[i][j].getComponent(0);
					num++;
				} catch(ClassCastException cce) { }
			}
		}
		return num;
	}//End numberOfButtonsLeft method
	
	/**
	 * Gets the time in seconds since 1970-01-01 00:00:00
	 * 
	 * @return long The time right now
	 */
	private long setTime() {
		Date d = new Date();
		return (d.getTime() / 1000);
	}
	
	/*
	 * INNER CLASSES
	 */
	/**
	 * Handles the actions when a GameButton is pressed.
	 * 
	 * @author Niklas Larsson
	 * @version 1.0
	 */
	private class GameButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			GameButton buttonPressed = ((GameButton) event.getSource());
			if (!dead && buttonPressed.getState() < 1) {
				if (grid[buttonPressed.getXPos()][buttonPressed.getYPos()] != 1) {
					checkArea(buttonPressed);
					if (numberOfButtonsLeft() == mineLocation.length) {
						//WIN!
						dead = true;
						endTime = setTime();
						timeDisplay.setText("" + (endTime - startTime));
						//TODO Highscore list?
						JOptionPane.showMessageDialog(frame, "You win!\nIt took " + (endTime - startTime) + " seconds.");
					}
				} else {
					showAllMines();
					dead = true;
				}
			}
			
			if (!started) {
				startTime = setTime();
				timeThread = new Thread(new TimeRunnable());
				timeThread.start();
				started = true;
			}
		}//End actionPerformed method
	}//End GameButtonListener inner class
	
	/**
	 * Handles the actions when the Restart-button is clicked.
	 * 
	 * @author Niklas Larsson
	 * @version 1.0
	 */
	private class RestartButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			dead = false;
			started = false;
			startTime = 0;
			endTime = 0;
			frame.remove(buttonPanel);
			fillGrid(gameMode);
			buttonPanel.updateUI();
			amountFlaggedMines = 0;
			information.setText("Mines Left: " + (mineLocation.length - amountFlaggedMines));
			timeDisplay.setText("0");
		}//End actionPerformed method
	}//End RestartButtonListener inner class
	
	/**
	 * Handles the actions performed when the right button on the mouse is clicked.
	 * 
	 * @author Niklas Larsson
	 * @version 1.0
	 */
	private class RightClickListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON3 && !dead) {
				GameButton buttonPressed = (GameButton) event.getSource();
				buttonPressed.changeState();
				if (buttonPressed.getState() == GameButton.NORMAL) {
					buttonPressed.setIcon(null);
				}
				if (buttonPressed.getState() == GameButton.FLAG) {
					amountFlaggedMines++;
					if (amountFlaggedMines > mineLocation.length) {
						buttonPressed.changeState();
					} else {
						buttonPressed.setIcon(new ImageIcon("flag.png"));
						information.setText("Mines Left: " + (mineLocation.length - amountFlaggedMines));
					}
				} 
				if (buttonPressed.getState() == GameButton.QUESTION){
					buttonPressed.setIcon(new ImageIcon("questionmark.png"));
					amountFlaggedMines--;
					information.setText("Mines Left: " + (mineLocation.length - amountFlaggedMines));
				}
			}
		}//End mouseClicked method
	}//End RightClickListener inner class
	
	private class TimeRunnable implements Runnable {
		@Override
		public void run() {
			while(!dead) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) { }
				timeDisplay.setText("" + (setTime() - startTime));
			}
		}
	}
}//End Game class
