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
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.UserCenterActivity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.bumptech.glide.Glide;

/**
 * Created by Joker on 2015/8/26.
 */
public class SettingActivity extends BaseActivity {

  private static final String TAG = SettingActivity.class.getSimpleName();
  private static final int ANIM_DURATION = 300;

  @Nullable @Bind(R.id.setting_layout_root_view) RevealFrameLayout revealFrameLayout;
  @Nullable @Bind(R.id.white_toolbar_root) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.white_toolbar_title_iv) ImageView toolbarTitleIv;
  @Nullable @Bind(R.id.setting_layout_cache_tv) TextView cacheTv;
  private SupportAnimator revealAnimator;

  public static void navigateToSetting(UserCenterActivity startingActivity) {

    Intent intent = new Intent(startingActivity, SettingActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_setting_layout);
    ButterKnife.bind(this);

    SettingActivity.this.initView(savedInstanceState);
    SettingActivity.this.getCache();
  }

  private void getCache() {

    cacheTv.setText(Utils.FormetFileSize(Glide.getPhotoCacheDir(SettingActivity.this).length()));
  }

  private void initView(final Bundle savedInstanceState) {

    SettingActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    toolbarTitleIv.setVisibility(View.INVISIBLE);
    toolbarTitleTv.setText("设置");

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

    AboutActivity.startFromLocation(SettingActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  @Nullable @OnClick(R.id.setting_layout_help_ll) void onHelpClick(View view) {

    HelpActivity.startFromLocation(SettingActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  @Nullable @OnClick(R.id.setting_layout_clear_ll) void onClearClick() {

    Glide.get(SettingActivity.this).clearMemory();
    cacheTv.setText("0KB");
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
    revealAnimator.setDuration(ANIM_DURATION);
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
      revealAnimator.setDuration(ANIM_DURATION);
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
    ButterKnife.unbind(SettingActivity.this);
  }
}
