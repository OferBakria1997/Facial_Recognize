package com.example.puff.kimaiaphotolabler;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/*
 * TakePhotoOrUploadActivity
 * this activity is a part of the "add picture" flow branch, its main purpose is to create a new LabeledPhoto instance, populated with relevant data, and save it to the DB.
 * it does so by first retrieving a path to a picture on the device, whether from gallery or by an immediate capture.
 * it then proceeds to send the image file to Google`s Cloud Vision Server for analysis,
 * once the results are returned, it populates the new LabeledPhoto instance with current data-  and saves it to the database.
 * */
public class TakePhotoOrUploadActivity extends AppCompatActivity {
    private static final String TAG = "TakeOrUploadActivity"; //for logging
    //to distinguish between invoked views
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_PHOTO_GALLERY = 2;
    static final int REQUEST_FACE_COMPARE = 3;
    //members
    Button cameraButton, uploadButton, compareButton, lockButton;
    String mCurrentPhotoPath;
    int sentPhotoIndex;
    LabeledPhoto currentPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_take_photo_or_upload);
        setListeners(); //wiring buttons
        currentPhoto = new LabeledPhoto();
    }
    //setting listeners for camera and gallery buttons
    private void setListeners(){
        Log.d(TAG, "setListeners");
        cameraButton = (Button)findViewById(R.id.takePhotoButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //invokes the take photo intent
                Log.d(TAG, "cameraButton");
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE);
            }
        });
//        uploadButton = (Button)findViewById(R.id.uploadPicButton);
//        uploadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) { //invokes the open gallery intent
//                Log.d(TAG, "galleryButton");
//                openGallery();
//            }
//        });
        lockButton = (Button)findViewById(R.id.lockButton);
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //invokes the open gallery intent
                ArduinoSingleton.getInstance().lock();
                Log.d(TAG,"LOCKING");
            }
        });
        compareButton = (Button)findViewById(R.id.comparePicButton);
        compareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //invokes the open gallery intent
                Log.d(TAG, "compareButton");
                dispatchTakePictureIntent(REQUEST_FACE_COMPARE);
            }
        });
    }
    //opens the gallery so tha the user could select a picture to upload
   private void openGallery() {
       Log.d(TAG, "openGallery");
       Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
      startActivityForResult(gallery, REQUEST_PHOTO_GALLERY);
   }

   //open a photo capture activity
    private void dispatchTakePictureIntent(int requestCode) {
        Log.d(TAG, "dispatchTakePictureIntent");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "IOException");
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created, using its URI
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

