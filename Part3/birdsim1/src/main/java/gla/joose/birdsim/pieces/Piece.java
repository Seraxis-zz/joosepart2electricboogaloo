package gla.joose.birdsim.pieces;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Observable;

import gla.joose.birdsim.boards.Board;


/**
 * An abstract class that represents a piece on a board.
 */
public abstract class Piece extends Observable {
    protected Board board;
    private int row = -1;
    private int column = -1;
    private boolean moving = false;
    private boolean draggable = false;
    private static int draggableCount = 0;
    private boolean selectable = true;
    @SuppressWarnings("unused")
	private static Piece selectedPiece = null;
    private static MouseDragger listener;
    private int x;
    private int y;
    private int speed = -1; // Negative means to use Board default
    private static final int PAUSE_MS = 50;
    private static final int FRAME_RATE = 1000 / PAUSE_MS;

    boolean isSurface;
    /**
     * Creates a Piece. The piece is not actually placed on any
     * board; for that, see the methods
     * <code>board.place(piece, row, column)</code> and
     * <code>piece.place(board, row, column)</code>.
     */
    public Piece() {
    }

    /**
     * @return The board (if any) containing this piece.
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Returns a printable String representing this Piece.
     * 
     * @return A printable representation of this Piece.
     */
    public String toString() {
        return getClass().getName() + "[" + row + "][" + column + "]";
    }

    /**
     * Determines whether the piece can be dragged by the mouse.
     * 
     * @param draggable
     *        Tell whether the piece can be dragged by user mouse
     *        movement.
     */
    public void setDraggable(boolean draggable) {
        if (this.draggable == draggable) return;
        this.draggable = draggable;
        if (board != null) {
            if (draggable) {
                addDragListener();
            } else {
                removeDragListener();
            }
        }
    }

    /**
     * Makes it possible for the user to drag this piece.
     * The instance variable <code>board</code> must be non-null.
     */
    private void addDragListener() {
        draggableCount++;
        if (listener == null) {
            listener = new MouseDragger(board);
            if (draggableCount == 1) {
                board.getJPanel().addMouseListener(listener);
                board.getJPanel().addMouseMotionListener(listener);
            }
        }
    }

    /**
     * Prevents the user from dragging this piece.
     * The instance variable <code>board</code> must be non-null.
     */
    private void removeDragListener() {
        draggableCount--;
        if (draggableCount == 0) {
            board.getJPanel().removeMouseListener(listener);
            board.getJPanel().removeMouseMotionListener(listener);
        }
    }

    /**
     * Returns <code>true</code> if the piece can be dragged by the mouse.
     * 
     * @return <code>true</code> if the piece is draggable.
     */
    public boolean isDraggable() {
        return draggable;
    }

    /**
     * Determines whether the piece can be selected by the mouse.
     * 
     * @param selectable
     *        Set to true if and only if the piece can be selected by clicking
     *        on it.
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    /**
     * Returns <code>true</code> if the piece can be selected by the mouse.
     * 
     * @return <code>true</code> if the piece is selectable.
     */
    public boolean isSelectable() {
        return selectable;
    }

    /**
     * Returns the x-coordinate at which this piece will be painted.
     * 
     * @return The x-coordinate.
     */
    protected int getX() {
        if (moving)
            return x;
        else if(board != null)
        	return board.columnToX(column);
    	
        return 0;
    }

    /**
     * Returns the y-coordinate at which this piece will be painted.
     * 
     * @return The y-coordinate.
     */
    protected int getY() {
        if (moving)
            return y;
        else if(board != null)
        	return board.rowToY(row);
        return 0;
    }

    /**
     * Returns the rectangle in which this piece should be painted.
     * 
     * @return The rectangle in which to paint this piece.
     */
    public Rectangle getRectangle() {
        int x = getX() + 1;
        int y = getY() + 1;
        int width;
        int height;
        if (moving) {
            width = board.getCellWidth() - 1;
            height = board.getCellHeight() - 1;
        } else {
        	if(board == null)
        		return null;
            width = board.columnToX(column + 1) - x;
            height = board.rowToY(row + 1) - y;
        }
        if(isSurface){
        	return new Rectangle(x, y, width*2, height*2);
        }
        return new Rectangle(x, y, width, height);
    }

    /**
     * Returns the row that this piece is in, or -1 if this piece is not on the
     * board.
     * 
     * @return The row number.
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column that this piece is in, or -1 if this piece is not on
     * the board.
     * 
     * @return The column number.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns true if this piece is legally placed on the board.
     * 
     * @return True if this piece is at a legal position.
     */
    protected boolean atLegalPosition() {
    	if(board !=null){
            return board.isLegalPosition(row, column);
    	}
    	else{
    		return false;
    	}
    }

    /**
     * Places this piece at the given row and column on the board.
     * @param board 
     *        The Board on which to place this piece.
     * @param row
     *        The row in which to place this piece.
     * @param column
     *        The column in which to place this piece.
     */
    public void place(Board board, int row, int column) {
        board.place(this, row, column);
    }
    
