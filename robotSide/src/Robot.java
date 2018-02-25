import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;

public class Robot {
	// Declare sensors and classes used with the program
	private EV3TouchSensor leftBump, rightBump;
	private EV3UltrasonicSensor UltrasonicSensor;
	private EV3ColorSensor cSensor;
	private SampleProvider leftSP, rightSP, distSP, colourSP;
	private float[] leftSample, rightSample, distSample, colourSample;
	private MovePilot pilot;
	private OdometryPoseProvider PoseProvidor;

	// private variables for distance storing
	private double leftDistance = 0;
	private double rightDistance = 0;
	private double frontDistance = 0;
	private double backDistance = 0;
	// set arena information
	private int Arena_Length = 195;
	//private int Arena_Length = 158;//for the square
	private int Arena_Width = 158;
	private int LengthCellNum = 6;
	private int WidthCellNum = 5;

	private double LengthMovingDis = Arena_Length / LengthCellNum;
	private double WidthMovingDis = Arena_Width / WidthCellNum;

	private static final double ROBOT_ANGULAR_SPEED = 50;// set angular speed
	private static final double ROBOT_LINEAR_SPEED = 13;
	private static final double ROBOT_Acceleration_SPEED = 20;

	private int scanCount = 0;

	private static final int interval = 50;

	public Boolean[] scan_Information;

	public Boolean localizationDone = false;

	public int[][] data = new int[8][8];
	
	public int moving_count = 0;

	/**
	 * Constructor of the class PilotRobot Instantiation of variables
	 */
	public Robot() {
		Brick myEV3 = BrickFinder.getDefault();
		// set up sensors with corresponding port
		leftBump = new EV3TouchSensor(myEV3.getPort("S1"));
		rightBump = new EV3TouchSensor(myEV3.getPort("S4"));
		UltrasonicSensor = new EV3UltrasonicSensor(myEV3.getPort("S3"));
		cSensor = new EV3ColorSensor(myEV3.getPort("S2"));

		leftSP = leftBump.getTouchMode();
		rightSP = rightBump.getTouchMode();
		distSP = UltrasonicSensor.getDistanceMode();
		colourSP = cSensor.getRGBMode();

		leftSample = new float[leftSP.sampleSize()]; // Size is 1
		rightSample = new float[rightSP.sampleSize()]; // Size is 1
		distSample = new float[distSP.sampleSize()]; // Size is 1
		colourSample = new float[colourSP.sampleSize()]; // Size is 3
		// setup parameters for wheels
		Wheel leftWheel = WheeledChassis.modelWheel(Motor.B, 4.37).offset(-5.55);
		Wheel rightWheel = WheeledChassis.modelWheel(Motor.D, 4.4).offset(5.55);
		// set up chassis
		Chassis myChassis = new WheeledChassis(new Wheel[] { leftWheel, rightWheel }, WheeledChassis.TYPE_DIFFERENTIAL);

		pilot = new MovePilot(myChassis);
		pilot.setAngularSpeed(ROBOT_ANGULAR_SPEED);
		pilot.setLinearSpeed(ROBOT_LINEAR_SPEED);
		pilot.setLinearAcceleration(ROBOT_Acceleration_SPEED);
		PoseProvidor = new OdometryPoseProvider(pilot);

	}

	// Getter and Setters
	// return distance for ultrasonic sensor
	public float getDistance() {
		distSP.fetchSample(distSample, 0);
		return distSample[0];
	}

	public float[] getColour() {
		colourSP.fetchSample(colourSample, 0);
		return colourSample; // return array of 3 colors
	}

	/**
	 * Close sensor of the robot
	 */
	public void closeRobot() {
		leftBump.close();
		rightBump.close();
		UltrasonicSensor.close();
		cSensor.close();
	}

	public Boolean[] getScan_Information() {
		return scan_Information;
	}

	public Boolean[] scan_Around() throws InterruptedException {

		scan_Information = new Boolean[4];
		// Scan left
		Motor.C.rotateTo(95);
		Sound.beep();
		leftDistance = getAccurateDistance(interval);
		if (leftDistance < Arena_Width / WidthCellNum) {
			scan_Information[2] = true;
		} else {
			scan_Information[2] = false;
		}
		// sensor face to front
		Motor.C.rotateTo(0);
		frontDistance = getAccurateDistance(interval);
		if (frontDistance < Arena_Width / WidthCellNum) {
			scan_Information[0] = true;
		} else {
			scan_Information[0] = false;
		}
		// Scan right
		Motor.C.rotateTo(-95);
		rightDistance = getAccurateDistance(interval);
		if (rightDistance < Arena_Width / WidthCellNum) {
			scan_Information[1] = true;
		} else {
			scan_Information[1] = false;
		}
		// Scan back
		if (scanCount == 0) {
			pilot.rotate(90);
			backDistance = getAccurateDistance(interval);
			pilot.rotate(-90);
			if (backDistance < Arena_Width / WidthCellNum) {
				scan_Information[3] = true;
			} else {
				scan_Information[3] = false;
			}
		} else {
			scan_Information[3] = false;
		}
		scanCount++;
		return scan_Information;
	}

