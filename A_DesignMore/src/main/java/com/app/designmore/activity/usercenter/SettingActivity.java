package com.app.designmore.activity.usercenter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.LoginActivity;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.activity.SplashActivity;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.signature.StringSignature;

/**
 * Created by Joker on 2015/8/26.
 */
public class SettingActivity extends BaseActivity {

  private static final String TAG = SettingActivity.class.getSimpleName();

  @Nullable @Bind(R.id.setting_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.setting_layout_cache_tv) TextView cacheTv;
  private SupportAnimator revealAnimator;

  public static void navigateToSetting(MineActivity startingActivity) {

    Intent intent = new Intent(startingActivity, SettingActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_setting_layout);

    SettingActivity.this.initView(savedInstanceState);
    SettingActivity.this.getCache();
  }

  @Override public void initView(final Bundle savedInstanceState) {

    SettingActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_icon);

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.rightMargin = DensityUtil.getActionBarSize(SettingActivity.this);
    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("设置");

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          SettingActivity.this.startEnterAnim();
          return true;
        }
      });
    }
  }

  private void getCache() {
    cacheTv.setText(Utils.FormetFileSize(Glide.getPhotoCacheDir(SettingActivity.this).length()));

    /*MemorySizeCalculator calculator = new MemorySizeCalculator(SettingActivity.this);
    int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
    int defaultBitmapPoolSize = calculator.getBitmapPoolSize();

    Log.e(TAG, "defaultMemoryCacheSize:  " + defaultMemoryCacheSize);
    Log.e(TAG, "defaultBitmapPoolSize: " + defaultBitmapPoolSize);*/
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

  @Nullable @OnClick(R.id.setting_layout_unregister_btn) void onUnregisterClick() {

    DialogManager.getInstance()
        .showNormalDialog(SettingActivity.this, "请确认退出登陆", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {

              DBHelper.getInstance(getApplicationContext()).deleteLoginInfo(SettingActivity.this);

              Intent intent = new Intent(SettingActivity.this, SplashActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
              startActivity(intent);
              overridePendingTransition(0, 0);
            }
          }
        });
  }

  private void startEnterAnim() {
    final Rect bounds = new Rect();
    rootView.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(rootView.getChildAt(0), bounds.right, 0, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.start();
  }

  @Override public void exit() {

    if (revealAnimator != null && !revealAnimator.isRunning()) {
      revealAnimator = revealAnimator.reverse();
      revealAnimator.setDuration(Constants.MILLISECONDS_400);
      revealAnimator.setInterpolator(new AccelerateInterpolator());
      revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
        @Override public void onAnimationEnd() {
          rootView.setVisibility(View.GONE);
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

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
