import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class RentalSystemGUI extends Application {
    private RentalSystem rentalSystem = RentalSystem.getInstance();
    private ObservableList<Vehicle> availableVehicles = FXCollections.observableArrayList();
    private ObservableList<Vehicle> rentedVehicles = FXCollections.observableArrayList();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private ObservableList<RentalRecord> rentalHistory = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Vehicle Rental System");
        
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createVehicleTab(),
            createCustomerTab(),
            createRentTab(),
            createReturnTab(),
            createHistoryTab()
        );

        refreshData();

        Scene scene = new Scene(tabPane, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Tab createVehicleTab() {
        Tab tab = new Tab("Manage Vehicles");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(15);

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Car", "Motorcycle", "Truck"
        ));
        typeCombo.setPromptText("Select Vehicle Type");
        
        TextField makeField = new TextField();
        TextField modelField = new TextField();
        TextField yearField = new TextField();
        TextField plateField = new TextField();
        TextField specField = new TextField();
        
        grid.addRow(0, new Label("Type:"), typeCombo);
        grid.addRow(1, new Label("Make:"), makeField);
        grid.addRow(2, new Label("Model:"), modelField);
        grid.addRow(3, new Label("Year:"), yearField);
        grid.addRow(4, new Label("License Plate:"), plateField);
        grid.addRow(5, new Label("Specification (Car: Int | MotCyc: true/false | Truck: Double) :"), specField);

        Button addBtn = new Button("Add Vehicle");
        addBtn.setOnAction(e -> {
            try {
                Vehicle vehicle = createVehicle(
                    typeCombo.getValue(),
                    makeField.getText(),
                    modelField.getText(),
                    Integer.parseInt(yearField.getText()),
                    plateField.getText(),
                    specField.getText()
                );
                if (rentalSystem.addVehicle(vehicle)) {
                    showAlert("Success", "Vehicle added successfully!");
                    clearFields(makeField, modelField, yearField, plateField, specField);
                    refreshData();
                }
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });

        VBox layout = new VBox(15, grid, addBtn);
        layout.setPadding(new Insets(15));
        tab.setContent(layout);
        return tab;
    }

    private Tab createCustomerTab() {
        Tab tab = new Tab("Manage Customers");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(15);

        TextField idField = new TextField();
        TextField nameField = new TextField();
        ListView<Customer> customerListView = new ListView<>(customers);
        
        grid.addRow(0, new Label("Customer ID:"), idField);
        grid.addRow(1, new Label("Name:"), nameField);
        
        Button addBtn = new Button("Add Customer");
        addBtn.setOnAction(e -> {
            try {
                Customer customer = new Customer(
                    idField.getText(),
                    nameField.getText()
                );
                if (rentalSystem.addCustomer(customer)) {
                    showAlert("Success", "Customer added successfully!");
                    idField.clear();
                    nameField.clear();
                    refreshData();
                }
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });

        VBox layout = new VBox(15, 
            new VBox(10, grid, addBtn),
            new Label("Existing Customers:"), 
            customerListView
        );
        layout.setPadding(new Insets(15));
        tab.setContent(layout);
        return tab;
    }

    private Tab createRentTab() {
        Tab tab = new Tab("Rent Vehicle");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        ListView<Vehicle> vehicleList = new ListView<>(availableVehicles);
        ListView<Customer> customerList = new ListView<>(customers);
        TextField amountField = new TextField();

        Button rentBtn = new Button("Rent Vehicle");
        rentBtn.setOnAction(e -> {
            Vehicle vehicle = vehicleList.getSelectionModel().getSelectedItem();
            Customer customer = customerList.getSelectionModel().getSelectedItem();
            
            if (vehicle == null || customer == null) {
                showAlert("Error", "Please select both a vehicle and customer");
                return;
            }

            try {
                double amount = Double.parseDouble(amountField.getText());
                if (rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), amount)) {
                    showAlert("Success", "Vehicle rented successfully!");
                    amountField.clear();
                    refreshData();
                }
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });

        HBox selectionBox = new HBox(15,
            new VBox(10, new Label("Available Vehicles"), vehicleList),
            new VBox(10, new Label("Customers"), customerList)
        );
        selectionBox.setPrefHeight(300);

        layout.getChildren().addAll(
            selectionBox,
            new HBox(15, new Label("Rental Amount:"), amountField, rentBtn)
        );
        tab.setContent(layout);
        return tab;
    }

    private Tab createReturnTab() {
        Tab tab = new Tab("Return Vehicle");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        ListView<Vehicle> vehicleList = new ListView<>(rentedVehicles);
        ListView<Customer> customerList = new ListView<>(customers);
        TextField feesField = new TextField();

        Button returnBtn = new Button("Return Vehicle");
        returnBtn.setOnAction(e -> {
            Vehicle vehicle = vehicleList.getSelectionModel().getSelectedItem();
            Customer customer = customerList.getSelectionModel().getSelectedItem();
            
            if (vehicle == null || customer == null) {
                showAlert("Error", "Please select both a vehicle and customer");
                return;
            }

            try {
                double fees = Double.parseDouble(feesField.getText());
                if (rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), fees)) {
                    showAlert("Success", "Vehicle returned successfully!");
                    feesField.clear();
                    refreshData();
                }
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });

        HBox selectionBox = new HBox(15,
            new VBox(10, new Label("Rented Vehicles"), vehicleList),
            new VBox(10, new Label("Customers"), customerList)
        );
        selectionBox.setPrefHeight(300);

        layout.getChildren().addAll(
            selectionBox,
            new HBox(15, new Label("Extra Fees:"), feesField, returnBtn)
        );
        tab.setContent(layout);
        return tab;
    }

    private Tab createHistoryTab() {
        Tab tab = new Tab("Rental History");
        TableView<RentalRecord> table = new TableView<>(rentalHistory);
        
        // Type Column
        TableColumn<RentalRecord, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        
        // Vehicle Column (uses getVehicle().getLicensePlate())
        TableColumn<RentalRecord, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getVehicle().getLicensePlate()));
        
        // Customer Column (uses getCustomer().getCustomerName())
        TableColumn<RentalRecord, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomer().getCustomerName()));
        
        // Date Column
        TableColumn<RentalRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        // Amount Column
        TableColumn<RentalRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        table.getColumns().addAll(typeCol, vehicleCol, customerCol, dateCol, amountCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        tab.setContent(table);
        return tab;
    }

    private Vehicle createVehicle(String type, String make, String model, int year, 
            String plate, String spec) throws Exception {
		if (type == null) throw new Exception("Please select a vehicle type");
		
		switch (type) {
			case "Car":
				Car car = new Car(make, model, year, Integer.parseInt(spec));
				car.setLicensePlate(plate);
				return car;
			case "Motorcycle":
				Motorcycle mc = new Motorcycle(make, model, year, Boolean.parseBoolean(spec));
				mc.setLicensePlate(plate);
				return mc;
			case "Truck":
				Truck truck = new Truck(make, model, year, Double.parseDouble(spec));
				truck.setLicensePlate(plate);
				return truck;
			default:
				throw new Exception("Invalid vehicle type");
		}
    }

    private void refreshData() {
        availableVehicles.setAll(rentalSystem.getVehicles().stream()
            .filter(v -> v.getStatus() == Vehicle.VehicleStatus.AVAILABLE)
            .collect(Collectors.toList()));

        rentedVehicles.setAll(rentalSystem.getVehicles().stream()
            .filter(v -> v.getStatus() == Vehicle.VehicleStatus.RENTED)
            .collect(Collectors.toList()));

        customers.setAll(rentalSystem.getCustomers());
        rentalHistory.setAll(rentalSystem.getRentalHistory().getRentalHistory());
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }
}