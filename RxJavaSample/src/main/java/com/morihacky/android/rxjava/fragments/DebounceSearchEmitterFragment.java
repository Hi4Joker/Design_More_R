package com.morihacky.android.rxjava.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toolbar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.AdapterViewItemClickEvent;
import com.jakewharton.rxbinding.widget.CompoundButtonCheckedChangeEvent;
import com.jakewharton.rxbinding.widget.RxAdapter;
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxProgressBar;
import com.jakewharton.rxbinding.widget.RxSearchView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.RxToolbar;
import com.jakewharton.rxbinding.widget.TextViewEditorActionEvent;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.morihacky.android.rxjava.R;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

import static java.lang.String.format;

public class DebounceSearchEmitterFragment extends BaseFragment {

  @InjectView(R.id.list_threading_log) ListView _logsList;
  @InjectView(R.id.input_txt_debounce) EditText _inputSearchText;

  private LogAdapter _adapter;
  private List<String> _logs;

  private Subscription _subscription;

  @Override public void onDestroy() {
    super.onDestroy();
    if (_subscription != null) {
      _subscription.unsubscribe();
    }
  }

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_debounce, container, false);
    ButterKnife.inject(this, layout);
    return layout;
  }

  @OnClick(R.id.clr_debounce) public void onClearLog() {
    _logs = new ArrayList<>();
    _adapter.clear();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    _setupLogger();

    /*RxAdapter.dataChanges(_adapter).map(new Func1<LogAdapter, Object>() {
      @Override public Object call(LogAdapter logAdapter) {
        return null;
      }
    });

    RxAdapterView.itemClickEvents(_logsList).map(new Func1<AdapterViewItemClickEvent, Object>() {
      @Override public Object call(AdapterViewItemClickEvent adapterViewItemClickEvent) {
        adapterViewItemClickEvent.clickedView();
        return null;
      }
    });

    RxAdapterView.itemClicks(_logsList).map(new Func1<Integer, Object>() {
      @Override public Object call(Integer integer) {
        return null;
      }
    });

    RxTextView.editorActionEvents(_inputSearchText)
        .map(new Func1<TextViewEditorActionEvent, Object>() {
          @Override public Object call(TextViewEditorActionEvent textViewEditorActionEvent) {

            textViewEditorActionEvent.actionId();
            textViewEditorActionEvent.keyEvent();

            return null;
          }
        });

    CompoundButton button = new CompoundButton(getActivity()) {
      @Override public void toggle() {
        super.toggle();
      }
    };

    RxCompoundButton.checkedChangeEvents(button)
        .map(new Func1<CompoundButtonCheckedChangeEvent, Object>() {
          @Override
          public Object call(CompoundButtonCheckedChangeEvent compoundButtonCheckedChangeEvent) {
            return null;
          }
        });

    Toolbar toolbar = new Toolbar(getActivity());

    RxToolbar.itemClicks(toolbar).map(new Func1<MenuItem, Object>() {
      @Override public Object call(MenuItem menuItem) {
        return null;
      }
    });

    RxView.visibility(_inputSearchText).call(true);*/





    /**/
    _subscription = RxTextView.textChangeEvents(_inputSearchText)//
        .doOnNext(new Action1<TextViewTextChangeEvent>() {
          @Override public void call(TextViewTextChangeEvent textViewTextChangeEvent) {

            Log.e("joker", textViewTextChangeEvent.text().toString());
          }
        }).debounce(400, TimeUnit.MILLISECONDS)// default Scheduler is Computation
        .observeOn(AndroidSchedulers.mainThread())//
        .subscribe(_getSearchObserver());
  }

  // -----------------------------------------------------------------------------------
  // Main Rx entities

  private Observer<TextViewTextChangeEvent> _getSearchObserver() {
    return new Observer<TextViewTextChangeEvent>() {
      @Override public void onCompleted() {
        Timber.d("--------- onComplete");
      }

      @Override public void onError(Throwable e) {
        Timber.e(e, "--------- Woops on error!");
        _log("Dang error. check your logs");
      }

      @Override public void onNext(TextViewTextChangeEvent onTextChangeEvent) {
        _log(format("Searching for %s", onTextChangeEvent.text().toString()));
      }
    };
  }

  // -----------------------------------------------------------------------------------
  // Method that help wiring up the example (irrelevant to RxJava)

  private void _setupLogger() {
    _logs = new ArrayList<String>();
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

        @Override public void run() {
          _adapter.clear();
          _adapter.addAll(_logs);
        }
      });
    }
  }

  private boolean _isCurrentlyOnMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

  private class LogAdapter extends ArrayAdapter<String> {

    public LogAdapter(Context context, List<String> logs) {
      super(context, R.layout.item_log, R.id.item_log, logs);
    }
  }
}