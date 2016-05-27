package com.christopherluc.popularmovies.activity;

import android.content.Intent;
import android.database.Cursor;
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
import com.christopherluc.popularmovies.api.ApiServiceGenerator;
import com.christopherluc.popularmovies.api.RetrieveMoviesService;
import com.christopherluc.popularmovies.api.json.Constants;
import com.christopherluc.popularmovies.api.json.Movie;
import com.christopherluc.popularmovies.api.json.MovieListResponse;
import com.christopherluc.popularmovies.data.FavoriteMovieAsyncQueryHandler;
import com.christopherluc.popularmovies.data.FavoriteMovieContract;
import com.christopherluc.popularmovies.fragment.MovieDetailFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity that contains the recyclerview that displays movies
 */
public class MovieListActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener, FavoriteMovieAsyncQueryHandler.QueryCallback {

    static final int COL_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_RELEASE_DATE = 2;
    static final int COL_MOVIE_ID = 3;
    static final int COL_POSTER_PATH = 4;
    static final int COL_VOTE_AVERAGE = 5;
    static final int COL_OVERVIEW = 6;
    private static final String[] MOVIE_COLUMNS = {
            FavoriteMovieContract.FavoriteMovieEntry.TABLE_NAME + "." + FavoriteMovieContract.FavoriteMovieEntry._ID,
            FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE,
            FavoriteMovieContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE,
            FavoriteMovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID,
            FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH,
            FavoriteMovieContract.FavoriteMovieEntry.COLUMN_VOTE_AVERAGE,
            FavoriteMovieContract.FavoriteMovieEntry.COLUMN_OVERVIEW
    };
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mSwipeRefresh;

    private MovieAdapter mMovieAdapter;
    private String mCurrentPath;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        ButterKnife.bind(this);
        mMovieAdapter = new MovieAdapter(savedInstanceState != null ? savedInstanceState.<Movie>getParcelableArrayList(Constants.EXTRA_MOVIE_LIST) : null);
        mRecyclerView.setAdapter(mMovieAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mSwipeRefresh.setOnRefreshListener(this);
        mCurrentPath = savedInstanceState == null ? Constants.MOVIE_LIST_POPULAR_URL : savedInstanceState.getString(Constants.EXTRA_PATH);

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
        mMovieAdapter.removeAll();
        if (mCurrentPath != null && mCurrentPath.equals(Constants.MOVIE_LIST_FAVORITE)) {
            retrieveFavorites();
        }
        else {
            makeApiCall();
        }
    }

    private void makeApiCall() {
        RetrieveMoviesService service = ApiServiceGenerator.createService(RetrieveMoviesService.class);
        Call<MovieListResponse> call = service.getMovies(mCurrentPath, BuildConfig.MOVIE_DATABASE_KEY);
        call.enqueue(new Callback<MovieListResponse>() {
            @Override
            public void onResponse(Call<MovieListResponse> call, Response<MovieListResponse> response) {
                if (response.isSuccessful()) {
                    mMovieAdapter.addItems(response.body().results);
                } else {
                    Toast.makeText(MovieListActivity.this, "Error loading data", Toast.LENGTH_LONG).show();
                }
                mSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<MovieListResponse> call, Throwable t) {
                Toast.makeText(MovieListActivity.this, "Error loading data", Toast.LENGTH_LONG).show();
                mSwipeRefresh.setRefreshing(false);
            }
        });
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
            case R.id.action_favorites:
                mCurrentPath = Constants.MOVIE_LIST_FAVORITE;
                onRefresh();
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveFavorites() {
        new FavoriteMovieAsyncQueryHandler(getContentResolver(), this)
                .startQuery(0, null, FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI, MOVIE_COLUMNS, null, null, null);
    }

    @Override
    public void onInsertComplete(boolean successful) {
        //Do nothing
    }

    @Override
    public void onDeleteComplete(boolean successful) {
        //Do nothing
    }

    @Override
    public void onQueryComplete(Cursor cursor) {
        ArrayList<Movie> movieArrayList = new ArrayList<>();
        if (cursor != null && cursor.moveToPosition(0)) {
            do {
                Movie movie = new Movie();
                movie.id = cursor.getInt(COL_MOVIE_ID);
                movie.overview = cursor.getString(COL_OVERVIEW);
                movie.title = cursor.getString(COL_TITLE);
                movie.poster_path = cursor.getString(COL_POSTER_PATH);
                movie.vote_average = cursor.getFloat(COL_VOTE_AVERAGE);
                movie.release_date = cursor.getString(COL_RELEASE_DATE);
                movieArrayList.add(movie);
            } while (cursor.moveToNext());
            cursor.close();
        }
        mMovieAdapter.addItems(movieArrayList);
        mSwipeRefresh.setRefreshing(false);
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
