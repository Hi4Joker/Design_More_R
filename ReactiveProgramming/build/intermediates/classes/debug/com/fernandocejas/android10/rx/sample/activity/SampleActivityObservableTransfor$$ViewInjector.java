// Generated code from Butter Knife. Do not modify!
package com.fernandocejas.android10.rx.sample.activity;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class SampleActivityObservableTransfor$$ViewInjector {
  public static void inject(Finder finder, final com.fernandocejas.android10.rx.sample.activity.SampleActivityObservableTransfor target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131034121, "field 'tv_streamOriginalOrder'");
    target.tv_streamOriginalOrder = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131034122, "field 'tv_flatMapResult'");
    target.tv_flatMapResult = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131034123, "field 'tv_concatMapResult'");
    target.tv_concatMapResult = (android.widget.TextView) view;
    view = finder.findRequiredView(source, 2131034119, "field 'btn_flatMap' and method 'onFlatMapClick'");
    target.btn_flatMap = (android.widget.Button) view;
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onFlatMapClick();
        }
      });
    view = finder.findRequiredView(source, 2131034120, "field 'btn_concatMap' and method 'onConcatMapClick'");
    target.btn_concatMap = (android.widget.Button) view;
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onConcatMapClick();
        }
      });
  }

  public static void reset(com.fernandocejas.android10.rx.sample.activity.SampleActivityObservableTransfor target) {
    target.tv_streamOriginalOrder = null;
    target.tv_flatMapResult = null;
    target.tv_concatMapResult = null;
    target.btn_flatMap = null;
    target.btn_concatMap = null;
  }
}
