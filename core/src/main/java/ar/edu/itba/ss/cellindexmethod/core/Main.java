package ar.edu.itba.ss.cellindexmethod.core;

import ar.edu.itba.ss.cellindexmethod.models.Point;
import ar.edu.itba.ss.cellindexmethod.services.PointFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		
		// read N, L and rs from an input file
		
		// if  not given, fail and exit
		
		// create the points
		generateDynamicDatFile();
		
		// load points from file
		
		
	}
	
	private static void generateDynamicDatFile() {
		// +++xmagicnumber (should be the read ones)
		final int N = 10;
		final int L = 20;
		double[] radios = new double[N];
		Arrays.fill(radios, 2);
		
		final PointFactory pF = PointFactory.getInstance();
		
		final Point leftBottomPoint = Point.builder(0,0).build();
		final Point rightTopPoint = Point.builder(L,L).build();
		
		final Set<Point> pointsSet = pF.randomPoints(leftBottomPoint, rightTopPoint, radios, false, 10);
		
		// save data to a new file
		final String destinationFolder = "data";
		final File dataFolder = new File(destinationFolder);
		dataFolder.mkdirs(); // tries to make directories for the .dat files

		/* delete previous dynamic.dat file, if any */
		final Path pathToDatFile = Paths.get(destinationFolder, "dynamic.dat");
		try {
			Files.deleteIfExists(pathToDatFile);
		} catch (IOException e) {
			LOGGER.warn("Could not delete previous dynamic.dat file: '{}'. Caused by: ", pathToDatFile, e);
			System.out.println("Could not delete previous dynamic.dat file: '" + pathToDatFile + "'.\n" +
							"New dynamic.dat could not be saved. Aborting...");
			return;
		}

		/* write the new dynamic.dat file */
		final String pointsAsFileFormat = pointsSetToString(N, L, pointsSet);
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(pathToDatFile.toFile()));
			writer.write(pointsAsFileFormat);
		} catch (IOException e) {
			LOGGER.warn("Could not write 'dynamic.dat' file. Caused by: ", e);
			System.out.println("An unknown error occurred while writing 'dynamic.dat' file. Aborting...");
			System.exit(-1); // +++xmagicnumber
		} finally {
			try {
				// close the writer regardless of what happens...
				if (writer != null) {
					writer.close();
				}
			} catch (Exception ignored) {
				
			}
		}
	}
	
	private static String pointsSetToString(final int N, final int L, final Set<Point> pointsSet) {
		final StringBuffer sb = new StringBuffer();
		sb.append("t0").append('\n');
		pointsSet.forEach(point -> sb.append(point.x()).append(' ').append(point.y()).append('\n'));
		return sb.toString();
	}
}
