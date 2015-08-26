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

    String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n"
        + "<head>\n"
        + "\t<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">\n"
        + "\t<title>关于我们</title>\n"
        + "</head>\n"
        + "<body>\n"
        + "\t<div id=\"container\" style=\"font-family\">\n"
        + "\t\t<p><font face='微软雅黑'  color=\"gray\">欢迎来到聚美优品，深受千万用户信赖的全球领先化妆品限时特卖网站。</font></p>\n"
        + "\t\t<p><font face='微软雅黑'  color=\"gray\">聚美优品由海归学子、陈欧、戴雨森、刘辉创立于2010年3月，致力于创造简单、有趣、值得信赖的化妆品购物体验。首创了“化妆品团购”概念：每天在网站推荐几百款热门化妆品，并以远低于市场价折扣限量出售。从创立伊始，聚美优品便坚持以用户体验为最高诉求，承诺“100%正品”、“100%实拍” 和 “30天拆封无条件退货” 政策，竭力为每一个女孩带来独一无二的美丽惊喜。\n"
        + "\t\t</font></p>\n"
        + "\t\t<p><font face='微软雅黑'  color=\"gray\">从2010年3月成立至今，凭借口碑传播，聚美优品已经发展成为在北京、上海、成都、广州、沈阳拥有总面积达五万多平米的自建仓储、专业客服中心、超过3000万注册用户、月销售超过6亿元中国领先的化。</font></p>\n"
        + "\t</div>\n"
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
