package dao;

import models.ParkingSlot;

import java.sql.SQLException;
import java.util.List;

/**
 * Abstraction for all "parking_slots" table data access.
 * See IUserDAO for the full explanation of why this interface exists
 * (Dependency Inversion Principle).
 */
public interface IParkingSlotDAO {

    List<ParkingSlot> getAllSlots() throws SQLException;

    List<ParkingSlot> getAvailableSlots() throws SQLException;

    boolean addSlot(String slotNumber, double ratePerHour) throws SQLException;

    boolean updateSlot(int slotId, String slotNumber, double ratePerHour) throws SQLException;

    boolean updateStatus(int slotId, String status) throws SQLException;

    boolean deleteSlot(int slotId) throws SQLException;

    ParkingSlot getSlotById(int slotId) throws SQLException;
}
