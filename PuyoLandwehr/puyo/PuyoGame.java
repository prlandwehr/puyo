package puyo;

import java.awt.*;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

//The game class, handles input, drawing, and game status processing
public class PuyoGame extends JPanel implements ActionListener {

	//FIELDS
	
	private static final long serialVersionUID = 1L; //avoids compiler warnings
	private int targetframedt = 16; //delay between frames in ms, target fps is 60
	private int dropinterval = 45; //number of frames it takes active piece to drop 1 spot on the grid
	private int droptimer = 0; //timer for dropping active piece
	private boolean gameover = false;
	private boolean isClearPaused = false; //used to briefly pause input during sphere clearing
	private int delayonclear = 60; // ""
	private int cleartimer = 0; // ""
	private int chain = 1; //used to indicate multiplier based on chain clearing
	private int points = 0; //used to track points, 1 per sphere cleared unless the chain multiplier is raised
	//private JFrame parentframe; //the frame to which the game panel is attached
	private int gridx; //width of sphere grid
	private int gridy; //height of sphere grid
	private int spherewidth; //px. width of sphere
	private int sphereheight; //px. height of sphere
	private PuyoSphere[][] spheregrid; //array of spheres in x,y format
	private Timer timer; //swing type timer
	private enum CollisionType {S1, S2, BOTH, NONE}; //enum defining collision types
	
	private PuyoPiece activepiece; //the player controlled sphere pair
	
	//bitmaps for each sphere color
	private Image spherered;
	private Image spheregreen;
	private Image sphereblue;
	private Image sphereyellow;
	
	//CONSTRUCTORS
	
	//Constructor taking window and drawing related params
	public PuyoGame(int gridx, int gridy, int spherewidth, int sphereheight) {
		//General init
		//this.parentframe = pframe;
		this.gridx = gridx;
		this.gridy = gridy;
		this.spherewidth = spherewidth;
		this.sphereheight = sphereheight;
		this.spheregrid = new PuyoSphere[gridx][gridy];
		this.setBackground(Color.WHITE);
		this.timer = new Timer(targetframedt, this);
		this.addKeyListener(new InputProcessor());
		this.setFocusable(true);
		
		//Load in sphere images for later use
		ImageIcon sphereicon = new ImageIcon(getClass().getResource("images/puyo_red.png"));
        spherered = sphereicon.getImage();
        sphereicon = new ImageIcon(getClass().getResource("images/puyo_green.png"));
        spheregreen = sphereicon.getImage();
        sphereicon = new ImageIcon(getClass().getResource("images/puyo_blue.png"));
        sphereblue = sphereicon.getImage();
        sphereicon = new ImageIcon(getClass().getResource("images/puyo_yellow.png"));
        sphereyellow = sphereicon.getImage();
        
        activepiece = new PuyoPiece(gridx, gridy);
        
        timer.start();
	}
	
	//METHODS
	
	//game update loop, called once per frame as specified by the game timer
	private void update() {
		//Attempts to gain focus for this component if the program window has OS focus
		//a fix for a glitch where keyboard input isn't being registered
		requestFocusInWindow();
		
		//If the game is over skip the fancy stuff
		if(gameover) {
			repaint();
			return;
		}
		
		//If paused for link clearing, act as such
		if(isClearPaused) {
			if(delayonclear >= cleartimer) {
				cleartimer += 1;
			}
			else {
				cleartimer = 0;
				isClearPaused = false;
			}
		}
		else {
			//Have active piece drop down over time
			droptimer++;
			if(droptimer >= dropinterval) {
				activepiece.moveDown(spheregrid, gridx, gridy);
				droptimer = 0;
			}
			
			//Check for collisions between the active piece and fallen spheres
			processCollision();
			
			//if a line was cleared - process one chain each update with a set delay between
			if(processLinks()) {
				
				chain++; //increase chain value
				isClearPaused = true; //turn on temp. delay
				
				//drop down any floating spheres
				for(int y = gridy - 1; y >= 0; y--) {
					for(int x = 0; x < gridx; x++) {
						if(spheregrid[x][y] != null) {
							dropSphere(spheregrid[x][y]);
						}
					}
				}
			}
			else {
				//reset chain value if chain is over
				chain = 1;
			}
		
		}
		//redraw the window for this frame
		repaint();
	}
	
