package com.app.designmore.mvp.viewinterface;

import com.app.designmore.retrofit.entity.Province;
import com.app.designmore.view.CustomWheelPicker;
import java.util.ArrayList;

/**
 * Created by Joker on 2015/9/9.
 */
public interface AddressView {

  void showProgress();

  void hideProgress();

  void onInflateFinish(ArrayList<Province> provinces);

  void showError();
}
