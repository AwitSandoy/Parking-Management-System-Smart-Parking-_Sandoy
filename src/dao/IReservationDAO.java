package dao;

import models.Reservation;

import java.sql.SQLException;
import java.util.List;

/**
 * Abstraction for all "reservations" table data access (booking/releasing
 * slots and reservation history). See IUserDAO for the full explanation
 * of why this interface exists (Dependency Inversion Principle).
 */
public interface IReservationDAO {

    boolean bookSlot(int userId, int slotId) throws SQLException;

    boolean releaseSlot(int reservationId) throws SQLException;

    List<Reservation> getActiveReservationsForUser(int userId) throws SQLException;

    List<Reservation> getAllReservations() throws SQLException;
}
