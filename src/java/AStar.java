/**
 * Implementation of A* algorithm.
 * This A* is suitable for the GridWorldModel.
 * @author Yuan Gao
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class AStar {

	

	private List<Node> openList = new ArrayList<>();
	private List<Node> closeList = new ArrayList<>();

	private GridWorldModel arena;
	
	//Constructor
	public AStar(GridWorldModel arena) {
		this.arena = arena;
	}

	/**
	 * Main implementation of the algorithm.
	 * @param from Start location
	 * @param to target location
	 * @return List of location indicate the path
	 */
	public List<Location> findPath(Location from, Location to) {
		openList.clear();
		closeList.clear();
		Node start = new Node(from);
		Node target = new Node(to);
		Node point = start;
		closeList.add(point);
		boolean flag = false;
		while (!flag) {
			for (Node next : point.getSurrounds()) {
				if (isOutOfBounds(next)) {
					continue;
				}
				if (next.equals(target)) {
					target.parent = point;
					flag = true;
					break;
				}
				if (closeList.contains(next)) {
					continue;
				}
				if (openList.contains(next)) {
					if (point.g + 1 < next.g) {
						next.parent = point;
						next.g++;
						next.f = next.g + next.h;
					}
					continue;
				}
				if (!isOccupied(next)) {
					next.parent = point;
					next.g++;
					next.h = getDistance(point, next);
					next.f = next.g + next.h;
					openList.add(next);
				}
			}
			if (flag) {
				break;
			}
			if (openList.isEmpty()) {
				return new ArrayList<>();
			}
			point = Collections.min(openList);
			openList.remove(point);
			closeList.add(point);
		}
		List<Location> path = new ArrayList<>();
		
		point = target;
		while (point.parent != null) {
			path.add(point.toLocation());
			point = point.parent;
		}
		Collections.reverse(path);
		
		
		return path;
	}

	/**
	 * Method used to calculate the manhattan distance between two nodes.
	 * @param node1 
	 * @param node2
	 * @return int distance
	 */
	private int getDistance(Node node1, Node node2) { // Manhattan
		return Math.abs(node1.x - node2.x) + Math.abs(node1.y - node2.y);
	}
	
	/**
	 * Method used to check whether a node is out of bound
	 * @param node
	 * @return boolean
	 */
	private boolean isOutOfBounds(Node node) {
		if (node.x < 0 || node.y < 0) {
			return true;
		}
		if (node.x >= arena.getWidth() || node.y >= arena.getHeight()) {
			return true;
		}
		return false;
	}

	/**
	 * Method to check whether a node is occupied by a obstacle.
	 * @param node
	 * @return boolean
	 */
	private boolean isOccupied(Node node) {
		return !arena.isFreeOfObstacle(node.x,node.y);
	}

	/**
	 * Implementation of node object
	 *
	 */
	private class Node implements Comparable<Node> {

		private int x;
		private int y;

		private int g;
		private int h;
		private int f;

		private Node parent;

		public Node(Location position) {
			this(position.x, position.y);
		}

		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Location toLocation() {
			return new Location (x,y);
		}

		public List<Node> getSurrounds() {
			List<Node> surrounds = new ArrayList<>(4);
			surrounds.add(new Node(x, y + 1)); // up
			surrounds.add(new Node(x, y - 1)); // down
			surrounds.add(new Node(x - 1, y)); // left
			surrounds.add(new Node(x + 1, y)); // right
			return surrounds;
		}

		/**
		 * Override method defined by the comparable interface.
		 */
		@Override
		public int compareTo(Node other) {
			return f - other.f;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

		private AStar getOuterType() {
			return AStar.this;
		}

	}

	
	public static void main(String[] args) {
		Model model = new Model();
		AStar aStar = new AStar(model);
		List fList = aStar.findPath(new Location(1, 1), new Location(5, 2));
		for(int index = 0; index<fList.size(); index++) {
			System.out.println(fList.get(index));
		}
		
		System.out.println(fList.size());
	}
	 

}
