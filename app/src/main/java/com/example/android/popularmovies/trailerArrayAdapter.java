package com.example.android.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class trailerArrayAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private ArrayList<String> adapter;

    public trailerArrayAdapter(Context context, ArrayList<String> trailers) {
        super(context, -1, trailers);
        mContext = context;
        this.adapter = new ArrayList<>();
        this.adapter = trailers;
    }
    public int getCount() {
        return adapter.size();
    }

    public String getItem(int position) {
        return adapter.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if(row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.trailer_list_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.text = (TextView) row.findViewById(R.id.play_text);
            holder.image = (ImageView) row.findViewById(R.id.play_logo);
            row.setTag(holder);

        }
        ViewHolder viewHolder = (ViewHolder) row.getTag();
        viewHolder.image.setImageResource(R.drawable.play);
        viewHolder.text.setText("Play Trailer " + String.valueOf(position + 1));

        return row;
    }
    static class ViewHolder {
        public TextView text;
        public ImageView image;
    }
}
