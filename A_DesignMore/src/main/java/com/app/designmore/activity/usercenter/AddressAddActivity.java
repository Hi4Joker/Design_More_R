package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.event.RefreshAddressEvent;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.mvp.presenter.AddressPresenter;
import com.app.designmore.mvp.presenter.AddressPresenterImp;
import com.app.designmore.mvp.viewinterface.AddressView;
import com.app.designmore.retrofit.AddressRetrofit;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.retrofit.entity.Province;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.CustomWheelPicker;
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
import rx.functions.Func6;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/25.
 */
public class AddressAddActivity extends BaseActivity implements AddressView {

  private static final String TAG = AddressAddActivity.class.getSimpleName();

  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.address_add_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.address_add_layout_rfl) RevealFrameLayout revealFrameLayout;

  @Nullable @Bind(R.id.address_add_layout_username_et) EditText usernameEt;
  @Nullable @Bind(R.id.address_add_layout_mobile_et) EditText mobileEt;
  @Nullable @Bind(R.id.address_add_layout_zipcode_et) EditText zipcodeEt;
  @Nullable @Bind(R.id.address_add_layout_address_et) EditText addressEt;
  @Nullable @Bind(R.id.address_add_layout_province_tv) TextView provinceTv;
  @Nullable @Bind(R.id.address_add_layout_city_tv) TextView cityTv;

  private Observable<TextViewTextChangeEvent> userNameChangeObservable;
  private Observable<TextViewTextChangeEvent> mobileChangeObservable;
  private Observable<TextViewTextChangeEvent> zipCodeChangeObservable;
  private Observable<TextViewTextChangeEvent> provinceChangeObservable;
  private Observable<TextViewTextChangeEvent> cityChangeObservable;
  private Observable<TextViewTextChangeEvent> addressChangeObservable;

  private SupportAnimator revealAnimator;
  private Subscription subscription = Subscriptions.empty();
  private ProgressDialog progressDialog;
  private ProgressDialog simpleProgressDialog;
  private CustomWheelPicker customWheelPicker;

  private AddressPresenter addressPresenter;

  private Button actionButton;
  private String userName;
  private String mobile;
  private String zipCode;
  private String province;
  private String city;
  private String address;

  private Province defaultProvince;
  private Province.City defaultCity;

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  private CustomWheelPicker.Callback callback = new CustomWheelPicker.Callback() {
    @Override public void onPicked(Province selectProvince, Province.City selectCity) {

      AddressAddActivity.this.defaultProvince = selectProvince;
      AddressAddActivity.this.defaultCity = selectCity;

      provinceTv.setText(defaultProvince.getProvinceName());
      cityTv.setText(defaultCity.cityName);
    }
  };

  public static void navigateToAddressEditor(AppCompatActivity startingActivity) {
    Intent intent = new Intent(startingActivity, AddressAddActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_address_add_layout);

    addressPresenter = new AddressPresenterImp();
    addressPresenter.attach(AddressAddActivity.this, this);

    AddressAddActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    AddressAddActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("新增地址");

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          AddressAddActivity.this.startEnterAnim();
          return true;
        }
      });
    }
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    revealFrameLayout.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(revealFrameLayout.getChildAt(0), bounds.right, 0, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.start();
  }

  /**
   * 合并observable
   */
  private void combineLatestEvents() {

    /*全部skip(0)，也能达到效果，但是会闪顿*/
    userNameChangeObservable = RxTextView.textChangeEvents(usernameEt).skip(1);
    mobileChangeObservable = RxTextView.textChangeEvents(mobileEt).skip(1);
    zipCodeChangeObservable = RxTextView.textChangeEvents(zipcodeEt).skip(1);
    provinceChangeObservable = RxTextView.textChangeEvents(provinceTv).skip(1);
    cityChangeObservable = RxTextView.textChangeEvents(cityTv).skip(1);
    addressChangeObservable = RxTextView.textChangeEvents(addressEt).skip(1);

    Observable.combineLatest(userNameChangeObservable, mobileChangeObservable,
        zipCodeChangeObservable, provinceChangeObservable, cityChangeObservable,
        addressChangeObservable,
        new Func6<TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, TextViewTextChangeEvent, Boolean>() {
          @Override public Boolean call(TextViewTextChangeEvent userNameEvent,
              TextViewTextChangeEvent mobileEvent, TextViewTextChangeEvent zipCodeEvent,
              TextViewTextChangeEvent provinceEvent, TextViewTextChangeEvent cityEvent,
              TextViewTextChangeEvent addressEvent) {

            userName = userNameEvent.text().toString();
            mobile = mobileEvent.text().toString();
            zipCode = zipCodeEvent.text().toString();
            province = provinceEvent.text().toString();
            city = cityEvent.text().toString();
            address = addressEvent.text().toString();

           /* Log.e(TAG, "userName: " + userName);
            Log.e(TAG, "mobile: " + mobile);
            Log.e(TAG, "zipCode: " + zipCode);
            Log.e(TAG, "province: " + province);
            Log.e(TAG, "city: " + city);
            Log.e(TAG, "address: " + address);*/

            boolean userNameValid = !TextUtils.isEmpty(userName);
            boolean mobileValid = !TextUtils.isEmpty(mobile);
            boolean zipCodeValid = !TextUtils.isEmpty(zipCode);
            boolean provinceValid = !TextUtils.isEmpty(province);
            boolean cityValid = !TextUtils.isEmpty(city);
            boolean addressValid = !TextUtils.isEmpty(address);

            return userNameValid
                && mobileValid
                && zipCodeValid
                && provinceValid
                && cityValid
                && addressValid;
          }
        })
        .debounce(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS)
        .compose(AddressAddActivity.this.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
        .observeOn(AndroidSchedulers.mainThread())
        .startWith(false)
        .subscribe(new Action1<Boolean>() {
          @Override public void call(Boolean aBoolean) {

            //Log.e(TAG, "call() called with: " + "aBoolean = [" + aBoolean + "]");
            actionButton.setEnabled(aBoolean);
          }
        });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    actionButton.setText(getText(R.string.action_done));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AddressAddActivity.this.requestAddAddress();
      }
    });

    /*创建联合observable*/
    AddressAddActivity.this.combineLatestEvents();

    return true;
  }

  /**
   * 增加地址
   */
  private void requestAddAddress() {

    /*Action=AddUserByAddress
        &consignee=Eric //收货人
        &mobile=18622816323 //手机号码
        &zipcode=1000111  //邮政编码
        &province=%E5%8C%97%E4%BA%AC //省市
        &city=%E5%8C%97%E4%BA%AC //城市
        &address= //详细地址
        &uid= //用户id
        */
    Map<String, String> params = new HashMap<>(8);

    params.put("Action", "AddUserByAddress");
    params.put("consignee", usernameEt.getText().toString());
    params.put("mobile", mobileEt.getText().toString());
    params.put("zipcode", zipcodeEt.getText().toString());
    params.put("province", provinceTv.getText().toString());
    params.put("city", cityTv.getText().toString());
    params.put("address", addressEt.getText().toString());
    params.put("uid", "1");

    subscription =
        AddressRetrofit.getInstance()
            .requestAddAddress(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*加载数据，显示进度条*/
                progressDialog = DialogManager.
                    getInstance().showSimpleProgressDialog(AddressAddActivity.this, cancelListener);
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
            .filter(new Func1<RefreshAddressEvent, Boolean>() {
              @Override public Boolean call(RefreshAddressEvent refreshAddressEvent) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(
                AddressAddActivity.this.<RefreshAddressEvent>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(new Subscriber<RefreshAddressEvent>() {
              @Override public void onCompleted() {
                  /*增加成功，返回，刷新*/
                AddressAddActivity.this.exit();
              }

              @Override public void onError(Throwable error) {
                AddressAddActivity.this.showError(error);
              }

              @Override public void onNext(RefreshAddressEvent refreshAddressEvent) {
                  /*通过eventBus发送通知，刷新地址列表*/
                EventBusInstance.getDefault().post(refreshAddressEvent);
              }
            });
  }

  private void showError(Throwable error) {
    if (error instanceof TimeoutException) {
      AddressAddActivity.this.showSnackBar(getResources().getString(R.string.timeout_title));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "kind:  " + ((RetrofitError) error).getKind());
      AddressAddActivity.this.showSnackBar(getResources().getString(R.string.six_word));
    } else if (error instanceof WebServiceException) {
      AddressAddActivity.this.showSnackBar(
          getResources().getString(R.string.service_exception_content));
    } else {
      Log.e(TAG, error.getMessage());
      error.printStackTrace();
      throw new RuntimeException("See inner exception");
    }
  }

  private void showSnackBar(String text) {
    Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT)
        .setAction("确定", new View.OnClickListener() {
          @Override public void onClick(View v) {
        /*do nothing*/
          }
        })
        .show();
  }

  @Override public void exit() {

    if (revealAnimator != null && !revealAnimator.isRunning()) {
      revealAnimator = revealAnimator.reverse();
      revealAnimator.setDuration(Constants.MILLISECONDS_400);
      revealAnimator.setInterpolator(new AccelerateInterpolator());
      revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
        @Override public void onAnimationEnd() {
          rootView.setVisibility(View.GONE);
          AddressAddActivity.this.finish();
        }

        @Override public void onAnimationCancel() {
          AddressAddActivity.this.finish();
        }
      });
      revealAnimator.start();
    } else if (revealAnimator != null && revealAnimator.isRunning()) {
      revealAnimator.cancel();
    } else if (revealAnimator == null) {
      AddressAddActivity.this.finish();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    this.progressDialog = null;
    this.simpleProgressDialog = null;
    this.addressPresenter.detach();
    if (customWheelPicker != null && !customWheelPicker.isShowing()) customWheelPicker.destroy();
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }

  @Nullable @OnClick(R.id.address_add_layout_province_ll) void onProvinceClick() {
    addressPresenter.showPicker();
  }

  @Nullable @OnClick(R.id.address_add_layout_city_ll) void onCityClick() {
    addressPresenter.showPicker();
  }

  @Override public void showProgress() {
    simpleProgressDialog = DialogManager.getInstance()
        .showSimpleProgressDialog(AddressAddActivity.this, new DialogInterface.OnCancelListener() {
          @Override public void onCancel(DialogInterface dialog) {
            addressPresenter.detach();
          }
        });
  }

  @Override public void hideProgress() {
    if (simpleProgressDialog != null && simpleProgressDialog.isShowing()) {
      simpleProgressDialog.dismiss();
    }
  }

  @Override public void onInflateFinish(List<Province> provinces) {

    if (customWheelPicker == null) {
      customWheelPicker = new CustomWheelPicker(AddressAddActivity.this, provinces, callback);
    }

    customWheelPicker.updateDefault(defaultProvince, defaultCity);
    customWheelPicker.show();
  }

  @Override public void showError() {
    AddressAddActivity.this.showSnackBar("请重新获取省市");
  }
}
