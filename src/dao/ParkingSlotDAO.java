package dao;

import models.ParkingSlot;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParkingSlotDAO implements IParkingSlotDAO {

    public List<ParkingSlot> getAllSlots() throws SQLException {
        List<ParkingSlot> list = new ArrayList<>();
        String sql = "SELECT slot_id, slot_number, status, rate_per_hour FROM parking_slots ORDER BY slot_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<ParkingSlot> getAvailableSlots() throws SQLException {
        List<ParkingSlot> list = new ArrayList<>();
        String sql = "SELECT slot_id, slot_number, status, rate_per_hour FROM parking_slots " +
                     "WHERE status = 'Available' ORDER BY slot_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** ADMIN: create a new slot. */
    public boolean addSlot(String slotNumber, double ratePerHour) throws SQLException {
        String sql = "INSERT INTO parking_slots (slot_number, status, rate_per_hour) VALUES (?, 'Available', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotNumber);
            ps.setDouble(2, ratePerHour);
            return ps.executeUpdate() == 1;
        }
    }

    /** ADMIN: update slot number / rate. */
    public boolean updateSlot(int slotId, String slotNumber, double ratePerHour) throws SQLException {
        String sql = "UPDATE parking_slots SET slot_number = ?, rate_per_hour = ? WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotNumber);
            ps.setDouble(2, ratePerHour);
            ps.setInt(3, slotId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Used both by Admin (manual override) and by booking/release logic. */
    public boolean updateStatus(int slotId, String status) throws SQLException {
        String sql = "UPDATE parking_slots SET status = ? WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, slotId);
            return ps.executeUpdate() == 1;
        }
    }

    /** ADMIN: delete a slot. */
    public boolean deleteSlot(int slotId) throws SQLException {
        String sql = "DELETE FROM parking_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            return ps.executeUpdate() == 1;
        }
    }

    public ParkingSlot getSlotById(int slotId) throws SQLException {
        String sql = "SELECT slot_id, slot_number, status, rate_per_hour FROM parking_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private ParkingSlot mapRow(ResultSet rs) throws SQLException {
        return new ParkingSlot(
                rs.getInt("slot_id"),
                rs.getString("slot_number"),
                rs.getString("status"),
                rs.getDouble("rate_per_hour")
        );
    }
}
