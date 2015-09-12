package com.app.designmore.rxAndroid;

import rx.Observer;

/**
 * Created by Joker on 08.08.15.
 */
public class SimpleObserver<T> implements Observer<T> {
  @Override public void onCompleted() {
  }

  @Override public void onError(Throwable e) {
    e.printStackTrace();
  }

  @Override public void onNext(T o) {

  }
}