	//event from game timer calls update each frame
	public void actionPerformed(ActionEvent e) {
		update();
	}
	
	//Creates a new active piece for the player to control
	private void newPiece() {
		activepiece = new PuyoPiece(gridx, gridy);
	}
	
	//resets the checked boolean for all spheres in the grid
	private void resetCheckedStatus() {
		for(int y = gridy - 1; y >= 0; y--) {
			for(int x = 0; x < gridx; x++) {
				if(spheregrid[x][y] != null) {
					spheregrid[x][y].isChecked = false;
				}
			}
		}
	}
	
	//clears all active 4+ sphere links. returns true if any links were cleared
	private boolean processLinks() {
		boolean toreturn = false;
		
		for(int y = gridy - 1; y >= 0; y--) {
			for(int x = 0; x < gridx; x++) {
				if(spheregrid[x][y] != null && linksFromSphere(spheregrid[x][y]) >= 4) {
					clearFromSphere(spheregrid[x][y]);
					toreturn = true;
				}
				resetCheckedStatus();
			}
		}
		
		return toreturn;
	}
	
	//simple depth first search for color links
	private int linksFromSphere(PuyoSphere sphere) {
		int linksum = 0;
		sphere.isChecked = true;
		
		//check to the right
		if(sphere.location.x < gridx - 1) {
			PuyoSphere rsphere = spheregrid[sphere.location.x+1][sphere.location.y];
			if(rsphere != null && rsphere.color == sphere.color && !rsphere.isChecked) {
				linksum += linksFromSphere(rsphere);
			}
		}
		//check to the left
		if(sphere.location.x > 0) {
			PuyoSphere lsphere = spheregrid[sphere.location.x-1][sphere.location.y];
			if(lsphere != null && lsphere.color == sphere.color && !lsphere.isChecked) {
				linksum += linksFromSphere(lsphere);
			}
		}
		//check up
		if(sphere.location.y > 0) {
			PuyoSphere usphere = spheregrid[sphere.location.x][sphere.location.y-1];
			if(usphere != null && usphere.color == sphere.color && !usphere.isChecked) {
				linksum += linksFromSphere(usphere);
			}
		}
		//check down
		if(sphere.location.y < gridy - 1) {
			PuyoSphere dsphere = spheregrid[sphere.location.x][sphere.location.y+1];
			if(dsphere != null && dsphere.color == sphere.color && !dsphere.isChecked) {
				linksum += linksFromSphere(dsphere);
			}
		}
		
		//add number of cleared spheres * chain value to score
		if(linksum + 1 >= 4) {
			points += (linksum + 1) * chain;
		}
		
		return linksum + 1;
	}
	
	//depth first clearing of matching spheres from the given sphere
	private void clearFromSphere(PuyoSphere sphere) {
		sphere.isChecked = false;
		
		//check to the right
		if(sphere.location.x < gridx - 1) {
			PuyoSphere rsphere = spheregrid[sphere.location.x+1][sphere.location.y];
			if(rsphere != null && rsphere.color == sphere.color && rsphere.isChecked) {
				clearFromSphere(rsphere);
			}
		}
		//check to the left
		if(sphere.location.x > 0) {
			PuyoSphere lsphere = spheregrid[sphere.location.x-1][sphere.location.y];
			if(lsphere != null && lsphere.color == sphere.color && lsphere.isChecked) {
				clearFromSphere(lsphere);
			}
		}
		//check up
		if(sphere.location.y > 0) {
			PuyoSphere usphere = spheregrid[sphere.location.x][sphere.location.y-1];
			if(usphere != null && usphere.color == sphere.color && usphere.isChecked) {
				clearFromSphere(usphere);
			}
		}
		//check down
		if(sphere.location.y < gridy - 1) {
			PuyoSphere dsphere = spheregrid[sphere.location.x][sphere.location.y+1];
			if(dsphere != null && dsphere.color == sphere.color && dsphere.isChecked) {
				clearFromSphere(dsphere);
			}
		}
		
		spheregrid[sphere.location.x][sphere.location.y] = null;
	}
	
