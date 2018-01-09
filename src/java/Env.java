
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.grid.Location;

public class Env extends Environment {

	static Logger logger = Logger.getLogger(Env.class.getName());

	// view initial
	private static Model model;
	private WarView view;

	private LinkedList<Location> victims = new LinkedList<>();

	// robotsimulator
	// robSimulator simulator;//Initial the simulator

	Boolean[] scan_information;

	// Direction vectors
	DirectionVector north = new DirectionVector(0, -1);
	DirectionVector south = new DirectionVector(0, 1);
	DirectionVector west = new DirectionVector(-1, 0);
	DirectionVector east = new DirectionVector(1, 0);
   
	// Initial connection
	static Client client;
	
	 final static int OBSTACLE = 4;
	 public static final int possibleVictim = 128;// possible Victim code in the grid model

	// map
	static LinkedList<String> moving_path = new LinkedList<String>();// record whole moving path of the robot

	static LinkedList<Pair> victims_index = new LinkedList<>();// record path index of the victim and its RGB parameters

	@Override
	public void init(String[] args) {
		model = new Model();
		view = new WarView(model);
		model.setView(view);
		victims = model.getVictims();
		// simulator = new robSimulator(model);
		client = new Client(model);
	}

	
	
	/**
	 * Method used to distinguish and execute the method that call with an Agent.
	 */
	@Override
	public boolean executeAction(String ag, Structure action) {
		logger.info(ag + " doing: " + action);
		updateVictimCount();

		try {
			Thread.sleep(200);
			if (action.getFunctor().equals("nextPosition")) {
				// read parameters from Agent
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.nextPosition(x, y);
				updateVictimCount();
			} else if (action.getFunctor().equals("currentpos")) {
				// read parameters from Agent
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.currentPosition(x, y);
			} else if (action.getFunctor().equals("initial_location")) {
				initial_location();
			} else if (action.getFunctor().equals("possible_location")) {
				possible_location();
			} else if (action.getFunctor().equals("loclization_move_next")) {
				loclization_move_next();
			} else if (action.getFunctor().equals("update_percepts")) {
				updatePercepts();
			} else if (action.getFunctor().equals("robotbeep")) {
				robotbeep();
			}else if (action.getFunctor().equals("constructenv")) {
				List<Term> obstacleList= (List<Term>) action.getTerm(0);//read obstacle list from doctor
				List<Term> victimList = (List<Term>) action.getTerm(1);//read victim list from doctor
				//add obstacles into the model
				for(Term Term : obstacleList) {
					Literal literal = (Literal) Term;
					int x =(int) ((NumberTerm) literal.getTerm(0)).solve();
					int y =(int) ((NumberTerm) literal.getTerm(1)).solve();
					model.add(OBSTACLE, x, y);
					view.repaint();
				}
				//add victims into the model
				for(Term Term : victimList) {
					Literal literal = (Literal) Term;
					int x = (int) ((NumberTerm) literal.getTerm(0)).solve();
					int y =(int) ((NumberTerm) literal.getTerm(1)).solve();
					model.add(possibleVictim, x, y);
					Location victimPosition = new Location(x,y);
					model.Victims.add(victimPosition);
					model.storage.add(victimPosition);
				}
			}  else {
				return false;
			}
			Thread.sleep(200);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// send map information to the robot
			client.sendMapData();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			Thread.sleep(200);
		} catch (Exception e) {
		}
		informAgsEnvironmentChanged();
		return true;
	}

	
	
