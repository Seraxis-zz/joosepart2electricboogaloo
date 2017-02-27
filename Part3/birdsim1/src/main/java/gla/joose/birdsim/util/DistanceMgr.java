package gla.joose.birdsim.util;

import java.util.Arrays;

/**
 * A class for managing distances between pieces on a board.
 */
public class DistanceMgr {
	private Distance distances [];
	
	public DistanceMgr(){
		distances = new Distance[0];
	}
	
	public void addDistance(Distance d){
		Distance td[] = new Distance[distances.length +1];
		for(int i=0;i<distances.length;i++){
			td[i] = distances[i];
		}
		td[distances.length] = d;
		distances = td;
	}
	/**
	 * @return  distance sorted in ascending order of rowdist or coldist
	 */
	public Distance[] getDistances() {
		synchronized(distances){
			Arrays.sort(distances);
			return distances;
        }
		
	}
	
}
