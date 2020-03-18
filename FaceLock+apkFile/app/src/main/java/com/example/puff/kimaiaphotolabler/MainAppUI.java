package com.example.puff.kimaiaphotolabler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
 * MainAppUI
 * this is the applications main screen
 * here the user can flow either to adding a new photo (through gallery or camera) OR he can press on an entry in the list view
 * and flow to an entry-display view to see a more detailed (extra details not implemented) display of the selected instance
 * */
public class MainAppUI extends AppCompatActivity {
    private final String TAG = "MainAppUI"; //for logging
    //to distinguish between invoked views
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_PHOTO_GALLERY = 2;
    static final int DISPLAY_PHOTO = 3;
    //members
    ArrayList<LabeledPhoto> allEntries;
    Button addPicButton;
    ListView labledPhotosList;
    EditText photoTagsFilter;
    CustomListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app_ui);
        httpTest();
        setListeners();//wiring, listeners for the list, the textfield and the buttons
        refreshList(""); //to trigger a listview-populating event in the app,- calling this on an empty string returns all entries
    }

    private void setListeners(){
        Log.d(TAG, "setListeners");
        //search field
        photoTagsFilter = (EditText)findViewById(R.id.filterResultsEditText);
        photoTagsFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { //moot but necessary
                Log.d(TAG, "beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { //moot but necessary
                Log.d(TAG, "onTextChanged");
            }

            //invoked after each typing event to filter the list according to the users constraints, then refresh the display with the new results
            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged");
                if (editable.toString().isEmpty()) {
                    refreshList("");
                } else {
                    adapter.getFilter().filter(editable.toString());
                    adapter.notifyDataSetChanged();
                }
            }
        });

        labledPhotosList = (ListView) findViewById(R.id.listLabledPhotos);
        labledPhotosList.setTextFilterEnabled(true);

        addPicButton = (Button)findViewById(R.id.addPictureButton);
        addPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "add button clicked");
                Intent takePhotoOrUploadIntent = new Intent(MainAppUI.this, TakePhotoOrUploadActivity.class);
                startActivity(takePhotoOrUploadIntent);
            }
        });
        //loads an intent with the data members from the selected instance and invokes the Display Photo Activity with these parameters as payload
        labledPhotosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick");
                LabeledPhoto chosenPhoto = (LabeledPhoto) parent.getAdapter().getItem(position); //get selected instance
                Intent intent = new Intent(MainAppUI.this, DisplayPhotoActivity.class);
                intent.putExtra("Path", chosenPhoto.getPath());
                intent.putExtra("UID", chosenPhoto.getPhotoUID());
                intent.putExtra("Date", chosenPhoto.getDate());
                intent.putExtra("Tags", chosenPhoto.getTags());
                startActivityForResult(intent, DISPLAY_PHOTO);
            }
        });
    }


    //implemented inside "onResume"
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (((requestCode == REQUEST_IMAGE_CAPTURE) || (requestCode == REQUEST_PHOTO_GALLERY)) && resultCode == RESULT_OK) {
            //not needed since we have only one action to take for all current cases - so its implemented via the "onResume()" callback function
        }
    }
//refresh the display, to do so we reload the entries from our now updated database, and we repopulate the listview using a new adapter wired to the current data set
    private void refreshList(String filter) {
        Log.d(TAG, "refreshList");
        allEntries = com.example.puff.kimaiaphotolabler.LabeledPhoto.getAllLabledPhotosFromDB(this); //reload database
        adapter = new CustomListAdapter(this, allEntries); //recreate the adapter
        labledPhotosList.setAdapter(adapter); //re-set the list`s adapter
    }
    //in any case we returned to this view - we need to update the display to represent the current data set
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        refreshList("");
    }

    private void httpTest() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://www.google.com";// https://api-us.faceplusplus.com/facepp/v3/compare
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    MainAppUI.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            textView.setText(myResponse);
                        }
                    });
                }
            }
        });
    }
}
