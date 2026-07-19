# Smart Parking Management System — Full Setup Guide

## Project Structure Overview

```
SmartParkingSystem/
├── database/
│   └── schema.sql                       <- run this in phpMyAdmin
├── src/
│   ├── application/
│   │   └── Main.java                    <- app entry point
│   ├── controllers/
│   │   ├── LoginRegisterController.java
│   │   ├── AdminDashboardController.java
│   │   └── CustomerDashboardController.java
│   ├── models/
│   │   ├── User.java
│   │   ├── ParkingSlot.java
│   │   └── Reservation.java
│   ├── dao/
│   │   ├── UserDAO.java
│   │   ├── ParkingSlotDAO.java
│   │   └── ReservationDAO.java
│   ├── utils/
│   │   ├── DatabaseConnection.java      <- JDBC connection factory
│   │   └── PasswordUtil.java            <- SHA-256 password hashing
│   └── views/
│       ├── LoginRegister.fxml
│       ├── AdminDashboard.fxml
│       └── CustomerDashboard.fxml
└── README.md
```

This is an **MVC layout**:
- **Model** → `models/` (plain data classes)
- **View** → `views/*.fxml` (Scene Builder–editable layouts)
- **Controller** → `controllers/` (UI logic, wired to FXML via `fx:controller`)
- `dao/` and `utils/` are the "persistence" layer the Controllers call into — this
  keeps SQL code completely out of your UI classes.

Default accounts seeded by `schema.sql`:

| Username | Password  | Role     |
|----------|-----------|----------|
| admin    | admin123  | Admin    |
| john     | john123   | Customer |

---

## Part A — Database & Server Setup (XAMPP & phpMyAdmin)

1. **Install XAMPP** (if not already installed) from https://www.apachefriends.org
   and complete the installer with default options.

2. **Open the XAMPP Control Panel.**
   - Click **Start** next to **Apache**.
   - Click **Start** next to **MySQL**.
   - You only need these two modules running (Apache serves phpMyAdmin's web
     pages; MySQL is the actual database server our Java app connects to).
   - Both rows should turn green and show a port number (Apache: 80/443,
     MySQL: 3306) once started.

3. **Open phpMyAdmin.**
   - In your browser, go to `http://localhost/phpmyadmin`.
   - You should see the phpMyAdmin home page (no login needed for the default
     XAMPP setup — it uses the `root` user with an empty password).

4. **Create the database and import the schema.**
   - Click the **SQL** tab at the top of phpMyAdmin (this lets you run a raw
     SQL script, no need to create the database by hand first).
   - Open the file `database/schema.sql` from this project in a text editor,
     copy its **entire contents**, and paste them into the SQL box in
     phpMyAdmin.
   - Click **Go**.
   - This single script will: create the `parking_system` database, create the
     three tables (`users`, `parking_slots`, `reservations`) with proper
     foreign keys, and insert default seed data (1 Admin, 1 Customer, 5
     parking slots).
   - Click on the `parking_system` database in the left sidebar and confirm
     you now see 3 tables, each with the expected rows.

> **Troubleshooting:** If MySQL fails to start in XAMPP (common on Windows if
> another MySQL instance or Skype is using port 3306), stop the conflicting
> service or change MySQL's port in XAMPP's `config` button, then update the
> `URL` constant in `DatabaseConnection.java` accordingly.

---

## Part B — Gathering the Dependencies

You need two things: the **JavaFX SDK** and the **MySQL Connector/J** JDBC driver.

### 1. JavaFX SDK
- Go to **https://gluonhq.com/products/javafx/**
- Under "Downloads", choose:
  - **Version:** a recent LTS build (e.g. JavaFX 21.x) — match it to the JDK
    version you have installed if possible.
  - **Operating System:** yours (Windows / macOS / Linux)
  - **Architecture:** x64 (or aarch64 for Apple Silicon)
  - **Type:** **SDK** (NOT "jmods" — we want the plain SDK zip containing a
    `lib` folder full of `.jar` files)
- Download the `.zip`, then **extract it** somewhere permanent, e.g.:
  - Windows: `C:\Java\javafx-sdk-21.0.x`
  - macOS/Linux: `/opt/javafx-sdk-21.0.x` or `~/javafx-sdk-21.0.x`
- Inside that folder you should see a `lib` subfolder containing files like
  `javafx.controls.jar`, `javafx.fxml.jar`, `javafx.graphics.jar`, etc.
  **This `lib` folder is what you will point IntelliJ to.**

### 2. MySQL Connector/J (JDBC Driver)
- Go to **https://dev.mysql.com/downloads/connector/j/**
- Under "Select Operating System", choose **"Platform Independent"**.
- Download the **ZIP Archive** (not the `.deb`/`.rpm`/`.msi` installers).
- Extract it. Inside you'll find a file like `mysql-connector-j-8.x.x.jar`.
  **Remember this file's location** — you'll add it as a library too.

---

### 3. Add the JavaFX and MySQL JARs as Libraries
- Go to **File → Project Structure** (or press `Ctrl+Alt+Shift+S` /
  `Cmd+;` on macOS).
