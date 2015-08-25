package com.joker.app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.joker.app.R;
import com.joker.app.utils.DensityUtil;
import com.joker.app.view.transformation.CBorderTransformation;
import com.joker.app.view.transformation.CircleTransformation;
import com.squareup.picasso.Picasso;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joker on 2015/7/17.
 */
public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

  private static final String TAG = DetailAdapter.class.getSimpleName();
  private WeakReference<Context> weakReference;
  private List<DetailEntity> items = new ArrayList<>();
  private int lastAnimatedPosition = -1;
  private boolean animationsLocked = false;
  private int width;
  private int topMargin;
  private Callback callback;

  public DetailAdapter(Context context, List<DetailEntity> items) {
    this.weakReference = new WeakReference<>(context);
    this.items = items;

    // 屏幕宽度（像素）
    width = Resources.getSystem().getDisplayMetrics().widthPixels;
    /*topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16.0f,
        Resources.getSystem().getDisplayMetrics());*/
  }

  @Override public DetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_item, parent, false));
  }

  @Override public void onBindViewHolder(DetailAdapter.ViewHolder holder, int position) {

    holder.avatar.setTag(position);
    holder.itemView.setTag(position);

    /*执行进入动画*/
    DetailAdapter.this.runEnterAnimation(holder, position);
    DetailAdapter.this.loadAvatar(holder.avatar, position);
  }

  private void loadAvatar(ImageView avatar, int position) {

    if ((Integer) (avatar.getTag()) != position) return;

    if (weakReference.get() == null) {
      avatar.setBackgroundResource(R.mipmap.ic_launcher);
    } else {
      Picasso.with(weakReference.get())
          .load(items.get(position).getAvatar())
          .noFade()
          .noPlaceholder()
          .fit()
          .centerCrop()
          .transform(new CBorderTransformation())
          .into(avatar);
    }
  }

  private void runEnterAnimation(final ViewHolder viewHolder, final int position) {

    if ((Integer) (viewHolder.itemView.getTag()) != position) return;

    /*if (position == 0) {
      RecyclerView.LayoutParams layoutParams =
          (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();
      layoutParams.topMargin = topMargin;
      Log.e(TAG, "topMargin:"
          + ((RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams()).topMargin);
    }*/

    ViewCompat.animate(viewHolder.itemView).cancel();

    if (animationsLocked) {
      viewHolder.description.setText(items.get(position).getDescription());
      return;
    }

    if ((Integer) viewHolder.itemView.getTag() > lastAnimatedPosition) {
      this.lastAnimatedPosition = position;

      ViewCompat.setPivotX(viewHolder.itemView, width / 2);
      ViewCompat.setScaleX(viewHolder.itemView, 1.3f);
      /*个人感觉原型里面Y轴木有放大，但加上也毫无违和感*/
      ViewCompat.setScaleY(viewHolder.itemView, 1.3f);
      ViewCompat.setAlpha(viewHolder.itemView, 0.0f);
      //viewHolder.itemView.setTranslationX(-10.0f);
      ViewCompat.setTranslationY(viewHolder.itemView, -100.0f * (position + 1));

      ViewCompat.animate(viewHolder.itemView)
          .scaleX(1.0f)
          .scaleY(1.0f)
          .alpha(1.0f)
          .translationX(0.0f)
          .translationY(0.0f)
          .setStartDelay(30 * position)
          .setInterpolator(new LinearInterpolator())
          .setDuration(300)
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override public void onAnimationEnd(View view) {

              viewHolder.description.setText(items.get(position).getDescription());
              animationsLocked = true;
            }
          });
    }
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    //@Nullable @Bind(R.id.detail_item_root) CardView cardView;
    @Nullable @Bind(R.id.detail_item_avatar) ImageView avatar;
    @Nullable @Bind(R.id.detail_item_text) TextView description;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @OnClick(R.id.detail_item_avatar) public void onAvatarClick(ImageView avatarItem) {
      /*处理点击事件*/
      if (callback != null) callback.onAvatarClick(avatarItem);
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {
    void onAvatarClick(ImageView avatarItem);
  }
}