	/**
	 * Method used to find the optimal path between possible victims locations. An
	 * tree searching algorithm is implemented within this method in order to find
	 * the optimal path. A* algorithm is also used here which is responsible for
	 * calculate path cost between two possible locations within the tree searching
	 * algorithm.
	 * 
	 * @return List of locations indicate the optimal path between possible victims.
	 */
	public List<Location> calculateMinPath() {
		// construct the tree
		Location currentPos = model.getAgPos(0);
		SearchingTree tree = new SearchingTree(currentPos);// initial the tree using current location as root
		Queue<Location> building_queue = new LinkedList<Location>();
		Queue<Integer> id_queue = new LinkedList<>();
		int building_count = 0;
		int id_count = 1;
		building_queue.offer(currentPos);
		id_queue.offer(0);
		// build the tree
		while (building_count < 5) {
			Queue<Location> tmp_queue = new LinkedList<>();
			Queue<Integer> tmpID_queue = new LinkedList<>();
			while (!building_queue.isEmpty()) {
				Location locationFromQueue = building_queue.poll();
				int locationFromQueue_id = id_queue.poll();
				LinkedList<Node> parentSet_node = tree.getNode(locationFromQueue_id).getParentSet();
				LinkedList<Location> parentSet_location = new LinkedList<>();
				for (Node node : parentSet_node) {
					parentSet_location.add(node.getLocation());
				}

				for (Location location : victims) {
					if (!location.equals(locationFromQueue) && !parentSet_location.contains(location)) {
						AStar aStar = new AStar(model);
						int pathConsumption = aStar.findPath(locationFromQueue, location).size();
						tree.addNode(location, tree.getNode(locationFromQueue_id), pathConsumption, id_count);
						tmpID_queue.offer(id_count);
						tmp_queue.offer(location);
						id_count++;
					}
				}
			}

			building_queue = tmp_queue;
			id_queue = tmpID_queue;
			building_count++;
		}

		LinkedList<Node> allLeafNodes = tree.allLeafNodes();// get all leaf nodes

		// find the minimum cost leaf node
		int minIndex = 0;
		int minCost = allLeafNodes.get(0).getPathConsumption();
		for (int index = 0; index < allLeafNodes.size(); index++) {
			if (allLeafNodes.get(index).getPathConsumption() < minCost) {
				minIndex = index;
				minCost = allLeafNodes.get(index).getPathConsumption();
			}
		}

		Node minCostNode = allLeafNodes.get(minIndex);
		List<Node> path_node = minCostNode.getParentSet();// get minimum leaf node's parent nodes
		path_node.add(0, minCostNode);
		List<Location> path_location = new LinkedList<>();

		for (Node node : path_node) {
			path_location.add(node.getLocation());
		}

		List<Location> optimal_full_path = new LinkedList<>();
		for (int index = path_location.size() - 1; index >= 1; index--) {
			AStar aStar = new AStar(model);
			List<Location> path = aStar.findPath(path_location.get(index), path_location.get(index - 1));
			for (Location location : path) {
				optimal_full_path.add(location);
			}
		}

		return optimal_full_path;
	}

	/**
	 * Construct and update optimal path percept to doctor. The format of the
	 * percept is: path(pos(a,b)...pos(c,d)).
	 */
	void updatePercepts() {
		List<Location> nextVictims = calculateMinPath();
		LinkedList<Term> nextVictims_Term = new LinkedList<>();
		for (int index = 0; index < nextVictims.size(); index++) {
			NumberTerm xCoord = ASSyntax.createNumber(nextVictims.get(index).x);
			NumberTerm yCoord = ASSyntax.createNumber(nextVictims.get(index).y);
			nextVictims_Term.add(ASSyntax.createLiteral("pos", xCoord, yCoord));
		}
		addPercept("doctor", ASSyntax.createLiteral("path", ASSyntax.createList(nextVictims_Term)));

	}

	/**
	 * Construct and update victim count percept to doctor. Before the newest victim
	 * count is added the old one should be removed. The format of the percept is:
	 * victimcount(x).
	 */
	void updateVictimCount() {
		removePerceptsByUnif("doctor", Literal.parseLiteral("victimcount(_)"));
		NumberTerm VictimCount_Term = ASSyntax.createNumber(Model.VictimCount);
		addPercept("doctor", ASSyntax.createLiteral("victimcount", VictimCount_Term));
	}

	/**
	 * Construct all possibilities of locations with direction and add these
	 * percepts to scout. The format of the percept is:
	 * possible(position(x,y),direction(a,b)).
	 */
	private void initial_location() {

		for (int widthIndex = 1; widthIndex < model.getWidth() - 1; widthIndex++) {
			for (int heightIndex = 1; heightIndex < model.getHeight() - 1; heightIndex++) {
				if (model.isFreeOfObstacle(widthIndex, heightIndex)) {
					LinkedList<DirectionVector> PossibleDirections = new LinkedList<DirectionVector>();
					PossibleDirections.add(north);
					PossibleDirections.add(south);
					PossibleDirections.add(west);
					PossibleDirections.add(east);
					view.possibilities.put(new Location(widthIndex, heightIndex), PossibleDirections);

					Term xCoord = ASSyntax.createNumber(widthIndex);
					Term yCoord = ASSyntax.createNumber(heightIndex);
					Literal position_Literal = ASSyntax.createLiteral("position", xCoord, yCoord);

					for (DirectionVector direction : PossibleDirections) {
						Term directionX_Term = ASSyntax.createNumber(direction.DVgetX());
						Term directionY_Term = ASSyntax.createNumber(direction.DVgetY());
						Literal direction_Literal = ASSyntax.createLiteral("direction", directionX_Term,
								directionY_Term);
						Literal pair = ASSyntax.createLiteral("possible", position_Literal, direction_Literal);
						addPercept("scout", pair);
					}
				}
			}
		}

	}

