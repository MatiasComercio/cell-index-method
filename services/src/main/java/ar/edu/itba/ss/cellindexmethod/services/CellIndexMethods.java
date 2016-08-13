package ar.edu.itba.ss.cellindexmethod.services;

import ar.edu.itba.ss.cellindexmethod.models.Point;

import static java.lang.Math.*;

public abstract class CellIndexMethods {
	public static double distanceBetween(final Point p1, final Point p2) {
		return sqrt(pow(p2.x() - p1.x(), 2) + pow(p2.y() - p1.y(), 2)) - p1.radio() - p2.radio();
	}
}
