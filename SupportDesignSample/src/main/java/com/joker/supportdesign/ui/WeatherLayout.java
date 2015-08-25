package com.joker.supportdesign.ui;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.joker.supportdesign.R;
import com.joker.supportdesign.mvp.domain.ForecastEntity;
import com.joker.supportdesign.mvp.domain.WeatherEntity;
import com.joker.supportdesign.mvp.model.MainInteractorImp;
import com.joker.supportdesign.util.CircleWithBorderTransF;
import com.joker.supportdesign.util.DayFormatter;
import com.joker.supportdesign.util.DensityUtil;
import com.joker.supportdesign.util.TemperatureFormatter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * created  by Joker on 2015/7/5
 */
@SuppressWarnings("unchecked") public class WeatherLayout extends FrameLayout {

  private static final String TAG = WeatherLayout.class.getSimpleName();

  private static final int MARGIN_BOTTOM = DensityUtil.dip2px(80);

  public static final int FETCH_LOCATION_ING = 0;
  public static final int FETCH_LOCATION_ERROR = 1;
  public static final int FETCH_WEATHER_ING = 2;
  public static final int FETCH_WEATHER_ERROR = 3;

  @IntDef({ FETCH_LOCATION_ING, FETCH_LOCATION_ERROR, FETCH_WEATHER_ING, FETCH_WEATHER_ERROR })
  public @interface Status {
  }

  @Nullable @Bind(R.id.menu_weather_state_tv) TextView stateTextView;
  @Nullable @Bind(R.id.menu_weather_current_icon) ImageView currentIcon;
  @Nullable @Bind(R.id.menu_weather_current_tv) TextView currentTempTextView;
  @Nullable @Bind(R.id.menu_weather_attribution) TextView attributionTextView;
  private ImageView stateIcon;
  private RecyclerView recyclerView;

  private OnItemClickListener onItemClickListener;
  private WeatherAdapter weatherAdapter;
  private int headerAvatarSize;

  private int currentState;

  public WeatherLayout(Context context) {
    super(context);
    WeatherLayout.this.init();
  }

  private void init() {

    headerAvatarSize =
        getContext().getResources().getDimensionPixelSize(R.dimen.weather_header_avatar_size);

    // 屏幕宽度（像素）
    int width =
        getContext().getResources().getDisplayMetrics().widthPixels - DensityUtil.dip2px(40);
    // 屏幕高度（像素）
    int height = getContext().getResources().getDisplayMetrics().heightPixels;

    LayoutInflater.from(getContext()).inflate(R.layout.meun_weather_layout, this, true);
    setLayoutParams(new LayoutParams(width, height - MARGIN_BOTTOM));

    stateIcon = (ImageView) findViewById(R.id.menu_weather_state_icon);
    recyclerView = (RecyclerView) findViewById(R.id.menu_weather_recycler_view);

    WeatherLayout.this.setAdapter();
  }

  private void setAdapter() {

    Picasso.with(getContext())
        .load(R.drawable.ic_launcher)
        .resize(headerAvatarSize, headerAvatarSize)
        .centerCrop()
        .transform(new CircleWithBorderTransF())
        .into(stateIcon);

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);
    recyclerView.setLayoutManager(linearLayoutManager);

