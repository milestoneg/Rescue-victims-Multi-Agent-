/**
 * Object of direction vector
 * all moving  procedure within this program is based on this
 * @author Yuan Gao
 *
 */
public class DirectionVector {
	private int Xcoord;
	private int Ycoord;
	
	//constructor of the direction vector
	public DirectionVector(int Xcoord, int Ycoord) {
		this.Xcoord  = Xcoord;
		this.Ycoord = Ycoord;
	}

	public int DVgetX() {
		return Xcoord;
	}
	public int DVgetY() {
		return Ycoord;
	}
}
