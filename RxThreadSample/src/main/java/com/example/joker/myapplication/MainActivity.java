package com.example.joker.myapplication;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.joker.myapplication.schedulers.HandlerScheduler;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private final CompositeSubscription compositeSubscription = new CompositeSubscription();

  private Handler backgroundHandler;
  private Subscription subscription = Subscriptions.empty();
  private Subscription connect;

  static class BackgroundThread extends HandlerThread {
    BackgroundThread() {
      super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
    }
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(MainActivity.this);

    BackgroundThread backgroundThread = new BackgroundThread();
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
  }

  @OnClick(R.id.publish) void onPublishClick() {

    BehaviorSubject<Observable<String>> hiddenSubject =
        BehaviorSubject.create(Observable.defer(new Func0<Observable<String>>() {
          @Override public Observable<String> call() {
            return Observable.just("default");
          }
        }));

    Subject<Observable<String>, Observable<String>> subject = hiddenSubject.toSerialized();

    Observable<String> output = subject.onBackpressureBuffer()
        .concatMap(new Func1<Observable<String>, Observable<String>>() {
          @Override public Observable<String> call(Observable<String> observable) {
            return observable;
          }
        });

    output.subscribe(new Action1<String>() {
      @Override public void call(String o) {
        System.out.println(o);
      }
    });

    subject.onNext(Observable.just("zero"));
    subject.onNext(Observable.just("one"));

    output.subscribe(new Action1<String>() {
      @Override public void call(String o) {
        System.out.println(o);
      }
    });

    subject.onNext(Observable.just("two"));
    subject.onNext(Observable.just("three"));

    /*****************************************************************************/
    /*****************************************************************************/
    /*****************************************************************************/

   /* Observable<String> defaultValueObservable = Observable.defer(new Func0<Observable<String>>() {
      @Override public Observable<String> call() {
        // simulate blocking work
        return Observable.just("blocking-default");
      }
    });

    // observer will receive the "blocking-default", "zero", "one"
    SerializedSubject<Observable<String>, Observable<String>> subject =
        BehaviorSubject.create(defaultValueObservable.single()).toSerialized();

    Observable<String> stringObservable = subject.onBackpressureBuffer()
        .concatMap(new Func1<Observable<String>, Observable<String>>() {
          @Override public Observable<String> call(Observable<String> stringObservable) {
            return stringObservable;
          }
        });

    stringObservable.subscribe(new Action1<Object>() {
      @Override public void call(Object s) {
        System.out.println(s);
      }
    });

    subject.onNext(Observable.just("0"));
    subject.onNext(Observable.just("1"));

    // observer2 will receive the "one", "two", "three"
    stringObservable.subscribe(new Action1<String>() {
      @Override public void call(String s) {
        System.out.println(s);
      }
    });
    subject.onNext(Observable.just("2"));
    subject.onNext(Observable.just("3"));*/

    /*****************************************************************************/
    /*****************************************************************************/
    /*****************************************************************************/

    Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
      @Override public void call(Subscriber<? super String> subscriber) {

        int i = 0;
        Log.e(TAG, "start call");
        while (!subscriber.isUnsubscribed()) {
          //subscriber.onNext("item " + i);
          subscriber.onNext("" + i);
          i++;

          if (i == 10) subscriber.unsubscribe();
        }
        Log.e(TAG, "completed");
        subscriber.onCompleted();
      }
    })/*.observeOn(Schedulers.newThread()).subscribeOn(Schedulers.newThread())*/.publish();

    observable.take(10).subscribe(new Subscriber<String>() {

      @Override public void onStart() {

        Log.e(TAG, "onStart");

        /*如果onnext不再调用request()，则订阅直接完成*/
        request(2);
      }

      @Override public void onCompleted() {

       /* Log.e(TAG, "isUnsubscribed : " + connect.isUnsubscribed());
        if (connect != null && !connect.isUnsubscribed()) {
          connect.unsubscribe();
        }*/

        Log.e(TAG, "onCompleted");
      }

      @Override public void onError(Throwable e) {
        Log.e(TAG, "onError : " + e.getMessage());

        e.printStackTrace();
      }

      @Override public void onNext(String s) {

        Log.e(TAG, "item received : " + String.valueOf(s));

        if (Integer.parseInt(s) == 1) {
          SystemClock.sleep(3000);
          request(1);
        } else if (Integer.parseInt(s) < 7 && Integer.parseInt(s) >= 2) {

          SystemClock.sleep(1000);
          request(1);
        }
      }
    });

    /*HandlerScheduler.from(new Handler(Looper.getMainLooper()))
        .createWorker()
        .schedule(new Action0() {
          @Override public void call() {

            Log.e(TAG, "isUnsubscribed : " + subscription.isUnsubscribed());
            if (!subscription.isUnsubscribed()) {
              subscription.unsubscribe();
            }
          }
        }, 3 * 1000, TimeUnit.MILLISECONDS);*/

    connect = ((ConnectableObservable) observable).connect();

    connect.unsubscribe();
  }
}
