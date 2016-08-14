package ar.edu.itba.ss.cellindexmethod.services;

import ar.edu.itba.ss.cellindexmethod.interfaces.CellIndexMethod;
import ar.edu.itba.ss.cellindexmethod.models.Point;

import java.util.*;


public class CellIndexMethodImpl implements CellIndexMethod {
	
	/**
	 * On the run method, given a cell (row, col), it is necessary to go up, up-right, right and down-right. This is:
	 *    up = row-1, col
	 *    up-right = row-1, col+1
	 *    right = row, col+1
	 *    down-right = row+1, col+1
	 */
	private final static int[][] neighbourDirections = new int[][] {
					{-1, 0}, // up
					{-1, +1}, // up-right
					{0, +1}, // right
					{+1, +1} // down-right
	};
	private static final int ROW = 0;
	private static final int COL = 1;
	
	@Override
	public Map<Point, Set<Point>> run(final Set<Point> points,
	                                        final double L,
	                                        final int M,
	                                        final double rc,
	                                        final boolean periodicLimit) {
		// create the square cell matrix
		final SquareMatrix cellMatrix = new SquareMatrix(M);
		
		final Map<Point, Set<Point>> collisionPerPoint = new HashMap<>(points.size());
		
		if (M <= 0) {
			return collisionPerPoint;
		}
		
		// ********************************************************************
		// ********************************************************************
		// ********************************************************************
		// +++xtodo TODO: check the condition L/M > rc + r1 + r2 for each pair of points.
		// if condition is not met, null should be returned.
		// ********************************************************************
		// ********************************************************************
		// ********************************************************************
		
		points.forEach(point -> {
			// add the point to the map to be returned, with a new empty set
			collisionPerPoint.put(point, new HashSet<>());
			
			// put each point on the corresponding cell of the cell's matrix
			saveToMatrix(L, M, point, cellMatrix);
		});
		
		// run the cell index method itself
		run(cellMatrix, rc, periodicLimit, collisionPerPoint);
		
		// return the created map with each point information
		return collisionPerPoint;
	}
	
	private void run(final SquareMatrix cellMatrix, final double rc,
	                 final boolean periodicLimit, final Map<Point, Set<Point>> collisionPerPoint) {
		/*
			Takes one cell at a time and applies the patter saw in class to take advantage of the symmetry of the
			 method. Let's explain it a little bit.
			
			Given a cell (row, col), it is necessary to go up, up-right, right and down-right. This is:
			* up = row-1, col
			* up-right = row-1, col+1
			* right = row, col+1
			* down-right = row+1, col+1
			 
			Periodic Limit Cases
			
			if periodic limit is false
				if row-1 < 0 || row+1 = M || col+1 = M => do not consider that cell, with M = matrix.dimension()
			
			if periodic limit is true
				if row-1 < 0 => use M-1 //+++xcheck: changed from scratched
				if row+1 = M => use 0
				if col+1 = M => use 0
				
				, with M = matrix.dimension()
				
		 */
		
		final int M = cellMatrix.dimension();
		int oRow, oCol;
		Cell cCell, oCell;
		for (int row = 0 ; row < M ; row ++) {
			for (int col = 0 ; col < M ; col ++) {
				cCell = cellMatrix.get(row, col); // get current cell
				for (final int[] neighbourDirection : neighbourDirections) { // travel getting different neighbours
					// get the other cell's row & col
					oRow = row + neighbourDirection[ROW];
					oCol = col + neighbourDirection[COL];
					
					// adapt to periodicLimit condition
					if (!periodicLimit) {
						if (oRow < 0 || oRow == M || oCol == M) {
							continue; // do not consider this cell, because it does not exists
						}
					} else {
						if (oRow < 0) {
							oRow = M - 1;
						}
						if (oRow == M) {
							oRow = 0;
						}
						if (oCol == M) {
							oCol = 0;
						}
					}
					
					oCell = cellMatrix.get(oRow, oCol);
					
					// check the distance between each pair of points on the current pair of cells,
					// and add the necessary mappings, if two points collide
					checkCollisions(cCell, oCell, rc, collisionPerPoint);
				}
			}
		}
	}
	
	private void checkCollisions(final Cell cCell, final Cell oCell, final double rc,
	                 final Map<Point, Set<Point>> collisionPerPoint) {
		cCell.points.forEach(cPoint -> oCell.points.forEach(oPoint -> {
			if (CellIndexMethods.distanceBetween(cPoint, oPoint) <= rc) { // points are colliding
				// add each one to the set of colliding points of the other
				collisionPerPoint.get(cPoint).add(oPoint);
				collisionPerPoint.get(oPoint).add(cPoint);
			}
		}));
	}
	
	
	private void saveToMatrix(final double mapSideLength, final int nCells,
	                          final Point point, final SquareMatrix cellMatrix) {
		/*
				Each point has an x & y component.
				To get at which cell of the matrix the point belongs, here it is the idea of what's done.
				Consider the case of a column:
				* check which is the number t that makes t*k <= point.x() < (t+1)*k
				* if t is an integer, the column taken is t-1 (unless t = 0), as it would be the case that the point is
						at a cell boundary, and it can be classified in any of those.
				* if t is not an integer, the floor of t is taken as the column number
				
				The same goes for the row.
				
				Notes:
					* For translating points to the correct matrix index, the following formula is used:
							column = ( nCells - 1 ) - t
							
							The problem is "drawn" following, with nCell = 5 for this example
							
								(x,y) plain with t
								indexes as they are got
							
							y
							|
							4
							3
							2
							1
							0 1 2 3 4 --> x
							
								matrix with
								translated t indexes
								0 1 2 3 4
							0
							1
							2
							3
							4
							
							Notice that the second form is the one needed to work with the matrix,
							so as to be able to make a more efficient process
							
					* rows are calculated with point.y() and cols with point.x()
					(see previous graphics for a better understanding).
		 */
		
		final double k = mapSideLength / nCells;
		
		final int row, col;
		
		row = getT(k, point.y());
		col = (nCells - 1) - getT(k, point.x());
		
		// +++xcheck: could row or col be out of bounds?
		cellMatrix.addToCell(row, col, point);
	}
	
	private int getT(final double k, final double v) {
		// +++ xcheck: ArithmeticException could be thrown if the argument of Math.toIntExact overflows an int
		return Math.toIntExact(Math.round(Math.floor(v/k)));
	}
	
	
	private static class Cell {
		private final Set<Point> points;
		
		private Cell() {
			this.points = new HashSet<>();
		}
		
	}
	
	private static class SquareMatrix {
		private final ArrayList<ArrayList<Cell>> matrix;
		
		private SquareMatrix(final int dimension) {
			this.matrix = new ArrayList<>(dimension);
			matrix.forEach(row -> {
				row = new ArrayList<>(dimension);
				row.forEach(cell -> new Cell());
			});
		}
		
		/* +++xcheck: ¿should check if x || y are out of bounds from now on? */
		private Cell get(final int row, final int col) {
			return matrix.get(row).get(col);
		}
		
		private boolean addToCell(final int row, final int col, final Point p) {
			return get(row, col).points.add(p);
		}
		
		private int dimension() {
			return matrix.size();
		}
		
	}
}
