import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private static final int port = 12345;
	private DataOutputStream dataOut;
	private String answer = "";
	private DataInputStream dataIn;

	// Only used for simulator!
	// Model model = new Model();
	// robSimulator robot = new robSimulator(model);

	// Robot robot = new Robot();
	Robot robot;

	// initial display
	// Display display = new Display(robot, 100);

	Server(Robot robot) {
		this.robot = robot;
	}

	// Method get answer from the PC (this will be instructions what to do next )
	public String getAnswerFromPC() {
		String done = "Done";
		try {
			answer = dataIn.readUTF();
			//System.out.println("Got " + answer);
		} catch (IOException e) {
			// suitable printing that we had exception while trying to get answer from pc
		}
		if (answer.equals("Front")) {
			//System.out.println("Going front" + answer);
			// moveRobot(distance);

			// code used to simulate
			robot.travel_orientation("F");
			return done;
		} else if (answer.equals("Back")) {
			// moveRobot(-distance)
			// or
			// turnRobot(180)
			// moveRobot(distance)

			// code used to simulate
			robot.travel_orientation("B");
			return done;
		} else if (answer.equals("Left")) {
			// turnRobot(90)
			// and maybe moveRobot(distance)

			// code used to simulate
			robot.travel_orientation("L");
			return done;
		} else if (answer.equals("Right")) {
			// turnRobot(-90)
			// and maybe moveRobot(distance)

			// code used to simulate
			robot.travel_orientation("R");
			return done;
		} else if (answer.equals("Beep")) {
			// turnRobot(-90)
			// and maybe moveRobot(distance)

			// code used to simulate
			robot.Beep();
			return done;
		} else if (answer.equals("LocalizationDone")) {
			// turnRobot(-90)
			// and maybe moveRobot(distance)

			// code used to simulate
			robot.localizationDone = true;
			return done;
		} else if (answer.equals("-1,0")) {
			int x = -1;
			int y = 0;
			try {
				robot.calibration_front(x, y);
				robot.calibration_side(x, y);
				robot.calibration_front(x, y);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return done;
		} else if (answer.equals("1,0")) {

			int x = 1;
			int y = 0;
			try {
				robot.calibration_front(x, y);
				robot.calibration_side(x, y);
				robot.calibration_front(x, y);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return done;
		} else if (answer.equals("0,1")) {
			int x = 0;
			int y = 1;
			try {
				robot.calibration_front(x, y);
				robot.calibration_side(x, y);
				robot.calibration_front(x, y);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return done;
		} else if (answer.equals("0,-1")) {
			int x = 0;
			int y = -1;
			try {
				robot.calibration_front(x, y);
				robot.calibration_side(x, y);
				robot.calibration_front(x, y);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return done;
		} else if (answer.equals("Color")) {
			String color = robot.scan_color();
			// return color;
			return color;
		} else if (answer.equals("Scan")) {
			Boolean[] booleanArray;
			try {
				booleanArray = robot.scan_Around();

				String stringResult = "";
				for (int index = 0; index < booleanArray.length; index++) {
					stringResult += String.valueOf(booleanArray[index]) + ",";
				}
				return stringResult;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}else if (answer.split(":")[0].equals("data")) {
			//System.out.println(answer);
			String[] data_splited = answer.split(":")[1].split(",");
			
			int count = 0;
			while(count < 7) {
			for(int index = 0; index<8; index++) {
				robot.data[count][index] = Integer.valueOf(data_splited[index+count*8]);
			}
			count ++;
			}
			return done;
		}
		return "Error";
	}

	public void run() {
		try {

			// display.run();

			// server and client socket
			ServerSocket server = new ServerSocket(port);
			Socket client = server.accept();

			// output streams
			OutputStream out = client.getOutputStream();
			dataOut = new DataOutputStream(out);

			// input streams
			InputStream in = client.getInputStream();
			dataIn = new DataInputStream(in);

			while (client.isConnected() && !client.isClosed()) {
				String answerToJason = getAnswerFromPC();
				try {
					dataOut.writeUTF(answerToJason);
					dataOut.flush();
					Thread.sleep(500);
				} catch (Exception e) {
					//System.out.println(e);
					System.exit(0);
				}
			}

			dataOut.close();
			out.close();
			server.close();

		} catch (IOException e) {
			System.out.println("Disconnected.");
		} finally {
			// can clean up here
		}
	}
}
