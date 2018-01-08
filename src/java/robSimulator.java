import java.util.Random;

import jason.environment.grid.Location;

public class robSimulator {
	Model model;
	protected Random random = new Random();
	// Direction vectors
	DirectionVector north = new DirectionVector(0, -1);
	DirectionVector south = new DirectionVector(0, 1);
	DirectionVector west = new DirectionVector(-1, 0);
	DirectionVector east = new DirectionVector(1, 0);
	
	Location location;
	DirectionVector direction;
    
	Boolean isExecuted = false;
	
	
	public robSimulator(Model model) {
		this.model = model;
		location = locationInitial();
		direction = headingInitial();
	}

	Location locationInitial() {
		while (location == null) {
			int x = random.nextInt(model.getWidth());
			int y = random.nextInt(model.getHeight());
			//int x = 2;
			//int y = 5;
			Location l = new Location(x, y);
			if (model.isFree(l)) {
				System.out.println(l);
				return l;
				
			}
		}
		return null; // not found
	}

	DirectionVector headingInitial() {
		int selector = random.nextInt(4);
		//int selector = 1;
		//debug
		//System.out.println(selector);
		
		DirectionVector direction;
		if (selector == 0) {
			direction = north;
		} else if (selector == 1) {
			direction = south;
		} else if (selector == 2) {
			direction = west;
		} else {
			direction = east;
		}
		//System.out.println(direction.DVgetX()+","+direction.DVgetY());
		return direction;
	}

	public Boolean[] scan_Around() {
		//calculate based on the direction vectors and the rotate matrix
		Boolean[] aroundObstacle = new Boolean[4];
		Location front_location = new Location(location.x+direction.DVgetX(), location.y+direction.DVgetY());
		Location right_location = new Location(location.x-direction.DVgetY(),location.y+direction.DVgetX());
		Location left_location = new Location(location.x+direction.DVgetY(), location.y-direction.DVgetX());
		Location back_location = new Location(location.x-direction.DVgetX(), location.y-direction.DVgetY());
		
		System.out.println(direction.DVgetX()+","+direction.DVgetY());
		System.out.println(front_location);
		System.out.println(right_location);
		System.out.println(left_location);
		System.out.println(back_location);
		
		
		if(model.isFreeOfObstacle(front_location)) {
			aroundObstacle[0] = false;
		}else {
			aroundObstacle[0] = true;
		}
		if(model.isFreeOfObstacle(right_location)) {
			aroundObstacle[1] = false;
		}else {
			aroundObstacle[1] = true;
		}
		if(model.isFreeOfObstacle(left_location)) {
			aroundObstacle[2] = false;
		}else {
			aroundObstacle[2] = true;
		}
		if(isExecuted==false) {
			if(model.isFreeOfObstacle(back_location)) {
				aroundObstacle[3] = false;
			}else {
				aroundObstacle[3] = true;
			}
		}else if(isExecuted==true) {
			aroundObstacle[3] = false;
		}
		//isExecuted = true;
		
		return aroundObstacle;
	}
	
	public void travel_orientation(String dir) {
		switch(dir) {
		case "F":
			Location nextLocation_F = new Location(location.x+direction.DVgetX(), location.y+direction.DVgetY());
			location = nextLocation_F;
			break;
		case "R":
			Location nextLocation_R = new Location(location.x-direction.DVgetY(),location.y+direction.DVgetX());
			location = nextLocation_R;
			DirectionVector newdirection_R = new DirectionVector(-direction.DVgetY(), direction.DVgetX());
			direction = newdirection_R;
			break;
		case "L":
			Location nextLocation_L = new Location(location.x+direction.DVgetY(), location.y-direction.DVgetX());
			location = nextLocation_L;
			DirectionVector newdirection_L = new DirectionVector(direction.DVgetY(), -direction.DVgetX());
			direction = newdirection_L;
			break;
		case "B":
			Location nextLocation_B = new Location(location.x-direction.DVgetX(), location.y-direction.DVgetY());
			location = nextLocation_B;
			DirectionVector newdirection_B = new DirectionVector(-direction.DVgetX(), -direction.DVgetY());
			direction = newdirection_B;
		}
			
	}
//	public void goForward() {
//		Location nextLocation = new Location(location.x+direction.DVgetX(), location.y+direction.DVgetY());
//		location = nextLocation;
//	}
//	public void goRight() {
//		Location nextLocation = new Location(location.x-direction.DVgetY(),location.y+direction.DVgetX());
//		location = nextLocation;
//		DirectionVector newdirection = new DirectionVector(-direction.DVgetY(), direction.DVgetX());
//		direction = newdirection;
//	}
//	public void goLeft() {
//		Location nextLocation = new Location(location.x+direction.DVgetY(), location.y-direction.DVgetX());
//		location = nextLocation;
//		DirectionVector newdirection = new DirectionVector(direction.DVgetY(), -direction.DVgetX());
//		direction = newdirection;
//	}
//	
//
//	public void goBackward() {
//		Location nextLocation = new Location(location.x-direction.DVgetX(), location.y-direction.DVgetY());
//		location = nextLocation;
//		DirectionVector newdirection = new DirectionVector(-direction.DVgetX(), -direction.DVgetY());
//		direction = newdirection;
//	}
}
