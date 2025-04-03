public abstract class Vehicle {
    private String licensePlate;
    private String make;
    private String model;
    private int year;
    private VehicleStatus status;

    public enum VehicleStatus { AVAILABLE, RESERVED, RENTED, MAINTENANCE, OUTOFSERVICE }

    public Vehicle(String make, String model, int year) {
        this.make = capitalize(make);
        this.model = capitalize(model);
        this.year = year;
        this.status = VehicleStatus.AVAILABLE;
        this.licensePlate = null;
    }

    public Vehicle() {
        this(null, null, 0);
    }

    // Task 2-1: Modify the setLicensePlate to 
    public void setLicensePlate(String plate) {
    	if (!isValidPlate(plate)) {
            throw new IllegalArgumentException("Invalid license plate.");
        }
        this.licensePlate = plate.toUpperCase();
    }
    
    // Task 2-1: Create isValidPlate method to check if the plate number is in correct format
    private boolean isValidPlate(String plate) {
    	if (plate == null || plate.length() != 6) {
            return false;
        }

        // Check first three characters are uppercase or lowercase letters (A-Z or a-z)
        for (int i = 0; i < 3; i++) {
            char ch = plate.charAt(i);
            if (!((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))) {
                return false;
            }
        }

        // Check last three characters are digits (0-9)
        for (int i = 3; i < 6; i++) {
            char ch = plate.charAt(i);
            if (!(ch >= '0' && ch <= '9')) {
                return false;
            }
        }
        
        return true;
    }


    public void setStatus(VehicleStatus status) {
    	this.status = status;
    }

    public String getLicensePlate() { return licensePlate; }

    public String getMake() { return make; }

    public String getModel() { return model;}

    public int getYear() { return year; }

    public VehicleStatus getStatus() { return status; }

    public String getInfo() {
        return "| " + licensePlate + " | " + make + " | " + model + " | " + year + " | " + status + " |";
    }

    private String capitalize(String input) {
    if (input == null || input.isEmpty()) {
        return input;
    }
    return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
}

}
