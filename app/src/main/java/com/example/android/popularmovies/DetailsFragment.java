package com.example.android.popularmovies;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DetailsFragment extends Fragment {

    private String movie;
    private ArrayList<String> trailers;
    private ArrayList<String> reviewsUrls;
    private ArrayAdapter<String> reviews;
    ToggleButton toggleButton;
    private FavouritesDB myDB;
    boolean isChecked;


    public DetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.details_fragment, container, false);

        Log.v("a7a", getArguments().getString(Intent.EXTRA_TEXT));
        movie = getArguments().getString(Intent.EXTRA_TEXT);
//            movie = intent.getStringExtra(Intent.EXTRA_TEXT);
        final String[] movieArray = movie.split(" %% ");
        String base = "http://image.tmdb.org/t/p/w185/";
        Picasso.with(getActivity()).load(base + movieArray[0]).into((ImageView) rootView.findViewById(R.id.movie_poster));
        ((TextView) rootView.findViewById(R.id.movie_title)).setText(movieArray[1]);
        ((TextView) rootView.findViewById(R.id.movie_description)).setText(movieArray[2]);
        ((TextView) rootView.findViewById(R.id.movie_release_date)).setText(movieArray[3]);
        ((TextView) rootView.findViewById(R.id.movie_user_rating)).setText(movieArray[4] + "/10");

        myDB = new FavouritesDB(getActivity());
        trailers = new ArrayList<>();
        reviewsUrls = new ArrayList<>();
        new FetchTrailers().execute(movieArray[5]);
        new FetchReviews().execute(movieArray[5]);
        final trailerArrayAdapter adapter = new trailerArrayAdapter(getActivity(), trailers);

        ExpandableHeightListView trailerList = (ExpandableHeightListView) rootView.findViewById(R.id.trailers);
        trailerList.setAdapter(adapter);
        trailerList.setExpanded(true);
        trailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailers.get(i))));
            }
        });

        ExpandableHeightListView reviewsList = (ExpandableHeightListView) rootView.findViewById(R.id.reviews);
        reviews = new ArrayAdapter<>(getActivity(), R.layout.review_list_item, R.id.review_item, new ArrayList<String>());
        reviewsList.setAdapter(reviews);
        reviewsList.setExpanded(true);
        reviewsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(reviewsUrls.get(i))));
            }
        });
        toggleButton = (ToggleButton) rootView.findViewById(R.id.favourite);
        isChecked = false;
        if (myDB.getData(Integer.parseInt(movieArray[5])).getCount() == 0 || !(myDB.getData(Integer.parseInt(movieArray[5])).moveToFirst())) {
            toggleButton.setChecked(isChecked);
            toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.favoritesoff));
        } else {
            isChecked = true;
            toggleButton.setChecked(isChecked);
            toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.favorites));
        }
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isChecked) {
                    isChecked = false;
                    myDB.deleteMovie(Integer.parseInt(movieArray[5]));
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.favoritesoff));
                }
                else {
                    isChecked = true;
                    myDB.insertMovie(movieArray[5], movie);
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.favorites));
                }
            }
        });


        return rootView;
    }


    public class FetchTrailers extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String trailerJsonString = null;
            String movieId = strings[0];

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY_VALUE = "api_key";

                Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(movieId)
                        .appendPath("trailers")
                        .appendQueryParameter(API_KEY_VALUE, BuildConfig.MOVIES_API_KEY).build();

                URL url = new URL(buildUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                trailerJsonString = buffer.toString();


            } catch (IOException ioException) {
                Log.e("Query error", ioException.getLocalizedMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Error closing stream", e.getLocalizedMessage());
                    }
                }
            }
            try {
                return parseTrailerJson(trailerJsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        private String[] parseTrailerJson(String trailerJsonString) throws JSONException {

            final String TRAILER_RESULTS = "youtube";
            final String KEY = "source";

            JSONObject reader = new JSONObject(trailerJsonString);
            JSONArray trailersArray = reader.getJSONArray(TRAILER_RESULTS);

            String[] results = new String[trailersArray.length()];
            for (int i = 0; i < trailersArray.length(); i++) {

                String key;

                JSONObject movie = trailersArray.getJSONObject(i);

                key = movie.getString(KEY);

                results[i] = key;
            }

            return results;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null && trailers != null) {
                trailers.clear();
                for (String trailer : strings) {
                    trailers.add("https://www.youtube.com/watch?v=" + trailer);
                }
            }
        }
    }

    public class FetchReviews extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String reviewsJsonString = null;
            String movieId = strings[0];

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY_VALUE = "api_key";

                Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(movieId)
                        .appendPath("reviews")
                        .appendQueryParameter(API_KEY_VALUE, BuildConfig.MOVIES_API_KEY).build();

                URL url = new URL(buildUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                reviewsJsonString = buffer.toString();


            } catch (IOException ioException) {
                Log.e("Query error", ioException.getLocalizedMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Error closing stream", e.getLocalizedMessage());
                    }
                }
            }
            try {
                return parseReviewJson(reviewsJsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }
        private String[] parseReviewJson(String reviewsJsonString) throws JSONException {

            final String REVIEW_RESULTS = "results";
            final String URL = "url";

            JSONObject reader = new JSONObject(reviewsJsonString);
            JSONArray reviewsArray = reader.getJSONArray(REVIEW_RESULTS);

            String[] results = new String[reviewsArray.length()];
            for (int i = 0; i < reviewsArray.length(); i++) {

                String url;

                JSONObject movie = reviewsArray.getJSONObject(i);

                url = movie.getString(URL);


                results[i] = url;


            }

            return results;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null && reviews != null) {
                reviews.clear();
                int i = 1;
                for (String review : strings) {
                    reviewsUrls.add(review);
                    reviews.add("Read Review" + String.valueOf(i));
                    i++;
                }
            }
        }
    }
}
