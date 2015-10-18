package com.app.designmore.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.CollectionEntity;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.app.designmore.rxAndroid.schedulers.HandlerScheduler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/9/24.
 */
public class HomeBannerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

  private static final String TAG = HomeBannerAdapter.class.getSimpleName();

  private Context context;
  private List<ProductEntity> items;
  private ViewPager viewPager;

  private LayoutInflater layoutInflater;
  private Callback callback;

  private volatile int currentPosition = 0;
  private volatile int lastPosition;
  private volatile int currentScrollState;

  private Scheduler.Worker worker;

  private float previousPositionOffset;
  private int previousPosition = -1;
  private boolean scrollingLeft;

  public HomeBannerAdapter(Context context, final ViewPager viewPager) {
    this.context = context;
    this.viewPager = viewPager;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @Override public int getCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, final int position) {

    View view = layoutInflater.inflate(R.layout.i_banner_item, container, false);
    ImageView imageView = (ImageView) view.findViewById(R.id.banner_item_iv);

    Glide.with(context)
        .load(Constants.THUMB_URL + items.get(position).getGoodThumbUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(imageView);

    imageView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (callback != null) callback.onItemClick(items.get(position));
      }
    });

    container.addView(view);

    return view;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  @Override public void finishUpdate(ViewGroup container) {

    if (callback != null) {
      if (callback != null) callback.changeIndicator(currentPosition);
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

   /* if ((positionOffset > previousPositionOffset && position == previousPosition) || (positionOffset
        < previousPositionOffset && position > previousPosition)) {

      scrollingLeft = true;
    } else if (positionOffset < previousPositionOffset) {

      scrollingLeft = false;
    }

    *//*towards left [0,1]; towards right [1,0]*/
    /*previousPositionOffset = positionOffset;
    previousPosition = position;*/
  }

  @Override public void onPageScrollStateChanged(int state) {

    HomeBannerAdapter.this.handleScrollState(state);
    this.currentScrollState = state;
  }

  private void handleScrollState(final int state) {
    if (state == ViewPager.SCROLL_STATE_IDLE) {
      HomeBannerAdapter.this.scrollEndless();
    }
  }

  private void scrollEndless() {
    if (this.currentScrollState != ViewPager.SCROLL_STATE_SETTLING
        && this.currentPosition == lastPosition) {
      viewPager.setCurrentItem(this.currentPosition = 0, true);
    }
  }

  @Override public void onPageSelected(int position) {
    this.currentPosition = position;
  }

  /**
   * 更新整张列表
   */
  public void updateItems(List<ProductEntity> productEntities) {

    if (worker != null && !worker.isUnsubscribed()) {
      worker.unsubscribe();
    }
    worker = HandlerScheduler.from(new Handler(Looper.getMainLooper())).createWorker();

    this.items = productEntities;
    this.lastPosition = productEntities.size() - 1;
    HomeBannerAdapter.this.notifyDataSetChanged();
    this.viewPager.setCurrentItem(currentPosition = 0, false);

    if (items.size() != 0) {
      worker.schedulePeriodically(new Action0() {
                                    @Override public void call() {

                                      if (currentPosition != lastPosition) {
                                        viewPager.setCurrentItem(++currentPosition, true);
                                      } else {
                                        viewPager.setCurrentItem(currentPosition = 0, true);
                                      }
                                    }
                                  }, Constants.MILLISECONDS_4000, Constants.MILLISECONDS_4000,
          TimeUnit.MILLISECONDS);
    }
  }

  public void detach() {
    if (worker != null && !worker.isUnsubscribed()) {
      this.worker.unsubscribe();
      this.worker = null;
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    void onItemClick(ProductEntity productEntity);

    void changeIndicator(int position);
  }
}