	//ensure collision is properly responded to and creates a new piece for the player
	private void processCollision() {
		CollisionType collision = collisionCheck();
		try {
			if(collision == CollisionType.BOTH) {
				spheregrid[activepiece.s1.location.x][activepiece.s1.location.y] = activepiece.s1;
				spheregrid[activepiece.s2.location.x][activepiece.s2.location.y] = activepiece.s2;
				newPiece();
			}
			else if(collision == CollisionType.S1) {
				spheregrid[activepiece.s1.location.x][activepiece.s1.location.y] = activepiece.s1;
				dropSphere(activepiece.s2);
				newPiece();
			}
			else if(collision == CollisionType.S2) {
				spheregrid[activepiece.s2.location.x][activepiece.s2.location.y] = activepiece.s2;
				dropSphere(activepiece.s1);
				newPiece();
			}
		}
		//An offscreen collision means game over!
		//This is a pretty ugly way to handle it. If I ever revise this further it would be better
		//to pad the width and height of spheregrid[][] to avoid the need for so many sanity checks
		catch(ArrayIndexOutOfBoundsException e) {
			gameover = true;
		}
	}
	
	//drops a sphere the furthest it can then sticks it in place
	private void dropSphere(PuyoSphere sphere) {
		Point startingloc = sphere.location;
		spheregrid[startingloc.x][startingloc.y] = null;
		Point nextloc = new Point(startingloc);
		while(nextloc.y < gridy - 1 && spheregrid[nextloc.x][nextloc.y] == null) {
			nextloc.y += 1;
		}
		if(nextloc.y == gridy - 1) {
			if(spheregrid[nextloc.x][nextloc.y] == null) {
				sphere.location = nextloc;
				spheregrid[nextloc.x][nextloc.y] = sphere;
			}
			else {
				nextloc.y -= 1;
				sphere.location = nextloc;
				spheregrid[nextloc.x][nextloc.y] = sphere;
			}
		}
		else {
			nextloc.y -= 1;
			sphere.location = nextloc;
			spheregrid[nextloc.x][nextloc.y] = sphere;
		}
	}
	
	//determines what type of collision between the active piece and fallen spheres exists if any
	private CollisionType collisionCheck() {
		CollisionType collision;
		
		//out of bounds sanity checks here
		if(activepiece.s1.location.y == -1 && spheregrid[activepiece.s1.location.x][0] != null) {
			gameover = true;
			return CollisionType.NONE;
		}
		else if(activepiece.s2.location.y == -1 && spheregrid[activepiece.s2.location.x][0] != null) {
			gameover = true;
			return CollisionType.NONE;
		}
		else if(activepiece.s1.location.y == -1 || activepiece.s2.location.y == -1) {
			return CollisionType.NONE;
		}
		//end sanity checking here
		
		//collision detection for right and left rotation states
		if(activepiece.facing == PuyoPiece.Rotation.RIGHT || activepiece.facing == PuyoPiece.Rotation.LEFT) {
			if(activepiece.s1.location.y == gridy - 1) {
				collision = CollisionType.BOTH;
			}
			else if(spheregrid[activepiece.s1.location.x][activepiece.s1.location.y + 1] != null &&
					spheregrid[activepiece.s2.location.x][activepiece.s2.location.y + 1] != null) {
				collision = CollisionType.BOTH;
			}
			else if(spheregrid[activepiece.s1.location.x][activepiece.s1.location.y + 1] == null &&
					spheregrid[activepiece.s2.location.x][activepiece.s2.location.y + 1] != null) {
				collision = CollisionType.S2;
			}
			else if(spheregrid[activepiece.s1.location.x][activepiece.s1.location.y + 1] != null &&
					spheregrid[activepiece.s2.location.x][activepiece.s2.location.y + 1] == null) {
				collision = CollisionType.S1;
			}
			else {
				collision = CollisionType.NONE;
			}
		}
		//collision detection for up/down rotation states
		else {
			if(activepiece.s1.location.y == gridy - 1) {
				collision = CollisionType.BOTH;
			}
			else if(activepiece.s2.location.y == gridy - 1) {
				collision = CollisionType.BOTH;
			}
			else if(spheregrid[activepiece.s1.location.x][activepiece.s1.location.y + 1] != null) {
				collision = CollisionType.BOTH;
			}
			else if(spheregrid[activepiece.s2.location.x][activepiece.s2.location.y + 1] != null) {
				collision = CollisionType.BOTH;
			}
			else {
				collision = CollisionType.NONE;
			}
		}
		
		return collision;
	}
	
