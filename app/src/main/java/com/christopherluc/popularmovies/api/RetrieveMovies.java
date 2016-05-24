package com.christopherluc.popularmovies.api;

import com.christopherluc.popularmovies.data.MovieListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Chris on 5/23/2016.
 */
public interface RetrieveMovies {

    @GET("/{path}")
    public Call<MovieListResponse> getMovies(@Path("path") String path);
}
