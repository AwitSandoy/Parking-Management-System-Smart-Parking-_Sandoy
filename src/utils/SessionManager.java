package utils;

import models.Session;
import models.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Handles the full lifecycle of the on-disk session file used for basic
 * session management via Java Serialization, as required by the capstone
 * activity.
 *
 * File written: "session.dat" in the project's working directory (the
 * folder IntelliJ runs the app from). This is intentionally a single,
 * fixed file rather than one-per-user, since this desktop app only ever
 * has one active logged-in user per running instance.
 */
public final class SessionManager {

    private static final String SESSION_FILE_NAME = "session.dat";

    private SessionManager() {
    }

    /**
     * Serializes a new Session object to disk right after a successful
     * login. Overwrites any previous session file if one exists.
     */
    public static boolean createSession(User user) {
        Session session = new Session(user.getId(), user.getUsername(), user.getRole());
        try (FileOutputStream fileOut = new FileOutputStream(SESSION_FILE_NAME);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            objectOut.writeObject(session);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to create session file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deserializes session.dat back into a Session object, or returns null
     * if no session file exists (e.g. no one is logged in) or it can't be
     * read (e.g. corrupted file).
     *
     * Controllers call this while navigating the app to confirm a session
     * is genuinely active, rather than trusting an in-memory reference
     * alone.
     */
    public static Session getActiveSession() {
        File file = new File(SESSION_FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream fileIn = new FileInputStream(file);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
            return (Session) objectIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to read session file: " + e.getMessage());
            return null;
        }
    }

    /** Quick check without fully deserializing the object. */
    public static boolean hasActiveSession() {
        return new File(SESSION_FILE_NAME).exists();
    }

    /**
     * Deletes session.dat from disk. Called on logout, and also on app
     * startup as a safety net in case a previous run crashed and left a
     * stale file behind.
     */
    public static boolean destroySession() {
        File file = new File(SESSION_FILE_NAME);
        if (!file.exists()) {
            return true; // nothing to delete, already "clean"
        }
        boolean deleted = file.delete();
        if (!deleted) {
            System.err.println("Failed to delete session file: " + file.getAbsolutePath());
        }
        return deleted;
    }
}
