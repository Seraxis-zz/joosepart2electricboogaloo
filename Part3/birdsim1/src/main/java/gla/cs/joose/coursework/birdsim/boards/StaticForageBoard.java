package gla.joose.birdsim.boards;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gla.joose.birdsim.pieces.Bird;
import gla.joose.birdsim.pieces.Grain;
import gla.joose.birdsim.pieces.Piece;
import gla.joose.birdsim.util.Distance;
import gla.joose.birdsim.util.DistanceMgr;

/**
 * A BirdSim board with where birds simultaneously fly and perch on  static grains.
 */
public class StaticForageBoard extends Board{

    JPanel buttonPanel;
    JButton hatchEggButton;
    JButton feedBirdButton;
    JButton scareBirdsButton;
    boolean scareBirds;
    JButton starveBirdsButton;
    boolean starveBirdspressed;
    
    JLabel noOfGrainsLabel;
    JLabel noOfBirdsLabel;
    
    Thread runningthread;
        
	public StaticForageBoard(int rows, int columns) {
		super(rows, columns);		
	}

	@Override
	public void initBoard(final JFrame frame) {
		JPanel display = getJPanel();
        frame.getContentPane().add(display, BorderLayout.CENTER);
        
        // Install button panel
        buttonPanel = new JPanel();
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        hatchEggButton = new JButton("hatch egg");
        buttonPanel.add(hatchEggButton);
        hatchEggButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	scareBirds = false;
            	runningthread = new Thread(new Runnable(){
					public void run() {
						fly();
					}            		
            	});
            	runningthread.start();
        }}); 
        
        feedBirdButton = new JButton("feed birds");
        buttonPanel.add(feedBirdButton);
        feedBirdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	starveBirdspressed = false;

            	Grain grain = new Grain();
            	int randRow = rand.nextInt((getRows() - 3) + 1) + 0;
            	int randCol = rand.nextInt((getColumns() - 3) + 1) + 0;
            	place(grain,randRow, randCol);
        		grain.setDraggable(false);
        		
        		updateStockDisplay();
        }}); 

        starveBirdsButton = new JButton("starve birds");
        buttonPanel.add(starveBirdsButton);
        starveBirdsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	starveBirdspressed = true;

        }}); 
        
        scareBirdsButton = new JButton("scare birds");
        buttonPanel.add(scareBirdsButton);
        scareBirdsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	scareBirds = true;

        }}); 
        noOfBirdsLabel = new JLabel();
        noOfBirdsLabel.setText("#birds: "+0);
        buttonPanel.add(noOfBirdsLabel);

        noOfGrainsLabel = new JLabel();
        noOfGrainsLabel.setText("#grains: "+0);
        buttonPanel.add(noOfGrainsLabel);

        // Implement window close box
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	scareBirds = true;            	
            	if(runningthread != null){
                    clear();
                    try {
						runningthread.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
            	}
            	frame.dispose();
                System.exit(0);
        }});
     
        frame.pack();
        frame.setSize(650, 650);
        frame.setVisible(true);
        		
	}
	
	
	@Override
	public void updateStockDisplay(){
		updateStock();
		noOfBirdsLabel.setText("#birds: "+noofbirds);
		noOfGrainsLabel.setText("#grains: "+noofgrains);
	}
		
	@Override
	public void fly(){
		
		Bird bird = new Bird();
		
		int randRow = rand.nextInt((getRows() - 3) + 1) + 0;
    	int randCol = rand.nextInt((getColumns() - 3) + 1) + 0;    	
		place(bird,randRow, randCol);
		bird.setDraggable(false);
		bird.setSpeed(20);
		updateStockDisplay();
		
		while(!scareBirds){
			
			DistanceMgr dmgr = new DistanceMgr();
			int current_row = bird.getRow();
			int current_col = bird.getColumn();
			
			synchronized(allPieces){
				for (int i=0;i< getAllPieces().size(); i++) {
	                Piece piece = getAllPieces().get(i);
	                if(piece instanceof Grain){
	                	
	                int dist_from_food_row = current_row - piece.getRow();
		                	int dist_from_food_col = piece.getColumn() - current_col;
		                	Distance d = null;
		                	if(dist_from_food_row <= dist_from_food_col){
		                		d = new Distance(bird, (Grain)piece, dist_from_food_row, dist_from_food_col);
		                	}
		                	else{
		               		 	d = new Distance(bird, (Grain)piece, dist_from_food_row, dist_from_food_col);
		                	}                    	
		                	dmgr.addDistance(d);		
	                	
	                	
	                }
				}	       
			}
			////
			
			Distance distances[] = dmgr.getDistances();
			boolean movedone = false;

			for(int i =0; i< distances.length;i++){
				Distance d = distances[i];
				
				if(d.getRowDist() <= d.getColDist()){
					
					if(d.getRowDist() >0){
						boolean can_move_down= bird.canMoveTo(current_row-1, current_col);
			    		if(can_move_down){
							bird.moveTo(current_row-1, current_col);
							movedone = true;
							break;
						}
					}
					else if(d.getRowDist() < 0){
						boolean can_move_down= bird.canMoveTo(current_row+1, current_col);
			    		if(can_move_down){
							bird.moveTo(current_row+1, current_col);
							movedone = true;
							break;
						}
					}
					else if(d.getRowDist() ==0){
						if(d.getColDist() >0){
							boolean can_move_right = bird.canMoveTo(current_row, current_col+1);
							if(can_move_right){
								bird.moveTo(current_row, current_col+1);
								movedone = true;
								break;
							}
						}
						else if(d.getColDist()< 0){
							boolean can_move_left = bird.canMoveTo(current_row, current_col-1);
							if(can_move_left){
								bird.moveTo(current_row, current_col-1);
								movedone = true;
								break;
							}
						}
						else if(d.getColDist() ==0){
							//bingo -food found (eat and move away)
							Grain grain = (Grain)d.getTargetpiece();
							grain.deplete();

							if(starveBirdspressed){
		                		grain.remove();
		                		updateStockDisplay();
		                	}
							else if(grain.getRemaining() <=0){
			        			grain.remove();	
			        			updateStockDisplay();
			        		} 
			        		int randRow1 = rand.nextInt((getRows() - 3) + 1) + 0;
			            	int randCol2 = rand.nextInt((getColumns() - 3) + 1) + 0; 
			            	bird.moveTo(randRow1, randCol2);
			        		bird.setSpeed(20);
							movedone = true;
							break;


						}
						
					}
				}
				///////
				else if(d.getRowDist() > d.getColDist()){
	            	
					if(d.getColDist() >0){
						boolean can_move_right = bird.canMoveTo(current_row, current_col+1);
						if(can_move_right){
							bird.moveTo(current_row, current_col+1);
							movedone = true;
							break;
						}
					}
					else if(d.getColDist()<0){
						boolean can_move_left = bird.canMoveTo(current_row, current_col-1);
						if(can_move_left){
							bird.moveTo(current_row, current_col-1);
							movedone = true;
							break;
						}
					}
					else if(d.getColDist() == 0){
						if(d.getRowDist() >0){
							boolean can_move_up= bird.canMoveTo(current_row-1, current_col);
				    		if(can_move_up){
								bird.moveTo(current_row-1, current_col);
								movedone = true;
								break;
							}
							
						}
						else if(d.getRowDist() < 0){
							boolean can_move_down = bird.canMoveTo(current_row+1, current_col);
				    		if(can_move_down){
								bird.moveTo(current_row+1, current_col);
								movedone = true;
								break;
							} 
						}
						else if(d.getRowDist() ==0){
							//bingo -food found (eat and move away)
							Grain grain = (Grain)d.getTargetpiece();
							grain.deplete();
							
							if(starveBirdspressed){
		                		grain.remove();
		                		updateStockDisplay();
		                	}
							else if(grain.getRemaining() <=0){
			        			grain.remove();	
			        			updateStockDisplay();
			        		} 
			        		int randRow1 = rand.nextInt((getRows() - 3) + 1) + 0;
			            	int randCol2 = rand.nextInt((getColumns() - 3) + 1) + 0; 
			            	bird.moveTo(randRow1, randCol2);	
			        		bird.setSpeed(20);
							movedone = true;
							break;

						}
					}
				}
			}
			if(!movedone){
				int randRow1 = rand.nextInt((getRows() - 3) + 1) + 0;
            	int randCol2 = rand.nextInt((getColumns() - 3) + 1) + 0; 
            	bird.moveTo(randRow1, randCol2);
			}
			
		}
		bird.remove();
		updateStockDisplay();
       
	}

}
