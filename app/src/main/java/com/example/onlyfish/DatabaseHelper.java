package com.example.onlyfish;

import android.util.Log;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";
    private Connection connection;

    public DatabaseHelper() {
        try {
            // Load environment variables from .env file
            Dotenv dotenv = Dotenv.configure().load();
            String dbHost = dotenv.get("DB_HOST");
            String dbPort = dotenv.get("DB_PORT");
            String dbName = dotenv.get("DB_NAME");
            String dbUser = dotenv.get("DB_USER");
            String dbPassword = dotenv.get("DB_PASSWORD");

            // Construct JDBC URL
            String jdbcUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;

            // Establish connection
            connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
            Log.d(TAG, "Database connection established successfully.");
        } catch (SQLException e) {
            Log.e(TAG, "Error establishing database connection: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error loading environment variables: " + e.getMessage(), e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Log.d(TAG, "Database connection closed.");
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error closing database connection: " + e.getMessage(), e);
        }
    }

    // Add your database interaction methods here (e.g., saveRecognitionResult, queryData)
    // Remember to use PreparedStatement for security.
}