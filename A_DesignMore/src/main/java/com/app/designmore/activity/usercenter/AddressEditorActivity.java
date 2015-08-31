package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import com.app.designmore.activity.MineActivity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/8/25.
 */
public class AddressEditorActivity extends RxAppCompatActivity {

  private static final String TAG = AddressEditorActivity.class.getSimpleName();

  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.address_editor_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.address_editor_layout_rfl) RevealFrameLayout revealFrameLayout;
  @Nullable @Bind(R.id.address_editor_layout_pl) ProgressLayout progresslayout;
  @Nullable @Bind(R.id.address_editor_layout_rv) RecyclerView recyclerView;
  private SupportAnimator revealAnimator;

  public static void navigateToAddressEditor(AppCompatActivity startingActivity) {

    Intent intent = new Intent(startingActivity, AddressEditorActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_address_editor_layout);
    ButterKnife.bind(AddressEditorActivity.this);

    AddressEditorActivity.this.initView(savedInstanceState);
  }

  private void initView(Bundle savedInstanceState) {

    AddressEditorActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("新增地址");

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          AddressEditorActivity.this.startEnterAnim();
          return true;
        }
      });
    }
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    revealFrameLayout.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(revealFrameLayout.getChildAt(0), bounds.right, 0, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.REVEAL_DURATION);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.start();
  }

  private void startExitAnim() {

    if (revealAnimator != null && !revealAnimator.isRunning()) {
      revealAnimator = revealAnimator.reverse();
      revealAnimator.setDuration(Constants.REVEAL_DURATION);
      revealAnimator.setInterpolator(new AccelerateInterpolator());
      revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
        @Override public void onAnimationEnd() {
          rootView.setVisibility(View.GONE);
          AddressEditorActivity.this.finish();
        }

        @Override public void onAnimationCancel() {
          AddressEditorActivity.this.finish();
        }
      });
      revealAnimator.start();
    } else if (revealAnimator != null && revealAnimator.isRunning()) {
      revealAnimator.cancel();
    } else if (revealAnimator == null) {
      AddressEditorActivity.this.finish();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    TextView textView = (TextView) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    textView.setText(getText(R.string.action_done));

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

      }
    });
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        AddressEditorActivity.this.startExitAnim();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      AddressEditorActivity.this.startExitAnim();
    }
    return false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(AddressEditorActivity.this);
  }
}
