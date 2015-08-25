package com.joker.app.rx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Joker on 2015/8/10.
 */
public class SchedulersCompat {

  private static final Observable.Transformer schedulersTransformer = new Observable.Transformer() {
    @Override public Object call(Object observable) {

      return ((Observable) observable).subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread());
    }
  };


  /**
   * Don't break the chain: use RxJava's compose() operator
   */
 /* public static <T> Observable.Transformer<T, T> applySchedulers() {
    return new Observable.Transformer<T, T>() {
      @Override public Observable<T> call(Observable<T> observable) {
        return observable.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread());
      }
    };
  }*/
  public static <T> Observable.Transformer<T, T> applySchedulers() {

    return (Observable.Transformer<T, T>) schedulersTransformer;
  }
}
