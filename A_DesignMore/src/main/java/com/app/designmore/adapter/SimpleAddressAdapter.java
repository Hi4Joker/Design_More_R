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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.AddressEntity;
import java.util.List;

/**
 * Created by Joker on 2015/9/27.
 */
public class SimpleAddressAdapter extends RecyclerView.Adapter<SimpleAddressAdapter.ViewHolder> {

  private int lastAnimatedPosition = -1;
  private boolean animationsLocked = false;
  private Callback callback;
  private Context context;

  /*数据*/
  private List<AddressEntity> items;

  public SimpleAddressAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_address_simple_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    holder.rootView.setTag(items.get(position));

    /*绑定数据*/
    SimpleAddressAdapter.this.bindToValue(holder, items.get(position));

    /*执行进入动画*/
    SimpleAddressAdapter.this.runEnterAnimation(holder.itemView, position);
  }

  private void bindToValue(ViewHolder holder, AddressEntity address) {

    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    spannableStringBuilder.append(address.getUserName());
    spannableStringBuilder.append("   " + address.getMobile() + "\n \n");
    spannableStringBuilder.append(address.getProvince() + address.getCity() + address.getAddress());
    holder.addressTv.setText(spannableStringBuilder);

    if ("1".equals(address.isDefault())) {
      /*当前默认地址*/
      holder.radioBtn.setImageDrawable(
          context.getResources().getDrawable(R.drawable.ic_radio_selected_icon));
    } else {
      holder.radioBtn.setImageDrawable(null);
    }
  }

  @Override public long getItemId(int position) {
    return super.getItemId(position);
  }

  private void runEnterAnimation(View itemView, int position) {

    if (animationsLocked) return;

    if (position > lastAnimatedPosition) {
      SimpleAddressAdapter.this.lastAnimatedPosition = position;

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
              SimpleAddressAdapter.this.animationsLocked = true;
            }
          })
          .start();
    }
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  /**
   * 更新整张列表
   */
  public void updateItems(List<AddressEntity> addresses) {

    this.lastAnimatedPosition = -1;
    this.animationsLocked = false;

    this.items = addresses;
    SimpleAddressAdapter.this.notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.order_address_item_ll) LinearLayout rootView;
    @Nullable @Bind(R.id.order_address_item_tv) TextView addressTv;
    @Nullable @Bind(R.id.order_address_item_radio_iv) ImageView radioBtn;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.order_address_item_ll) void onItemClick(LinearLayout linearLayout) {

      AddressEntity entity = (AddressEntity) linearLayout.getTag();
      if (callback != null) {
        callback.onItemClick(entity);
      }
    }
  }

  public void setAnimationsLocked(boolean animationsLocked) {
    this.animationsLocked = animationsLocked;
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*checkbox状态改变*/
    void onItemClick(AddressEntity addressEntity);
  }
}
