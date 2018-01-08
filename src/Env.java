import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.grid.Location;

public class Env extends Environment {

	static Logger logger = Logger.getLogger(MarsEnv.class.getName());

	// view initial
	private static Model model;
	private WarView view;

	

	private LinkedList<Location> victims = new LinkedList<>();

	//HashMap<Location, LinkedList<DirectionVector>> possibilities = new HashMap<Location, LinkedList<DirectionVector>>();

	// robotsimulator
	robSimulator simulator;
	
	Boolean[] scan_information;

	// Direction vectors
	DirectionVector north = new DirectionVector(0, -1);
	DirectionVector south = new DirectionVector(0, 1);
	DirectionVector west = new DirectionVector(-1, 0);
	DirectionVector east = new DirectionVector(1, 0);

	@Override
	public void init(String[] args) {

		model = new Model();
		view = new WarView(model);
		model.setView(view);
		victims = model.getVictims();
		simulator = new robSimulator(model);
		//updatePercepts();
	}

	@Override
	public boolean executeAction(String ag, Structure action) {
		logger.info(ag + " doing: " + action);
		try {
			Thread.sleep(200);
			if (action.getFunctor().equals("nextPosition")) {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.nextPosition(x, y);
			} else if (action.getFunctor().equals("currentpos")) {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.currentPosition(x, y);
			} else if (action.getFunctor().equals("initial_location")) {
				initial_location();
			} else if (action.getFunctor().equals("possible_location")) {
				possible_location();
			} else if(action.getFunctor().equals("loclization_move_next")){
				loclization_move_next();
			}else if(action.getFunctor().equals("update_percepts")){
				updatePercepts();
			}else if (action.getFunctor().equals("setfinaldir")) {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.setFinalDirection(x, y);
			}else {
				return false;
			}
				
			}catch (Exception e) {
			e.printStackTrace();
		}

		// updatePercepts();

		try {
			Thread.sleep(200);
		} catch (Exception e) {
		}
		informAgsEnvironmentChanged();
		return true;
	}

	// calculate path to all victims
	private List<Location> calculateMinPath() {

		LinkedList<List<Location>> AllPath = new LinkedList<List<Location>>();
		LinkedList<Location> optimalPath = new LinkedList<Location>();
		AllPath = calAllPath(model.getAgPos(0));
		while (AllPath.isEmpty() == false) {
			int minIndex = calMinCost(AllPath);
			List subList = AllPath.get(minIndex);
			for (int index = 0; index < subList.size(); index++) {
				optimalPath.add((Location) subList.get(index));
			}
			AllPath.remove(minIndex);
			victims.remove(subList.get(subList.size() - 1));
			AllPath = calAllPath((Location) subList.get(subList.size() - 1));
		}

		return optimalPath;
	}

	private LinkedList<List<Location>> calAllPath(Location currentLocation) {
		LinkedList<List<Location>> AllPath = new LinkedList<List<Location>>();
		AStar calculator = new AStar(model);
		for (int index = 0; index < victims.size(); index++) {
			Location startPoint = new Location(currentLocation.x, currentLocation.y);
			Location endPoint = new Location(victims.get(index).x, victims.get(index).y);
			AllPath.add(calculator.findPath(startPoint, endPoint));
		}
		return AllPath;
	}

	private int calMinCost(LinkedList<List<Location>> AllPath) {
		int minCost = AllPath.get(0).size();
		int minIndex = 0;
		for (int index = 0; index < AllPath.size(); index++) {
			if (AllPath.get(index).size() < minCost) {
				minCost = AllPath.get(index).size();
				minIndex = index;
			}
		}
		return minIndex;
	}

	/** creates the agents perception based on the MarsModel */
	void updatePercepts() {
		// clearPercepts();
		List<Location> nextVictims = calculateMinPath();
		LinkedList<Term> nextVictims_Term = new LinkedList<>();
		for (int index = 0; index < nextVictims.size(); index++) {
			// nextVictims_Term.add(Literal.parseLiteral("pos("+nextVictims.get(index).x+","+nextVictims.get(index).y+")"));
			NumberTerm xCoord = ASSyntax.createNumber(nextVictims.get(index).x);
			NumberTerm yCoord = ASSyntax.createNumber(nextVictims.get(index).y);
			nextVictims_Term.add(ASSyntax.createLiteral("pos", xCoord, yCoord));
		}
		addPercept("doctor", ASSyntax.createLiteral("path", ASSyntax.createList(nextVictims_Term)));

	}

