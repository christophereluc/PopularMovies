package com.christopherluc.popularmovies.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.christopherluc.popularmovies.R;
import com.christopherluc.popularmovies.api.json.Constants;
import com.christopherluc.popularmovies.api.json.Movie;
import com.christopherluc.popularmovies.fragment.MovieDetailFragment;

/**
 * Detail activity for selected movie
 */
public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Movie movie = getIntent().getParcelableExtra(Constants.EXTRA_MOVIE);
            if (movie != null) {
                MovieDetailFragment fragment = MovieDetailFragment.newInstance(movie);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.root_layout, fragment)
                        .commit();
            }
        }
        populateUI();
    }

    private void populateUI() {
        Movie movie = getIntent().getParcelableExtra(Constants.EXTRA_MOVIE);
        getSupportActionBar().setTitle(movie.title);
    }
}
