package com.vilakshan.weathermaster.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vilakshan.weathermaster.R;
import com.vilakshan.weathermaster.utils.Constants;
import com.vilakshan.weathermaster.utils.Utils;

/**
 * <p>
 * Created by VilakshanSaxena on 12/19/2015.
 * </p>
 */
public class RecForecastAdapter extends RecyclerView.Adapter<RecForecastAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private RecForecastAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private boolean mUseTodayLayout;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final LinearLayout recyclerBG;
        final ImageView imgIcon;
        final TextView dateTV;
        final TextView forecastTV;
        final TextView highTempTV;
        final TextView lowTempTV;
        final TextView mainCityName;

        ViewHolder(View view) {
            super(view);
            recyclerBG = (LinearLayout) view.findViewById(R.id.recycler_BG);
            imgIcon = (ImageView) view.findViewById(R.id.list_item_icon);
            dateTV = (TextView) view.findViewById(R.id.list_item_date_textview);
            forecastTV = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempTV = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempTV = (TextView) view.findViewById(R.id.list_item_low_textview);
            mainCityName = (TextView) view.findViewById(R.id.main_City_Name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(mCursor.getLong(Constants.COL_WEATHER_DATE), this, v);
        }
    }

    public interface RecForecastAdapterOnClickHandler {
        void onClick(Long date, ViewHolder vh, View v);
    }

    public RecForecastAdapter(Context context, RecForecastAdapterOnClickHandler dh,
                              View emptyView) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    //Public Setter Method
    public void setTodayLayoutFlag(boolean todayFlag) {
        mUseTodayLayout = todayFlag;
    }

    @Override
    public RecForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;
        View view;
        viewType = getItemViewType(viewType);
        if (parent instanceof RecyclerView) {
            if (viewType == VIEW_TYPE_TODAY) {
                //TODAY
                layoutId = R.layout.list_item_forecast_today;
            } else {
                //FUTURE DAY
                layoutId = R.layout.list_item_forecast;
            }
            view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }

    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public void onBindViewHolder(RecForecastAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        //Setting Image
        int weatherId = mCursor.getInt(Constants.COL_WEATHER_CONDITION_ID);
        String iconName = mCursor.getString(Constants.COL_ICON_NAME);
        Utils.loadImage(mContext, iconName, holder.imgIcon);
        //Setting unique transition name to each image icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setTransitionName(holder.imgIcon, Constants.MAIN_ICON_TRANSITION_STRING +
                    position);
        }
        //Setting City Name
        String cityName = mCursor.getString(Constants.COL_CITY_NAME);
        holder.mainCityName.setText(cityName);
        //Setting date
        long date = mCursor.getLong(Constants.COL_WEATHER_DATE);
        String formattedDate = Utils.getFriendlyDayString(mContext, date);
        holder.dateTV.setText(formattedDate);
        //Setting Forecast
        String forecast = Utils.getDescForWeatherCondition(mContext, weatherId);
        holder.forecastTV.setText(forecast);
        //Setting High Temp
        Double highTemp = mCursor.getDouble(Constants.COL_WEATHER_MAX_TEMP);
        holder.highTempTV.setText(Utils.formatTemperature(mContext, highTemp,
                Utils.isMetric(mContext)));
        //Setting Low Temp
        Double lowTemp = mCursor.getDouble(Constants.COL_WEATHER_MIN_TEMP);
        holder.lowTempTV.setText(Utils.formatTemperature(mContext, lowTemp,
                Utils.isMetric(mContext)));
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }
}
