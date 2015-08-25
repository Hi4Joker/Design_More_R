// Generated code from Butter Knife. Do not modify!
package com.fernandocejas.android10.rx.sample.activity;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class SampleActivityBackpressure$$ViewInjector {
  public static void inject(Finder finder, final com.fernandocejas.android10.rx.sample.activity.SampleActivityBackpressure target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131034117, "field 'btn_backpressureSample' and method 'onButtonSampleClick'");
    target.btn_backpressureSample = (android.widget.Button) view;
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onButtonSampleClick();
        }
      });
    view = finder.findRequiredView(source, 2131034118, "field 'btn_backpressureBuffer'");
    target.btn_backpressureBuffer = (android.widget.Button) view;
  }

  public static void reset(com.fernandocejas.android10.rx.sample.activity.SampleActivityBackpressure target) {
    target.btn_backpressureSample = null;
    target.btn_backpressureBuffer = null;
  }
}
