package controllers;

import dao.IParkingSlotDAO;
import dao.IReservationDAO;
import dao.IUserDAO;
import dao.ParkingSlotDAO;
import dao.ReservationDAO;
import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.ParkingSlot;
import models.Reservation;
import models.Session;
import models.User;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;

//  SLOTS TAB :
    @FXML private TableView<ParkingSlot> slotsTable;
    @FXML private TableColumn<ParkingSlot, Integer> colSlotId;
    @FXML private TableColumn<ParkingSlot, String> colSlotNumber;
    @FXML private TableColumn<ParkingSlot, String> colSlotStatus;
    @FXML private TableColumn<ParkingSlot, Double> colSlotRate;
    @FXML private TextField slotNumberField;
    @FXML private TextField slotRateField;
    @FXML private Label slotsMessageLabel;

// USERS TAB :
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TextField newUsernameField;
    @FXML private TextField newUserPasswordField;
    @FXML private ComboBox<String> newUserRoleCombo;
    @FXML private Label usersMessageLabel;

// RESERVATIONS TAB :
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, Integer> colResId;
    @FXML private TableColumn<Reservation, String> colResUsername;
    @FXML private TableColumn<Reservation, String> colResSlotNumber;
    @FXML private TableColumn<Reservation, String> colResEntryTime;
    @FXML private TableColumn<Reservation, String> colResExitTime;
    @FXML private TableColumn<Reservation, Double> colResAmount;

    /*      Depends on the I_DAO (Data Access Object pattern) abstractions,
    not the concrete classes (Dependency Inversion Principle).          */
    private final IParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private final IUserDAO userDAO = new UserDAO();
    private final IReservationDAO reservationDAO = new ReservationDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        colSlotId.setCellValueFactory(new PropertyValueFactory<>("slotId"));
        colSlotNumber.setCellValueFactory(new PropertyValueFactory<>("slotNumber"));
        colSlotStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colSlotRate.setCellValueFactory(new PropertyValueFactory<>("ratePerHour"));

        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colResId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colResUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colResSlotNumber.setCellValueFactory(new PropertyValueFactory<>("slotNumber"));
        colResEntryTime.setCellValueFactory(new PropertyValueFactory<>("entryTime"));
        colResExitTime.setCellValueFactory(new PropertyValueFactory<>("exitTime"));
        colResAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        newUserRoleCombo.setItems(FXCollections.observableArrayList("Customer", "Admin"));
        newUserRoleCombo.getSelectionModel().selectFirst();

        // Selecting a row pre-fills the slot edit form for convenience.
        slotsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                slotNumberField.setText(newVal.getSlotNumber());
                slotRateField.setText(String.valueOf(newVal.getRatePerHour()));
            }
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;

        /*      Validate against the serialized session file rather than trusting the
        in-memory User object alone.                                                */
        Session session = SessionManager.getActiveSession();
        if (session == null || session.getUserId() != user.getId()) {
            showError("No valid session found. Please log in again.");
            returnToLogin();
            return;
        }

        welcomeLabel.setText("Logged in as " + user.getUsername());
        refreshSlots();
        refreshUsers();
        refreshReservations();
    }