	public double getAccurateDistance(int interval) throws InterruptedException {
		double Distance = 0.0;
		double[] distanceArray = new double[6];
		boolean distanceAccurate = false;
		// loop until distance is decided to be use
		while (distanceAccurate == false) {
			for (int index = 0; index < distanceArray.length; index++) {
				distanceArray[index] = getDistance() * 100;
				Thread.sleep(interval);
			}
			// find the max and min distance of the array
			double maxvalue = distanceArray[0];
			double minvalue = distanceArray[0];
			for (int index = 0; index < distanceArray.length; index++) {
				if (distanceArray[index] > maxvalue) {
					maxvalue = distanceArray[index];
				}
				if (distanceArray[index] < minvalue) {
					minvalue = distanceArray[index];
				}
			}
			// if the deviation of distances is plus or minus 3cm then decided use these
			// distances
			if (maxvalue < minvalue + 2 && maxvalue > minvalue - 2) {
				distanceAccurate = true;
			}
		}
		double sumDistiance = 0.0;
		for (int index = 0; index < distanceArray.length; index++) {
			sumDistiance = sumDistiance + distanceArray[index];
		}
		Distance = sumDistiance / distanceArray.length;
		return Distance;
	}

	public void travel_orientation(String dir) {
		moving_count++;
		Pose poseBeforeMove = PoseProvidor.getPose();
		switch (dir) {
		case "F":
			// do {
			// pilot.forward();
			// try {
			// Thread.sleep(50);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }while(!(getColour()[0]<0.1&&getColour()[1]<0.1&&getColour()[2]<0.1));
			pilot.forward();
			while (true) {
				if (getColour()[0] < 0.1 && getColour()[1] < 0.1 && getColour()[2] < 0.1) {
					pilot.stop();
					Point poseAfterMove_Front = PoseProvidor.getPose().getLocation();
					float DistanceTraveled_Front = poseBeforeMove.distanceTo(poseAfterMove_Front);
					pilot.travel(DistanceTraveled_Front*0.8);
					break;
				}
			}
			break;
		case "R":
			pilot.rotate(90);
			pilot.forward();
			while (true) {
				if (getColour()[0] < 0.1 && getColour()[1] < 0.1 && getColour()[2] < 0.1) {
					pilot.stop();
					Point poseAfterMove_Right = PoseProvidor.getPose().getLocation();
					float DistanceTraveled_Right = poseBeforeMove.distanceTo(poseAfterMove_Right);
					pilot.travel(DistanceTraveled_Right*0.8);
					break;
				}
			}
			break;
		case "L":
			pilot.rotate(-90);
			pilot.forward();
			while (true) {
				if (getColour()[0] < 0.1 && getColour()[1] < 0.1 && getColour()[2] < 0.1) {
					pilot.stop();
					Point poseAfterMove_Left = PoseProvidor.getPose().getLocation();
					float DistanceTraveled_Left = poseBeforeMove.distanceTo(poseAfterMove_Left);
					pilot.travel(DistanceTraveled_Left*0.8);
					break;
				}
			}
			break;
		case "B":
			pilot.rotate(180);
			pilot.forward();
			while (true) {
				if (getColour()[0] < 0.1 && getColour()[1] < 0.1 && getColour()[2] < 0.1) {
					pilot.stop();
					Point poseAfterMove_Back = PoseProvidor.getPose().getLocation();
					float DistanceTraveled_Back = poseBeforeMove.distanceTo(poseAfterMove_Back);
					pilot.travel(DistanceTraveled_Back*0.8);
					break;
				}
			}
			break;
		}
	}

	public String scan_color() {
		String color_String = "";
		float[] color_RGB = getColour();
		for (int index = 0; index < color_RGB.length; index++) {
			color_String += String.valueOf(color_RGB[index]) + ",";
		}
		// System.out.println(color_String);
		return color_String;
	}

	/**
	 * Front Calibration function
	 * @param x X coordinate Direction vector 
	 * @param y Y coordinate of Direction vector
	 * @throws InterruptedException
	 */
	public void calibration_front(int x, int y) throws InterruptedException {
		double LengthOffset = 10;
		//double LengthOffset = 6;//for the square map;
		//double WidthOffset = 5.5;
		double WidthOffset = 8;
		if (x == 1 || x == -1) {
			pilot.travel(-3);
			Motor.C.rotateTo(0);
			double distance = getAccurateDistance(interval);
			int frontCellnum = (int) distance / (Arena_Width / WidthCellNum);
			// System.out.println(frontCellnum+"!!!!!!");
			double currentOffset = distance - (frontCellnum * (Arena_Width / WidthCellNum));
			// System.out.println(currentOffset+"currentOffset");
			double offsetDiff = WidthOffset - currentOffset;
			if (-offsetDiff < (Arena_Width / WidthCellNum)/2 && -offsetDiff > -(Arena_Width / WidthCellNum)/2) {
				pilot.travel(-offsetDiff);
			}
		} else {
			pilot.travel(-3);			
			Motor.C.rotateTo(0);
			double distance = getAccurateDistance(interval);
			int frontCellnum = (int) distance / (Arena_Length / LengthCellNum);
			double currentOffset = distance - (frontCellnum * (Arena_Length / LengthCellNum));
			// System.out.println(currentOffset+"currentOffset");
			double offsetDiff = LengthOffset - currentOffset;
			if (-offsetDiff < (Arena_Length / LengthCellNum)/2 && -offsetDiff > -(Arena_Length / LengthCellNum)/2) {
				pilot.travel(-offsetDiff);
			}
		}

	}

