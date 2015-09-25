package com.app.designmore.adapter;

import android.os.Handler;
import android.os.HandlerThread;
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
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Joker on 2015/9/24.
 */
public class HomeBannerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

  private static final String TAG = HomeBannerAdapter.class.getSimpleName();

  private RxAppCompatActivity activity;
  private List<ProductEntity> items;

  private LayoutInflater layoutInflater;
  private Callback callback;

  private int count = 0;
  private volatile int currentPosition = 0;

  public HomeBannerAdapter(RxAppCompatActivity activity, final List<ProductEntity> items) {
    this.activity = activity;
    this.items = items;
    this.layoutInflater = LayoutInflater.from(activity);
    this.count = items.size();

    final Scheduler.Worker worker =
        HandlerScheduler.from(new Handler(Looper.getMainLooper())).createWorker();

    Observable.interval(Constants.MILLISECONDS_4000, Constants.MILLISECONDS_4000,
        TimeUnit.MILLISECONDS)
        .compose(activity.<Long>bindUntilEvent(ActivityEvent.DESTROY))
        .forEach(new Action1<Long>() {
          @Override public void call(Long aLong) {

            worker.schedule(new Action0() {
              @Override public void call() {
                if (callback != null) {
                  if (currentPosition != count - 1) {
                    callback.onRecycling(++currentPosition, true);
                  } else {
                    callback.onRecycling(currentPosition = 0, true);
                  }
                }
              }
            });
          }
        });
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

    Glide.with(activity)
        .load(items.get(position).getGoodThumbUrl())
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

    /*在这个地方回调Indicator，有稍微延迟，但是感觉很帅:)*/
    if (callback != null) {
      if (callback != null) callback.changeIndicator(currentPosition);
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
  }

  @Override public void onPageScrollStateChanged(int state) {

  }

  @Override public void onPageSelected(int position) {

    this.currentPosition = position;

    /*无延迟回调Indicator*/
    /* if (callback != null) {
      if (callback != null) callback.changeIndicator(currentPosition);
    }*/
  }

  public void setCallback(Callback callback) {

    this.callback = callback;
  }

  public interface Callback {

    void onItemClick(ProductEntity productEntity);

    void onRecycling(int position, boolean smoothScroll);

    void changeIndicator(int position);
  }
}
