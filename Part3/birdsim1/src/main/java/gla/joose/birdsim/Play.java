package gla.joose.birdsim;

import javax.swing.JFrame;

import gla.joose.birdsim.boards.Board;
import gla.joose.birdsim.boards.FlockBoard;
import gla.joose.birdsim.boards.MovingForageBoard;
import gla.joose.birdsim.boards.StaticForageBoard;


/**
 *  @author inah
 *  The main method for bootstrapping BirdSim.
 */
public class Play extends JFrame {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		Play play = new Play();
		
//		Board forageBoard = new StaticForageBoard(50,50);
//		forageBoard.initBoard(play);
//		Board forageBoard = new MovingForageBoard(50,50);
//		forageBoard.initBoard(play);
		Board simpleBoard = new FlockBoard(50,50);
		simpleBoard.initBoard(play);

	}

}
