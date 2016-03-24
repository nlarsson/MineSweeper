import javax.swing.JButton;

public class GameButton extends JButton {
	private int xPos;
	private int yPos;
	private int state;
	public static int NORMAL = 0;
	public static int FLAG = 1;
	public static int QUESTION = 2;
	
	public GameButton(int xPosition, int yPosition) {
		super("");
		xPos = xPosition;
		yPos = yPosition;
		state = GameButton.NORMAL;
	}
	
	public int getXPos() {
		return xPos;
	}
	
	public int getYPos() {
		return yPos;
	}
	
	public void changeState() {
		state++;
		if (state > 2) {
			state = 0;
		}
	}
	
	public int getState() {
		return state;
	}
}
