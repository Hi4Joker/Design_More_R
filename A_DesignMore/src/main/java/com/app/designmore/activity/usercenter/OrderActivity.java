package com.app.designmore.activity.usercenter;

import android.content.Intent;
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
import butterknife.ButterKnife;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.ProgressLayout;

/**
 * Created by Joker on 2015/9/27.
 */
public class OrderActivity extends BaseActivity {

  private static final String TAG = OrderActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.order_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;

  @Nullable @Bind(R.id.order_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.order_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.order_layout_rv) RecyclerView recyclerView;

  public static void startFromLocation(MineActivity startingActivity, int startingLocationY) {
    Intent intent = new Intent(startingActivity, OrderActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_order_layout);

    OrderActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    OrderActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.rightMargin = DensityUtil.getActionBarSize(OrderActivity.this);

    OrderActivity.this.toolbarTitleTv.setVisibility(View.VISIBLE);
    OrderActivity.this.toolbarTitleTv.setText("我的订单");

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          OrderActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
          return true;
        }
      });
    } else {
      OrderActivity.this.loadData();
    }
  }

  private void loadData() {

    progressLayout.showEmpty(getResources().getDrawable(R.drawable.ic_grey_logo_icon), "您还没有订单",
        null);
  }

  private void startEnterAnim(int startLocationY) {

    rootView.setPivotY(startLocationY);
    ViewCompat.setScaleY(rootView, 0.0f);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(Constants.MILLISECONDS_400 / 2)
        .setInterpolator(new AccelerateInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            if (progressLayout != null) OrderActivity.this.loadData();
          }
        });
  }

  @Override public void exit() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(OrderActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            OrderActivity.this.finish();
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
