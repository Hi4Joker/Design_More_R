package com.joker.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.joker.app.sample.Data;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Joker on 2015/8/13.
 */
public class RxActivity extends AppCompatActivity {
  private static final String RXTAG = RxActivity.class.getSimpleName();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.e(RXTAG, "mainThread id::" + Thread.currentThread().getId());

    Observable.defer(new Func0<Observable<String>>() {
      @Override public Observable<String> call() {
        return Observable.concat(Observable.just("0"), Observable.just("1"), Observable.just("2"))
            .first(new Func1<String, Boolean>() {
              @Override public Boolean call(String s) {

                Log.e(RXTAG, "第零个 Thread id::" + Thread.currentThread().getId());
                return s.equals("2");
              }
            });
      }
    }).compose(new Observable.Transformer<String, String>() {
      @Override public Observable<String> call(Observable<String> stringObservable) {

        return stringObservable.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread());
      }
    }).subscribe(new Subscriber<String>() {
      @Override public void onCompleted() {

        Log.e(RXTAG, "onCompleted");
      }

      @Override public void onError(Throwable e) {

        Log.e(RXTAG, e.toString());
        e.printStackTrace();
      }

      @Override public void onNext(String s) {

        Log.e(RXTAG, "s:" + s);
        Log.e(RXTAG, "onNext Thread id::" + Thread.currentThread().getId());
      }
    });

    Observable.merge(Observable.just(1), Observable.just(2));
    Observable.concat(Observable.just(1), Observable.just(2));

    Observable.just(1, 2, 3).distinct().onErrorReturn(new Func1<Throwable, Integer>() {
      @Override public Integer call(Throwable throwable) {
        return null;
      }
    }).onErrorResumeNext(new Func1<Throwable, Observable<? extends Integer>>() {
      @Override public Observable<? extends Integer> call(Throwable throwable) {
        return null;
      }
    });

    Observable.just(1, 2).single().forEach(new Action1<Integer>() {
      @Override public void call(Integer integer) {

      }
    }, new Action1<Throwable>() {
      @Override public void call(Throwable throwable) {

      }
    });

    Data someSource = new Data("");


 /*   Observable.from(someSource)
        .map(data -> manipulate(data))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(data -> doSomething(data));*/

   /* Observable.from(someSource)
        .map(new Func1<Data, Data>() {
          @Override public Data call(Data data) {
            return manipulate(data);
          }
        })
        .subscribe(new Action1<Data>() {
          @Override public void call(Data data) {
            doSomething(data);
          }
        });*/

    /*applySchedulers(Observable.from(someSource).map(new Func1<Data, Data>() {
      @Override public Data call(Data data) {
        return manipulate(data);
      }
    })).subscribe(new Action1<Data>() {
      @Override public void call(Data data) {
        doSomething(data);
      }
    });

    Observable.from(someSource).map(new Func1<Data, Data>() {
      @Override public Data call(Data data) {
        return manipulate(data);
      }
    })
        .compose(this.<YourType>applySchedulers())
        .subscribe(new Action1<Data>() {
          @Override public void call(Data data) {
            doSomething(data);
          }
        });*/

    Collections.emptyList();


  }

  /*<T> Observable<T> applySchedulers(Observable<T> observable) {
    return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }*/

  /*<T> Observable.Transformer<T, T> applySchedulers() {
    return new Transformer<T, T>() {
      @Override public Observable<T> call(Observable<T> observable) {
        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
      }
    };
  }*/
}
