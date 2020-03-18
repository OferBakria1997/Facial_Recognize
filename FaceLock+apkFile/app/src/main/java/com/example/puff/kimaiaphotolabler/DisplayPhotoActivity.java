package com.example.puff.kimaiaphotolabler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * DisplayPhotoActivity
 * this activity is used to display a chosen entry in the apps main list view
 * when a user clicks a list item in the main view - this activity is invoked
 * this activity receives the data used to populate itself via the calling intent`s "Extra" payload
 *
 * the class has almost no functionality, maybe except for the default back operation
 * */
public class DisplayPhotoActivity extends AppCompatActivity {
    ImageView displayedImage;
    private final String TAG = "DisplayPhotoActivity"; //for logging
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_photo);
        //getting sent payload
        Intent intent = getIntent();
        //populate image view
        displayedImage = (ImageView)findViewById(R.id.displayedImage);
        //compressing the image so it could be presented within an imageView
        Bitmap d = BitmapFactory.decodeFile(intent.getStringExtra("Path"));
        Bitmap scaled = Bitmap.createScaledBitmap(d, 640, 480, true);
        displayedImage.setImageBitmap(scaled);
        //populate text views with date and tags
        TextView date, Tags;
        date = (TextView)findViewById(R.id.textViewDate);
        Tags = (TextView)findViewById(R.id.textViewTags);
        date.setText(intent.getStringExtra("Date"));
        Tags.setText(intent.getStringExtra("Tags"));

    }
}
