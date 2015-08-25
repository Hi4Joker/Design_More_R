package com.joker.app;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.joker.app.rx.LoginService;
import com.joker.app.rx.error.PassWordError;
import com.joker.app.rx.ResultEntity;
import com.joker.app.rx.SchedulersCompat;
import com.joker.app.rx.Solution;
import com.joker.app.rx.error.UserNameError;
import com.joker.app.view.RevealBackgroundView;
import com.joker.app.rx.UserInfoEntity;
import com.joker.app.view.circularProgress.CircularProgressButton;
import com.joker.app.view.transformation.BlurTransformation;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.squareup.picasso.Picasso;
import java.net.HttpRetryException;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class LaunchActivity extends BaseActivity {

  private static final String TAG = LaunchActivity.class.getSimpleName();
  private static final int PROGRESSING_DURATION = Integer.MAX_VALUE;
  private static final int ERROR_TO_IDLE_DURATION = 2 * 1000;
  private static final int NAVIGATE_TO_DETAIL = 0;
  private static final int ERROR_TO_IDLE = 1;
  private static final int PROGRESS_TO_ERROR = 2;
  private static final int ON_LOGIN_CLICK = 4;

  @Nullable @Bind(R.id.main_background) ImageView backgroundImageView;
  @Nullable @Bind(R.id.main_username_text_input_layout) TextInputLayout usernameInput;
  @Nullable @Bind(R.id.main_password_text_input_layout) TextInputLayout passwordInput;
  @Nullable @Bind(R.id.main_login_btn) CircularProgressButton circularProgressButton;

  @Nullable @Bind(R.id.main_username_root) LinearLayout usernameRoot;
  @Nullable @Bind(R.id.main_password_root) LinearLayout passwordRoot;

  private LoginService loginService;
  private final CompositeSubscription compositeSubscription = new CompositeSubscription();

  /*private final BooleanSubscription booleanSubscription = BooleanSubscription.create(new Action0() {
    @Override public void call() {

    }
  });*/

  private int statusBarHeight = 0;
  private int width = 0;
  private int height = 0;

  @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);

      switch (msg.what) {

        case NAVIGATE_TO_DETAIL:
          int[] startingLocation = new int[3];
          LaunchActivity.this.getLocation(startingLocation);

          DetailActivity.startFromLocation(LaunchActivity.this, startingLocation);
          overridePendingTransition(0, 0);
          break;

        case ON_LOGIN_CLICK:

          circularProgressButton.setProgress(1);
          break;
        case ERROR_TO_IDLE:

          circularProgressButton.setProgress(0);
          circularProgressButton.setClickable(true);
          break;

        case PROGRESS_TO_ERROR:

          usernameInput.setErrorEnabled(false);
          passwordInput.setErrorEnabled(false);

          circularProgressButton.setProgress(-1);
          handler.sendEmptyMessageDelayed(ERROR_TO_IDLE, ERROR_TO_IDLE_DURATION);
          break;
      }
    }
  };

  private void getLocation(int[] startingLocation) {

    // 得到相对于整个屏幕的区域坐标（左上角坐标——右下角坐标）
    Rect viewRect = new Rect();
    circularProgressButton.getGlobalVisibleRect(viewRect);

    startingLocation[0] = (viewRect.left + viewRect.right) / 2;
    startingLocation[1] = (viewRect.bottom + viewRect.top) / 2 - statusBarHeight;

    Rect bounds = circularProgressButton.getDrawableBounds();
    if (bounds != null) {

      startingLocation[2] = (viewRect.bottom - viewRect.top) / 2;
    } else {
      startingLocation[2] = (viewRect.bottom - viewRect.top) / 2;
    }
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    LaunchActivity.this.initView();
  }

  private void initView() {

    width = Resources.getSystem().getDisplayMetrics().widthPixels; // 屏幕宽度（像素）
    height = Resources.getSystem().getDisplayMetrics().heightPixels; // 屏幕高度（像素）

    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      statusBarHeight = getResources().getDimensionPixelSize(resourceId);
    }

    usernameInput.setHint(getResources().getString(R.string.username_hint));
    passwordInput.setHint(getResources().getString(R.string.password_hint));

    /*使用不确定的Progress*/
    circularProgressButton.setIndeterminateProgressMode(true);

    usernameRoot.getViewTreeObserver()
        .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            usernameRoot.getViewTreeObserver().removeOnPreDrawListener(this);
            LaunchActivity.this.startLaunchAnim();
            return true;
          }
        });
  }

  /**
   * 执行进入动画
   */
  private void startLaunchAnim() {

    ViewCompat.setPivotY(usernameRoot, (usernameRoot.getTop() + usernameRoot.getBottom()) / 2);
    ViewCompat.setTranslationY(usernameRoot, usernameRoot.getHeight());
    ViewCompat.setAlpha(usernameRoot, 0.0f);
    ViewCompat.animate(usernameRoot)
        .translationY(1.0f)
        .alpha(1.0f)
        .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
        .setStartDelay(getResources().getInteger(android.R.integer.config_shortAnimTime))
        .setInterpolator(new LinearInterpolator());

    ViewCompat.setPivotY(passwordRoot, (passwordRoot.getTop() + passwordRoot.getBottom()) / 2);
    ViewCompat.setTranslationY(passwordRoot, passwordRoot.getHeight());
    ViewCompat.setAlpha(passwordRoot, 0.0f);
    ViewCompat.animate(passwordRoot)
        .translationY(1.0f)
        .alpha(1.0f)
        .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
        .setStartDelay(getResources().getInteger(android.R.integer.config_shortAnimTime))
        .setInterpolator(new LinearInterpolator());

    ViewCompat.setAlpha(circularProgressButton, 0.0f);
    ViewCompat.animate(circularProgressButton)
        .alpha(1.0f)
        .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
        .setStartDelay(getResources().getInteger(android.R.integer.config_longAnimTime))
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationStart(View view) {

            /*Blur background*/
            Picasso.with(LaunchActivity.this)
                .load(R.drawable.main_background)
                .noPlaceholder()
                .noFade()
                .resize(width, height - statusBarHeight)
                .centerCrop()
                .transform(new BlurTransformation(LaunchActivity.this))
                .into(backgroundImageView);
          }
        });
  }

  /**
   * 定义Subscribe
   */
  private void defineSubscribe(Observable<ResultEntity> resultObservable) {

    compositeSubscription.clear();
    compositeSubscription.add(
        resultObservable.compose(SchedulersCompat.<ResultEntity>applySchedulers())

            .subscribe(new Action1<ResultEntity>() {
              @Override public void call(ResultEntity resultEntity) {

                if (resultEntity.code == 1) {
                  circularProgressButton.setFrameToFinish(true);
                  LaunchActivity.this.navigateTo();
                } else {
                  handler.sendEmptyMessageDelayed(PROGRESS_TO_ERROR, ERROR_TO_IDLE_DURATION / 2);
                }
              }
            }, new Action1<Throwable>() {
              @Override public void call(final Throwable error) {

                if (error instanceof UserNameError) {
                  Log.e(TAG, error.getMessage());
                  circularProgressButton.setErrorText(error.getMessage());

                  usernameInput.setErrorEnabled(true);
                  usernameInput.setError(error.getMessage());
                } else if (error instanceof PassWordError) {
                  Log.e(TAG, error.getMessage());
                  circularProgressButton.setErrorText(error.getMessage());

                  usernameInput.setErrorEnabled(true);
                  usernameInput.setError(error.getMessage());
                } else if (error instanceof TimeoutException) {
                  Log.e(TAG, "登陆失败，请重试");
                  circularProgressButton.setErrorText(error.getMessage());
                } else if (error instanceof RetrofitError || error instanceof HttpRetryException) {
                  Log.e(TAG, "请检查您网络");
                  circularProgressButton.setErrorText(error.getMessage());
                } else {
                  Log.e(TAG, error.getMessage());
                  error.printStackTrace();
                  throw new RuntimeException("什么鬼");
                }

                //circularProgressButton.setFrameToFinish(true);
                //LaunchActivity.this.navigateTo();
                handler.sendEmptyMessageDelayed(PROGRESS_TO_ERROR, ERROR_TO_IDLE_DURATION / 2);
              }
            }));
  }

  @OnClick(R.id.main_login_btn) public void onClick() {

    handler.sendEmptyMessage(ON_LOGIN_CLICK);

    final UserInfoEntity userInfoEntity = new UserInfoEntity();
    final String userName = usernameInput.getEditText().getText().toString();
    final String passWord = passwordInput.getEditText().getText().toString();

    Observable<ResultEntity> resultObservable =
        Solution.checkLoginStream(userInfoEntity.valueObservable(userName, passWord))
            .flatMap(new Func1<UserInfoEntity, Observable<? extends ResultEntity>>() {
              @Override
              public Observable<? extends ResultEntity> call(UserInfoEntity userInfoEntity) {
                if (loginService == null) {
                  loginService = new LoginService();
                }
                return loginService.login(userInfoEntity);
              }
            });

    LaunchActivity.this.defineSubscribe(resultObservable);
  }

  /**
   * navigateTo detail 界面
   */
  private void navigateTo() {

    LaunchActivity.this.handler.sendEmptyMessage(NAVIGATE_TO_DETAIL);

    /*设置这里设置的一些动画属性动画效果，点击登录的缩放并且透明效果*/
    ObjectAnimator scaleXAnim =
        ObjectAnimator.ofFloat(circularProgressButton, "ScaleX", 1.0f, 0.9f);
    ObjectAnimator scaleYAnim =
        ObjectAnimator.ofFloat(circularProgressButton, "ScaleY", 1.0f, 0.9f);
    ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(circularProgressButton, "Alpha", 1.0f, 0.6f);
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.play(scaleXAnim).with(scaleYAnim).with(alphaAnim);
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        circularProgressButton.setVisibility(View.GONE);
      }
    });
    animatorSet.setInterpolator(new AccelerateInterpolator());
    animatorSet.setDuration(RevealBackgroundView.FILL_TIME / 5);
    animatorSet.start();
  }

  @Override public void onBackPressed() {
    super.onBackPressed();

    if (compositeSubscription.isUnsubscribed()) {
      compositeSubscription.clear();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    handler.removeCallbacksAndMessages(null);
  }
}
