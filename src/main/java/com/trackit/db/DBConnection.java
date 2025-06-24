package com.trackit.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/trackit";
    private static final String USER = "root";
    private static final String PASSWORD = "q1A2I3M4!!";

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "UTC");
            props.setProperty("allowPublicKeyRetrieval", "true");

            connection = DriverManager.getConnection(URL, props); //connect
        } catch (SQLException e) {
            System.err.println("Database Connection Error: " + e.getMessage());
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null || instance.getConnection() == null) {
            instance = new DBConnection(); //agar null ho ya nh ho tab
        }
        return instance;
    } //syncronized means only one data enters at a time

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection: " + e.getMessage());
            throw new RuntimeException("Failed to get database connection", e);
        }
        return connection;
    }  

    public void closeConnection() { //for free up the memory if closes 
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
} 