    /** For internal use only! 
     * 
     * @param board The Board on which to place this piece.
     * @param row The row number.
     * @param column The column number.
     */
    public void placeHelper(Board board, int row, int column) {
        this.board = board;
        this.row = row;
        this.column = column;
        x = board.columnToX(column);
        y = board.rowToY(row);
        if (draggable) {
            addDragListener();
        }
        addObserver(board);
        redraw(getRectangle());
    }

    /**
     * Removes this piece from whatever board it may be on,
     * but does not delete the piece itself.
     */
    public void remove() {
        if (!atLegalPosition())
            return;
        board.remove(this);
    }
    
    /** For internal use only! */
    public void removeHelper() {
        if (draggable) {
            removeDragListener();
        }
        redraw();
        board = null;
        row = column = -1;
    }

    /**
     * @return True if this piece is currently on the board.
     */
    protected boolean isOnBoard() {
        return board != null;
    }

    /**
     * Sets the speed of movement (in squares/second) for this piece. Values of
     * 1 to 10 are reasonable; very large values will cause this piece to
     * "teleport" to the new location; a zero or negative value will cause the
     * default speed (set by the board) to be used.
     * 
     * @param speed
     *        The desired speed of movement, in pixels/redraw.
     */
    public void setSpeed(int speed) {
        this.speed = speed;
        if (speed <= 0)
            speed = board.getSpeed();
    }

    /**
     * Get the speed (in pixels/redraw) at which this piece will move.
     * 
     * @return This Piece's speed of movement, in pixels/redraw.
     */
    public int getSpeed() {
        if (speed <= 0)
            if (board != null) {
                return board.getSpeed();
            } else {
                return 0;
            }
        else
            return speed;
    }

    /**
     * Paints this piece on the board within the given rectangle;
     * must be implemented by a subclass.
     * 
     * @param g The Graphics object on which painting should be done.
     * @param r The rectangle in which to paint this piece.
     */
    public abstract void paint(Graphics g, Rectangle r);
    
    /**
     * Ensures that this piece will be drawn on top of any other pieces
     * in the same array location.
     */
    public void moveToTop() {
        if (board != null) {
            board.moveToTop(this);
        }
    }

    /**
     * Moves this piece the given number of rows and columns.
     * 
     * @param deltaRow
     *        The number of rows down to move this piece; a negative number will
     *        move the piece up.
     * @param deltaColumn
     *        The number of columns to move this piece to the right; a negative
     *        number will move the piece left.
     * @return False if the move would take the piece outside the boundaries of
     *         the board.
     */
    public boolean move(int deltaRow, int deltaColumn) {
        return moveTo(row + deltaRow, column + deltaColumn);
    }

    /**
     * Moves this piece to a new position on the board.
     * 
     * @param newRow
     *        The destination row.
     * @param newColumn
     *        The destination column.
     * @return <code>false</code> if the destination is not a legal board
     *         position, or if the piece is already moving.
     */
    public boolean moveTo(int newRow, int newColumn) {
        if (!board.isLegalPosition(newRow, newColumn))
            return false;
        
        if (moving)
            return false;
        
        int startX = board.columnToX(column);
        int startY = board.rowToY(row);
        int finishX = board.columnToX(newColumn);
        int finishY = board.rowToY(newRow);
        int changeInX = finishX - startX;
        int changeInY = finishY - startY;
        Rectangle oldRect = getRectangle();
        Rectangle newRect = new Rectangle(oldRect);
        
        // compensate for squares being slightly different sizes
        oldRect.width += 2;
        newRect.width += 2;
        oldRect.height += 2;
        newRect.height += 2;

        // Move smoothly towards new position
        moving = true;
        board.moveToTop(this);
        int deltaRow = Math.abs(row - newRow);
        int deltaColumn = Math.abs(column - newColumn);
        int distance = Math.max(deltaRow, deltaColumn)
                - Math.min(deltaRow, deltaColumn) / 2;
        int numberOfSteps = distance * FRAME_RATE / getSpeed();
        
        for (int i = 1; i <= numberOfSteps; i++) {
            oldRect.x = x;
            oldRect.y = y;
            x = startX + (i * changeInX) / numberOfSteps;
            y = startY + (i * changeInY) / numberOfSteps;
            newRect.x = x;
            newRect.y = y;
            redraw(oldRect.union(newRect));
        }
        moving = false;
        if (canMoveTo(newRow, newColumn)) {
            changePosition(newRow, newColumn);
        }
        redraw(oldRect.union(newRect));
        return true;
    }

    /**
     * Determines whether this piece can be moved to the specified
     * location. This method returns <code>true</code> if this
     * piece is on a board and the location is legal for that board.
     * This method can be overridden with more specific tests.
     * 
     * @param newRow The desired row.
     * @param newColumn The desired column.
     * @return <code>true</code> if the move can be made.
     */
    public boolean canMove(int deltaRow, int deltaColumn) {
        return canMoveTo(row + deltaRow, column + deltaColumn);
    }
    
