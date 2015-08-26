package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
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
  @Nullable @Bind(R.id.setting_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root) Toolbar toolbar;
  @Nullable @Bind(R.id.about_layout_about_tv) TextView aboutTv;

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

    String html = "<!DOCTYPE html>\n"
        + "<html>\n"
        + "<body>\n"
        + "<p><font color=\"#B9B9BC\" face=\"微软雅黑\">兑换码说明:</font><br/>\n"
        + "<font color=\"black\" face=\"微软雅黑\"><b>一.兑换码是什么?</b></font><br/>\n"
        + "<font color=\"#B9B9BC\" face=\"微软雅黑\">可兑换成积分/优惠券，积分/优惠券可在APP内使用</font></p>\n"
        + "\n"
        + "<p><font color=\"black\" face=\"微软雅黑\"><b>二.用户如何获得&nbsp;“兑换码”&nbsp;?\n</b></font><br/>\n"
        + "<font color=\"#B9B9BC\" face=\"微软雅黑\">1、用户参加e洗车平台内活动领取兑换码;<br/>\n"
        + "2、其他与e洗车合作的平台赠送的兑换码,<br/>\n"
        + "兑换码由e洗车提供;</font></p>\n"
        + "\n"
        + "</body>\n"
        + "</html>";

    aboutTv.setText(Html.fromHtml(html));

    AboutActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbar.findViewById(R.id.white_toolbar_title_iv).setVisibility(View.INVISIBLE);
    TextView title = (TextView) toolbar.findViewById(R.id.white_toolbar_title_tv);
    title.setVisibility(View.VISIBLE);
    title.setText("关于我们");

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

    rootView.setScaleY(0.0f);
    rootView.setPivotY(startLocationY);

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

  @Override public void onBackPressed() {

    AboutActivity.this.startExitAnim();
  }

  private void startExitAnim() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(AboutActivity.this))
        .setDuration(200)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            AboutActivity.super.onBackPressed();
            overridePendingTransition(0, 0);
          }
        });
  }
}
