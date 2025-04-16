package com.example.onlyfish;

import android.net.Uri;
import android.os.Bundle;
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

        // Load the image using Glide (or your preferred image loading library)
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            Glide.with(this)
                    .load(imageUri)
                    .into(imageView);
        }

        // Set the text
        textView.setText(text);
    }
}