    /**
     * Determines whether this piece can be moved to the specified
     * location.  This method returns <code>true</code> if this
     * piece is on a board and the location is legal for that board.
     * This method can be overridden with more specific tests.
     * 
     * @param newRow The desired row.
     * @param newColumn The desired column.
     * @return <code>true</code> if the move can be made.
     */
    public boolean canMoveTo(int newRow, int newColumn) {
        if (board == null) return false;
        else return board.isLegalPosition(newRow, newColumn);
    }

    /**
     * Change the position of this piece on the board.
     * 
     * @param newRow
     *        The new row for this piece.
     * @param newColumn
     *        The new column for this piece.
     */
    private void changePosition(int newRow, int newColumn) {
    	if(board != null){
    		if (!atLegalPosition())
                return;
            int oldRow = row;
            int oldColumn = column;
            board.changePositionOnBoard(this, oldRow, oldColumn, newRow, newColumn);
            row = newRow;
            column = newColumn;
            x = board.columnToX(column);
            y = board.rowToY(row);
            redraw();
    	}        
    }

    /**
     * Causes this piece to be redrawn.
     */
    public void redraw() {
    	Rectangle rect = getRectangle();
    	if(rect != null)
    		redraw(getRectangle());
    }

    /**
     * Causes the given rectangle to be redrawn.
     */
    public void redraw(Rectangle rect) {
        setChanged();
        notifyObservers(rect);
        try {
            Thread.sleep(PAUSE_MS);
        }
        catch (InterruptedException e) {}
    }

    private class MouseDragger extends MouseAdapter implements
            MouseMotionListener {
        private Piece pieceBeingDragged = null;
        private Board board;

        private MouseDragger(Board board) {
            this.board = board;
        }

        /**
         * When the mouse button is pressed over a draggable piece,
         * begins the dragging process. Only one piece can be dragged
         * at a time.
         * 
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent e) {
            Piece chosenPiece = board.findPiece(e.getX(), e.getY());
            if (chosenPiece == null){
                return;
            }
            if (chosenPiece.isSelectable()) {
                selectedPiece = chosenPiece;
            }
            if (pieceBeingDragged != null) {
                return; // can only drag one piece at a time
            }
            if (!chosenPiece.draggable) {
                return;
            }
            pieceBeingDragged = chosenPiece;
            board = pieceBeingDragged.board;
            pieceBeingDragged.moving = true;
            board.moveToTop(pieceBeingDragged);
        }

        /**
         * Continues dragging a piece.
         * 
         * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
         */
        public void mouseDragged(MouseEvent e) {
            int x;
            int y;
            
            if (pieceBeingDragged == null)
                return;
            // Don't allow drag outside board boundaries
            int maxX = board.columnToX(board.getColumns() - 1);
            int maxY = board.rowToY(board.getRows() - 1);
            x = e.getX() - board.getCellWidth() / 2;
            if (x < 0) {
                x = 0;
            } else {
                if (x > maxX)
                    x = maxX;
            }
            y = e.getY() - board.getCellHeight() / 2;
            if (y < 0) {
                y = 0;
            } else {
                if (y > maxY)
                    y = maxY;
            }
            pieceBeingDragged.x = x;
            pieceBeingDragged.y = y;
            // Track mouse movement
            setChanged();
            notifyObservers();

        }

        /**
         * Terminates the drag process, putting the dragged piece in the
         * nearest square.
         * 
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent e) {
            if (pieceBeingDragged == null) return;
            Rectangle oldRect = pieceBeingDragged.getRectangle();
            Rectangle newRect = oldRect;
            int newRow = board.yToRow(pieceBeingDragged.y
                    + board.getCellHeight() / 2);
            int newColumn = board.xToColumn(pieceBeingDragged.x
                    + board.getCellWidth() / 2);

            if (canMoveTo(newRow, newColumn)) {
                pieceBeingDragged.changePosition(newRow, newColumn);
            }
            pieceBeingDragged.moving = false;
            newRect = pieceBeingDragged.getRectangle();
            pieceBeingDragged = null;
            redraw(enlarge(oldRect.union(newRect)));
        }

        public void mouseMoved(MouseEvent e) {}
    }
    
    /**
     * @param rectangle
     * @return
     */
    private Rectangle enlarge(Rectangle r) {
        r.x -= 2;
        r.y -= 2;
        r.width += 4;
        r.height += 4;
        return r;
    }

    // ------------------------------ Debugging methods

    /**
     * Debugging method to print out the status of this Piece.
     */
    public void dump() {
        System.out.println(" x = " + x + ", y = " + y);
        System.out.println("    draggable = " + draggable + ", selectable = " +
                           selectable + ", moving = " + moving);
    }
}