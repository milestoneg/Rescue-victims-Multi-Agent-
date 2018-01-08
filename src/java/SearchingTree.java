
/**
 * Implementation of a multi branches tree.
 * This tree is used to calculate the optimal path within the Env class.
 * @author Yuan Gao
 */
import java.util.LinkedList;

import jason.environment.grid.Location;

public class SearchingTree {

	private Node root;
	private LinkedList<Node> AllNodes = new LinkedList<>();

	// constructor
	public SearchingTree(Location rootLocation) {
		Node root_building = new Node(rootLocation);
		root = root_building;
		AllNodes.add(root_building);
	}

	/**
	 * Return node object with specific id.
	 * @param id node id
	 * @return node with specific id
	 */
	public Node getNode(int id) {
		for (Node node : AllNodes) {
			if (node.getId() == id) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Add a node into the tree. The location, parent node, path consumption and id should be specified.
	 * @param location location 
	 * @param parent parent node
	 * @param pathConsumption consumption from root node to this node
	 * @param id node id
	 */
	public void addNode(Location location, Node parent, int pathConsumption, int id) {
		Node node = new Node(location, parent, pathConsumption, id);
		parent.getChildList().add(node);
		AllNodes.add(node);
		
	}

	/**
	 * Return all nodes without child node
	 * @return list of leaf node
	 */
	public LinkedList<Node> allLeafNodes() {
		LinkedList<Node> allLeafNodes = new LinkedList<>();
		for (Node node : AllNodes) {
			if (node.isLeaf()) {
				allLeafNodes.add(node);
			}
		}
		return allLeafNodes;
	}

}
