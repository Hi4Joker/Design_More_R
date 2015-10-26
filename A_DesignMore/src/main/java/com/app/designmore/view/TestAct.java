package com.app.designmore.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import com.app.designmore.rxAndroid.schedulers.HandlerScheduler;
import java.util.concurrent.atomic.AtomicInteger;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;

/**
 * Created by Joker on 2015/10/23.
 */
public class TestAct extends AppCompatActivity {

  private final SerializedSubject<ACTEvent, ACTEvent> lifecycleSubject =
      new SerializedSubject<>(BehaviorSubject.<ACTEvent>create());
  private final RecyclerView generousRecyclerView = null;

  @Override protected void onDestroy() {
    super.onDestroy();

    lifecycleSubject.onNext(ACTEvent.Destroy);
  }

  enum ACTEvent {
    Destroy
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Observable.create(new Observable.OnSubscribe<int[]>() {
      @Override public void call(final Subscriber<? super int[]> subscriber) {

        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final int[] scrollEvent = new int[2];

        if (Looper.myLooper() != Looper.getMainLooper()) {
          throw new IllegalStateException("请检查线程，并在主线程中调用");
        }

        final RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
          @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!subscriber.isUnsubscribed()) {

              scrollEvent[0] = dx;
              scrollEvent[1] = dy;

              subscriber.onNext(scrollEvent);
            }
          }
        };
        generousRecyclerView.addOnScrollListener(listener);

        subscriber.add(new Subscription() {
          @Override public void unsubscribe() {

            atomicInteger.getAndIncrement();

            if (Looper.myLooper() == Looper.getMainLooper()) {
              generousRecyclerView.clearOnScrollListeners();
            } else {
              HandlerScheduler.from(new Handler(Looper.getMainLooper()))
                  .createWorker()
                  .schedule(new Action0() {
                    @Override public void call() {
                      generousRecyclerView.clearOnScrollListeners();
                    }
                  });
            }
          }

          @Override public boolean isUnsubscribed() {
            return atomicInteger.get() != 0;
          }
        });
      }
    }).skip(1).compose(new Observable.Transformer<int[], int[]>() {
      @Override public Observable<int[]> call(Observable<int[]> transformer) {

        return transformer.takeUntil(lifecycleSubject.takeFirst(new Func1<ACTEvent, Boolean>() {
          @Override public Boolean call(ACTEvent actEvent) {
            return ACTEvent.Destroy == actEvent;
          }
        }));
      }
    }).forEach(new Action1<int[]>() {
      @Override public void call(int[] recyclerViewScrollEvent) {

        int dx = recyclerViewScrollEvent[0];
        int dy = recyclerViewScrollEvent[1];
      }
    });
  }
}

