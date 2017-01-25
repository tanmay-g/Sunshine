package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.sunshine.app.data.WeatherContract;

import static com.example.android.sunshine.app.ForecastFragment.COL_WEATHER_DATE;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link RecyclerView}.
 */
class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final String LOG_TAG = ForecastAdapter.class.getSimpleName();
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE = 1;
    private boolean mIsTwoPane = false;
    final private ItemChoiceManager mICM;

    private Cursor mCursor;
    private Context mContext;

    private final ForecastAdapterOnClickHandler handler;

    private final View emptyView;



    public static interface ForecastAdapterOnClickHandler {
        void onClick(Long date, ForecastAdapterViewHolder vh);
    }

    ForecastAdapter(Context context, Cursor resultCursor, ForecastAdapterOnClickHandler clickHandler, View emptyView, int choiceMode) {
        mContext = context;
        mCursor = resultCursor;
        handler = clickHandler;
        this.emptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }

    Cursor getCursor(){
        return mCursor;
    }

    void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
        emptyView.setVisibility((getItemCount() == 0 ? View.VISIBLE : View.GONE));
    }

    void setIsTwoPane(boolean IsTwoPane) {
        this.mIsTwoPane = IsTwoPane;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && !mIsTwoPane)?(VIEW_TYPE_TODAY):(VIEW_TYPE_FUTURE);
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor )
            return 0;
        return mCursor.getCount();
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        int viewType = getItemViewType(mCursor.getPosition());
        if ( parent instanceof RecyclerView ) {
            int layoutID = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY: {
                    layoutID = R.layout.list_item_forecast_today;
                    break;
                }
                case VIEW_TYPE_FUTURE: {
                    layoutID = R.layout.list_item_forecast;
                    break;
                }
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutID, parent, false);
            return new ForecastAdapterViewHolder(view);
        }
        else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        if (position >= mCursor.getCount())
            return;
//        mCursor.
        mCursor.moveToPosition(position);
        ImageView iconView = forecastAdapterViewHolder.mIconView;
        int defaultImage;
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        boolean useLongToday;
        switch (getItemViewType(position)){
            case VIEW_TYPE_TODAY:{
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                useLongToday = true;
                break;
            }
            default: {
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                useLongToday = false;
            }
        }
        if (Utility.usingLocalGraphics(mContext)){
            forecastAdapterViewHolder.mIconView.setImageResource(defaultImage);
        }
        else {
            Glide
                    .with(mContext)
                    .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                    .error(defaultImage)
                    .crossFade()
                    .into(iconView);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            iconView.setTransitionName(mContext.getString(R.string.transition_name_shared_main, position));
//        }
        ViewCompat.setTransitionName(forecastAdapterViewHolder.mIconView,
                mContext.getString(R.string.transition_name_shared_main, position));
//                "iconView" + position);

        Long dateText = mCursor.getLong(COL_WEATHER_DATE);
        TextView dateView = forecastAdapterViewHolder.mDateView;
        dateView.setText(Utility.getFriendlyDayString(mContext, dateText, useLongToday));

        String forecastText = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView forecastView = forecastAdapterViewHolder.mDescriptionView;
        forecastView.setText(forecastText);
        forecastView.setContentDescription(mContext.getString(R.string.a11y_forecast, forecastText));

        // Read high temperature from cursor
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView highView = forecastAdapterViewHolder.mHighTempView;
        String highText = Utility.formatTemperature(mContext, high);
        highView.setText(highText);
        highView.setContentDescription(mContext.getString(R.string.a11y_high_temp, highText));

        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = forecastAdapterViewHolder.mLowTempView;
        String lowText = Utility.formatTemperature(mContext, low);
        lowView.setText(lowText);
        lowView.setContentDescription(mContext.getString(R.string.a11y_low_temp, lowText));
        mICM.onBindViewHolder(forecastAdapterViewHolder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }


    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof ForecastAdapterViewHolder) {
            ForecastAdapterViewHolder vfh = (ForecastAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }


    /**
     * Cache of the children views for a forecast list item.
     */
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final ImageView mIconView;
        final TextView mDateView;
        final TextView mDescriptionView;
        final TextView mHighTempView;
        final TextView mLowTempView;

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            handler.onClick(mCursor.getLong(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE)), this);
            mICM.onClick(this);
        }

        ForecastAdapterViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            mDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            mHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            mLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}