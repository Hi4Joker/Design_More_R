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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.R;
import com.app.designmore.event.EditorAddressEvent;
import com.app.designmore.event.RefreshAddressEvent;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.retrofit.AddressRetrofit;
import com.app.designmore.retrofit.HttpException;
import com.app.designmore.retrofit.entity.Address;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/25.
 */
public class AddressEditorActivity extends RxAppCompatActivity {

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
  private ProgressDialog progressDialog;
  private Address address;

  public static void navigateToAddressEditor(AppCompatActivity startingActivity, Address address) {

    Intent intent = new Intent(startingActivity, AddressEditorActivity.class);
    Bundle bundle = new Bundle();
    bundle.putSerializable(ADDRESS, address);

    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_address_editor_layout);
    ButterKnife.bind(AddressEditorActivity.this);

    AddressEditorActivity.this.initView();
  }

  private void initView() {

    AddressEditorActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("编辑地址");

    /*bind value*/
    address = (Address) getIntent().getSerializableExtra(ADDRESS);
    usernameEt.setHint(address.getUserName());
    mobileEt.setHint(address.getMobile());
    zipcodeEt.setHint(address.getZipcode());

    provinceTv.setText(address.getProvince());
    cityTv.setText(address.getCity());
    addressEt.setHint(address.getAddress());
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    TextView textView = (TextView) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    textView.setText(getText(R.string.action_done));

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AddressEditorActivity.this.requestEditorAddress();
      }
    });
    return true;
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      AddressEditorActivity.this.finish();
      overridePendingTransition(0, 0);
    }
    return false;
  }

  /**
   * 修改地址
   */
  private void requestEditorAddress() {

    if (!AddressEditorActivity.this.checkParams()) return;

    /*Action=AddUserByAddress
        &consignee=Eric //收货人
        &mobile=18622816323 //手机号码
        &zipcode=1000111  //邮政编码
        &province=%E5%8C%97%E4%BA%AC //省市
        &city=%E5%8C%97%E4%BA%AC //城市
        &address= //详细地址
    &address_id= //收货地址ID*/

    HashMap<String, String> params = new HashMap<>(8);
    params.put("Action", "AddUserByAddress");
    params.put("consignee", usernameEt.getText().toString());
    params.put("mobile", mobileEt.getText().toString());
    params.put("zipcode", zipcodeEt.getText().toString());
    params.put("province", URLEncoder.encode(provinceTv.getText().toString()));
    params.put("city", URLEncoder.encode(cityTv.getText().toString()));
    params.put("address", addressEt.getText().toString());
    params.put("address_id", address.getAddressId());

    subscription =
        AddressRetrofit.getInstance().requestEditorAddress(params).doOnSubscribe(new Action0() {
          @Override public void call() {

            /*加载数据，显示进度条*/
            progressDialog = DialogManager.
                getInstance().showProgressDialog(AddressEditorActivity.this, null, cancelListener);
          }
        }).subscribe(new Subscriber<Address>() {
          @Override public void onCompleted() {

            /*修改成功，隐藏进度条*/
            if (progressDialog != null && progressDialog.isShowing()) {

              progressDialog.dismiss();
              AddressEditorActivity.this.finish();
              overridePendingTransition(0, 0);
            }
          }

          @Override public void onError(Throwable error) {

            if (subscription.isUnsubscribed()) return;

            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
            AddressEditorActivity.this.showSnackBar("网络连接超时，请重试");

            if (error instanceof TimeoutException) {
              Log.e(TAG, "TimeoutException");
            } else if (error instanceof RetrofitError) {
              Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
            } else if (error instanceof HttpException) {
              Log.e(TAG, "HttpException");
            } else {
              Log.e(TAG, error.getMessage());
              error.printStackTrace();
              throw new RuntimeException("See inner exception");
            }
          }

          @Override public void onNext(Address address) {

            if (subscription.isUnsubscribed()) return;

            EventBusInstance.getDefault().post((EditorAddressEvent) address);
          }
        });
  }

  /**
   * 校验参数
   */
  private boolean checkParams() {

    if (TextUtils.isEmpty(usernameEt.getText().toString())) {
      AddressEditorActivity.this.showSnackBar("请填写收货人");
      return false;
    }
    if (TextUtils.isEmpty(mobileEt.getText().toString())) {
      AddressEditorActivity.this.showSnackBar("请填写手机号码");
      return false;
    }
    if (TextUtils.isEmpty(zipcodeEt.getText().toString())) {
      AddressEditorActivity.this.showSnackBar("请填写邮编");
      return false;
    }
    if (TextUtils.isEmpty(provinceTv.getText().toString())) {
      AddressEditorActivity.this.showSnackBar("请选择省份");
      return false;
    }
    if (TextUtils.isEmpty(cityTv.getText().toString())) {
      AddressEditorActivity.this.showSnackBar("请选择城市");
      return false;
    }
    if (TextUtils.isEmpty(addressEt.getText().toString())) {
      AddressEditorActivity.this.showSnackBar("请填写地址");
      return false;
    }

    return true;
  }

  private void showSnackBar(String text) {
    Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT).setAction("确定", null).show();
  }

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
      AddressEditorActivity.this.showSnackBar("修改操作被终止");
    }
  };

  @Override protected void onDestroy() {
    super.onDestroy();

    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
    ButterKnife.unbind(AddressEditorActivity.this);
  }
}
