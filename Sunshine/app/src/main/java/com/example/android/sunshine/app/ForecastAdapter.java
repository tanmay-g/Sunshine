package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE = 1;
    private boolean mIsTwoPane = false;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());
        int layoutID = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutID = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE:{
                layoutID = R.layout.list_item_forecast;
                break;
            }
        }
        View view = LayoutInflater.from(context).inflate(layoutID, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }
    //    /*
//        This is where we fill-in the views with the contents of the cursor.
//     */
//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        // our view is pretty simple here --- just a text view
//        // we'll keep the UI functional with a simple (and slow!) binding.
//
//        TextView tv = (TextView)view;
//        tv.setText(convertCursorRowToUXFormat(cursor));
//    }
    /*
        This is where we fill-in the views with the contents of the cursor.
    */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Use placeholder image for now
        ImageView iconView = viewHolder.iconView;
        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType){
            case VIEW_TYPE_TODAY:{
                iconView.setImageResource(
                        Utility.getArtResourceForWeatherCondition(
                                cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
            }
            case VIEW_TYPE_FUTURE: {
                iconView.setImageResource(
                        Utility.getIconResourceForWeatherCondition(
                                cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
            }
        }

        Long dateText = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        TextView dateView = viewHolder.dateView;
        dateView.setText(Utility.getFriendlyDayString(context, dateText));

        String forecastText = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView forecastView = viewHolder.descriptionView;
        forecastView.setText(forecastText);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = viewHolder.highTempView;
        highView.setText(Utility.formatTemperature(context, high, isMetric));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = viewHolder.lowTempView;
        lowView.setText(Utility.formatTemperature(context, low, isMetric));
    }

    public void setIsTwoPane(boolean IsTwoPane) {
        this.mIsTwoPane = IsTwoPane;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && !mIsTwoPane)?(VIEW_TYPE_TODAY):(VIEW_TYPE_FUTURE);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }


    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}