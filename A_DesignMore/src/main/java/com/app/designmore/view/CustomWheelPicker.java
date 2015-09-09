package com.app.designmore.view;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
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

public class CustomWheelPicker extends Dialog {

  private final static int DEFAULT_VISIBLE_ITEMS = 5;

  private Activity context;

  private ArrayList<Province> provinces = new ArrayList<>();
  private ArrayList<Province.City> cities = new ArrayList<>();
  private AbstractWheelTextAdapter provinceAdapter;
  private AbstractWheelTextAdapter cityAdapter;
  private WheelView provinceWheel;
  private WheelView citiesWheel;

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

  public CustomWheelPicker(Activity context, List<Province> provinces, final Callback listener) {
    super(context);
    getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    getWindow().setGravity(Gravity.BOTTOM);
    getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    getWindow().setWindowAnimations(R.style.AnimBottom);
    View rootView = getLayoutInflater().inflate(R.layout.wheel_layout, null);
    int screenWidth = DensityUtil.getScreenWidth(context);
    LayoutParams params = new LayoutParams(screenWidth, LayoutParams.MATCH_PARENT);
    super.setContentView(rootView, params);

    this.context = context;
    /*设置dialog不能取消*/
    CustomWheelPicker.this.setCancelable(false);

    CustomWheelPicker.this.initView();
    CustomWheelPicker.this.setListener(listener);
    this.provinces.addAll(provinces);
  }

  private void initView() {

    provinceWheel = (WheelView) findViewById(R.id.wheel_layout_province_wheel);
    citiesWheel = (WheelView) findViewById(R.id.wheel_layout_city_wheel);

    CustomWheelPicker.this.setupAdapter();
  }

  private void setupAdapter() {

    provinceAdapter = new AbstractWheelTextAdapter(context, R.layout.wheel_text_item) {
      @Override public int getItemsCount() {
        return provinces.size();
      }

      @Override protected CharSequence getItemText(int index) {
        return provinces.get(index).getProvinceName();
      }
    };

    cityAdapter = new AbstractWheelTextAdapter(context, R.layout.wheel_text_item) {
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

  private void setListener(final Callback callback) {
    findViewById(R.id.wheel_layout_done_btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (callback != null) {
          Province province =
              CustomWheelPicker.this.provinces.size() > 0 ? CustomWheelPicker.this.provinces.get(
                  provinceWheel.getCurrentItem()) : null;
          Province.City city = cities.size() > 0 ? cities.get(citiesWheel.getCurrentItem()) : null;
          callback.onPicked(province, city);
        }
        CustomWheelPicker.this.dismiss();
      }
    });

    findViewById(R.id.wheel_layout_cancel_btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        CustomWheelPicker.this.dismiss();
      }
    });

    provinceWheel.addClickingListener(wheelClickedListener);
    citiesWheel.addClickingListener(wheelClickedListener);
    provinceWheel.addChangingListener(wheelChangedListener);
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

  public interface Callback {
    void onPicked(Province selectProvince, Province.City selectCity);
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public void destroy() {
    provinceWheel.removeClickingListener(wheelClickedListener);
    citiesWheel.removeClickingListener(wheelClickedListener);
    provinceWheel.removeChangingListener(wheelChangedListener);
  }
}
