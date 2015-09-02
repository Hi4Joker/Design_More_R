package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.Address;
import com.app.designmore.view.MaterialCheckBox;
import java.util.List;
import rx.Observer;

/**
 * Created by Joker on 2015/9/1.
 */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder>
    implements Observer<Address>, MaterialCheckBox.OnCheckedChangeListener {

  private List<Address> items;
  private int lastAnimatedPosition = -1;
  private boolean animationsLocked = false;
  private Callback callback;

  private Context context;

  public AddressAdapter(Context context, List<Address> items) {
    this.context = context;
    this.items = items;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.address_manager_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    holder.deleteBtn.setTag(position);
    holder.editorBtn.setTag(position);
    holder.checkBox.setTag(position);

    /*bindCheckbox监听事件*/
    holder.checkBox.setOnCheckedChangedListener(AddressAdapter.this);

    /*绑定数据*/
    AddressAdapter.this.bindToValue(holder, items.get(position));

    /*执行进入动画*/
    AddressAdapter.this.runEnterAnimation(holder.itemView, position);
  }

  private void bindToValue(ViewHolder holder, Address address) {

    holder.userName.setText(address.getUserName());
    holder.userMobile.setText(address.getMobile());
    holder.userAddress.setText(address.getAddress());
    holder.checkBox.setChecked(address.getChecked());
  }

  private void runEnterAnimation(View itemView, int position) {

    if (animationsLocked) return;

    if (position > lastAnimatedPosition) {
      AddressAdapter.this.lastAnimatedPosition = position;

      ViewCompat.setTranslationY(itemView, 100);
      ViewCompat.setAlpha(itemView, 0.0f);

      ViewCompat.animate(itemView)
          .translationY(0.0f)
          .alpha(1.0f)
          .setStartDelay(position * 20)
          .setInterpolator(new DecelerateInterpolator(2.0f))
          .setDuration(Constants.REVEAL_DURATION)
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override public void onAnimationEnd(View view) {
              AddressAdapter.this.animationsLocked = true;
            }
          });
    }
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  @Override public void onCompleted() {
    AddressAdapter.this.notifyDataSetChanged();
  }

  @Override public void onError(Throwable e) {

    if (callback != null) {
      callback.onError(e);
    }
  }

  @Override public void onNext(Address address) {
    items.remove(address);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.address_manager_item_name) TextView userName;
    @Nullable @Bind(R.id.address_manager_item_phone) TextView userMobile;
    @Nullable @Bind(R.id.address_manager_item_address) TextView userAddress;
    @Nullable @Bind(R.id.address_manager_item_radio_btn) MaterialCheckBox checkBox;
    @Nullable @Bind(R.id.address_manager_item_delete_btn) Button deleteBtn;
    @Nullable @Bind(R.id.address_manager_item_editor_btn) Button editorBtn;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(itemView);
    }

    @Nullable @OnClick(R.id.address_manager_item_delete_btn) void onDeleteClick(Button button) {

      if (callback != null) {
        int pos = (int) button.getTag();
        callback.onDeleteClick(pos);
      }
    }

    @Nullable @OnClick(R.id.address_manager_item_editor_btn) void onEditorClick(Button button) {

      if (callback != null) {
        int pos = (int) button.getTag();
        callback.onEditorClick(pos);
      }
    }
  }

  public void setAnimationsLocked(boolean animationsLocked) {
    this.animationsLocked = animationsLocked;
  }

  /*public void setDelayEnterAnimation(boolean delayEnterAnimation) {
    this.delayEnterAnimation = delayEnterAnimation;
  }*/

  @Override public void onCheckedChanged(MaterialCheckBox materialCheckBox, boolean isChecked) {

    if (callback != null) {
      callback.onCheckChange(materialCheckBox, isChecked, (Integer) materialCheckBox.getTag());
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*点击删除按钮*/
    void onDeleteClick(int position);

    /*点击编辑按钮*/
    void onEditorClick(int position);

    /*checkbox状态改变*/
    void onCheckChange(MaterialCheckBox checkBox, boolean isCheck, int position);

    void onError(Throwable error);
  }
}
