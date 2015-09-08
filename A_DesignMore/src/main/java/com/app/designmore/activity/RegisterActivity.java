package com.app.designmore.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.event.RefreshAddressEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.retrofit.LoginRetrofit;
import com.app.designmore.retrofit.entity.LoginCodeEntity;
import com.app.designmore.retrofit.entity.RegisterEntity;
import com.app.designmore.retrofit.entity.SearchItemEntity;
import com.app.designmore.rxAndroid.SchedulersCompat;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func4;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/25.
 */
public class RegisterActivity extends BaseActivity {

  private static final String TAG = RegisterActivity.class.getSimpleName();

  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.register_layout_name_et) EditText userNameEt;
  @Nullable @Bind(R.id.register_layout_password_et) EditText passwordEt;
  @Nullable @Bind(R.id.register_layout_phone_et) EditText mobileEt;
  @Nullable @Bind(R.id.register_layout_code_et) EditText codeEt;
  @Nullable @Bind(R.id.register_layout_code_btn) Button codeBtn;
  @Nullable @Bind(R.id.register_layout_register_btn) Button registerBtn;

  private Observable<TextViewTextChangeEvent> userNameChangeObservable;
  private Observable<TextViewTextChangeEvent> codeChangeObservable;
  private Observable<TextViewTextChangeEvent> passwordChangeObservable;
  private Observable<TextViewTextChangeEvent> mobileChangeObservable;
  private String userName;
  private String password;
  private String mobile;
  private String code;

  private CompositeSubscription compositeSubscription = new CompositeSubscription();
  private ProgressDialog progressDialog;
  private Subscription subscription = Subscriptions.empty();

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.register_layout);
    ButterKnife.bind(RegisterActivity.this);

    RegisterActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    RegisterActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.rightMargin = DensityUtil.getActionBarSize(RegisterActivity.this);
    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("注 册");

    RegisterActivity.this.combineLatestEvents();
  }

  private void combineLatestEvents() {

    userNameChangeObservable = RxTextView.textChangeEvents(userNameEt).skip(1);
    passwordChangeObservable = RxTextView.textChangeEvents(passwordEt).skip(1);
    mobileChangeObservable = RxTextView.textChangeEvents(mobileEt).skip(1);
    codeChangeObservable = RxTextView.textChangeEvents(codeEt).skip(1);

    compositeSubscription.add(mobileChangeObservable.doOnSubscribe(new Action0() {
      @Override public void call() {
        codeBtn.setEnabled(false);
      }
    })
        .debounce(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<TextViewTextChangeEvent>() {
          @Override public void call(TextViewTextChangeEvent textEvent) {
            codeBtn.setEnabled(!TextUtils.isEmpty(textEvent.text().toString()));
          }
        }));

    compositeSubscription.add(
        Observable.combineLatest(userNameChangeObservable, passwordChangeObservable,
            mobileChangeObservable, codeChangeObservable,
            new Func4<TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, Boolean>() {
              @Override public Boolean call(TextViewTextChangeEvent userNameEvent,
                  TextViewTextChangeEvent passwordEvent, TextViewTextChangeEvent mobileEvent,
                  TextViewTextChangeEvent codeEvent) {

                userName = userNameEvent.text().toString();
                password = passwordEvent.text().toString();
                mobile = mobileEvent.text().toString();
                code = codeEvent.text().toString();

             /*   Log.e(TAG, "userName: " + userName);
                Log.e(TAG, "password: " + password);
                Log.e(TAG, "mobile: " + mobile);
                Log.e(TAG, "code: " + code);*/

                boolean userNameValid = !TextUtils.isEmpty(userName);
                boolean passwordValid = !TextUtils.isEmpty(password);
                boolean mobileValid = !TextUtils.isEmpty(mobile) /*&& Utils.isMobile(mobile)*/;
                boolean codeValid = !TextUtils.isEmpty(code);

                return userNameValid && passwordValid && mobileValid && codeValid;
              }
            })
            .debounce(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS)
            .startWith(false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<Boolean>() {
              @Override public void call(Boolean aBoolean) {

                //Log.e(TAG, "call() called with: " + "aBoolean = [" + aBoolean + "]");
                registerBtn.setEnabled(aBoolean);
              }
            }));
  }

  @Nullable @OnClick(R.id.register_layout_code_btn) void onButtonClick(final Button button) {

    RegisterActivity.this.startCountDown();
    RegisterActivity.this.getLoginCode();
  }

  private void getLoginCode() {

    /*Action=SendCheckMessage&mobile=18622816323&message=欢迎您注册设计猫，*/
    Map<String, String> params = new HashMap<>();
    params.put("Action", "SendCheckMessage");
    params.put("mobile", mobileEt.getText().toString());
    params.put("message", "欢迎您注册设计猫,");

    LoginRetrofit.getInstance()
        .getLoginCode(params)
        .compose(RegisterActivity.this.<LoginCodeEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Action1<LoginCodeEntity>() {
          @Override public void call(LoginCodeEntity loginCodeEntity) {
            RegisterActivity.this.code = loginCodeEntity.getCode();
            codeEt.setText(code);
          }
        });
  }

  private void startCountDown() {

    Observable.interval(0, 1, TimeUnit.SECONDS)
        .take(10)
        .map(new Func1<Long, Long>() {
          @Override public Long call(Long aLong) {
            return 10 - aLong;
          }
        })
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            codeBtn.setEnabled(false);
          }
        })
        .compose(SchedulersCompat.<Long>applyComputationSchedulers())
        .compose(RegisterActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<Long>() {
          @Override public void onCompleted() {

            /*60秒倒计时结束，回复按钮状态*/
            codeBtn.setEnabled(true);
            codeBtn.setText("点击获取");
            compositeSubscription.remove(this);
          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onNext(Long aLong) {
            codeBtn.setText(aLong + "");
          }
        });
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        RegisterActivity.this.finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Nullable @OnClick(R.id.register_layout_name_clear_btn) void onClearClick() {
    userNameEt.setText(null);
  }

  @Nullable @OnClick(R.id.register_layout_register_btn) void onRegisterClick() {

    /* username=linuxlan22221
    &password=lanlan1111
    &Action=RegisterUser
    &mobile_phone=18622816322
    &email=adsfasdf%40aaa.com*/

    Map<String, String> params = new HashMap<>();
    params.put("Action", "RegisterUser");
    params.put("username", userName);
    params.put("mobile_phone", mobile);
    params.put("email", "1170859911@qq.com");

    subscription = LoginRetrofit.getInstance()
        .requestRegister(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progressDialog = DialogManager.
                getInstance().showProgressDialog(RegisterActivity.this, null, cancelListener);
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
        .filter(new Func1<RegisterEntity, Boolean>() {
          @Override public Boolean call(RegisterEntity registerEntity) {
            return !subscription.isUnsubscribed();
          }
        })
        .compose(RegisterActivity.this.<RegisterEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<RegisterEntity>() {
          @Override public void onCompleted() {
          }

          @Override public void onError(Throwable error) {
            RegisterActivity.this.showError(error);
          }

          @Override public void onNext(RegisterEntity registerEntity) {
          }
        });
  }

  private void showError(Throwable error) {
    if (error instanceof TimeoutException) {
      RegisterActivity.this.showSnackBar(getResources().getString(R.string.timeout_title));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "kind:  " + ((RetrofitError) error).getKind());
      RegisterActivity.this.showSnackBar(getResources().getString(R.string.six_word));
    } else if (error instanceof WebServiceException) {
      RegisterActivity.this.showSnackBar(
          getResources().getString(R.string.service_exception_content));
    } else {
      Log.e(TAG, error.getMessage());
      error.printStackTrace();
      throw new RuntimeException("See inner exception");
    }
  }

  private void showSnackBar(String text) {
    Snackbar.make(toolbar, text, Snackbar.LENGTH_SHORT).setAction("确定", new View.OnClickListener() {
      @Override public void onClick(View v) {
        /*do nothing*/
      }
    }).show();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (compositeSubscription.hasSubscriptions()) compositeSubscription.clear();
  }
}
