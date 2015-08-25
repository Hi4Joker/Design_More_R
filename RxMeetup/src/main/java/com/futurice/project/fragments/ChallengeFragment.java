package com.futurice.project.fragments;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;
import com.futurice.project.R;
import com.futurice.project.Solution;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;
import rx.android.observables.ViewObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ChallengeFragment extends Fragment {

  private static final String TAG = ChallengeFragment.class.getSimpleName();
  private final CompositeSubscription compositeSubscription = new CompositeSubscription();
  private Observable<String> inputStream;
  private Observable<String> sequenceStream;
  private Observable<String> successStream;

  private View buttonA;
  private View buttonB;
  private TextView resultTextView;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    inputStream = defineInputStream();
    sequenceStream = defineSequenceStream(inputStream);
    //successStream = Solution.defineSuccessStream(sequenceStream, Schedulers.immediate());
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_main, container, false);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    buttonA = getView().findViewById(R.id.buttonA);
    buttonB = getView().findViewById(R.id.buttonB);
    resultTextView = (TextView) getView().findViewById(R.id.result);
  }

  private Observable<String> defineInputStream() {

    Observable<String> inputA = Observable.create(new Observable.OnSubscribe<String>() {
      @Override public void call(final Subscriber<? super String> subscriber) {

        Log.e(TAG, "inputA init");

        buttonA.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {

            Log.e(TAG, "onClickA");

            int i = 5;
            //subscriber.onNext(String.valueOf(++i));
            subscriber.onNext("A");
          }
        });
      }
    }).startWith(Observable.just("A startWith")).publish().refCount();

    Observable<String> inputB = Observable.defer(new Func0<Observable<String>>() {
      @Override public Observable<String> call() {
        return Observable.create(new Observable.OnSubscribe<String>() {
          @Override public void call(final Subscriber<? super String> subscriber) {

            Log.e(TAG, "inputB init");

            buttonB.setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {

                Log.e(TAG, "onClickB");
                subscriber.onNext("B");
              }
            });
          }
        });
      }
    }).publish().refCount().startWith(Observable.just("B startWith"));

    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    //AndroidObservable.fromBroadcast(context, filter)

    return Observable.merge(inputA, inputB);
  }

  private static Observable<String> defineSequenceStream(Observable<String> inputStream) {

    return inputStream.scan("1", new Func2<String, String, String>() {
      @Override public String call(String sum, String i2) {

        String concatenation = (sum + i2);
        /*if (concatenation.length() <= 6) {
          return concatenation;
        } else {
          return concatenation.substring(
              concatenation.length() - Solution.SECRET_SEQUENCE.length());
        }*/

        Log.e(TAG, "concatenation:" + concatenation);
        Log.e(TAG, "s1:" + concatenation);
        Log.e(TAG, "s2:" + concatenation);

        return concatenation;
      }
    });
  }

  @Override public void onResume() {
    super.onResume();
    subscribeResultTextView();
  }

  private void subscribeResultTextView() {

    /*Observable<String> correctStream = successStream.map(new Func1<String, String>() {
      @Override public String call(String s) {

        Log.e(TAG, s + "correct");

        return s + "correct";
      }
    });*/

   /* compositeSubscription.add(Observable.merge(successStream, sequenceStream)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<String>() {
          @Override public void call(String s) {

            Log.e(TAG, "text:" + s);
            resultTextView.setText(s);
          }
        }));*/

    /*compositeSubscription.add(
        inputStream.subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
          @Override public void onCompleted() {

            Log.e(TAG, "onCompleted");
          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onNext(String s) {

            Log.e(TAG, "text:" + s);
          }
        }));*/
    /*compositeSubscription.add(sequenceStream.subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<String>() {
          @Override public void call(String s) {
            resultTextView.setText(s);
          }
        }));*/
  }

  @Override public void onPause() {
    super.onPause();
    compositeSubscription.clear();
  }
}
