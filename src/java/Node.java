/**
 * Node object used within the multi branch searching tree.
 * Location is the location of the node.
 * Parent indicate the parent node of the node.
 * clildList stores all child node of this node.
 * pathConsuption is the consumption from root to this node.
 * id is the id of the node.
 * @author Yuan Gao
 */
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
		
		//constructor
		public Node(Location location, Node parent, int pathConsumption, int id) {
			this.location = location;		
			this.parent = parent;
			childList = new ArrayList<>();
			//height = ;
			this.pathConsumption = pathConsumption+parent.getPathConsumption();
			this.id  = id;
		}
		//constructor
		public Node(Location location) {
			this.location = location;
			childList = new ArrayList<>();
			//height = 0;
			pathConsumption = 0;
			parent = null;	
			id = 0;
		}
		/**
		 * Method used to check whether the node is a leaf node
		 * @return Boolean
		 */
		public Boolean isLeaf() {
			if(childList.isEmpty()) {
				return true;
			}
			return false;
		}
		
		/**
		 * Method used to get all parent nodes of the node
		 * @return A set of all parent nodes
		 */
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
