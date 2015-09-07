package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.view.ViewClickEvent;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.wiring.LogAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * This is a demonstration of the `buffer` Observable.
 *
 * The buffer observable allows taps to be collected only within a time span. So taps outside the
 * 2s limit imposed by buffer will get accumulated in the next log statement.
 *
 * If you're looking for a more foolproof solution that accumulates "continuous" taps vs
 * a more dumb solution as show below (i.e. number of taps within a timespan)
 * look at {@link com.morihacky.android.rxjava.rxbus.RxBusDemo_Bottom3Fragment} where a combo
 * of `publish` and `buffer` is used.
 *
 * Also http://nerds.weddingpartyapp.com/tech/2015/01/05/debouncedbuffer-used-in-rxbus-example/
 * if you're looking for words instead of code
 */
public class BufferDemoFragment
      extends BaseFragment {

    @InjectView(R.id.list_threading_log) ListView _logsList;
    @InjectView(R.id.btn_start_operation) Button _tapBtn;

    private LogAdapter _adapter;
    private List<String> _logs;

    private Subscription _subscription;

    @Override
    public void onStart() {
        super.onStart();
        _subscription = _getBufferedSubscription();
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscription.unsubscribe();
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
        View layout = inflater.inflate(R.layout.fragment_buffer, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Subscription _getBufferedSubscription() {
        return RxView.clickEvents(_tapBtn)
              .map(new Func1<ViewClickEvent, Integer>() {
                  @Override
                  public Integer call(ViewClickEvent onClickEvent) {
                      Timber.d("--------- GOT A TAP");
                      _log("GOT A TAP");
                      return 1;
                  }
              })
              .buffer(2, TimeUnit.SECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Observer<List<Integer>>() {

                  @Override
                  public void onCompleted() {
                      // fyi: you'll never reach here
                      Timber.d("----- onCompleted");
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "--------- Woops on error!");
                      _log("Dang error! check your logs");
                  }

                  @Override
                  public void onNext(List<Integer> integers) {
                      Timber.d("--------- onNext");
                      if (integers.size() > 0) {
                          _log(String.format("%d taps", integers.size()));
                      } else {
                          Timber.d("--------- No taps received ");
                      }
                  }
              });
    }

    // -----------------------------------------------------------------------------------
    // Methods that help wiring up the example (irrelevant to RxJava)

    private void _setupLogger() {
        _logs = new ArrayList<>();
        _adapter = new LogAdapter(getActivity(), new ArrayList<String>());
        _logsList.setAdapter(_adapter);
    }

    private void _log(String logMsg) {

        if (_isCurrentlyOnMainThread()) {
            _logs.add(0, logMsg + " (main thread) ");
            _adapter.clear();
            _adapter.addAll(_logs);
        } else {
            _logs.add(0, logMsg + " (NOT main thread) ");

            // You can only do below stuff on main thread.
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    _adapter.clear();
                    _adapter.addAll(_logs);
                }
            });
        }
    }

    private boolean _isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
