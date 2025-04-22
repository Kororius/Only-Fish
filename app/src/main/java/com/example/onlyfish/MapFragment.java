package com.example.onlyfish;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;

public class MapFragment extends Fragment {

    private MapView map;
    private static final String TAG = "MapFragment";
    private GeoPoint currentMapCenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Osmdroid configuration
        Context context = requireContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        // Map initialization
        map = view.findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // Set initial view to Lithuania
        IMapController mapController = map.getController();
        mapController.setZoom(9.0);
        GeoPoint startPoint = new GeoPoint(55.1694, 23.8813);
        mapController.setCenter(startPoint);
        currentMapCenter = startPoint;

        // Add red dot in the center
        addCenterDot();

        // Find the "Add Pin" button and set its click listener
        Button addPinButton = view.findViewById(R.id.add_pin_button);
        addPinButton.setOnClickListener(v -> showAddPinDialog());

        // Find the Home button and set its click listener
        Button homeButton = view.findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> navigateToHome());

        return view;
    }

    private void addCenterDot() {
        // Create a red dot drawable
        Drawable dotDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.red_dot); // You'll need to create this drawable
        if (dotDrawable == null) {
            Log.e(TAG, "Drawable 'red_dot' not found. Make sure it exists in your resources.");
            return;
        }

        // Convert drawable to bitmap
        Bitmap dotBitmap = Bitmap.createBitmap(dotDrawable.getIntrinsicWidth(), dotDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dotBitmap);
        dotDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        dotDrawable.draw(canvas);

        // Create an overlay to draw the dot
        Overlay dotOverlay = new Overlay() {
            @Override
            public void draw(Canvas c, MapView osmv, boolean shadow) {
                if (!shadow) {
                    GeoPoint center = (GeoPoint) osmv.getMapCenter();
                    android.graphics.Point point = osmv.getProjection().toPixels(center, null);
                    c.drawBitmap(dotBitmap, point.x - dotBitmap.getWidth() / 2f, point.y - dotBitmap.getHeight() / 2f, null);
                }
            }
        };

        map.getOverlays().add(dotOverlay);
        map.invalidate();
    }

    private void showAddPinDialog() {
        // Get the current map center as the pin location
        currentMapCenter = (GeoPoint) map.getMapCenter();

        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_pin, null);

        // Find the views in the dialog layout
        EditText pinNameEditText = dialogView.findViewById(R.id.pin_name_edit_text);
        EditText waterBodyEditText = dialogView.findViewById(R.id.water_body_edit_text);
        EditText fishCaughtEditText = dialogView.findViewById(R.id.fish_caught_edit_text);
        EditText biggestFishEditText = dialogView.findViewById(R.id.biggest_fish_edit_text);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        builder.setTitle("Add New Pin");

        // Set the positive button (Add)
        builder.setPositiveButton("Add", (dialog, which) -> {
            // Get the values from the input fields
            String pinName = pinNameEditText.getText().toString().trim();
            String waterBody = waterBodyEditText.getText().toString().trim();
            String fishCaughtStr = fishCaughtEditText.getText().toString().trim();
            String biggestFishStr = biggestFishEditText.getText().toString().trim();

            // Validate required fields
            if (pinName.isEmpty() || waterBody.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in the required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            int fishCaught = 0;
            if (!fishCaughtStr.isEmpty()) {
                try {
                    fishCaught = Integer.parseInt(fishCaughtStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid number for 'Fish Caught'.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            double biggestFish = 0.0;
            if (!biggestFishStr.isEmpty()) {
                try {
                    biggestFish = Double.parseDouble(biggestFishStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid number for 'Biggest Fish'.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Add the marker to the map
            addMarkerOnMap(pinName, currentMapCenter);

            // TODO: Save the data to the database (including user ID, location, and other details)
            savePinData(pinName, waterBody, fishCaught, biggestFish, currentMapCenter);

            Toast.makeText(requireContext(), "Pin added successfully!", Toast.LENGTH_SHORT).show();
        });

        // Set the negative button (Cancel)
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }

    private void savePinData(String pinName, String waterBody, int fishCaught, double biggestFish, GeoPoint location) {
        // TODO: Implement the database saving logic here
        // You'll need to:
        // 1. Get the user ID (if you have user authentication)
        // 2. Create a database entry with the provided data (pin name, water body, fish caught, biggest fish, location coordinates)
        // 3. Handle potential errors during the database operation

        // Example (replace with your actual database logic):
        Log.d(TAG, "Saving pin data: Name=" + pinName + ", WaterBody=" + waterBody +
                ", FishCaught=" + fishCaught + ", BiggestFish=" + biggestFish +
                ", Location=" + location.getLatitude() + ", " + location.getLongitude());

        // For now, just show a toast message
        Toast.makeText(requireContext(), "Data saved (simulated).", Toast.LENGTH_SHORT).show();
    }

    // Method to navigate back to the HomeFragment
    private void navigateToHome() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(); // This will remove the MapFragment from the stack and reveal the HomeFragment
        }
    }

    // Method to add a marker on the map
    private void addMarkerOnMap(String title, GeoPoint location) {
        Log.d(TAG, "Adding marker for: " + title);
        Marker marker = new Marker(map);
        marker.setPosition(location);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        map.getOverlays().add(marker);
        map.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        map = null;
    }
}