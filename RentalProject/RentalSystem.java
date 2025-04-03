import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
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

    private static final String VEHICLES_FILE = "vehicles.txt";
    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String RECORDS_FILE = "rental_records.txt";

    private RentalSystem() {
        loadData();
    }

    public static RentalSystem getInstance() {
        if (instance == null) {
            instance = new RentalSystem();
        }
        return instance;
    }

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
        if (findCustomerById(customer.getCustomerId()) != null) {
            System.out.println("Error: Customer with ID " + customer.getCustomerId() + " already exists.");
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
            updateVehiclesFile();
            RentalRecord record = new RentalRecord(vehicle, customer, date, amount, "RENT");
            rentalHistory.addRecord(record);
            saveRecord(record);
            saveVehicle(vehicle);
            System.out.println("Vehicle rented to " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not available for renting.");
        }
    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.RENTED) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            updateVehiclesFile();
            RentalRecord record = new RentalRecord(vehicle, customer, date, extraFees, "RETURN");
            rentalHistory.addRecord(record);
            saveRecord(record);
            saveVehicle(vehicle);
            System.out.println("Vehicle returned by " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not rented.");
        }
    }    

    private void saveVehicle(Vehicle vehicle){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VEHICLES_FILE, false))){
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
            writer.write(String.format("%s,%s",
                customer.getCustomerId(),
                customer.getCustomerName()));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving customer: " + e.getMessage());
        }
    }

    private void saveRecord(RentalRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECORDS_FILE, true))) {
            writer.write(String.format("%s,%s,%s,%.2f,%s",
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

    private void loadData(){
        loadVehicles();
        loadCustomers();
        loadRentalRecords();
    }

    private void loadVehicles(){
        try (BufferedReader reader = new BufferedReader(new FileReader(VEHICLES_FILE))){

            String line;
            while ((line = reader.readLine()) != null){
                String[] parts = line.split(",");
                if (parts.length >= 6){
                    String type = parts[0].trim();
                    String plate = parts[1].trim();
                    String make = parts[2].trim();
                    String model = parts[3].trim();
                    int year = Integer.parseInt(parts[4].trim());
                    Vehicle.VehicleStatus status = Vehicle.VehicleStatus.valueOf(parts[5].trim());

                    Vehicle vehicle;
                    switch (type){
                        case "Car":
                            int seats = Integer.parseInt(parts[6].trim());
                            vehicle = new Car(make, model, year, seats);
                            break;
                        case "Motorcycle":
                            boolean sidecar = Boolean.parseBoolean(parts[6].trim());
                            vehicle = new Motorcycle(make, model, year, sidecar);
                            break;
                        case "Truck":
                            double cargoCapacity = Double.parseDouble(parts[6].trim());
                            vehicle = new Truck(make, model, year, cargoCapacity);
                            break;
                        default:
                            continue;
                    }
                    vehicle.setLicensePlate(plate);
                    vehicle.setStatus(status);
                    vehicles.add(vehicle);
                }
            }
        }
        catch (IOException e){
            System.err.println("Error loading vehicles: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CUSTOMERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    customers.add(new Customer(id, name));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
    }

    private void loadRentalRecords() {
        try (BufferedReader reader = new BufferedReader(new FileReader(RECORDS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String plate = parts[0].trim();
                    String customerId = parts[1].trim();
                    LocalDate date = LocalDate.parse(parts[2].trim());
                    double amount = Double.parseDouble(parts[3].trim());
                    String transactionType = parts[4].trim();

                    Vehicle vehicle = findVehicleByPlate(plate);
                    Customer customer = findCustomerById(String.valueOf(customerId));

                    if (vehicle != null && customer != null) {
                        RentalRecord record = new RentalRecord(vehicle, customer, date, amount, transactionType);
                        rentalHistory.addRecord(record);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading rental records: " + e.getMessage());
        }
    }

    public void displayVehicles(boolean onlyAvailable) {
        System.out.println("|     Type         |\tPlate\t|\tMake\t|\tModel\t|\tYear\t|");
        System.out.println("---------------------------------------------------------------------------------");
         
        for (Vehicle v : vehicles) {
            if (!onlyAvailable || v.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
                String type = (v instanceof Car) ? "Car" : 
                             (v instanceof Motorcycle) ? "Motorcycle" :
                             (v instanceof Truck) ? "Truck" : "Unknown";
                System.out.println("|     " + type + "          |\t" + v.getLicensePlate() + "\t|\t" + 
                    v.getMake() + "\t|\t" + v.getModel() + "\t|\t" + v.getYear() + "\t|\t");
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
            if (c.getCustomerId().equals(id))
                return c;
        return null;
    }

    private void updateVehiclesFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VEHICLES_FILE, false))) {
            for (Vehicle v : vehicles) {
                String type = "Unknown";
                String extra = "";
    
                if (v instanceof Car) {
                    type = "Car";
                    extra = "," + ((Car) v).getNumSeats();
                } else if (v instanceof Motorcycle) {
                    type = "Motorcycle";
                    extra = "," + ((Motorcycle) v).hasSidecar();
                } else if (v instanceof Truck) {
                    type = "Truck";
                    extra = "," + ((Truck) v).getCargoCapacity();
                }
    
                writer.write(type + "," + v.getLicensePlate() + "," + v.getMake() + "," + v.getModel() + "," + v.getYear() + "," + v.getStatus() + extra);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error updating vehicles file: " + e.getMessage());
        }
    }
    
}