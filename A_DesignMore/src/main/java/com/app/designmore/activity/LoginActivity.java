package com.app.designmore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;

public class LoginActivity extends BaseActivity {

  /* DBHelper.getInstance(getApplicationContext())
       .saveLoginInfo(new UserInfoEntity(null, "1", "1", "1", "1", "1"));
   DBHelper.getInstance(getApplicationContext())
       .saveLoginInfo(new UserInfoEntity(null, "2", "2", "2", "2", "2"));

   PreferencesUtils.putString(LoginActivity.this, Constants.CURRENT_USER, "1");

   UserInfoEntity userInfoEntity =
       DBHelper.getInstance(getApplicationContext()).getCurrentUser(LoginActivity.this);

   Log.e(TAG, userInfoEntity.getPhone());*/

  private static final String TAG = LoginActivity.class.getSimpleName();

  @Nullable @Bind(R.id.login_layout_anim_root) RelativeLayout animRootView;
  @Nullable @Bind(R.id.login_layout_logo_iv) ImageView loginLogoIv;
  @Nullable @Bind(R.id.login_layout_name_et) EditText userNameEt;
  @Nullable @Bind(R.id.login_layout_name_clear_btn) ImageView clearNameBtn;
  @Nullable @Bind(R.id.login_layout_password_et) EditText passwordEt;
  @Nullable @Bind(R.id.login_layout_login_btn) Button loginBtn;

  private Observable<TextViewTextChangeEvent> userNameChangeObservable;
  private Observable<TextViewTextChangeEvent> passwordChangeObservable;
  private String userName;
  private String password;

  public static void navigateToLogin(AppCompatActivity startingActivity) {

    Intent intent = new Intent(startingActivity, LoginActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_layout);

    LoginActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    /*创建联合observable*/
    LoginActivity.this.combineLatestEvents();

    if (savedInstanceState == null) {
      loginLogoIv.getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
              loginLogoIv.getViewTreeObserver().removeOnPreDrawListener(this);
              LoginActivity.this.startEnterAnim();
              return true;
            }
          });
    } else {
      animRootView.setVisibility(View.VISIBLE);
    }
  }

  private void combineLatestEvents() {

    userNameChangeObservable = RxTextView.textChangeEvents(userNameEt).skip(1);
    passwordChangeObservable = RxTextView.textChangeEvents(passwordEt).skip(1);

    Observable.combineLatest(userNameChangeObservable, passwordChangeObservable,
        new Func2<TextViewTextChangeEvent, TextViewTextChangeEvent, Boolean>() {
          @Override public Boolean call(TextViewTextChangeEvent userNameEvent,
              TextViewTextChangeEvent passwordEvent) {

            userName = userNameEvent.text().toString();
            password = passwordEvent.text().toString();

            Log.e(TAG, "userName: " + userName);
            Log.e(TAG, "password: " + password);

            boolean userNameValid = !TextUtils.isEmpty(userName);
            boolean passwordValid = !TextUtils.isEmpty(password);

            return userNameValid && passwordValid;
          }
        })
        .debounce(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS)
        .compose(LoginActivity.this.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
        .observeOn(AndroidSchedulers.mainThread())
        .startWith(false)
        .subscribe(new Action1<Boolean>() {
          @Override public void call(Boolean aBoolean) {

            Log.e(TAG, "call() called with: " + "aBoolean = [" + aBoolean + "]");
            loginBtn.setEnabled(aBoolean);
          }
        });
  }

  private void startEnterAnim() {

    int startY = DensityUtil.getScreenHeight(LoginActivity.this) + DensityUtil.getStatusBarHeight(
        LoginActivity.this) - loginLogoIv.getHeight();

    ViewCompat.setLayerType(loginLogoIv, ViewCompat.LAYER_TYPE_HARDWARE, null);
    ViewCompat.setY(loginLogoIv, startY / 2);
    ViewCompat.setAlpha(loginLogoIv, 0);

    ViewCompat.animate(loginLogoIv)
        .alpha(255)
        .translationY(0.0f)
        .setStartDelay(Constants.MILLISECONDS_300)
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            if (animRootView != null) animRootView.setVisibility(View.VISIBLE);
          }
        });
  }

  @Override public void exit() {
    LoginActivity.this.finish();
  }

  @Nullable @OnClick(R.id.login_layout_register_tv) void onRegisterClick() {
    LoginActivity.this.startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    overridePendingTransition(0, 0);
  }

  @Nullable @OnClick(R.id.login_layout_retrieve_password_tv) void onRetrieveClick() {
    LoginActivity.this.startActivity(new Intent(LoginActivity.this, RetrieveActivity.class));
    overridePendingTransition(0, 0);
  }
}
