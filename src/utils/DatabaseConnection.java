package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
    DESCRIPTION :   This central place is responsible for creating JDBC connections to the MySQL "parking_system"
    database.

    DESIGN NOTE   :  Expose the single static factory method ("Singleton responsibility" for connection management)
    rather than caching one shared java.sql.Connection object. A single shared Connection is not safe to use from
    multiple parts of a JavaFX app (e.g. a background task and the UI thread at the same time), and once it is closed
    by one screen it becomes useless for every other screen. Instead, every DAO method opens a short-lived connection
    with try-with-resources and closes it immediately after use.

    SETUP NOTE :   Update these 3 constraints to match your XAMPP / MySQL configuration.
 */
public final class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/parking_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // default XAMPP MySQL root password is empty

    // Private constructor: this class is never instantiated.
    private DatabaseConnection() {
    }

    /*
      Opens and returns a brand-new JDBC connection.
      Always use this inside a try-with-resources block, e.g.:

        try (Connection conn = DatabaseConnection.getConnection()) {
            ...
        }
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
