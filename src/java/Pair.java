/**
 * 	Object used to staor the victim path index and the RGB parameters corresbonding to the position
 * @author Yuan Gao
 *
 */
public class Pair {
  int Victim_path_index;
   float[] color_RGB;
  
   //Constructor
  public Pair(int index, float[] color_RGB) {
	  this.Victim_path_index = index;
	  this.color_RGB = color_RGB;
  }
}
