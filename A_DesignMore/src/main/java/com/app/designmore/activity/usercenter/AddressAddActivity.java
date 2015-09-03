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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.event.RefreshAddressEvent;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.retrofit.AddressRetrofit;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.retrofit.request.address.AddAddressRequest;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.Utils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.net.URLEncoder;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/25.
 */
public class AddressAddActivity extends RxAppCompatActivity {

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

  private SupportAnimator revealAnimator;
  private Subscription subscription = Subscriptions.empty();
  private ProgressDialog progressDialog;

  public static void navigateToAddressEditor(AppCompatActivity startingActivity) {

    Intent intent = new Intent(startingActivity, AddressAddActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_address_add_layout);
    ButterKnife.bind(AddressAddActivity.this);

    AddressAddActivity.this.initView(savedInstanceState);
  }

  private void initView(Bundle savedInstanceState) {

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
    revealAnimator.setDuration(Constants.REVEAL_DURATION);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.start();
  }

  private void startExitAnim() {

    if (revealAnimator != null && !revealAnimator.isRunning()) {
      revealAnimator = revealAnimator.reverse();
      revealAnimator.setDuration(Constants.REVEAL_DURATION);
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

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    TextView textView = (TextView) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    textView.setText(getText(R.string.action_done));

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AddressAddActivity.this.requestAddAddress();
      }
    });
    return true;
  }

  private void requestAddAddress() {

    if (!AddressAddActivity.this.checkParams()) return;

    AddAddressRequest addAddressRequest = new AddAddressRequest("AddUserByAddress", //
        usernameEt.getText().toString(),//
        mobileEt.getText().toString(),//
        zipcodeEt.getText().toString(),//
        URLEncoder.encode(provinceTv.getText().toString()),//
        URLEncoder.encode(cityTv.getText().toString()),//
        addressEt.getText().toString()//
    );

    subscription = AddressRetrofit.getInstance()
        .requestAddAddress(addAddressRequest)
        .doOnSubscribe(new Action0() {
          @Override public void call() {

            /*加载数据，显示进度条*/
            progressDialog = DialogManager.
                getInstance().showProgressDialog(AddressAddActivity.this, null, cancelListener);
          }
        })
        .doOnTerminate(new Action0() {
          @Override public void call() {

            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
          }
        })
        .filter(new Func1<RefreshAddressEvent, Boolean>() {
          @Override public Boolean call(RefreshAddressEvent refreshAddressEvent) {

            return !subscription.isUnsubscribed();
          }
        })
        .subscribe(new Subscriber<RefreshAddressEvent>() {
          @Override public void onCompleted() {

            /*增加成功，返回，刷新*/
            AddressAddActivity.this.startExitAnim();
          }

          @Override public void onError(Throwable error) {

            if (subscription.isUnsubscribed()) return;

            if (error instanceof TimeoutException) {
              AddressAddActivity.this.showSnackBar(
                  getResources().getString(R.string.timeout_title));
            } else if (error instanceof RetrofitError) {
              AddressAddActivity.this.showSnackBar("网络连接异常:" + ((RetrofitError) error).getKind());
            } else if (error instanceof WebServiceException) {
              AddressAddActivity.this.showSnackBar("很抱歉:" + error.getMessage());
            } else {
              Log.e(TAG, error.getMessage());
              error.printStackTrace();
              throw new RuntimeException("See inner exception");
            }
          }

          @Override public void onNext(RefreshAddressEvent refreshAddressEvent) {

            /*通过eventBus发送通知，刷新地址列表*/
            EventBusInstance.getDefault().post(refreshAddressEvent);
          }
        });
  }

  /**
   * 校验参数
   */
  private boolean checkParams() {

    if (TextUtils.isEmpty(usernameEt.getText().toString())) {
      AddressAddActivity.this.showSnackBar("请填写收货人");
      return false;
    }
    if (TextUtils.isEmpty(mobileEt.getText().toString())) {
      AddressAddActivity.this.showSnackBar("请填写手机号码");
      return false;
    }
    if (TextUtils.isEmpty(zipcodeEt.getText().toString())) {
      AddressAddActivity.this.showSnackBar("请填写邮编");
      return false;
    }
    if (TextUtils.isEmpty(provinceTv.getText().toString())) {
      AddressAddActivity.this.showSnackBar("请选择省份");
      return false;
    }
    if (TextUtils.isEmpty(cityTv.getText().toString())) {
      AddressAddActivity.this.showSnackBar("请选择城市");
      return false;
    }
    if (TextUtils.isEmpty(addressEt.getText().toString())) {
      AddressAddActivity.this.showSnackBar("请填写地址");
      return false;
    }

    return true;
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

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
      AddressAddActivity.this.showSnackBar("增加操作被终止");
    }
  };

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        AddressAddActivity.this.startExitAnim();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      AddressAddActivity.this.startExitAnim();
    }
    return false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
    ButterKnife.unbind(AddressAddActivity.this);
  }
}
