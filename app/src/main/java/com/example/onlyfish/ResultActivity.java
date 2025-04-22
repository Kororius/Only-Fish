package com.example.onlyfish;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get data from the intent
        String imageUriString = getIntent().getStringExtra("imageUri");
        String text = getIntent().getStringExtra("text");
        String databaseId = getIntent().getStringExtra("databaseId"); // Example

        // Find views
        ImageView imageView = findViewById(R.id.result_image);
        TextView textView = findViewById(R.id.result_text);
        Button backToCameraButton = findViewById(R.id.back_to_camera_button); // Added
        Button backToHomeButton = findViewById(R.id.back_to_home_button); // Added

        // Load the image using Glide (or your preferred image loading library)
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            Glide.with(this)
                    .load(imageUri)
                    .into(imageView);
        }

        // Set the text
        textView.setText(text);

        // Set click listeners for the new buttons
        backToCameraButton.setOnClickListener(v -> {
            // Navigate back to CameraFragment
            finish(); // Simply finish the activity to return to the previous fragment (CameraFragment)
        });

        backToHomeButton.setOnClickListener(v -> {
            // Navigate back to HomeFragment
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
            startActivity(intent);
            finish(); // Close ResultActivity
        });
    }
}