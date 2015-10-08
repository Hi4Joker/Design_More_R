package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.AddressEntity;
import com.app.designmore.retrofit.entity.DeliveryEntity;
import java.util.List;
import rx.Observer;

/**
 * Created by Joker on 2015/10/8.
 */
public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder>
    implements Observer<List<DeliveryEntity>> {

  private int lastAnimatedPosition = -1;
  private boolean animationsLocked = false;
  private Callback callback;
  private Context context;

  private List<DeliveryEntity> items;

  public DeliveryAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_delivery_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    holder.rootView.setTag(items.get(position));

    /*绑定数据*/
    DeliveryAdapter.this.bindToValue(holder, items.get(position));

    /*执行进入动画*/
    DeliveryAdapter.this.runEnterAnimation(holder.itemView, position);
  }

  private void bindToValue(ViewHolder holder, DeliveryEntity deliveryEntity) {

    holder.deliveryTv.setText(deliveryEntity.getDeliveryName());
  }

  @Override public long getItemId(int position) {
    return super.getItemId(position);
  }

  private void runEnterAnimation(View itemView, int position) {

    if (animationsLocked) return;

    if (position > lastAnimatedPosition) {
      DeliveryAdapter.this.lastAnimatedPosition = position;

      ViewCompat.setTranslationY(itemView, 100);
      ViewCompat.setAlpha(itemView, 0.0f);

      ViewCompat.animate(itemView)
          .translationY(0.0f)
          .alpha(1.0f)
          .setStartDelay(position * 20)
          .setInterpolator(new DecelerateInterpolator(2.0f))
          .setDuration(Constants.MILLISECONDS_400)
          .withLayer()
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override public void onAnimationEnd(View view) {
              DeliveryAdapter.this.animationsLocked = true;
            }
          })
          .start();
    }
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  @Override public void onCompleted() {
    /*never invoked*/
  }

  @Override public void onError(Throwable e) {
    if (callback != null) callback.onError(e);
  }

  @Override public void onNext(List<DeliveryEntity> deliveryEntities) {
    this.items = deliveryEntities;
    DeliveryAdapter.this.notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.order_delivery_item_ll) LinearLayout rootView;
    @Nullable @Bind(R.id.order_delivery_item_tv) TextView deliveryTv;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.order_delivery_item_ll) void onItemClick(LinearLayout linearLayout) {

      DeliveryEntity entity = (DeliveryEntity) linearLayout.getTag();
      if (callback != null) {
        callback.onItemClick(entity);
      }
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*checkbox状态改变*/
    void onItemClick(DeliveryEntity deliveryEntity);

    void onError(Throwable e);
  }
}
