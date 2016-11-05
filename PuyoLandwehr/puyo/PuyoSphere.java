package puyo;

import java.awt.Point;
import java.util.Random;

//An individual Puyo sphere
public class PuyoSphere {
	
	//FIELDS
	
	//used in searching for sphere links
	public boolean isChecked = false;
	//color indicator for sphere (r,g,b,y)
	public char color;
	//current location of sphere on grid
	public Point location;
	//random number object for color
	private Random rand;
	private int numcolors = 4;
	
	//CONSTRUCTORS
	
	//create new sphere of given color
	public PuyoSphere(char color) {
		this.color = color;
		this.location = new Point();
	}
	
	//create new sphere with random color
	public PuyoSphere() {
		this.rand = new Random();
		int colorint = rand.nextInt(numcolors);
		switch(colorint) {
			case 0: this.color = 'r'; break;
			case 1: this.color = 'g'; break;
			case 2: this.color = 'b'; break;
			case 3: this.color = 'y'; break;
			default: ; break;
		}
		this.location = new Point();
	}
	
	//METHODS
}