	/**
	 * Side calibration function
	 * @param x X coordinate Direction vector
	 * @param y Y coordinate Direction vector
	 * @throws InterruptedException
	 */
	public void calibration_side(int x, int y) throws InterruptedException {
		Motor.C.rotateTo(-90);
		double right_dis = getAccurateDistance(interval);
		Motor.C.rotateTo(90);
		double left_dis = getAccurateDistance(interval);
		Motor.C.rotateTo(0);
		double front_dis = getAccurateDistance(interval);
		if (right_dis < Arena_Width / WidthCellNum) {
			double DeviationDis;
			if (y == 1 || y == -1) {
				DeviationDis = 17 - right_dis;// deviation distance between the robot and the object
			} else {
				DeviationDis = 17 - right_dis;// deviation distance between the robot and the object
				//DeviationDis = 10 - right_dis;// deviation distance between the robot and the object
			}

			double LengthDeviationAngle = Math.toDegrees(Math.atan(DeviationDis / (LengthMovingDis / 2)));// calculate
																											// the
																											// deviation
																											// angle for
																											// the
																											// direction
																											// is
																											// north or
																											// south
			double WidthDeviationAngle = Math.toDegrees(Math.atan(DeviationDis / (WidthMovingDis / 2)));// calculate the
																										// deviation
																										// angle
																										// for teh
																										// direction
			if (front_dis > Arena_Width / WidthCellNum) { // is east or west
				// when current direction is north
				if (y == 1 || y == -1) {
					pilot.rotate(-LengthDeviationAngle);
					pilot.travel(LengthMovingDis / 2);
					pilot.rotate(LengthDeviationAngle);
					pilot.travel(-(LengthMovingDis / 2) - 2);// go backward(the distance should be little bit smaller
																// because it
																// is the right-angle side of the triangle)
				} else {
					pilot.rotate(-WidthDeviationAngle);
					pilot.travel(WidthMovingDis / 2);
					pilot.rotate(WidthDeviationAngle);
					pilot.travel(-(WidthMovingDis / 2) - 2);// go backward(the distance should be little bit smaller
															// because it
															// is the right-angle side of the triangle)
				}
			}
		} else if (left_dis < Arena_Width / WidthCellNum) {
			double DeviationDis;
			if (y == 1 || y == -1) {
				DeviationDis = 17 - left_dis;// deviation distance between the robot and the object
			} else {
				DeviationDis = 17 - left_dis;// deviation distance between the robot and the object
				//DeviationDis = 10 - left_dis;// deviation distance between the robot and the object
			}

			double LengthDeviationAngle = Math.toDegrees(Math.atan(DeviationDis / (LengthMovingDis / 2)));// calculate
																											// the
																											// deviation
																											// angle for
																											// the
																											// direction
																											// is
																											// north or
																											// south
			double WidthDeviationAngle = Math.toDegrees(Math.atan(DeviationDis / (WidthMovingDis / 2)));// calculate the
																										// deviation
																										// angle
																										// for teh
																										// direction
			if (front_dis > Arena_Width / WidthCellNum) { // is east or west
				// when current direction is north
				if (y == 1 || y == -1) {
					pilot.rotate(LengthDeviationAngle);
					pilot.travel(LengthMovingDis / 2);
					pilot.rotate(-LengthDeviationAngle);
					pilot.travel(-(LengthMovingDis / 2) - 2);// go backward(the distance should be little bit smaller
																// because it
																// is the right-angle side of the triangle)
				} else {
					pilot.rotate(WidthDeviationAngle);
					pilot.travel(WidthMovingDis / 2);
					pilot.rotate(-WidthDeviationAngle);
					pilot.travel(-(WidthMovingDis / 2) - 2);// go backward(the distance should be little bit smaller
															// because it
															// is the right-angle side of the triangle)
				}
			}
		}

	}

	/**
	 * Method used to let robot to make some noise
	 */
	public void Beep() {
		Sound.beepSequenceUp();
		Sound.beepSequence();
	}


	public boolean inGrid(int x, int y) {
		return y >= 0 && y < 8 && x >= 0 && x < 8;
	}

	public boolean hasObject(int obj, int x, int y) {
		return inGrid(x, y) && (data[x][y] & obj) != 0;
	}

}
