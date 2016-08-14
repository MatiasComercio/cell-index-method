package ar.edu.itba.ss.cellindexmethod.services;

import ar.edu.itba.ss.cellindexmethod.interfaces.BruteForceMethod;
import ar.edu.itba.ss.cellindexmethod.models.Point;

import java.util.*;

public class BruteForceMethodImpl implements BruteForceMethod{

    //TODO: Check if points in the given set are not colliding (distance <0) && check rc>=0
    // && check given set != null
    @Override
    public Map<Point, Set<Point>> run(Set<Point> points, double rc, boolean periodicLimit) {

        final List<Point> pointsAsList = new ArrayList<>(points);
        final Map<Point, Set<Point>> collisionPerPoint = new HashMap<>(points.size());

        points.forEach(point -> {
            // add the point to the map to be returned, with a new empty set
            collisionPerPoint.put(point, new HashSet<>());
        });

        if(!periodicLimit){
            calculateCollisions(collisionPerPoint, pointsAsList, rc);
        }

        return collisionPerPoint;
    }

    private void calculateCollisions(Map<Point, Set<Point>> collisionPerPoint,
                                     List<Point> pointsAsList, double rc) {
        double distance;

        for(int i=0; i<pointsAsList.size(); i++){
            for(int j=i+1; j<pointsAsList.size(); j++){
                distance = CellIndexMethods.distanceBetween(pointsAsList.get(i),
                        pointsAsList.get(j));
                if(distance <= rc){
                    collisionPerPoint.get(pointsAsList.get(i)).add(pointsAsList.get(j));
                    collisionPerPoint.get(pointsAsList.get(j)).add(pointsAsList.get(i));
                }
            }
        }
    }
}
