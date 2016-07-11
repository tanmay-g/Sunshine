package com.example.android.sunshine.app;

/**
 * Code for the main fragment in the sunshine app
 * Created by Tanmay Godbole on 27-05-2016.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A fragment containing the code to get the prediction data.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;
    public final static String WEATHER_DATA = "com.example.sunshine.MESSAGE";
    public ForecastFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        /*String[] fakeDataArray = {
                "Today - Sunny - 32/35°C",
                "Tomorrow - Sunny - 35/40°C",
                "Wednesday - Cloudy - 27/30°C",
                "Thursday - Cloudy - 27/30°C",
                "Friday - Windy - 21/25°C",
                "Saturday - Rainy - 15/20°C",
                "Sunday - Sunny - 37/42°C"
        };
        //R.layout.list_item_forecast;

        List<String> dummyData = new ArrayList<>(Arrays.asList(fakeDataArray));
        dummyData.add("Monday - Sunny - 32/35°C");
        dummyData.add("Tuesday - Sunny - 35/40°C");
        dummyData.add("Wednesday - Cloudy - 27/30°C");
        dummyData.add("Thursday - Cloudy - 27/30°C");
        dummyData.add("Friday - Windy - 21/25°C");
        dummyData.add("Saturday - Rainy - 15/20°C");
        dummyData.add("Sunday - Sunny - 37/42°C");*/

        mForecastAdapter = new ArrayAdapter<>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,new ArrayList<String>());

        ListView mainListView = (ListView)rootView.findViewById(R.id.listview_forecast);
        mainListView.setAdapter(mForecastAdapter);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String forecast = ((TextView) view).getText().toString();
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class).putExtra(WEATHER_DATA, forecast);
                startActivity(detailIntent);
                //Toast.makeText(getActivity(),mForecastAdapter.getItem(position),Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    private void updateWeather(){
        //SharedPreferences settings = getActivity().getSharedPreferences(SettingsActivity.PREFS_FILE_NAME,0);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = settings.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        //new FetchWeatherTask().execute(location);
        new FetchWeatherTask(getActivity(), mForecastAdapter).execute(location);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }

//    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
//
//        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//
//        /* The date/time conversion code is going to be moved outside the asynctask later,
// * so for convenience we're breaking it out into its own method now.
// */
//        private String getReadableDateString(long time){
//            // Because the API returns a unix timestamp (measured in seconds),
//            // it must be converted to milliseconds in order to be converted to valid date.
//            //SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            //return SimpleDateFormat.getDateInstance().format(time);
//            //return shortenedDateFormat.format(time);
//            return new SimpleDateFormat("EEE MMM dd").format(time);
//        }
//
//        /**
//         * Prepare the weather high/lows for presentation.
//         */
//        private String formatHighLows(double high, double low, String units) {
//            // For presentation, assume the user doesn't care about tenths of a degree.
//            long roundedHigh = (units.equals(getString(R.string.pref_units_metric)))?Math.round(high):Math.round((9*high/5)+32);
//            long roundedLow = (units.equals(getString(R.string.pref_units_metric)))?Math.round(low):Math.round((9*low/5)+32);
//
//            return roundedHigh + "/" + roundedLow;
//        }
//
//        /**
//         * Take the String representing the complete forecast in JSON Format and
//         * pull out the data we need to construct the Strings needed for the wireframes.
//         *
//         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
//         * into an Object hierarchy for us.
//         */
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {
//
//            // These are the names of the JSON objects that need to be extracted.
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//            final String OWM_DESCRIPTION = "main";
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//            // OWM returns daily forecasts based upon the local time of the city that is being
//            // asked for, which means that we need to know the GMT offset to translate this data
//            // properly.
//
//            // Since this data is also sent in-order and the first day is always the
//            // current day, we're going to take advantage of that to get a nice
//            // normalized UTC date for all of our weather.
//
//            Time dayTime = new Time();
//            dayTime.setToNow();
//
//            // we start at the day returned by local time. Otherwise this is a mess.
//            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
//            // now we work exclusively in UTC
//            dayTime = new Time();
//            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String unitsSetting = settings.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_metric));
//
//            String[] resultStrs = new String[numDays];
//            for(int i = 0; i < weatherArray.length(); i++) {
//                // For now, using the format "Day, description, hi/low"
//                String day;
//                String description;
//                String highAndLow;
//
//                // Get the JSON object representing the day
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                // The date/time is returned as a long.  We need to convert that
//                // into something human-readable, since most people won't read "1400356800" as
//                // "this saturday".
//                long dateTime;
//                // Cheating to convert this to UTC time, which is what we want anyhow
//                dateTime = dayTime.setJulianDay(julianStartDay+i);
//                day = getReadableDateString(dateTime);
//
//                // description is in a child array called "weather", which is 1 element long.
//                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//
//                // Temperatures are in a child object called "temp".  Try not to name variables
//                // "temp" when working with temperature.  It confuses everybody.
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                highAndLow = formatHighLows(high, low, unitsSetting);
//                resultStrs[i] = day + " - " + description + " - " + highAndLow;
//            }
//
//            for (String s : resultStrs) {
//                Log.v(LOG_TAG, "Forecast entry: " + s);
//            }
//            return resultStrs;
//
//        }
//
//        @Override
//        protected String[] doInBackground(String... params) {
//            if (params.length==0){
//                return null;
//            }
//            // These two need to be declared outside the try/catch
//            // so that they can be closed in the finally block.
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//            String format = "json";
//            String units = "metric";
//            int numDays = 7;
//            try {
//                // Construct the URL for the OpenWeatherMap query
//                // Possible parameters are available at OWM's forecast API page, at
//                // http://openweathermap.org/API#forecast
//                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043,usa&appid=96f6ce84f3495bda1d4516f3a0c476af&mode=json&units=metric&cnt=7");
//                if (params[0] == null){
//                    params[0]="Mountain View";
//                }
//                Uri.Builder weatherURLbuilder= new Uri.Builder();
//                weatherURLbuilder.scheme("http")
//                        .authority("api.openweathermap.org")
//                        .appendPath("data")
//                        .appendPath("2.5")
//                        .appendPath("forecast")
//                        .appendPath("daily")
//                        .appendQueryParameter("q",params[0])
//                        .appendQueryParameter("mode", format)
//                        .appendQueryParameter("units", units)
//                        .appendQueryParameter("cnt", Integer.toString(numDays))
//                        .appendQueryParameter("APPID", BuildConfig.OPEN_WEATHER_MAP_API_KEY);
//                URL url = new URL(weatherURLbuilder.build().toString());
//
//                // Create the request to OpenWeatherMap, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuilder buffer = new StringBuilder();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line).append("\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                Log.i(LOG_TAG, buffer.toString());
//                forecastJsonStr = buffer.toString();
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
//                // If the code didn't successfully get the weather data, there's no point in attempting
//                // to parse it.
//                return null;
//            } finally{
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//            try {
//                return getWeatherDataFromJson(forecastJsonStr, numDays);
//            } catch (JSONException e) {
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String[] forecastJson) {
//            if (forecastJson != null){
//                mForecastAdapter.clear();
//                mForecastAdapter.addAll(forecastJson);
//            }
//        }
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
        if (id== R.id.action_refresh){
            updateWeather();
            //Log.i("Selected:", "True");
            return true;
        }
        else if (id==R.id.action_settings){

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
