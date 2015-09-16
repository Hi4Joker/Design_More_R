package com.app.designmore.activity.usercenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.event.AvatarRefreshEvent;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.CropImageView;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;
import com.jakewharton.rxbinding.view.RxView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/9/14.
 */
public class CameraFrontActivity extends BaseActivity implements CameraHostProvider {

  private static final String TAG = CameraFrontActivity.class.getCanonicalName();

  private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
  private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
  @Nullable @Bind(R.id.profile_camera_layout_root_view) RelativeLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.profile_camera_layout_camera_view) CameraView cameraView;
  @Nullable @Bind(R.id.profile_camera_layout_taken_view) CropImageView cropImageView;
  @Nullable @Bind(R.id.profile_camera_layout_shutter_view) View shutter;
  @Nullable @Bind(R.id.profile_camera_layout_camera_btn) FloatingActionButton floatingActionButton;

  private Button doneActionButton;
  private ImageButton switchActionButton;

  private State currentState = State.TAKE;
  private File photoFile;

  private Subscription subscription = Subscriptions.empty();
  private Subscription threadSubscription = Subscriptions.empty();
  private ProgressDialog progressDialog;

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {

      threadSubscription.unsubscribe();
      subscription.unsubscribe();
    }
  };

  public enum State {
    TAKE,//拍照状态
    DISPLAY//展示状态
  }

  public static void navigateToCamera(AppCompatActivity startingActivity) {
    Intent intent = new Intent(startingActivity, CameraFrontActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_profile_camera_layout);

    CameraFrontActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    CameraFrontActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    /*拍摄状态*/
    CameraFrontActivity.this.updateState(State.TAKE);

    RxView.touches(cameraView).forEach(new Action1<MotionEvent>() {
      @Override public void call(MotionEvent motionEvent) {
        cameraView.autoFocus();
      }
    });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);

    MenuItem doneMenuItem = menu.findItem(R.id.action_inbox_2);
    doneMenuItem.setActionView(R.layout.menu_inbox_tv_item);
    doneActionButton = (Button) doneMenuItem.getActionView().findViewById(R.id.action_inbox_btn);
    doneActionButton.setText("完成");
    this.doneActionButton.setEnabled(false);
    doneActionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (EventBusInstance.getDefault().hasSubscriberForEvent(AvatarRefreshEvent.class)) {
          CameraFrontActivity.this.saveBitmap();
        }
      }
    });

    MenuItem switchMenuItem = menu.findItem(R.id.action_inbox_1);
    switchMenuItem.setActionView(R.layout.menu_inbox_btn_item);
    switchActionButton =
        (ImageButton) switchMenuItem.getActionView().findViewById(R.id.action_inbox_btn);
    switchActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera_black));
    switchActionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        /*切换至后置摄像头*/
        CameraBackActivity.navigateToCamera(CameraFrontActivity.this, false);
        CameraFrontActivity.this.finish();
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  public void saveBitmap() {

    subscription = Observable.create(new Observable.OnSubscribe<File>() {
      @Override public void call(final Subscriber<? super File> subscriber) {

        threadSubscription = Schedulers.newThread().createWorker().schedule(new Action0() {
          @Override public void call() {
            FileOutputStream out = null;
            File file = new File(Environment.getExternalStorageDirectory().getPath(),
                "A_DM_profile_header" + Utils.dateFormat(System.currentTimeMillis()));
            if (file.exists()) {
              file.delete();
            }
            try {
              out = new FileOutputStream(file);
              cropImageView.getCroppedBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
              out.flush();
              out.close();
            } catch (IOException e) {
              subscriber.onError(e);
            } finally {
              try {
                if (out != null) {
                  out.close();
                }
              } catch (IOException e) {
                subscriber.onError(e);
              }
            }
            subscriber.onNext(file);
            subscriber.onCompleted();
          }
        });
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof IOException && integer < 1;
      }
    }).doOnTerminate(new Action0() {
      @Override public void call() {
        /*隐藏进度条*/
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
      }
    }).doOnSubscribe(new Action0() {
      @Override public void call() {
         /*加载数据，显示进度条*/
        if (progressDialog == null) {
          progressDialog = DialogManager.
              getInstance().showSimpleProgressDialog(CameraFrontActivity.this, cancelListener);
        } else {
          progressDialog.show();
        }
      }
    }).filter(new Func1<File, Boolean>() {
      @Override public Boolean call(File file) {
        return !subscription.isUnsubscribed();
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<File>() {
      @Override public void onCompleted() {
        EventBusInstance.getDefault()
            .post(new AvatarRefreshEvent(photoFile, cropImageView.getCroppedBitmap()));
        CameraFrontActivity.this.exitWhitAnim();
      }

      @Override public void onError(Throwable e) {
        e.printStackTrace();
        Toast.makeText(CameraFrontActivity.this, "保存失败，请重试", Toast.LENGTH_LONG).show();
      }

      @Override public void onNext(File file) {
        CameraFrontActivity.this.photoFile = file;
      }
    });
  }

  @Override public CameraHost getCameraHost() {
    return new MyCameraHost(CameraFrontActivity.this, true);
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
              CameraFrontActivity.this.showTakenPicture(bitmap);
            }
          });
        }
      });
    }

    @Override public void saveImage(PictureTransaction xact, byte[] image) {
      super.saveImage(xact, image);
      CameraFrontActivity.this.photoFile = this.getPhotoPath();
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

    this.cropImageView.setImageBitmap(bitmap);
    /*拍摄 -> 展示*/
    CameraFrontActivity.this.updateState(State.DISPLAY);
  }

  private void updateState(State state) {

    this.currentState = state;
    if (currentState == State.TAKE) {/*拍摄状态*/

      ViewCompat.animate(floatingActionButton)
          .translationY(0.0f)
          .setDuration(Constants.MILLISECONDS_300);

      this.floatingActionButton.setEnabled(true);
      if (switchActionButton != null) this.switchActionButton.setEnabled(true);
      if (doneActionButton != null) this.doneActionButton.setEnabled(false);
      this.cropImageView.setVisibility(View.GONE);
    } else if (currentState == State.DISPLAY) {/*拍摄完毕状态*/

      ViewCompat.animate(floatingActionButton)
          .translationY(DensityUtil.hideFromBottom(floatingActionButton))
          .setDuration(Constants.MILLISECONDS_300);

      this.floatingActionButton.setEnabled(false);
      this.switchActionButton.setEnabled(false);
      this.doneActionButton.setEnabled(true);
      this.cropImageView.setVisibility(View.VISIBLE);
    }
  }


 /* private void switchCamera() {
    if (cameraState == CameraState.BACK) {
      // TODO: 2015/9/15 切换至前置摄像头
      cameraState = CameraState.FRONT;
      doneActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera_front));
      cameraView.setHost(new MyCameraHost(CameraActivity.this, true));
      cameraView.restartPreview();
    } else {
      // TODO: 2015/9/15 切换至后置摄像头
      cameraState = CameraState.BACK;
      doneActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_camera_black));
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

  @Override public void exit() {

    if (currentState == State.DISPLAY) {
      this.updateState(State.TAKE);
    } else {
      CameraFrontActivity.this.exitWhitAnim();
    }
  }

  private void exitWhitAnim() {
    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(CameraFrontActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            CameraFrontActivity.this.finish();
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
