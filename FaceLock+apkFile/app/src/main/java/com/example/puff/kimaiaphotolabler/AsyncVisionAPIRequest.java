package com.example.puff.kimaiaphotolabler;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
/*
* AsyncVisionAPIRequest
* this class is used to send requests using the google vision API Asynchronously
 * it handles sending and receiving requests/responses from the server, as well as the loading animation, response parsing,
 * and playing a notification sound when the response has been fetched from the server.
 * it communicates the response back to its caller via a call back function to which it sends the parsed response.
* */
public class AsyncVisionAPIRequest extends AsyncTask<Void, Void, Void> {
    ProgressDialog progress;
    Context context;
    String pathToFile, response = "";;
    private final String TAG = "AsyncVisionAPIRequest"; //for logging
    TakePhotoOrUploadActivity parent;

    //ctor
    AsyncVisionAPIRequest(Context inputContext, String inputedPath, TakePhotoOrUploadActivity creator){
        super();
        parent = creator;
        context = inputContext;
        progress = new ProgressDialog(context);;
        pathToFile = inputedPath;
    }

    //
    @Override
    protected Void doInBackground(Void... voids) {
        response = "";
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);
        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("AIzaSyAxrCMSSEXY_0okB4JcwZTsfHXXwpdf2Xc"));

        final Vision vision = visionBuilder.build();
                // encoding a selected photo
                InputStream inputStream = null;
                byte[] photoData = null;
                try {
                    //compress file before sending
                    File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    Bitmap b = BitmapFactory.decodeFile(pathToFile);
                    //best result for this API are with 640x480
                    Bitmap out = Bitmap.createScaledBitmap(b, 640, 480, false);
                    //handle to resized file
                    File file = new File(dir, "resize.png");
                    FileOutputStream fOut;
                    try {//applying compression and converting to png format
                        fOut = new FileOutputStream(file);
                        out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                        b.recycle();
                        out.recycle();
                    } catch (Exception e) {
                        Log.d(TAG, "Exception");
                        e.printStackTrace();
                    }
                    inputStream = new FileInputStream(file);
                    //now we have an input stream with the file as binary
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "FileNotFoundException");
                    e.printStackTrace();
                }
                Log.d(TAG, "compressed file succesfuly");
                try {
                    if (inputStream != null) {//if everything went ok, we can start encoding the file to base64
                        photoData = IOUtils.toByteArray(inputStream); //first we convert it to a byte array
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "IOException");
                    e.printStackTrace();
                }
                  Log.d(TAG, "converted to byte array");

                 //we load the byte array to an image object, thankfully, it defaults to a base64 encoding
                Image inputImage = new Image();
                inputImage.encodeContent(photoData);
                //we start setting up the feature object containing the object and request parameters
                Feature labelDetection  = new Feature();
                //max 10 labels
                labelDetection.setMaxResults(10);
                //set request data type to labels
                labelDetection .setType("LABEL_DETECTION");
                AnnotateImageRequest request = new AnnotateImageRequest();
                //now that we have a base64 encoded image we can attach to the request, this is done by adding it as a Feature object`s payload
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(labelDetection ));
                //must be sent as a batch, but since we send each image separately, this batch object we always contain only one entry
                BatchAnnotateImagesRequest batchRequest =
                        new BatchAnnotateImagesRequest();
                //an array containing only one request
                batchRequest.setRequests(Arrays.asList(request));
                //handle to result batch
                BatchAnnotateImagesResponse batchResponse = null;
                Log.d(TAG, "request created");

                try {//here we send the requset to the server
                    Log.d(TAG, "sending request");
                    batchResponse = vision.images().annotate(batchRequest).execute();
                    Log.d(TAG, "response received");
                    //after response received - we load the labels into a container
                    List<EntityAnnotation> labels = batchResponse.getResponses().get(0).getLabelAnnotations();
                    int size, counter = 0;
                    size = labels.size();
                    for (EntityAnnotation entityAnnotation : labels){ //we extract the tags from within the entityAnnotation object we got in the result batch
                        counter++;
                        //this inserts the delimiter and space between tags
                        if (entityAnnotation.getDescription().compareTo("null") != 0){
                            if (counter == size){//if its the last one - no delimiter needed
                                response += entityAnnotation.getDescription();
                            } else { //otherwise add delimiter
                                response += entityAnnotation.getDescription() + ", ";
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, "IOException");
                    e.printStackTrace();
                }
        Log.d(TAG, "response parsed successfully");
        return null;
    }

    //show loading dialog while the operation is being performed
    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute");
        super.onPreExecute();
        progress.setTitle("Fetching Results From Server");
        progress.setMessage("Processing...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        //progress.show();
    }
    //dismiss loading dialog and notify parent that the action is done via a callback function
    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(TAG, "onPostExecute");
        super.onPostExecute(aVoid);
        //playFinishedLoadingSound();
        parent.responseReceived(response);
        //progress.dismiss();
        //Toast.makeText(context,"Identified Tags: " + response , Toast.LENGTH_SHORT).show();
    }

    //plays a default notification once the fetching process has finished and results are shown.
    public void playFinishedLoadingSound(){
        Log.d(TAG, "playFinishedLoadingSound");
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(context, defaultRingtoneUri);
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
