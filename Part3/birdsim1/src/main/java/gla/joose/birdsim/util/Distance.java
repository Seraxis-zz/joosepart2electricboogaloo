package gla.joose.birdsim.util;

import gla.joose.birdsim.pieces.Piece;

/**
 * A class that represents the distance between two pieces.
 */
public class Distance implements Comparable<Distance>{
	private Piece sourcepiece;
	private Piece targetpiece;
	private int rowdist;
	private int coldist;
	
	public Distance(Piece sourcepiece, Piece targetpiece, int rowdist, int coldist){
		this.sourcepiece = sourcepiece;
		this.targetpiece = targetpiece;
		this.coldist = coldist;
		this.rowdist = rowdist;
	}


	public Piece getSourcepiece() {
		return sourcepiece;
	}

	public Piece getTargetpiece() {
		return targetpiece;
	}
		
	public int getRowDist(){
		return rowdist;
	}
	public int getColDist(){
		return coldist;
	}

	public int compareTo(Distance o) {
		if(rowdist<coldist){
			if(o.getRowDist() <o.getColDist()){
				return Math.abs(this.rowdist) - Math.abs(o.getRowDist());
			}
			else{
				return Math.abs(this.rowdist) - Math.abs(o.getColDist());
			}
		}
		else{
			if(o.getRowDist() <o.getColDist()){
				return Math.abs(this.coldist) - Math.abs(o.getRowDist());
			}
			else{
				return Math.abs(this.coldist) - Math.abs(o.getColDist());
			}
		}		
	}	
}