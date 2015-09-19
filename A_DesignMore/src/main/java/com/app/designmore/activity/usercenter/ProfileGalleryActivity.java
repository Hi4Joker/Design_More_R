package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.event.AvatarRefreshEvent;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.rxAndroid.SimpleObserver;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.CropImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/9/17.
 */
public class ProfileGalleryActivity extends BaseActivity {

  private static String TAG = ProfileGalleryActivity.class.getCanonicalName();
  private static final int RESULT_LOAD_IMAGE = 666;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.profile_gallery_layout_root_view) RelativeLayout rootView;
  @Nullable @Bind(R.id.profile_gallery_layout_taken_view) CropImageView cropImageView;

  private ProgressDialog progressDialog;
  private Subscription threadSubscription = Subscriptions.empty();
  private Subscription subscription = Subscriptions.empty();

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {

      threadSubscription.unsubscribe();
      subscription.unsubscribe();
    }
  };

  public static void navigateToGallery(AppCompatActivity startingActivity) {
    Intent intent = new Intent(startingActivity, ProfileGalleryActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_profile_gallery_layout);

    ProfileGalleryActivity.this.initView(savedInstanceState);
    ProfileGalleryActivity.this.startGallery();
  }

  @Override public void initView(Bundle savedInstanceState) {
    ProfileGalleryActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
  }

  private void startGallery() {
    Intent intent = new Intent(Intent.ACTION_PICK,
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(intent, RESULT_LOAD_IMAGE);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
      Uri selectedImage = data.getData();
      String[] filePathColumn = { MediaStore.Images.Media.DATA };
      Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
      cursor.moveToFirst();
      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      String picturePath = cursor.getString(columnIndex);
      cursor.close();

      cropImageView.setVisibility(View.VISIBLE);
      cropImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
    } else {
      ProfileGalleryActivity.this.finish();
      overridePendingTransition(0, 0);
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem doneMenuItem = menu.findItem(R.id.action_inbox);
    doneMenuItem.setActionView(R.layout.menu_inbox_tv_item);
    Button actionBtn = (Button) doneMenuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionBtn.setText("完成");
    actionBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (EventBusInstance.getDefault().hasSubscriberForEvent(AvatarRefreshEvent.class)) {
          ProfileGalleryActivity.this.saveBitmap();
        }
      }
    });

    return true;
  }

  private void saveBitmap() {

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
    }).doOnSubscribe(new Action0() {
      @Override public void call() {
         /*加载数据，显示进度条*/
        if (progressDialog == null) {
          progressDialog = DialogManager.
              getInstance().showSimpleProgressDialog(ProfileGalleryActivity.this, cancelListener);
        } else {
          progressDialog.show();
        }
      }
    }).doOnTerminate(new Action0() {
      @Override public void call() {
        /*隐藏进度条*/
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
      }
    }).filter(new Func1<File, Boolean>() {
      @Override public Boolean call(File file) {
        return !threadSubscription.isUnsubscribed() && !subscription.isUnsubscribed();
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<File>() {

      @Override public void onCompleted() {
        ProfileGalleryActivity.this.exit();
      }

      @Override public void onError(Throwable e) {
        e.printStackTrace();
        Toast.makeText(ProfileGalleryActivity.this, "保存失败，请重试", Toast.LENGTH_LONG).show();
      }

      @Override public void onNext(File file) {
        EventBusInstance.getDefault()
            .post(new AvatarRefreshEvent(file, cropImageView.getCroppedBitmap()));
      }
    });
  }

  @Override public void exit() {
    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(ProfileGalleryActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            ProfileGalleryActivity.this.finish();
            overridePendingTransition(0, 0);
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    this.progressDialog = null;
    if (!threadSubscription.isUnsubscribed()) threadSubscription.unsubscribe();
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
