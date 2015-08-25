package com.joker.app;

import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;

/**
 * Created by Joker on 2015/7/15.
 */
public class BaseActivity extends AppCompatActivity {

  @Override public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);

    /**/
    ButterKnife.bind(BaseActivity.this);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(BaseActivity.this);
  }
}
