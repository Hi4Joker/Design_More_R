package com.app.designmore.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.app.designmore.R;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.manager.EventBusInstance;

public class SplashActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    UserCenterActivity.navigateToUserCenter(SplashActivity.this);
    SplashActivity.this.finish();
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(0, 0);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
