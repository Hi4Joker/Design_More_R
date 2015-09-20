package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.LoginRetrofit;
import com.app.designmore.retrofit.response.BaseResponse;
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
import rx.functions.Func3;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/27.
 */
public class SafetyActivity extends BaseActivity {

  private static final String TAG = SafetyActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.safety_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.safety_layout_old_password_et) EditText oldPasswordEt;
  @Nullable @Bind(R.id.safety_layout_new_password_et) EditText newPasswordEt;
  @Nullable @Bind(R.id.safety_layout_confim_password_et) EditText confirmPasswordEt;

  private Observable<TextViewTextChangeEvent> oldPasswordChangeObservable;
  private Observable<TextViewTextChangeEvent> newPasswordChangeObservable;
  private Observable<TextViewTextChangeEvent> confirmPasswordChangeObservable;
  private String oldPassword;
  private String newPassword;
  private String confirmPassword;

  private Button actionButton;
  private ProgressDialog progressDialog;
  private Subscription subscription = Subscriptions.empty();

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  public static void startFromLocation(ProfileActivity startingActivity, int startingLocationY) {
    Intent intent = new Intent(startingActivity, SafetyActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_profile_safety_layout);

    SafetyActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    SafetyActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("账户安全");

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

    ViewCompat.setLayerType(rootView, ViewCompat.LAYER_TYPE_HARDWARE, null);
    rootView.setScaleY(0.0f);
    ViewCompat.setPivotY(rootView, startLocationY);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(Constants.MILLISECONDS_400 / 2)
        .setInterpolator(new AccelerateInterpolator());
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionButton.setText(getText(R.string.action_submit));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        SafetyActivity.this.requestChangePassword();
      }
    });

    /*创建联合observable*/
    SafetyActivity.this.combineLatestEvents();

    return true;
  }

  private void combineLatestEvents() {

    oldPasswordChangeObservable = RxTextView.textChangeEvents(oldPasswordEt).skip(1);
    newPasswordChangeObservable = RxTextView.textChangeEvents(newPasswordEt).skip(1);
    confirmPasswordChangeObservable = RxTextView.textChangeEvents(confirmPasswordEt).skip(1);

    Observable.combineLatest(oldPasswordChangeObservable, newPasswordChangeObservable,
        confirmPasswordChangeObservable,
        new Func3<TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, Boolean>() {
          @Override public Boolean call(TextViewTextChangeEvent oldPasswordEvent,
              TextViewTextChangeEvent newPasswordEvent,
              TextViewTextChangeEvent confirmPasswordEvent) {

            oldPassword = oldPasswordEvent.text().toString();
            newPassword = newPasswordEvent.text().toString();
            confirmPassword = confirmPasswordEvent.text().toString();

           /* Log.e(TAG, "oldPassword: " + oldPassword);
            Log.e(TAG, "newPassword: " + newPassword);
            Log.e(TAG, "confirmPassword: " + confirmPassword);*/

            boolean oldPasswordValid = !TextUtils.isEmpty(oldPassword);
            boolean newPasswordValid = !TextUtils.isEmpty(newPassword);
            boolean confirmPasswordValid = !TextUtils.isEmpty(confirmPassword);

            return oldPasswordValid && newPasswordValid && confirmPasswordValid;
          }
        })
        .debounce(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS)
        .compose(SafetyActivity.this.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
        .observeOn(AndroidSchedulers.mainThread())
        .startWith(false)
        .subscribe(new Action1<Boolean>() {
          @Override public void call(Boolean aBoolean) {

            //Log.e(TAG, "call() called with: " + "aBoolean = [" + aBoolean + "]");
            actionButton.setEnabled(aBoolean);
          }
        });
  }

  private void requestChangePassword() {

    if (!confirmPassword.equals(newPassword)) {
      Toast.makeText(SafetyActivity.this, "两次密码不一致", Toast.LENGTH_LONG).show();
      return;
    }

    /*Action=UserByChangePwd&old_passwd=lanlan111&new_passwd=lanlan&uid=2*/
    Map<String, String> params = new HashMap<>(4);
    params.put("Action", "UserByChangePwd");
    params.put("uid", DBHelper.getInstance(getApplicationContext()).getUserID(SafetyActivity.this));
    params.put("old_passwd", oldPassword);
    params.put("new_passwd", newPassword);

    subscription =
        LoginRetrofit.getInstance()
            .requestChangePassword(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.getInstance()
                      .showSimpleProgressDialog(SafetyActivity.this, cancelListener);
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
            .filter(new Func1<BaseResponse, Boolean>() {
              @Override public Boolean call(BaseResponse baseResponse) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(SafetyActivity.this.<BaseResponse>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(new Subscriber<BaseResponse>() {
              @Override public void onCompleted() {
                SafetyActivity.this.exit();
              }

              @Override public void onError(Throwable e) {
                Toast.makeText(SafetyActivity.this, "修改失败，请重试", Toast.LENGTH_LONG).show();
              }

              @Override public void onNext(BaseResponse baseResponse) {
                Toast.makeText(SafetyActivity.this, "修改成功", Toast.LENGTH_LONG).show();
              }
            });
  }

  @Override public void exit() {
    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(SafetyActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            SafetyActivity.super.onBackPressed();
            overridePendingTransition(0, 0);
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
