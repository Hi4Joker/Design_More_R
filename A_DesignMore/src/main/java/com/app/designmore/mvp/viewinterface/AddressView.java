package com.app.designmore.mvp.viewinterface;

import com.app.designmore.retrofit.entity.Province;
import com.app.designmore.view.CustomWheelPicker;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joker on 2015/9/9.
 */
public interface AddressView {

  void showProgress();

  void hideProgress();

  void onInflateFinish(List<Province> provinces);

  void showError();
}
