package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_HUMIDITY = 9;
    static final int COL_PRESSURE = 10;
    static final int COL_WIND_SPEED = 11;
    static final int COL_WIND_DEGREES = 12;
    static final String[] DETAIL_COLUMNS = {
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
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int DETAIL_LOADER = 0;
//    private ShareActionProvider mShareActionProvider;

//    private String mForecastURI;
    private String mForecastString = null;

    private TextView dayView;
//    private TextView dateView;
    private TextView maxView;
    private TextView minView;
    private TextView weatherDescView;
    private TextView humidityView;
    private TextView humidityLabelView;
    private TextView pressureView;
    private TextView pressureLabelView;
    private TextView windView;
    private TextView windLabelView;
    private ImageView weatherIconView;

    static final String DETAIL_URI = "URI";
    private Uri mUri;

    public DetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailsFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);
//        Intent callingIntent = getActivity().getIntent();
//        if (callingIntent!=null) {
//            //mForecastURI = callingIntent.getStringExtra(ForecastFragment.WEATHER_DATA);
//            mForecastURI = callingIntent.getDataString();
//        }
//            if (mForecastURI != null){
//                ((TextView) rootView.findViewById(R.id.day_text)).setText(mForecastURI);
//            }
        dayView = ((TextView) rootView.findViewById(R.id.detail_date_textview));
//        dateView =((TextView) rootView.findViewById(R.id.dateText));
        maxView = ((TextView) rootView.findViewById(R.id.detail_high_textview));
        minView = ((TextView) rootView.findViewById(R.id.detail_low_textview));
        weatherDescView = ((TextView) rootView.findViewById(R.id.detail_forecast_textview));
        humidityView = ((TextView) rootView.findViewById(R.id.detail_humidity_textview));
        humidityLabelView = (TextView) rootView.findViewById(R.id.detail_humidity_label_textview);
        pressureView = ((TextView) rootView.findViewById(R.id.detail_pressure_textview));
        pressureLabelView = (TextView) rootView.findViewById(R.id.detail_pressure_label_textview);
        windView = ((TextView) rootView.findViewById(R.id.detail_wind_textview));
        windLabelView = (TextView) rootView.findViewById(R.id.detail_wind_label_textview);
        weatherIconView = ((ImageView) rootView.findViewById(R.id.detail_icon));
        return rootView;
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if ( getActivity() instanceof DetailActivity ) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
            finishCreatingMenu(menu);
        }
//        // Retrieve the share menu item
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//
//        // Get the provider and hold onto it to set/change the share intent.
//        mShareActionProvider =
//                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//
//        // Attach an intent to this ShareActionProvider.  You can update this at any time,
//        // like when the user selects a new piece of data they might like to share.
//        if (mShareActionProvider != null && mForecastString != null) {
//            mShareActionProvider.setShareIntent(createShareForecastIntent());
//        } else {
//            Log.d(LOG_TAG, "Share Action Provider is null?");
//        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                //((TextView) getView().findViewById(R.id.day_text)).getText()
                mForecastString
                        + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        if (getView().getParent() instanceof CardView)
            ((CardView) getView().getParent()).setVisibility(View.INVISIBLE);
        else
            getView().setVisibility(View.INVISIBLE);
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (! data.moveToFirst()) {
            return;
        }
        if (getView().getParent() instanceof CardView)
            ((CardView) getView().getParent()).setVisibility(View.VISIBLE);
        else
            getView().setVisibility(View.VISIBLE);
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

        String dayString = Utility.getDayName(getActivity(),
                data.getLong(COL_WEATHER_DATE));

        String dateString = Utility.getFullFriendlyDayString(getActivity(),
                data.getLong(COL_WEATHER_DATE));

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(
                getActivity(),
                data.getDouble(COL_WEATHER_MAX_TEMP),
                isMetric);

        String low = Utility.formatTemperature(
                getActivity(),
                data.getDouble(COL_WEATHER_MIN_TEMP),
                isMetric);

        String weatherDescription =
                data.getString(COL_WEATHER_DESC);

        float humidityValue = data.getFloat(COL_HUMIDITY);

        //As the api seems to always return 0, we'll change it to a random value for fun
        if (Float.compare(humidityValue, 0) == 0) {
            humidityValue = (float) Math.random() * 100;
        }
        String humidityText = getActivity().getString(R.string.format_humidity, humidityValue);

        String pressureText = getString(R.string.format_pressure,data.getFloat(COL_PRESSURE));

        String windText = Utility.getFormattedWind(getActivity(),
                data.getFloat(COL_WIND_SPEED),
                data.getFloat(COL_WIND_DEGREES));

        mForecastString = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        if (Utility.usingLocalGraphics(getActivity()))
            weatherIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        else {
            Glide.with(this)
                    .load(Utility.getArtUrlForWeatherCondition(getActivity(), weatherId))
                    .error(Utility.getArtResourceForWeatherCondition(weatherId))
                    .crossFade()
                    .into(weatherIconView);
        }
        dayView.setText(dayString);
//        dateView.setText(dateString);
        maxView.setText(high);
        minView.setText(low);
        weatherDescView.setText(weatherDescription);
        humidityView.setText(humidityText);
        pressureView.setText(pressureText);
        windView.setText(windText);

//        getActivity().setTitle(dateString);

        weatherIconView.setContentDescription(getString(R.string.a11y_forecast_icon, weatherDescription));
        weatherDescView.setContentDescription(getString(R.string.a11y_forecast, weatherDescription));
        maxView.setContentDescription(getString(R.string.a11y_high_temp, high));
        minView.setContentDescription(getString(R.string.a11y_low_temp, low));
        humidityView.setContentDescription(getString(R.string.a11y_humidity, humidityText));
        humidityLabelView.setContentDescription(humidityView.getContentDescription());
        pressureView.setContentDescription(getString(R.string.a11y_pressure, pressureText));
        pressureLabelView.setContentDescription(pressureView.getContentDescription());
        windView.setContentDescription(getString(R.string.a11y_wind, windText));
        windLabelView.setContentDescription(windView.getContentDescription());

//        if (mShareActionProvider != null && mForecastString != null) {
//            mShareActionProvider.setShareIntent(createShareForecastIntent());
//        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);
        // We need to start the enter transition after the data has loaded
        if (activity instanceof DetailActivity) {
                activity.supportStartPostponedEnterTransition();
                if ( null != toolbarView ) {
                    activity.setSupportActionBar(toolbarView);
                    activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
        } else {
            if ( null != toolbarView ) {
                Menu menu = toolbarView.getMenu();
                if ( null != menu ) menu.clear();
                toolbarView.inflateMenu(R.menu.detailfragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.reset();
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}
