package com.example.android.popularmovies;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
import java.util.Collections;

public class MoviesFragment extends Fragment {

    ImageAdapter imageAdapter;
    ArrayList<String> data;
    String sort;
    FavouritesDB myDB;
    MovieListener movieListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_fragement, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        if (id == R.id.popular) {
            sort = "popular";
            data.clear();
            if(isOnline()) {

                new FetchMoviesData().execute(sort);
                Toast.makeText(getActivity(), "sorting by Most Popular",
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getActivity(), "No Internet Connection",
                        Toast.LENGTH_LONG).show();
            }

        }
        else if (id == R.id.topRated) {
            sort = "top_rated";
            data.clear();
            if(isOnline()) {

                new FetchMoviesData().execute(sort);
                Toast.makeText(getActivity(), "sorting by Top Rated",
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getActivity(), "No Internet Connection",
                        Toast.LENGTH_LONG).show();
            }
        }
        else {
            data = myDB.getAllMovies();
            imageAdapter.setGridData(data);
            if (data.size() == 0) {
                Toast.makeText(getActivity(), "You Do Not Have Any Favourite Movie",
                        Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    public MoviesFragment() {

        imageAdapter = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isOnline()) {
            if (sort == null) {
                sort = "popular";
                new FetchMoviesData().execute(sort);
            }

        } else {
            Toast.makeText(getActivity(), "No Internet Connection",
                    Toast.LENGTH_LONG).show();
        }

    }




    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.movies_fragment, container, false);
        myDB = new FavouritesDB(getActivity());
        data = new ArrayList<>();

        imageAdapter = new ImageAdapter(getActivity(), R.layout.grid_item_movie, data);

        GridView moviesList = (GridView) rootView.findViewById(R.id.movies_gridview);
        moviesList.setAdapter(imageAdapter);

        movieListener = (MainActivity) getActivity();

        moviesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                String movie = imageAdapter.getItem(position);
                movieListener.chooseMovie(movie);
            }
        });


        return rootView;

    }

    public class FetchMoviesData extends AsyncTask<String, Void, String[]> {


        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonString = null;

            String sort = params[0];



            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY_VALUE = "api_key";

                Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(sort)
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

                moviesJsonString = buffer.toString();


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
                return parseJson(moviesJsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        private String[] parseJson(String moviesJsonString) throws JSONException {

            final String MOV_RESULTS = "results";
            final String IMAGE_PATH = "poster_path";
            final String TITLE = "title";
            final String OVERVIEW = "overview";
            final String DATE = "release_date";
            final String RATING = "vote_average";
            final String ID = "id";

            JSONObject reader = new JSONObject(moviesJsonString);
            JSONArray moviesArray = reader.getJSONArray(MOV_RESULTS);

            String[] results = new String[moviesArray.length()];
            for (int i = 0; i < moviesArray.length(); i++) {

                String imagePath;
                String title;
                String overview;
                String releaseDate;
                String rating;
                String id;

                JSONObject movie = moviesArray.getJSONObject(i);

                imagePath = movie.getString(IMAGE_PATH);
                title = movie.getString(TITLE);
                overview = movie.getString(OVERVIEW);
                releaseDate = movie.getString(DATE);
                rating = movie.getString(RATING);
                id = movie.getString(ID);


                results[i] = imagePath + " %% " + title + " %% " + overview + " %% " + releaseDate + " %% "
                        + rating + " %% " + id;


            }

            return results;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            Collections.addAll(data, strings);
            imageAdapter.setGridData(data);
        }
    }
}
