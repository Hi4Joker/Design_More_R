package com.joker.supportdesign.mvp.presenter;

import com.joker.supportdesign.mvp.viewInterface.MvpView;

/**
 * Created by Joker on 2015/6/28.
 */
public interface MvpPresenter<T extends MvpView> {

  void attachView(T view);

  void detachView(boolean retainInstance);
}
