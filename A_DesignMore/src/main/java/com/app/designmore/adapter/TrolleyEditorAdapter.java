package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.TrolleyActivity;
import com.app.designmore.retrofit.entity.TrolleyEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;

/**
 * Created by Joker on 2015/9/23.
 */
public class TrolleyEditorAdapter extends RecyclerView.Adapter<TrolleyEditorAdapter.ViewHolder>
    implements Observer<BaseResponse> {

  private Context context;
  private List<TrolleyEntity> items;
  private Callback callback;

  public TrolleyEditorAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_trolley_editor_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    /*绑定数据*/
    TrolleyEditorAdapter.this.bindToValue(holder, items.get(position));
  }

  private void bindToValue(ViewHolder holder, TrolleyEntity trolleyEntity) {

    Glide.with(context)
        .load(trolleyEntity.getGoodThumb())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_256_icon)
        .error(R.drawable.ic_default_256_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.goodIv);

    holder.goodNameTv.setText(trolleyEntity.getGoodName());
    holder.goodCountTv.setText(trolleyEntity.getGoodCount());
    holder.goodAttrTv.setText("颜色分类: " + trolleyEntity.getGoodAttrValue());

    holder.radioBtn.setImageDrawable(
        context.getResources().getDrawable(R.drawable.ic_radio_normal_icon_icon));

    holder.rootView.setTag(trolleyEntity);
    holder.addCountIb.setTag(trolleyEntity);
    holder.subtractCountIb.setTag(trolleyEntity);
    holder.arrowIb.setTag(trolleyEntity);
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

  @Override public void onNext(BaseResponse baseResponse) {

    Observable.from(items).subscribe(new Action1<TrolleyEntity>() {
      @Override public void call(TrolleyEntity trolleyEntity) {
        TrolleyEditorAdapter.this.removeItem(items.indexOf(trolleyEntity));
      }
    });
  }

  /**
   * 刷新整个列表
   */
  public void updateItems(List<TrolleyEntity> trolleyEntities) {
    this.items = trolleyEntities;
    TrolleyEditorAdapter.this.notifyDataSetChanged();
  }

  private void removeItem(int position) {
    this.items.remove(position);
    TrolleyEditorAdapter.this.notifyItemRemoved(position);
  }

  public void updateItem(int position, TrolleyEntity trolleyEntity) {

    this.items.set(position, trolleyEntity);
    TrolleyEditorAdapter.this.notifyItemChanged(position);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.trolley_editor_item_root_view) RelativeLayout rootView;
    @Nullable @Bind(R.id.trolley_editor_item_radio_iv) ImageView radioBtn;
    @Nullable @Bind(R.id.trolley_editor_item_good_iv) ImageView goodIv;
    @Nullable @Bind(R.id.trolley_editor_item_good_name_tv) TextView goodNameTv;
    @Nullable @Bind(R.id.trolley_editor_item_count_tv) TextView goodCountTv;
    @Nullable @Bind(R.id.trolley_editor_item_good_attr_tv) TextView goodAttrTv;

    @Nullable @Bind(R.id.trolley_editor_item_count_add_ib) ImageButton addCountIb;
    @Nullable @Bind(R.id.trolley_editor_item_count_subtract_ib) ImageButton subtractCountIb;
    @Nullable @Bind(R.id.trolley_editor_fuck_arrow_ib) ImageButton arrowIb;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.trolley_editor_item_root_view) void onItemClick(
        RelativeLayout relativeLayout) {

      TrolleyEntity entity = (TrolleyEntity) relativeLayout.getTag();
      entity.isChecked = !entity.isChecked;
      if (callback != null) callback.onRadioClick(entity);
    }

    @Nullable @OnClick(R.id.trolley_editor_item_count_add_ib) void onAddCountClick(
        ImageButton imageButton) {

      TrolleyEntity entity = (TrolleyEntity) imageButton.getTag();
      if (callback != null) callback.onAddCountClick(entity);
    }

    @Nullable @OnClick(R.id.trolley_editor_item_count_subtract_ib) void onSubtractCountClick(
        ImageButton imageButton) {

      TrolleyEntity entity = (TrolleyEntity) imageButton.getTag();
      if (callback != null && Integer.parseInt(entity.getGoodCount()) > 1) {
        callback.onSubtractCountClick(entity);
      }
    }

    @Nullable @OnClick(R.id.trolley_editor_fuck_arrow_ib) void onArrowClick(
        ImageButton imageButton) {

      TrolleyEntity entity = (TrolleyEntity) imageButton.getTag();
      if (callback != null) callback.onArrowClick(entity);
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*点击单个条目的radio*/
    void onRadioClick(TrolleyEntity position);

    /*增加加一*/
    void onAddCountClick(TrolleyEntity position);

    /*数量减一*/
    void onSubtractCountClick(TrolleyEntity position);

    /*点击箭头，弹出属性框*/
    void onArrowClick(TrolleyEntity position);

    void onError(Throwable error);
  }
}
