import java.util.ArrayList;
import java.util.LinkedList;

import jason.environment.grid.Location;

public class Node {
	
		private Location location;
		private Node parent;
		private ArrayList<Node> childList;
		//private int height;
		private int pathConsumption;
		private int id;
		
		public Node(Location location, Node parent, int pathConsumption, int id) {
			this.location = location;		
			this.parent = parent;
			childList = new ArrayList<>();
			//height = ;
			this.pathConsumption = pathConsumption+parent.getPathConsumption();
			this.id  = id;
		}
		
		public Node(Location location) {
			this.location = location;
			childList = new ArrayList<>();
			//height = 0;
			pathConsumption = 0;
			parent = null;	
			id = 0;
		}
		
		public Boolean isLeaf() {
			if(childList.isEmpty()) {
				return true;
			}
			return false;
		}
		
		public LinkedList<Node> getParentSet(){
			LinkedList<Node> parentSet = new LinkedList<>();
			Node parent_interate = parent;
			while(parent_interate!=null) {
				parentSet.add(parent_interate);				
				parent_interate = parent_interate.getParent();	
			}
			return parentSet;
		}
		
		//Getters 
		public Location getLocation() {
			return location;
		}

		public ArrayList<Node> getChildList() {
			return childList;
		}

		public Node getParent() {
			return parent;
		}

		public int getPathConsumption() {
			return pathConsumption;
		}

		public int getId() {
			return id;
		}
		
	
}
