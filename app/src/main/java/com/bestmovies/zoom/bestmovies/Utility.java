package com.bestmovies.zoom.bestmovies;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.bestmovies.zoom.bestmovies.data.MoviesContract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;

/**
 * Created by ZooM- on 12/26/2015.
 */
public class Utility {
    static final String IMAGE_URL = "https://image.tmdb.org/t/p/w185";
    static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    static final String YOUTUBE_SEARCH_URL = "https://www.youtube.com/results?search_query=";
    static final String KEY = "PUT YOUR KEY";

    public static boolean sTwoPane = false;
    public static MovieObject sMovieObject;
    public static boolean sCreateNewGridFragment = false;

    Context mContext;

    public Utility(Context context)
    {
        mContext = context;
    }

    public String getSortingType(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String SortingType = sharedPreferences.getString(mContext.getString(R.string.sorting_key),mContext.getString(R.string.most_popular_value));
        return SortingType;
    }

    public String getViewType(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String viewType = sharedPreferences.getString(mContext.getString(R.string.viewtypekey), mContext.getString(R.string.hide_main_details_value));
        return viewType;
    }

    public Uri.Builder getMoviesURI(){
        Uri.Builder uri = new Uri.Builder();
        uri.scheme("http");
        uri.authority("api.themoviedb.org");
        uri.appendPath("3").appendPath("discover").appendPath("movie");
        uri.appendQueryParameter("sort_by", getSortingType()).appendQueryParameter("api_key", KEY);
        return uri;
    }

    public Uri.Builder getTrailersURI(String MovieId){
        Uri.Builder uri = new Uri.Builder();
        uri.scheme("http");
        uri.authority("api.themoviedb.org");
        uri.appendPath("3").appendPath("movie").appendPath(MovieId).appendPath("videos");
        uri.appendQueryParameter("api_key", KEY);
        return uri;
    }

    public Uri.Builder getReviewsURI(String MovieId){
        Uri.Builder uri = new Uri.Builder();
        uri.scheme("http");
        uri.authority("api.themoviedb.org");
        uri.appendPath("3").appendPath("movie").appendPath(MovieId).appendPath("reviews");
        uri.appendQueryParameter("api_key",KEY);
        return uri;
    }

    public Uri.Builder getInformationURI(String MovieId){
        Uri.Builder uri = new Uri.Builder();
        uri.scheme("http");
        uri.authority("api.themoviedb.org");
        uri.appendPath("3").appendPath("movie").appendPath(MovieId);
        uri.appendQueryParameter("api_key",KEY);
        return uri;
    }

    public boolean isMovieExist(String key){
        Cursor locationCursor = mContext.getContentResolver().query(
                MoviesContract.MoviesColumns.CONTENT_URI,
                null,
                MoviesContract.MoviesColumns.COLUMN_KEY + " = ?",
                new String[]{key},
                null);

        if (locationCursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public void saveImage(Bitmap bitmapImage, String imageName){
        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File path =new File(directory,imageName+".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getImageBitmap(String imageName){
        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        try {
            File f=new File(directory, imageName+".jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void removeImageBitmap(String imageName){
        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File path =new File(directory,imageName+".jpg");
        try {
            path.delete();
        }
        catch (Exception e) {e.printStackTrace();}
    }
}
