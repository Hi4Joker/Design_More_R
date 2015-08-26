package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.Toolbar;
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
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.DialogManager;
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
  @Nullable @Bind(R.id.white_toolbar_root) Toolbar toolbar;
  @Nullable @Bind(R.id.profile_layout_avatar_iv) ImageView profileLayoutAa;
  @Nullable @Bind(R.id.profile_layout_username_tv) TextView usernameTv;
  @Nullable @Bind(R.id.profile_layout_nickname_et) EditText nicknameEt;
  @Nullable @Bind(R.id.profile_layout_sex_tv) TextView sexTv;
  @Nullable @Bind(R.id.profile_layout_birthday_tv) TextView birthdayTv;

  public static void startFromLocation(UserCenterActivity startingActivity, int startingLocationY) {

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

    toolbar.findViewById(R.id.white_toolbar_title_iv).setVisibility(View.INVISIBLE);
    TextView title = (TextView) toolbar.findViewById(R.id.white_toolbar_title_tv);
    title.setVisibility(View.VISIBLE);
    title.setText("个人资料");

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

  @Nullable @OnClick(R.id.profile_layout_avatar_rl) void onAvatarClick(View view) {

  }

  @Nullable @OnClick(R.id.profile_layout_sex_rl) void onSexClick() {

    DialogManager.getInstance()
        .showPickSexDialog(ProfileActivity.this, R.layout.dialog_profile_pick_sex_layout,
            new OnClickListener() {
              @Override public void onClick(DialogPlus dialog, View view) {
                sexTv.setText(((TextView) view).getText().toString());
                dialog.dismiss();
              }
            });
  }

  @Nullable @OnClick(R.id.profile_layout_birthday_rl) void onBirthdayClick() {

    DialogManager.getInstance()
        .showPickBirthdayDialog(ProfileActivity.this, R.layout.dialog_profile_pick_birthday_layout,
            new OnClickListener() {
              @Override public void onClick(DialogPlus dialog, View view) {

                dialog.dismiss();
              }
            });
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
        ProfileActivity.this.startExitAnim();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {

    ProfileActivity.this.startExitAnim();
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
    ButterKnife.unbind(ProfileActivity.this);
  }
}
