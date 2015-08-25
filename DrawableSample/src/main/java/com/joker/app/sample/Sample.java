package com.joker.app.sample;

import android.util.Log;
import rx.Observable;

import java.util.concurrent.TimeUnit;
import rx.functions.Action1;
import rx.functions.Func1;

public class Sample {

  private static final String TAG = Sample.class.getSimpleName();

  public void start() {

    final Sources sources = new Sources();

    // Create our sequence for querying best available data
    final Observable<Data> source =
        Observable.concat(sources.memory(), sources.disk(), sources.network())
            .first(new Func1<Data, Boolean>() {
              @Override public Boolean call(Data data) {
                return data != null && data.isUpToDate();
              }
            });

    // "Request" latest data once a second
    Observable.interval(1, TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<Data>>() {
      @Override public Observable<Data> call(Long aLong) {
        return source;
      }
    }).subscribe(new Action1<Data>() {
      @Override public void call(Data data) {
        Log.e(TAG, "Received: " + data.value);
      }
    });

    // Occasionally clear memory (as if app restarted) so that we must go to disk
    Observable.interval(3, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
      @Override public void call(Long aLong) {
        sources.clearMemory();
      }
    });

    // Java will quit unless we idle
    sleep(15 * 1000);
  }

  static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      // Ignore
    }
  }
}
