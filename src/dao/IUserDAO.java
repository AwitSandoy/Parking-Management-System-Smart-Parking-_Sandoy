package dao;

import models.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Abstraction for all "users" table data access.
 *
 * This interface exists specifically to apply the Dependency Inversion
 * Principle (DIP): controllers depend on THIS interface rather than the
 * concrete UserDAO class directly. That means a controller only knows
 * "something that can look up, register, and manage users" exists - it
 * has no idea (and doesn't need to care) whether that's backed by MySQL,
 * a mock/in-memory implementation for testing, or any other data source.
 */
public interface IUserDAO {

    boolean isUsernameAvailable(String username) throws SQLException;

    boolean register(String username, String plainPassword) throws SQLException;

    User login(String username, String plainPassword) throws SQLException;

    List<User> getAllUsers() throws SQLException;

    boolean addUser(String username, String plainPassword, String role) throws SQLException;

    boolean updateUserRole(int userId, String newRole) throws SQLException;

    boolean deleteUser(int userId) throws SQLException;
}
