package com.app.designmore.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.greendao.entity.Dao_LoginInfo;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.LoginRetrofit;
import com.app.designmore.retrofit.entity.LoginEntity;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.Subscriptions;

public class LoginActivity extends BaseActivity {

  /* DBHelper.getInstance(getApplicationContext())
       .saveLoginInfo(new UserInfoEntity(null, "1", "1", "1", "1", "1"));
   DBHelper.getInstance(getApplicationContext())
       .saveLoginInfo(new UserInfoEntity(null, "2", "2", "2", "2", "2"));

   PreferencesUtils.putString(LoginActivity.this, Constants.CURRENT_USER_ID, "1");

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
  private ProgressDialog progressDialog;

  private Subscription subscription = Subscriptions.empty();

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

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

           /* Log.e(TAG, "userName: " + userName);
            Log.e(TAG, "password: " + password);*/

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

            //Log.e(TAG, "call() called with: " + "aBoolean = [" + aBoolean + "]");
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

  @Nullable @OnClick(R.id.login_layout_login_btn) void onLoginClick() {


    /*UserId=linuxlan&Password=lanlan&Action=UserLogin*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "UserLogin");
    params.put("UserId", userName);
    params.put("Password", password);

    subscription = LoginRetrofit.getInstance()
        .requestLogin(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            if (progressDialog == null) {
              progressDialog = DialogManager.
                  getInstance().showSimpleProgressDialog(LoginActivity.this, cancelListener);
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
        .filter(new Func1<LoginEntity, Boolean>() {
          @Override public Boolean call(LoginEntity loginEntity) {
            return !subscription.isUnsubscribed();
          }
        })
        .compose(LoginActivity.this.<LoginEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<LoginEntity>() {
          @Override public void onCompleted() {
            HomeActivity.navigateToHome(LoginActivity.this);
            overridePendingTransition(0, 0);
          }

          @Override public void onError(Throwable e) {
            if (e instanceof WebServiceException) {
              Toast.makeText(LoginActivity.this, "密码错误，请重试", Toast.LENGTH_LONG).show();
            } else {
              Toast.makeText(LoginActivity.this, "登陆失败，请重试", Toast.LENGTH_LONG).show();
            }
          }

          @Override public void onNext(LoginEntity loginEntity) {

            /*save数据库*/
            DBHelper.getInstance(getApplicationContext())
                .saveLoginInfo(LoginActivity.this,
                    new Dao_LoginInfo(null, loginEntity.getUserId(), loginEntity.getAddressId()));
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
