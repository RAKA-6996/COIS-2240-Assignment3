import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

public class RentalSystem {

    private static RentalSystem instance;

    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();

    public boolean addVehicle(Vehicle vehicle) {
        if (findVehicleByPlate(vehicle.getLicensePlate()) != null) {
            System.out.println("Error: Vehicle with plate " + vehicle.getLicensePlate() + " already exists.");
            return false; 
        }
        vehicles.add(vehicle);
        saveVehicle(vehicle); 
        System.out.println("Vehicle added successfully.");
        return true;
    }

    public boolean addCustomer(Customer customer) {
        if (findCustomerById(customer.getId()) != null) {
            System.out.println("Error: Customer with ID " + customer.getId() + " already exists.");
            return false;
        }
        customers.add(customer);
        saveCustomer(customer);  
        System.out.println("Customer added successfully.");
        return true;
    }

    public void rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
            vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
            RentalRecord record = new RentalRecord(vehicle, customer, date, amount, "RENT");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle rented to " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not available for renting.");
        }
    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.RENTED) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            RentalRecord record = new RentalRecord(vehicle, customer, date, extraFees, "RETURN");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle returned by " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not rented.");
        }
    }    

    private void saveVehicle(Vehicle vehicle){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VEHICLES_FILE, true))){
            String vehicleType = vehicle instanceof Car ? "Car": vehicle instanceof Motorcycle ? "Motorcycle" : "Truck";

            writer.write(String.format("%s, %s, %s, %s, %d, %s", vehicleType, vehicle.getLicensePlate(), vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getStatus()));

            if (vehicle instanceof Car){
                writer.write("," + ((Car) vehicle).getNumSeats());
            }
            else if (vehicle instanceof Motorcycle){
                writer.write("," + ((Motorcycle) vehicle).hasSidecar());
            }
            else if (vehicle instanceof Truck){
                writer.write("," + ((Truck) vehicle).getCargoCapacity());
            }

            writer.newLine();
        }
        catch (IOException e){
            System.err.println("Error saving vehicle: " + e.getMessage());
        }
    }

    private void saveCustomer(Customer customer) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CUSTOMERS_FILE, true))) {
            writer.write(String.format("%d,%s",
                customer.getCustomerId(),
                customer.getCustomerName()));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving customer: " + e.getMessage());
        }
    }

    private void saveRecord(RentalRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECORDS_FILE, true))) {
            writer.write(String.format("%s,%d,%s,%s,%.2f,%s",
                record.getVehicle().getLicensePlate(),
                record.getCustomer().getCustomerId(),
                record.getDate(),
                record.getAmount(),
                record.getTransactionType()));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving rental record: " + e.getMessage());
        }
    }

    public void displayVehicles(boolean onlyAvailable) {
    	System.out.println("|     Type         |\tPlate\t|\tMake\t|\tModel\t|\tYear\t|");
    	System.out.println("---------------------------------------------------------------------------------");
    	 
        for (Vehicle v : vehicles) {
            if (!onlyAvailable || v.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
                System.out.println("|     " + (v instanceof Car ? "Car          " : "Motorcycle   ") + "|\t" + v.getLicensePlate() + "\t|\t" + v.getMake() + "\t|\t" + v.getModel() + "\t|\t" + v.getYear() + "\t|\t");
            }
        }
        System.out.println();
    }
    
    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }
    
    public void displayRentalHistory() {
        for (RentalRecord record : rentalHistory.getRentalHistory()) {
            System.out.println(record.toString());
        }
    }
    
    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(String id) {
        for (Customer c : customers)
            if (c.getCustomerId() == Integer.parseInt(id))
                return c;
        return null;
    }
}