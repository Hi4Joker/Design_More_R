package com.joker.supportdesign.mvp.view;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.joker.supportdesign.R;
import com.joker.supportdesign.util.EventBusInstance;

/**
 * Created by Joker on 2015/6/26.
 */
public abstract class BaseActivity extends AppCompatActivity {

  /*http://jakewharton.github.io/butterknife/*/

  @Nullable @Bind(R.id.toolbar) protected Toolbar toolbar;

  @Override public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);

    /*ButterKnife（编译时生成辅助类或者xml文件等）你懂得
    相较于Guice、xutils、afinal（运行时读取注解通过反射生成类，再进行依赖注入）性能上有所提升*/
    ButterKnife.bind(BaseActivity.this);

    /*注册EventBus*/
    EventBusInstance.getDefault().register(BaseActivity.this);

    /*初始化ToolBar*/
    BaseActivity.this.setupToolbar();
  }

  protected abstract void setupToolbar();

  @Override protected void onDestroy() {
    super.onDestroy();

    ButterKnife.unbind(BaseActivity.this);
    EventBusInstance.getDefault().unregister(BaseActivity.this);
    EventBusInstance.getDefault().clearCaches();
  }
}
