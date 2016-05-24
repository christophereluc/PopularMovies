package com.christopherluc.popularmovies.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.christopherluc.popularmovies.BuildConfig;
import com.christopherluc.popularmovies.R;
import com.christopherluc.popularmovies.api.RetrieveMovies;
import com.christopherluc.popularmovies.data.Constants;
import com.christopherluc.popularmovies.data.Movie;
import com.christopherluc.popularmovies.data.MovieListResponse;
import com.christopherluc.popularmovies.fragment.MovieDetailFragment;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Activity that contains the recyclerview that displays movies
 */
public class MovieListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    Retrofit retrofit;
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private String mCurrentPath;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        mMovieAdapter = new MovieAdapter(savedInstanceState != null ? savedInstanceState.<Movie>getParcelableArrayList(Constants.EXTRA_MOVIE_LIST) : null);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setAdapter(mMovieAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mSwipeRefresh.setOnRefreshListener(this);
        mCurrentPath = savedInstanceState == null ? Constants.MOVIE_LIST_POPULAR_URL : savedInstanceState.getString(Constants.MOVIE_LIST_RATED_URL);
        retrofit = new Retrofit.Builder().baseUrl(Constants.MOVIE_LIST_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        if (savedInstanceState == null) {
            onRefresh();
        }
        if (findViewById(R.id.detail_layout) != null) {
            mTwoPane = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.EXTRA_MOVIE_LIST, mMovieAdapter.retrieveData());
        outState.putString(Constants.EXTRA_PATH, mCurrentPath);
    }

    @Override
    public void onRefresh() {
        mSwipeRefresh.setRefreshing(true);
        RetrieveMovies retrieveMovies = retrofit.create(RetrieveMovies.class);
        Call<MovieListResponse> call = retrieveMovies.getMovies(mCurrentPath, BuildConfig.MOVIE_DATABASE_KEY);
        call.enqueue(new Callback<MovieListResponse>() {
            @Override
            public void onResponse(Call<MovieListResponse> call, Response<MovieListResponse> response) {
                mMovieAdapter.addItems(response.body().results);
            }

            @Override
            public void onFailure(Call<MovieListResponse> call, Throwable t) {
                Toast.makeText(MovieListActivity.this, "Error getting data", Toast.LENGTH_LONG).show();
            }
        });
        mMovieAdapter.removeAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_movie_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_popular:
                mCurrentPath = Constants.MOVIE_LIST_POPULAR_URL;
                onRefresh();
                break;
            case R.id.action_rated:
                mCurrentPath = Constants.MOVIE_LIST_RATED_URL;
                onRefresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Recyclerview adapter
     */
    private class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

        private ArrayList<Movie> mDataSet;

        public MovieAdapter(@Nullable ArrayList<Movie> movies) {
            if (movies == null) {
                mDataSet = new ArrayList<>();
            } else {
                mDataSet = movies;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Movie movie = mDataSet.get(position);
            Uri uri = Uri.parse(Constants.BASE_IMAGE_URL).buildUpon().appendPath(movie.poster_path.replace("/", "")).build();
            Glide.clear(holder.itemView);
            Glide.with(holder.itemView.getContext()).load(uri).into((ImageView) holder.itemView);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        MovieDetailFragment fragment = MovieDetailFragment.newInstance(movie);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.detail_layout, fragment)
                                .commit();
                        return;
                    }
                    Intent i = new Intent(MovieListActivity.this, MovieDetailActivity.class).putExtra(Constants.EXTRA_MOVIE, movie);
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataSet != null ? mDataSet.size() : 0;
        }

        public ArrayList<Movie> retrieveData() {
            return mDataSet;
        }

        public void addItems(ArrayList<Movie> list) {
            if (list == null) {
                return;
            }
            mDataSet.addAll(list);
            notifyDataSetChanged();
        }

        public void removeAll() {
            mDataSet.clear();
            notifyDataSetChanged();
        }

        //Viewholder class to contain imageview
        protected class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