	//Draw method for spheres and gui text
	public void paint(Graphics g)
    {
		super.paint(g);
		
		Graphics2D g2d = (Graphics2D) g;
		
		//itterate over grid, draw existing spheres
		for(int x = 0; x < gridx; x++) {
			for(int y = 0; y < gridy; y++) {
				if(spheregrid[x][y] != null) {
					//when sphere exists draw with correct color
					drawSphere(g2d, spheregrid[x][y]);
				}
			}
		}
		
		//draw active sphere if it's onscreen
		if(activepiece.s1.location.x >= 0 && activepiece.s1.location.x < gridx &&
				activepiece.s1.location.y >= 0 && activepiece.s1.location.y < gridy) {
			drawSphere(g2d, activepiece.s1);
		}
		if(activepiece.s2.location.x >= 0 && activepiece.s2.location.x < gridx &&
				activepiece.s2.location.y >= 0 && activepiece.s2.location.y < gridy) {
			drawSphere(g2d, activepiece.s2);
		}
		
		//draw gui text
		g.drawString("Chain: " + chain, 3, 15);
		g.drawString("Points: " + points, 140, 15);
		if(gameover) {
			g.drawString("Game Over", 70, 200);
		}
		
    }
	
	//helper method for sphere drawing
	private void drawSphere(Graphics2D g, PuyoSphere sphere) {
		switch(sphere.color) {
			case 'r': g.drawImage(spherered, sphere.location.x*spherewidth, sphere.location.y*sphereheight, null); break;
			case 'g': g.drawImage(spheregreen, sphere.location.x*spherewidth, sphere.location.y*sphereheight, null); break;
			case 'b': g.drawImage(sphereblue, sphere.location.x*spherewidth, sphere.location.y*sphereheight, null); break;
			case 'y': g.drawImage(sphereyellow, sphere.location.x*spherewidth, sphere.location.y*sphereheight, null); break;
			default: ; break;
		}
	}
	
	//inner class for input handling, when instantiated it is attached to PuyoGame
	class InputProcessor extends KeyAdapter {
		//Respond to player input if game isn't in a paused state
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if(!isClearPaused && !gameover) {
				
				switch (key) {
				case KeyEvent.VK_LEFT:
					activepiece.moveLeft(spheregrid, gridx, gridy); 
					break;
				case KeyEvent.VK_RIGHT:
					activepiece.moveRight(spheregrid, gridx, gridy); 
					break;
				case KeyEvent.VK_UP:
		        	activepiece.rotate(spheregrid, gridx, gridy); 
		        	break;
		        case KeyEvent.VK_DOWN:
	        		activepiece.moveDown(spheregrid, gridx, gridy); 
	        		droptimer = 0;
		        	break;
		        case KeyEvent.VK_SPACE:
		        	activepiece.rotate(spheregrid, gridx, gridy); 
		        	break;
		        default: ; break;
		        }
			}
			//let user quit at any time
			if(key == KeyEvent.VK_ESCAPE) {
				System.exit(0);
			}
		}
		public void keyReleased(KeyEvent e) {
			//
		}
		public void keyTyped(KeyEvent e) {
			//
		}
	}
}
