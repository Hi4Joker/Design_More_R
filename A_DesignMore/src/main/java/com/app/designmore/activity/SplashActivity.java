package com.app.designmore.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    HomeActivity.navigateToHome(SplashActivity.this);
    SplashActivity.this.finish();
    overridePendingTransition(0, 0);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
