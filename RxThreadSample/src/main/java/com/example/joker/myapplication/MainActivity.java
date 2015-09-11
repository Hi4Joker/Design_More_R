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
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Notification;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
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


   /* final Subscription intervalSubscription =
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

    /*Observable.<Integer>empty()
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

  @OnClick(R.id.scan_operator) void onScanClick() {
    Observable.just(1, 2, 3).scan(new Func2<Integer, Integer, Integer>() {
      @Override public Integer call(Integer sum, Integer item) {

        Log.e(TAG, "sum: " + sum);
        Log.e(TAG, "item: " + item);

        return sum + item;
      }
    }).subscribe(new Subscriber<Integer>() {
      @Override public void onNext(Integer item) {
        Log.e(TAG, "onNext: " + item);
      }

      @Override public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
      }

      @Override public void onCompleted() {
        System.out.println("Sequence complete.");
      }
    });
  }

  @OnClick(R.id.distinct_operator) void onDistinctClick() {

    Observable.just(1, 2, 1, 1, 2, 3).distinct(new Func1<Integer, String>() {
      @Override public String call(Integer integer) {

        return null;
      }
    }).elementAtOrDefault(1, 6666).subscribe(new Subscriber<Integer>() {
      @Override public void onNext(Integer item) {
        System.out.println("Next: " + item);
      }

      @Override public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
      }

      @Override public void onCompleted() {
        System.out.println("Sequence complete.");
      }
    });
  }

  @OnClick(R.id.first_operator) void onFirstClick() {

    Observable.just(1, 2, 3, 4, 5).first().first(new Func1<Integer, Boolean>() {
      @Override public Boolean call(Integer integer) {

        /*如果没有符合条件item，则抛出"NoSuchElementException"*/
        return null;
      }
    }).subscribe(new Subscriber<Integer>() {
      @Override public void onNext(Integer item) {
        Log.e(TAG, "Next: " + item);
      }

      @Override public void onError(Throwable error) {
        Log.e(TAG, "Error: " + error.getMessage());
        error.printStackTrace();
      }

      @Override public void onCompleted() {
        Log.e(TAG, "Sequence complete.");
      }
    });
  }

  @OnClick(R.id.take_operator) void onTakeClick() {

    Observable.just(1, 2, 3, 4).take(1).takeFirst(new Func1<Integer, Boolean>() {
      @Override public Boolean call(Integer integer) {

        /*如果没有符合条件item，不抛出"NoSuchElementException"*/
        return null;
      }
    });
  }

  @OnClick(R.id.single_operator) void onSingleClick() {

    Observable.just(1, 2, 3, 4)/*single(new Func1<Integer, Boolean>() {
      @Override public Boolean call(Integer integer) {
        return integer < 4;
      }
    })*/.single().singleOrDefault(0, new Func1<Integer, Boolean>() {
      @Override public Boolean call(Integer integer) {
        return integer < 4;
      }
    }).subscribe(new Subscriber<Integer>() {
      @Override public void onNext(Integer item) {
        Log.e(TAG, "Next: " + item);
      }

      @Override public void onError(Throwable error) {
        Log.e(TAG, "Error: " + error.getMessage());
        error.printStackTrace();
      }

      @Override public void onCompleted() {
        Log.e(TAG, "Sequence complete.");
      }
    });

    /*Observable.just(1).onErrorResumeNext(new Func1<Throwable, Observable<? extends Integer>>() {
      @Override public Observable<? extends Integer> call(Throwable throwable) {
        return null;
      }
    }).onErrorResumeNext(Observable.<Integer>empty());

    Observable.just(1).onErrorReturn(new Func1<Throwable, Integer>() {
      @Override public Integer call(Throwable throwable) {
        return null;
      }
    });*/
  }

  @OnClick(R.id.publish_operator) void onPublishClick() {

    Observable.just(1).publish().share();
  }

  @OnClick(R.id.do_operator) void onDoClick() {

    Observable.just(1, 2).doOnNext(new Action1<Integer>() {
      @Override public void call(Integer integer) {
        Log.e(TAG, "doOnNext");
      }
    }).doOnEach(new Action1<Notification<? super Integer>>() {
      @Override public void call(Notification<? super Integer> notification) {

        /*发射任何一个事件都会触发，包括observable.onCompleted()也会触发*/
        Log.e(TAG, "notification");
      }
    }).doOnTerminate(new Action0() {
      @Override public void call() {
        Log.e(TAG, "doOnTerminate");
      }
    }).doOnCompleted(new Action0() {
      @Override public void call() {

        Log.e(TAG, "doOnCompleted");
      }
    }).onErrorResumeNext(new Func1<Throwable, Observable<? extends Integer>>() {
      @Override public Observable<? extends Integer> call(Throwable throwable) {

        return Observable.empty();
      }
    }).onErrorReturn(new Func1<Throwable, Integer>() {
      @Override public Integer call(Throwable throwable) {

        return null;
      }
    }).finallyDo(new Action0() {
      @Override public void call() {

        Log.e(TAG, "finallyDo");
      }
    }).doOnUnsubscribe(new Action0() {
      @Override public void call() {
        Log.e(TAG, "doOnUnsubscribe");
      }
    }).subscribe(new Subscriber<Integer>() {
      @Override public void onCompleted() {

        Log.e(TAG, "onCompleted");
      }

      @Override public void onError(Throwable e) {

        Log.e(TAG, "onError");
      }

      @Override public void onNext(Integer integer) {

        Log.e(TAG, "onNext");
      }
    });
  }

  @OnClick(R.id.sub_operator) void onSubClick() {

    subscription = Observable.interval(1, 1, TimeUnit.SECONDS).doOnUnsubscribe(new Action0() {
      @Override public void call() {
        Log.e(TAG, "doOnUnsubscribe");
      }
    }).subscribe(new Subscriber<Long>() {
      @Override public void onCompleted() {

      }

      @Override public void onError(Throwable e) {

      }

      @Override public void onNext(Long aLong) {

        Log.e(TAG, aLong + "");
      }
    });
  }

  @OnClick(R.id.unsub_operator) void onUnSubClick() {

    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }

  @OnClick(R.id.to_operator) void onToClick() {

    Integer[] items = new Integer[] { 1, 2, 3, 4, 5, 6 };

   /* Observable.from(items).subscribe(new Action1<Integer>() {
      @Override public void call(Integer integer) {
        Log.e(TAG, "integer: " + integer);
      }
    });*/

    Observable.from(items)
        .toList()
        .reduce(new Func2<List<Integer>, List<Integer>, List<Integer>>() {
          @Override public List<Integer> call(List<Integer> integers, List<Integer> integers2) {
            return integers;
          }
        })
        .subscribe(new Action1<List<Integer>>() {
          @Override public void call(List<Integer> integers) {

            Log.e(TAG, "size: " + integers.size());
            for (Integer integer : integers) {
              Log.e(TAG, "integer: " + integer);
            }
          }
        });
  }

  @OnClick(R.id.blocking_operator) void onBlockingClick() {

    Integer[] items = new Integer[] { 1, 2, 3, 4, 5, 6 };

    int i = Observable.from(items).map(new Func1<Integer, Integer>() {
      @Override public Integer call(Integer integer) {
        Log.e(TAG, "integer: " + integer);
        return integer;
      }
    }).subscribeOn(Schedulers.io()).toBlocking().first();
  }
}