	/**
	 * Method used to update the possibilities. Compare scanned around information
	 * and expected around information, if matching then keep this possibilities,
	 * otherwise delete. If current color is blue or green or red then delete all
	 * possibilities locate outside the possible victims set.
	 * 
	 * @throws InterruptedException
	 */
	private void possible_location() throws InterruptedException {

		// scan_information = simulator.scan_Around();//use this code if using simulator
		scan_information = client.sendScanCommand();

		// compare scanned around information with expected around information for each
		// remained possibilities
		Iterator<Location> key_iterator = view.possibilities.keySet().iterator();
		while (key_iterator.hasNext()) {
			LinkedList<DirectionVector> delete_index = new LinkedList<DirectionVector>();
			Location key = key_iterator.next();
			for (int index = 0; index < view.possibilities.get(key).size(); index++) {

				Boolean[] map_information = new Boolean[4];
				DirectionVector direction = view.possibilities.get(key).get(index);
				Location front_location = new Location(key.x + direction.DVgetX(), key.y + direction.DVgetY());
				Location right_location = new Location(key.x - direction.DVgetY(), key.y + direction.DVgetX());
				Location left_location = new Location(key.x + direction.DVgetY(), key.y - direction.DVgetX());
				Location back_location = new Location(key.x - direction.DVgetX(), key.y - direction.DVgetY());

				map_information[0] = !model.isFreeOfObstacle(front_location);

				map_information[1] = !model.isFreeOfObstacle(right_location);

				map_information[2] = !model.isFreeOfObstacle(left_location);

				map_information[3] = !model.isFreeOfObstacle(back_location);

				// print specific information
				System.out.println("Location:" + key);
				System.out.println("direction vection:" + direction.DVgetX() + "," + direction.DVgetY());
				System.out.println("fornt:" + front_location + ";" + map_information[0]);
				System.out.println("right:" + right_location + ";" + map_information[1]);
				System.out.println("fornt:" + left_location + ";" + map_information[2]);
				System.out.println("fornt:" + back_location + ";" + map_information[3]);
				System.out.println("+++++++++++++++++++++++++++++++++++++");

				if (!Arrays.equals(map_information, scan_information)) {
					delete_index.add(direction);
				}
			}
			for (DirectionVector delete_Vectors : delete_index) {
				view.possibilities.get(key).remove(delete_Vectors);
			}

		}
		// if find the victim(color is red, blue or green)
		float[] color_RGB = Env.client.sendColorCommand();
		if ((color_RGB[0] < 0.1 && color_RGB[1] > 0.1 && color_RGB[2] > 0.15)
				|| (color_RGB[0] < 0.13 && color_RGB[1] > 0.195 && color_RGB[2] < 0.1)
				|| (color_RGB[0] > 0.165 && color_RGB[1] < 0.1 && color_RGB[2] < 0.1)) {

			Model.VictimCount++;

			LinkedList<Location> tobeRemove = new LinkedList<>();

			for (Location key : view.possibilities.keySet()) {
				int count = 0;

				for (Location location : model.Victims) {

					if (!key.equals(location)) {
						count++;
					}
				}
				if (count == 5) {
					tobeRemove.add(key);
				}
			}

			for (Location location : tobeRemove) {
				view.possibilities.remove(location);
			}

			// record index in the moving path
			victims_index.add(new Pair(moving_path.size(), color_RGB));

		}

		// if a locations' possible directions equals to 0
		Iterator<Location> delet_iterator = view.possibilities.keySet().iterator();

		while (delet_iterator.hasNext()) {

			LinkedList<DirectionVector> directions = view.possibilities.get(delet_iterator.next());
			if (directions.isEmpty()) {
				delet_iterator.remove();
			}
		}

		// update model and view
		for (Location location : view.possibilities.keySet()) {
			model.remove(Model.possibleLocation, location.x, location.y);
		}
		WarView casted_view = view;
		casted_view.repaint();

		ScoutUpdatePercepts();

		try {
			model.updateModel();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Moving function during the localization process.
	 * The method is trying to reduce the rotation of the robot thus go front has the highest priority.
	 * After move, all possibilities' location should plus previous moving direction vector.
	 */
	private void loclization_move_next() {
		HashMap<Location, LinkedList<DirectionVector>> tmp = new HashMap<>();
		if (scan_information[0] == false) {
			// simulator.goForward();
			// simulator.travel_orientation("F");
			client.sendMoveCommand("Front");
			for (Location location : view.possibilities.keySet()) {
				for (DirectionVector directionVector : view.possibilities.get(location)) {
					Location newlocation = new Location(location.x + directionVector.DVgetX(),
							location.y + directionVector.DVgetY());
					LinkedList<DirectionVector> directions;
					try {
						directions = tmp.get(newlocation);
						if (!directions.contains(directionVector)) {
							directions.add(directionVector);
						} 
					} catch (Exception e) {
						directions = new LinkedList<>();
						directions.add(directionVector);
					}

					tmp.put(newlocation, directions);
				}
			}

			moving_path.add("F");

		} else if (scan_information[1] == false) {
			// simulator.goRight();
			// simulator.travel_orientation("R");
			client.sendMoveCommand("Right");
			for (Location location : view.possibilities.keySet()) {
				for (DirectionVector directionVector : view.possibilities.get(location)) {
					Location newlocation = new Location(location.x - directionVector.DVgetY(),
							location.y + directionVector.DVgetX());
					DirectionVector newdirection = new DirectionVector(-directionVector.DVgetY(),
							directionVector.DVgetX());
					LinkedList<DirectionVector> directions = view.possibilities.get(newlocation);
					try {
						directions = tmp.get(newlocation);
						if (!directions.contains(newdirection)) {
							directions.add(newdirection);
						} else {
							// do nothing
						}
					} catch (Exception e) {
						directions = new LinkedList<>();
						directions.add(newdirection);
					}
					tmp.put(newlocation, directions);
				}
			}

			moving_path.add("R");
		} else if (scan_information[2] == false) {
			// simulator.goLeft();
			// simulator.travel_orientation("L");
			client.sendMoveCommand("Left");
			for (Location location : view.possibilities.keySet()) {

				for (DirectionVector directionVector : view.possibilities.get(location)) {
					Location newlocation = new Location(location.x + directionVector.DVgetY(),
							location.y - directionVector.DVgetX());
					DirectionVector newdirection = new DirectionVector(directionVector.DVgetY(),
							-directionVector.DVgetX());
					LinkedList<DirectionVector> directions = view.possibilities.get(newlocation);
					try {
						directions = tmp.get(newlocation);
						if (!directions.contains(newdirection)) {
							directions.add(newdirection);
						} else {
							// do nothing
						}
					} catch (Exception e) {
						directions = new LinkedList<>();
						directions.add(newdirection);
					}
					tmp.put(newlocation, directions);
				}
			}
			moving_path.add("L");
		} else if (scan_information[3] == false) {
			// simulator.goBackward();
			// simulator.travel_orientation("B");
			client.sendMoveCommand("Back");
			for (Location location : view.possibilities.keySet()) {

				for (DirectionVector directionVector : view.possibilities.get(location)) {
					Location newlocation = new Location(location.x - directionVector.DVgetX(),
							location.y - directionVector.DVgetY());
					DirectionVector newdirection = new DirectionVector(-directionVector.DVgetX(),
							-directionVector.DVgetY());
					LinkedList<DirectionVector> directions = view.possibilities.get(newlocation);
					try {
						directions = tmp.get(newlocation);
						if (!directions.contains(newdirection)) {
							directions.add(newdirection);
						} else {
							// do nothing
						}
					} catch (Exception e) {
						directions = new LinkedList<>();
						directions.add(newdirection);
					}
					tmp.put(newlocation, directions);
				}
			}
			moving_path.add("B");
		}

		view.possibilities.clear();
		view.possibilities = tmp;

	
		ScoutUpdatePercepts();

	}

	/**
	 * Method used to update percepts of scout after move.
	 */
	public void ScoutUpdatePercepts() {
		clearPercepts("scout");//clear all old possibilities
		//construct and add new percepts
		for (Location location : view.possibilities.keySet()) {
			Term Xcoord = ASSyntax.createNumber(location.x);
			Term Ycoord = ASSyntax.createNumber(location.y);
			Literal position_Literal = ASSyntax.createLiteral("position", Xcoord, Ycoord);
			for (DirectionVector directionVector : view.possibilities.get(location)) {
				Term XVector = ASSyntax.createNumber(directionVector.DVgetX());
				Term YVector = ASSyntax.createNumber(directionVector.DVgetY());
				Literal direction_Literal = ASSyntax.createLiteral("direction", XVector, YVector);
				Literal pair = ASSyntax.createLiteral("possible", position_Literal, direction_Literal);
				addPercept("scout", pair);
				// debug
				System.out.println(pair);
			}
		}
	}

	// Getters
	public static Model getModel() {
		return model;
	}

	//send beep instruction to robot
	public void robotbeep() throws IOException, InterruptedException {
		client.sendBeep();
	}

}
