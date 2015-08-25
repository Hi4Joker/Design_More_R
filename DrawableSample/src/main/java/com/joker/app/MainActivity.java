package com.joker.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import com.joker.app.view.CircleImageView;
import com.joker.app.view.CircleView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final String RXTAG = "RXTAG";

  private ImageView iv1;
  private CircleView iv2;
  private CircleImageView iv3;

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    iv1 = (ImageView) findViewById(R.id.iv1);
    iv2 = (CircleView) findViewById(R.id.circle);
    iv3 = (CircleImageView) findViewById(R.id.round);

    Drawable drawable1 = getResources().getDrawable(R.drawable.detail_background_2);
    Drawable drawable2 = getResources().getDrawable(R.drawable.detail_avatar);
    Drawable drawable3 = getResources().getDrawable(R.drawable.detail_background_2);

    /*iv1.setImageDrawable((getResources().getDrawable(R.mipmap.detail_background_2)).mutate());
    iv1.getDrawable().setAlpha(100);*/
    //或者
  /*  iv1.setImageDrawable((getResources().getDrawable(R.mipmap.detail_background_2)));
    iv1.getDrawable().mutate().setAlpha(100);

    iv2.setImageDrawable((getResources().getDrawable(R.mipmap.detail_background_2)));
    iv3.setImageDrawable((getResources().getDrawable(R.mipmap.detail_background_2)));*/
    //Log.e(TAG, "drawable1,ConstantState:" + drawable1.getConstantState());
    Log.e(TAG, "drawable1,ConstantState:" + drawable1.getConstantState());

    iv1.setImageDrawable(drawable1.mutate());
    iv2.setImageDrawable(drawable2.mutate());
    iv3.setImageDrawable(drawable3.mutate());

    iv1.setImageAlpha(100);

    Log.e(TAG, "iv1,ConstantState:" + iv1.getDrawable());
    Log.e(TAG, "iv2,ConstantState:" + iv2.getDrawable());
    Log.e(TAG, "iv3,ConstantState:" + iv3.getDrawable());

    Log.e(TAG, "iv1,ConstantState:" + iv1.getDrawable().getConstantState());
    Log.e(TAG, "iv2,ConstantState:" + iv2.getDrawable().getConstantState());
    Log.e(TAG, "iv3,ConstantState:" + iv3.getDrawable().getConstantState());

    Log.e(TAG, "drawable1,ConstantState:" + drawable1.getConstantState());
    Log.e(TAG, "drawable2,ConstantState:" + drawable2.getConstantState());
    Log.e(TAG, "drawable3,ConstantState:" + drawable3.getConstantState());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      Log.e(TAG, "iv1,ImageAlpha:" + iv1.getImageAlpha());
      Log.e(TAG, "iv2,ImageAlpha:" + iv2.getImageAlpha());
    }
  }
}
