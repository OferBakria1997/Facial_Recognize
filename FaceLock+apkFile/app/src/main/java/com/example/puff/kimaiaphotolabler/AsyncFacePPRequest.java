package com.example.puff.kimaiaphotolabler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLException;


public class AsyncFacePPRequest extends AsyncTask<String, Void, Void> {
    private Exception exception;
    Context context;
    final String TAG = "AsyncFacePPRequest"; //for logging
    TakePhotoOrUploadActivity parent;
    String response = "";

public AsyncFacePPRequest(Context inputContext, TakePhotoOrUploadActivity creator){
    super();
    parent = creator;
    context = inputContext;
}

    protected Void doInBackground(String... photoPaths) {
            String photoPath = photoPaths[0];
            String photoPath2 = photoPaths[1];
            String inputPhotoIndex = photoPaths[2];
            Log.d(TAG, "constructor");
            Log.d(TAG, "photo path:"+photoPath);
            Log.d(TAG, "photo path2:"+photoPath2);


            Bitmap bmp = BitmapFactory.decodeFile(photoPath);
            Bitmap bmp2 = BitmapFactory.decodeFile(photoPath2);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 75, bos);
            bmp2.compress(Bitmap.CompressFormat.JPEG, 75, bos2);
            String base64 = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
            String base64_2 = Base64.encodeToString(bos2.toByteArray(), Base64.DEFAULT);
            byte[] buff = base64.getBytes();
            byte[] buff2 = base64_2.getBytes();

            String url = "https://api-us.faceplusplus.com/facepp/v3/compare";
            HashMap<String, String> map = new HashMap<>();
            HashMap<String, byte[]> byteMap = new HashMap<>();
            map.put("api_key", "PUrsZfTJ3XoQlLRJ1NXu98IkKhw4_Akp");
            map.put("api_secret", "AVAwF6r0g4IvE1jWvllKGQ6y1IgINoaX");

            map.put("image_base64_1", base64);
            map.put("image_base64_2", base64_2);
            Log.d(TAG,"BUFF IS: "+buff);
            Log.d(TAG,"BUFF2 IS: "+buff2);

            try{
                byte[] bacd = post(url, map, byteMap);
                String jsonStr = new String(bacd);
                Log.d(TAG,"COMPARE RESULT IS: "+jsonStr);
                // Parse the JSON and get the confidence value
                JSONObject obj = new JSONObject(jsonStr);
                double confidence;
                response = inputPhotoIndex;
                if (obj.has("confidence")) {
                    confidence = obj.getDouble("confidence");
                    Log.d(TAG,"CONFIDENCE IS:"+confidence);
                    if (confidence > 87.5) {
                        ArduinoSingleton.getInstance().unlock();
                        response = "-1";
                        Log.d(TAG,"UNLOCKING");
                    }
                }
            }catch (Exception e) {
                this.exception = e;
                e.printStackTrace();
                response = inputPhotoIndex;
            }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(TAG, "onPostExecute");
        super.onPostExecute(aVoid);
        //playFinishedLoadingSound();
        parent.responseReceived(response);
    }

    private final static int CONNECT_TIME_OUT = 30000;
    private final static int READ_OUT_TIME = 50000;
    private static String boundaryString = getBoundary();
    protected static byte[] post(String url, HashMap<String, String> map, HashMap<String, byte[]> fileMap) throws Exception {
        HttpURLConnection conne;
        URL url1 = new URL(url);
        conne = (HttpURLConnection) url1.openConnection();
        conne.setDoOutput(true);
        conne.setUseCaches(false);
        conne.setRequestMethod("POST");
        conne.setConnectTimeout(CONNECT_TIME_OUT);
        conne.setReadTimeout(READ_OUT_TIME);
        conne.setRequestProperty("accept", "*/*");
        conne.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
        conne.setRequestProperty("connection", "Keep-Alive");
        conne.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");
        DataOutputStream obos = new DataOutputStream(conne.getOutputStream());
        Iterator iter = map.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String, String> entry = (Map.Entry) iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            obos.writeBytes("--" + boundaryString + "\r\n");
            obos.writeBytes("Content-Disposition: form-data; name=\"" + key
                    + "\"\r\n");
            obos.writeBytes("\r\n");
            obos.writeBytes(value + "\r\n");
        }
        if(fileMap != null && fileMap.size() > 0){
            Iterator fileIter = fileMap.entrySet().iterator();
            while(fileIter.hasNext()){
                Map.Entry<String, byte[]> fileEntry = (Map.Entry<String, byte[]>) fileIter.next();
                obos.writeBytes("--" + boundaryString + "\r\n");
                obos.writeBytes("Content-Disposition: form-data; name=\"" + fileEntry.getKey()
                        + "\"; filename=\"" + encode(" ") + "\"\r\n");
                obos.writeBytes("\r\n");
                obos.write(fileEntry.getValue());
                obos.writeBytes("\r\n");
            }
        }
        obos.writeBytes("--" + boundaryString + "--" + "\r\n");
        obos.writeBytes("\r\n");
        obos.flush();
        obos.close();
        InputStream ins = null;
        int code = conne.getResponseCode();
        try{
            if(code == 200){
                ins = conne.getInputStream();
            }else{
                ins = conne.getErrorStream();
            }
        }catch (SSLException e){
            e.printStackTrace();
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        int len;
        while((len = ins.read(buff)) != -1){
            baos.write(buff, 0, len);
        }
        byte[] bytes = baos.toByteArray();
        ins.close();
        return bytes;
    }
    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 32; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length())));
        }
        return sb.toString();
    }
    private static String encode(String value) throws Exception{
        return URLEncoder.encode(value, "UTF-8");
    }

    public static byte[] getBytesFromFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
                out.write(b, 0, n);
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }
    public byte[] getCompressedBase64ImageFromFile(String pathToFile) {
        context = AppContextSingleton.getInstance().getApplicationContext();
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
//        Image inputImage = new Image();
//        inputImage.encodeContent(photoData);
        String base64 = Base64.encodeToString(photoData, Base64.DEFAULT);
        return base64.getBytes();
    }

}