//this helper function creates an empty image file with current, relevant metadata, that`s to be populated with the newly captured picture
    private File createImageFile() throws IOException {
        Log.d(TAG, "createImageFile");
        // Create an image file name
        Date currentTimeStamp = new Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(currentTimeStamp);
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save file path
        mCurrentPhotoPath = image.getAbsolutePath();
        //populate relevant fields in the current labeledPhoto instance
        populatLabeledPhotoHelper(currentTimeStamp);
        return image;
    }

    //saves the newly created, now fully populated LabeledPhoto instance to the database with the received response
    public void saveNewEntryToDB(){
        Log.d(TAG, "saveNewEntryToDB");
        currentPhoto.setTags("Approved User"); //set response
        currentPhoto.saveToDB(TakePhotoOrUploadActivity.this);//save
        currentPhoto = new LabeledPhoto();// prepare for another use
    }

    //this sets the current image instance`s inner fields with relevant values
    //populates the path, UID and date using wither given or current date depending on the sent arguments
    private void populatLabeledPhotoHelper(Date inputedTimeStamp){
        Log.d(TAG, "populatLabeledPhotoHelper");
        Date currentTimeStamp;
        if (inputedTimeStamp == null) {//either current for gallery objects, or received for captured (it receives the time of capture to prevent any discrepancies)
            currentTimeStamp = new Date();
        } else {
            currentTimeStamp = inputedTimeStamp;
        }
        currentPhoto.setPath(mCurrentPhotoPath);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(currentTimeStamp); //set time
        currentPhoto.setPhotoUID(timeStamp); //set UID
        currentPhoto.setDate(formatDate(currentTimeStamp)); //set date string
    }
    //a little formating helper for the date string
    private String formatDate(Date timeStampToformat){
        Log.d(TAG, "formatDate");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = sdf.format(timeStampToformat).replace(" ", "  At: ");
        return formattedDate;
    }
    //converts a URI to an absolute path
    private String getRealPathFromURI(Uri contentUri) {
        Log.d(TAG, "getRealPathFromURI");
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(TakePhotoOrUploadActivity.this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    //after returning from the gallery or capture activity, we now have a valid path to an image resource,
    // all that's left is to send it for analysis and save the results to the instance/DB
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG, "REQUEST_IMAGE_CAPTURE");//we already have the absolute path from the intent itself, so we just send it
            //send to vision API
//            new AsyncVisionAPIRequest(this, mCurrentPhotoPath, this).execute();
            saveNewEntryToDB();

        }
        if (requestCode == REQUEST_PHOTO_GALLERY && resultCode == RESULT_OK) {
            Log.d(TAG, "REQUEST_PHOTO_GALLERY");
            Uri imageUri = data.getData(); //we extract the absolute path so we could send a file located anywhere (although it has to be registered to the gallery)
            mCurrentPhotoPath = getRealPathFromURI(imageUri);
            populatLabeledPhotoHelper(null);
            saveNewEntryToDB();
        }
        if (requestCode == REQUEST_FACE_COMPARE && resultCode == RESULT_OK) {
            Log.d(TAG, "REQUEST_FACE_COMPARE");//we already have the absolute path from the intent itself, so we just send it
            //send to face++ compare API
            sentPhotoIndex = 0;
            sendFaceCompareRequest(mCurrentPhotoPath, sentPhotoIndex);
        }
    }
    //this is a callback function invoked by the AsyncTask, when the results from the server are done loading
    // - it sends them back to the activity via this method, and triggers a saveToDB event to insert the new LabeledPhoto into the DB
    public void responseReceived(String fetchedResponse){
        Log.d(TAG, "responseReceived, response is: "+fetchedResponse);
        int res = Integer.valueOf(fetchedResponse);
        if (fetchedResponse == "-1") {
            playFinishedLoadingSound();
            Toast.makeText(this,"Success! unlocking..." , Toast.LENGTH_LONG).show();
        } else {
            sendFaceCompareRequest(mCurrentPhotoPath, res+1);
        }
    }

    private void sendFaceCompareRequest(String testedPhotoPath, int comparedUserPhotoIndex){
        Log.d(TAG, "sendFaceCompareRequest");

        ArrayList<LabeledPhoto> allEntries = com.example.puff.kimaiaphotolabler.LabeledPhoto.getAllLabledPhotosFromDB(this); //reload database
        if (comparedUserPhotoIndex < allEntries.size()) {
            Log.d(TAG, "checking approved user photo no."+comparedUserPhotoIndex);
            String approvedUserPhotoPath = allEntries.get(comparedUserPhotoIndex).Path;
            new AsyncFacePPRequest(this, this).execute(testedPhotoPath, approvedUserPhotoPath, String.valueOf(comparedUserPhotoIndex));
        } else {
            playFinishedLoadingSound();
            Toast.makeText(this,"Failed! please try again" , Toast.LENGTH_LONG).show();
        }
    }
    //plays a default notification once the fetching process has finished and results are shown.
    public void playFinishedLoadingSound(){
        Log.d(TAG, "playFinishedLoadingSound");
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(this, defaultRingtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    Log.d(TAG, "playFinishedLoadingSound(): onCompletion");
                    mp.release();
                }
            });
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "IllegalArgumentException");
            e.printStackTrace();
        } catch (SecurityException e) {
            Log.d(TAG, "SecurityException");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "IOException");
            e.printStackTrace();
        }
    }
}
