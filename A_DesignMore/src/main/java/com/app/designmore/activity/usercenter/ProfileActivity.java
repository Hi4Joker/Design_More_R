package com.app.designmore.activity.usercenter;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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
import com.app.designmore.event.AvatarRefreshEvent;
import com.app.designmore.manager.CropCircleTransformation;
import com.app.designmore.retrofit.response.UserInfoEntity;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.view.dialog.CustomCameraDialog;
import com.app.designmore.view.dialog.CustomDatePickDialog;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DialogManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.trello.rxlifecycle.ActivityEvent;
import java.io.File;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func3;

/**
 * Created by Joker on 2015/8/25.
 */
public class ProfileActivity extends BaseActivity implements CustomCameraDialog.Callback {

  private static final String TAG = ProfileActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";
  private static final String USER_INFO_ENTITY = "USER_INFO_ENTITY";
  private static final int MALE = 0;
  private static final int FEMALE = 1;

  @Nullable @Bind(R.id.profile_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.profile_layout_avatar_iv) ImageView avatarIv;
  @Nullable @Bind(R.id.profile_layout_username_tv) TextView usernameTv;
  @Nullable @Bind(R.id.profile_layout_nickname_et) EditText nicknameEt;
  @Nullable @Bind(R.id.profile_layout_sex_tv) TextView genderTv;
  @Nullable @Bind(R.id.profile_layout_birthday_tv) TextView birthdayTv;

  private Observable<TextViewTextChangeEvent> nickNameChangeObservable;
  private Observable<TextViewTextChangeEvent> genderChangeObservable;
  private Observable<TextViewTextChangeEvent> birthdayChangeObservable;

  private String nickName;
  private String gender;
  private String birthday;

  private Button actionButton;
  private UserInfoEntity userInfoEntity;
  private CustomCameraDialog customCameraDialog;
  private AlertDialog genderDialog;
  private CustomDatePickDialog dateTimePicKDialog;

  public static void startFromLocation(MineActivity startingActivity, int startingLocationY,
      UserInfoEntity currentUserInfoEntity) {

    Intent intent = new Intent(startingActivity, ProfileActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    intent.putExtra(USER_INFO_ENTITY, currentUserInfoEntity);
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

    /*绑定数据*/
    ProfileActivity.this.bindValue();

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

  private void bindValue() {

    this.userInfoEntity = (UserInfoEntity) getIntent().getSerializableExtra(USER_INFO_ENTITY);

    nickName = userInfoEntity.getNickname();
    gender = userInfoEntity.getGender();
    birthday = userInfoEntity.getBirthday();

    usernameTv.setText(userInfoEntity.getUserName());
    nicknameEt.setText(nickName);
    genderTv.setText("0".equals(gender) ? "男" : gender);
    birthdayTv.setText("0000-00-00".equals(birthday) ? "1900年01月01日" : birthday);

    BitmapPool bitmapPool = Glide.get(ProfileActivity.this).getBitmapPool();
    Glide.with(ProfileActivity.this)
        .load(userInfoEntity.getHeaderUrl())
        .centerCrop()
        .crossFade()
        .bitmapTransform(new CropCircleTransformation(bitmapPool))
        .placeholder(R.drawable.center_profile_default_icon)
        .error(R.drawable.center_profile_default_icon)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(avatarIv);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionButton.setText(getText(R.string.action_submit));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

      }
    });

     /*创建联合observable*/
    ProfileActivity.this.combineLatestEvents();

    return true;
  }

  private void combineLatestEvents() {

    nickNameChangeObservable = RxTextView.textChangeEvents(nicknameEt);
    genderChangeObservable = RxTextView.textChangeEvents(genderTv);
    birthdayChangeObservable = RxTextView.textChangeEvents(birthdayTv);

    Observable.combineLatest(nickNameChangeObservable, genderChangeObservable,
        birthdayChangeObservable,
        new Func3<TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, Boolean>() {
          @Override public Boolean call(TextViewTextChangeEvent nickNameEvent,
              TextViewTextChangeEvent genderEvent, TextViewTextChangeEvent birthdayEvent) {

            nickName = nickNameEvent.text().toString();
            gender = genderEvent.text().toString();
            birthday = birthdayEvent.text().toString();

            boolean oldPasswordValid = !TextUtils.isEmpty(nickName);
            boolean newPasswordValid = !TextUtils.isEmpty(gender);
            boolean confirmPasswordValid = !TextUtils.isEmpty(birthday);

            return oldPasswordValid && newPasswordValid && confirmPasswordValid;
          }
        })
        .debounce(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS)
        .compose(ProfileActivity.this.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
        .observeOn(AndroidSchedulers.mainThread())
        .startWith(false)
        .subscribe(new Action1<Boolean>() {
          @Override public void call(Boolean aBoolean) {

            actionButton.setEnabled(aBoolean);
          }
        });
  }

  @Nullable @OnClick(R.id.profile_layout_avatar_rl) void onAvatarClick(View view) {
    if (customCameraDialog == null) {
      customCameraDialog = new CustomCameraDialog(ProfileActivity.this, this);
    }
    customCameraDialog.show();
  }

  @Nullable @OnClick(R.id.profile_layout_sex_rl) void onGenderClick() {

    genderDialog = DialogManager.getInstance()
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
    if (dateTimePicKDialog == null) {
      dateTimePicKDialog = CustomDatePickDialog.getInstance();
    }
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

  @Override public void onCameraClick() {
    CameraBackActivity.navigateToCamera(ProfileActivity.this, true);
    overridePendingTransition(0, 0);
  }

  @Override public void onPhotoClick() {
    // TODO: 2015/9/14 打开相册

  }

  /**
   * 加载头像
   */
  public void onEventMainThread(AvatarRefreshEvent event) {

    Log.e(TAG, event.getFile().getName());

    BitmapPool bitmapPool = Glide.get(ProfileActivity.this).getBitmapPool();
    Glide.with(ProfileActivity.this)
        .load(Uri.fromFile(event.getFile()))
        .centerCrop()
        .crossFade()
        .skipMemoryCache(true)
        .bitmapTransform(new CropCircleTransformation(bitmapPool))
        .placeholder(R.drawable.center_profile_default_icon)
        .error(R.drawable.center_profile_default_icon)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(avatarIv);
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    this.genderDialog = null;
    this.dateTimePicKDialog = null;
  }
}
