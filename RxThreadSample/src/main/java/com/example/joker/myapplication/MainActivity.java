package com.example.joker.myapplication;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.joker.myapplication.schedulers.AndroidSchedulers;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.schedulers.TimeInterval;
import rx.subscriptions.BooleanSubscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private final CompositeSubscription compositeSubscription = new CompositeSubscription();

  private Observable<String> observable0;
  private Observable<String> observable1;
  private Handler backgroundHandler;
  private Subscription subscription = Subscriptions.empty();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(MainActivity.this);

    BackgroundThread backgroundThread = new BackgroundThread();
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());

    observable1 = Observable.create(new Observable.OnSubscribe<String>() {
      @Override public void call(Subscriber<? super String> subscriber) {

        if (!subscriber.isUnsubscribed()) {
          String s = "6666";
          Log.e(TAG, s + " run on which thread id was:" + Thread.currentThread().getId());
          subscriber.onNext(s);
          subscriber.onCompleted();
        }
      }
    });
  }

  static class BackgroundThread extends HandlerThread {
    BackgroundThread() {
      super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
    }
  }

  @Nullable @OnClick(R.id.defer) public void onDeferClick() {

    /*取决于第一个subscribeOn()*/
    observable0 = Observable.defer(new Func0<Observable<String>>() {
      @Override public Observable<String> call() {
        return Observable.concat(Observable.just("0"), Observable.just("1"), Observable.just("2"))
            .first(new Func1<String, Boolean>() {
              @Override public Boolean call(String s) {

                Log.e(TAG, s + " run on which thread id was:" + Thread.currentThread().getId());
                return s.equals("1");
              }
            });
      }
    }).subscribeOn(AndroidSchedulers.mainThread()).map(new Func1<String, String>() {
      @Override public String call(String s) {

        Log.e(TAG, " map operator run on which thread id was:" + Thread.currentThread().getId());
        return s;
      }
    }).subscribeOn(Schedulers.newThread()).map(new Func1<String, String>() {
      @Override public String call(String s) {

        Log.e(TAG, " map operator run on which thread id was:" + Thread.currentThread().getId());
        return s;
      }
    }).subscribeOn(Schedulers.newThread()).flatMap(new Func1<String, Observable<String>>() {
      @Override public Observable<String> call(String s) {

        Log.e(TAG,
            " flatMap operator run on which thread id was:" + Thread.currentThread().getId());
        return Observable.just("just").subscribeOn(Schedulers.newThread());
      }
    }).map(new Func1<String, String>() {
      @Override public String call(String s) {
        Log.e(TAG, " just operator run on which thread id was:" + Thread.currentThread().getId());
        return s;
      }
    });

    compositeSubscription.add(observable0.subscribe(new Subscriber<String>() {
      @Override public void onCompleted() {

        Log.e(TAG, "onCompleted");
      }

      @Override public void onError(Throwable e) {

        Log.e(TAG, e.toString());
        e.printStackTrace();
      }

      @Override public void onNext(String s) {

        Log.e(TAG, s + " on next which thread id was:" + Thread.currentThread().getId());
      }
    }));
  }

  @Nullable @OnClick(R.id.another_defer) public void onAnotherDeferClick() {

    compositeSubscription.add(observable1.compose(SchedulersCompat.<String>applyNewSchedulers())
        .subscribe(new Subscriber<String>() {
          @Override public void onCompleted() {

            Log.e(TAG, "onCompleted");
          }

          @Override public void onError(Throwable e) {

            Log.e(TAG, e.toString());
            e.printStackTrace();
          }

          @Override public void onNext(String s) {

            Log.e(TAG, s + " on next which thread id was:" + Thread.currentThread().getId());
          }
        }));
  }

  @Nullable @OnClick(R.id.scheduler_defer) public void onSchedulerClick() {
    final Scheduler.Worker worker = Schedulers.newThread().createWorker();
    final Subscription workSubscription = worker.schedule(new Action0() {
      @Override public void call() {

        if (!worker.isUnsubscribed()) {
          Log.e(TAG, "Worker on next which thread id was:" + Thread.currentThread().getId());
          worker.unsubscribe();
        }
      }
    });

    subscription = Observable.interval(1, TimeUnit.SECONDS).doOnUnsubscribe(new Action0() {
      @Override public void call() {
        Log.e(TAG, "时间重置");
      }
    }).compose(SchedulersCompat.<Long>applyTrampolineSchedulers()).subscribe(new Action1<Long>() {
      @Override public void call(Long aLong) {
        //Log.e(TAG, aLong + "Unsubscribed ?" + subscription.isUnsubscribed());
        Log.e(TAG, "time ?" + aLong);
        //Log.e(TAG, "intervalSubscription thread id:" + Thread.currentThread().getId());
        if (aLong == 10 && !subscription.isUnsubscribed()) subscription.unsubscribe();
      }
    }, new Action1<Throwable>() {
      @Override public void call(Throwable throwable) {

      }
    });


/*    final Subscription intervalSubscription =
        Observable.interval(1, TimeUnit.SECONDS)
            .doOnUnsubscribe(new Action0() {
              @Override public void call() {
                Log.e(TAG, "时间重置");
              }
            })
            .compose(SchedulersCompat.<Long>applyTrampolineSchedulers())
            .subscribe(new Action1<Long>() {
              @Override public void call(Long aLong) {
                //Log.e(TAG, aLong + "Unsubscribed ?" + subscription.isUnsubscribed());
                Log.e(TAG, "time ?" + aLong);
                //Log.e(TAG, "intervalSubscription thread id:" + Thread.currentThread().getId());
              }
            }, new Action1<Throwable>() {
              @Override public void call(Throwable throwable) {

              }
            });*/

    /*Observable.just(1).timeInterval().subscribe(new Action1<TimeInterval<Integer>>() {
      @Override public void call(TimeInterval<Integer> integerTimeInterval) {

        integerTimeInterval.getIntervalInMilliseconds();
      }
    });*/

   /* Observable.<Integer>empty()
        .delay(10, TimeUnit.SECONDS)
        .compose(SchedulersCompat.<Integer>applyTrampolineSchedulers())
        .subscribe(new Action1<Integer>() {
          @Override public void call(Integer integer) {
          }
        }, new Action1<Throwable>() {
          @Override public void call(Throwable throwable) {
          }
        }, new Action0() {
          @Override public void call() {
            if (!intervalSubscription.isUnsubscribed()) {
              intervalSubscription.unsubscribe();
            }
            Log.e(TAG, "Unsubscribed ?" + subscription.isUnsubscribed());
          }
        });*/
  }
}
