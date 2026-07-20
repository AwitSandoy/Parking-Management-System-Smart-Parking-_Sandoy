package models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents an active login session. This is the exact object that gets
 * written to disk (session.dat) via Java Serialization when a user logs in,
 * and deleted when they log out.
 *
 * We deliberately do NOT store the user's password (hashed or otherwise)
 * in this object - a session file only needs enough information to know
 * *who* is logged in and *what role* they have, not their credentials.
 */
public class Session implements Serializable {

    // Used by Java Serialization to check class compatibility between the
    // version that wrote the file and the version reading it back.
    private static final long serialVersionUID = 1L;

    private final int userId;
    private final String username;
    private final String role;
    private final String loginTime;

    public Session(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.loginTime = LocalDateTime.now().toString();
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "Session{user=" + username + ", role=" + role + ", loginTime=" + loginTime + "}";
    }
}
