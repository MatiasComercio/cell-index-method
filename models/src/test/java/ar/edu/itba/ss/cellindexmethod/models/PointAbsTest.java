package ar.edu.itba.ss.cellindexmethod.models;

import org.junit.Test;

public class PointAbsTest {
	
	
	
	@Test
	public void idTest() {
		final Point p1 = Point.builder(0,0).build();
		assert 0 == p1.id();
		assert 0 == p1.id();
		
		
		final Point p2 = Point.builder(0,0).build();
		assert 1 == p2.id();
		assert 1 == p2.id();
	}
	
}