- Select **Libraries** in the left list under "Project Settings".
- Click the **+** (plus) button → **Java**.
- Navigate to your extracted JavaFX SDK folder → open the **`lib`** folder →
  select **all the `.jar` files inside it** (Ctrl/Cmd-click or Ctrl+A) →
  click **OK**.
- When prompted "Add library to module", select your `SmartParkingSystem`
  module and click **OK**. You should now see a new library (e.g.
  `javafx-sdk-21.0.x`) listed, attached to your module.
- Click **+** again → **Java** → navigate to and select the
  `mysql-connector-j-8.x.x.jar` file you downloaded earlier → **OK** → attach
  it to your module the same way.
- Click **Apply**, then **OK** to close Project Structure.

At this point, your project should compile with zero red underlines on the
`import javafx.*` and `import java.sql.*` / MySQL-related lines.

---

## Part C — Configuring and Running the App

JavaFX is no longer bundled with the JDK, and since we're not using a build
tool, IntelliJ needs to be told explicitly how to launch the JavaFX runtime
via VM options.

### 1. Create a Run/Debug Configuration
- Open `Main.java` (in `application` package).
- Right-click inside the editor → **Modify Run Configuration...**
  (or click the dropdown near the Run button at the top → **Edit
  Configurations...** → click **+** → **Application**).
- Set:
  - **Name:** `Main`
  - **Main class:** `application.Main`
  - **Use classpath of module:** `SmartParkingSystem`

### 2. Add the required VM options
- In the same configuration dialog, find the **VM options** field (if it's
  not visible, click **Modify options** → check **Add VM options**).
- Paste the following, **replacing the path with your actual JavaFX `lib`
  folder path**:

  **Windows example:**
  ```
  --module-path "C:\Java\javafx-sdk-21.0.x\lib" --add-modules javafx.controls,javafx.fxml
  ```

  **macOS/Linux example:**
  ```
  --module-path "/opt/javafx-sdk-21.0.x/lib" --add-modules javafx.controls,javafx.fxml
  ```

- Click **Apply** → **OK**.

### 3. Verify your database credentials
- Open `src/utils/DatabaseConnection.java`.
- Confirm the constants match your XAMPP MySQL setup:
  ```java
  private static final String URL = "jdbc:mysql://localhost:3306/parking_system?useSSL=false&serverTimezone=UTC";
  private static final String USER = "root";
  private static final String PASSWORD = ""; // default XAMPP password is empty
  ```
- If you ever set a root password in MySQL/phpMyAdmin, update `PASSWORD` here
  to match.

### 4. Run it
- Make sure **Apache** and **MySQL** are still running in the XAMPP Control
  Panel.
- Click the green **Run** ▶ button in IntelliJ (or `Shift+F10`).
- The Login/Register window should appear.
- Log in with the seeded Admin account (`admin` / `admin123`) to see the
  Admin Dashboard, or the Customer account (`john` / `john123`) to see the
  Customer Dashboard — or register a brand-new Customer account from the
  login screen.

### Common Errors & Fixes

| Error | Fix |
|---|---|
| `Error: JavaFX runtime components are missing` | Your VM options weren't applied to the Run Configuration you're actually running — double-check Part D, Step 2. |
| `com.mysql.cj.jdbc.exceptions.CommunicationsException` / connection refused | MySQL isn't running in XAMPP, or the port/URL is wrong. |
| `Access denied for user 'root'@'localhost'` | Your MySQL root password isn't empty — update `PASSWORD` in `DatabaseConnection.java`. |
| `Unknown database 'parking_system'` | You didn't run `schema.sql` in phpMyAdmin yet, or ran it against the wrong server. |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | The MySQL Connector/J jar wasn't attached as a library to your module — redo Part C, Step 3. |
| FXML fails to load / `Location is not set` | The `views` folder wasn't marked correctly, or `src` isn't the Sources Root — confirm Part C, Step 2. |

---

## How the Security & Architecture Pieces Fit Together

- **SQL Injection prevention:** every single query in `dao/` uses
  `PreparedStatement` with `?` placeholders — user input is never concatenated
  into SQL strings.
- **Password safety:** passwords are hashed with SHA-256 (`PasswordUtil`)
  before being stored or compared; plain-text passwords never touch the
  database.
- **Connection safety:** `DatabaseConnection.getConnection()` returns a fresh
  connection every time, always used inside `try-with-resources`, so
  connections are never leaked even if an exception occurs mid-query.
- **Transactional integrity:** booking and releasing a slot
  (`ReservationDAO`) wrap the reservation + slot-status updates in a single
  JDBC transaction (`setAutoCommit(false)` + `commit()`/`rollback()`), so the
  two tables can never drift out of sync.
- **Role-based access:** `LoginRegisterController` routes Admins to
  `AdminDashboard.fxml` and Customers to `CustomerDashboard.fxml` after
  checking `User.getRole()` — Customers never even load the Admin screen's
  FXML or controller.