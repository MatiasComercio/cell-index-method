package ar.edu.itba.ss.cellindexmethod.services;

import ar.edu.itba.ss.cellindexmethod.models.Point;

import java.util.Map;
import java.util.Set;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public abstract class CellIndexMethods {
	public static double distanceBetween(final Point p1, final Point p2) {
		return sqrt(pow(p2.x() - p1.x(), 2) + pow(p2.y() - p1.y(), 2)) - p1.radio() - p2.radio();
	}
	
	public static boolean checkCollision(final Point pi, final Point pj, final double rc,
	                               final Map<Point, Set<Point>> collisionPerPoint) {
		if (CellIndexMethods.distanceBetween(pi, pj) <= rc) {
			collisionPerPoint.get(pi).add(pj);
			collisionPerPoint.get(pj).add(pi);
			return true;
		}
		return false;
	}
}
