package com.fernandocejas.android10.rx.sample.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.fernandocejas.android10.rx.sample.R;
import com.fernandocejas.android10.rx.sample.data.NumberGenerator;
import java.io.IOException;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class SampleActivityBackpressure extends Activity implements Observer<Integer> {

  private static final String TAG = SampleActivityBackpressure.class.getSimpleName();
  @InjectView(R.id.btn_backpressureSample) Button btn_backpressureSample;
  @InjectView(R.id.btn_backpressureBuffer) Button btn_backpressureBuffer;

  private NumberGenerator numberGenerator;
  private Subscription subscription;

  public static Intent getCallingIntent(Context context) {
    return new Intent(context, SampleActivityBackpressure.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sample_backpressure);

    ButterKnife.inject(this);
    initialize();
  }

  @Override protected void onDestroy() {
    this.subscription.unsubscribe();
    super.onDestroy();
  }

  private void initialize() {
    this.subscription = Subscriptions.empty();
    this.numberGenerator = new NumberGenerator();
  }

  @OnClick(R.id.btn_backpressureSample) void onButtonSampleClick() {
    this.subscription = numberGenerator.getResults()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this);
  }

  @Override public void onCompleted() {

    Log.e(TAG, "onCompleted");

    //Toast.makeText(this, "onCompleted!", Toast.LENGTH_SHORT).show();
  }

  @Override public void onError(Throwable e) {
    Log.e(TAG, e.getMessage());
  }

  @Override public void onNext(Integer integer) {
    Log.e(TAG, "onNext --> " + integer);
  }

  public class SomeType {
    private String value;

    public void setValue(String value) {
      this.value = value;
    }

    public Observable<String> valueObservable() {
      return Observable.defer(new Func0<Observable<String>>() {
        @Override public Observable<String> call() {
          return Observable.just(value);
        }
      });
    }
  }
}
