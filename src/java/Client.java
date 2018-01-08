/**
 * This class is the PC side code which take responsible to exchange data with robot side.
 * Through our design, the robot is considered as a server and PC is the client.
 * All moving and scan instructions are send by this class.
 * @author Yuan Gao & Geri Georgieva
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// Have to change the IP address to the one of the robot.
public class Client {
	Socket mySocket = null;
	InputStream in;
	DataInputStream dataIn;
	OutputStream out;
	DataOutputStream dataOut;
	Model model;

	//constructor
	public Client(Model model) {
		try {
			//mySocket = new Socket("172.20.1.132", 12345);//IP address of robot NO.11
			mySocket = new Socket("0.0.0.0", 12345);//IP address of simulator
			// input streams
			in = mySocket.getInputStream();
			dataIn = new DataInputStream(in);

			// output streams
			out = mySocket.getOutputStream();
			dataOut = new DataOutputStream(out);
			this.model = model;
		} catch (Exception e) {
		}
	}

	/**
	 * Encode scan color instruction then send to the robot.
	 * Decode data returned by the robot to an float array and return.
	 * @return float array with scanned RGB parameters inside.
	 */
	public float[] sendColorCommand() {
		try {
			float[] colorArray_float = new float[3];
			dataOut.writeUTF("Color");
			dataOut.flush();
			String color = null;
			color = dataIn.readUTF();
			while (color == null) {
				System.out.println("Thread sleep 50!");
				Thread.sleep(50);
			}
			//Decode
			String[] colorArray = color.split(","); //split string by comma
			for (int index = 0; index < colorArray.length; index++) {
				colorArray_float[index] = Float.valueOf(colorArray[index]);
			}

			return colorArray_float;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Send robot a instruction to scan objects around.
	 * Take the result from robot and decode it into a Boolean array.
	 * @return Boolean array contains the information around robot. The order of this array is front, right, left, back.
	 */
	public Boolean[] sendScanCommand() {
		try {
			Boolean[] scanDataBooleanArray = new Boolean[4];
			dataOut.writeUTF("Scan");
			dataOut.flush();
			String scanData = null;
			scanData = dataIn.readUTF();
			while (scanData == null) {
				System.out.println("Thread sleep 50!");
				Thread.sleep(50);

			}
			//Decode into an boolean array
			String[] scanDataStringArray = scanData.split(","); //split string by comma

			for (int index = 0; index < scanDataBooleanArray.length; index++) {
				String ToBoolean = "";
				ToBoolean = scanDataStringArray[index];
				switch (ToBoolean) {
				case "true":
					scanDataBooleanArray[index] = true;
					break;
				case "false":
					scanDataBooleanArray[index] = false;
					break;
				}

			}
			return scanDataBooleanArray;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Send a moving instruction to robot. 
	 * Thread block until robot return a message back.
	 * @param direction front, right, left or back.
	 */
	public void sendMoveCommand(String direction) {
		try {
			dataOut.writeUTF(direction);
			dataOut.flush();
			System.out.println("Sent " + direction);
			String answerBack = null;
			answerBack = dataIn.readUTF();
			while (answerBack == null) {
				System.out.println("Thread sleep 50!");
				Thread.sleep(50);

			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Send calibration instruction to robot.
	 * Thread will block until robot return some data.
	 * @param direction current direction.
	 */
	public void sendCaliFront(DirectionVector direction) {
		String direction_string = direction.DVgetX() + "," + direction.DVgetY();
		try {
			dataOut.writeUTF(direction_string);
			dataOut.flush();
			System.out.println("Sent " + direction_string);
			String answerBack = null;
			answerBack = dataIn.readUTF();
			while (answerBack == null) {
				System.out.println("Thread sleep 50!");
				Thread.sleep(50);

			}
		} catch (Exception e) {

		}
	}

	/**
	 * Send instruction to robot to let robot to make some noise as the end of the mission.
	 * Thread will block until robot return some data.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void sendBeep() throws IOException, InterruptedException {
		dataOut.writeUTF("Beep");
		dataOut.flush();
		System.out.println("Sent " + "Beep");
		String answerBack = null;
		answerBack = dataIn.readUTF();
		while (answerBack == null) {
			System.out.println("Thread sleep 50!");
			Thread.sleep(50);

		}
	}

	/**
	 * Send robot a message to tell it the localization process is done.
	 * Thread will be blocked until robot return some data.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void sendLocalizationDone() throws IOException, InterruptedException {
		dataOut.writeUTF("LocalizationDone");
		dataOut.flush();
		System.out.println("Sent " + "Localization Done");
		String answerBack = null;
		answerBack = dataIn.readUTF();
		while (answerBack == null) {
			System.out.println("Thread sleep 50!");
			Thread.sleep(50);
		}
	}

	/**
	 * Send a two dimensional array to the robot to provide it information to draw the map on the LCD screen.
	 * @throws IOException 
	 * @throws InterruptedException
	 */
	public void sendMapData() throws IOException, InterruptedException {
		int[][] data = model.getData();
		//Encode the two dimensional array to a string
		String data_string = "data:";
		for (int widthIndex = 0; widthIndex < model.getWidth(); widthIndex++) {
			for (int heightIndex = 0; heightIndex < model.getHeight(); heightIndex++) {
				data_string += data[widthIndex][heightIndex] + ",";
			}
		}
		//Send the string to the robot
		dataOut.writeUTF(data_string);
		dataOut.flush();

		System.out.println("Sent " + data_string);
		String answerBack = null;
		answerBack = dataIn.readUTF();
		while (answerBack == null) {
			System.out.println("Thread sleep 50!");
			Thread.sleep(50);
		}

	}
}