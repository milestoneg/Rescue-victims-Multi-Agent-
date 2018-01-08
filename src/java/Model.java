
/**
 * This class is the building class of the model of the scenario.
 * The size of the world, The edge of the arena, locations of Victims, obstacle and possibilities are defined in this class.
 * Also some of the methods related to the modification of the model are implemented within this class.
 * @author Yuan Gao
 */
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

class Model extends GridWorldModel {
	// public static final int GWidth = 5 + 2; // grid Wdith for 5*6 arena
	public static final int GWidth = 6 + 2; // grid Wdith for 6*6 arena
	public static final int GLength = 6 + 2;// grid Length

	// declaration of the objects of the model
	public static final int CriticalVictim = 16; // Crucial Victim code in grid model
	public static final int MinorVictim = 32;// Minor Victim code in the grid model
	public static final int SeriousVictim = 64;// Serious Victim code in the grid model
	public static final int possibleVictim = 128;// possible Victim code in the grid model
	public static final int possibleLocation = 256;// possible Location code in the grid model
	public static final int MErr = 2; // max error in pick garb
	int nerr; // number of tries of pick garb
	boolean r1HasGarb = false; // whether r1 is carrying garbage or not

	// list used to store objects
	LinkedList<Location> Victims = new LinkedList<Location>();
	LinkedList<Location> storage = new LinkedList<Location>();

	Random random = new Random(System.currentTimeMillis());

	DirectionVector finaldirection;// direction after localization

	static int VictimCount = 0;// Count of founded victims

	// Constructor
	Model() {
		super(GWidth, GLength, 2);// call parent class

		// Victim locations for the 6*6 arena
		Location VicPos0 = new Location(1, 1);
		Location VicPos1 = new Location(5, 1);
		Location VicPos2 = new Location(3, 3);
		Location VicPos3 = new Location(4, 4);
		Location VicPos4 = new Location(3, 5);
        
		// Victim locations for the 5*6 arena
		// Location VicPos0 = new Location(1, 4);
		// Location VicPos1 = new Location(2, 3);
		// Location VicPos2 = new Location(2, 5);
		// Location VicPos3 = new Location(4, 1);
		// Location VicPos4 = new Location(1, 1);

		// add victim locations into lists
		Victims.add(VicPos0);
		Victims.add(VicPos1);
		Victims.add(VicPos2);
		Victims.add(VicPos3);
		Victims.add(VicPos4);
		storage.add(VicPos0);
		storage.add(VicPos1);
		storage.add(VicPos2);
		storage.add(VicPos3);
		storage.add(VicPos4);

		// add wall for the 6*6 arena
		addWall(0, 0, 0, 7);
		addWall(7, 0, 7, 7);
		addWall(0, 0, 7, 0);
		addWall(0, 7, 7, 7);

		// initial locations of Obstacles for the 6*6 arena
		add(OBSTACLE, 1, 6);
		add(OBSTACLE, 2, 3);
		add(OBSTACLE, 3, 2);
		add(OBSTACLE, 5, 2);
		add(OBSTACLE, 5, 5);
		add(OBSTACLE, 6, 5);

		// add wall for the 5*6 arena
		// addWall(0, 0, 6, 0);
		// addWall(0, 0, 0, 7);
		// addWall(6, 0, 6, 7);
		// addWall(0, 7, 6, 7);

		// initial location of Obstacles for the 5*6 arena
		// add(OBSTACLE, 1, 3);
		// add(OBSTACLE, 2, 2);
		// add(OBSTACLE, 4, 2);
		// add(OBSTACLE, 4, 5);
		// add(OBSTACLE, 5, 5);

		// initial location of victims
		add(possibleVictim, VicPos0);
		add(possibleVictim, VicPos1);
		add(possibleVictim, VicPos2);
		add(possibleVictim, VicPos3);
		add(possibleVictim, VicPos4);
	}

	// Getters
	public LinkedList<Location> getVictims() {
		return Victims;
	}

