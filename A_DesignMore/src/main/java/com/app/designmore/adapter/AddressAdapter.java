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
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.AddressEntity;
import java.util.List;
import rx.Observer;

/**
 * Created by Joker on 2015/9/1.
 */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder>
    implements Observer<Integer> {

  private int lastAnimatedPosition = -1;
  private boolean animationsLocked = false;
  private Callback callback;
  private Context context;
  private AddressEntity defaultAddress;

  /*数据*/
  private List<AddressEntity> items;

  public AddressAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_address_manager_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    holder.radioBtn.setTag(items.get(position));
    holder.editorBtn.setTag(items.get(position));
    holder.deleteBtn.setTag(items.get(position));

    /*绑定数据*/
    AddressAdapter.this.bindToValue(holder, items.get(position));

    /*执行进入动画*/
    AddressAdapter.this.runEnterAnimation(holder.itemView, position);
  }

  private void bindToValue(ViewHolder holder, AddressEntity address) {

    holder.userName.setText(address.getUserName());
    holder.userMobile.setText(address.getMobile());
    holder.userAddress.setText(address.getProvince() + address.getCity() + address.getAddress());

    if ("1".equals(address.isDefault())) {
      /*当前默认地址*/
      this.defaultAddress = address;
      holder.radioBtn.setImageDrawable(
          context.getResources().getDrawable(R.drawable.ic_radio_selected));
    } else {
      holder.radioBtn.setImageDrawable(
          context.getResources().getDrawable(R.drawable.ic_radio_normal));
    }
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
          .setDuration(Constants.MILLISECONDS_400)
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override public void onAnimationEnd(View view) {
              AddressAdapter.this.animationsLocked = true;
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

  @Override public void onNext(Integer deletePosition) {

    this.items.remove((int) deletePosition);
    AddressAdapter.this.notifyItemRemoved(deletePosition);
  }

  /**
   * 更新整张列表
   */
  public void updateItems(List<AddressEntity> addresses) {
    this.items = addresses;
    AddressAdapter.this.notifyDataSetChanged();
  }

  /**
   * 更新单个条目数据
   */
  public void updateItem(AddressEntity address, int editorPosition) {

    this.items.set(editorPosition, address);
    AddressAdapter.this.notifyItemChanged(editorPosition);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.address_manager_item_name) TextView userName;
    @Nullable @Bind(R.id.address_manager_item_phone) TextView userMobile;
    @Nullable @Bind(R.id.address_manager_item_address) TextView userAddress;
    @Nullable @Bind(R.id.address_manager_item_radio_btn) ImageButton radioBtn;
    @Nullable @Bind(R.id.address_manager_item_delete_btn) Button deleteBtn;
    @Nullable @Bind(R.id.address_manager_item_editor_btn) Button editorBtn;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.address_manager_item_delete_btn) void onDeleteClick(Button button) {

      if (callback != null) {
        AddressEntity entity = (AddressEntity) button.getTag();
        callback.onDeleteClick(entity);
      }
    }

    @Nullable @OnClick(R.id.address_manager_item_editor_btn) void onEditorClick(Button button) {

      if (callback != null) {
        AddressEntity entity = (AddressEntity) button.getTag();
        callback.onEditorClick(entity);
      }
    }

    @Nullable @OnClick(R.id.address_manager_item_radio_btn) void onRadionClick(ImageButton button) {

      AddressEntity newDefaultEntity = (AddressEntity) button.getTag();
      if (callback != null && newDefaultEntity != defaultAddress) {

        int oldPos = items.indexOf(defaultAddress);
        int newPos = items.indexOf(newDefaultEntity);
        AddressAdapter.this.defaultAddress = newDefaultEntity;

        if (oldPos != -1) {
          defaultAddress.setDefault("0");
          /*true -> false*/
          AddressAdapter.this.notifyItemChanged(oldPos);
        }

        newDefaultEntity.setDefault("1");
        /*false -> true*/
        AddressAdapter.this.notifyItemChanged(newPos);

        callback.onDefaultChange(newDefaultEntity);
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

    /*点击删除按钮*/
    void onDeleteClick(AddressEntity addressEntity);

    /*点击编辑按钮*/
    void onEditorClick(AddressEntity entity);

    /*checkbox状态改变*/
    void onDefaultChange(AddressEntity addressEntity);

    void onError(Throwable error);
  }
}
