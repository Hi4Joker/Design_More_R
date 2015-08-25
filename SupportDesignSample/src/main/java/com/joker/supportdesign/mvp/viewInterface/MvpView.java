package com.joker.supportdesign.mvp.viewInterface;

/**
 * Created by Joker on 2015/6/28.
 */
public interface MvpView {

  void showProgress();

  void hideProgress();

  void showMessage(int resString);
}
