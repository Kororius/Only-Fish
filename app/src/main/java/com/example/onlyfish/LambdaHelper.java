package com.example.onlyfish;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LambdaHelper {

    private static final String TAG = "LambdaHelper";
    private static final Logger log = LoggerFactory.getLogger(LambdaHelper.class);
    private static LambdaHelper instance;
    private final Context context;
    private ExecutorService executorService;
    private Map<LambdaFunction, String> lambdaUrls;
    private int userId = 1;
    private int hasLicense = 1;

    // Enum to represent different Lambda functions
    public enum LambdaFunction {
        GET_STATISTICS_DATA,
        GET_FISH_RULES,
        POST_REGISTRATION,
        GET_LICENCE,
        POST_LOGINS,
        POST_WATER_BODIES_AND_PINS,
        POST_LICENCE,
        GET_PINS
    }

    public interface LambdaResponseListener {
        void onResponse(String response);
        void onError(String error);
    }

    private LambdaHelper(Context context) {
        this.context = context.getApplicationContext();
        executorService = Executors.newFixedThreadPool(4);
        lambdaUrls = loadLambdaUrls();
    }

    public static synchronized LambdaHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LambdaHelper(context);
        }
        return instance;
    }

    public void sendRequestToLambda(LambdaFunction function, JSONObject requestBody, LambdaResponseListener listener) {
        executorService.submit(() -> {
            HttpURLConnection urlConnection = null;
            try {
                // Determine the correct Lambda URL based on the function
                String lambdaUrl = getLambdaUrl(function);
                if (lambdaUrl == null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onError("Invalid Lambda function specified.");
                    });
                    return;
                }

                URL url = new URL(lambdaUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                // Send the JSON payload
                OutputStream os = urlConnection.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.close();

                // Get the response
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Lambda HTTP IS OKKK!!!!");
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Notify the listener on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onResponse(response.toString());
                    });
                } else {
                    // Handle error response
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onError("HTTP error code: " + responseCode + " - " + errorResponse.toString());
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Error sending request to Lambda: " + e.getMessage(), e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onError("Error sending request to Lambda: " + e.getMessage());
                });
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    public void sendImageToLambda(LambdaFunction function, byte[] imageData, LambdaResponseListener listener) {
        executorService.submit(() -> {
            HttpURLConnection urlConnection = null;
            try {
                // Determine the correct Lambda URL based on the function
                String lambdaUrl = getLambdaUrl(function);
                if (lambdaUrl == null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onError("Invalid Lambda function specified.");
                    });
                    return;
                }

                URL url = new URL(lambdaUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/octet-stream"); // Or "image/jpeg"
                urlConnection.setDoOutput(true);

                // Send the image data
                OutputStream os = urlConnection.getOutputStream();
                os.write(imageData);
                os.close();

                // Get the response
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Lambda HTTP IS OKKK!!!!");
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Notify the listener on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onResponse(response.toString());
                    });
                } else {
                    // Handle error response
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onError("HTTP error code: " + responseCode + " - " + errorResponse.toString());
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Error sending request to Lambda: " + e.getMessage(), e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onError("Error sending request to Lambda: " + e.getMessage());
                });
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    private String getLambdaUrl(LambdaFunction function) {
        return lambdaUrls.get(function);
    }

    private Map<LambdaFunction, String> loadLambdaUrls() {
        Map<LambdaFunction, String> urls = new HashMap<>();
        AssetManager assetManager = context.getAssets();
        try (InputStream is = assetManager.open("api_endpoints.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    try {
                        LambdaFunction function = LambdaFunction.valueOf(parts[0]);
                        urls.put(function, parts[1]);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Invalid LambdaFunction in api_endpoints.txt: " + parts[0]);
                    }
                } else {
                    Log.e(TAG, "Invalid line format in api_endpoints.txt: " + line);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading api_endpoints.txt", e);
        }
        return urls;
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
    public void setHasLicense(int hasLicense) {
        this.hasLicense = hasLicense;
    }

    public int getHasLicense() {
        return hasLicense;
    }
}