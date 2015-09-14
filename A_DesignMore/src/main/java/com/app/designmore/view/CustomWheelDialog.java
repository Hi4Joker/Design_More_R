package com.app.designmore.view;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.Province;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.wheel.OnWheelChangedListener;
import com.app.designmore.view.wheel.OnWheelClickedListener;
import com.app.designmore.view.wheel.WheelView;
import com.app.designmore.view.wheel.adapter.AbstractWheelTextAdapter;
import java.util.ArrayList;
import java.util.List;

public class CustomWheelDialog extends Dialog {

  private static final String TAG = CustomWheelDialog.class.getSimpleName();
  private final static int DEFAULT_VISIBLE_ITEMS = 5;

  private Activity activity;
  private ArrayList<Province> provinces = new ArrayList<>();
  private ArrayList<Province.City> cities = new ArrayList<>();
  private AbstractWheelTextAdapter provinceAdapter;
  private AbstractWheelTextAdapter cityAdapter;
  private WheelView provinceWheel;
  private WheelView citiesWheel;
  private Callback callback;

  private OnWheelClickedListener wheelClickedListener = new OnWheelClickedListener() {
    @Override public void onItemClicked(WheelView wheel, int itemIndex) {
      if (itemIndex != wheel.getCurrentItem()) {
        wheel.setCurrentItem(itemIndex, true, Constants.MILLISECONDS_300);
      }
    }
  };

  private OnWheelChangedListener wheelChangedListener = new OnWheelChangedListener() {
    @Override public void onChanged(WheelView wheel, int oldValue, int newValue) {
      cities.clear();
      cities.addAll(provinces.get(newValue).getCities());
      citiesWheel.invalidateWheel(true);
      citiesWheel.setCurrentItem(0, false);
    }
  };

  public CustomWheelDialog(Activity activity, List<Province> provinces, final Callback callback) {
    super(activity);
    getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    getWindow().setGravity(Gravity.BOTTOM);
    getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    getWindow().setWindowAnimations(R.style.AnimBottom);
    View rootView = getLayoutInflater().inflate(R.layout.custom_wheel_layout, null);
    LayoutParams params =
        new LayoutParams(DensityUtil.getScreenWidth(activity), LayoutParams.MATCH_PARENT);
    super.setContentView(rootView, params);

    this.activity = activity;
    this.callback = callback;
    this.provinces.addAll(provinces);

    /*设置dialog不能取消*/
    CustomWheelDialog.this.setCancelable(false);
    CustomWheelDialog.this.setCanceledOnTouchOutside(false);

    /*初始化View*/
    CustomWheelDialog.this.initView();
  }

  private void initView() {

    provinceWheel = (WheelView) findViewById(R.id.wheel_layout_province_wheel);
    citiesWheel = (WheelView) findViewById(R.id.wheel_layout_city_wheel);

    CustomWheelDialog.this.setupAdapter();
  }

  private void setupAdapter() {

    provinceAdapter = new AbstractWheelTextAdapter(activity, R.layout.wheel_text_item) {
      @Override public int getItemsCount() {
        return provinces.size();
      }

      @Override protected CharSequence getItemText(int index) {
        return provinces.get(index).getProvinceName();
      }
    };

    cityAdapter = new AbstractWheelTextAdapter(activity, R.layout.wheel_text_item) {
      @Override public int getItemsCount() {
        return cities.size();
      }

      @Override protected CharSequence getItemText(int index) {
        return cities.get(index).cityName;
      }
    };

    provinceWheel.setViewAdapter(provinceAdapter);
    provinceWheel.setCyclic(false);
    provinceWheel.setVisibleItems(DEFAULT_VISIBLE_ITEMS);

    citiesWheel.setViewAdapter(cityAdapter);
    citiesWheel.setCyclic(false);
    citiesWheel.setVisibleItems(DEFAULT_VISIBLE_ITEMS);
  }

  public void updateDefault(Province defaultProvince, Province.City defaultCity) {

    int provinceItem = 0;
    int cityItem = 0;

    if (defaultProvince == null) {
      defaultProvince = provinces.get(0);
    } else {
      for (int i = 0; i < provinces.size(); i++) {
        if (provinces.get(i).getProvinceId().equals(defaultProvince.getProvinceId())) {
          provinceItem = i;
          break;
        }
      }
    }

    cities.clear();
    cities.addAll(defaultProvince.getCities());

    if (defaultCity != null) {
      for (int i = 0; i < cities.size(); i++) {
        if (cities.get(i).cityId.equals(defaultCity.cityId)) {
          cityItem = i;
          break;
        }
      }
    }

    provinceWheel.setCurrentItem(provinceItem, false);
    citiesWheel.setCurrentItem(cityItem, false);
  }

  @Nullable @OnClick(R.id.wheel_layout_done_btn) void onDoneClick() {
    if (callback != null) {
      Province province =
          CustomWheelDialog.this.provinces.size() > 0 ? CustomWheelDialog.this.provinces.get(
              provinceWheel.getCurrentItem()) : null;
      Province.City city = cities.size() > 0 ? cities.get(citiesWheel.getCurrentItem()) : null;
      callback.onPicked(province, city);
    }
    CustomWheelDialog.this.dismiss();
  }

  @Nullable @OnClick(R.id.wheel_layout_cancel_btn) void onCancelClick() {
    CustomWheelDialog.this.dismiss();
  }

  public interface Callback {
    void onPicked(Province selectProvince, Province.City selectCity);
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    ButterKnife.bind(CustomWheelDialog.this);
    provinceWheel.addClickingListener(wheelClickedListener);
    citiesWheel.addClickingListener(wheelClickedListener);
    provinceWheel.addChangingListener(wheelChangedListener);
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    provinceWheel.removeClickingListener(wheelClickedListener);
    citiesWheel.removeClickingListener(wheelClickedListener);
    provinceWheel.removeChangingListener(wheelChangedListener);
    ButterKnife.unbind(CustomWheelDialog.this);
  }
}
