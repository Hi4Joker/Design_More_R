package com.app.designmore.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.response.DetailResponse;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

/**
 * Created by Joker on 2015/9/20.
 */
public class DetailBannerAdapter extends PagerAdapter {

  private static final String TAG = DetailBannerAdapter.class.getCanonicalName();
  private Context context;
  private List<DetailResponse.Detail.ProductBanner> productBanners;
  private LayoutInflater layoutInflater;
  private Callback callback;

  public DetailBannerAdapter(Context context,
      List<DetailResponse.Detail.ProductBanner> productBanners) {
    this.context = context;
    this.productBanners = productBanners;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @Override public int getCount() {
    return (this.productBanners != null) ? this.productBanners.size() : 0;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, final int position) {

    View view = layoutInflater.inflate(R.layout.i_banner_item, container, false);
    ImageView imageView = (ImageView) view.findViewById(R.id.banner_item_iv);

    Glide.with(context)
        .load(Constants.THUMB_URL + productBanners.get(position).thumbUrl)
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(imageView);

    imageView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (callback != null) callback.onItemClick(productBanners.get(position).thumbUrl);
      }
    });

    container.addView(view);
    return view;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    void onItemClick(String thumbUrl);
  }
}
