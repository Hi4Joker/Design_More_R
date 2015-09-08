package com.app.designmore.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.LoginRetrofit;
import com.app.designmore.retrofit.entity.LoginCodeEntity;
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
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func4;
import rx.subscriptions.CompositeSubscription;

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

  //private int redColor = getResources().getColor(R.color.design_more_red);

  private CompositeSubscription compositeSubscription = new CompositeSubscription();

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
    }).subscribe(new Action1<TextViewTextChangeEvent>() {
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

                boolean userNameValid = !TextUtils.isEmpty(userName);
                boolean passwordValid = !TextUtils.isEmpty(password);
                boolean mobileValid = !TextUtils.isEmpty(mobile) && Utils.isMobile(mobile);
                boolean codeValid = !TextUtils.isEmpty(code);

                return userNameValid && passwordValid && mobileValid && codeValid;
              }
            })
            .debounce(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS)
            .startWith(false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<Boolean>() {
              @Override public void call(Boolean aBoolean) {

                Log.e(TAG, "call() called with: " + "aBoolean = [" + aBoolean + "]");
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
    params.put("Action", "");
    params.put("username", userName);
    params.put("mobile_phone", mobile);
    params.put("email", code);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (compositeSubscription.hasSubscriptions()) compositeSubscription.clear();
  }
}
