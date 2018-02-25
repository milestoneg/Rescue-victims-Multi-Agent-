/**
 * Main class of the robot
 * 
 * @author Yuan Gao
 *
 */
public class Main {
	public static void main(String[] args) {
		Robot robot = new Robot();
		Server srv = new Server(robot);
		Display display = new Display(robot, 100);

		display.start();

		srv.run();

	}
}
