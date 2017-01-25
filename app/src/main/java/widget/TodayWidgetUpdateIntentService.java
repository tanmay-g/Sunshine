package widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class TodayWidgetUpdateIntentService extends IntentService {

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEMP = 2;

    public TodayWidgetUpdateIntentService() {
        super("TodayWidgetUpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        for (int appWidgetId : appWidgetIds)
            updateAppWidget(this,appWidgetManager, appWidgetId);


    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {

        String location = Utility.getPreferredLocation(context);
        Cursor data = getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, System.currentTimeMillis()),
                FORECAST_COLUMNS,
                null,
                null,
                WeatherContract.WeatherEntry.COLUMN_DATE + " ASC"
                );

        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }


        // Extract the weather data from the Cursor
        int weatherId = data.getInt(INDEX_WEATHER_ID);
        int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
        String description = data.getString(INDEX_SHORT_DESC);
        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
        double minTemp = data.getDouble(INDEX_MIN_TEMP);
        String formattedMaxTemperature = Utility.formatTemperature(this, maxTemp);
        String formattedMinTemperature = Utility.formatTemperature(this, minTemp);
        data.close();

        int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
        int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
        int layoutId;
        if (widgetWidth >= largeWidth) {
            layoutId = R.layout.today_widget_large;
        } else if (widgetWidth >= defaultWidth) {
            layoutId = R.layout.today_widget_default;
        } else {
            layoutId = R.layout.today_widget_small;
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

        // Add the data to the RemoteViews
        views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
        // Content Descriptions for RemoteViews were only added in ICS MR1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            setRemoteContentDescription(views, description);
        }
        views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
        views.setTextViewText(R.id.widget_low_temperature, formattedMinTemperature);
        views.setTextViewText(R.id.widget_description, description);
        // Create an Intent to launch MainActivity
        Intent launchIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }

}
