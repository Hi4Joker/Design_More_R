package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.Utils;
import com.bumptech.glide.Glide;

/**
 * Created by Joker on 2015/8/26.
 */
public class SettingActivity extends BaseActivity {

  private static final String TAG = SettingActivity.class.getSimpleName();

  @Nullable @Bind(R.id.white_toolbar_root) Toolbar toolbar;
  @Nullable @Bind(R.id.setting_layout_reveal_view) RevealFrameLayout revealFrameLayout;
  @Nullable @Bind(R.id.setting_layout_cache_tv) TextView cacheTv;
  private SupportAnimator revealAnimator;
  private int statusBarHeight;

  public static void navigateToSetting(UserCenterActivity startingActivity) {

    Intent intent = new Intent(startingActivity, SettingActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_setting_layout);
    ButterKnife.bind(this);

    SettingActivity.this.initView(savedInstanceState);
  }

  private void getStatusBarHeight() {
    statusBarHeight = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      statusBarHeight = getResources().getDimensionPixelSize(resourceId);
    }
  }

  private void initView(final Bundle savedInstanceState) {

    SettingActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    toolbar.findViewById(R.id.white_toolbar_title_iv).setVisibility(View.INVISIBLE);
    TextView title = (TextView) toolbar.findViewById(R.id.white_toolbar_title_tv);
    title.setVisibility(View.VISIBLE);
    title.setText("设置");

    if (savedInstanceState == null) {
      revealFrameLayout.getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
              revealFrameLayout.getViewTreeObserver().removeOnPreDrawListener(this);
              SettingActivity.this.startEnterAnim();
              return true;
            }
          });
    } else {
      revealFrameLayout.setVisibility(View.VISIBLE);
    }

    SettingActivity.this.getStatusBarHeight();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        SettingActivity.this.startExitAnim();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Nullable @OnClick(R.id.setting_layout_about_ll) void onAboutClick(View view) {

    AboutActivity.startFromLocation(SettingActivity.this, SettingActivity.this.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  @Nullable @OnClick(R.id.setting_layout_help_ll) void onHelpClick(View view) {

    HelpActivity.startFromLocation(SettingActivity.this, SettingActivity.this.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  @Nullable @OnClick(R.id.setting_layout_clear_ll) void onClearClick() {


  }

  private int getLocationY(View item) {

    int[] startingLocation = new int[1];
    // 得到相对于整个屏幕的区域坐标（左上角坐标——右下角坐标）
    Rect viewRect = new Rect();
    item.getGlobalVisibleRect(viewRect);

    startingLocation[0] = (viewRect.top - statusBarHeight) + (viewRect.bottom - statusBarHeight);

    return startingLocation[0] / 2;
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      SettingActivity.this.startExitAnim();
    }
    return false;
  }

  private void startEnterAnim() {
    final Rect bounds = new Rect();
    revealFrameLayout.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(revealFrameLayout.getChildAt(0), bounds.right,
            bounds.top, 0, Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(200);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationStart() {
        revealFrameLayout.setVisibility(View.VISIBLE);
      }
    });
    revealAnimator.start();
  }

  private void startExitAnim() {

    if (revealAnimator != null && !revealAnimator.isRunning()) {
      revealAnimator = revealAnimator.reverse();
      revealAnimator.setDuration(200);
      revealAnimator.setInterpolator(new AccelerateInterpolator());
      revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
        @Override public void onAnimationEnd() {
          revealFrameLayout.setVisibility(View.GONE);
          SettingActivity.this.finish();
        }

        @Override public void onAnimationCancel() {
          SettingActivity.this.finish();
        }
      });
      revealAnimator.start();
    } else if (revealAnimator != null && revealAnimator.isRunning()) {
      revealAnimator.cancel();
    } else if (revealAnimator == null) {
      SettingActivity.this.finish();
    }
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(0, 0);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(this);
  }
}
