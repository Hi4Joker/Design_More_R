package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.R;
import com.app.designmore.utils.DensityUtil;

/**
 * Created by Joker on 2015/8/26.
 */
public class AboutActivity extends BaseActivity {

  private static final String START_LOCATION_Y = "START_LOCATION_Y";
  @Nullable @Bind(R.id.about_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.about_layout_about_wv) WebView aboutWv;

  private String ABOUT_HTML = "file:///android_asset/about_designMore.html";

  public static void startFromLocation(SettingActivity startingActivity, int startingLocationY) {

    Intent intent = new Intent(startingActivity, AboutActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_setting_about_layout);

    AboutActivity.this.initView(savedInstanceState);
  }

  private void initView(Bundle savedInstanceState) {

    aboutWv.loadUrl(ABOUT_HTML);

    AboutActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.rightMargin = DensityUtil.getActionBarSize(AboutActivity.this);
    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("关于我们");

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          AboutActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
          return true;
        }
      });
    }
  }

  private void startEnterAnim(int startLocationY) {

    rootView.setPivotY(startLocationY);
    rootView.setScaleY(0.0f);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(200)
        .setInterpolator(new AccelerateInterpolator());
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        AboutActivity.this.startExitAnim();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      AboutActivity.this.startExitAnim();
    }
    return false;
  }

  private void startExitAnim() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(AboutActivity.this))
        .setDuration(200)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            AboutActivity.this.finish();
            overridePendingTransition(0, 0);
          }
        });
  }
}
