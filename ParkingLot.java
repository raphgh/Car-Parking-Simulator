import java.io.File;
import java.util.Scanner;

/**
 * @author Mehrdad Sabetzadeh, University of Ottawa
 */
public class ParkingLot {
	/**
	 * The delimiter that separates values
	 */
	private static final String SEPARATOR = ",";

	/**
	 * The delimiter that separates the parking lot design section from the parked
	 * car data section
	 */
	private static final String SECTIONER = "###";

	/**
	 * Instance variable for storing the number of rows in a parking lot
	 */
	private int numRows;

	/**
	 * Instance variable for storing the number of spaces per row in a parking lot
	 */
	private int numSpotsPerRow;

	/**
	 * Instance variable (two-dimensional array) for storing the lot design
	 */
	private CarType[][] lotDesign;

	/**
	 * Instance variable (two-dimensional array) for storing occupancy information
	 * for the spots in the lot
	 */
	private Car[][] occupancy;

	/**
	 * Constructs a parking lot by loading a file
	 * 
	 * @param strFilename is the name of the file
	 */
	public ParkingLot(String strFilename) throws Exception {

		if (strFilename == null) {
			System.out.println("File name cannot be null.");
			return;
		}


		calculateLotDimensions(strFilename);

		lotDesign = new CarType[numRows][numSpotsPerRow];
		occupancy = new Car[numRows][numSpotsPerRow];

		populateFromFile(strFilename);
	}

	/**
	 * Parks a car (c) at a give location (i, j) within the parking lot.
	 * 
	 * @param i is the parking row index
	 * @param j is the index of the spot within row i
	 * @param c is the car to be parked
	 */
	public void park(int i, int j, Car c) throws Exception{
		if(!canParkAt(i,j,c)){ // if out of bounds
			throw new Exception("Car " + c + "cannot be parked at (" + i + "," + j +")");
		}
		if(occupancy[i][j] != null){ // if alr occupied
			throw new Exception("Car " + c + "cannot be parked at (" + i + "," + j +")");
		}

		occupancy[i][j] = c;

	}


	/**
	 * Removes the car parked at a given location (i, j) in the parking lot
	 * 
	 * @param i is the parking row index
	 * @param j is the index of the spot within row i
	 * @return the car removed; the method returns null when either i or j are out
	 *         of range, or when there is no car parked at (i, j)
	 */
	public Car remove(int i, int j) { 
		
		if (i >= numRows || j >= numSpotsPerRow || i < 0 || j < 0 || occupancy[i][j] == null){ // out-of-bounds spot OR already empty
			return null;
		}
		else{ //if there's a car in the spot
			
			Car removedCar = occupancy[i][j];

			occupancy[i][j] = null;

			return removedCar;
		}
	}

	/**
	 * Checks whether a car (which has a certain type) is allowed to park at
	 * location (i, j)
	 * 
	 * @param i is the parking row index
	 * @param j is the index of the spot within row i
	 * @return true if car c can park at (i, j) and false otherwise
	 */
    public boolean canParkAt(int i, int j, Car c) {
        // Check bounds and if the spot is already occupied
        if (i < 0 || i >= numRows || j < 0 || j >= numSpotsPerRow || occupancy[i][j] != null) {
            return false;
        }
    
        CarType spotType = lotDesign[i][j];
        CarType carType = c.getType();
    
        // Assuming ELECTRIC cars can park in any spot except NA
        if (spotType == CarType.NA){

            return false;
        } 
        
        else if (carType == CarType.ELECTRIC){

            return true; 
        } 
        
        else if (spotType == CarType.ELECTRIC){

            return carType == CarType.ELECTRIC; 
        } 

        else if (spotType == CarType.SMALL){

            return carType == CarType.SMALL || carType == CarType.ELECTRIC; 
        } 
        
        else if (spotType == CarType.REGULAR){
        
            return carType == CarType.SMALL || carType == CarType.ELECTRIC || carType == CarType.REGULAR;
        } 
        
        else if (spotType == CarType.LARGE) {

            return true;
        }

        return false;
	}
	/**
	 * @return the total capacity of the parking lot excluding spots that cannot be
	 *         used for parking (i.e., excluding spots that point to CarType.NA)
	 */
	public int getTotalCapacity() {
		int capacity = 0;

		for( int i = 0; i<lotDesign.length; i++){

			for(int j=0; j<lotDesign[i].length;j++){

				if(lotDesign[i][j] != CarType.NA){ 

					capacity++;
				}
			}
		}
		return capacity;

	}

