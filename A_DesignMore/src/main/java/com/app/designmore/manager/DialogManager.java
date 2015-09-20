package com.app.designmore.manager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.FashionActivity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.rxAndroid.schedulers.HandlerScheduler;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.dialog.CustomAccountDialog;
import com.app.designmore.view.dialog.CustomCameraDialog;
import com.app.designmore.view.dialog.CustomShareDialog;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by Joker on 2015/8/26.
 */
public class DialogManager {

  private static final String TAG = DialogManager.class.getCanonicalName();

  private DialogManager() {

  }

  private static class SingletonHolder {
    private static DialogManager instance = new DialogManager();
  }

  public static DialogManager getInstance() {
    return SingletonHolder.instance;
  }

  public ProgressDialog showSimpleProgressDialog(Context context,
      final DialogInterface.OnCancelListener onCancelListener) {
    return showCancelableProgressDialog(context, null, onCancelListener, true);
  }

  public ProgressDialog showCancelableProgressDialog(Context context,
      final DialogInterface.OnShowListener onShowListener,
      final DialogInterface.OnCancelListener onCancelListener, boolean cancelable) {

    ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setCancelable(cancelable);
    progressDialog.setCanceledOnTouchOutside(false);

    progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
      @Override public void onShow(DialogInterface dialog) {
        if (onShowListener != null) onShowListener.onShow(dialog);
      }
    });

    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override public void onCancel(DialogInterface dialog) {
        dialog.dismiss();
        if (onCancelListener != null) {
          onCancelListener.onCancel(dialog);
        }
      }
    });

    progressDialog.show();
    progressDialog.setContentView(R.layout.dialog_progressing_layout);

    return progressDialog;
  }

  public AlertDialog showGenderPickerDialog(Context context, String initGender,
      final DialogInterface.OnClickListener onClickListener) {

    final String[] arrayGender = new String[] { "男", "女" };
    int which = "男".endsWith(initGender) ? 0 : 1;

    AlertDialog genderDialog = new AlertDialog.Builder(context).setTitle("请选择性别")
        .setCancelable(false)
        .setInverseBackgroundForced(false)
        .setSingleChoiceItems(arrayGender, which, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            if (onClickListener != null) {
              onClickListener.onClick(dialog, which);
            }
          }
        })
        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .create();

    genderDialog.getWindow().setWindowAnimations(R.style.AnimCenter);
    genderDialog.show();

    return genderDialog;
  }

  public void showExitDialog(Context context,
      final DialogInterface.OnClickListener onClickListener) {

    AlertDialog exitDialog = new AlertDialog.Builder(context).setTitle("提示")
        .setMessage("确认退出吗？")
        .setCancelable(false)
        .setInverseBackgroundForced(false)
        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            if (onClickListener != null) {
              onClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            }
            EventBusInstance.getDefault().removeAllStickyEvents();
          }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .create();

    exitDialog.getWindow().setWindowAnimations(R.style.AnimCenter);
    exitDialog.show();
  }

  public void showNormalDialog(Context context, String content,
      final DialogInterface.OnClickListener onClickListener) {

    AlertDialog normalDialog = new AlertDialog.Builder(context).setTitle("提示")
        .setMessage(content)
        .setCancelable(false)
        .setInverseBackgroundForced(false)
        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            if (onClickListener != null) {
              onClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            }
          }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            if (onClickListener != null) {
              onClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
            }
          }
        })
        .create();

    normalDialog.getWindow().setWindowAnimations(R.style.AnimCenter);
    normalDialog.show();
  }

  public void showConfirmDialog(Context context, String content) {

    AlertDialog confirmDialog = new AlertDialog.Builder(context).setTitle("提示")
        .setMessage(content)
        .setCancelable(false)
        .setInverseBackgroundForced(false)
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .create();

    confirmDialog.getWindow().setWindowAnimations(R.style.AnimCenter);
    confirmDialog.show();
  }

  public CustomCameraDialog showCameraDialog(Activity activity,
      CustomCameraDialog.Callback callback) {

    return new CustomCameraDialog(activity, callback);
  }

  public CustomShareDialog showShareDialog(Activity activity, CustomShareDialog.Callback callback) {
    return new CustomShareDialog(activity, callback);
  }

  public CustomAccountDialog showDetailDialog(Activity activity, Map map,
      CustomAccountDialog.Callback callback) {
    return new CustomAccountDialog(activity, map, callback);
  }

  public ViewGroup showNoMoreDialog(BaseActivity activity, int gravity, String content) {

    final LinearLayout parent =
        (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.custom_toast_layout, null);
    TextView toast = (TextView) parent.findViewById(R.id.custom_toast_content_tv);
    if (!TextUtils.isEmpty(content)) toast.setText(content);

    final WindowManager windowManager =
        (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);

    final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

    layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
    layoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    layoutParams.format = PixelFormat.TRANSLUCENT;

    layoutParams.width = DensityUtil.getScreenWidth(activity);
    layoutParams.height = DensityUtil.dip2px(64.0f);
    layoutParams.gravity = gravity;
    layoutParams.windowAnimations = R.style.AnimTop;

    windowManager.addView(parent, layoutParams);

    HandlerScheduler.from(new Handler(Looper.getMainLooper()))
        .createWorker()
        .schedule(new Action0() {
          @Override public void call() {

            if (parent != null && parent.getParent() != null) {

              /*rootView ViewRootImpl*/
              windowManager.removeView(parent);
            }
          }
        }, Constants.MILLISECONDS_2000, TimeUnit.MILLISECONDS);

    return parent;
  }
}
