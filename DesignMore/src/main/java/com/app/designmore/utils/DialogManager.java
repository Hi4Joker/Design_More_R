package com.app.designmore.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import com.app.designmore.view.dialogplus.DialogPlus;
import com.app.designmore.view.dialogplus.OnCancelListener;
import com.app.designmore.view.dialogplus.OnClickListener;
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

  public void showPickSexDialog(Context context, int layoutId,
      final OnClickListener onClickListener) {

    DialogPlus dialog = DialogPlus.newDialog(context)
        .setContentHolder(new ViewHolder(layoutId))
        .setGravity(Gravity.BOTTOM)
        .setOnClickListener(new OnClickListener() {
          @Override public void onClick(DialogPlus dialog, View view) {
            if (onClickListener != null) onClickListener.onClick(dialog, view);
          }
        })
        .setCancelable(true)
        .create();
    dialog.show();
  }

  public void showPickBirthdayDialog(Context context, int layoutId,
      final OnClickListener onClickListener) {

    DialogPlus dialog = DialogPlus.newDialog(context)
        .setContentHolder(new ViewHolder(layoutId))
        .setGravity(Gravity.CENTER)
        .setOnClickListener(new OnClickListener() {
          @Override public void onClick(DialogPlus dialog, View view) {
            if (onClickListener != null) onClickListener.onClick(dialog, view);
          }
        })
        .setCancelable(false)
        .create();
    dialog.show();
  }
}
