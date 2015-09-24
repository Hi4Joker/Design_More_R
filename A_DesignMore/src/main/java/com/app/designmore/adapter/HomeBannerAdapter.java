package com.app.designmore.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

/**
 * Created by Joker on 2015/9/24.
 */
public class HomeBannerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

  private static final String TAG = DetailBannerAdapter.class.getCanonicalName();
  private final int FAKE_BANNER_SIZE = 100;

  private Context context;
  private List<ProductEntity> items;
  private LayoutInflater layoutInflater;

  public HomeBannerAdapter(Context context, List<ProductEntity> items) {
    this.context = context;
    this.items = items;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @Override public int getCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {

    View view = layoutInflater.inflate(R.layout.i_banner_item, container, false);
    ImageView imageView = (ImageView) view.findViewById(R.id.banner_item_iv);

    Glide.with(context)
        .load(items.get(position).getGoodThumbUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(imageView);

    container.addView(view);
    return view;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

  }

  @Override public void onPageSelected(int position) {

  }

  @Override public void onPageScrollStateChanged(int state) {

  }
}