	/**
	 * Method used move the scout to the next path node after localization and scan
	 * the color of the position to judge whether this position contains a victim.
	 * Moving instruction and calibration instruction will send to the robot by this
	 * method. Agent position(Scout) will be upload by this method. Possibilities
	 * will also be upload including the location and the direction.
	 * 
	 * @param x
	 *            coordinate of target position
	 * @param y
	 *            coordinate of target position
	 * @throws Exception
	 *             client may throw interrupt exception
	 */
	void nextPosition(int x, int y) throws Exception {

		WarView view_casted = (WarView) view;
		Location r1 = getAgPos(0);
		DirectionVector direction = view_casted.possibilities.get(r1).get(0);
		view_casted.possibilities.clear();

		// calculate direction vector
		int Xcoord_vector = x - r1.x;
		int Ycoord_vector = y - r1.y;
		if (r1.x < x) {
			r1.x++;
		} else if (r1.x > x) {
			r1.x--;
		}
		if (r1.y < y) {
			r1.y++;
		} else if (r1.y > y) {
			r1.y--;
		}

		// send move command based on the direction vector calculated before
		DirectionVector newDirection = new DirectionVector(Xcoord_vector, Ycoord_vector);
		if (Xcoord_vector == direction.DVgetX() && Ycoord_vector == direction.DVgetY()) {
			// go front
			Env.client.sendCaliFront(direction);// send calibration command
			Env.client.sendMoveCommand("Front");
			LinkedList<DirectionVector> newDirections = new LinkedList<>();
			newDirections.add(newDirection);
			view_casted.possibilities.put(r1, newDirections);// update information
		} else if (Xcoord_vector == -direction.DVgetY() && Ycoord_vector == direction.DVgetX()) {
			// go right
			Env.client.sendCaliFront(direction);// send calibration command
			Env.client.sendMoveCommand("Right");
			LinkedList<DirectionVector> newDirections = new LinkedList<>();
			newDirections.add(newDirection);
			view_casted.possibilities.put(r1, newDirections);// update information
		} else if (Xcoord_vector == direction.DVgetY() && Ycoord_vector == -direction.DVgetX()) {
			// go left
			Env.client.sendCaliFront(direction);// send calibration command
			Env.client.sendMoveCommand("Left");
			LinkedList<DirectionVector> newDirections = new LinkedList<>();
			newDirections.add(newDirection);
			view_casted.possibilities.put(r1, newDirections);// update information
		} else {
			// go back
			Env.client.sendCaliFront(direction);// send calibration command
			Env.client.sendMoveCommand("Back");
			LinkedList<DirectionVector> newDirections = new LinkedList<>();
			newDirections.add(newDirection);
			view_casted.possibilities.put(r1, newDirections);// update information
		}

		Scan_color(r1);// scan color and judge whether the color is a victim
		setAgPos(0, r1);// update scout location in the model
	}

	/**
	 * Method used to read color parameters form robot and judge whether the color
	 * is a victim. After judge process, the model will be upload.
	 * 
	 * @param r1
	 *            current location
	 */
	void Scan_color(Location r1) {

		if (storage.contains(r1)) {// if r1 is a possible victim location
			float[] color_RGB = Env.client.sendColorCommand();// send read color command to robot and get the return
																// value
			try {
				if (color_RGB[0] < 0.1 && color_RGB[1] > 0.1 && color_RGB[2] > 0.2) {// Blue
					VictimCount++;
					remove(possibleVictim, r1);
					add(SeriousVictim, r1);
					view.repaint();
				} else if (color_RGB[0] < 0.13 && color_RGB[1] > 0.195 && color_RGB[2] < 0.1) {// green
					VictimCount++;
					remove(possibleVictim, r1);
					add(MinorVictim, r1);
					view.repaint();
				} else if (color_RGB[0] > 0.165 && color_RGB[1] < 0.1 && color_RGB[2] < 0.1) {// red
					VictimCount++;
					remove(possibleVictim, r1);
					add(CriticalVictim, r1);
					view.repaint();
				} else {
					remove(possibleVictim, r1);
					view.repaint();
				}

			} catch (Exception e) {
				// TODO: handle exception
				remove(possibleVictim, r1);
				view.repaint();
			}
		}
	}

