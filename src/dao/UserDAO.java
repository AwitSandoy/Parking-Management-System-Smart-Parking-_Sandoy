package dao;

import models.User;
import utils.DatabaseConnection;
import utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * All database access for the "users" table.
 * Every query uses PreparedStatement (never string concatenation) to
 * prevent SQL injection, and every Connection is opened/closed with
 * try-with-resources so nothing is ever leaked.
 */
public class UserDAO implements IUserDAO {

    /** Returns true if the username is not already taken. */
    public boolean isUsernameAvailable(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next();
            }
        }
    }

    /** Registers a new Customer account. Returns true on success. */
    public boolean register(String username, String plainPassword) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'Customer')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(plainPassword));
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Validates login credentials.
     * Returns the matching User object, or null if the username/password
     * combination is invalid. The plain password supplied by the user is
     * hashed and compared against the stored hash - the raw password is
     * never sent to, or compared inside, the database.
     */
    public User login(String username, String plainPassword) throws SQLException {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String suppliedHash = PasswordUtil.hash(plainPassword);
                    if (storedHash.equals(suppliedHash)) {
                        return new User(rs.getInt("id"), rs.getString("username"),
                                storedHash, rs.getString("role"));
                    }
                }
            }
        }
        return null;
    }

    /** ADMIN: list every user account. */
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, password, role FROM users ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new User(rs.getInt("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("role")));
            }
        }
        return list;
    }

    /** ADMIN: create a user with an explicit role (Admin or Customer). */
    public boolean addUser(String username, String plainPassword, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(plainPassword));
            ps.setString(3, role);
            return ps.executeUpdate() == 1;
        }
    }

    /** ADMIN: update a user's role (password left untouched). */
    public boolean updateUserRole(int userId, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /** ADMIN: delete a user account. */
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        }
    }
}