    weatherAdapter = new WeatherAdapter();
    recyclerView.setAdapter(weatherAdapter);
  }

  public void fillData(HashMap<String, ForecastEntity> entityHashMap) {

    WeatherEntity currentWeather =
        (WeatherEntity) entityHashMap.get(MainInteractorImp.KEY_CURRENT_WEATHER);
    WeatherLayout.this.bindCurrentItem(currentWeather);

    WeatherEntity forecastEntity =
        (WeatherEntity) entityHashMap.get(MainInteractorImp.KEY_FORECAST_WEATHERS);
    List<ForecastEntity> dataList = forecastEntity.getList();

    weatherAdapter.setData(dataList);
  }

  private void bindCurrentItem(WeatherEntity currentWeather) {

    //  http://openweathermap.org/img/w/10n.png
    final String currUrl =
        "http://openweathermap.org/img/w/" + currentWeather.getIconUrl() + ".png";

    Log.e(TAG, currUrl);

    Picasso.with(getContext())
        .load(currUrl)
        .resize(headerAvatarSize, headerAvatarSize)
        .centerCrop()
        .into(currentIcon, new Callback() {
          @Override public void onSuccess() {

            if (attributionTextView.getVisibility() == View.GONE) {
              attributionTextView.setVisibility(VISIBLE);
              attributionTextView.setAlpha(0.0f);
              ViewCompat.animate(attributionTextView).alpha(1.0f).setDuration(800);
            }
          }

          @Override public void onError() {

          }
        });

    stateTextView.setText(currentWeather.getLocationName());
    currentTempTextView.setText(TemperatureFormatter.format(currentWeather.getCurrTemperature()));
  }

  public void setCurrentState(@Status int state) {

    WeatherLayout.this.currentState = state;

    switch (state) {
      case FETCH_LOCATION_ING:
        stateTextView.setText(getResources().getString(R.string.fetch_location_ing));
        break;
      case FETCH_LOCATION_ERROR:
        stateTextView.setText(getResources().getString(R.string.fetch_location_timeout));
        break;
      case FETCH_WEATHER_ING:
        stateTextView.setText(getResources().getString(R.string.fetch_weather_ing));
        break;
      case FETCH_WEATHER_ERROR:
        stateTextView.setText(getResources().getString(R.string.fetch_weather_error));
        break;
    }
  }

  public int getCurrentState() {
    return currentState;
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    ButterKnife.bind(WeatherLayout.this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    ButterKnife.unbind(WeatherLayout.this);
  }

  public void dismiss() {
    this.onItemClickListener = null;
    ((ViewGroup) getParent()).removeView(WeatherLayout.this);
  }

  public void addOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void removeOnItemClickListener() {
    this.onItemClickListener = null;
  }

  @OnClick(R.id.menu_weather_btn_confirm) public void onConfirmOnclick(View view) {

    if (onItemClickListener != null) {
      onItemClickListener.onWeatherConfirm();
    }
  }

  @OnClick(R.id.menu_weather_btn_refresh) public void onRefreshOnclick(View view) {

    if (onItemClickListener != null) {
      onItemClickListener.onWeatherRefresh();
    }
  }

  public interface OnItemClickListener {
    void onWeatherRefresh();

    void onWeatherConfirm();
  }

  class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private List<ForecastEntity> dataList = new ArrayList<>();
    private View rootView;
    int itemAvatarSize;

    public WeatherAdapter() {
      itemAvatarSize =
          getContext().getResources().getDimensionPixelSize(R.dimen.weather_item_avatar_size) - 10;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

      rootView =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_item, parent, false);

      return new ViewHolder(rootView);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {

      ForecastEntity forecastEntity = dataList.get(position);

      //openweathermap.org/img/w/10n.png
      String currUrl = "http://openweathermap.org/img/w/" + forecastEntity.getIconUrl() + ".png";
      Log.e(TAG, currUrl);

      Picasso.with(getContext())
          .load(currUrl)
          .resize(itemAvatarSize, itemAvatarSize)
          .centerCrop()
          .into(holder.icon);

      final DayFormatter dayFormatter = new DayFormatter(getContext());
      final String day = dayFormatter.format(forecastEntity.getDate());

      holder.day.setText(day);
      holder.maxTemp.setText(TemperatureFormatter.format(forecastEntity.getMaxTemperature()));
      holder.minTemp.setText(TemperatureFormatter.format(forecastEntity.getMinTemperature()));
    }

    @Override public int getItemCount() {
      return dataList.size();
    }

    public void setData(List<ForecastEntity> dataList) {

      WeatherAdapter.this.dataList = dataList;
      WeatherAdapter.this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

      @Nullable @Bind(R.id.weather_item_day) TextView day;
      @Nullable @Bind(R.id.weather_item_icon) ImageView icon;
      @Nullable @Bind(R.id.weather_item_max_temperature) TextView maxTemp;
      @Nullable @Bind(R.id.weather_item_min_temperature) TextView minTemp;

      public ViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
      }
    }
  }
}