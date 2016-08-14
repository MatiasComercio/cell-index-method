package ar.edu.itba.ss.cellindexmethod.interfaces;


import ar.edu.itba.ss.cellindexmethod.models.Point;

import java.util.Map;
import java.util.Set;

public interface CellIndexMethod {
	
	/**
	 * For each point of the given set, this method gets the ones that are colliding with other points
	 * of the set, considering that a collision is produced when two points are at distance lower or equal than rc,
	 * considering both point's radios.
	 * <p>
	 * Points are supposed to be contained on a square with sides of length L.
	 * <p>
	 * The method will divide that square in cells - with sides of length L/M -, and will use this cells
	 * to apply the algorithm.
	 *
	 * @param points set containing the points for the algorithm
	 * @param L length of the side of the square containing all the points of the set
	 * @param M number of cells on which the side of the square will be divided
	 * @param rc max distance to consider that two points
	 * @param periodicLimit if the end of a limit cell should be consider as it were from the opposite side
	 * @return a map containing as key each of the points of the set, and a list of the points with the ones
	 * each point collides
	 */
	Map<Point, Set<Point>> collision(Set<Point> points, double L, int M, double rc, boolean periodicLimit);
}
