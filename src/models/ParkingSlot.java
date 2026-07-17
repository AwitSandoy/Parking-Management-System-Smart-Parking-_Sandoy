package models;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * ParkingSlot exposes JavaFX properties so it can be plugged directly
 * into a TableView with PropertyValueFactory, without needing a
 * separate view-model wrapper class.
 */
public class ParkingSlot {

    private final SimpleIntegerProperty slotId;
    private final SimpleStringProperty slotNumber;
    private final SimpleStringProperty status;
    private final SimpleDoubleProperty ratePerHour;

    public ParkingSlot(int slotId, String slotNumber, String status, double ratePerHour) {
        this.slotId = new SimpleIntegerProperty(slotId);
        this.slotNumber = new SimpleStringProperty(slotNumber);
        this.status = new SimpleStringProperty(status);
        this.ratePerHour = new SimpleDoubleProperty(ratePerHour);
    }

    public int getSlotId() {
        return slotId.get();
    }

    public String getSlotNumber() {
        return slotNumber.get();
    }

    public void setSlotNumber(String value) {
        slotNumber.set(value);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String value) {
        status.set(value);
    }

    public double getRatePerHour() {
        return ratePerHour.get();
    }

    public void setRatePerHour(double value) {
        ratePerHour.set(value);
    }

    public SimpleIntegerProperty slotIdProperty() {
        return slotId;
    }

    public SimpleStringProperty slotNumberProperty() {
        return slotNumber;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public SimpleDoubleProperty ratePerHourProperty() {
        return ratePerHour;
    }
}
