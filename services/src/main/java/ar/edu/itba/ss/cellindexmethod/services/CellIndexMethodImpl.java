package ar.edu.itba.ss.cellindexmethod.services;

import ar.edu.itba.ss.cellindexmethod.interfaces.CellIndexMethod;
import ar.edu.itba.ss.cellindexmethod.models.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class CellIndexMethodImpl implements CellIndexMethod {
	
	
	
	@Override
	public Map<Point, Set<Point>> collision(final Set<Point> points,
	                                        final double L,
	                                        final int M,
	                                        final double rc,
	                                        final boolean periodicLimit) {
		
		// create the square cell matrix
		final SquareMatrix cellMatrix = new SquareMatrix(M);
		

		
		// put each point on the corresponding cell on the matrix
		saveToMatrix(L, M, points, cellMatrix);
		
		
		
		return null; // +++ xdoing
	}
	
	
	private void saveToMatrix(final double mapSideLength, final int nCells,
	                          final Set<Point> points, final SquareMatrix cellMatrix) {
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
		
		points.forEach(point -> {
			int row, col;
			
			row = getT(k, point.y());
			col = (nCells - 1) - getT(k, point.x());
			
			// +++xcheck: could row or col be out of bounds?
			cellMatrix.addToCell(row, col, point);
		});
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
		public Cell get(final int row, final int col) {
			return matrix.get(row).get(col);
		}
		
		public boolean addToCell(final int row, final int col, final Point p) {
			return get(row, col).points.add(p);
		}
		
	}
}
