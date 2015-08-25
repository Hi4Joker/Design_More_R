package com.morihacky.android.rxjava;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.morihacky.android.rxjava.wiring.LogAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.observables.MathObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;

public class ExponentialBackoffFragment
      extends BaseFragment {

    @InjectView(R.id.list_threading_log) ListView _logList;
    private LogAdapter _adapter;
    private List<String> _logs;

    private CompositeSubscription _subscriptions = new CompositeSubscription();

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(_subscriptions);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _setupLogger();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_exponential_backoff, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();

        RxUtils.unsubscribeIfNotNull(_subscriptions);
    }

    // -----------------------------------------------------------------------------------

    @OnClick(R.id.btn_eb_retry)
    public void startRetryingWithExponentialBackoffStrategy() {
        _logs = new ArrayList<>();
        _adapter.clear();

        _subscriptions.add(//
              Observable//
                    .error(new RuntimeException("testing")) // always fails
                    .retryWhen(new RetryWithDelay(5, 1000))//
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            _log("Attempting the impossible 5 times in intervals of 1s");
                        }
                    })//
                    .subscribe(new Observer<Object>() {
                        @Override
                        public void onCompleted() {
                            Timber.d("on Completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            _log("Error: I give up!");
                        }

                        @Override
                        public void onNext(Object aVoid) {
                            Timber.d("on Next");
                        }
                    }));
    }

    @OnClick(R.id.btn_eb_delay)
    public void startExecutingWithExponentialBackoffDelay() {

        _logs = new ArrayList<>();
        _adapter.clear();

        _subscriptions.add(//

              Observable.range(1, 4)//
                    .delay(new Func1<Integer, Observable<Integer>>() {
                        @Override
                        public Observable<Integer> call(final Integer integer) {
                            // Rx-y way of doing the Fibonnaci :P
                            return MathObservable//
                                  .sumInteger(Observable.range(1, integer))
                                  .flatMap(new Func1<Integer, Observable<Integer>>() {
                                      @Override
                                      public Observable<Integer> call(Integer targetSecondDelay) {
                                          return Observable.just(integer)
                                                .delay(targetSecondDelay, TimeUnit.SECONDS);
                                      }
                                  });
                        }
                    })//
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            _log(String.format("Execute 4 tasks with delay - time now: [xx:%02d]",
                                  _getSecondHand()));
                        }
                    })//
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onCompleted() {
                            Timber.d("onCompleted");
                            _log("Completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.d(e, "arrrr. Error");
                            _log("Error");
                        }

                        @Override
                        public void onNext(Integer integer) {
                            Timber.d("executing Task %d [xx:%02d]", integer, _getSecondHand());
                            _log(String.format("executing Task %d  [xx:%02d]",
                                  integer,
                                  _getSecondHand()));

                        }
                    }));
    }

    // -----------------------------------------------------------------------------------

    private int _getSecondHand() {
        long millis = System.currentTimeMillis();
        return (int) (TimeUnit.MILLISECONDS.toSeconds(millis) -
                      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    // -----------------------------------------------------------------------------------

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logList.setAdapter(_adapter);
    }

    private void _log(String logMsg) {
        _logs.add(logMsg);

        // You can only do below stuff on main thread.
        new Handler(getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                _adapter.clear();
                _adapter.addAll(_logs);
            }
        });
    }

    // -----------------------------------------------------------------------------------

    // CAUTION:
    // --------------------------------------
    // THIS class HAS NO BUSINESS BEING non-static
    // I ONLY did this cause i wanted access to the `_log` method from inside here
    // for the purpose of demonstration. In the real world, make it static and LET IT BE!!

    // It's 12am in the morning and i feel lazy dammit !!!

    //public static class RetryWithDelay
    public class RetryWithDelay
          implements Func1<Observable<? extends Throwable>, Observable<?>> {

        private final int _maxRetries;
        private final int _retryDelayMillis;
        private int _retryCount;

        public RetryWithDelay(final int maxRetries, final int retryDelayMillis) {
            _maxRetries = maxRetries;
            _retryDelayMillis = retryDelayMillis;
            _retryCount = 0;
        }

        @Override
        public Observable<?> call(Observable<? extends Throwable> attempts) {
            return attempts.flatMap(new Func1<Throwable, Observable<?>>() {
                @Override
                public Observable<?> call(Throwable throwable) {
                    if (++_retryCount < _maxRetries) {
                        // When this Observable calls onNext, the original
                        // Observable will be retried (i.e. re-subscribed).
                        Timber.d("Retrying in %d ms", _retryCount * _retryDelayMillis);
                        _log(String.format("Retrying in %d ms", _retryCount * _retryDelayMillis));

                        return Observable.timer(_retryCount * _retryDelayMillis,
                              TimeUnit.MILLISECONDS);
                    }

                    Timber.d("Argh! i give up");
                    // Max retries hit. Just pass the error along.
                    return Observable.error(throwable);
                }
            });
        }
    }
}