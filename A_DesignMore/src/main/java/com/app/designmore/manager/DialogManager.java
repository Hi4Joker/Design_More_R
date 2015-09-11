package com.app.designmore.manager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import com.app.designmore.R;
import com.app.designmore.view.dialogplus.DialogPlus;
import com.app.designmore.view.dialogplus.OnBackPressListener;
import com.app.designmore.view.dialogplus.OnCancelListener;
import com.app.designmore.view.dialogplus.OnClickListener;
import com.app.designmore.view.dialogplus.OnDismissListener;
import com.app.designmore.view.dialogplus.ViewHolder;

/**
 * Created by Joker on 2015/8/26.
 */
public class DialogManager {

  private DialogManager() {

  }

  private static class SingletonHolder {
    private static DialogManager instance = new DialogManager();
  }

  public static DialogManager getInstance() {
    return SingletonHolder.instance;
  }

  public DialogPlus showSelectorDialog(Context context, int gravity, int layoutId,
      boolean cancelable, final OnClickListener onClickListener,
      final OnBackPressListener onBackPressListener, final OnCancelListener onCancelListener,
      final OnDismissListener onDismissListener) {

    DialogPlus dialog = DialogPlus.newDialog(context)
        .setContentHolder(new ViewHolder(layoutId))
        .setGravity(gravity)
        .setOnClickListener(new OnClickListener() {
          @Override public void onClick(DialogPlus dialog, View view) {
            if (onClickListener != null) onClickListener.onClick(dialog, view);
          }
        })
        .setOnBackPressListener(new OnBackPressListener() {
          @Override public void onBackPressed(DialogPlus dialogPlus) {
            if (onBackPressListener != null) onBackPressListener.onBackPressed(dialogPlus);
          }
        })
        .setOnCancelListener(new OnCancelListener() {
          @Override public void onCancel(DialogPlus dialog) {
            if (onCancelListener != null) onCancelListener.onCancel(dialog);
          }
        })
        .setOnDismissListener(new OnDismissListener() {
          @Override public void onDismiss(DialogPlus dialog) {
            if (onDismissListener != null) onDismissListener.onDismiss(dialog);
          }
        })
        .setCancelable(cancelable)
        .create();
    dialog.show();

    return dialog;
  }

  public ProgressDialog showSimpleProgressDialog(Context context,
      final DialogInterface.OnCancelListener onCancelListener) {
    return showCancelableProgressDialog(context, null, onCancelListener, true);
  }

  public ProgressDialog showProgressDialog(Context context,
      final DialogInterface.OnShowListener onShowListener,
      final DialogInterface.OnCancelListener onCancelListener) {
    return showCancelableProgressDialog(context, onShowListener, onCancelListener, true);
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

  public void showGenderPickerDialog(Context context, String initGender,
      final DialogInterface.OnClickListener onClickListener) {

    final String[] arrayGender = new String[] { "男", "女" };

    int which = "男".endsWith(initGender) ? 0 : 1;

    new AlertDialog.Builder(context).setTitle("请选择性别")
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
        .create()
        .show();
  }

  public void showExitDialog(Context context,
      final DialogInterface.OnClickListener onClickListener) {

    new AlertDialog.Builder(context).setTitle("提示")
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
        .create()
        .show();
  }

  public void showNormalDialog(Context context, String content,
      final DialogInterface.OnClickListener onClickListener) {

    new AlertDialog.Builder(context).setTitle("提示")
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
        .create()
        .show();
  }

  public void showConfirmDialog(Context context, String content) {

    new AlertDialog.Builder(context).setTitle("提示")
        .setMessage(content)
        .setCancelable(false)
        .setInverseBackgroundForced(false)
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
        .create()
        .show();
  }
}
