package com.example.android.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements MovieListener {

    boolean twoPaneUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameLayout secondPanel = (FrameLayout) findViewById(R.id.second_panel);
        if(null == secondPanel) {
            twoPaneUI = false;
        }
        else
        twoPaneUI = true;

        if (savedInstanceState == null) {
            MoviesFragment moviesFragment = new MoviesFragment();
//            moviesFragment.setMovieListener(this);
            getSupportFragmentManager().beginTransaction().add(R.id.container, moviesFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void chooseMovie(String movie) {

        if (twoPaneUI) {
            DetailsFragment detailsFragment = new DetailsFragment();
            Bundle extras = new Bundle();
            extras.putString(Intent.EXTRA_TEXT, movie);
            detailsFragment.setArguments(extras);
            getFragmentManager().beginTransaction().replace(R.id.second_panel, detailsFragment).commit();
        }
        else {
            startActivity(new Intent(this, DetailsActivity.class).putExtra(Intent.EXTRA_TEXT, movie));
        }

    }
}