	/**
	 * Method used to initial the scout location after localization process.
	 * Positions of victims founded during the localization process will be
	 * calculate by this method, thus, scout do not need to be these locations when
	 * doing the path finding.
	 * 
	 * @param x
	 * @param y
	 * @throws IOException
	 * @throws InterruptedException
	 */
	void currentPosition(int x, int y) throws IOException, InterruptedException {
		Env.client.sendLocalizationDone();//send command to robot to switch display mode(to display the map on the LCD screen)
		WarView casted_view = (WarView) view;//cast view to WarView
		casted_view.repaint();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Calculate locations of victims founded in the localization process
		// This algorithm is based on the path parameters recored in the Env class
		Location finalLocation = new Location(x, y);
		DirectionVector directionVector = casted_view.possibilities.get(finalLocation).get(0);//direction after localization
		

		int VictimNum = Env.victims_index.size();

		while (VictimNum > 0) {
			List<String> moving_path_sublist = Env.moving_path
					.subList(Env.victims_index.get(VictimNum - 1).Victim_path_index, Env.moving_path.size());
			Collections.reverse(moving_path_sublist);
			DirectionVector newDirection = directionVector;
			Location backwordPos = finalLocation;

			for (String path : moving_path_sublist) {
				backwordPos = new Location(backwordPos.x - newDirection.DVgetX(),
						backwordPos.y - newDirection.DVgetY());//go back to the previous location

				//update direction vector to the previous location
				if (path.equals("F")) {
					newDirection = new DirectionVector(newDirection.DVgetX(), newDirection.DVgetY());
				} else if (path.equals("R")) {
					newDirection = new DirectionVector(newDirection.DVgetY(), -newDirection.DVgetX());
				} else if (path.equals("L")) {
					newDirection = new DirectionVector(-newDirection.DVgetY(), newDirection.DVgetX());
				} else if (path.equals("B")) {
					newDirection = new DirectionVector(-newDirection.DVgetX(), -newDirection.DVgetY());
				}
			}
			
			//judge color and update model
			Victims.remove(backwordPos);

			remove(possibleVictim, backwordPos);
			if (Env.victims_index.get(VictimNum - 1).color_RGB[0] < 0.1
					&& Env.victims_index.get(VictimNum - 1).color_RGB[1] > 0.1
					&& Env.victims_index.get(VictimNum - 1).color_RGB[2] > 0.15) {
				add(SeriousVictim, backwordPos);
				view.repaint();
			} else if (Env.victims_index.get(VictimNum - 1).color_RGB[0] < 0.13
					&& Env.victims_index.get(VictimNum - 1).color_RGB[1] > 0.195
					&& Env.victims_index.get(VictimNum - 1).color_RGB[2] < 0.1) {
				add(MinorVictim, backwordPos);
				view.repaint();
			} else if (Env.victims_index.get(VictimNum - 1).color_RGB[0] > 0.165
					&& Env.victims_index.get(VictimNum - 1).color_RGB[1] < 0.1
					&& Env.victims_index.get(VictimNum - 1).color_RGB[2] < 0.1) {
				add(CriticalVictim, backwordPos);
				view.repaint();
			}

			VictimNum--;
		}

		setAgPos(0, x, y);//initial the scout position

		casted_view.Status = true;
		casted_view.repaint();

	}

	void pickVictims() {
		// r1 location has victim
		if (Env.getModel().hasObject(CriticalVictim, getAgPos(0))) {
			// sometimes the "picking" action doesn't work
			// but never more than MErr times
			if (random.nextBoolean() || nerr == MErr) {
				remove(CriticalVictim, getAgPos(0));
				nerr = 0;
				r1HasGarb = true;
			} else {
				nerr++;
			}
		}
	}

	//getter and setter
	void setFinalDirection(int x, int y) {
		finaldirection = new DirectionVector(x, y);
	}

	public DirectionVector getFinaldirection() {
		return finaldirection;
	}
	
	public int[][] getData() {
		return data;
	}

	/**
	 * This method is used to add possibilities into the model during the localization process.
	 * All possibilities added here will be displayed.
	 * @throws InterruptedException
	 */
	public void updateModel() throws InterruptedException {
		WarView casted_view = (WarView) view;

		for (Location location : casted_view.possibilities.keySet()) {
			add(possibleLocation, location.x, location.y);
			Thread.sleep(300);
		}

	}

}