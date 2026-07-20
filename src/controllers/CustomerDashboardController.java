package controllers;

import dao.IParkingSlotDAO;
import dao.IReservationDAO;
import dao.ParkingSlotDAO;
import dao.ReservationDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.ParkingSlot;
import models.Reservation;
import models.Session;
import models.User;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class CustomerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<ParkingSlot> availableSlotsTable;
    @FXML private TableColumn<ParkingSlot, String> colSlotNumber;
    @FXML private TableColumn<ParkingSlot, String> colStatus;
    @FXML private TableColumn<ParkingSlot, Double> colRate;

    @FXML private TableView<Reservation> myReservationsTable;
    @FXML private TableColumn<Reservation, String> colResSlot;
    @FXML private TableColumn<Reservation, String> colResEntry;
    @FXML private TableColumn<Reservation, Double> colResAmount;

    // Depends on the IParkingSlotDAO / IReservationDAO abstractions, not
    // the concrete classes (Dependency Inversion Principle).
    private final IParkingSlotDAO slotDAO = new ParkingSlotDAO();
    private final IReservationDAO reservationDAO = new ReservationDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        colSlotNumber.setCellValueFactory(new PropertyValueFactory<>("slotNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("ratePerHour"));

        colResSlot.setCellValueFactory(new PropertyValueFactory<>("slotNumber"));
        colResEntry.setCellValueFactory(new PropertyValueFactory<>("entryTime"));
        colResAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Validate against the serialized session file rather than trusting
        // the in-memory User object alone - this is the "use the file to
        // maintain the session while navigating" requirement in practice.
        Session session = SessionManager.getActiveSession();
        if (session == null || session.getUserId() != user.getId()) {
            showError("No valid session found. Please log in again.");
            returnToLogin();
            return;
        }

        welcomeLabel.setText("Welcome, " + user.getUsername());
        refreshAll();
    }

    private void refreshAll() {
        loadAvailableSlots();
        loadMyReservations();
    }

    private void loadAvailableSlots() {
        try {
            ObservableList<ParkingSlot> slots = FXCollections.observableArrayList(slotDAO.getAvailableSlots());
            availableSlotsTable.setItems(slots);
        } catch (SQLException e) {
            showError("Unable to load parking slots: " + e.getMessage());
        }
    }

    private void loadMyReservations() {
        try {
            ObservableList<Reservation> reservations =
                    FXCollections.observableArrayList(reservationDAO.getActiveReservationsForUser(currentUser.getId()));
            myReservationsTable.setItems(reservations);
        } catch (SQLException e) {
            showError("Unable to load your reservations: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        refreshAll();
        statusLabel.setText("");
    }

    @FXML
    private void handleBookSlot() {
        ParkingSlot selected = availableSlotsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
            statusLabel.setText("Please select a slot first.");
            return;
        }
        try {
            boolean success = reservationDAO.bookSlot(currentUser.getId(), selected.getSlotId());
            if (success) {
                statusLabel.setStyle("-fx-text-fill: #16a34a;");
                statusLabel.setText("Slot " + selected.getSlotNumber() + " booked successfully!");
                refreshAll();
            } else {
                statusLabel.setStyle("-fx-text-fill: #dc2626;");
                statusLabel.setText("Sorry, that slot was just taken. Please pick another.");
                refreshAll();
            }
        } catch (SQLException e) {
            showError("Booking failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleReleaseSlot() {
        Reservation selected = myReservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
            statusLabel.setText("Please select an active reservation to release.");
            return;
        }
        try {
            boolean success = reservationDAO.releaseSlot(selected.getReservationId());
            if (success) {
                statusLabel.setStyle("-fx-text-fill: #16a34a;");
                statusLabel.setText("Slot " + selected.getSlotNumber() + " released. Thank you!");
                refreshAll();
            } else {
                showError("Unable to release that reservation.");
            }
        } catch (SQLException e) {
            showError("Release failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Delete the serialized session file - this is the required
        // "session file must be automatically deleted" behavior on logout.
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
