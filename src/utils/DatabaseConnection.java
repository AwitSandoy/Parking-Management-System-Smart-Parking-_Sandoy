package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*  Central place responsible for creating JDBC connections to the MySQL "parking_system" database.

    DESIGN PATTERN — SINGLETON (Creational):
    -   This class is implemented as a classic Singleton. Its constructor is private, so the only
    way to obtain the class's single instance is through the static getInstance() method,
    which lazily creates it on first use and returns that same instance on every later call.
    This guarantees there is exactly one object in the whole application responsible for
    knowing the database's URL/credentials and producing connections from them, instead of that
    configuration being duplicated or re-created in multiple places.

    NOTE ON CONNECTION LIFETIME (separate from the Singleton patternitself):
    -   The single DatabaseConnection instance does NOT hold onto one shared
    java.sql.Connection object. Each call to createConnection() still opens a fresh,
    short-lived JDBC connection, which every DAO uses inside try-with-resources. Sharing one
    live Connection across the app would be unsafe for a desktop app doing work from multiple
    screens, so the Singleton here manages *connection creation*, not a single long-lived connection.

    IMPORTANT NOTE: Update these 3 constants to match your XAMPP / MySQL setup.                         */
public final class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/parking_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // default XAMPP MySQL root password is empty.

    // The single instance of this class. Starts null; created the first time getInstance() is called (lazy initialization).
    private static DatabaseConnection instance;

    // Private constructor: prevents any other class from doing "new DatabaseConnection()" - the Singleton pattern's key rule.
    private DatabaseConnection() {
    }

    /*  Returns the single shared instance of DatabaseConnection,
        creating it the first time this is called. "synchronized" keeps
        this safe even if two threads happened to call it at once.          */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /*  Opens and returns a brand-new JDBC connection using this
        Singleton's configuration. Always use this inside a
        try-with-resources block.                                   */
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /*  Convenience static method kept so existing DAO code
        (DatabaseConnection.getConnection()) continues to work unchanged.
        Internally, it simply routes through the Singleton instance -
        every DAO is still ultimately going through the one
        DatabaseConnection object managed by getInstance().                 */
    public static Connection getConnection() throws SQLException {
        return getInstance().createConnection();
    }
}