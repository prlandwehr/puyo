package puyo;

//Two linked Puyo spheres controlled by the player
public class PuyoPiece {
	
	//FIELDS
	public PuyoSphere s1;
	public PuyoSphere s2;
	public enum Rotation { UP, DOWN, LEFT, RIGHT }
	public Rotation facing = Rotation.UP;
	
	//CONSTRUCTORS
	public PuyoPiece(int gridx, int gridy) {
		this.s1 = new PuyoSphere();
		this.s2 = new PuyoSphere();
		this.s1.location.setLocation(2,-1);
		this.s2.location.setLocation(2,-2);
	}
	
	//METHODS
	public void moveUp(PuyoSphere[][] grid, int gridx, int gridy) {
		s1.location.y -= 1;
		s2.location.y -= 1;
	}
	
	public void moveDown(PuyoSphere[][] grid, int gridx, int gridy) {
		//avoid array indexing exceptions
		if(s1.location.y < 0 && s2.location.y < 0) {
			s1.location.y += 1;
			s2.location.y += 1;
		}
		//further sanity checks
		else if(s1.location.y < gridy - 1 && s2.location.y < gridy - 1 &&
				grid[s1.location.x][s1.location.y+1] == null &&
				grid[s2.location.x][s2.location.y+1] == null) {
			s1.location.y += 1;
			s2.location.y += 1;
		}
	}
	
	public void moveRight(PuyoSphere[][] grid, int gridx, int gridy) {
		//avoid array indexing exceptions
		if( (s1.location.y < 0 || s2.location.y < 0) && s1.location.x < gridx - 1 && s2.location.x < gridx - 1) {
			s1.location.x += 1;
			s2.location.x += 1;
		}
		else if(s1.location.x < gridx - 1 && s2.location.x < gridx - 1 &&
				grid[s1.location.x+1][s1.location.y] == null &&
				grid[s2.location.x+1][s2.location.y] == null) {
			s1.location.x += 1;
			s2.location.x += 1;
		}
	}
	
	public void moveLeft(PuyoSphere[][] grid, int gridx, int gridy) {
		//avoid array indexing exceptions
		if( (s1.location.y < 0 || s2.location.y < 0) && s1.location.x > 0 && s2.location.x > 0) {
			s1.location.x -= 1;
			s2.location.x -= 1;
		}
		else if(s1.location.x > 0 && s2.location.x > 0 &&
				grid[s1.location.x-1][s1.location.y] == null &&
				grid[s2.location.x-1][s2.location.y] == null) {
			s1.location.x -= 1;
			s2.location.x -= 1;
		}
	}
	
	//rotates piece around center if it won't bump into something in the process
	//some sanity checking as well
	public void rotate(PuyoSphere[][] grid, int gridx, int gridy) {
		switch(facing) {
			case UP: 
				if(s1.location.y >=0 && s1.location.x+1 < gridx && grid[s1.location.x+1][s1.location.y] == null) {
					s2.location.y = s1.location.y;
					s2.location.x = s1.location.x + 1;
					facing = Rotation.RIGHT;
				}
				break;
			case DOWN: 
				if(s1.location.y >=0 && s1.location.x-1 >= 0 && grid[s1.location.x-1][s1.location.y] == null) {
					s2.location.y = s1.location.y;
					s2.location.x = s1.location.x - 1;
					facing = Rotation.LEFT;
				}
				break;
			case LEFT: 
				s2.location.y = s1.location.y - 1;
				s2.location.x = s1.location.x;
				facing = Rotation.UP;
				break;
			case RIGHT: 
				if(s1.location.y >= -1 && s1.location.y+1 < gridy && grid[s1.location.x][s1.location.y+1] == null) {
					s2.location.y = s1.location.y + 1;
					s2.location.x = s1.location.x;
					facing = Rotation.DOWN;
				}
				break;
			default: ; break;
		}
	}
}