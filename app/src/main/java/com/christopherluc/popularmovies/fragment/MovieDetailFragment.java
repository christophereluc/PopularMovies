package com.christopherluc.popularmovies.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.christopherluc.popularmovies.BuildConfig;
import com.christopherluc.popularmovies.R;
import com.christopherluc.popularmovies.api.ApiServiceGenerator;
import com.christopherluc.popularmovies.api.RetrieveMovieDataService;
import com.christopherluc.popularmovies.api.json.Constants;
import com.christopherluc.popularmovies.api.json.Movie;
import com.christopherluc.popularmovies.api.json.Review;
import com.christopherluc.popularmovies.api.json.ReviewListResponse;
import com.christopherluc.popularmovies.api.json.Video;
import com.christopherluc.popularmovies.api.json.VideoListResponse;
import com.christopherluc.popularmovies.data.FavoriteMovieAsyncQueryHandler;
import com.christopherluc.popularmovies.data.FavoriteMovieContract;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 * Fragment used to display more information about a movie
 *
 * Created by Chris on 5/23/2016.
 */
public class MovieDetailFragment extends Fragment implements FavoriteMovieAsyncQueryHandler.QueryCallback {

    @BindView(R.id.reviews)
    RecyclerView mRecyclerView;
    @BindView(R.id.favorite)
    Button mFavoriteButton;
    @BindView(R.id.date)
    TextView mDate;
    @BindView(R.id.poster)
    ImageView mPoster;
    @BindView(R.id.vote_average)
    TextView mVotes;
    @BindView(R.id.description)
    TextView mDescription;

    private ArrayList<Review> mReviewList = new ArrayList<>();
    private ArrayList<Video> mVideoList = new ArrayList<>();
    private DetailAdapter mAdapter;
    private Movie movie;
    //Null indicates no check has been done
    private Boolean mFavorite;

