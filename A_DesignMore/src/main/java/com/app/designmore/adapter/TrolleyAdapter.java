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
import com.app.designmore.retrofit.entity.TrolleyEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import rx.Observer;

/**
 * Created by Joker on 2015/9/5.
 */
public class TrolleyAdapter extends RecyclerView.Adapter<TrolleyAdapter.ViewHolder>
    implements Observer<List<TrolleyEntity>> {

  private Context context;
  private List<TrolleyEntity> items;
  private Callback callback;

  public TrolleyAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_trolley_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    holder.radioBtn.setTag(position);
    //holder.rootView.setTag(position);

    TrolleyAdapter.this.bindToValue(holder, items.get(position));
  }

  private void bindToValue(ViewHolder holder, TrolleyEntity trolleyEntity) {

    Glide.with(context)
        .load(trolleyEntity.getGoodThumb())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.test_background)
        .error(R.drawable.test_background)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.goodIv);

    holder.goodNameTv.setText(trolleyEntity.getGoodName());
    holder.goodAttrTv.setText(trolleyEntity.getGoodAttr());
    holder.goodCountTv.setText(trolleyEntity.getGoodCount());
    holder.goodPriceTv.setText(trolleyEntity.getGoodPrice());

    holder.radioBtn.setImageDrawable(
        trolleyEntity.isChecked ? context.getResources().getDrawable(R.drawable.ic_radio_selected)
            : context.getResources().getDrawable(R.drawable.ic_radio_normal));
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  @Override public void onCompleted() {
    /*never invoked*/
  }

  @Override public void onError(Throwable e) {
    //if (callback != null) callback.onError(e);
  }

  @Override public void onNext(List<TrolleyEntity> trolleyEntities) {

    this.items = trolleyEntities;
    TrolleyAdapter.this.notifyDataSetChanged();
  }

  /**
   * 刷新整个列表
   */
  public void updateItems(List<TrolleyEntity> trolleyEntities) {

    this.items = trolleyEntities;
    TrolleyAdapter.this.notifyDataSetChanged();
  }

  /**
   * 删除固定条目
   */
  public void removeItem(int position) {

    this.items.remove(position);
    TrolleyAdapter.this.removeItem(position);
  }

  public void updataItem(TrolleyEntity trolleyEntity, int position) {

    this.items.set(position, trolleyEntity);
    TrolleyAdapter.this.notifyItemChanged(position);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    //@Nullable @Bind(R.id.trolley_item_root_view) RelativeLayout rootView;
    @Nullable @Bind(R.id.trolley_item_radio_btn) ImageButton radioBtn;
    @Nullable @Bind(R.id.trolley_item_good_iv) ImageView goodIv;
    @Nullable @Bind(R.id.trolley_item_good_name_tv) TextView goodNameTv;
    @Nullable @Bind(R.id.trolley_item_good_attr_tv) TextView goodAttrTv;
    @Nullable @Bind(R.id.trolley_item_good_count_tv) TextView goodCountTv;
    @Nullable @Bind(R.id.trolley_item_price_tv) TextView goodPriceTv;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.trolley_item_radio_btn) void onRadioClick(ImageButton imageButton) {

      int pos = (int) imageButton.getTag();

      TrolleyAdapter.this.items.get(pos).isChecked = !items.get(pos).isChecked;
      TrolleyAdapter.this.notifyItemChanged(pos);

      if (callback != null) callback.onRadioClick(pos);
    }

    /*@Nullable @OnClick(R.id.trolley_item_root_view) void onItemClick(RelativeLayout rootView) {

      int pos = (int) rootView.getTag();

      *//*TrolleyAdapter.this.items.get(pos).isChecked = !items.get(pos).isChecked;
      TrolleyAdapter.this.notifyItemChanged(pos);*//*

      if (callback != null) callback.onRadioClick(pos);
    }*/
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*点击单个条目的radio*/
    void onRadioClick(int position);

    //void onError(Throwable error);
  }
}
