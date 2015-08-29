package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.view.dialogplus.DialogPlus;
import com.app.designmore.view.dialogplus.OnClickListener;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/8/25.
 */
public class ProfileActivity extends RxAppCompatActivity {

  private static final String TAG = ProfileActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.profile_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.profile_layout_avatar_iv) ImageView AvatarIv;
  @Nullable @Bind(R.id.profile_layout_username_tv) TextView usernameTv;
  @Nullable @Bind(R.id.profile_layout_nickname_et) EditText nicknameEt;
  @Nullable @Bind(R.id.profile_layout_sex_tv) TextView sexTv;
  @Nullable @Bind(R.id.profile_layout_birthday_tv) TextView birthdayTv;
  private DialogPlus dialogPlus;

  public static void startFromLocation(MineActivity startingActivity, int startingLocationY) {

    Intent intent = new Intent(startingActivity, ProfileActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_profile_layout);
    ButterKnife.bind(ProfileActivity.this);

    ProfileActivity.this.initView(savedInstanceState);
  }

  private void initView(Bundle savedInstanceState) {

    ProfileActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("个人资料");

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          ProfileActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
          return true;
        }
      });
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    TextView textView = (TextView) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    textView.setText(getText(R.string.action_submit));

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

      }
    });
    return true;
  }

  @Nullable @OnClick(R.id.profile_layout_avatar_rl) void onAvatarClick(View view) {

  }

  @Nullable @OnClick(R.id.profile_layout_sex_rl) void onSexClick() {

    dialogPlus = DialogManager.getInstance()
        .showSelectorDialog(ProfileActivity.this, Gravity.BOTTOM,
            R.layout.dialog_profile_pick_sex_layout, new OnClickListener() {
              @Override public void onClick(DialogPlus dialog, View view) {
                if (view.getId() != R.id.pick_sex_cancel_tv) {
                  sexTv.setText(((TextView) view).getText().toString());
                }
                dialog.dismiss();
              }
            });
  }

  EditText yearEt, monthEt, dayEt;

  @Nullable @OnClick(R.id.profile_layout_birthday_rl) void onBirthdayClick() {

    dialogPlus = DialogManager.getInstance()
        .showSelectorDialog(ProfileActivity.this, Gravity.CENTER,
            R.layout.dialog_profile_pick_birthday_layout, new OnClickListener() {
              @Override public void onClick(DialogPlus dialog, View view) {

                if (view.getId() == R.id.pick_birthday_confirm_tv) {
                  String year =
                      "".endsWith(yearEt.getText().toString()) ? yearEt.getHint().toString()
                          : yearEt.getText().toString();
                  String month =
                      "".endsWith(monthEt.getText().toString()) ? monthEt.getHint().toString()
                          : monthEt.getText().toString();
                  String day = "".endsWith(dayEt.getText().toString()) ? dayEt.getHint().toString()
                      : dayEt.getText().toString();

                  birthdayTv.setText(year + "/" + month + "/" + day);
                }
                dialog.dismiss();
              }
            });

    ViewGroup viewGroup = (ViewGroup) dialogPlus.getHolderView();
    yearEt = (EditText) viewGroup.findViewById(R.id.pick_birthday_year_et);
    monthEt = (EditText) viewGroup.findViewById(R.id.pick_birthday_month_et);
    dayEt = (EditText) viewGroup.findViewById(R.id.pick_birthday_day_et);

    String[] split = birthdayTv.getText().toString().split("/");

    yearEt.setHint(split[0]);
    monthEt.setHint(split[1]);
    dayEt.setHint(split[2]);
  }

  @Nullable @OnClick(R.id.profile_layout_safety_rl) void onSafetyClick(View view) {

    SafetyActivity.startFromLocation(ProfileActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
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
        ProfileActivity.this.startExitAnim();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      if (dialogPlus != null && dialogPlus.isShowing()) {
        return true;
      }
      ProfileActivity.this.startExitAnim();
    }
    return false;
  }

  private void startExitAnim() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(ProfileActivity.this))
        .setDuration(400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            ProfileActivity.super.onBackPressed();
            overridePendingTransition(0, 0);
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    this.dialogPlus = null;
    ButterKnife.unbind(ProfileActivity.this);
  }
}
