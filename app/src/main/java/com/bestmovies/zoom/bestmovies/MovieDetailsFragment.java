package com.bestmovies.zoom.bestmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bestmovies.zoom.bestmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieDetailsFragment extends Fragment {

    public MovieDetailsFragment() {
    }

    final static int MOVIE_TRAILERS = 0;
    final static int MOVIE_REVIEWS = 1;
    final static int MOVIE_INFO = 2;
    LinearLayout mLayout ;
    TextView mTxtTitle,mTxtReleaseDate ,mTxtOverview, mTxtVoteAverage,mTxtReviews,mTxtDuration;
    Button mBtnShowTrailers,mBtnShowReviews;
    ImageView imgPoster;
    ListView lstTrailers;
    ToggleButton btnFavorite;
    ArrayList<String> lstTrailersNames,lstTrailersUrl,lstReviewsContent ;
    TrailersAdapter adpTrailersUrl;
    Utility mUtility;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        init(rootView);
        configClicks();
        if (Utility.sMovieObject == null) {
            mLayout.setVisibility(View.INVISIBLE);
            return rootView;
        }
        reInitFragment();
        if(!Utility.sTwoPane){
            mBtnShowReviews.setVisibility(View.INVISIBLE);
            mBtnShowTrailers.setVisibility(View.INVISIBLE);
            new FetchAttachments().execute(MOVIE_TRAILERS);
            new FetchAttachments().execute(MOVIE_INFO);
            new FetchAttachments().execute(MOVIE_REVIEWS);
        }
        return rootView;
    }

    public void reInitFragment(){
        mLayout.setVisibility(View.VISIBLE);
        mTxtDuration.setVisibility(View.GONE);
        mTxtTitle.setText(Utility.sMovieObject.Title);
        mTxtOverview.setText(Utility.sMovieObject.Overview);
        mTxtVoteAverage.setText(Utility.sMovieObject.VoteAverage + "/10");
        try {
            mTxtReleaseDate.setText(Utility.sMovieObject.ReleaseDate.split("-")[0]);
        }
        catch(Exception e){}
        mTxtReviews.setText("");
        lstTrailersUrl.clear();
        lstReviewsContent.clear();
        lstTrailersNames.clear();
        adpTrailersUrl.notifyDataSetChanged();
        configListViewHeight(lstTrailers);
        if(mUtility.isMovieExist(Utility.sMovieObject.Id))
        {
            imgPoster.setImageBitmap(mUtility.getImageBitmap(Utility.sMovieObject.MoviePoster));
            btnFavorite.setChecked(true);
        }
        else {
            Picasso.with(getActivity()).load(mUtility.IMAGE_URL + Utility.sMovieObject.MoviePoster).
                    placeholder(getContext().getResources().getDrawable(R.drawable.imgdefaultmovie))
                    .error(getContext().getResources().getDrawable(R.drawable.imgdefaultmovie)).into(imgPoster);
            btnFavorite.setChecked(false);
        }
        new FetchAttachments().execute(MOVIE_INFO);
    }

    private void init(View mRootView){
        lstTrailersNames = new ArrayList<String>();
        lstTrailersUrl = new ArrayList<String>();
        lstReviewsContent = new ArrayList<String>();
        adpTrailersUrl = new TrailersAdapter (getContext(), R.layout.list_row, lstTrailersNames);
        mUtility = new Utility(getContext());

        lstTrailers = (ListView) mRootView.findViewById(R.id.lstTrailers);
        lstTrailers.setAdapter(adpTrailersUrl);

        mLayout = (LinearLayout)mRootView.findViewById(R.id.lytMovieDetails);
        mTxtTitle = (TextView) mRootView.findViewById(R.id.txtTitle);
        mTxtReleaseDate = (TextView) mRootView.findViewById(R.id.txtReleaseDate);
        mTxtOverview = (TextView) mRootView.findViewById(R.id.txtOverview);
        mTxtVoteAverage = (TextView) mRootView.findViewById(R.id.txtVoteAverage);
        mTxtReviews = (TextView) mRootView.findViewById(R.id.txtReviews);
        mTxtDuration = (TextView) mRootView.findViewById(R.id.txtDuration);
        imgPoster = (ImageView) mRootView.findViewById(R.id.imgPoster);
        btnFavorite = (ToggleButton) mRootView.findViewById(R.id.btnFavorite);
        mBtnShowReviews = (Button) mRootView.findViewById(R.id.btnShowReviews);
        mBtnShowTrailers = (Button) mRootView.findViewById(R.id.btnShowTrailers);
        mTxtTitle.setFocusable(true);
        mTxtTitle.setFocusableInTouchMode(true);
    }

    private void configClicks(){

        lstTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(lstTrailersUrl.get(i)));
                startActivity(intent);
            }
        });

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnFavorite.isChecked()) {
                    ContentValues MoviesValues = new ContentValues();

                    MoviesValues.put(MoviesContract.MoviesColumns.COLUMN_KEY, Utility.sMovieObject.Id);
                    MoviesValues.put(MoviesContract.MoviesColumns.COLUMN_OVERVIEW, Utility.sMovieObject.Overview);
                    MoviesValues.put(MoviesContract.MoviesColumns.COLUMN_RELEASE_DATE, Utility.sMovieObject.ReleaseDate);
                    MoviesValues.put(MoviesContract.MoviesColumns.COLUMN_TITLE, Utility.sMovieObject.Title);
                    MoviesValues.put(MoviesContract.MoviesColumns.COLUMN_VOTE_AVERAGE, Utility.sMovieObject.VoteAverage);
                    MoviesValues.put(MoviesContract.MoviesColumns.COLUMN_MOVIE_POSTER, Utility.sMovieObject.MoviePoster);
                    mUtility.saveImage(((BitmapDrawable) imgPoster.getDrawable()).getBitmap(), Utility.sMovieObject.MoviePoster);
                    getContext().getContentResolver().insert(MoviesContract.MoviesColumns.CONTENT_URI, MoviesValues);
                } else {
                    String selection = MoviesContract.MoviesColumns.COLUMN_KEY + " = ?";
                    String[] selectionArgs = {String.valueOf(Utility.sMovieObject.Id)};
                    getContext().getContentResolver().delete(MoviesContract.MoviesColumns.CONTENT_URI, selection, selectionArgs);
                    mUtility.removeImageBitmap(Utility.sMovieObject.MoviePoster);
                }
            }
        });
        mBtnShowReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lstTrailersUrl.size()==0)
                new FetchAttachments().execute(MOVIE_REVIEWS);
            }
        });
        mBtnShowTrailers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchAttachments().execute(MOVIE_TRAILERS);
            }
        });
    }

    private void configListViewHeight(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems ; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();
            if(!Utility.sTwoPane)
            mTxtTitle.requestFocus();
        }
    }

    public class FetchAttachments extends AsyncTask<Integer,Void, Void>
    {
        String strDuration;
        public void fillReviewsList(String strJson)
        {
            try {
                JSONObject Reviews = new JSONObject(strJson);
                JSONArray ReviewsArray = Reviews.getJSONArray("results");
                lstReviewsContent.clear();
                for(int i = 0; i < ReviewsArray.length(); i++) {
                    String Content =  ReviewsArray.getJSONObject(i).getString("content")
                            + "\r\n\r\nAuthor : " + ReviewsArray.getJSONObject(i).getString("author");
                    lstReviewsContent.add(Content);
                }
            }
            catch (Exception e){}
        }

        public void fillTrailersList(String strJson)
        {
            try {
                JSONObject Trailers = new JSONObject(strJson);
                JSONArray TrailersArray = Trailers.getJSONArray("results");
                lstTrailersNames.clear();
                lstTrailersUrl.clear();

                if(TrailersArray.length() == 0) {
                    lstTrailersUrl.add(mUtility.YOUTUBE_SEARCH_URL + mTxtTitle.getText());
                    lstTrailersNames.add("No trailers exist, Click to search in Youtube");
                }
                else {
                    for (int i = 0; i < TrailersArray.length(); i++) {
                        String Key = TrailersArray.getJSONObject(i).getString("key");
                        String name = TrailersArray.getJSONObject(i).getString("name");
                        lstTrailersUrl.add(mUtility.YOUTUBE_URL + Key);
                        lstTrailersNames.add(name);
                    }
                }
            }
            catch (Exception e){}
        }

        public void fillINFOList (String strJson){
            try {
                JSONObject Info = new JSONObject(strJson);
                strDuration= Info.getString("runtime");
            }
            catch (Exception e){}
        }
        int iAttachmentType ;
        @Override
        protected Void doInBackground(Integer... ints) {
            iAttachmentType = ints[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String strJsonMovies = null;
            try {
                Uri.Builder uri;
                if(iAttachmentType == MOVIE_TRAILERS)
                    uri = mUtility.getTrailersURI(Utility.sMovieObject.Id);
                else if(iAttachmentType == MOVIE_REVIEWS)
                    uri = mUtility.getReviewsURI(Utility.sMovieObject.Id);
                else
                    uri = mUtility.getInformationURI(Utility.sMovieObject.Id);
                URL url = new URL( uri.build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return  null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    strJsonMovies = null;
                }
                strJsonMovies = buffer.toString();
                Log.v("JSON", strJsonMovies);
                if(iAttachmentType == MOVIE_TRAILERS)
                    fillTrailersList(strJsonMovies);
                else if (iAttachmentType == MOVIE_REVIEWS)
                    fillReviewsList(strJsonMovies);
                else
                    fillINFOList(strJsonMovies);
                return null;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                strJsonMovies = null;
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (iAttachmentType == MOVIE_TRAILERS) {
                    adpTrailersUrl.notifyDataSetChanged();
                    configListViewHeight(lstTrailers);
                }
                else if (iAttachmentType == MOVIE_REVIEWS) {
                    mTxtReviews.setText("");
                    if(lstReviewsContent.size() == 0 )
                        mTxtReviews.setText("No Reviews Exist");
                    else {
                        for (String S : lstReviewsContent)
                            mTxtReviews.append(S + "\r\n______________\r\n\r\n");
                    }
                }
                else{
                    if(!strDuration.isEmpty())
                    {
                        mTxtDuration.setVisibility(View.VISIBLE);
                        mTxtDuration.setText(strDuration + " min");
                    }
                }

            }
            catch (Exception e){}
        }
    }

    public class TrailersAdapter extends ArrayAdapter{
        Context context;
        int layoutResourceId;
        ArrayList<String> data = null;

        public TrailersAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            TextView txtTrailerName;
            if(row == null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                txtTrailerName = (TextView)row.findViewById(R.id.txtTrailerName);
                row.setTag(txtTrailerName);
            }
            else
            {
                txtTrailerName = (TextView)row.getTag();
            }
            txtTrailerName.setText(data.get(position));
            return row;
        }
    }
 }
