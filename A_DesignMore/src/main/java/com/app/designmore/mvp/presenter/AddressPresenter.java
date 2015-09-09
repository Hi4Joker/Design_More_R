package com.app.designmore.mvp.presenter;

import android.content.Context;
import com.app.designmore.mvp.viewinterface.AddressView;

/**
 * Created by Joker on 2015/9/9.
 */
public interface AddressPresenter {

  void attach(Context context, AddressView addressView);

  void detach();

  void showPicker();
}
