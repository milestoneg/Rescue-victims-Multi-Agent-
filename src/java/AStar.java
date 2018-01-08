

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class AStar {

	

	private List<Node> openList = new ArrayList<>();
	private List<Node> closeList = new ArrayList<>();

	private GridWorldModel arena;
	
	public AStar(GridWorldModel arena) {
		this.arena = arena;
	}

	public List<Location> findPath(Location from, Location to) {
		openList.clear();
		closeList.clear();
		Node start = new Node(from);
		Node end = new Node(to);
		Node point = start;
		closeList.add(point);
		boolean flag = false;
		while (!flag) {
			for (Node next : point.getSurrounds()) {
				if (isOutOfBounds(next)) {
					continue;
				}
				if (next.equals(end)) {
					end.parent = point;
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
		
		point = end;
		while (point.parent != null) {
			path.add(point.toLocation());
			point = point.parent;
		}
		Collections.reverse(path);
		
		
		return path;
	}

	private int getDistance(Node node1, Node node2) { // Manhattan
		return Math.abs(node1.x - node2.x) + Math.abs(node1.y - node2.y);
	}

	private boolean isOutOfBounds(Node node) {
		if (node.x < 0 || node.y < 0) {
			return true;
		}
		if (node.x >= arena.getWidth() || node.y >= arena.getHeight()) {
			return true;
		}
		return false;
	}

	private boolean isOccupied(Node node) {
		//System.out.println(node.x+","+node.y+!arena.isFreeOfObstacle(node.x,node.y));
		return !arena.isFreeOfObstacle(node.x,node.y);
	}

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
