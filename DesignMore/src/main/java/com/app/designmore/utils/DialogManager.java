package com.app.designmore.utils;

import android.content.Context;
import android.view.Gravity;
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
      final OnClickListener onClickListener) {

    return DialogManager.this.showSelectorDialog(context, gravity, layoutId, false, onClickListener,
        null, null, null);
  }

  public DialogPlus showSelectorDialog(Context context, int gravity, int layoutId,
      final OnClickListener onClickListener, final OnBackPressListener onBackPressListener) {

    return DialogManager.this.showSelectorDialog(context, gravity, layoutId, false, onClickListener,
        onBackPressListener, null, null);
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

  public DialogPlus showProgressDialog(Context context,
      final OnBackPressListener onBackPressListener, final OnCancelListener onCancelListener,
      final OnDismissListener onDismissListener) {

    DialogPlus dialog = DialogPlus.newDialog(context)
        .setContentHolder(new ViewHolder(R.layout.dialog_progressing_layout))
        .setGravity(Gravity.CENTER)
        .setInAnimation(R.anim.slide_in_bottom_super)
        .setOutAnimation(R.anim.slide_out_bottom_super)
        .setMargin(DensityUtil.dip2px(88), 0, DensityUtil.dip2px(88), 0)
        .setPadding(0, DensityUtil.dip2px(24), 0, DensityUtil.dip2px(24))
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
        .setCancelable(true)
        .create();
    dialog.show();

    return dialog;
  }
}
