package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.R;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.DialogManager;
import com.app.designmore.view.dialogplus.DialogPlus;
import com.app.designmore.view.dialogplus.OnBackPressListener;
import com.app.designmore.view.dialogplus.OnCancelListener;
import com.app.designmore.view.dialogplus.OnDismissListener;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/8/27.
 */
public class SafetyActivity extends RxAppCompatActivity {

  private static final String TAG = SafetyActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.safety_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.white_toolbar_title_iv) ImageView toolbarTitleIv;
  @Nullable @Bind(R.id.safety_layout_old_password_et) EditText oldPasswordEt;
  @Nullable @Bind(R.id.safety_layout_new_password_et) EditText newPasswordEt;
  @Nullable @Bind(R.id.safety_layout_confim_password_et) EditText confimPasswordEt;

  public static void startFromLocation(ProfileActivity startingActivity, int startingLocationY) {

    Intent intent = new Intent(startingActivity, SafetyActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_profile_safety_layout);
    ButterKnife.bind(SafetyActivity.this);

    SafetyActivity.this.initView(savedInstanceState);
  }

  private void initView(Bundle savedInstanceState) {

    SafetyActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbarTitleIv.setVisibility(View.GONE);
    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText(getText(R.string.action_submit));

    if (savedInstanceState == null) {

      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          SafetyActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
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

  private void startExitAnim() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(SafetyActivity.this))
        .setDuration(400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            SafetyActivity.super.onBackPressed();
            overridePendingTransition(0, 0);
          }
        });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    TextView textView = (TextView) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    textView.setText(getText(R.string.action_submit));

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        DialogPlus dialogPlus = DialogManager.getInstance()
            .showProgressDialog(SafetyActivity.this, new OnBackPressListener() {
              @Override public void onBackPressed(DialogPlus dialogPlus) {
                Log.e(TAG, "onBackPressed");
              }
            }, new OnCancelListener() {
              @Override public void onCancel(DialogPlus dialog) {
                Log.e(TAG, "onCancel");
              }
            }, new OnDismissListener() {
              @Override public void onDismiss(DialogPlus dialog) {
                Log.e(TAG, "onDismiss");
              }
            });
      }
    });
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case android.R.id.home:
        SafetyActivity.this.startExitAnim();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      SafetyActivity.this.startExitAnim();
    }
    return false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(SafetyActivity.this);
  }
}
