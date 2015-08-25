package com.joker.supportdesign.mvp.view.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.joker.supportdesign.R;
import com.joker.supportdesign.mvp.domain.Animal;
import com.joker.supportdesign.ui.MaterialRippleLayout;
import com.joker.supportdesign.util.CircleTransF;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Joker on 2015/6/29.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

  private static final String TAG = ListAdapter.class.getSimpleName();
  private List<Animal> dateList;
  private int avatarSize;
  private int primaryColor;
  private int primaryDarkColor;

  private WeakReference<Context> weakReference;
  private int lastAnimatedPosition = -1;
  private boolean animationsLocked = false;

  public ListAdapter(Context context, List<Animal> dateList) {
    this.weakReference = new WeakReference<>(context);

    this.dateList = dateList;
    avatarSize = context.getResources().getDimensionPixelSize(R.dimen.list_item_avatar_size);

    primaryColor = context.getResources().getColor(R.color.primary);
    primaryDarkColor = context.getResources().getColor(R.color.primary_dark);
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_fragment_item, parent, false));
  }

  @Override public void onBindViewHolder(final ViewHolder holder, int position) {

    holder.avatar.setTag(position);
    holder.itemView.setTag(position);
    holder.cardView.setCardBackgroundColor(Color.WHITE);

    holder.name.setText(dateList.get(position).getName());

    /*执行进入动画*/
    ListAdapter.this.runEnterAnimation(holder.itemView, position);

    /*执行蜜蜂加载动画*/
    ListAdapter.this.runLoadingBee(holder.avatar);

    if (weakReference.get() == null) {
      holder.avatar.setBackgroundResource(R.drawable.ic_launcher);
    } else {
      Picasso.with(weakReference.get())
          .load(dateList.get(position).getUrl())
          .centerCrop()
          .resize(avatarSize, avatarSize)
          .transform(new CircleTransF())
          .into(holder.avatar, new Callback() {
            @Override public void onSuccess() {

              final Drawable animationDrawable = holder.avatar.getBackground();
              if (animationDrawable instanceof AnimationDrawable
                  && ((AnimationDrawable) animationDrawable).isRunning()) {
                holder.avatar.setBackgroundResource(0);
                ((AnimationDrawable) animationDrawable).stop();
              }

           /* Palette.generateAsync(((BitmapDrawable) holder.avatar.getDrawable()).getBitmap(), 24,
                new Palette.PaletteAsyncListener() {
                  @Override public void onGenerated(Palette palette) {
                    ListAdapter.this.applyBackgroundColor(palette);
                  }
                });*/

           /* Palette.generateAsync(((BitmapDrawable) holder.avatar.getDrawable()).getBitmap(),
                new Palette.PaletteAsyncListener() {
                  @Override public void onGenerated(Palette palette) {
                    ListAdapter.this.applyBackgroundColor(palette);
                  }
                });*/

             /* Palette.from(((BitmapDrawable) holder.avatar.getDrawable()).getBitmap())
                  .generate(new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette palette) {
                      ListAdapter.this.setRippleColor(palette, holder);
                    }
                  });*/
            }

            @Override public void onError() {
            }
          });
    }
  }

  private void runLoadingBee(ImageView avatar) {

    avatar.setBackgroundResource(0);
    avatar.setBackgroundResource(R.anim.bee_loading);
    ((AnimationDrawable) avatar.getBackground()).start();
  }

  public void setAnimationsLocked(boolean animationsLocked) {
    this.animationsLocked = animationsLocked;
  }

  private void runEnterAnimation(View itemView, int position) {

    ViewCompat.animate(itemView).cancel();

    if (animationsLocked) {
      return;
    }

    if ((Integer) itemView.getTag() > lastAnimatedPosition) {
      lastAnimatedPosition = position;
      itemView.setPivotX(0.0f);
      itemView.setPivotY(0.0f);
      itemView.setScaleX(0.1f);
      itemView.setScaleY(0.1f);
      itemView.setAlpha(0.0f);

      ViewCompat.animate(itemView)
          .scaleX(1.0f)
          .scaleY(1.0f)
          .alpha(1.0f)
          .setStartDelay(20 * position)
          .setInterpolator(new DecelerateInterpolator(2.0f))
          .setDuration(300);
    }
  }

  /*private void setRippleColor(Palette palette, ViewHolder holder) {
    ViewGroup parent = (ViewGroup) holder.item.getParent();
    if (parent != null && parent instanceof MaterialRippleLayout) {
      ((MaterialRippleLayout) parent).setRippleColor(palette.getDarkMutedColor(primaryDarkColor));
    }
  }*/

  @Override public int getItemCount() {
    return dateList.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.list_item_card_view) CardView cardView;
    @Nullable @Bind(R.id.list_item_avatar) ImageView avatar;
    @Nullable @Bind(R.id.list_item_text) TextView name;

    public ViewHolder(View view) {
      super(view);
      ButterKnife.bind(ViewHolder.this, view);
    }

    @OnClick(R.id.list_item_avatar) public void onAvatarClick(final View view) {

      if (onListClickListener != null) {
        if (avatar.getDrawable() instanceof BitmapDrawable) {
          Palette.from(((BitmapDrawable) avatar.getDrawable()).getBitmap())
              .generate(new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                  onListClickListener.onHeaderClick(view, palette, (Integer) view.getTag());
                }
              });
        } else {
          onListClickListener.onHeaderClick(view, null, (Integer) view.getTag());
        }
      }
    }

    @OnLongClick(R.id.list_item_rl) public boolean onItemLongClick(final View view) {

      if (onListClickListener != null) {
        if (avatar.getDrawable() instanceof BitmapDrawable) {

          Palette.from(((BitmapDrawable) avatar.getDrawable()).getBitmap())
              .generate(new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {

                  int color = palette.getLightMutedColor(primaryDarkColor);
                  onListClickListener.onItemLongClick(view, color, 199);
                }
              });
        } else {
          onListClickListener.onItemLongClick(view, Color.DKGRAY, 199);
        }
      }
      return true;
    }
  }

  private OnListClickListener onListClickListener;

  public void setOnListClickListener(OnListClickListener listener) {
    this.onListClickListener = listener;
  }

  public interface OnListClickListener {

    void onHeaderClick(View view, Palette palette, int position);

    void onItemLongClick(View view, int backgroundColor, int position);
  }
}
