package gla.joose.birdsim.boards;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import gla.joose.birdsim.pieces.Bird;
import gla.joose.birdsim.pieces.Grain;
import gla.joose.birdsim.pieces.Piece;


/**
 * A generic board that can be used to display any piece.
 */
public abstract class Board extends Observable implements Observer {
    @SuppressWarnings("rawtypes")
	private Vector[][] board;
    protected Vector<Piece> allPieces = new Vector<Piece>();
    private int[] selectedSquare;
    private int rows;
    private int columns;
    private int defaultSpeed = 5;
    private Board thisBoard;
    private JPanel display;
    protected boolean panelHasBeenResized = false;
    
    protected Random rand;
    protected boolean scareBirds;
    protected boolean starveBirds;
    protected int noofbirds;
    protected int noofgrains;


    /**
     * Creates a board with the given number of rows and columns. This
     * board is a Swing <code>JPanel</code> and may be used wherever a
     * <code>JPanel</code> may be used.
     * 
     * @param rows
     *        Desired number of rows.
     * @param columns
     *        Desired number of columns.
     */
    @SuppressWarnings("rawtypes")
	public Board(int rows, int columns) {
    	rand = new Random();
        display = new DisplayPanel();
        this.rows = rows;
        this.columns = columns;
        thisBoard = this;
        board = new Vector[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                board[i][j] = new Vector(1);
            }
        }
        display.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = yToRow(e.getY());
                int column = xToColumn(e.getX());
                selectedSquare = new int[] { row, column };
                setChanged();
                notifyObservers(selectedSquare);
            }
        });        
        display.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent arg0) {
                panelHasBeenResized  = true;
            }
        });
    }
    
    /**
     * Configures a board with specific set of behaviour;
     * must be implemented by a subclass.
     * 
     * @param frame The JFrame on which the board will be created.
     */
    public abstract void initBoard(JFrame frame);
    
    
    /**
     * Notifies the board frame on changes in the number of birds/grains;
     * must be implemented by a subclass.
     * 
     */
    public abstract void updateStockDisplay();

    
    /**
     * Generic bird behaviour for any concrete board. 
     * This class is overridden when a different board behaviour is preferred
     * 
     */
	public void fly(){
		
		Bird bird = new Bird();
		
		int randRow = rand.nextInt((getRows() - 3) + 1) + 0;
    	int randCol = rand.nextInt((getColumns() - 3) + 1) + 0;
    	
		place(bird,randRow, randCol);
		bird.setDraggable(false);
		bird.setSpeed(20);
		updateStockDisplay();
		
		while(!scareBirds){
			randRow = rand.nextInt((getRows() - 3) + 1) + 0;
        	randCol = rand.nextInt((getColumns() - 3) + 1) + 0; 
        	bird.moveTo(randRow, randCol);
    		bird.setSpeed(20);
			
		} 
		bird.remove();
		updateStockDisplay();
	}
	
	/**
     * updates the number of birds and grains on the board.
     */
	public void updateStock(){
		synchronized(allPieces){
			noofbirds = 0;
			noofgrains = 0;
			for (int i=0;i< getAllPieces().size(); i++) {
                Piece piece = getAllPieces().get(i);
                if(piece instanceof Grain){
                	noofgrains = noofgrains +1;
                }
                else if(piece instanceof Bird){
                	noofbirds = noofbirds +1;
                }
			}
			
		}
	}
		
	
    /**
     * Returns the JPanel on which this board is displayed.
     * 
     * @return The JPanel on which this Board is displayed.
     */
    public JPanel getJPanel() {
        return display;
    }

    /**
     * Returns the number of rows in this Board.
     * 
     * @return The number of rows.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns in this Board.
     * 
     * @return The number of columns.
     */
    public int getColumns() {
        return columns;
    }
    
    public Vector<Piece> getAllPieces(){
    	synchronized(allPieces){
            return allPieces;
        }
    }

    /**
     * Returns the topmost piece at the given row and column in this Board, or
     * null if the given location is empty.
     * 
     * @param row
     *        The row number.
     * @param column
     *        The column number.
     * @return The <code>Piece</code> in the given [row][column], or
     *         <code>null</code> if that location is empty. If the board
     *         location contains more than one piece, the "topmost" piece is
     *         returned.
     * @throws ArrayIndexOutOfBoundsException
     *         If the specified location does not exist.
     */
    public Piece getPiece(int row, int column) {
        if (board[row][column].isEmpty()) {
            return null;
        }
        return (Piece) board[row][column].lastElement();
    }

    /**
     * Returns a (possibly empty) Stack of all the pieces in the given position.
     * The top element of the stack is the topmost element in that board
     * location.
     * 
     * @param row
     *        A row number on this board.
     * @param column
     *        A column number on this board.
     * @return The pieces in this board location.
     * @throws ArrayIndexOutOfBoundsException
     *         If the specified location does not exist.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Stack getPieces(int row, int column) {
        Stack pieces = new Stack();
        for (Iterator iter = board[row][column].iterator(); iter.hasNext();) {
            pieces.push(iter.next());
        }
        return pieces;
    }
    
    /**
     * Returns <code>true</code> if the given row and column on this board
     * contains no Pieces.
     * 
     * @param row The row to examine.
     * @param column The column to examine.
     * @return <code>true</code> if this location is empty.
     */
    public boolean isEmpty(int row, int column) {
        return board[row][column].isEmpty();
    }

    /**
     * Given x-y coordinates, finds and returns the topmost piece at that
     * location on this board, or null if there is no such piece.
     * 
     * @param x
     *        The local x coordinate.
     * @param y
     *        The local y coordinate.
     * @return The <code>Piece</code> in the [row][column] containing the
     *         given (x, y) coordinates, or <code>null</code> if that location
     *         is empty. If the board location contains more than one piece, the
     *         "topmost" piece is returned.
     * @throws ArrayIndexOutOfBoundsException
     *         If the specified location does not exist.
     */
    public Piece findPiece(int x, int y) {
        return getPiece(yToRow(y), xToColumn(x));
    }

    /**
     * Given an x coordinate, determines which column it is in.
     * 
     * @param x
     *        A local x coordinate.
     * @return The number of the column containing the given x coordinate.
     */
    public int xToColumn(int x) {
        return Math.min(columns - 1, (x * columns) / display.getWidth());
    }

    /**
     * Given a y coordinate, determines which row it is in.
     * 
     * @param y
     *        A local y coordinate.
     * @return The number of the row containing the given y coordinate.
     */
    public int yToRow(int y) {
        return Math.min(rows - 1, (y * rows) / display.getHeight());
    }

    /**
     * Returns the X coordinate of the left side of cells in the given column of
     * this Board.
     * 
     * @param columnNumber
     *        A column number.
     * @return The X coordinate of the left side of that column.
     */
    public int columnToX(int columnNumber) {
        return (columnNumber * (display.getWidth() - 1)) / columns;
    }

    /**
     * Returns the Y coordinate of the top side of cells in the given column of
     * this Board.
     * 
     * @param rowNumber
     *        A row number.
     * @return The Y coordinate of the top side of that row.
     *      */
    public int rowToY(int rowNumber) {
        return (rowNumber * (display.getHeight() - 1)) / rows;
    }

    /**
     * Places the given piece at the given location in this board.
     * It is possible to place more than one piece in a given board
     * location, in which case later pieces go "on top of" earlier
     * pieces.
     * 
     * @param piece
     *        The <code>Piece</code> to be placed.
     * @param row
     *        The row in which to place the piece.
     * @param column
     *        The column in which to place the piece.
     * @throws ArrayIndexOutOfBoundsException
     *         If the specified location does not exist.
     */
    @SuppressWarnings("unchecked")
	public void place(Piece piece, int row, int column) {
        if (piece.getBoard() != null) {
            throw new IllegalArgumentException("Piece " + piece + " is already on a board");
        }
        board[row][column].add(piece);
        synchronized (allPieces) {
            allPieces.add(piece);
        }
        piece.placeHelper(this, row, column);
    }
    
    /**
     * Removes all Pieces from this Board.
     */
    public void clear() {
    	synchronized (allPieces) {
    		for (int i = allPieces.size() - 1; i >= 0; i--) {
                remove (allPieces.get(i));
            }
		}
    }

    /**
     * Removes the top piece at the given row and column on this Board.
     * 
     * @param row
     *        The row of the piece to be removed.
     * @param column
     *        The column of the piece to be removed.
     * @throws ArrayIndexOutOfBoundsException
     *         If the specified location does not exist.
     */
    public Piece remove(int row, int column) {
        Piece piece = getPiece(row, column);
        if (piece == null) {
            return null;
        } else {
            remove(piece);
            return piece;
        }
    }
    
    @SuppressWarnings("unchecked")
	public void changePositionOnBoard(Piece piece,
                                         int oldRow, int oldColumn,
                                         int newRow, int newColumn) {
        board[oldRow][oldColumn].remove(piece);
        board[newRow][newColumn].add(piece);
    }

    /**
     * Removes this piece from the board. Does nothing if the piece
     * is not, in fact, on the board.
     * 
     * @param piece
     *        The piece to remove.
     * @param row
     *        The row containing the piece.
     * @param column
     *        The column containing the piece.
     */
    public boolean remove(Piece piece) {
        if (piece == null || piece.getBoard() != this) {
            return false;
        }
        board[piece.getRow()][piece.getColumn()].remove(piece);
        synchronized (allPieces) {
            allPieces.remove(piece);
        }
        piece.removeHelper();
        return true;
    }

    /**
     * Ensures that the given piece will be drawn on top of any other pieces
     * in the same array location.
     * 
     * @param piece
     *        The piece to promote to the top.
     */
    public void moveToTop(Piece piece) {
        synchronized (allPieces) {
            allPieces.remove(piece);
            allPieces.add(piece);
        }
    }

    /**
     * Sets the default speed of movement for pieces on this board, in squares
     * per second. This value is used only for pieces that do not specify their
     * own speed.
     * 
     * @param speed
     *        The default speed for pieces on this board.
     */
    public void setSpeed(int speed) {
        if (speed > 0)
            defaultSpeed = speed;
    }

    /**
     * Returns the default speed (in squares per second) of pieces on this
     * board.
     * 
     * @return The default speed for pieces on this board.
     */
    public int getSpeed() {
        return defaultSpeed;
    }

    /**
     * Returns the current width, in pixels, of a single cell on this Board. The
     * value will change if this Board is resized.
     */
    public int getCellWidth() {
        return display.getWidth() / columns;
    }

    /**
     * @return Returns the current height, in pixels, of a single cell on this
     *         Board. The value will change if this Board is resized.
     */
    public int getCellHeight() {
        return display.getHeight() / rows;
    }

    /**
     * Determines whether the given row and column denote a legal position on
     * this Board.
     * 
     * @param row
     *        The given row number.
     * @param column
     *        The given column number.
     * @return <code>true</code> if the given row and column number represent
     *         a valid location on this board
     */
    public boolean isLegalPosition(int row, int column) {
        if (row < 0 || row >= rows)
            return false;
        if (column < 0 || column >= columns)
            return false;
        return true;
    }
    
    /**
     * Redraws this Board whenever a Piece is modified.
     * This method should <b>not</b> be overridden.
     * 
     * @param piece
     *        The piece that needs to be redrawn.
     * @param nothing
     *        Not used.
     */
    @SuppressWarnings("unused")
	public void update(Observable changedPiece, Object rectangle) {
        Piece piece = (Piece)changedPiece;
        if (rectangle == null) {
            display.repaint();
        } else {
            Rectangle r = (Rectangle)rectangle;
            display.repaint(r.x, r.y, r.width, r.height);
        }
    }
    
    /**
     * Paints th1s board itself, not including the pieces.
     * 
     * @param g
     *        The Graphics context on which this board is painted.
     */
    public void paint(Graphics g) {
        int height = display.getHeight();
        int width = display.getWidth();
        int x, y;
        Color oldColor = g.getColor();
        Color backgroundColor = Color.white;
        Color lineColor = new Color(192, 192, 255);

        // Fill background with solid color
        g.setColor(backgroundColor);
        g.fillRect(0, 0, display.getWidth(), display.getHeight());

        // Paint vertical lines
        g.setColor(lineColor);
        for (int i = 0; i <= columns; i++) {
            x = columnToX(i);
            g.drawLine(x, 0, x, height);
        }
        // Paint horizontal lines
        for (int i = 0; i <= rows; i++) {
            y = rowToY(i);
            g.drawLine(0, y, width, y);
        }
        g.setColor(oldColor);
    }


    /**
     * Displays the board contents (for debugging).
     */
    @SuppressWarnings("rawtypes")
	protected void dump() {
        System.out.println("----------- Board is " + rows + " rows, "
                           + columns + " columns.");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (!board[i][j].isEmpty()) {
                    System.out.println("Board [" + i + "][" + j + "] contains:");
                    for (Iterator iter = board[i][j].iterator(); iter.hasNext();) {
                        Piece piece = (Piece) iter.next();
                        System.out.println("    " + piece.toString());
                    }
                }
            }
        }
        synchronized (allPieces) {
            System.out.println("Vector allPieces:");
            for (Iterator<Piece> iter = allPieces.iterator(); iter.hasNext();) {
                Piece piece = iter.next();
                System.out.println("    " + piece.toString());
            }
//            System.out.println("Selected piece = " + selectedPiece);
            System.out.println("----------- Pieces: ");
            for (Iterator<Piece> iter = allPieces.iterator(); iter.hasNext();) {
                Piece piece = iter.next();
                System.out.print(piece.toString());
                piece.dump();
            }
        }
    }
    
//  -------------------------------------------------- inner class DisplayPanel
    
    class DisplayPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		/**
         * Repaints this Board and everything on it.
         * 
         * @param g
         *        The Graphics context on which this board is painted.
         */
        public void update(Graphics g) {
            paint(g);
        }

        /**
         * Repaints this Board and everything on it.
         * 
         * @param g
         *        The Graphics context on which this board is painted.
         */
        public void paint(Graphics g) {
            // Paint the board
            thisBoard.paint(g);
            // Paint the pieces
            synchronized (allPieces) {
                for (Iterator<Piece> iter = allPieces.iterator(); iter.hasNext();) {
                    Piece piece = iter.next();
                    piece.paint(g, piece.getRectangle());
                }
            }
        }
    } // end inner class DisplayPanel

    /**
     * @return
     */
    public int[] getSelectedSquare() {
        return selectedSquare;
    }
    
    public void setSelectedSquare(int[] selection) {
        selectedSquare = selection;
    }
}