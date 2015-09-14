package com.app.designmore.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import butterknife.ButterKnife;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.helper.MyApplication;
import com.app.designmore.manager.EventBusInstance;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/7/15.
 */
public abstract class BaseActivity extends RxAppCompatActivity {

  /*初始化View*/
  public abstract void initView(Bundle savedInstanceState);

  @Override public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);

    ButterKnife.bind(BaseActivity.this);
    EventBusInstance.getDefault().register(BaseActivity.this);
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    ButterKnife.unbind(BaseActivity.this);
    EventBusInstance.getDefault().unregister(BaseActivity.this);
    MyApplication.getRefWatcher().watch(this);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      BaseActivity.this.exit();
    }
    return false;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        BaseActivity.this.exit();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(0, 0);
  }

  public abstract void exit();

  /*退出*/
  public void onEventMainThread(FinishEvent event) {
    BaseActivity.this.finish();
  }
}
