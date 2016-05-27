package com.christopherluc.popularmovies.api;

import com.christopherluc.popularmovies.api.json.Constants;
import com.christopherluc.popularmovies.api.json.ReviewListResponse;
import com.christopherluc.popularmovies.api.json.VideoListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 *
 * Retrofit interface to retrieve either trailers or reviews for a movie
 *
 * Created by Chris on 5/24/2016.
 */
public interface RetrieveMovieDataService {

    @GET("/3/movie/{id}/videos")
    public Call<VideoListResponse> getTrailers(@Path("id") int path, @Query(Constants.API_KEY_KEY) String key);

    @GET("/3/movie/{id}/reviews")
    public Call<ReviewListResponse> getReviews(@Path("id") int path, @Query(Constants.API_KEY_KEY) String key);
}
