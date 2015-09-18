package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.event.EditorAddressEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.mvp.presenter.AddressPresenter;
import com.app.designmore.mvp.presenter.AddressPresenterImp;
import com.app.designmore.mvp.viewinterface.AddressView;
import com.app.designmore.retrofit.AddressRetrofit;
import com.app.designmore.retrofit.entity.AddressEntity;
import com.app.designmore.retrofit.entity.Province;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.view.dialog.CustomWheelDialog;
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
public class AddressEditorActivity extends BaseActivity implements AddressView {

  private static final String TAG = AddressEditorActivity.class.getSimpleName();
  private static final String ADDRESS = "ADDRESS";

  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.address_editor_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.address_editor_layout_username_et) EditText usernameEt;
  @Nullable @Bind(R.id.address_editor_layout_mobile_et) EditText mobileEt;
  @Nullable @Bind(R.id.address_editor_layout_zipcode_et) EditText zipcodeEt;
  @Nullable @Bind(R.id.address_editor_layout_province_tv) TextView provinceTv;
  @Nullable @Bind(R.id.address_editor_layout_city_tv) TextView cityTv;
  @Nullable @Bind(R.id.address_editor_layout_address_et) EditText addressEt;

  private Subscription subscription = Subscriptions.empty();
  private AddressEntity addressEntity;

  private ProgressDialog progressDialog;
  private ProgressDialog simpleProgressDialog;
  private CustomWheelDialog customWheelDialog;
  private AddressPresenter addressPresenter;

  private Province defaultProvince;
  private Province.City defaultCity;

  private Observable<TextViewTextChangeEvent> userNameChangeObservable;
  private Observable<TextViewTextChangeEvent> mobileChangeObservable;
  private Observable<TextViewTextChangeEvent> zipCodeChangeObservable;
  private Observable<TextViewTextChangeEvent> provinceChangeObservable;
  private Observable<TextViewTextChangeEvent> cityChangeObservable;
  private Observable<TextViewTextChangeEvent> addressChangeObservable;

  private Button actionButton;
  private String userName;
  private String mobile;
  private String zipCode;
  private String province;
  private String city;
  private String address;

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  private CustomWheelDialog.Callback callback = new CustomWheelDialog.Callback() {
    @Override public void onPicked(Province selectProvince, Province.City selectCity) {

      AddressEditorActivity.this.defaultProvince = selectProvince;
      AddressEditorActivity.this.defaultCity = selectCity;

      provinceTv.setText(defaultProvince.getProvinceName());
      cityTv.setText(defaultCity.cityName);
    }
  };

  public static void navigateToAddressEditor(AppCompatActivity startingActivity,
      AddressEntity address) {
    Intent intent = new Intent(startingActivity, AddressEditorActivity.class);
    intent.putExtra(ADDRESS, address);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_address_editor_layout);

    addressPresenter = new AddressPresenterImp();
    addressPresenter.attach(AddressEditorActivity.this, this);
    AddressEditorActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    AddressEditorActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("编辑地址");

    /*bind value*/
    addressEntity = (AddressEntity) getIntent().getSerializableExtra(ADDRESS);
    usernameEt.setText(addressEntity.getUserName());
    usernameEt.setHint(addressEntity.getUserName());
    mobileEt.setText(addressEntity.getMobile());
    mobileEt.setHint(addressEntity.getMobile());
    zipcodeEt.setText(addressEntity.getZipcode());
    zipcodeEt.setHint(addressEntity.getZipcode());
    provinceTv.setText(addressEntity.getProvince());
    provinceTv.setHint(addressEntity.getProvince());
    cityTv.setText(addressEntity.getCity());
    cityTv.setHint(addressEntity.getCity());
    addressEt.setText(addressEntity.getAddress());
    addressEt.setHint(addressEntity.getAddress());
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionButton.setText(getText(R.string.action_done));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AddressEditorActivity.this.requestEditorAddress();
      }
    });

     /*创建联合observable*/
    AddressEditorActivity.this.combineLatestEvents();

    return true;
  }

  private void combineLatestEvents() {

    userNameChangeObservable = RxTextView.textChangeEvents(usernameEt);
    mobileChangeObservable = RxTextView.textChangeEvents(mobileEt);
    zipCodeChangeObservable = RxTextView.textChangeEvents(zipcodeEt);
    provinceChangeObservable = RxTextView.textChangeEvents(provinceTv);
    cityChangeObservable = RxTextView.textChangeEvents(cityTv);
    addressChangeObservable = RxTextView.textChangeEvents(addressEt);

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

            /*Log.e(TAG, "userName: " + userName);
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
        .compose(AddressEditorActivity.this.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<Boolean>() {
          @Override public void call(Boolean aBoolean) {

            //Log.e(TAG, "call() called with: " + "aBoolean = [" + aBoolean + "]");
            actionButton.setEnabled(aBoolean);
          }
        });
  }

  /**
   * 修改地址
   */
  private void requestEditorAddress() {

    /*Action=AddUserByAddress
        &consignee=Eric //收货人
        &mobile=18622816323 //手机号码
        &zipcode=1000111  //邮政编码
        &province=%E5%8C%97%E4%BA%AC //省市
        &city=%E5%8C%97%E4%BA%AC //城市
        &address= //详细地址
        &address_id= //收货地址ID*/

    Map<String, String> params = new HashMap<>(8);

    userName = TextUtils.isEmpty(userName) ? usernameEt.getText().toString() : userName;
    mobile = TextUtils.isEmpty(mobile) ? mobileEt.getText().toString() : mobile;
    zipCode = TextUtils.isEmpty(zipCode) ? zipcodeEt.getText().toString() : zipCode;
    province = TextUtils.isEmpty(province) ? addressEt.getText().toString() : province;
    city = TextUtils.isEmpty(city) ? addressEt.getText().toString() : city;
    address = TextUtils.isEmpty(address) ? addressEt.getText().toString() : address;

    params.put("Action", "AddUserByAddress");
    params.put("consignee", userName);
    params.put("mobile", mobile);
    params.put("zipcode", zipCode);
    params.put("province", province);
    params.put("city", city);
    params.put("address", address);
    params.put("address_id", addressEntity.getAddressId());
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(AddressEditorActivity.this));

    subscription =
        AddressRetrofit.getInstance()
            .requestEditorAddress(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.
                      getInstance()
                      .showSimpleProgressDialog(AddressEditorActivity.this, cancelListener);
                } else {
                  progressDialog.show();
                }
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
                /*修改成功，隐藏进度条*/
                if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
              }
            })
            .filter(new Func1<EditorAddressEvent, Boolean>() {
              @Override public Boolean call(EditorAddressEvent editorAddressEvent) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(
                AddressEditorActivity.this.<AddressEntity>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(new Subscriber<AddressEntity>() {
              @Override public void onCompleted() {
                AddressEditorActivity.this.finish();
              }

              @Override public void onError(Throwable error) {
                AddressEditorActivity.this.showError(error);
              }

              @Override public void onNext(AddressEntity address) {
                EventBusInstance.getDefault().post((EditorAddressEvent) address);
              }
            });
  }

  private void showError(Throwable error) {
    if (error instanceof TimeoutException) {
      AddressEditorActivity.this.showSnackBar(getResources().getString(R.string.timeout_title));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "kind:  " + ((RetrofitError) error).getKind());
      AddressEditorActivity.this.showSnackBar(getResources().getString(R.string.six_word_title));
    } else if (error instanceof WebServiceException) {
      AddressEditorActivity.this.showSnackBar(
          getResources().getString(R.string.service_exception_content));
    } else {
      Log.e(TAG, error.getMessage());
      error.printStackTrace();
      throw new RuntimeException("See inner exception");
    }
  }

  private void showSnackBar(String text) {
    Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT).setAction("确定", null).show();
  }

  @Override public void exit() {
    AddressEditorActivity.this.finish();
  }

  @Nullable @OnClick(R.id.address_editor_layout_province_ll) void onProvinceClick() {
    addressPresenter.showPicker();
  }

  @Nullable @OnClick(R.id.address_editor_layout_city_ll) void onCityClick() {
    addressPresenter.showPicker();
  }

  @Override public void showProgress() {
    simpleProgressDialog = DialogManager.getInstance()
        .showSimpleProgressDialog(AddressEditorActivity.this,
            new DialogInterface.OnCancelListener() {
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

    if (customWheelDialog == null) {
      customWheelDialog = new CustomWheelDialog(AddressEditorActivity.this, provinces, callback);
    }
    customWheelDialog.updateDefault(defaultProvince, defaultCity);
    customWheelDialog.show();
  }

  @Override public void showError() {
    AddressEditorActivity.this.showSnackBar("请重新获取省市");
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    this.progressDialog = null;
    this.simpleProgressDialog = null;
    this.customWheelDialog = null;
    this.addressPresenter.detach();
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
