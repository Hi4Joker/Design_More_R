package com.app.designmore.activity.usercenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.event.AvatarRefreshEvent;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;
import com.jakewharton.rxbinding.view.RxView;
import java.io.File;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Joker on 2015/9/14.
 */
public class CameraActivity extends BaseActivity implements CameraHostProvider {

  private static final String TAG = CameraActivity.class.getCanonicalName();

  private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
  private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
  @Nullable @Bind(R.id.profile_camera_layout_root_view) RelativeLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.profile_camera_layout_taken_view) ImageView photo;
  @Nullable @Bind(R.id.profile_camera_layout_camera_view) CameraView cameraView;
  @Nullable @Bind(R.id.profile_camera_layout_shutter_view) View shutter;
  @Nullable @Bind(R.id.profile_camera_layout_camera_btn) FloatingActionButton floatingActionButton;

  private Button actionButton;

  private CameraState cameraState = CameraState.BACK;
  private State currentState = State.TAKE;
  private File photoFile;

  public enum CameraState {
    BACK,
    FRONT
  }

  public enum State {
    TAKE,//拍照状态
    DISPLAY//展示状态
  }

  public static void navigateToCamera(AppCompatActivity startingActivity) {
    Intent intent = new Intent(startingActivity, CameraActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_profile_camera_layout);

    CameraActivity.this.initView(savedInstanceState);

    RxView.touches(cameraView).forEach(new Action1<MotionEvent>() {
      @Override public void call(MotionEvent motionEvent) {
        cameraView.autoFocus();
      }
    });
  }

  @Override public void initView(Bundle savedInstanceState) {

    CameraActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    /*拍摄状态*/
    CameraActivity.this.updateState(State.TAKE);

    if (savedInstanceState == null) {
      toolbar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          toolbar.getViewTreeObserver().removeOnPreDrawListener(this);
          CameraActivity.this.startEnterAnim();
          return true;
        }
      });
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionButton.setText("完成");
    this.actionButton.setEnabled(false);
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (EventBusInstance.getDefault().hasSubscriberForEvent(AvatarRefreshEvent.class)) {
          EventBusInstance.getDefault().post(new AvatarRefreshEvent(photoFile));

          CameraActivity.this.exitWhitAnim();
        }
      }
    });

    return true;
  }

  @Override public CameraHost getCameraHost() {
    return new MyCameraHost(CameraActivity.this, false);
  }

  class MyCameraHost extends SimpleCameraHost implements Camera.FaceDetectionListener {

    private Camera.Size previewSize;
    private boolean isFFC = false;

    public MyCameraHost(Context context, boolean isFFC) {
      super(context);
      this.isFFC = isFFC;
    }

    /*useFullBleedPreview()的意思和ImageView中的缩放类型centerCrop差不多*/
    @Override public boolean useFullBleedPreview() {
      return true;
    }

    @Override
    public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
      return previewSize;
    }

    @Override public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
      Camera.Parameters parameters1 = super.adjustPreviewParameters(parameters);
      previewSize = parameters1.getPreviewSize();
      return parameters1;
    }

    @Override public void saveImage(PictureTransaction xact, final Bitmap bitmap) {
      runOnUiThread(new Runnable() {
        @Override public void run() {
          AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override public void call() {
               /*显示照片*/
              CameraActivity.this.showTakenPicture(bitmap);
            }
          });
        }
      });
    }

    @Override public void saveImage(PictureTransaction xact, byte[] image) {
      super.saveImage(xact, image);
      CameraActivity.this.photoFile = this.getPhotoPath();
    }

    @Override public boolean useFrontFacingCamera() {
      return isFFC;
    }

    /*只允许照一次*/
    @Override public boolean useSingleShotMode() {
      return false;
    }

    @Override public void onFaceDetection(Camera.Face[] faces, Camera camera) {

    }
  }

  private void showTakenPicture(Bitmap bitmap) {

    this.photo.setImageBitmap(bitmap);
    /*拍摄 -> 展示*/
    CameraActivity.this.updateState(State.DISPLAY);
  }

  private void updateState(State state) {

    this.currentState = state;
    if (currentState == State.TAKE) {/*拍摄状态*/

      ViewCompat.animate(floatingActionButton)
          .translationY(0.0f)
          .setDuration(Constants.MILLISECONDS_300);

      this.floatingActionButton.setEnabled(true);
      if (actionButton != null) this.actionButton.setEnabled(false);
      this.photo.setVisibility(View.GONE);
    } else if (currentState == State.DISPLAY) {/*拍摄完毕状态*/

      ViewCompat.animate(floatingActionButton)
          .translationY(DensityUtil.hideFromBottom(floatingActionButton))
          .setDuration(Constants.MILLISECONDS_300);

      this.floatingActionButton.setEnabled(false);
      this.actionButton.setEnabled(true);
      this.photo.setVisibility(View.VISIBLE);
    }
  }


 /* private void switchCamera() {
    if (cameraState == CameraState.BACK) {
      // TODO: 2015/9/15 切换至前置摄像头
      cameraState = CameraState.FRONT;
      actionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera_front));
      cameraView.setHost(new MyCameraHost(CameraActivity.this, true));
      cameraView.restartPreview();
    } else {
      // TODO: 2015/9/15 切换至后置摄像头
      cameraState = CameraState.BACK;
      actionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera_black));
      cameraView.setHost(new MyCameraHost(CameraActivity.this, false));
      cameraView.restartPreview();
    }
  }*/

  @Nullable @OnClick(R.id.profile_camera_layout_camera_btn) void onFABClick() {
    cameraView.takePicture(true, true);
    this.animateShutter();
  }

  private void animateShutter() {

    shutter.setVisibility(View.VISIBLE);
    shutter.setAlpha(0.0f);

    ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(shutter, "alpha", 0f, 0.8f);
    alphaInAnim.setDuration(Constants.MILLISECONDS_100);
    alphaInAnim.setStartDelay(Constants.MILLISECONDS_100);
    alphaInAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

    ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(shutter, "alpha", 0.8f, 0f);
    alphaOutAnim.setDuration(Constants.MILLISECONDS_200);
    alphaOutAnim.setInterpolator(DECELERATE_INTERPOLATOR);

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playSequentially(alphaInAnim, alphaOutAnim);
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        shutter.setVisibility(View.GONE);
      }
    });
    animatorSet.start();
  }

  private void startEnterAnim() {
    ViewCompat.setTranslationY(rootView, rootView.getHeight());
    ViewCompat.animate(rootView)
        .translationY(0.0f)
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator());
  }

  @Override public void exit() {

    if (currentState == State.DISPLAY) {
      this.updateState(State.TAKE);
    } else {
      CameraActivity.this.exitWhitAnim();
    }
  }

  private void exitWhitAnim() {
    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(CameraActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            CameraActivity.this.finish();
            overridePendingTransition(0, 0);
          }
        });
  }

  @Override protected void onResume() {
    super.onResume();
    this.cameraView.onResume();
  }

  @Override protected void onPause() {
    super.onPause();
    this.cameraView.onPause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
