/**
 * This class is the display class related WarView class.
 * After WarView being initialized then this class could display the model on the PC screen.
 * @Yuan Gao
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.HashMap;
import java.util.LinkedList;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

class WarView extends GridWorldView {
	public HashMap<Location, LinkedList<DirectionVector>> possibilities = new HashMap<Location, LinkedList<DirectionVector>>();
	public Boolean Status = false;

	//constructor
	public WarView(Model model) {
		super(model, "War World", 800);
		defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
		setVisible(true);
		repaint();
	}

	/** draw application objects */
	@Override
	public void draw(Graphics g, int x, int y, int object) {
		switch (object) {
		case Model.CriticalVictim:
			drawCriticalVictim(g, x, y);
			break;
		case Model.MinorVictim:
			drawMinorVictim(g, x, y);
			break;
		case Model.SeriousVictim:
			drawSeriousVictim(g, x, y);
			break;
		case Model.possibleVictim:
			drawPossibleVictim(g, x, y);
			break;
		case Model.possibleLocation:
			if (Status == false) {
				drawPossibilities(g);
			}
			break;

		}
	}
	
	/**
	 * Method used to draw agent on the screen.
	 */
	@Override
	public void drawAgent(Graphics g, int x, int y, Color c, int id) {
		String label = "Scout";
		c = Color.GREEN;
		if (id == 0) {
			c = Color.yellow;
		}
		//draw circle
		g.setColor(c);
		g.fillOval(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);

		if (id == 0) {
			g.setColor(Color.black);
		} else {
			g.setColor(Color.white);
		}
		//draw string on the circle
		super.drawString(g, x, y, defaultFont, label);
		
	}

	/**
	 * Method used to draw all possibilities on the screen.
	 * 
	 * @param g graphics
	 */
	public void drawPossibilities(Graphics g) {
		java.util.Random random = new java.util.Random();

		g.setColor(new Color(random.nextInt(150) + 50, random.nextInt(150) + 50, random.nextInt(150) + 50));

		//traverse the possibilities table and draw them
		for (Location location : possibilities.keySet()) {
			int x = location.x;
			int y = location.y;
			for (DirectionVector direction : possibilities.get(location)) {
				Polygon polygon = new Polygon();
				// System.out.println(direction.DVgetX()+","+direction.DVgetY()+"@@@@@@@");
				if (direction.DVgetX() == 0 && direction.DVgetY() == -1) {
					polygon.addPoint(x * cellSizeW, (y + 1) * cellSizeH);
					polygon.addPoint((x + 1) * cellSizeW, (y + 1) * cellSizeH);
					polygon.addPoint(x * cellSizeW + cellSizeW / 2, y * cellSizeH);
					g.fillPolygon(polygon);
				} else if (direction.DVgetX() == 0 && direction.DVgetY() == 1) {
					polygon.addPoint(x * cellSizeW, y * cellSizeH);
					polygon.addPoint((x + 1) * cellSizeW, y * cellSizeH);
					polygon.addPoint(x * cellSizeW + cellSizeW / 2, (y + 1) * cellSizeH);
					g.fillPolygon(polygon);
				} else if (direction.DVgetX() == -1 && direction.DVgetY() == 0) {
					polygon.addPoint((x + 1) * cellSizeW, y * cellSizeH);
					polygon.addPoint((x + 1) * cellSizeW, (y + 1) * cellSizeH);
					polygon.addPoint(x * cellSizeW, y * cellSizeH + cellSizeH / 2);
					g.fillPolygon(polygon);
				} else {
					polygon.addPoint(x * cellSizeW, y * cellSizeH);
					polygon.addPoint(x * cellSizeW, (y + 1) * cellSizeH);
					polygon.addPoint((x + 1) * cellSizeW, y * cellSizeH + cellSizeH / 2);
					g.fillPolygon(polygon);

				}
			}
		}
	
	}

	/**
	 * Draw crucial victim on the screen
	 * @param g graphics
	 * @param x victim X coordinate
	 * @param y victim Y coordinate
	 */
	public void drawCriticalVictim(Graphics g, int x, int y) {
		g.setColor(Color.red);
		g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1, cellSizeH - 1);
		g.setColor(Color.black);
		g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
		g.setColor(Color.white);
		drawString(g, x, y, defaultFont, "Victim");
	}

	/**
	 * Draw serious victim on the screen
	 * @param g graphics
	 * @param x victim X coordinate
	 * @param y victim Y coordinate
	 */
	public void drawSeriousVictim(Graphics g, int x, int y) {
		g.setColor(Color.BLUE);
		g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1, cellSizeH - 1);
		g.setColor(Color.black);
		g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
		g.setColor(Color.white);
		drawString(g, x, y, defaultFont, "Victim");
	}

	/**
	 * Draw minor victim on the screen
	 * @param g graphics
	 * @param x victim X coordinate
	 * @param y victim Y coordinate
	 */
	public void drawMinorVictim(Graphics g, int x, int y) {
		g.setColor(Color.GREEN);
		g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1, cellSizeH - 1);
		g.setColor(Color.black);
		g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
		g.setColor(Color.white);
		drawString(g, x, y, defaultFont, "Victim");
	}

	/**
	 * Draw possible victims on the screen(unexplored victims)
	 * @param g graphics
	 * @param x victim X coordinate
	 * @param y victim Y coordinate
	 */
	public void drawPossibleVictim(Graphics g, int x, int y) {
		g.setColor(Color.orange);
		g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1, cellSizeH - 1);
		g.setColor(Color.black);
		g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
		g.setColor(Color.white);
		drawString(g, x, y, defaultFont, "Victim");
	}

}
