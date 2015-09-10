package com.app.designmore.activity.usercenter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.utils.DatePickDialog;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DialogManager;

/**
 * Created by Joker on 2015/8/25.
 */
public class ProfileActivity extends BaseActivity {

  private static final String TAG = ProfileActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";
  private static final int MALE = 0;
  private static final int FEMALE = 1;

  @Nullable @Bind(R.id.profile_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.profile_layout_avatar_iv) ImageView AvatarIv;
  @Nullable @Bind(R.id.profile_layout_username_tv) TextView usernameTv;
  @Nullable @Bind(R.id.profile_layout_nickname_et) EditText nicknameEt;
  @Nullable @Bind(R.id.profile_layout_sex_tv) TextView genderTv;
  @Nullable @Bind(R.id.profile_layout_birthday_tv) TextView birthdayTv;

  public static void startFromLocation(MineActivity startingActivity, int startingLocationY) {

    Intent intent = new Intent(startingActivity, ProfileActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_profile_layout);

    ProfileActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

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
    Button actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    actionButton.setText(getText(R.string.action_submit));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

      }
    });
    return true;
  }

  @Nullable @OnClick(R.id.profile_layout_avatar_rl) void onAvatarClick(View view) {

  }

  @Nullable @OnClick(R.id.profile_layout_sex_rl) void onGenderClick() {

    DialogManager.getInstance()
        .showGenderPickerDialog(ProfileActivity.this, genderTv.getText().toString(),
            new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                  case MALE:
                    genderTv.setText("男");
                    break;
                  case FEMALE:
                    genderTv.setText("女");
                    break;
                }
              }
            });
  }

  @Nullable @OnClick(R.id.profile_layout_birthday_rl) void onBirthdayClick() {

    DatePickDialog dateTimePicKDialog = DatePickDialog.getInstance();
    dateTimePicKDialog.showPickerDialog(ProfileActivity.this, birthdayTv,
        birthdayTv.getText().toString());
  }

  @Nullable @OnClick(R.id.profile_layout_safety_rl) void onSafetyClick(View view) {

    SafetyActivity.startFromLocation(ProfileActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  private void startEnterAnim(int startLocationY) {

    ViewCompat.setLayerType(rootView, ViewCompat.LAYER_TYPE_HARDWARE, null);
    rootView.setPivotY(startLocationY);
    ViewCompat.setScaleY(rootView, 0.0f);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(Constants.MILLISECONDS_400 / 2)
        .setInterpolator(new AccelerateInterpolator());
  }

  @Override public void exit() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(ProfileActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            ProfileActivity.this.finish();
          }
        });
  }
}
