package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
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
import android.view.Gravity;
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
import android.widget.Toast;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.event.AvatarRefreshEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.CropCircleTransformation;
import com.app.designmore.retrofit.LoginRetrofit;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.retrofit.response.UserInfoEntity;
import com.app.designmore.rxAndroid.SchedulersCompat;
import com.app.designmore.rxAndroid.SimpleObserver;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.view.ProgressLayout;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.functions.Func4;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/25.
 */
public class ProfileActivity extends BaseActivity implements CustomCameraDialog.Callback {

  private static final String TAG = ProfileActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";
  private static final int MALE = 0;
  private static final int FEMALE = 1;

  @Nullable @Bind(R.id.profile_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.profile_layout_pl) ProgressLayout progressLayout;
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
  private File avatarFile = null;

  private Button actionButton;
  private CustomCameraDialog customCameraDialog;
  private AlertDialog genderDialog;
  private CustomDatePickDialog dateTimePicKDialog;
  private ProgressDialog progressDialog;
  private View toast;

  private Subscription subscription = Subscriptions.empty();

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      ProfileActivity.this.loadData();
    }
  };

  public static void startFromLocation(MineActivity startingActivity, int startingLocationY) {
    Intent intent = new Intent(startingActivity, ProfileActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivityForResult(intent, Constants.ACTIVITY_CODE);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_profile_layout);
    ProfileActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    ProfileActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

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
    } else {
      ProfileActivity.this.loadData();
    }
  }

  private void loadData() {

    /*Action=GetUserInfo&uid=2*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GetUserInfo");
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(ProfileActivity.this));

    LoginRetrofit.getInstance()
        .requestUserInfo(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progressLayout.showLoading();
          }
        })
        .compose(ProfileActivity.this.<UserInfoEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<UserInfoEntity>() {
          @Override public void onCompleted() {
            progressLayout.showContent();
          }

          @Override public void onError(Throwable error) {
            /*加载失败，显示错误界面*/
            ProfileActivity.this.showErrorLayout(error);
          }

          @Override public void onNext(UserInfoEntity userInfoEntity) {

            ProfileActivity.this.bindValue(userInfoEntity);

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
        });
  }

  private void showErrorLayout(Throwable error) {
    if (error instanceof TimeoutException) {
      ProfileActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      ProfileActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_title));
    } else if (error instanceof WebServiceException) {
      ProfileActivity.this.showError(getResources().getString(R.string.service_exception_title),
          getResources().getString(R.string.service_exception_content));
    } else {
      Log.e(TAG, error.getMessage());
      error.printStackTrace();
      throw new RuntimeException("See inner exception");
    }
  }

  private void showError(String errorTitle, String errorContent) {
    progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon), errorTitle,
        errorContent, getResources().getString(R.string.retry_button_text), retryClickListener);
  }

  private void bindValue(UserInfoEntity userInfoEntity) {

    nickName = userInfoEntity.getNickname();
    gender = userInfoEntity.getGender();
    birthday = userInfoEntity.getBirthday();

    usernameTv.setText(userInfoEntity.getUserName());
    nicknameEt.setText(nickName);
    genderTv.setText("0".equals(gender) ? "男" : gender);
    birthdayTv.setText(birthday.contains("-") ? "1900年01月01日" : birthday);

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
    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionButton.setText(getText(R.string.action_done));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        ProfileActivity.this.requestChangeUserInfo();
      }
    });

    /*创建联合observable*/
    ProfileActivity.this.combineLatestEvents();

    return true;
  }

  /**
   * 修改用户资料
   */
  private void requestChangeUserInfo() {

    final LoginRetrofit loginRetrofit = LoginRetrofit.getInstance();
    /* Action=EditUserInfo&uid=2&key=nickname&data=aaaaa*/
    final Map<String, String> baseParams = new HashMap<>(2);
    baseParams.put("Action", "EditUserInfo");
    baseParams.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(ProfileActivity.this));

    final Map<String, String> nickParams = new HashMap<>(4);
    nickParams.putAll(baseParams);
    nickParams.put("key", "nickname");
    nickParams.put("data", nickName);

    final Map<String, String> genderParams = new HashMap<>(4);
    genderParams.putAll(baseParams);
    genderParams.put("key", "sex");
    genderParams.put("data", gender);

    final Map<String, String> birthdayParams = new HashMap<>(4);
    birthdayParams.putAll(baseParams);
    birthdayParams.put("key", "birthday");
    birthdayParams.put("data", birthday);

    subscription = Observable.defer(new Func0<Observable<Boolean>>() {
      @Override public Observable<Boolean> call() {

        if (avatarFile == null) {
          return Observable.zip(loginRetrofit.requestChangeUserInfo(nickParams),
              loginRetrofit.requestChangeUserInfo(genderParams),
              loginRetrofit.requestChangeUserInfo(birthdayParams),
              new Func3<BaseResponse, BaseResponse, BaseResponse, Boolean>() {
                @Override public Boolean call(BaseResponse baseResponse, BaseResponse baseResponse2,
                    BaseResponse baseResponse3) {
                  return baseResponse.resultOK()
                      && baseResponse2.resultOK()
                      && baseResponse3.resultOK();
                }
              });
        } else {

          final Map<String, TypedString> uploadParams = new HashMap<>(2);
          uploadParams.put("Action", new TypedString("UploadHeader"));
          uploadParams.put("uid", new TypedString(
              DBHelper.getInstance(getApplicationContext()).getUserID(ProfileActivity.this)));
          final TypedFile typedFile = new TypedFile("multipart/form-data", avatarFile);

          return Observable.zip(loginRetrofit.requestChangeUserInfo(nickParams),
              loginRetrofit.requestChangeUserInfo(genderParams),
              loginRetrofit.requestChangeUserInfo(birthdayParams),
              loginRetrofit.uploadProfileHeader(uploadParams, typedFile),
              new Func4<BaseResponse, BaseResponse, BaseResponse, BaseResponse, Boolean>() {
                @Override public Boolean call(BaseResponse baseResponse, BaseResponse baseResponse2,
                    BaseResponse baseResponse3, BaseResponse baseResponse4) {
                  return baseResponse.resultOK()
                      && baseResponse2.resultOK()
                      && baseResponse3.resultOK()
                      && baseResponse4.resultOK();
                }
              });
        }
      }
    })
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*显示进度条*/
            if (progressDialog == null) {
              progressDialog = DialogManager.getInstance()
                  .showSimpleProgressDialog(ProfileActivity.this, cancelListener);
            } else {
              progressDialog.show();
            }
          }
        })
        .doOnTerminate(new Action0() {
          @Override public void call() {
            /*隐藏进度条*/
            if (progressDialog != null && progressDialog.isShowing()) {
              progressDialog.dismiss();
            }
          }
        })
        .filter(new Func1<Boolean, Boolean>() {
          @Override public Boolean call(Boolean b) {
            return !subscription.isUnsubscribed();
          }
        })
        .compose(ProfileActivity.this.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new SimpleObserver<Boolean>() {
          @Override public void onError(Throwable e) {

            toast = DialogManager.getInstance()
                .showNoMoreDialog(ProfileActivity.this, Gravity.TOP, "操作失败，请重试，O__O …");
          }

          @Override public void onNext(Boolean isOk) {

            Toast.makeText(ProfileActivity.this, "操作成功", Toast.LENGTH_LONG).show();

            Intent intent = new Intent();
            if (avatarFile != null) {
              intent.putExtra(MineActivity.FILE_URL, Uri.fromFile(avatarFile));
            }
            intent.putExtra(MineActivity.NICKNAME, nickName);
            ProfileActivity.this.setResult(RESULT_OK, intent);

            ProfileActivity.this.exit();
          }
        });
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
      customCameraDialog = DialogManager.getInstance().showCameraDialog(ProfileActivity.this, this);
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
      dateTimePicKDialog = new CustomDatePickDialog(ProfileActivity.this);
    }
    dateTimePicKDialog.showPickerDialog(birthdayTv, birthdayTv.getText().toString());
  }

  @Nullable @OnClick(R.id.profile_layout_safety_rl) void onSafetyClick(View view) {
    SafetyActivity.startFromLocation(ProfileActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  @Override public void onCameraClick() {
    CameraBackActivity.navigateToCamera(ProfileActivity.this, true);
    overridePendingTransition(0, 0);
  }

  @Override public void onPhotoClick() {
    ProfileGalleryActivity.navigateToGallery(ProfileActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 加载头像
   */
  public void onEventMainThread(AvatarRefreshEvent event) {

    avatarFile = event.getFile();

    BitmapPool bitmapPool = Glide.get(ProfileActivity.this).getBitmapPool();
    Glide.with(ProfileActivity.this)
        .load(Uri.fromFile(avatarFile))
        .centerCrop()
        .crossFade()
        .skipMemoryCache(true)
        .bitmapTransform(new CropCircleTransformation(bitmapPool))
        .placeholder(R.drawable.center_profile_default_icon)
        .error(R.drawable.center_profile_default_icon)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(avatarIv);
  }

  private void startEnterAnim(int startLocationY) {

    rootView.setPivotY(startLocationY);
    ViewCompat.setScaleY(rootView, 0.0f);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(Constants.MILLISECONDS_400 / 2)
        .setInterpolator(new AccelerateInterpolator()) .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            if (progressLayout != null) ProfileActivity.this.loadData();
          }
        });
  }

  @Override public void exit() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(ProfileActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator()) .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            ProfileActivity.this.finish();
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);
    }
    this.toast = null;
    this.progressDialog = null;
    this.genderDialog = null;
    this.dateTimePicKDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
