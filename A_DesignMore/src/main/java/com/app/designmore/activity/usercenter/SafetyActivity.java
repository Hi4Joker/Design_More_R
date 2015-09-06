package com.app.designmore.activity.usercenter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DialogManager;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

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
  @Nullable @Bind(R.id.safety_layout_confim_password_et) EditText confimPasswordEt;

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
    toolbarTitleTv.setText(getText(R.string.action_submit));

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
        .setDuration(Constants.ANIMATION_DURATION / 2)
        .setInterpolator(new AccelerateInterpolator());
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    TextView textView = (TextView) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    textView.setText(getText(R.string.action_submit));

  /*  Observable.range(0, 60, Schedulers.computation()).first().filter(new Func1<Integer, Boolean>() {
      @Override public Boolean call(Integer integer) {
        return null;
      }
    }).timeout(new Func1<Integer, Observable<Integer>>() {
      @Override public Observable<Integer> call(Integer integer) {

        return null;
      }
    }, Observable.create(new Observable.OnSubscribe<Integer>() {
      @Override public void call(Subscriber<? super Integer> subscriber) {

      }
    })).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
      @Override public void call(Integer integer) {
        Log.e(TAG, "call:" + integer);
      }
    });*/

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        DialogManager.getInstance()
            .showProgressDialog(SafetyActivity.this, new DialogInterface.OnShowListener() {
              @Override public void onShow(DialogInterface dialog) {
                Log.e(TAG, "onShow");
              }
            }, new DialogInterface.OnCancelListener() {
              @Override public void onCancel(DialogInterface dialog) {
                Log.e(TAG, "onCancel");
              }
            });
      }
    });

    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case android.R.id.home:
        SafetyActivity.this.startExitAnim();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      SafetyActivity.this.startExitAnim();
    }
    return false;
  }

  private void startExitAnim() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(SafetyActivity.this))
        .setDuration(Constants.ANIMATION_DURATION)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            SafetyActivity.super.onBackPressed();
            overridePendingTransition(0, 0);
          }
        });
  }
}
