package models;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Reservation {

    private final SimpleIntegerProperty reservationId;
    private final SimpleIntegerProperty userId;
    private final SimpleStringProperty username;   // joined field, for display only
    private final SimpleIntegerProperty slotId;
    private final SimpleStringProperty slotNumber; // joined field, for display only
    private final SimpleStringProperty entryTime;
    private final SimpleStringProperty exitTime;
    private final SimpleDoubleProperty totalAmount;

    public Reservation(int reservationId, int userId, String username, int slotId,
                        String slotNumber, String entryTime, String exitTime, double totalAmount) {
        this.reservationId = new SimpleIntegerProperty(reservationId);
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.slotId = new SimpleIntegerProperty(slotId);
        this.slotNumber = new SimpleStringProperty(slotNumber);
        this.entryTime = new SimpleStringProperty(entryTime);
        this.exitTime = new SimpleStringProperty(exitTime == null ? "-" : exitTime);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
    }

    public int getReservationId() {
        return reservationId.get();
    }

    public int getUserId() {
        return userId.get();
    }

    public String getUsername() {
        return username.get();
    }

    public int getSlotId() {
        return slotId.get();
    }

    public String getSlotNumber() {
        return slotNumber.get();
    }

    public String getEntryTime() {
        return entryTime.get();
    }

    public String getExitTime() {
        return exitTime.get();
    }

    public double getTotalAmount() {
        return totalAmount.get();
    }

    public SimpleIntegerProperty reservationIdProperty() {
        return reservationId;
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public SimpleStringProperty slotNumberProperty() {
        return slotNumber;
    }

    public SimpleStringProperty entryTimeProperty() {
        return entryTime;
    }

    public SimpleStringProperty exitTimeProperty() {
        return exitTime;
    }

    public SimpleDoubleProperty totalAmountProperty() {
        return totalAmount;
    }
}
