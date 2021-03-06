package com.app.designmore.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.manager.CropCircleTransformation;
import com.app.designmore.retrofit.entity.CategoryEntity;
import com.app.designmore.view.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import java.util.List;

/**
 * Created by Joker on 2015/9/24.
 */
public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder> {

  private static final String TAG = HomeCategoryAdapter.class.getCanonicalName();

  private Activity activity;
  private List<CategoryEntity> items;
  private Callback callback;

  public HomeCategoryAdapter(Activity activity) {
    this.activity = activity;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(
        LayoutInflater.from(activity).inflate(R.layout.i_home_category_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    if (position == 0) {

      holder.categoryItemTv.setText("全部商品");
      Glide.with(activity)
          .load(R.drawable.home_all_product_icon)
          .centerCrop()
          .crossFade()
          .placeholder(R.drawable.center_profile_default_icon)
          .error(R.drawable.center_profile_default_icon)
          .diskCacheStrategy(DiskCacheStrategy.RESULT)
          .into(holder.categoryItemIv);
    } else {

      holder.categoryItemTv.setText(items.get(position - 1).getCatName());
      BitmapPool bitmapPool = Glide.get(activity).getBitmapPool();
      Glide.with(activity)
          .load(items.get(position - 1).getCatThumbUrl())
          .centerCrop()
          .crossFade()
          .bitmapTransform(new CropCircleTransformation(bitmapPool))
          .placeholder(R.drawable.center_profile_default_icon)
          .error(R.drawable.center_profile_default_icon)
          .diskCacheStrategy(DiskCacheStrategy.RESULT)
          .into(holder.categoryItemIv);
    }

    ((MaterialRippleLayout) holder.categoryItemIv.getParent()).setTag(position);
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() + 1 : 0;
  }

  /**
   * 更新整张列表
   */
  public void updateItems(List<CategoryEntity> categoryEntities) {
    this.items = categoryEntities;
    HomeCategoryAdapter.this.notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.home_category_item_iv) ImageView categoryItemIv;
    @Nullable @Bind(R.id.home_category_item_tv) TextView categoryItemTv;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.home_category_item_iv) void onItemClick(ImageView imageView) {

      int position = (int) ((MaterialRippleLayout) imageView.getParent()).getTag();

      if (callback != null) {
        if (position == 0) {
          callback.onAllCategoryClick();
        } else {
          callback.onCategoryItemClick(items.get(position - 1));
        }
      }
    }
  }

  public void setCallback(Callback callBack) {
    this.callback = callBack;
  }

  public interface Callback {

    /*点击条目*/
    void onCategoryItemClick(CategoryEntity entity);

    /*点击全部商品*/
    void onAllCategoryClick();
  }
}
