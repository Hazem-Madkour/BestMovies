package com.bestmovies.zoom.bestmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesGridFragment extends Fragment {

    GridView grdMovies;
    GridAdapter adpMoviesGrid;
    ArrayList<MovieObject> lstMovies;
    Utility mUtility;
    String mSortingType;
    String mViewType;
    boolean mFailed;

    public MoviesGridFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies_grid,container,false);
        init(rootView);
        configClicks();
        return rootView;
    }

    private void init(View rootView){
        LinearLayout lytMvoiesGrid;
        lytMvoiesGrid = (LinearLayout) rootView.findViewById(R.id.lytMoviesGridFragment);
        if(lytMvoiesGrid == null)
        {
            lytMvoiesGrid = (LinearLayout) rootView.findViewById(R.id.lytMoviesGridFragmentWide);
            if(lytMvoiesGrid != null)
                lytMvoiesGrid.setBackgroundResource(R.drawable.backgroundmovieswide);
        }
        else
            lytMvoiesGrid.setBackgroundResource(R.drawable.backgroundmovies);

        mUtility = new Utility(getContext());
        grdMovies =(GridView) rootView.findViewById(R.id.grdMovies);
        lstMovies = new ArrayList<MovieObject>();
        adpMoviesGrid = new GridAdapter(getActivity(),lstMovies);
        grdMovies.setAdapter(adpMoviesGrid);
    }

    private void configClicks(){
        grdMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Utility.sMovieObject = lstMovies.get(i);
                    if (Utility.sTwoPane) {
                        MovieDetailsFragment MDF = (MovieDetailsFragment) getActivity().getSupportFragmentManager().findFragmentByTag(MoviesGrid.MOVIE_DETAILS);
                        if (null != MDF) {
                            MDF.reInitFragment();
                        }
                    } else {
                        Intent intentDetails = new Intent(getActivity(), MovieDetails.class);
                        startActivity(intentDetails);
                    }
                } catch (Exception e) {
                    Log.e("GridItemClick", e.getMessage());
                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        String SortingType = mUtility.getSortingType();
        String ViewType = mUtility.getViewType();
        if (!(null != mSortingType && SortingType == mSortingType)
                ||Utility.sCreateNewGridFragment)
        {
            mSortingType = SortingType;
            new FetchMovies().execute();
            Utility.sCreateNewGridFragment = false;
        }
        if(!(null != ViewType && ViewType== mViewType)) {
            mViewType = ViewType;
            adpMoviesGrid = new GridAdapter(getContext(),lstMovies);
            grdMovies.setAdapter(adpMoviesGrid);
        }
    }

    public class FetchMovies extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getContext(),"Loading Movies...",Toast.LENGTH_LONG).show();
        }

        private ArrayList<MovieObject> getMoviesDataFromJson(String strJsonMovies)
                throws JSONException {
            final String MDB_ID = "id";
            final String MDB_LIST = "results";
            final String MDB_POSTER = "poster_path";
            final String MDB_RELEASE_DATE = "release_date";
            final String MDB_OVERVIEW = "overview";
            final String MDB_TITLE = "original_title";
            final String MDB_VOTE_AVERAGE = "vote_average";

            JSONObject MoviesJson = new JSONObject(strJsonMovies);
            JSONArray MoviesArray = MoviesJson.getJSONArray(MDB_LIST);
            lstMovies.clear();
            String title,releaseDate,moviePoster,overview,id,voteAverage;
            for(int i = 0; i < MoviesArray.length(); i++) {
                JSONObject MovieJson = MoviesArray.getJSONObject(i);
                id = MovieJson.getString(MDB_ID);
                title = MovieJson.getString(MDB_TITLE);
                releaseDate = MovieJson.getString(MDB_RELEASE_DATE);
                moviePoster = MovieJson.getString(MDB_POSTER);
                overview = MovieJson.getString(MDB_OVERVIEW);
                voteAverage = MovieJson.getString(MDB_VOTE_AVERAGE);
                lstMovies.add(new MovieObject(id, title, releaseDate, moviePoster, voteAverage, overview));
                publishProgress();
            }
            return lstMovies;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String strJsonMovies = null;
            try {
                Uri.Builder uri = mUtility.getMoviesURI();
                URL url = new URL( uri.build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    mFailed = true;
                    return  null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    mFailed = true;
                }
                strJsonMovies = buffer.toString();
                getMoviesDataFromJson(strJsonMovies);

                return null;
            } catch (IOException e) {
                mFailed = true;
            } catch (JSONException e) {
                e.printStackTrace();
                mFailed = true;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(mFailed) {
                Toast.makeText(getContext(), "Please Check Your Internet Connection", Toast.LENGTH_LONG).show();
                mFailed =false;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            adpMoviesGrid.notifyDataSetChanged();
        }
    }

    public class GridAdapter extends BaseAdapter{
        private ArrayList<MovieObject> mListItems;
        private LayoutInflater mLayoutInflater;
        private Context mContext;

        public GridAdapter(Context context, ArrayList<MovieObject> arrayList){
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mListItems = arrayList;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mListItems.size();
        }

        @Override
        public Object getItem(int i) {
            return mListItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }
        private  ViewGroup.LayoutParams getParams(double widthDivision , View view,boolean needHeight)
        {
            int width = (int) (grdMovies.getMeasuredWidth()/widthDivision);
            int height = (int) (1.3 * width);

            ViewGroup.LayoutParams params = view.getLayoutParams();
            if(needHeight)
                params.height = height;
            params.width = width;
            return params;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View row = view;
            MovieGridHolder holder;
            if(row == null)
            {
                row = mLayoutInflater.inflate(R.layout.grid_row, viewGroup, false);
                holder = new MovieGridHolder();
                holder.imgGridPic = (ImageView) row.findViewById(R.id.imgGridPic);
                holder.txtTitle = (TextView) row.findViewById(R.id.txtGridTitle);
                holder.txtReleaseDate = (TextView) row.findViewById(R.id.txtGridReleaseDate);
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
                    ViewGroup.LayoutParams params = getParams(factor, holder.txtTitle, false) ;
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
                row.setTag(holder);
            }
            else
                holder = (MovieGridHolder) row.getTag();
            try {
                try{
                    holder.txtTitle.setText(mListItems.get(i).Title);
                    holder.txtReleaseDate.setText(mListItems.get(i).ReleaseDate.split("-")[0]);
                } catch (Exception e){}

                    Picasso.with(getActivity()).load(mUtility.IMAGE_URL +mListItems.get(i).MoviePoster).
                    placeholder(mContext.getResources().getDrawable(R.drawable.imgdefaultmovie))
                            .error(mContext.getResources().getDrawable(R.drawable.imgdefaultmovie)).into(holder.imgGridPic);
                return row;
            }
            catch (Exception e)
            {
                Log.e("GetViewInGridAdapter",e.getMessage());
                return  null;
            }
        }
        class MovieGridHolder
        {
            ImageView imgGridPic;
            TextView txtTitle;
            TextView txtReleaseDate;
        }
    }
}
