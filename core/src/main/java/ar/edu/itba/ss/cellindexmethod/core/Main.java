package ar.edu.itba.ss.cellindexmethod.core;

import ar.edu.itba.ss.cellindexmethod.models.ParticleType;
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
import java.util.*;
import java.util.stream.Stream;

import static ar.edu.itba.ss.cellindexmethod.core.Main.EXIT_CODE.*;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	private static final String DESTINATION_FOLDER = "data";
	private static final int FIRST_PARTICLE = 1;
	
	
	// Exit Codes
	enum EXIT_CODE {
		OK(0),
		NO_ARGS(-1),
		NO_FILE(-2),
		BAD_N_ARGUMENTS(-3),
		BAD_ARGUMENT(-4);
		
		private final int code;
		
		EXIT_CODE(final int code) {
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}
	}
	
	private static void exit(final EXIT_CODE exitCode) {
		System.exit(exitCode.getCode());
	}
	
	/*
			Options:
				* generate dynamic dat => gen dynamicdat
				* run cell index method => cim data/static.dat data/dynamic.dat M rc periodicLimit
				*                       => cim data/static.dat M rc periodicLimit
				*
				* generate ovito => gen ovito <particle_id>
				
	 */
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("[FAIL] - No arguments passed. Try 'help' for more information.");
			exit(NO_ARGS);
		}
		
		switch (args[0]) {
			case "gen":
				// another arg is needed
				if (args.length < 2) {
					System.out.println("[FAIL] - No file specified. Try 'help' for more information.");
					exit(NO_FILE);
				}
				
				switch (args[1]) {
					case "dynamicDat":
						// read N, L and rs from an input file
						
						// if not given, fail and exit
						
						// create the points position, given the static.dat file
						generateDynamicDatFile();
						break;
					case "ovito":
						// get particle id
						if (args.length != 3) {
							System.out.println("[FAIL] - Bad number of arguments. Try 'help' for more information.");
							exit(BAD_N_ARGUMENTS);
						}
						
						try {
							final int particleId = Integer.parseInt(args[2]);
							generateOvitoFile(particleId);
						} catch (NumberFormatException e) {
							LOGGER.warn("[FAIL] - <particle_id> must be a number. Caused by: ", e);
							System.out.println("[FAIL] - <particle_id> must be a number. Try 'help' for more information.");
							exit(BAD_ARGUMENT);
						}
						break;
					
					default:
						System.out.println("[FAIL] - Invalid argument. Try 'help' for more information.");
						exit(BAD_ARGUMENT);
						break;
				}
			default:
				System.out.println("[FAIL] - Invalid argument. Try 'help' for more information.");
				exit(BAD_ARGUMENT);
				break;
		}
		
		
		
		
		// load points from file
	}
	
	private static void generateDynamicDatFile() {
		// +++xmagicnumber (should be the read ones)
		final int N = 10;
		final int L = 20;
		double[] radios = new double[N];
		Arrays.fill(radios, 2);
		
		final PointFactory pF = PointFactory.getInstance();
		
		final Point leftBottomPoint = Point.builder(0, 0).build();
		final Point rightTopPoint = Point.builder(L, L).build();
		
		final Set<Point> pointsSet = pF.randomPoints(leftBottomPoint, rightTopPoint, radios, false, 10);
		
		// save data to a new file
//		final String destinationFolder = "data";
		final File dataFolder = new File(DESTINATION_FOLDER);
		dataFolder.mkdirs(); // tries to make directories for the .dat files

		/* delete previous dynamic.dat file, if any */
		final Path pathToDatFile = Paths.get(DESTINATION_FOLDER, "dynamic.dat");
		
		if(!deleteIfExists(pathToDatFile)) {
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
	
	/**
	 *  Generate a .XYZ file which contains the following information about a particle:
	 *  - Radius
	 *  - X Position
	 *  - Y Position
	 *  - Particle Type (For more information about how the value is saved, refer to the ParticleType Class):
	 *  	If it is the particleId then it is IMPORTANT
	 *  	If it is a neighbour of the particleId then it is NEIGHBOUR
	 *  	Else, it is UNIMPORTANT
	 *  By default, the output file is 'graphics.xyz' which is stored in the 'data' folder.
	 * @param particleId id of the particle to be assigned a color and in which its neighbours will
	 *                   be represented with another color
	 */
	private static void generateOvitoFile(final int particleId) {
		final Path pathToStaticDatFile = Paths.get(DESTINATION_FOLDER, "static.dat");
		final Path pathToDynamicDatFile = Paths.get(DESTINATION_FOLDER, "dynamic.dat");
		final Path pathToGraphicsFile = Paths.get(DESTINATION_FOLDER, "graphics.xyz");
		
		if(!deleteIfExists(pathToGraphicsFile)) {
			return;
		}
		
		Stream<String> staticDatStream = null;
		Stream<String> dynamicDatStream = null;
		
		try {
			staticDatStream = Files.lines(pathToStaticDatFile);
			dynamicDatStream = Files.lines(pathToDynamicDatFile);
		} catch (IOException e) {
			LOGGER.warn("Could not read file. Caused by: ", e); // +++ximprove
			System.out.println("Could not read  one of these files: 'static.dat' | 'dynamic.dat'.\n" +
							"Check the logs for a detailed info.\n" +
							"Aborting...");
			System.exit(-1); // +++xmagicnumber
		}
		
		BufferedWriter writer = null;
		
		try {
			final String stringN; // N as string
			final int N;
			final Iterator<String> staticDatIterator;
			final Iterator<String> dynamicDatIterator;
			
			writer = new BufferedWriter(new FileWriter(pathToGraphicsFile.toFile()));
			staticDatIterator = staticDatStream.iterator();
			dynamicDatIterator = dynamicDatStream.iterator();
			
			
			final Collection<Integer> neighbours = getNeighbours(particleId);
			
			if(neighbours == null) {
				return;
			}
			
			// Write number of particles
			stringN = staticDatIterator.next();
			writer.write(stringN);
			writer.newLine();
			
			// Write a comment - mandatory for ovito
			writer.write("This is a comment");
			writer.newLine();
			
			// Prepare to feed the lines
			N = Integer.valueOf(stringN);
			staticDatIterator.next(); //Skip L value
			dynamicDatIterator.next(); //Skip t0 value

			/*
				Write particle information in this order
				Particle_Type	Radius	X_Pos	Y_Pos
			*/
			for(int i = FIRST_PARTICLE; i <= N; i++) {
				// Write Particle Type
				if(i == particleId) {
					writer.write(ParticleType.IMPORTANT.toString());
				} else if(neighbours.contains(i)) {
					writer.write(ParticleType.NEIGHBOUR.toString());
				} else {
					writer.write(ParticleType.UNIMPORTANT.toString());
				}
				writer.write("\t");
				
				// Write Radius
				writer.write(staticDatIterator.next() + "\t");
				
				// Write X_Pos and Y_Pos
				writer.write(dynamicDatIterator.next() + "\t");
				
				// End line
				writer.newLine();
			}
		} catch(final IOException e) {
			LOGGER.warn("Could not write to 'graphics.xyz'. Caused by: ", e);
			System.out.println("Could not write to 'graphics.xyz'. Check the logs for a detailed info. Aborting...");
			System.exit(-1);
		} finally {
			try {
				if(writer != null) {
					writer.close();
				}
				staticDatStream.close();
				dynamicDatStream.close();
			} catch (final IOException ignored) {
				
			}
		}
	}
	
	/**
	 * Get the list of id's of neighbours of a specific particle
	 * @param particleId id of the particle from which it will retrieve its neighbours
	 * @return null if the particle does not exist;
	 * 		   else a list of neighbours (which can be empty);
	 */
	private static Collection<Integer> getNeighbours(final int particleId) {
		final Path pathToOutputDatFile = Paths.get(DESTINATION_FOLDER, "output.dat");
		
		try (final Stream<String> outputDatStream = Files.lines(pathToOutputDatFile)) {
			final Iterator outputDatIterator = outputDatStream.iterator();
			final Collection<Integer> neighbours = new LinkedList<>();
			String potentialNeighbours;
			
			while (outputDatIterator.hasNext()) {
				potentialNeighbours = String.valueOf(outputDatIterator.next());
				
				final Scanner intScanner = new Scanner(potentialNeighbours);
				if (intScanner.hasNextInt() && intScanner.nextInt() == particleId) {
					while (intScanner.hasNextInt()) {
						neighbours.add(intScanner.nextInt());
					}
					return neighbours;
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Could not get stream for 'output.dat' file. Caused by: ", e);
			System.exit(-1); // +++xmagicnumber
		}
		return null;
	}
	
	/**
	 * Try to delete a file, whether it exists or not
	 * @param pathToFile the file path that refers to the file that will be deleted
	 * @return true if there were not errors when trying to delete the file;
	 * 		   false in other case;
	 */
	private static boolean deleteIfExists(final Path pathToFile) {
		try {
			Files.deleteIfExists(pathToFile);
		} catch(IOException e) {
			LOGGER.warn("Could not delete previous file: '{}'. Caused by: ", pathToFile, e);
			System.out.println("Could not delete previous file: '" + pathToFile + "'.\n");
			return false;
		}
		return true;
	}
}
