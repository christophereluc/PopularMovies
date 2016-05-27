package com.christopherluc.popularmovies.api.json;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * Movie object
 *
 * Created by Chris on 5/21/2016.
 */
public class Movie implements Parcelable{

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    public String poster_path;
    public String overview;
    public String release_date;
    public String title;
    public double vote_average;
    public int id;

    public Movie() {

    }

    public Movie(Parcel in) {
        poster_path = in.readString();
        overview = in.readString();
        release_date = in.readString();
        title = in.readString();
        vote_average = in.readDouble();
        id = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(poster_path);
        dest.writeString(overview);
        dest.writeString(release_date);
        dest.writeString(title);
        dest.writeDouble(vote_average);
        dest.writeInt(id);
    }
}