	// find out all possible location and headings
	private void initial_location() {
		// initial possibilities
		// LinkedList<Literal> allPossibilities = new LinkedList<>();
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
						// debug
						// System.out.println(pair);
					}
				}
			}
		}

	}

	private void possible_location() {
		 scan_information = simulator.scan_Around();

		// debug
		System.out.println(scan_information[0] + "," + scan_information[1] + "," + scan_information[2] + ","
				+ scan_information[3]+" :simulator");
        System.out.println("Size of key: "+ view.possibilities.keySet().size()); 
		
        Iterator<Location> key_iterator = view.possibilities.keySet().iterator();
		//for (Location key : view.possibilities.keySet()) {
        while(key_iterator.hasNext()) {
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
				
					//debug
					System.out.println("Location:"+key);
					System.out.println("direction vection:"+direction.DVgetX()+","+direction.DVgetY());
					
					System.out.println("fornt:"+front_location+";"+map_information[0]);
					System.out.println("right:"+right_location+";"+map_information[1]);
					System.out.println("fornt:"+left_location+";"+map_information[2]);
					System.out.println("fornt:"+back_location+";"+map_information[3]);
					
					System.out.println("+++++++++++++++++++++++++++++++++++++");
				// debug
				if (Arrays.equals(map_information, scan_information)) {
					System.out.println(map_information[0] + "," + map_information[1] + "," + map_information[2] + ","
							+ map_information[3]);
				}

				if (!Arrays.equals(map_information, scan_information)) {
					// System.out.println(view.possibilities.get(key).size()+"before");
					delete_index.add(direction);		
					//view.possibilities.get(key).remove(direction);
					// System.out.println(view.possibilities.get(key).size()+"after");
				}
			}
			for(DirectionVector delete_Vectors :delete_index) {
				view.possibilities.get(key).remove(delete_Vectors);
			}
			// if a locations' possible directions equals to 0
			/*if (view.possibilities.get(key).isEmpty()) {
				view.possibilities.keySet().remove(key);
			}*/
		}
		// if a locations' possible directions equals to 0
		Iterator<Location> delet_iterator = view.possibilities.keySet().iterator();

		while  (delet_iterator.hasNext()) {
			LinkedList<DirectionVector> i = view.possibilities.get(delet_iterator.next());
			if(i.isEmpty()) {
				delet_iterator.remove();
			}
        }
		//Debug
		System.out.println(view.possibilities.keySet().size() + "size");

		ScoutUpdatePercepts();
	}

private void loclization_move_next() {
	HashMap<Location,LinkedList<DirectionVector>> tmp = new HashMap<>();
	if(scan_information[0] == false) {
		simulator.goForward();
		for(Location location : view.possibilities.keySet()) {
			
			for(DirectionVector directionVector : view.possibilities.get(location)) {
				Location newlocation = new Location(location.x+directionVector.DVgetX(), location.y+directionVector.DVgetY());
				LinkedList<DirectionVector> directions;
				try {
				 directions = view.possibilities.get(newlocation);
				 if(!directions.contains(directionVector)) {
					 directions.add(directionVector);
				 }else {
					 //do nothing
				 }				 
				}catch (Exception e) {
					directions = new LinkedList<>();
					directions.add(directionVector);
				}
						
				tmp.put(newlocation, directions);
			}
		}
		view.possibilities.clear();
		view.possibilities = tmp;
	}else if(scan_information[1] == false) {
	simulator.goRight();
	for(Location location : view.possibilities.keySet()) {
		for(DirectionVector directionVector : view.possibilities.get(location)) {
			Location newlocation = new Location(location.x-directionVector.DVgetY(),location.y+directionVector.DVgetX());
			DirectionVector newdirection = new DirectionVector(-directionVector.DVgetY(), directionVector.DVgetX());
			LinkedList<DirectionVector> directions = view.possibilities.get(newlocation);
			try {
				 directions = view.possibilities.get(newlocation);
				 if(!directions.contains(newdirection)) {
					 directions.add(newdirection);
				 }else {
					 //do nothing
				 }				 
				}catch (Exception e) {
					directions = new LinkedList<>();
					directions.add(newdirection);
				}
			tmp.put(newlocation, directions);
		}
	}
	view.possibilities.clear();
	view.possibilities = tmp;
}else if(scan_information[2] == false) {
	simulator.goLeft();
	for(Location location : view.possibilities.keySet()) {
		
		for(DirectionVector directionVector : view.possibilities.get(location)) {
			Location newlocation = new Location(location.x+directionVector.DVgetY(), location.y-directionVector.DVgetX());
			DirectionVector newdirection = new DirectionVector(directionVector.DVgetY(), -directionVector.DVgetX());
			LinkedList<DirectionVector> directions = view.possibilities.get(newlocation);
			try {
				 directions = view.possibilities.get(newlocation);
				 if(!directions.contains(newdirection)) {
					 directions.add(newdirection);
				 }else {
					 //do nothing
				 }				 
				}catch (Exception e) {
					directions = new LinkedList<>();
					directions.add(newdirection);
				}
			tmp.put(newlocation, directions);
		}
	}
	view.possibilities.clear();
	view.possibilities = tmp;
}else if(scan_information[3] == false){
	simulator.goBackward();
	for(Location location : view.possibilities.keySet()) {
		
		for(DirectionVector directionVector : view.possibilities.get(location)) {
			Location newlocation = new Location(location.x-directionVector.DVgetX(), location.y-directionVector.DVgetY());
			DirectionVector newdirection = new DirectionVector(-directionVector.DVgetX(), -directionVector.DVgetY());
			LinkedList<DirectionVector> directions = view.possibilities.get(newlocation);
			try {
				 directions = view.possibilities.get(newlocation);
				 if(!directions.contains(newdirection)) {
					 directions.add(newdirection);
				 }else {
					 //do nothing
				 }				 
				}catch (Exception e) {
					directions = new LinkedList<>();
					directions.add(newdirection);
				}
			tmp.put(newlocation, directions);
		}
	}
	view.possibilities.clear();
	view.possibilities = tmp;
}
	//debug
	System.out.println("After move size of key:"+view.possibilities.keySet().size());
	System.out.println("After move size of entity:"+view.possibilities.values().size());
	for(Location location : view.possibilities.keySet()) {
		for(DirectionVector directionVector : view.possibilities.get(location)) {
			System.out.println(location+"+++"+directionVector.DVgetX()+","+directionVector.DVgetY()+" after move");
		}
		
	}
	ScoutUpdatePercepts();
}
	public void ScoutUpdatePercepts() {
		clearPercepts("scout");
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

	

	

}
