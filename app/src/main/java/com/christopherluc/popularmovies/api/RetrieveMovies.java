package com.christopherluc.popularmovies.api;

import com.christopherluc.popularmovies.data.Constants;
import com.christopherluc.popularmovies.data.MovieListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Chris on 5/23/2016.
 */
public interface RetrieveMovies {

    @GET("/3/movie/{path}")
    public Call<MovieListResponse> getMovies(@Path("path") String path, @Query(Constants.API_KEY_KEY) String key);
}