    private Unbinder mUnbinder;

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
        ButterKnife.bind(this, viewGroup);
        populateUI();
        setupRecyclerView();
        makeApiCalls();
        Boolean isFavorite = savedInstanceState != null ? savedInstanceState.getBoolean(Constants.EXTRA_FAVORITE) : null;
        if (isFavorite != null) {
            mFavoriteButton.setEnabled(true);
            mFavoriteButton.setText(isFavorite ? getString(R.string.favorite_button) : getString(R.string.remove_favorite));
        }
        else {
            checkIfFavorite();
        }
        return viewGroup;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFavorite != null) {
            outState.putBoolean(Constants.EXTRA_FAVORITE, mFavorite);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).getSupportActionBar().setTitle(movie.title);
        }
    }

    /**
     * Initializes the recyclerview used for displaying trailers and reviews
     */
    private void setupRecyclerView() {
        mAdapter = new DetailAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        manager.setAutoMeasureEnabled(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setNestedScrollingEnabled(true);
    }

    /**
     * Populates layout with movie data from the movie object
     */
    private void populateUI() {
        Bundle b = getArguments();
        if (b != null) {
            movie = b.getParcelable(Constants.EXTRA_MOVIE);
            if (movie != null) {
                Uri uri = Uri.parse(Constants.BASE_IMAGE_URL).buildUpon().appendPath(movie.poster_path.replace("/", "")).build();
                Glide.with(this).load(uri).into(mPoster);
                mDate.setText(movie.release_date.substring(0, 4));
                mVotes.setText(getString(R.string.rating_text, movie.vote_average));
                mDescription.setText(movie.overview);
            }
        }
    }

    /**
     * Helper method used to fire off all api calls
     */
    private void makeApiCalls() {
        if (movie == null) {
            return;
        }
        fetchReviews();
        fetchTrailers();
    }

    /**
     * Retrieves reviews for a given movie
     */
    private void fetchReviews() {
        RetrieveMovieDataService service = ApiServiceGenerator.createService(RetrieveMovieDataService.class);
        Call<ReviewListResponse> call = service.getReviews(movie.id, BuildConfig.MOVIE_DATABASE_KEY);
        call.enqueue(new Callback<ReviewListResponse>() {
            @Override
            public void onResponse(Call<ReviewListResponse> call, Response<ReviewListResponse> response) {
                if (response.isSuccessful()) {
                    mReviewList = response.body().results;
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ReviewListResponse> call, Throwable t) {
                //Do nothing on failure
            }
        });
    }

    /**
     * Uses retrofit to check for any trailers for the movie
     */
    private void fetchTrailers() {
        RetrieveMovieDataService service = ApiServiceGenerator.createService(RetrieveMovieDataService.class);
        Call<VideoListResponse> videoCall = service.getTrailers(movie.id, BuildConfig.MOVIE_DATABASE_KEY);
        videoCall.enqueue(new Callback<VideoListResponse>() {
            @Override
            public void onResponse(Call<VideoListResponse> call, Response<VideoListResponse> response) {
                if (response.isSuccessful()) {
                    mVideoList = response.body().results;
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<VideoListResponse> call, Throwable t) {
                //Do nothing on failure
            }
        });
    }

    @OnClick(R.id.favorite)
    public void onFavoriteClicked() {
        if (movie == null) {
            return;
        }
        mFavoriteButton.setEnabled(false);
        if (mFavorite == null || !mFavorite) {
            ContentValues movieValues = new ContentValues();

            movieValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID, movie.id);
            movieValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_OVERVIEW, movie.overview);
            movieValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH, movie.poster_path);
            movieValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE, movie.release_date);
            movieValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE, movie.title);
            movieValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_VOTE_AVERAGE, movie.vote_average);

            new FavoriteMovieAsyncQueryHandler(getContext().getContentResolver(), this)
                    .startInsert(0, null, FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI, movieValues);
        }
        else {
            new FavoriteMovieAsyncQueryHandler(getContext().getContentResolver(), this)
                    .startDelete(0, null, FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI,
                                 FavoriteMovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID + "= ?", new String[]{String.valueOf(movie.id)});
        }
    }

    /**
     * Checks if the passed in movie is a favorite movie
     */
    private void checkIfFavorite() {
        new FavoriteMovieAsyncQueryHandler(getContext().getContentResolver(), this).startQuery(
                0,
                null,
                FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI,
                null,
                FavoriteMovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movie.id)},
                null);
    }

    @Override
    public void onInsertComplete(boolean successful) {
        mFavoriteButton.setText(successful ? getString(R.string.remove_favorite) : getString(R.string.favorite_button));
        mFavorite = successful;
        mFavoriteButton.setEnabled(true);
    }

    @Override
    public void onDeleteComplete(boolean successful) {
        mFavoriteButton.setText(successful ? getString(R.string.favorite_button) : getString(R.string.remove_favorite));
        mFavorite = !successful;
        mFavoriteButton.setEnabled(true);
    }

    @Override
    public void onQueryComplete(Cursor cursor) {
        mFavoriteButton.setEnabled(true);
        if (cursor != null && cursor.moveToFirst()) {
            //We found that movie id, so we can assume its a favorite.
            mFavoriteButton.setText(getString(R.string.remove_favorite));
            mFavorite = true;
        }
        else {
            mFavorite = false;
            mFavoriteButton.setText(getString(R.string.favorite_button));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Adapter class used to display data from both Reviews AND trailer list
     */
    protected class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_VIDEO = 0;
        private static final int TYPE_REVIEW = 1;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType == TYPE_VIDEO ?
                    new TrailerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trailer, parent, false)) :
                    new ReviewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TrailerViewHolder) {
                final Video video = mVideoList.get(position);
                ((TrailerViewHolder) holder).trailer.setText(getString(R.string.trailer_number, position + 1));
                ((TrailerViewHolder) holder).trailer_root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://www.youtube.com/watch?v=" + video.key));
                        startActivity(i);
                    }
                });
            }
            else if (holder instanceof ReviewViewHolder) {
                Review review = mReviewList.get(position - mVideoList.size());
                ((ReviewViewHolder) holder).author.setText(getString(R.string.author_says, review.author));
                ((ReviewViewHolder) holder).review.setText(review.content);
            }
        }

        @Override
        public int getItemCount() {
            return mReviewList.size() + mVideoList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position > mVideoList.size() - 1) {
                return TYPE_REVIEW;
            }
            else {
                return TYPE_VIDEO;
            }
        }

        class TrailerViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.trailer_root)
            View trailer_root;
            @BindView(R.id.trailer)
            TextView trailer;

            public TrailerViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class ReviewViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.author)
            TextView author;
            @BindView(R.id.review)
            TextView review;

            public ReviewViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
