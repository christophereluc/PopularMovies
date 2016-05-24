package com.christopherluc.popularmovies.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.christopherluc.popularmovies.BuildConfig;
import com.christopherluc.popularmovies.R;
import com.christopherluc.popularmovies.data.Constants;
import com.christopherluc.popularmovies.data.Movie;
import com.christopherluc.popularmovies.fragment.MovieDetailFragment;

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

/**
 * Activity that contains the recyclerview that displays movies
 */
public class MovieListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

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
        new MovieDownloader().execute(mCurrentPath);
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

    /**
     * AsyncTask used to download move info from the database
     */
    private class MovieDownloader extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = MovieDownloader.class.getName();

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieListString = null;
            String urlString = params[0];
            if (TextUtils.isEmpty(urlString)) {
                return null;
            }
            try {

                Uri builtUri = Uri.parse(Constants.MOVIE_LIST_BASE_URL).buildUpon()
                        .appendPath(urlString)
                        .appendQueryParameter(Constants.API_KEY_KEY, BuildConfig.MOVIE_DATABASE_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
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
                movieListString = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieListString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            mMovieAdapter.addItems(movies);
            mSwipeRefresh.setRefreshing(false);
        }

        /**
         * Take the String representing movies and generate a list containing movie objects.
         */
        private ArrayList<Movie> getMovieDataFromJson(String movieListString)
                throws JSONException {

            ArrayList<Movie> movieList = new ArrayList<>();
            JSONObject data = new JSONObject(movieListString);
            JSONArray movieListFromJson = data.getJSONArray(Constants.RESULTS_KEY);

            for (int i = 0; i < movieListFromJson.length(); i++) {
                try {
                    // Get the JSON object representing the day
                    JSONObject movieObject = movieListFromJson.getJSONObject(i);
                    movieList.add(new Movie(movieObject.getString(Constants.POSTER_PATH_KEY), movieObject.getString(Constants.OVERVIEW_KEY),
                            movieObject.getString(Constants.RELEASE_DATE_KEY), movieObject.getString(Constants.TITLE_KEY),
                            movieObject.getDouble(Constants.VOTE_AVERAGE_KEY)));
                } catch (JSONException e) {
                    //Skip
                }

            }
            return movieList;
        }
    }
}
