package com.example.puff.kimaiaphotolabler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/*
 * CustomListAdapter
 * this is a custom class used to populate each element (row) of the main listview with our custom list_item,
 * it then proceeds to populate each list item with our model`s (LabeledPhoto) data fields
 * */
public class CustomListAdapter extends ArrayAdapter<LabeledPhoto> implements Filterable{
    private final String TAG = "CustomListAdapter"; //for logging
    //members
    List<LabeledPhoto> mLabeledPhotos;
    List<LabeledPhoto> mLabeledPhotosFilterList;
    private ValueFilter valueFilter;
    LayoutInflater mInflater;

    //constructor takes in the activitt context and the list of our Model objects
    public CustomListAdapter(@NonNull Context context, @NonNull List<LabeledPhoto> objects) {
        super(context, R.layout.list_item, objects);
        Log.d(TAG, "constructor");
        mLabeledPhotosFilterList = objects;
        mLabeledPhotos = objects;
        mInflater = LayoutInflater.from(context);
        getFilter();
    }

    //How many items are in the data set represented by this Adapter.
    @Override
    public int getCount() {
        Log.d(TAG, "getCount");
        return mLabeledPhotos.size();
    }

    //Get the data item associated with the specified position in the data set.
    @Override
    public LabeledPhoto getItem(int position) {
        Log.d(TAG, "LabeledPhoto");
        return mLabeledPhotos.get(position);
    }

    //Returns a filter that can be used to constrain data with a filtering pattern.
    @Override
    public Filter getFilter() {
        Log.d(TAG, "getFilter");
        if(valueFilter==null) {
            valueFilter=new ValueFilter();
        }
        return valueFilter;
    }
    /*
     * getView - OVERRIDE
     * returns our already-data-filled instance of a list_item for the listview
     * */
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Log.d(TAG, "getView at position: " + position);

        if (convertView == null){ //at first run, we have no hanging instances of a row that we can recycle, so we create one
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
        }
        //wiring to list_item inner views
        TextView title = (TextView) convertView.findViewById(R.id.dateTitle);
        TextView subtitle = (TextView) convertView.findViewById(R.id.tagsSubtitle);
        ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
        //initializing views with the Model`s data
        LabeledPhoto currentlyRenderedPhoto = mLabeledPhotos.get(position);
        title.setText(currentlyRenderedPhoto.getDate());
        subtitle.setText(currentlyRenderedPhoto.getTags());
        //compressing the image so it could be presented within an imageView
        Bitmap d = BitmapFactory.decodeFile(currentlyRenderedPhoto.getPath());
        Bitmap scaled = Bitmap.createScaledBitmap(d, 200, 200, true);
        thumbnail.setImageBitmap(scaled);

        return convertView;
    }

    /*
      * getView - OVERRIDE
      * returns our already-data-filled instance of a list_item for the listview
      * */
    private class ValueFilter extends Filter {
        //Invoked in a worker thread to filter the data according to our inputed constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d(TAG, "FilterResults");
            FilterResults results=new FilterResults(); //result set
            if(constraint!=null && constraint.length()>0){ // validate non-null arguments to filter by
                ArrayList<LabeledPhoto> filterList=new ArrayList<LabeledPhoto>();//manipulated set - all the changes are commited no this set prior to its assignment as the result value
                for(int i=0;i<mLabeledPhotosFilterList.size();i++){ //iterate through all entries and remove those which dont conform to the inputed cnostraint
                    if(mLabeledPhotosFilterList.get(i).contains(constraint.toString())) { //we filter on whether the object contains a tag or not
                        filterList.add(mLabeledPhotosFilterList.get(i));
                    }
                }
                results.count=filterList.size();
                results.values=filterList;
            }else{ //if no arguments were given while invoked - return the full entries set
                results.count=mLabeledPhotosFilterList.size();
                results.values=mLabeledPhotosFilterList;
            }
            return results;
        }
        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.d(TAG, "publishResults");
            mLabeledPhotos =(ArrayList<LabeledPhoto>) results.values;
        }
}

}