	/**
	 * @return the total occupancy of the parking lot (i.e., the total number of
	 *         cars parked in the lot)
	 */
	public int getTotalOccupancy() {
		int occupied = 0;

		for( int i = 0; i<lotDesign.length; i++){

			for(int j=0; j<lotDesign[i].length;j++){

				if(occupancy[i][j] != null){ 

					occupied++;
				}
			}
		}
		return occupied; // REMOVE THIS STATEMENT AFTER IMPLEMENTING THIS METHOD		
	}

	private void calculateLotDimensions(String strFilename) throws Exception {
		Scanner scanner = new Scanner(new File(strFilename));
		
		numRows = 0;
		numSpotsPerRow = 0;

		

		while (scanner.hasNext()) {
			String str = scanner.nextLine().trim();
			if(str.equals("###")){
				break;
			}

			if(!(str.isEmpty())){
				numRows++;
				if(numSpotsPerRow == 0){
					numSpotsPerRow = str.split(",", -1).length;
				}
			}

		}

		scanner.close();
	}

    private void populateFromFile(String strFilename) throws Exception {
        Scanner scanner = new Scanner(new File(strFilename));
        boolean check = false;
        int r = 0; // Use for indexing rows in lotDesign
    
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
    
            if (line.isEmpty()) continue; // Skip empty lines to avoid interpreting them as valid design data
    
            if (line.equals("###")) {
                check = true;
                continue; // Once SECTIONER is encountered, switch to processing occupancy data
            }
    
            if (!check) {
                // Parsing lot design section
                String[] parkingSpots = line.split(",");
                for (int j = 0; j < parkingSpots.length && j < numSpotsPerRow; j++) {
                    lotDesign[r][j] = Util.getCarTypeByLabel(parkingSpots[j].trim());
                }
                r++;
            } else {
                if (!line.isEmpty()) {
                    String[] carInfo = line.split(",", -1);
                    if (carInfo.length == 4) {
                    	
                        int row = Integer.parseInt(carInfo[0].trim());
                        int column = Integer.parseInt(carInfo[1].trim());

                        CarType type = Util.getCarTypeByLabel(carInfo[2].trim());
                        String plateNum = carInfo[3].trim();

                        if (canParkAt(row, column, new Car(type, plateNum))) {

                            occupancy[row][column] = new Car(type, plateNum);

                        } else {

                            System.out.println("Car " + plateNum + " cannot be parked at (" + row + ", " + column + ")");
                        }
                    }
                }
            }
        }

        scanner.close();
    }

	/**
	 * Produce string representation of the parking lot
	 * 
	 * @return String containing the parking lot information
	 */
	public String toString() {
		// NOTE: The implementation of this method is complete. You do NOT need to
		// change it for the assignment.


		StringBuffer buffer = new StringBuffer();
		buffer.append("==== Lot Design ====").append(System.lineSeparator());

		for (int i = 0; i < lotDesign.length; i++) {
			for (int j = 0; j < lotDesign[0].length; j++) {
				buffer.append((lotDesign[i][j] != null) ? Util.getLabelByCarType(lotDesign[i][j])
						: Util.getLabelByCarType(CarType.NA));
				if (j < numSpotsPerRow - 1) {
					buffer.append(", ");
				}
			}
			buffer.append(System.lineSeparator());
		}

		buffer.append(System.lineSeparator()).append("==== Parking Occupancy ====").append(System.lineSeparator());

		for (int i = 0; i < occupancy.length; i++) {
			for (int j = 0; j < occupancy[0].length; j++) {
				buffer.append(
						"(" + i + ", " + j + "): " + ((occupancy[i][j] != null) ? occupancy[i][j] : "Unoccupied"));
				buffer.append(System.lineSeparator());
			}

		}
		return buffer.toString();
	}

	/**
	 * <b>main</b> of the application. The method first reads from the standard
	 * input the name of the file to process. Next, it creates an instance of
	 * ParkingLot. Finally, it prints to the standard output information about the
	 * instance of the ParkingLot just created.
	 * 
	 * @param args command lines parameters (not used in the body of the method)
	 * @throws Exception
	 */

	public static void main(String args[]) throws Exception {

		StudentInfo.display();

		System.out.print("Please enter the name of the file to process: ");

		Scanner scanner = new Scanner(System.in);

		String strFilename = scanner.nextLine();

		ParkingLot lot = new ParkingLot(strFilename);

		System.out.println("Total number of parkable spots (capacity): " + lot.getTotalCapacity());

		System.out.println("Number of cars currently parked in the lot: " + lot.getTotalOccupancy());

		System.out.print(lot);

	}
}