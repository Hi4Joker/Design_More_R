package com.app.designmore.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.LoginRetrofit;
import com.app.designmore.retrofit.entity.LoginCodeEntity;
import com.app.designmore.retrofit.entity.RetrieveEntity;
import com.app.designmore.rxAndroid.SchedulersCompat;
import com.app.designmore.rxAndroid.SimpleObserver;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Observable;
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
public class RetrieveActivity extends BaseActivity {

  private static final String TAG = RetrieveActivity.class.getSimpleName();

  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.retrieve_layout_mobile_et) EditText mobileEt;
  @Nullable @Bind(R.id.retrieve_layout_code_et) EditText codeEt;
  @Nullable @Bind(R.id.retrieve_layout_code_btn) Button codeBtn;
  @Nullable @Bind(R.id.retrieve_layout_password_et) EditText passwordEt;
  @Nullable @Bind(R.id.retrieve_layout_confirm_password_et) EditText confirmEt;
  @Nullable @Bind(R.id.retrieve_layout_retrieve_btn) Button retrieveBtn;

  private Observable<TextViewTextChangeEvent> mobileChangeObservable;
  private Observable<TextViewTextChangeEvent> codeChangeObservable;
  private Observable<TextViewTextChangeEvent> passwordChangeObservable;
  private Observable<TextViewTextChangeEvent> confirmChangeObservable;
  private Subscription subscription = Subscriptions.empty();
  private CompositeSubscription compositeSubscription = new CompositeSubscription();

  private String mobile;
  private String code;
  private String password;
  private String confirmPassword;
  private ProgressDialog progressDialog;
  private ViewGroup toast;

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.retrieve_layout);

    RetrieveActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    RetrieveActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_icon);

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.rightMargin = DensityUtil.getActionBarSize(RetrieveActivity.this);
    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("找回密码");

    RetrieveActivity.this.combineLatestEvents();
  }

  private void combineLatestEvents() {

    mobileChangeObservable = RxTextView.textChangeEvents(mobileEt).skip(1);
    codeChangeObservable = RxTextView.textChangeEvents(codeEt).skip(1);
    passwordChangeObservable = RxTextView.textChangeEvents(passwordEt).skip(1);
    confirmChangeObservable = RxTextView.textChangeEvents(confirmEt).skip(1);

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

    compositeSubscription.add(Observable.combineLatest(mobileChangeObservable, codeChangeObservable,
        passwordChangeObservable, confirmChangeObservable,
        new Func4<TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, Boolean>() {
          @Override public Boolean call(TextViewTextChangeEvent mobileEvent,
              TextViewTextChangeEvent codeEvent, TextViewTextChangeEvent passwordEvent,
              TextViewTextChangeEvent confirmEvent) {

            mobile = mobileEvent.text().toString();
            code = codeEvent.text().toString();
            password = passwordEvent.text().toString();
            confirmPassword = confirmEvent.text().toString();

            boolean userNameValid = !TextUtils.isEmpty(mobile);
            boolean codeValid = !TextUtils.isEmpty(code);
            boolean passwordValid = !TextUtils.isEmpty(password);
            boolean mobileValid = !TextUtils.isEmpty(confirmPassword);

            return userNameValid && passwordValid && mobileValid && codeValid;
          }
        })
        .debounce(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .startWith(false)
        .subscribe(new Action1<Boolean>() {
          @Override public void call(Boolean aBoolean) {

            retrieveBtn.setEnabled(aBoolean);
          }
        }));
  }

  @Nullable @OnClick(R.id.retrieve_layout_name_clear_btn) void onClearClick() {
    mobileEt.setText(null);
  }

  @Nullable @OnClick(R.id.retrieve_layout_code_btn) void onCodeClick() {

    RetrieveActivity.this.startCountDown();
    RetrieveActivity.this.getRetrieveCode();
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
        .compose(RetrieveActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new SimpleObserver<Long>() {
          @Override public void onCompleted() {
            /*60秒倒计时结束，回复按钮状态*/
            codeBtn.setEnabled(true);
            codeBtn.setText("点击获取");
          }

          @Override public void onNext(Long aLong) {
            codeBtn.setText(aLong + "");
          }
        });
  }

  private void getRetrieveCode() {

    /*Action=SendCheckMessage&mobile=18622816323&message=请妥善保管验证码，*/
    Map<String, String> params = new HashMap<>();
    params.put("Action", "SendCheckMessage");
    params.put("mobile", mobileEt.getText().toString());
    params.put("message", "设计猫提醒您，请妥善保管验证码,");

    LoginRetrofit.getInstance()
        .getAuthCode(params)
        .compose(RetrieveActivity.this.<LoginCodeEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Action1<LoginCodeEntity>() {
          @Override public void call(LoginCodeEntity loginCodeEntity) {
            RetrieveActivity.this.code = loginCodeEntity.getCode();
            codeEt.setText(code);
          }
        });
  }

  @Nullable @OnClick(R.id.retrieve_layout_retrieve_btn) void onRetrieveClick() {

    if (!password.equals(confirmPassword)) {
      Toast.makeText(RetrieveActivity.this, "两次密码不一致", Toast.LENGTH_LONG).show();
      return;
    }

    /* Action= findMyPasswd&mobile=18622816323&pwd=新密码*/
    Map<String, String> params = new HashMap<>();
    params.put("Action", "findMyPasswd");
    params.put("mobile", mobile);
    params.put("pwd", confirmPassword);

    subscription = LoginRetrofit.getInstance()
        .requestRetrieve(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            if (progressDialog == null) {
              progressDialog = DialogManager.
                  getInstance().showSimpleProgressDialog(RetrieveActivity.this, cancelListener);
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
        .filter(new Func1<RetrieveEntity, Boolean>() {
          @Override public Boolean call(RetrieveEntity retrieveEntity) {
            return !subscription.isUnsubscribed();
          }
        })
        .compose(RetrieveActivity.this.<RetrieveEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Action1<RetrieveEntity>() {
          @Override public void call(RetrieveEntity retrieveEntity) {

            Toast.makeText(RetrieveActivity.this, retrieveEntity.getRegisterMessage(),
                Toast.LENGTH_LONG).show();
            if (retrieveEntity.getRegisterCode() == Constants.RESULT_OK) {
              RetrieveActivity.this.finish();
            }
          }
        }, new Action1<Throwable>() {
          @Override public void call(Throwable error) {
            toast = DialogManager.getInstance()
                .showNoMoreDialog(RetrieveActivity.this, Gravity.TOP, "操作失败，请重试，O__O …");
          }
        });
  }

  @Override public void exit() {
    RetrieveActivity.this.finish();
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);
    }
    this.toast = null;
    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
    if (compositeSubscription.hasSubscriptions()) compositeSubscription.clear();
  }
}
