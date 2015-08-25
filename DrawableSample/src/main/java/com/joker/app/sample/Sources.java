package com.joker.app.sample;

import android.util.Log;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Simulates three different sources - one from memory, one from disk,
 * and one from network. In reality, they're all in-memory, but let's
 * play pretend.
 *
 * Observable.create() is used so that we always return the latest data
 * to the subscriber; if you use just() it will only return the data from
 * a certain point in time.
 */

public class Sources {

  private static final String TAG = "Sample";
  // Memory cache of data
  private Data memory = null;

  // What's currently "written" on disk
  private Data disk = null;

  // Each "network" response is different
  private int requestNumber = 0;

  // In order to simulate memory being cleared, but data still on disk
  public void clearMemory() {
    Log.e(TAG, "Wiping memory...");
    memory = null;
  }

  public Observable<Data> memory() {

    Observable<Data> observable = Observable.create(new Observable.OnSubscribe<Data>() {
      @Override public void call(Subscriber<? super Data> subscriber) {
        subscriber.onNext(memory);
        subscriber.onCompleted();
      }
    });

    return observable;

    //return observable.compose(logSource("MEMORY"));
  }

  public Observable<Data> disk() {

    Observable<Data> observable = Observable.create(new Observable.OnSubscribe<Data>() {
      @Override public void call(Subscriber<? super Data> subscriber) {
        subscriber.onNext(disk);
        subscriber.onCompleted();
      }
    });

    // Cache disk responses in memory
    return observable.doOnNext(new Action1<Data>() {
      @Override public void call(Data data) {
        memory = data;
      }
    });

    //return observable.doOnNext(data -> memory = data).compose(logSource("DISK"));
  }

  public Observable<Data> network() {

    Observable<Data> observable = Observable.create(new Observable.OnSubscribe<Data>() {
      @Override public void call(Subscriber<? super Data> subscriber) {
        requestNumber++;
        subscriber.onNext(new Data("Server Response #" + requestNumber));
        subscriber.onCompleted();
      }
    });

    // Save network responses to disk and cache in memory
    return observable.doOnNext(new Action1<Data>() {
      @Override public void call(Data data) {
        disk = data;
        memory = data;
      }
    });
  }

  // Simple logging to let us know what each source is returning
  private Observable.Transformer<Data, Data> logSource(final String source) {

    return new Observable.Transformer<Data, Data>() {
      @Override public Observable<Data> call(Observable<Data> dataObservable) {

        return dataObservable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
      }
    };


    /*return (Observable.Transformer<Data, Data>) Observable.create(
        new Observable.OnSubscribe<Data>() {
          @Override public void call(Subscriber<? super Data> subscriber) {

          }
        }).doOnNext(new Action1<Data>() {
      @Override public void call(Data data) {

        if (data == null) {
          System.out.println(source + " does not have any data.");
        } else if (!data.isUpToDate()) {
          System.out.println(source + " has stale data.");
        } else {
          System.out.println(source + " has the data you are looking for!");
        }
      }
    });*/



 /*return dataObservable -> {
      return dataObservable.doOnNext(data -> {
        if (data == null) {
          System.out.println(source + " does not have any data.");
        } else if (!data.isUpToDate()) {
          System.out.println(source + " has stale data.");
        } else {
          System.out.println(source + " has the data you are looking for!");
        }
      });
    };*/

  }
}
