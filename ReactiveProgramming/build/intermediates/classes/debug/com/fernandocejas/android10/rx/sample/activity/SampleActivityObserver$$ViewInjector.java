// Generated code from Butter Knife. Do not modify!
package com.fernandocejas.android10.rx.sample.activity;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class SampleActivityObserver$$ViewInjector {
  public static void inject(Finder finder, final com.fernandocejas.android10.rx.sample.activity.SampleActivityObserver target, Object source) {
    View view;
    view = finder.findRequiredView(source, 16908298, "field 'rv_elements'");
    target.rv_elements = (android.support.v7.widget.RecyclerView) view;
    view = finder.findRequiredView(source, 16908313, "field 'btn_AddElement' and method 'onAddElementClick'");
    target.btn_AddElement = (android.widget.Button) view;
    view.setOnClickListener(
      new butterknife.internal.DebouncingOnClickListener() {
        @Override public void doClick(
          android.view.View p0
        ) {
          target.onAddElementClick();
        }
      });
  }

  public static void reset(com.fernandocejas.android10.rx.sample.activity.SampleActivityObserver target) {
    target.rv_elements = null;
    target.btn_AddElement = null;
  }
}
