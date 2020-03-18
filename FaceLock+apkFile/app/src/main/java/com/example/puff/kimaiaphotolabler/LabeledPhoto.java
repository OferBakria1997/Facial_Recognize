package com.example.puff.kimaiaphotolabler;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
/*
 * LabeledPhoto
 * this class is our main "Model" in this application, we save and load our data from and to it through the application
 * since this class is basically the apps "native" data structure, it has minimal functionality needed to interact with the apps logic
 *
 * as an example - the class saves itself to the database, and can answer a basic question like "Do you contains one of these tags?"
 * */
public class LabeledPhoto {
    private static final String TAG = "LabeledPhoto"; //for logging
    //members
    String date, tags, Path, photoUID;
    public static final String TABLE_NAME = "labeledPhotos";
    //default ctor
    public LabeledPhoto(){
        Log.d(TAG, "default constructor");
    }
    //arguments ctor
    public LabeledPhoto(String inputedPath, String inputedTags, String inputedDate, String inputedPhotoUID){
        Log.d(TAG, "init arguments constructor");
        this.Path = inputedPath;
        this.tags = inputedTags;
        this.date = inputedDate;
        this.photoUID = inputedPhotoUID;
    }
    //invokes an insert query formatted with its own data members inside, then invokes a DBHelper`s execute with it
    public void saveToDB(Context context){
        Log.d(TAG, "saveToDB");
        String queryFull = "INSERT INTO labeledPhotos (photoUID, Path, Tags, dateTaken) values('" + this.photoUID + "', '" + this.Path + "', '" + this.tags + "', '" + this.date + "');";
        new DBHelper(context).getWritableDatabase().execSQL(queryFull);
    }
    //formats a "select all" query, then invokes a DBHelper`s execute with it
    public static ArrayList<LabeledPhoto> getAllLabledPhotosFromDB(Context context){
        Log.d(TAG, "getAllLabeledPhotosFromDB");
        ArrayList<LabeledPhoto> labeledPhotos = new ArrayList<>(); //result set
        String query = "SELECT * FROM labeledPhotos";
        Cursor c = new DBHelper(context).getReadableDatabase().rawQuery(query, null); //getting a cursor to the first entry
        c.moveToFirst(); //just to make sure, the default is we ge the first entry anyway
        //for each table entry - we create an instance of LabeledPhotos and push it into the result set
        while(c.isAfterLast() == false){
            labeledPhotos.add(new LabeledPhoto(
                    c.getString(c.getColumnIndex("Path")),
                    c.getString(c.getColumnIndex("Tags")),
                    c.getString(c.getColumnIndex("dateTaken")),
                    c.getString(c.getColumnIndex("photoUID"))));
            c.moveToNext();
        }
        return labeledPhotos;
    }
    //returns a boolean value representing the answer to the question: "does this instance contain ANY one of these given(denoted as inputString) tags?"
    //if any of hte given tags is present - returns true, otherwise returns false
    public boolean contains(String inputedString) {
        Log.d(TAG, "contains");
        boolean result = false;
        String[] searchedTags = inputedString.split(" ");
        for (int i = 0; i < searchedTags.length; i++) { //for each given tag
            if(getTags().contains(searchedTags[i])){ //is it contained within this instance`s tag list?
                result = true;
                break; //one hit is enough to qualify and make it into the result set
            }
        }
        Log.d(TAG, "contains recieved: " + inputedString + " and returned: " + result);
        return result;
    }
    //getters and setters
    public String getPhotoUID() {
        Log.d(TAG, "getPhotoUID");
        return photoUID;
    }
    public void setPhotoUID(String photoUID) {
        Log.d(TAG, "setPhotoUID");
        this.photoUID = photoUID;
    }
    public String getPath() {
        Log.d(TAG, "getPath");
        return Path;
    }
    public void setPath(String path) {
        Log.d(TAG, "setPath");
        Path = path;
    }
    public String getTags() {
        Log.d(TAG, "getTags");
        return tags;
    }
    public void setTags(String tags) {
        Log.d(TAG, "setTags");
        this.tags = tags;
    }
    public String getDate() {
        Log.d(TAG, "getDate");
        return date;
    }
    public void setDate(String date) {
        Log.d(TAG, "setDate");
        this.date = date;
    }
    public String toString(){
        Log.d(TAG, "toString");
        return date;
    }
}
