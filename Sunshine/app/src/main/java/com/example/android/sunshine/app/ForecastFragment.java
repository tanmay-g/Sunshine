package com.example.android.sunshine.app;

/**
 * Code for the main fragment in the sunshine app
 * Created by Tanmay Godbole on 27-05-2016.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * A fragment containing the code to get the prediction data.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
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
    private ForecastAdapter mForecastAdapter;
    public final static String WEATHER_DATA = "com.example.sunshine.MESSAGE";
    private int mSelectedItemPosition = ListView.INVALID_POSITION;;
    private final static String mSelectedItemPosKey = "selectedItemPosKey";
    private ListView mainListView;
    private boolean mtwoPane;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public ForecastFragment(){

    }

    void setAdapterIsTwoPane(boolean twoPane){
        mtwoPane = twoPane;
        if (mForecastAdapter != null)
            mForecastAdapter.setIsTwoPane(mtwoPane);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

//        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
//        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
//        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
//                locationSetting, System.currentTimeMillis());

//        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
//                null, null, null, sortOrder);

        //mForecastAdapter = new ArrayAdapter<>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,new ArrayList<String>());
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setIsTwoPane(mtwoPane);

        mainListView = (ListView)rootView.findViewById(R.id.listview_forecast);
        mainListView.setAdapter(mForecastAdapter);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mSelectedItemPosition = position;
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    //Intent intent = new Intent(getActivity(), DetailActivity.class)
                    ((Callback) getActivity())
                            //.putExtra(WEATHER_DATA, WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            //        locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            //));
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    //startActivity(intent);
                }
//                String forecast = ((TextView) view).getText().toString();
//                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
//                Intent detailIntent = new Intent(getActivity(), DetailActivity.class).putExtra(WEATHER_DATA, forecast);
//                startActivity(detailIntent);
                //Toast.makeText(getActivity(),mForecastAdapter.getItem(position),Toast.LENGTH_SHORT).show();
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(mSelectedItemPosKey)) {
            mSelectedItemPosition = savedInstanceState.getInt(mSelectedItemPosKey);
//            mainListView.smoothScrollToPosition(mSelectedItemPosition);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    private void updateWeather(){
        /*//SharedPreferences settings = getActivity().getSharedPreferences(SettingsActivity.PREFS_FILE_NAME,0);
        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        //new FetchWeatherTask(getActivity()).execute(location);
        Intent serviceIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        serviceIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, location);
        //getActivity().startService(serviceIntent);
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;
        alarmMgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        //Intent intent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, serviceIntent, PendingIntent.FLAG_ONE_SHOT);

        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() +
                        5 * 1000, alarmIntent);*/

        SunshineSyncAdapter.syncImmediately(getActivity());
    }

//    @Override
//    public void onStart(){

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectedItemPosition != ListView.INVALID_POSITION)
            outState.putInt(mSelectedItemPosKey, mSelectedItemPosition);
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
        if (mainListView != null && mSelectedItemPosition != ListView.INVALID_POSITION)
            mainListView.smoothScrollToPosition(mSelectedItemPosition);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }


}
