package com.joker.supportdesign.mvp.presenter;

import com.joker.supportdesign.mvp.viewInterface.MvpView;

/**
 * Created by Administrator on 2015/6/29.
 */
public interface FragmentPresenter<T extends MvpView> extends MvpPresenter<T> {

  void inflateData();
}
