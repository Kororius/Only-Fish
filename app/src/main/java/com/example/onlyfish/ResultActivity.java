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
        String fishName = getIntent().getStringExtra("fishName");
        double minLength = getIntent().getDoubleExtra("minLength", 0.0);
        double maxLength = getIntent().getDoubleExtra("maxLength", 0.0);
        String startDate = getIntent().getStringExtra("startDate");
        String endDate = getIntent().getStringExtra("endDate");
        int isBannedForever = getIntent().getIntExtra("isBannedForever", 0);
        String createdAt = getIntent().getStringExtra("createdAt");

        // Find views
        ImageView imageView = findViewById(R.id.result_image);
        TextView fishNameTextView = findViewById(R.id.fish_name_text);
        TextView minLengthTextView = findViewById(R.id.min_length_text);
        TextView maxLengthTextView = findViewById(R.id.max_length_text);
        TextView startDateTextView = findViewById(R.id.start_date_text);
        TextView endDateTextView = findViewById(R.id.end_date_text);
        TextView isBannedForeverTextView = findViewById(R.id.is_banned_forever_text);
        TextView createdAtTextView = findViewById(R.id.created_at_text);
        Button backToCameraButton = findViewById(R.id.back_to_camera_button);
        Button backToHomeButton = findViewById(R.id.back_to_home_button);

        // Load the image using Glide
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            Glide.with(this)
                    .load(imageUri)
                    .into(imageView);
        }

        // Set the text
        fishNameTextView.setText("Fish Name: " + fishName);
        minLengthTextView.setText("Min Length: " + minLength);
        maxLengthTextView.setText("Max Length: " + maxLength);
        startDateTextView.setText("Start Date: " + startDate);
        endDateTextView.setText("End Date: " + endDate);
        isBannedForeverTextView.setText("Is Banned Forever: " + (isBannedForever == 1 ? "Yes" : "No"));
        createdAtTextView.setText("Created At: " + createdAt);

        // Set click listeners for the buttons
        backToCameraButton.setOnClickListener(v -> {
            finish();
        });

        backToHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}