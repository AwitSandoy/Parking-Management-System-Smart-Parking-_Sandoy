package dao;

import models.Reservation;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO implements IReservationDAO {

    /**
     * Books a slot for a customer: inserts a reservation row and marks
     * the slot Occupied. Both statements run inside a single JDBC
     * transaction so the database can never end up in a state where a
     * slot is marked Occupied without a matching reservation, or vice
     * versa, even if something fails halfway through.
     */
    public boolean bookSlot(int userId, int slotId) throws SQLException {
        String insertSql = "INSERT INTO reservations (user_id, slot_id, entry_time) VALUES (?, ?, ?)";
        String updateSlotSql = "UPDATE parking_slots SET status = 'Occupied' " +
                                "WHERE slot_id = ? AND status = 'Available'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement updateSlot = conn.prepareStatement(updateSlotSql)) {
                updateSlot.setInt(1, slotId);
                int rowsChanged = updateSlot.executeUpdate();
                if (rowsChanged == 0) {
                    // Someone else already booked this slot - abort safely.
                    conn.rollback();
                    return false;
                }
            }
            try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                insert.setInt(1, userId);
                insert.setInt(2, slotId);
                insert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                insert.executeUpdate();
            }
            conn.commit();
            return true;
        }
    }

    /**
     * Releases a slot: sets exit_time, computes total_amount from the
     * elapsed time and the slot's hourly rate, and marks the slot
     * Available again - all inside one transaction.
     */
    public boolean releaseSlot(int reservationId) throws SQLException {
        String selectSql = "SELECT r.slot_id, r.entry_time, s.rate_per_hour " +
                            "FROM reservations r JOIN parking_slots s ON r.slot_id = s.slot_id " +
                            "WHERE r.reservation_id = ?";
        String updateReservationSql = "UPDATE reservations SET exit_time = ?, total_amount = ? " +
                                       "WHERE reservation_id = ?";
        String updateSlotSql = "UPDATE parking_slots SET status = 'Available' WHERE slot_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int slotId;
            LocalDateTime entryTime;
            double ratePerHour;

            try (PreparedStatement select = conn.prepareStatement(selectSql)) {
                select.setInt(1, reservationId);
                try (ResultSet rs = select.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    slotId = rs.getInt("slot_id");
                    entryTime = rs.getTimestamp("entry_time").toLocalDateTime();
                    ratePerHour = rs.getDouble("rate_per_hour");
                }
            }

            LocalDateTime exitTime = LocalDateTime.now();
            double hoursParked = Math.max(Duration.between(entryTime, exitTime).toMinutes() / 60.0, 0.25);
            // Minimum charge = 15 minutes, rounded up to keep the math simple and fair.
            double totalAmount = Math.round(hoursParked * ratePerHour * 100.0) / 100.0;

            try (PreparedStatement update = conn.prepareStatement(updateReservationSql)) {
                update.setTimestamp(1, Timestamp.valueOf(exitTime));
                update.setDouble(2, totalAmount);
                update.setInt(3, reservationId);
                update.executeUpdate();
            }

            try (PreparedStatement updateSlot = conn.prepareStatement(updateSlotSql)) {
                updateSlot.setInt(1, slotId);
                updateSlot.executeUpdate();
            }

            conn.commit();
            return true;
        }
    }

    /** CUSTOMER: active (not yet released) reservations for one user. */
    public List<Reservation> getActiveReservationsForUser(int userId) throws SQLException {
        String sql = "SELECT r.reservation_id, r.user_id, u.username, r.slot_id, s.slot_number, " +
                     "r.entry_time, r.exit_time, r.total_amount " +
                     "FROM reservations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN parking_slots s ON r.slot_id = s.slot_id " +
                     "WHERE r.user_id = ? AND r.exit_time IS NULL " +
                     "ORDER BY r.entry_time DESC";
        return queryReservations(sql, userId);
    }

    /** ADMIN: every reservation ever made (history + active). */
    public List<Reservation> getAllReservations() throws SQLException {
        String sql = "SELECT r.reservation_id, r.user_id, u.username, r.slot_id, s.slot_number, " +
                     "r.entry_time, r.exit_time, r.total_amount " +
                     "FROM reservations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN parking_slots s ON r.slot_id = s.slot_id " +
                     "ORDER BY r.entry_time DESC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private List<Reservation> queryReservations(String sql, int userId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Timestamp exit = rs.getTimestamp("exit_time");
        return new Reservation(
                rs.getInt("reservation_id"),
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getInt("slot_id"),
                rs.getString("slot_number"),
                rs.getTimestamp("entry_time").toLocalDateTime().toString(),
                exit == null ? null : exit.toLocalDateTime().toString(),
                rs.getDouble("total_amount")
        );
    }
}
