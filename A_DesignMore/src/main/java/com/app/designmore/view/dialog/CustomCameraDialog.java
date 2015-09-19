package com.app.designmore.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.utils.DensityUtil;

/**
 * Created by Joker on 2015/9/14.
 */
public class CustomCameraDialog extends Dialog {

  private Callback callback;

  public CustomCameraDialog(Activity activity, Callback callback) {
    super(activity);

    getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    getWindow().setGravity(Gravity.CENTER);
    getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    getWindow().setWindowAnimations(R.style.AnimCenter);
    View rootView = getLayoutInflater().inflate(R.layout.custom_camera_layout, null);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
        DensityUtil.getScreenWidth(activity) - DensityUtil.getStatusBarHeight(activity) * 2,
        ViewGroup.LayoutParams.MATCH_PARENT);
    super.setContentView(rootView, params);

    this.callback = callback;
    CustomCameraDialog.this.setCancelable(true);
    CustomCameraDialog.this.setCanceledOnTouchOutside(false);
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    ButterKnife.bind(CustomCameraDialog.this);
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    ButterKnife.unbind(CustomCameraDialog.this);
  }

  @Nullable @OnClick(R.id.camera_layout_camera_btn) void onCameraClick() {
    if (callback != null) callback.onCameraClick();
    CustomCameraDialog.this.dismiss();
  }

  @Nullable @OnClick(R.id.camera_layout_photo_btn) void onPhotoClick() {
    if (callback != null) callback.onPhotoClick();
    CustomCameraDialog.this.dismiss();
  }

  public interface Callback {

    void onCameraClick();

    void onPhotoClick();
  }
}
