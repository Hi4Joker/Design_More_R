package com.joker.supportdesign.mvp.view.activity;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import butterknife.Bind;
import butterknife.OnClick;
import com.joker.supportdesign.R;
import com.joker.supportdesign.mvp.domain.ForecastEntity;
import com.joker.supportdesign.mvp.domain.event.RlScrollEvent;
import com.joker.supportdesign.mvp.domain.event.RlScrollYEvent;
import com.joker.supportdesign.mvp.presenter.MainPresenter;
import com.joker.supportdesign.mvp.presenter.MainPresenterImp;
import com.joker.supportdesign.mvp.view.BaseActivity;
import com.joker.supportdesign.mvp.view.fragment.ListFragment;
import com.joker.supportdesign.mvp.viewInterface.MainView;
import com.joker.supportdesign.ui.WeatherLayout;
import com.joker.supportdesign.util.manager.WeatherLayoutManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity
    implements View.OnClickListener, WeatherLayout.OnItemClickListener,
    WeatherLayoutManager.WeatherWindowListener, MainView {

  private static final String TAG = MainActivity.class.getSimpleName();

  @Nullable @Bind(R.id.main_root_drawer_layout) DrawerLayout drawerLayout;
  @Nullable @Bind(R.id.main_content) CoordinatorLayout contentLayout;
  @Nullable @Bind(R.id.viewpager) ViewPager viewPager;
  @Nullable @Bind(R.id.tabs) TabLayout tabLayout;
  @Nullable @Bind(R.id.main_content_fab) FloatingActionButton fab;
  @Nullable @Bind(R.id.appBar_layout) AppBarLayout appBarLayout;

  private WeatherLayoutManager weatherLayoutManager;
  private MainPresenter<MainView> mainPresenter;
  private Snackbar snackbar;
  private LocationManager locationManager;

  private MenuItem inboxMenuItem;
  private MenuItem weatherMenuItem;
  private boolean enterWithAnim = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MainActivity.this.setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      enterWithAnim = true;
    }

    MainActivity.this.initView();
    MainActivity.this.setListener();
    MainActivity.this.iniData();
  }

  private void initView() {
  }

  private void setListener() {
  }

  private void iniData() {
    mainPresenter = new MainPresenterImp();
    mainPresenter.attachView(MainActivity.this);
    MainActivity.this.setupViewPager(viewPager);
  }

  private void setupViewPager(ViewPager viewPager) {

    Adapter adapter = new Adapter(getSupportFragmentManager());
    adapter.addFragment(new ListFragment(), "TabLayout 1");
    adapter.addFragment(new ListFragment(), "TabLayout 2");
    adapter.addFragment(new ListFragment(), "TabLayout 3");
    adapter.addFragment(new ListFragment(), "TabLayout 4");
    viewPager.setAdapter(adapter);

    tabLayout.setupWithViewPager(viewPager);
    tabLayout.setTabsFromPagerAdapter(adapter);
  }

  @Override protected void setupToolbar() {
    if (toolbar != null) {
      setSupportActionBar(toolbar);
      /*Navigation Icon 要设定在 setSupportActionBar 才有作用,否则会出现 back button*/
      toolbar.setNavigationIcon(R.drawable.ic_menu);
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_layout, menu);
    /*如果不用ActionView，点击效果看上去很奇怪，奇怪不是问题，重要是它丑*/
    inboxMenuItem = menu.findItem(R.id.action_inbox);
    inboxMenuItem.setActionView(R.layout.menu_inbox_item);
    inboxMenuItem.getActionView().setOnClickListener(this);

    weatherMenuItem = menu.findItem(R.id.action_weather);
    weatherMenuItem.setActionView(R.layout.menu_weather_item);
    weatherMenuItem.getActionView().setOnClickListener(this);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        drawerLayout.openDrawer(GravityCompat.START);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @OnClick(R.id.main_content_fab) public void OnMainFabClick(View view) {
    Snackbar.make(toolbar, "pay me a follower", Snackbar.LENGTH_LONG)
        .setAction("Go", new View.OnClickListener() {
          @Override public void onClick(View v) {
            // TODO: 2015/6/26 pay me a coffee
          }
        })
        .show();
  }

  @Override public void onClick(View view) {

    switch (view.getId()) {

      case R.id.action_inbox_view:
        Snackbar.make(view, "Encourage me", Snackbar.LENGTH_LONG)
            .setAction("GO 小鄧子简书", new View.OnClickListener() {
              @Override public void onClick(View v) {
                // TODO: 2015/6/26 跳转简书博客，点赞吧
              }
            })
            .show();
        break;

      case R.id.action_weather_view:

        weatherLayoutManager = WeatherLayoutManager.getInstance();
        if (!weatherLayoutManager.isShowing()) {
          weatherLayoutManager.setWindowChangeListener(this);
        }
        weatherLayoutManager.toggleWeatherLayout(view, this);
        break;
    }
  }

  public void onEventMainThread(RlScrollEvent event) {

    int translationY = 0;
    if (!event.isShow()) {
      translationY = MainActivity.this.calculateTranslation(fab);
    }

    ViewCompat.animate(fab)
        .translationY(translationY)
        .setInterpolator(new AccelerateDecelerateInterpolator())
        .setDuration(
            MainActivity.this.getResources().getInteger(android.R.integer.config_longAnimTime));
  }

  public void onEventMainThread(RlScrollYEvent event) {

    if (weatherLayoutManager != null) {
      weatherLayoutManager.onScrolled(event.getY());
    }
  }

  /**
   * onWeatherRefresh 刷新天气
   * onWeatherConfirm 隐藏天气
   */
  @Override public void onWeatherRefresh() {

    if (locationManager == null) {
      locationManager = (LocationManager) MainActivity.this.getApplication()
          .getSystemService(Context.LOCATION_SERVICE);
    }

    //WeakReference<LocationManager> weakReference = new WeakReference<>(locationManager);

    mainPresenter.requestWeatherData(locationManager);
  }

  @Override public void onWeatherConfirm() {

    weatherLayoutManager.hideWeatherLayout();
  }

  /**
   * setWeather       填充WeatherLayout
   * onWeatherStateChange   根据Observable状态，更改提示信息
   */
  @Override public void setWeather(HashMap<String, ForecastEntity> weatherData) {

    weatherLayoutManager.setWeather(weatherData);
  }

  @Override public void onWeatherStateChange(@WeatherLayout.Status int state) {

    weatherLayoutManager.setWeatherState(state);
  }

  /**
   * onWeatherShow 天气预报可见
   * onWeatherHide 天气预报不可见
   */
  @Override public void onWeatherShow() {

    if (locationManager == null) {
      locationManager = (LocationManager) MainActivity.this.getApplication()
          .getSystemService(Context.LOCATION_SERVICE);
    }

    //WeakReference<LocationManager> weakReference = new WeakReference<>(locationManager);

    mainPresenter.requestWeatherData(locationManager);
  }

  @Override public void onWeatherHide() {
    mainPresenter.onWeatherDetach();
  }

  @Override public void showMessage(int resString) {

    if (snackbar != null) {
      snackbar.dismiss();
      snackbar = null;
    }

    snackbar = Snackbar.make(toolbar, resString, Snackbar.LENGTH_LONG)
        .setAction("确定", new View.OnClickListener() {
          @Override public void onClick(View v) {
          }
        });
    snackbar.show();
  }

  @Override public void onBackPressed() {
    if (weatherLayoutManager.isShowing()) {
      weatherLayoutManager.hideWeatherLayout();
      return;
    }
    super.onBackPressed();
  }

  static class Adapter extends FragmentStatePagerAdapter {
    private final List<ListFragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();

    public Adapter(FragmentManager fm) {
      super(fm);
    }

    public void addFragment(ListFragment fragment, String title) {
      mFragments.add(fragment);
      mFragmentTitles.add(title);
    }

    @Override public Fragment getItem(int position) {
      return mFragments.get(position);
    }

    @Override public int getCount() {
      return mFragments.size();
    }

    @Override public CharSequence getPageTitle(int position) {
      return mFragmentTitles.get(position);
    }
  }

  @Override public void showProgress() {

  }

  @Override public void hideProgress() {

  }

  private int calculateTranslation(View view) {

    int height = view.getHeight();
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    int margins = params.topMargin + params.bottomMargin;
    return height + margins;
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mainPresenter.detachView(true);
  }
}
