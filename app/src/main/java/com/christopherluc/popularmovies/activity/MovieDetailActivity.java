package com.christopherluc.popularmovies.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.christopherluc.popularmovies.R;
import com.christopherluc.popularmovies.data.Constants;
import com.christopherluc.popularmovies.data.Movie;

/**
 * Detail activity for selected movie
 */
public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        populateUI();
    }

    private void populateUI() {
        Movie movie = getIntent().getParcelableExtra(Constants.EXTRA_MOVIE);
        Uri uri = Uri.parse(Constants.BASE_IMAGE_URL).buildUpon().appendPath(movie.poster_path.replace("/", "")).build();
        Glide.with(this).load(uri).into((ImageView) findViewById(R.id.poster));
        ((TextView) findViewById(R.id.date)).setText(movie.release_date.substring(0,4));
        ((TextView) findViewById(R.id.vote_average)).setText(getString(R.string.rating_text, movie.vote_average));
        ((TextView) findViewById(R.id.description)).setText(movie.overview);
        getSupportActionBar().setTitle(movie.title);
    }
}
