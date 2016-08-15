package ar.edu.itba.ss.cellindexmethod.core;

import ar.edu.itba.ss.cellindexmethod.interfaces.CellIndexMethod;
import ar.edu.itba.ss.cellindexmethod.models.ParticleType;
import ar.edu.itba.ss.cellindexmethod.models.Point;
import ar.edu.itba.ss.cellindexmethod.services.CellIndexMethodImpl;
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
		NO_ARGS(-1),
		NO_FILE(-2),
		BAD_N_ARGUMENTS(-3),
		BAD_ARGUMENT(-4),
		NOT_A_FILE(-5),
		UNEXPECTED_ERROR(-6),
		BAD_FILE_FORMAT(-7);
		
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
				* generate static dat => gen staticdat N L r
				* generate dynamic dat => gen dynamicdat data/static.dat
				* generate ovito => gen ovito <particle_id>
				* run cell index method => cim data/static.dat data/dynamic.dat M rc periodicLimit
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("[FAIL] - No arguments passed. Try 'help' for more information.");
			exit(NO_ARGS);
		}
		
		switch (args[0]) {
			case "gen":
				generateCase(args);
				break;
			case "cim":
				cellIndexMethod(args);
				break;
			default:
				System.out.println("[FAIL] - Invalid argument. Try 'help' for more information.");
				exit(BAD_ARGUMENT);
				break;
		}
		
		System.out.println("[DONE]");
	}
	
	
	private static void cellIndexMethod(final String[] args) {
		if (args.length != 6) {
			System.out.println("[FAIL] - Bad number of arguments. Try 'help' for more information.");
			exit(BAD_N_ARGUMENTS);
		}
		
		// create points' set with static and dynamic files
		final StaticData staticData = loadStaticFile(args[1]);
		
		final Set<Point> points = loadDynamicFile(args[2], staticData);
		
		// parse M, rc, and periodicLimit
		int M = 0;
		try {
			M = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			LOGGER.warn("[FAIL] - <M> must be an integer. Caused by: ", e);
			System.out.println("[FAIL] - <M> argument must be an integer. Try 'help' for more information.");
			exit(BAD_ARGUMENT);
		}
		
		double rc = 0;
		try {
			rc = Double.parseDouble(args[4]);
		} catch (NumberFormatException e) {
			LOGGER.warn("[FAIL] - <rc> must be a number. Caused by: ", e);
			System.out.println("[FAIL] - <rc> argument must be a number. Try 'help' for more information.");
			exit(BAD_ARGUMENT);
		}
		
		boolean periodicLimit = false;
		try {
			periodicLimit = Boolean.parseBoolean(args[5]);
		} catch (NumberFormatException e) {
			LOGGER.warn("[FAIL] - <periodicLimit> must be a boolean. Caused by: ", e);
			System.out.println("[FAIL] - <periodicLimit> argument must be a boolean. Try 'help' for more information.");
			exit(BAD_ARGUMENT);
		}
		
		// run cell index method
		final long startTime = System.nanoTime();
		final CellIndexMethod cim = new CellIndexMethodImpl();
		final Map<Point, Set<Point>> pointsWithNeighbours = cim.run(points, staticData.L, M, rc, periodicLimit);
		final long endTime = System.nanoTime();
		
		final long deltaTime = endTime - startTime;
		
		
		// write pointsWithNeighbours to a file called "output.dat"
		generateOutputDatFile(pointsWithNeighbours, deltaTime);
	}
	
	private static void generateOutputDatFile(final Map<Point, Set<Point>> pointsWithNeighbours, final long deltaTime) {
		
		// save data to a new file
//		final String destinationFolder = "data";
		final File dataFolder = new File(DESTINATION_FOLDER);
		dataFolder.mkdirs(); // tries to make directories for the .dat files

		/* delete previous dynamic.dat file, if any */
		final Path pathToDatFile = Paths.get(DESTINATION_FOLDER, "output.dat");
		
		if(!deleteIfExists(pathToDatFile)) {
			return;
		}

		/* write the new output.dat file */
		final String data = pointsToString(pointsWithNeighbours);
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(pathToDatFile.toFile()));
			writer.write(String.valueOf(deltaTime)); // nano seconds of execution
			writer.write("\n");
			writer.write(data); // list of neighbours per point
			
		} catch (IOException e) {
			LOGGER.warn("An unexpected IO Exception occurred while writing the file {}. Caused by: ", "output.dat", e);
			System.out.println("[FAIL] - An unexpected error occurred while writing the file '" + "output.dat" + "'. \n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(UNEXPECTED_ERROR);
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
	
	private static String pointsToString(final Map<Point, Set<Point>> pointsWithNeighbours) {
		final StringBuilder sb = new StringBuilder();
		pointsWithNeighbours.forEach((point, neighbours) -> {
			sb.append(point.id());
			neighbours.forEach(neighbour -> sb.append('\t').append(neighbour.id()));
			sb.append('\n');
		});
		return sb.toString();
	}
	
	private static void generateCase(final String[] args) {
		// another arg is needed
		if (args.length < 2) {
			System.out.println("[FAIL] - No file specified. Try 'help' for more information.");
			exit(NO_FILE);
		}
		
		switch (args[1]) {
			case "staticdat":
				if (args.length != 5) {
					System.out.println("[FAIL] - Bad number of arguments. Try 'help' for more information.");
					exit(BAD_N_ARGUMENTS);
				}
				
				int N = 0;
				try {
					N = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					LOGGER.warn("[FAIL] - <N> must be a positive integer. Caused by: ", e);
					System.out.println("[FAIL] - <N> must be a positive integer. Try 'help' for more information.");
					exit(BAD_ARGUMENT);
				}
				
				double L = 0;
				try {
					L = Double.parseDouble(args[3]);
				} catch (NumberFormatException e) {
					LOGGER.warn("[FAIL] - <L> must be a number. Caused by: ", e);
					System.out.println("[FAIL] - <L> argument must be a number. Try 'help' for more information.");
					exit(BAD_ARGUMENT);
				}
				
				double r = 0;
				try {
					r = Double.parseDouble(args[4]);
				} catch (NumberFormatException e) {
					LOGGER.warn("[FAIL] - <r> must be a number. Caused by: ", e);
					System.out.println("[FAIL] - <r> argument must be a number. Try 'help' for more information.");
					exit(BAD_ARGUMENT);
				}
				// create the points position, given the static.dat file
				generateStaticDatFile(N, L, r);
				
				break;
			case "dynamicdat":
				if (args.length != 3) {
					System.out.println("[FAIL] - Bad number of arguments. Try 'help' for more information.");
					exit(BAD_N_ARGUMENTS);
				}
				
				// read N, L and rs from an input file
				final StaticData staticData = loadStaticFile(args[2]);
				
				// create the points position, given the static.dat file
				generateDynamicDatFile(staticData);
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
	}
	
	private static void generateStaticDatFile(final int N, final double L, final double r) {
		// save data to a new file
		final File dataFolder = new File(DESTINATION_FOLDER);
		dataFolder.mkdirs(); // tries to make directories for the .dat files

		/* delete previous dynamic.dat file, if any */
		final Path pathToDatFile = Paths.get(DESTINATION_FOLDER, "static.dat");
		
		if(!deleteIfExists(pathToDatFile)) {
			return;
		}
		
		/* write the new static.dat file */
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(pathToDatFile.toFile()));
			writer.write(String.valueOf(N));
			writer.write("\n");
			writer.write(String.valueOf(L));
			writer.write("\n");
			String radio = String.valueOf(r);
			for (int i = 0 ; i < N ; i++) {
				writer.write(radio);
				writer.write("\n");
			}
			
		} catch (IOException e) {
			LOGGER.warn("An unexpected IO Exception occurred while writing the file {}. Caused by: ", "output.dat", e);
			System.out.println("[FAIL] - An unexpected error occurred while writing the file '" + "output.dat" + "'. \n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(UNEXPECTED_ERROR);
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
	
	private static void generateDynamicDatFile(final StaticData staticData) {
		final PointFactory pF = PointFactory.getInstance();
		
		final Point leftBottomPoint = Point.builder(0, 0).build();
		final Point rightTopPoint = Point.builder(staticData.L, staticData.L).build();
		
		final Set<Point> pointsSet = pF.randomPoints(leftBottomPoint, rightTopPoint, staticData.radios, false, 10);
		
		if (pointsSet.size() < staticData.radios.length) {
			System.out.println("[FAIL] - Could not generate all the particles from the static file.\n" +
							"They where crashing each other when trying to create them at different positions.\n" +
							"Check that N is not that big for the given L.\n" +
							"Aborting...");
			exit(UNEXPECTED_ERROR);
		}
		
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
		final String pointsAsFileFormat = pointsToString(pointsSet);
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(pathToDatFile.toFile()));
			writer.write(pointsAsFileFormat);
		} catch (IOException e) {
			LOGGER.warn("An unexpected IO Exception occurred while writing the file {}. Caused by: ", "output.dat", e);
			System.out.println("[FAIL] - An unexpected error occurred while writing the file '" + "dynamic.dat" + "'. \n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(UNEXPECTED_ERROR);
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
	
	private static String pointsToString(final Set<Point> pointsSet) {
		final StringBuffer sb = new StringBuffer();
		sb.append("t0").append('\n');
		pointsSet.forEach(point -> sb.append(point.x()).append('\t').append(point.y()).append('\n'));
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
				// write particle id
				writer.write(String.valueOf(i));
				writer.write("\t");
				
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
			
			// skip time of execution
			if (outputDatIterator.hasNext()) {
				outputDatIterator.next();
			}
			
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
	
	/* ------------------------------- */
	private static class StaticData {
		private int N;
		private double L;
		private double[] radios;
	}
	
	private static StaticData loadStaticFile(final String filePath) {
		final StaticData staticData = new StaticData();
		
		
		final File staticFile = new File(filePath);
		if (!staticFile.isFile()) {
			System.out.println("[FAIL] - File '" + filePath + "' is not a normal file. Aborting...");
			exit(NOT_A_FILE);
		}
		
		try (final Stream<String> staticStream = Files.lines(staticFile.toPath())) {
			final Iterator<String> staticFileLines = staticStream.iterator();
			
			// get N
			staticData.N = Integer.valueOf(staticFileLines.next());
			
			// get L
			staticData.L = Double.valueOf(staticFileLines.next());
			
			staticData.radios = new double[staticData.N];
			String cLine;
			double cRadio;
			for (int i = 0 ; i < staticData.N ; i++) {
				cLine = staticFileLines.next(); // caught runtime exception
				cRadio = Double.valueOf(cLine.split(" ")[0]); // at least it should have one component
				staticData.radios[i] = cRadio;
			}
			
		} catch (final IOException e) {
			LOGGER.warn("An unexpected IO Exception occurred while reading the file {}. Caused by: ", staticFile, e);
			System.out.println("[FAIL] - An unexpected error occurred while reading the file '" + staticFile + "'. \n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(UNEXPECTED_ERROR);
		} catch (final NumberFormatException e) {
			LOGGER.warn("[FAIL] - Number expected. Caused by: ", e);
			System.out.println("[FAIL] - Bad format of file '" + staticFile + "'.\n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(BAD_FILE_FORMAT);
		} catch (final NoSuchElementException e) {
			LOGGER.warn("[FAIL] - Particle Expected. Caused by: ", e);
			System.out.println("[FAIL] - Bad format of file '" + staticFile + "'.\n" +
							"Particle information expected: N is greater than the # of lines with particle information.\n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(BAD_FILE_FORMAT);
		}
		
		return staticData;
	}
	
	private static Set<Point> loadDynamicFile(final String fileName, final StaticData staticData) {
		final File dynamicFile = new File(fileName);
		if (!dynamicFile.isFile()) {
			System.out.println("[FAIL] - File '" + fileName + "' is not a normal file. Aborting...");
			exit(NOT_A_FILE);
		}
		
		
		final Set<Point> points = new HashSet<>(staticData.radios.length);
		
		try (final Stream<String> dynamicStream = Files.lines(dynamicFile.toPath())) {
			final Iterator<String> dynamicFileLines = dynamicStream.iterator();
			
			// skip time t0
			dynamicFileLines.next();
			
			double x, y;
			for (int i = 0 ; i < staticData.radios.length ; i++) {
				final Scanner intScanner = new Scanner(dynamicFileLines.next());
				x = intScanner.nextDouble(); // caught InputMismatchException
				y = intScanner.nextDouble(); // caught InputMismatchException
				points.add(Point.builder(x,y).radio(staticData.radios[i]).build());
			}
			
		} catch (IOException e) {
			LOGGER.warn("An unexpected IO Exception occurred while reading the file {}. Caused by: ", dynamicFile, e);
			System.out.println("[FAIL] - An unexpected error occurred while reading the file '" + dynamicFile + "'. \n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(UNEXPECTED_ERROR);
		} catch (final InputMismatchException e) {
			LOGGER.warn("[FAIL] - Number expected. Caused by: ", e);
			System.out.println("[FAIL] - Bad format of file '" + dynamicFile + "'.\n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(BAD_FILE_FORMAT);
		} catch (final NoSuchElementException e) {
			LOGGER.warn("[FAIL] - Particle Expected. Caused by: ", e);
			System.out.println("[FAIL] - Bad format of file '" + dynamicFile + "'.\n" +
							"Particle information expected: N is greater than the # of lines with particle information.\n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(BAD_FILE_FORMAT);
		} catch (final IndexOutOfBoundsException e) {
			LOGGER.warn("[FAIL] - Particle Information Missing. Caused by: ", e);
			System.out.println("[FAIL] - Bad format of file '" + dynamicFile + "'.\n" +
							"Particle information missing: x or y position is missing.\n" +
							"Check the logs for more info.\n" +
							"Aborting...");
			exit(BAD_FILE_FORMAT);
		}
		
		return points;
	}
}
