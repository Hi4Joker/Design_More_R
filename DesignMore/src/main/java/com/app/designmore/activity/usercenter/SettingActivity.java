package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.revealLib.widget.RevealLinearLayout;
import com.app.designmore.utils.Utils;

/**
 * Created by Joker on 2015/8/26.
 */
public class SettingActivity extends BaseActivity {

  private static final String TAG = SettingActivity.class.getSimpleName();

  @Nullable @Bind(R.id.white_toolbar) Toolbar toolbar;
  @Nullable @Bind(R.id.setting_layout_reveal_view) RevealFrameLayout revealFrameLayout;
  @Nullable @Bind(R.id.setting_layout_root_view) LinearLayout rootView;
  private SupportAnimator revealAnimator;
  //@Nullable @Bind(R.id.setting_layout_about_ll) LinearLayout aboutLl;
  //@Nullable @Bind(R.id.setting_layout_help_ll) LinearLayout helpLl;
  //@Nullable @Bind(R.id.setting_layout_clear_ll) LinearLayout clearLl;

  public static void startFromLocation(UserCenterActivity startingActivity) {

    Intent intent = new Intent(startingActivity, SettingActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_setting_layout);
    ButterKnife.bind(this);

    SettingActivity.this.initView(savedInstanceState);
  }

  private void initView(final Bundle savedInstanceState) {

    SettingActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    toolbar.findViewById(R.id.white_toolbar_iv).setVisibility(View.INVISIBLE);
    TextView title = (TextView) toolbar.findViewById(R.id.white_toolbar_title_tv);
    title.setVisibility(View.VISIBLE);
    title.setText("设置");

    if (savedInstanceState == null) {

      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          SettingActivity.this.startReveal();
          return true;
        }
      });
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        SettingActivity.this.exit();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {

    SettingActivity.this.exit();
  }

  private void exit() {

    if (revealAnimator != null && !revealAnimator.isRunning()) {
      revealAnimator = revealAnimator.reverse();
      revealAnimator.setDuration(400);
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

  private void startReveal() {
    final Rect bounds = new Rect();
    revealFrameLayout.getHitRect(bounds);

    revealAnimator = ViewAnimationUtils.createCircularReveal(rootView, bounds.right, bounds.top, 0,
        Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationStart() {
        revealFrameLayout.setVisibility(View.VISIBLE);
      }
    });

    revealAnimator.start();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
