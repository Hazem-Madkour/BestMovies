package com.bestmovies.zoom.bestmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bestmovies.zoom.bestmovies.data.MoviesContract.MoviesColumns;

/**
 * Created by ZooM- on 12/27/2015.
 */
public class MoviesFavoriteGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    FavoriteGridAdapter adpGrid;
    GridView grdImages;
    Utility mUtility;
    String mViewType;
    int mPosition = GridView.INVALID_POSITION;

    private static final int FAVORITE_LOADER = 0;
    private static final String SELECTED_KEY = "SIPF";

    private static final String[] MOVIES_COLUMNS = {
            MoviesColumns.TABLE_NAME + "." + MoviesColumns._ID,
            MoviesColumns.COLUMN_KEY,
            MoviesColumns.COLUMN_TITLE,
            MoviesColumns.COLUMN_RELEASE_DATE,
            MoviesColumns.COLUMN_VOTE_AVERAGE,
            MoviesColumns.COLUMN_MOVIE_POSTER,
            MoviesColumns.COLUMN_OVERVIEW
    };
    static final int COL_MOVIE_KEY = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_RELEASE_DATE = 3;
    static final int COL_MOVIE_VOTE_AVERAGE = 4;
    static final int COL_MOVIE_POSTER = 5;
    static final int COL_MOVIE_OVERVIEW = 6;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies_grid, container, false);
        LinearLayout lytMvoiesGrid;
        lytMvoiesGrid = (LinearLayout) rootView.findViewById(R.id.lytMoviesGridFragment);
        if(lytMvoiesGrid == null)
        {
            lytMvoiesGrid = (LinearLayout) rootView.findViewById(R.id.lytMoviesGridFragmentWide);
            if(lytMvoiesGrid != null)
                lytMvoiesGrid.setBackgroundResource(R.drawable.backgroundfavoritewide);
        }
        else
            lytMvoiesGrid.setBackgroundResource(R.drawable.backgroundfavorite);
        adpGrid = new FavoriteGridAdapter(getContext(),null,0);
        grdImages = (GridView) rootView.findViewById(R.id.grdMovies);
        grdImages.setAdapter(adpGrid);
        mUtility = new Utility(getContext());
        configClicks();
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    private void configClicks(){
        grdImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Utility.sMovieObject = (MovieObject) (view.findViewById(R.id.imgGridPic).getTag());
                mPosition= i;
                if (Utility.sTwoPane) {
                    MovieDetailsFragment MDF = (MovieDetailsFragment) getActivity().getSupportFragmentManager().findFragmentByTag(MoviesGrid.MOVIE_DETAILS);
                    if (null != MDF) {
                        MDF.reInitFragment();
                    }
                } else {
                    Intent intentDetails = new Intent(getContext(), MovieDetails.class);
                    startActivity(intentDetails);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FAVORITE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        String ViewType = mUtility.getViewType();
        if(!(null != ViewType && ViewType== mViewType)) {
            mViewType = ViewType;
            getLoaderManager().restartLoader(FAVORITE_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = MoviesColumns.COLUMN_VOTE_AVERAGE + " DESC";
        Uri moviesUri = MoviesColumns.getMoviesUri();
        return new CursorLoader(getActivity(),
                moviesUri,
                MOVIES_COLUMNS,
                null,
                null,
                sortOrder);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adpGrid.swapCursor(data);
        Utility.sCreateNewGridFragment = true;
        if (mPosition != GridView.INVALID_POSITION)
        grdImages.smoothScrollToPosition(mPosition);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adpGrid.swapCursor(null);
    }

    public class FavoriteGridAdapter extends CursorAdapter{

        public FavoriteGridAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        private  ViewGroup.LayoutParams getParams(double widthDivision , View view,boolean needHeight)
        {
            int width = (int) (grdImages.getMeasuredWidth()/widthDivision);
            int height = (int) (1.3 * width);

            ViewGroup.LayoutParams params = view.getLayoutParams();
            if(needHeight)
                params.height = height;
            params.width = width;
            return params;
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = LayoutInflater.from(context).inflate(R.layout.grid_row, viewGroup, false);
            MovieGridHolder holder = new MovieGridHolder();
            holder.imgGridPic = (ImageView) view.findViewById(R.id.imgGridPic);
            holder.txtTitle = (TextView) view.findViewById(R.id.txtGridTitle);
            holder.txtReleaseDate = (TextView) view.findViewById(R.id.txtGridReleaseDate);

            view.setTag(holder);
            return view;
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            MovieGridHolder holder = (MovieGridHolder) view.getTag();
            double factor = 2.5;
            if(mViewType.equals(getResources().getString(R.string.hide_main_details_value)))
                factor = 2;
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                factor = 5;
                if(mViewType.equals(getResources().getString(R.string.hide_main_details_value)))
                    factor = 4;
            }
            holder.imgGridPic.setLayoutParams(getParams(factor, holder.imgGridPic, true));
            holder.imgGridPic.requestLayout();
            if(mViewType.equals(getResources().getString(R.string.show_main_details_value))) {
                ViewGroup.LayoutParams params = getParams(factor, holder.txtTitle, false);
                holder.txtTitle.setVisibility(View.VISIBLE);
                holder.txtReleaseDate.setVisibility(View.VISIBLE);
                holder.txtTitle.setLayoutParams(params);
                holder.txtTitle.setTextSize(15);
                holder.txtTitle.requestLayout();
                holder.txtReleaseDate.setLayoutParams(getParams(factor, holder.txtReleaseDate, false));
                holder.txtReleaseDate.requestLayout();
            }
            else{
                holder.txtTitle.setVisibility(View.GONE);
                holder.txtReleaseDate.setVisibility(View.GONE);
            }
            holder.imgGridPic.setTag(new MovieObject(
                    cursor.getString(COL_MOVIE_KEY),
                    cursor.getString(COL_MOVIE_TITLE),
                    cursor.getString(COL_MOVIE_RELEASE_DATE),
                    cursor.getString(COL_MOVIE_POSTER),
                    cursor.getString(COL_MOVIE_VOTE_AVERAGE),
                    cursor.getString(COL_MOVIE_OVERVIEW)
            ));
            try {
                holder.txtTitle.setText(cursor.getString(COL_MOVIE_TITLE));
                holder.txtReleaseDate.setText(cursor.getString(COL_MOVIE_RELEASE_DATE).split("-")[0]);
            }
            catch (Exception e){}
            holder.imgGridPic.setImageBitmap(mUtility.getImageBitmap(cursor.getString(COL_MOVIE_POSTER)));
        }

        class MovieGridHolder
        {
            ImageView imgGridPic;
            TextView txtTitle;
            TextView txtReleaseDate;
        }
    }
}
