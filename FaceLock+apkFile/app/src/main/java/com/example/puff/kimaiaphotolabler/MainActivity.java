package com.example.puff.kimaiaphotolabler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
/*
 * MainActivity
 * this view is actually set below all the other views by default, as it is not used as the main app screen, despite the name..
 * this screen is used to initialize helpers, and to implement some basic functionality that was not requested, but is useful for debugging and demonstrating purposes
 * this is not a production-level design, and is made mainly for the instructor`s comfort, to prevent bloating on the lst view, for example;
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity"; //for logging
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        deleteDB();
        Log.d(TAG, "oncreate");
        setContentView(R.layout.activity_main);
       Intent AppUI = new Intent(MainActivity.this, MainAppUI.class);
        startActivity(AppUI);
        //wiring the two buttons for delete and return
        Button gotoMainScreenButton = (Button)findViewById(R.id.testbutton);
        gotoMainScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//back to app main screen button listener
                Log.d(TAG, "goto App button pressed");
                Intent AppUI = new Intent(MainActivity.this, MainAppUI.class);
                startActivity(AppUI);
            }
        });
        Button deleteDB = (Button)findViewById(R.id.deleteDBButton);
        deleteDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//delete DB button listener
                Log.d(TAG, "delete DB button pressed");
                deleteDB();
                Toast.makeText(getApplicationContext(),"App Database Deleted" , Toast.LENGTH_SHORT).show();
                Intent AppUI = new Intent(MainActivity.this, MainAppUI.class);
                startActivity(AppUI);
            }
        });
        final EditText setArduinoIPEditText = (EditText)findViewById(R.id.setlockipedittext);
        setArduinoIPEditText.setText(ArduinoSingleton.getArduinoIP());

        Button setArduinoIPButton = (Button)findViewById(R.id.setlockipbutton);
        setArduinoIPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        Log.d(TAG, "set Arduino IP Button pressed");
        String newIP = setArduinoIPEditText.getText().toString();
        ArduinoSingleton.setArduinoIP(newIP);
        Toast.makeText(getApplicationContext(),"arduino ip set to: " + newIP, Toast.LENGTH_SHORT).show();
            }
        });
    }
    //as straightforward as can be
    private void deleteDB(){
        Log.d(TAG, "delete DB");
        this.deleteDatabase("kimaiaDB");
    }
}
