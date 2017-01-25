package com.example.android.sunshine.app;

/**
 * Code for the main fragment in the sunshine app
 * Created by Tanmay Godbole on 27-05-2016.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import static android.R.attr.max;

/**
 * A fragment containing the code to get the prediction data.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };


    private static final int FORECAST_LOADER = 0;
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    public final static String WEATHER_DATA = "com.example.sunshine.MESSAGE";


//    private int mSelectedItemPosition = RecyclerView.NO_POSITION;
    private final static String mSelectedItemPosKey = "selectedItemPosKey";

    private boolean mtwoPane;

    private RecyclerView mainRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private boolean mAutoSelectView;
    private int mChoiceMode;
    private boolean mHoldForTransition;
    private RecyclerView.LayoutManager mainRecyclerLayoutManager;

    private TextView emptyMessageView;

    public void setInitialSelectedDate(long mInitialSelectedDate) {
        this.mInitialSelectedDate = mInitialSelectedDate;
    }

    private long mInitialSelectedDate = -1;

    private ForecastAdapter.ForecastAdapterOnClickHandler clickHandler = new ForecastAdapter.ForecastAdapterOnClickHandler() {
        @Override
        public void onClick(Long date, ForecastAdapter.ForecastAdapterViewHolder vh) {
            String locationSetting = Utility.getPreferredLocation(getActivity());
            ((Callback) getActivity())
                    //.putExtra(WEATHER_DATA, WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                    //        locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    //));
                    .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, date
                    ), vh);
//            mSelectedItemPosition = vh.getAdapterPosition();
        }
    };


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri, ForecastAdapter.ForecastAdapterViewHolder vh);
    }

    public ForecastFragment(){

    }

    void setAdapterIsTwoPane(boolean twoPane){
        mtwoPane = twoPane;
        if (mForecastAdapter != null)
            mForecastAdapter.setIsTwoPane(mtwoPane);
    }

//    public void setmSelectedItemPosition(int mSelectedItemPosition) {
//        this.mSelectedItemPosition = mSelectedItemPosition;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        emptyMessageView = (TextView) rootView.findViewById(R.id.empty);

        mForecastAdapter = new ForecastAdapter(getActivity(), null, clickHandler, emptyMessageView, mChoiceMode);
        mForecastAdapter.setIsTwoPane(mtwoPane);

        mainRecyclerView = (RecyclerView) rootView.findViewById(R.id.listview_forecast);
        mainRecyclerView.setHasFixedSize(true);
        mainRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mainRecyclerView.setLayoutManager(mainRecyclerLayoutManager);
        mainRecyclerView.setAdapter(mForecastAdapter);

        final View parallaxBar = rootView.findViewById(R.id.parallax_bar);
        if (parallaxBar != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    //                    float totalScrolled = 0;
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
//                        totalScrolled += (-1 * dy);
//                        int max = parallaxBar.getHeight();
//                        if (totalScrolled > max)
//                            totalScrolled = max;
//                        parallaxBar.setTranslationY(totalScrolled / 2);
                        if (dy > 0) {
                            parallaxBar.setTranslationY(Math.max(-max, parallaxBar.getTranslationY() - dy / 2));
                        }
                        else {
                            parallaxBar.setTranslationY(Math.min(0, parallaxBar.getTranslationY() - dy / 2));
                        }
                    }
                });
            }
        }

        final AppBarLayout appbarView = (AppBarLayout)rootView.findViewById(R.id.appbar);
        if (null != appbarView) {
            ViewCompat.setElevation(appbarView, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (0 == mainRecyclerView.computeVerticalScrollOffset()) {
                            appbarView.setElevation(0);
                        } else {
                            appbarView.setElevation(16);//appbarView.getTargetElevation());
                        }
                    }
                });
            }
        }

        if (savedInstanceState != null){
//            if (savedInstanceState.containsKey(mSelectedItemPosKey)) {
//                mSelectedItemPosition = savedInstanceState.getInt(mSelectedItemPosKey);
////                mainRecyclerView.smoothScrollToPosition(mSelectedItemPosition);
//            }
            mForecastAdapter.onRestoreInstanceState(savedInstanceState);
        }
        return rootView;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ForecastFragment,
                0, 0);
        mChoiceMode = a.getInt(R.styleable.ForecastFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
        mHoldForTransition = a.getBoolean(R.styleable.ForecastFragment_sharedElementTransitions, false);
        a.recycle();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // We hold for transition here just in-case the activity
        // needs to be re-created. In a standard return transition,
        // this doesn't actually make a difference.
        if ( mHoldForTransition ) {
            Log.i(LOG_TAG, "supportPostponeEnterTransition");
            getActivity().supportPostponeEnterTransition();
        }
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        settings.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        settings.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mainRecyclerView != null)
            mainRecyclerView.clearOnScrollListeners();
        super.onDestroy();
    }

    private void updateWeather(){
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

//    @Override
//    public void onStart(){

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        if (mSelectedItemPosition != RecyclerView.NO_POSITION)
//            outState.putInt(mSelectedItemPosKey, mSelectedItemPosition);
        mForecastAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
//        super.onStart();
//        updateWeather();
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.i("ForecastFragment", "Going to settings for id " + id);
//        if (id== R.id.action_refresh){
//            updateWeather();
//            //Log.i("Selected:", "True");
//            return true;
//        }
//        else
        if (id == R.id.action_launch_map){
            Log.i("MainActivity", "Going to launch map");
            openPreferredLocationInMap();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(){
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != mForecastAdapter ) {
            Cursor c = mForecastAdapter.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d("ForecastFragment", "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Utility.LOCATION_STATUS_PREF_KEY))
            updateEmptyView();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mForecastAdapter.swapCursor(data);
//        if (mainRecyclerView != null && mSelectedItemPosition != RecyclerView.NO_POSITION)
//            mainRecyclerView.smoothScrollToPosition(mSelectedItemPosition);
        updateEmptyView();

        if ( data.getCount() == 0 ) {
            getActivity().supportStartPostponedEnterTransition();
        }
        else {
            mainRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (mainRecyclerView.getChildCount() > 0) {
                        mainRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
//                        int itemPosition = mForecastAdapter.getSelectedItemPosition();
//                        if ( RecyclerView.NO_POSITION == itemPosition ) itemPosition = 0;
                        int position = mForecastAdapter.getSelectedItemPosition();
                        if (position == RecyclerView.NO_POSITION && mInitialSelectedDate != -1){
                            Cursor data = mForecastAdapter.getCursor();
                            int count = data.getCount();
                            int dateColumn = data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
                            for (int i=0; i < count; i++){
                                data.moveToPosition(i);
                                if ( data.getLong(dateColumn) == mInitialSelectedDate ) {
                                    position = i;
                                    break;
                                }
                            }
                        }

                        if (position == RecyclerView.NO_POSITION)
                            position = 0;
                        mainRecyclerView.smoothScrollToPosition(position);
                        RecyclerView.ViewHolder vh = mainRecyclerView.findViewHolderForAdapterPosition(position);
                        if ( null != vh && mAutoSelectView ) {
                            mForecastAdapter.selectView( vh );
                        }


                        if (mHoldForTransition)
                            Log.i(LOG_TAG, "Starting the supportStartPostponedEnterTransition");
                            getActivity().supportStartPostponedEnterTransition();
                        return true;
                    }
                    return false;
                }
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    private void updateEmptyView() {
        if ( mForecastAdapter.getItemCount() == 0 ){
            if ( null != emptyMessageView ) {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.default_empty_list_message;
                if (!Utility.isConnected(getActivity()) ) {
                    message = R.string.no_net_message;
                }
                else if (Utility.getLocationStatus(getActivity()) == SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN){
                    message = R.string.empty_list_server_down_message;
                }
                else if (Utility.getLocationStatus(getActivity()) == SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID){
                    message = R.string.empty_list_server_error_message;
                }
                else if (Utility.getLocationStatus(getActivity()) == SunshineSyncAdapter.LOCATION_STATUS_INVALID){
                    Log.i(LOG_TAG, "Invalid status");
                    message = R.string.empty_list_invalid_location_message;
                }
                emptyMessageView.setText(message);
            }
        }
    }

}
