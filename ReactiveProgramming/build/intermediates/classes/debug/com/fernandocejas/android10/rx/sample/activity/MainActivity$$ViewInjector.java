// Generated code from Butter Knife. Do not modify!
package com.fernandocejas.android10.rx.sample.activity;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class MainActivity$$ViewInjector {
  public static void inject(Finder finder, final com.fernandocejas.android10.rx.sample.activity.MainActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131034113, "field 'btn_sampleObserver' and method 'navigateToObserverSample'");
    target.btn_sampleObserver = (android.widget.Button) view;
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.navigateToObserverSample();
        }
      });
    view = finder.findRequiredView(source, 2131034115, "field 'btn_sampleObservable' and method 'navigateToBackpressureSample'");
    target.btn_sampleObservable = (android.widget.Button) view;
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.navigateToBackpressureSample();
        }
      });
    view = finder.findRequiredView(source, 2131034114, "field 'btn_sampleObservableTransformation' and method 'navigateToObservableTransformSample'");
    target.btn_sampleObservableTransformation = (android.widget.Button) view;
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.navigateToObservableTransformSample();
        }
      });
  }

  public static void reset(com.fernandocejas.android10.rx.sample.activity.MainActivity target) {
    target.btn_sampleObserver = null;
    target.btn_sampleObservable = null;
    target.btn_sampleObservableTransformation = null;
  }
}
