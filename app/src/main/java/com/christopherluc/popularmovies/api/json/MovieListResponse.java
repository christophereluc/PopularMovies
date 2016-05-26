package com.christopherluc.popularmovies.api.json;

import java.util.ArrayList;

/**
 * POJO Class to retrieve movies using Retrofit
 * <p/>
 * Created by Chris on 5/23/2016.
 */
public class MovieListResponse {

    public String page;
    public ArrayList<Movie> results;

}
