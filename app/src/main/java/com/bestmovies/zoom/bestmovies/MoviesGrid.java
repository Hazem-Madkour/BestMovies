package com.bestmovies.zoom.bestmovies;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class MoviesGrid extends FragmentActivity {

    final public static String MOVIE_DETAILS = "MVDT";

    private FragmentTabHost mTabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_grid);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(
                mTabHost.newTabSpec("Favorite").setIndicator("Favorite", null),
                MoviesFavoriteGridFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("All Movies").setIndicator("All Movies", null),
                MoviesGridFragment.class, null);
        if (findViewById(R.id.movie_details_container) != null) {
            Utility.sTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_details_container, new MovieDetailsFragment(), MOVIE_DETAILS)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movies_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(getApplicationContext(),SettingActivity.class);
            startActivity(settingIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
