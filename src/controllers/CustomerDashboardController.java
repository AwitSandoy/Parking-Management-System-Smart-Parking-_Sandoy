package controllers;

import dao.ParkingFacade;
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
import utils.SlotStatusObserver;
import utils.SlotStatusPublisher;

import java.io.IOException;
import java.sql.SQLException;

/*  Implements SlotStatusObserver - this controller reacts automatically whenever
    ParkingFacade announces that a booking/release happened, instead of every button
    handler needing to manually refresh tables itself (Observer pattern - Behavioral).  */
public class CustomerDashboardController implements SlotStatusObserver {

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

    /*  ParkingFacade (Facade pattern - Structural) wraps IParkingSlotDAO and
        IReservationDAO behind simplified booking/release methods, and owns the
        SlotStatusPublisher this controller subscribes to.                      */
    private final ParkingFacade parkingFacade = new ParkingFacade(
            new ParkingSlotDAO(),
            new ReservationDAO(),
            new SlotStatusPublisher()
    );
    private User currentUser;

    @FXML
    public void initialize() {
        colSlotNumber.setCellValueFactory(new PropertyValueFactory<>("slotNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("ratePerHour"));

        colResSlot.setCellValueFactory(new PropertyValueFactory<>("slotNumber"));
        colResEntry.setCellValueFactory(new PropertyValueFactory<>("entryTime"));
        colResAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        /*  Subscribe this controller to the Facade's publisher, so that onSlotStatusChanged()
            below fires automatically after any successful booking or release.   */
        parkingFacade.getPublisher().subscribe(this);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;

        /*  Validate against the serialized session file rather than trusting the in-memory
            User object alone - this is the "use the file to maintain the session while
            navigating" requirement in practice.                                            */
        Session session = SessionManager.getActiveSession();
        if (session == null || session.getUserId() != user.getId()) {
            showError("No valid session found. Please log in again.");
            returnToLogin();
            return;
        }

        welcomeLabel.setText("Welcome, " + user.getUsername());
        refreshAll();
    }

    /*  Observer callback - called automatically by SlotStatusPublisher whenever ParkingFacade
        successfully books or releases a slot.                                                  */
    @Override
    public void onSlotStatusChanged() {
        refreshAll();
    }

    private void refreshAll() {
        loadAvailableSlots();
        loadMyReservations();
    }

    private void loadAvailableSlots() {
        try {
            ObservableList<ParkingSlot> slots = FXCollections.observableArrayList(parkingFacade.getAvailableSlots());
            availableSlotsTable.setItems(slots);
        } catch (SQLException e) {
            showError("Unable to load parking slots: " + e.getMessage());
        }
    }

    private void loadMyReservations() {
        try {
            ObservableList<Reservation> reservations =
                    FXCollections.observableArrayList(parkingFacade.getActiveReservationsForUser(currentUser.getId()));
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
            /*  Booking now goes through the Facade. If it succeeds, the Facade notifies
                observers itself (see onSlotStatusChanged above) - no manual
                refreshAll() call needed here.                                              */
            boolean success = parkingFacade.bookSlot(currentUser.getId(), selected.getSlotId());
            if (success) {
                statusLabel.setStyle("-fx-text-fill: #16a34a;");
                statusLabel.setText("Slot " + selected.getSlotNumber() + " booked successfully!");
            } else {
                statusLabel.setStyle("-fx-text-fill: #dc2626;");
                statusLabel.setText("Sorry, that slot was just taken. Please pick another.");
                refreshAll(); // no successful change happened, so refresh manually here.
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
            boolean success = parkingFacade.releaseSlot(selected.getReservationId());
            if (success) {
                statusLabel.setStyle("-fx-text-fill: #16a34a;");
                statusLabel.setText("Slot " + selected.getSlotNumber() + " released. Thank you!");
            } else {
                showError("Unable to release that reservation.");
            }
        } catch (SQLException e) {
            showError("Release failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Unsubscribe from the publisher before leaving this screen.
        parkingFacade.getPublisher().unsubscribe(this);

        /*  Delete the serialized session file - this is the required "session file must be
            automatically deleted" behavior on logout.                                      */
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