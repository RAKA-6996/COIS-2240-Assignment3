import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

class VehicleRentalTest {
	
	private Vehicle vehicle;
    private Customer customer;
    private RentalSystem rentalSystem;

	@BeforeEach
	void setUp() throws Exception {
        rentalSystem = RentalSystem.getInstance();
        
        // Initialize test vehicle and customer
        vehicle = new Car("Toyota", "Camry", 2020, 5);
        vehicle.setLicensePlate("TES001");
        customer = new Customer("CUST001", "Test Customer");
        
        // Add to rental system
        rentalSystem.addVehicle(vehicle);
        rentalSystem.addCustomer(customer);
	}
	
	@Test
	void testLicensePlateValidation() {
        // Test valid license plates
        Vehicle validVehicle1 = new Car("Toyota", "Camry", 2020, 5);
        validVehicle1.setLicensePlate("AAA100");
        assertEquals("AAA100", validVehicle1.getLicensePlate());

        Vehicle validVehicle2 = new Car("Honda", "Civic", 2021, 4);
        validVehicle2.setLicensePlate("ABC567");
        assertEquals("ABC567", validVehicle2.getLicensePlate());

        Vehicle validVehicle3 = new Car("Ford", "Focus", 2019, 5);
        validVehicle3.setLicensePlate("ZZZ999");
        assertEquals("ZZZ999", validVehicle3.getLicensePlate());

        // Test invalid license plates
        
        // Test empty string
        Vehicle invalidVehicle1 = new Car("Toyota", "Camry", 2020, 5);
        try {
            invalidVehicle1.setLicensePlate("");
            fail("Empty plate should throw exception");
        } 
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid license plate."));
        }

        // Test null plate
        Vehicle invalidVehicle2 = new Car("Honda", "Civic", 2021, 4);
        try {
            invalidVehicle2.setLicensePlate(null);
            fail("Null plate should throw exception");
        } 
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid license plate."));
        }

        // Test plate that's too long
        Vehicle invalidVehicle3 = new Car("Ford", "Focus", 2019, 5);
        try {
            invalidVehicle3.setLicensePlate("AAA1000");
            fail("Long plate should throw exception");
        } 
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid license plate."));
        }

        // Test plate that's too short
        Vehicle invalidVehicle4 = new Car("Nissan", "Altima", 2022, 5);
        try {
            invalidVehicle4.setLicensePlate("ZZZ99");
            fail("Short plate should throw exception");
        } 
        catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid license plate."));
        }
    }
	
	@Test
    void testRentAndReturnVehicle() {
        // 1. Verify initial availability
        assertEquals(Vehicle.VehicleStatus.AVAILABLE, vehicle.getStatus());

        // 2. Test successful rent
        assertTrue(rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), 100.0),
            "First rental should succeed");
        assertEquals(Vehicle.VehicleStatus.RENTED, vehicle.getStatus(),
            "Status should update to RENTED");

        // 3. Test duplicate rent failure
        assertFalse(rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), 100.0),
            "Second rental attempt should fail");

        // 4. Test successful return
        assertTrue(rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), 0.0),
            "Return should succeed");
        assertEquals(Vehicle.VehicleStatus.AVAILABLE, vehicle.getStatus(),
            "Status should revert to AVAILABLE");

        // 5. Test duplicate return failure
        assertFalse(rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), 0.0),
            "Second return attempt should fail");
    }

}
