package com.christopherluc.popularmovies.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.christopherluc.popularmovies.R;
import com.christopherluc.popularmovies.data.Constants;
import com.christopherluc.popularmovies.data.Movie;

/**
 * Created by Chris on 5/23/2016.
 */
public class MovieDetailFragment extends Fragment {

    public static MovieDetailFragment newInstance(Movie movie) {
        Bundle b = new Bundle();
        b.putParcelable(Constants.EXTRA_MOVIE, movie);
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_movie_detail, container, false);
        populateUI(viewGroup);
        return viewGroup;
    }


    private void populateUI(ViewGroup viewGroup) {
        Bundle b = getArguments();
        if (b != null) {
            Movie movie = b.getParcelable(Constants.EXTRA_MOVIE);
            if (movie != null) {
                Uri uri = Uri.parse(Constants.BASE_IMAGE_URL).buildUpon().appendPath(movie.poster_path.replace("/", "")).build();
                Glide.with(this).load(uri).into((ImageView) viewGroup.findViewById(R.id.poster));
                ((TextView) viewGroup.findViewById(R.id.date)).setText(movie.release_date.substring(0, 4));
                ((TextView) viewGroup.findViewById(R.id.vote_average)).setText(getString(R.string.rating_text, movie.vote_average));
                ((TextView) viewGroup.findViewById(R.id.description)).setText(movie.overview);
            }
        }
    }
}