//  SLOTS :
    private void refreshSlots() {
        try {
            ObservableList<ParkingSlot> slots = FXCollections.observableArrayList(slotDAO.getAllSlots());
            slotsTable.setItems(slots);
        } catch (SQLException e) {
            slotsMessageLabel.setText("Failed to load slots: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshSlots() {
        refreshSlots();
        slotsMessageLabel.setText("");
    }

    @FXML
    private void handleAddSlot() {
        slotsMessageLabel.setStyle("-fx-text-fill: #dc2626;");
        String number = slotNumberField.getText().trim();
        Double rate = parseRate(slotRateField.getText());

        if (number.isEmpty() || rate == null) {
            slotsMessageLabel.setText("Enter a valid slot number and numeric rate.");
            return;
        }
        try {
            boolean success = slotDAO.addSlot(number, rate);
            if (success) {
                slotsMessageLabel.setStyle("-fx-text-fill: #16a34a;");
                slotsMessageLabel.setText("Slot added.");
                clearSlotForm();
                refreshSlots();
            } else {
                slotsMessageLabel.setText("Could not add slot (duplicate slot number?).");
            }
        } catch (SQLException e) {
            slotsMessageLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateSlot() {
        slotsMessageLabel.setStyle("-fx-text-fill: #dc2626;");
        ParkingSlot selected = slotsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            slotsMessageLabel.setText("Select a slot to update.");
            return;
        }
        String number = slotNumberField.getText().trim();
        Double rate = parseRate(slotRateField.getText());
        if (number.isEmpty() || rate == null) {
            slotsMessageLabel.setText("Enter a valid slot number and numeric rate.");
            return;
        }
        try {
            boolean success = slotDAO.updateSlot(selected.getSlotId(), number, rate);
            slotsMessageLabel.setStyle(success ? "-fx-text-fill: #16a34a;" : "-fx-text-fill: #dc2626;");
            slotsMessageLabel.setText(success ? "Slot updated." : "Update failed.");
            refreshSlots();
        } catch (SQLException e) {
            slotsMessageLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSlot() {
        ParkingSlot selected = slotsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            slotsMessageLabel.setStyle("-fx-text-fill: #dc2626;");
            slotsMessageLabel.setText("Select a slot to delete.");
            return;
        }
        try {
            boolean success = slotDAO.deleteSlot(selected.getSlotId());
            slotsMessageLabel.setStyle(success ? "-fx-text-fill: #16a34a;" : "-fx-text-fill: #dc2626;");
            slotsMessageLabel.setText(success ? "Slot deleted." : "Delete failed.");
            clearSlotForm();
            refreshSlots();
        } catch (SQLException e) {
            slotsMessageLabel.setText("Error: " + e.getMessage());
        }
    }

    private void clearSlotForm() {
        slotNumberField.clear();
        slotRateField.clear();
    }

    private Double parseRate(String text) {
        try {
            double value = Double.parseDouble(text.trim());
            return value >= 0 ? value : null;
        } catch (Exception e) {
            return null;
        }
    }

//  USERS :
    private void refreshUsers() {
        try {
            ObservableList<User> users = FXCollections.observableArrayList(userDAO.getAllUsers());
            usersTable.setItems(users);
        } catch (SQLException e) {
            usersMessageLabel.setText("Failed to load users: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshUsers() {
        refreshUsers();
        usersMessageLabel.setText("");
    }

    @FXML
    private void handleAddUser() {
        usersMessageLabel.setStyle("-fx-text-fill: #dc2626;");
        String username = newUsernameField.getText().trim();
        String password = newUserPasswordField.getText();
        String role = newUserRoleCombo.getValue();

        if (username.length() < 3 || password.length() < 6) {
            usersMessageLabel.setText("Username >= 3 chars and password >= 6 chars required.");
            return;
        }
        try {
            if (!userDAO.isUsernameAvailable(username)) {
                usersMessageLabel.setText("Username already taken.");
                return;
            }
            boolean success = userDAO.addUser(username, password, role);
            usersMessageLabel.setStyle(success ? "-fx-text-fill: #16a34a;" : "-fx-text-fill: #dc2626;");
            usersMessageLabel.setText(success ? "User created." : "Failed to create user.");
            newUsernameField.clear();
            newUserPasswordField.clear();
            refreshUsers();
        } catch (SQLException e) {
            usersMessageLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateUserRole() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            usersMessageLabel.setStyle("-fx-text-fill: #dc2626;");
            usersMessageLabel.setText("Select a user to update.");
            return;
        }
        String newRole = newUserRoleCombo.getValue();
        try {
            boolean success = userDAO.updateUserRole(selected.getId(), newRole);
            usersMessageLabel.setStyle(success ? "-fx-text-fill: #16a34a;" : "-fx-text-fill: #dc2626;");
            usersMessageLabel.setText(success ? "Role updated to " + newRole + "." : "Update failed.");
            refreshUsers();
        } catch (SQLException e) {
            usersMessageLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            usersMessageLabel.setStyle("-fx-text-fill: #dc2626;");
            usersMessageLabel.setText("Select a user to delete.");
            return;
        }
        if (currentUser != null && selected.getId() == currentUser.getId()) {
            usersMessageLabel.setStyle("-fx-text-fill: #dc2626;");
            usersMessageLabel.setText("You cannot delete the account you are currently logged in with.");
            return;
        }
        try {
            boolean success = userDAO.deleteUser(selected.getId());
            usersMessageLabel.setStyle(success ? "-fx-text-fill: #16a34a;" : "-fx-text-fill: #dc2626;");
            usersMessageLabel.setText(success ? "User deleted." : "Delete failed.");
            refreshUsers();
        } catch (SQLException e) {
            usersMessageLabel.setText("Error: " + e.getMessage());
        }
    }

//  RESERVATIONS :
    private void refreshReservations() {
        try {
            ObservableList<Reservation> reservations =
                    FXCollections.observableArrayList(reservationDAO.getAllReservations());
            reservationsTable.setItems(reservations);
        } catch (SQLException e) {
            showError("Failed to load reservations: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshReservations() {
        refreshReservations();
    }

// SHARED :
    @FXML
    private void handleLogout(ActionEvent event) {
        // Delete the serialized session file on logout.
        SessionManager.destroySession();
        returnToLogin();
    }

    private void returnToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginRegister.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Smart Parking Management System");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Unable to return to login screen: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}