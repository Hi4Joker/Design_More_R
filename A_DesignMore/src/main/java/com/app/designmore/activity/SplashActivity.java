package com.app.designmore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.app.designmore.helper.DBHelper;

public class SplashActivity extends AppCompatActivity {

  public static void navigateToSplash(AppCompatActivity startingActivity) {
    Intent intent = new Intent(startingActivity, LoginActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (DBHelper.getInstance(getApplicationContext()).getCurrentUser(SplashActivity.this) != null) {
      HomeActivity.navigateToHome(SplashActivity.this);
    } else {
      LoginActivity.navigateToLogin(SplashActivity.this);
    }
    SplashActivity.this.finish();
    overridePendingTransition(0, 0);
  }
}
