
import java.text.DecimalFormat;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;

/**
 * Thread used to draw the map on the lcd screen two showing mode implemented
 * which is probability and grid map
 * 
 * @author Yuan Gao
 *
 */
public class Display extends Thread {

	private int delay;
	public Robot robot;
	public static final int CLEAN = 0;
	public static final int AGENT = 2;
	public static final int OBSTACLE = 4;
	public static final int CriticalVictim = 16; 
	public static final int MinorVictim = 32;
	public static final int SeriousVictim = 64;
	public static final int possibleVictim = 128;
	public static final int possibleLocation = 256;

	GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();

	// Make the monitor a daemon and set
	// the robot it monitors and the delay
	public Display(Robot r, int d) {
		this.setDaemon(true);
		delay = d;
		robot = r;
	}

	// The monitor writes various bits of robot state to the screen, then
	// sleeps.
	@Override
	public void run() {
		// The decimalformat here is used to round the number to three significant
		// digits
		DecimalFormat df = new DecimalFormat("####0.000");
		lcd.setFont(Font.getSmallFont());

		while (true) {
			// lcd.clear();
			// float[] color = robot.getColour();
			// lcd.drawString("RGB:"+color[0]+","+color[1]+","+color[3], 10, 20, 0);
			//
			//
			lcd.clear();
			if (robot.localizationDone == false) {
				lcd.drawString("Moving Count: "+robot.moving_count, 35, 115, 0);
				lcd.drawRect(70, 50, 30, 30);
				lcd.drawString("R", 70 + 15, 50 + 15, 0);
				lcd.drawRect(70 - 30, 50, 30, 30);// left
				lcd.drawRect(70 + 30, 50, 30, 30);// right
				lcd.drawRect(70, 50 + 30, 30, 30);// back
				lcd.drawRect(70, 50 - 30, 30, 30);// front

				try {
					if (robot.getScan_Information()[0] == true) {
						lcd.fillRect(70, 50 - 30, 30, 30);
					}
					if (robot.getScan_Information()[1] == true) {
						lcd.fillRect(70 + 30, 50, 30, 30);
					}
					if (robot.getScan_Information()[2] == true) {
						lcd.fillRect(70 - 30, 50, 30, 30);
					}
					if (robot.getScan_Information()[3] == true) {
						lcd.fillRect(70, 50 + 30, 30, 30);
					}
				} catch (Exception e) {

				}
			}else {
				//draw map based on the data array
				//for(int widthIndex = 0; widthIndex<8; widthIndex++) {//code for draw the 5*6 arena
				for(int widthIndex = 0; widthIndex<8; widthIndex++) {//code for draw the 6*6 arena
					for(int heightIndex = 0; heightIndex<8; heightIndex++) {
						lcd.drawRect((widthIndex * 10)+45, heightIndex * 12, 10, 12);
						if(robot.hasObject(OBSTACLE, widthIndex, heightIndex)) {
							lcd.fillRect((widthIndex * 10)+45, heightIndex * 12, 10, 12);
						}else if(robot.hasObject(possibleVictim, widthIndex, heightIndex)) {
							lcd.drawString("?", (widthIndex * 10)+45+3, (heightIndex * 12)+4, 0);
						}else if(robot.hasObject(CriticalVictim, widthIndex, heightIndex)) {
							lcd.drawString("C", (widthIndex * 10)+45+3, (heightIndex * 12)+4, 0);
						}else if(robot.hasObject(MinorVictim, widthIndex, heightIndex)) {
							lcd.drawString("M", (widthIndex * 10)+45+3, (heightIndex * 12)+4, 0);
						}else if(robot.hasObject(SeriousVictim, widthIndex, heightIndex)) {
							lcd.drawString("S", (widthIndex * 10)+45+3, (heightIndex * 12)+4, 0);
						}else if(robot.hasObject(AGENT, widthIndex, heightIndex)) {
							lcd.drawString("R", (widthIndex * 10)+45+3, (heightIndex * 12)+4, 0);
						}
					}
				}
				lcd.drawString("Moving Count: "+robot.moving_count, 35, 110, 0);
			
			}
			// if escape is down then quit the program
			if (Button.ESCAPE.isDown()) {
				robot.closeRobot();
				System.exit(0);
			}
			try {
				sleep(delay);
			} catch (Exception e) {
				// We have no exception handling
				;
			}
		}
	}

}