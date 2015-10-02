package com.app.designmore.activity.usercenter;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;

/**
 * Created by Joker on 2015/10/3.
 */
public class OrderDeliveryActivity extends BaseActivity {

  private static final String TAG = OrderDeliveryActivity.class.getSimpleName();

  @Nullable @Bind(R.id.order_delivery_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;

  @Nullable @Bind(R.id.order_delivery_layout_rfl) RevealFrameLayout revealFrameLayout;
  @Nullable @Bind(R.id.order_delivery_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.order_address_layout_rl) RecyclerView recyclerView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.order_delivery_layout);

    OrderDeliveryActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {
    OrderDeliveryActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

    OrderDeliveryActivity.this.toolbarTitleTv.setVisibility(View.VISIBLE);
    OrderDeliveryActivity.this.toolbarTitleTv.setText("配送方式");

    /*创建Adapter*/
    OrderDeliveryActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      revealFrameLayout.getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
              revealFrameLayout.getViewTreeObserver().removeOnPreDrawListener(this);
              OrderDeliveryActivity.this.startEnterAnim();
              return true;
            }
          });
    } else {
      OrderDeliveryActivity.this.loadData();
    }
  }

  private void setupAdapter() {

  }

  private void loadData() {

  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    revealFrameLayout.getHitRect(bounds);

    OrderDeliveryActivity.this.revealFrameLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    SupportAnimator revealAnimator =
        ViewAnimationUtils.createCircularReveal(revealFrameLayout.getChildAt(0), 0, bounds.left, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {

        if (progressLayout != null) {
          OrderDeliveryActivity.this.revealFrameLayout.setLayerType(View.LAYER_TYPE_NONE, null);
          OrderDeliveryActivity.this.loadData();
        }
      }
    });
    revealAnimator.start();

  }

  @Override public void exit() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(OrderDeliveryActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            OrderDeliveryActivity.this.finish();
          }
        });
  }
}
