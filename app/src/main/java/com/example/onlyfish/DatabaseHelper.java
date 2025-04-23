package com.example.onlyfish;

import android.util.Log;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Map<String, Object>> getAllRecords() {
//        List<Map<String, Object>> records = new ArrayList<>();
//        String query = "SELECT * FROM fishing_records"; // TODO: Replace with your actual table name
//
//        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
//             ResultSet resultSet = preparedStatement.executeQuery()) {
//
//            while (resultSet.next()) {
//                Map<String, Object> record = new HashMap<>();
//                record.put("id", resultSet.getInt("id")); // TODO: Replace with your actual column names
//                record.put("waterBody", resultSet.getString("water_body"));
//                record.put("fishCaught", resultSet.getInt("fish_caught"));
//                record.put("biggestFish", resultSet.getDouble("biggest_fish"));
//                Date sqlDate = resultSet.getDate("date");
//                if (sqlDate != null) {
//                    LocalDate localDate = sqlDate.toLocalDate();
//                    record.put("date", localDate);
//                } else {
//                    record.put("date", null);
//                }
//                records.add(record);
//            }
//        } catch (SQLException e) {
//            Log.e(TAG, "Error querying database: " + e.getMessage(), e);
//        }
//
//        return records;
        return createDummyData();

    }

    private List<Map<String, Object>> createDummyData() {
        List<Map<String, Object>> dummyRecords = new ArrayList<>();

        // Dummy record 1
        Map<String, Object> record1 = new HashMap<>();
        record1.put("id", 1);
        record1.put("waterBody", "Lake Serenity");
        record1.put("fishCaught", 5);
        record1.put("biggestFish", 2.5);
        record1.put("date", LocalDate.of(2024, 5, 10));
        dummyRecords.add(record1);

        // Dummy record 2
        Map<String, Object> record2 = new HashMap<>();
        record2.put("id", 2);
        record2.put("waterBody", "Crystal River");
        record2.put("fishCaught", 3);
        record2.put("biggestFish", 1.8);
        record2.put("date", LocalDate.of(2024, 5, 15));
        dummyRecords.add(record2);

        // Dummy record 3
        Map<String, Object> record3 = new HashMap<>();
        record3.put("id", 3);
        record3.put("waterBody", "Lake Serenity");
        record3.put("fishCaught", 7);
        record3.put("biggestFish", 3.2);
        record3.put("date", LocalDate.of(2024, 5, 20));
        dummyRecords.add(record3);

        // Dummy record 4
        Map<String, Object> record4 = new HashMap<>();
        record4.put("id", 4);
        record4.put("waterBody", "Crystal River");
        record4.put("fishCaught", 2);
        record4.put("biggestFish", 1.1);
        record4.put("date", LocalDate.of(2024, 5, 25));
        dummyRecords.add(record4);

        return dummyRecords;
    }
}