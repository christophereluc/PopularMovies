package com.christopherluc.popularmovies.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.christopherluc.popularmovies.data.FavoriteMovieContract;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Chris on 5/23/2016.
 */
public class MovieDetailFragment extends Fragment {

    @BindView(R.id.reviews)
    RecyclerView mRecyclerView;

    private ArrayList<Review> mReviewList = new ArrayList<>();
    private ArrayList<Video> mVideoList = new ArrayList<>();
    private DetailAdapter mAdapter;
    private Movie movie;

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
        populateUI(viewGroup);
        setupRecyclerView();
        makeApiCalls();
        return viewGroup;
    }

    private void setupRecyclerView() {
        mAdapter = new DetailAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        manager.setAutoMeasureEnabled(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setNestedScrollingEnabled(true);
    }

    private void populateUI(ViewGroup viewGroup) {
        Bundle b = getArguments();
        if (b != null) {
            movie = b.getParcelable(Constants.EXTRA_MOVIE);
            if (movie != null) {
                Uri uri = Uri.parse(Constants.BASE_IMAGE_URL).buildUpon().appendPath(movie.poster_path.replace("/", "")).build();
                Glide.with(this).load(uri).into((ImageView) viewGroup.findViewById(R.id.poster));
                ((TextView) viewGroup.findViewById(R.id.date)).setText(movie.release_date.substring(0, 4));
                ((TextView) viewGroup.findViewById(R.id.vote_average)).setText(getString(R.string.rating_text, movie.vote_average));
                ((TextView) viewGroup.findViewById(R.id.description)).setText(movie.overview);
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

    private void onFavoriteClicked() {
        ContentValues weatherValues = new ContentValues();

        //new AsyncQueryHandler()
        weatherValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID, movie.id);
        weatherValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_OVERVIEW, movie.overview);
        weatherValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH, movie.poster_path);
        weatherValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE, movie.release_date);
        weatherValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE, movie.title);
        weatherValues.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_VOTE_AVERAGE, movie.vote_average);

        //getContext().getContentResolver().insert(Weath)
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
            } else if (holder instanceof ReviewViewHolder) {
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
            } else {
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
