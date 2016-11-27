package com.example.android.popularmovies;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class ImageAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private ArrayList<String> adapter;


    ImageAdapter(Context c, int layoutResourceId, ArrayList<String> adapter) {
        super(c, layoutResourceId , adapter);
        mContext = c;
        this.adapter = new ArrayList<>();
        this.adapter = adapter;
    }

    void setGridData(ArrayList<String> adapter) {
        this.adapter = adapter;
        notifyDataSetChanged();
    }

    public int getCount() {
        return adapter.size();
    }

    public String getItem(int position) {
        return adapter.get(position);
    }

    public long getItemId(int position)
    {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ImageView imageView;
        String base = "http://image.tmdb.org/t/p/w342/";

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }

        String s = getItem(position);
        assert s != null;
        String[] sArray = s.split(" - ");
        String url = sArray[0];

        Picasso.with(mContext).load(base + url).into(imageView);

        return imageView;
    }

}
