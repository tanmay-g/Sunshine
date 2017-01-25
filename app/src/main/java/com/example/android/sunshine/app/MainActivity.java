package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    String mLocation = null;
    boolean mTwoPane;
    //private final String FORECASTFRAGMENT_TAG = "ForecastFragmentTag";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ForecastFragment ff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.i(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mLocation = Utility.getPreferredLocation(this);
        Uri contentUri = getIntent() != null ? getIntent().getData() : null;
        if (findViewById(R.id.weather_detail_container) != null){
            mTwoPane = true;
            if (savedInstanceState == null) {

                DetailsFragment detailsFragment = new DetailsFragment();
                if (contentUri != null){
                    Bundle args = new Bundle();
                    args.putParcelable(DetailsFragment.DETAIL_URI, contentUri);
                    detailsFragment.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, detailsFragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
        else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        ff.setAdapterIsTwoPane(mTwoPane);
        if (contentUri != null){
            ff.setInitialSelectedDate(WeatherContract.WeatherEntry.getDateFromUri(contentUri));
        }
        SunshineSyncAdapter.initializeSyncAdapter(this);

//        if (!checkPlayServices()) {
//            // This is where we could either prompt a user that they should install
//            // the latest version of Google Play Services, or add an error snackbar
//            // that some features won't be available.
//        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
//    private boolean checkPlayServices() {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (apiAvailability.isUserResolvableError(resultCode)) {
//                apiAvailability.getErrorDialog(this, resultCode,
//                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
//            } else {
//                Log.i(LOG_TAG, "This device is not supported.");
//                finish();
//            }
//            return false;
//        }
//        return true;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.i("MainActivity", "Going to settings");
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onResume() {
        //Log.i(LOG_TAG, "onResume");
        super.onResume();
        String newLocation = Utility.getPreferredLocation(this);
        if ((newLocation != null) && !(mLocation.equals(newLocation))){
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (ff != null)
                ff.onLocationChanged();
            DetailsFragment df = (DetailsFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(newLocation);
            }
            mLocation = newLocation;
        }
    }


    @Override
    public void onItemSelected(Uri contentUri, ForecastAdapter.ForecastAdapterViewHolder vh) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailsFragment.DETAIL_URI, contentUri);

            DetailsFragment fragment = new DetailsFragment();
            fragment.setArguments(args);
//            Log.i(LOG_TAG, "Will now replace the fragment");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
//            Log.i(LOG_TAG, "Will start detail intent");
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ActivityOptionsCompat activityOptions;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        this,
//                        Pair.create((View)vh.mIconView, vh.mIconView.getTransitionName())
//                        );
//            }
            activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    new Pair<View, String>(vh.mIconView, getString(R.string.transition_name_shared_detail))
            );

            ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
        }
    }
}
