package com.joker.app.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.joker.app.R;

/**
 * Created by Joker on 2015/8/7.
 */
public class CircleView extends ImageView {

  private static final String TAG = CircleView.class.getSimpleName();
  private static final int COLORDRAWABLE_DIMENSION = 2;
  private static final int DEFAULT_BORDER_WIDTH = 0;
  private static final int DEFAULT_BORDER_ALPHA = 255;
  private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

  /**
   * 绘图的Paint
   */
  private Paint mBitmapPaint;
  private Paint mBorderPaint;
  /**
   * 圆角的半径
   */
  private int mRadius;
  /**
   * 3x3 矩阵，主要用于缩小放大
   */
  private Matrix mMatrix = new Matrix();
  /**
   * 渲染图像，使用图像为绘制图形着色
   */
  private BitmapShader mBitmapShader;
  /**
   * view的宽度
   */
  private int mWidth;
  /**
   * 画布与Bitmap
   */
  private Canvas tempCanvas;
  private Bitmap tempBitmap;
  private Bitmap sourceBitmap;
  /**
   * 边框宽度,颜色，透明
   */
  private int mBorderWidth;
  private float mBorderOffset;
  private int mBorderColor;
  private int mBorderAlpha = 255;
  /**
   * 两个初始化标志位
   */
  private boolean flag;
  private boolean mSetupPending;

  public CircleView(Context context) {
    super(context);
    CircleView.this.initTypeArray(context, null);
    CircleView.this.init();
  }

  public CircleView(Context context, AttributeSet attrs) {
    super(context, attrs);
    CircleView.this.initTypeArray(context, attrs);
    CircleView.this.init();
  }

  public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    CircleView.this.initTypeArray(context, attrs);
    CircleView.this.init();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public CircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);

    CircleView.this.initTypeArray(context, attrs);
    CircleView.this.init();
  }

  private void initTypeArray(Context context, AttributeSet attrs) {
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleView, 0, 0);

    if (typedArray == null) return;

    try {
      mBorderWidth = typedArray.getDimensionPixelSize(R.styleable.CircleView_cir_border_width,
          DEFAULT_BORDER_WIDTH);
      mBorderColor =
          typedArray.getColor(R.styleable.CircleView_cir_border_color, DEFAULT_BORDER_COLOR);
      mBorderAlpha =
          typedArray.getInteger(R.styleable.CircleView_cir_border_alpha, DEFAULT_BORDER_ALPHA);
    } catch (Exception ignored) {
    } finally {
      typedArray.recycle();
    }
  }

  private void init() {
    super.setScaleType(ScaleType.CENTER_CROP);

    this.flag = true;

    if (mSetupPending) {
      CircleView.this.setup();
      mSetupPending = false;
    }
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    Log.e(TAG, "onMeasure");

    if (sourceBitmap != null && mWidth != 0) {
      mWidth = Math.min(Math.min(sourceBitmap.getWidth(), sourceBitmap.getHeight()), mWidth);
      mRadius = (mWidth - mBorderWidth) / 2;
      setMeasuredDimension(mWidth, mWidth);
    } else {
      mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
      mRadius = (mWidth - mBorderWidth) / 2;
      setMeasuredDimension(mWidth, mWidth);
    }
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    Log.e(TAG, "onSizeChanged");
    CircleView.this.setup();
  }

  private void setup() {

    /*protect setImageDrawable*/
    if (!flag) {
      mSetupPending = true;
      return;
    }

    if (sourceBitmap == null || mWidth == 0) {
      return;
    }

    tempBitmap =
        Bitmap.createBitmap(getMeasuredWidth() == 0 ? COLORDRAWABLE_DIMENSION : getMeasuredWidth(),
            getMeasuredHeight() == 0 ? COLORDRAWABLE_DIMENSION : getMeasuredHeight(),
            Bitmap.Config.ARGB_4444);
    tempCanvas = new Canvas(tempBitmap);

    Log.e(TAG, "tempBitmap width:"
        + tempBitmap.getWidth()
        + "tempBitmap height:"
        + tempBitmap.getHeight());
    Log.e(TAG, "tempCanvas width:"
        + tempCanvas.getWidth()
        + "tempCanvas height:"
        + tempCanvas.getHeight());

    sourceBitmap = clipSource(sourceBitmap);
    /*根据source创建shader，并在指定区域绘制source*/
    mBitmapShader = new BitmapShader(sourceBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    /*根据source和view自身大小进行缩放*/
    CircleView.this.calculateMatrix(sourceBitmap);

    //设置Paint与Shader
    mBitmapPaint = new Paint();
    mBitmapPaint.setAntiAlias(true);
    mBitmapPaint.setShader(mBitmapShader);

    if (mBorderWidth != 0) {
      //设置BorderPaint
      mBorderPaint = new Paint();
      mBorderPaint.setAntiAlias(true);
      mBorderPaint.setStyle(Paint.Style.STROKE);
      mBorderPaint.setStrokeWidth(mBorderWidth / 2);
      mBorderPaint.setColor(mBorderColor);
      mBorderPaint.setAlpha(mBorderAlpha);
      this.mBorderOffset = mBorderWidth / 4;
    }
  }

  private Bitmap clipSource(Bitmap source) {
    /*截取居中bitmap*/
    if (Math.max(source.getWidth(), source.getHeight()) > Math.max(getWidth(), getHeight())) {
      int size = Math.min(source.getWidth(), source.getHeight());
      int left = (source.getWidth() - size) / 2;
      int top = (source.getHeight() - size) / 2;
      return Bitmap.createBitmap(source, left, top, size, size);
    }
    return source;
  }

  /**
   * drawable转bitmap
   */
  private Bitmap drawableToBitmap(Drawable drawable) {

    if (drawable == null) {
      return null;
    }

    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    try {
      Bitmap bitmap;

      if (drawable instanceof ColorDrawable) {
        bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION,
            Bitmap.Config.ARGB_8888);
      } else {
        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888);
      }

      Canvas canvas = new Canvas(bitmap);
      drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
      drawable.draw(canvas);
      return bitmap;
    } catch (OutOfMemoryError e) {
      return null;
    }
  }

  private float calculateMatrix(Bitmap source) {

    float scale = 1.0f;
    // 拿到bitmap宽或高的小值
    int size = Math.min(source.getWidth(), source.getHeight());
    scale = (mWidth - mBorderWidth / 2) * 1.0f / size;

    mMatrix.setScale(scale, scale);
    /*移动距离等于mBorderOffset =mBorderPaint.getStrokeWidth() / 2 */
    mMatrix.postTranslate(mBorderWidth / 4, mBorderWidth / 4);
    mBitmapShader.setLocalMatrix(mMatrix);

    Log.e(TAG, "scale:" + scale);

    return scale;
  }

  @Override protected void onDraw(Canvas canvas) {

    if (getDrawable() == null) {
      return;
    }

    tempCanvas.drawCircle(getWidth() / 2, getHeight() / 2, mRadius, mBitmapPaint);
    /*回收BitmapShader中的sourceBitmap，然而这并没有什么乱用*/
    if (sourceBitmap != null && !sourceBitmap.isRecycled()) sourceBitmap.recycle();
    if (mBorderPaint != null) {
      tempCanvas.drawCircle(getWidth() / 2, getHeight() / 2, mRadius + mBorderOffset, mBorderPaint);
    }

    canvas.drawBitmap(tempBitmap, 0, 0, null);
  }

  @Override public void setImageDrawable(Drawable drawable) {
    super.setImageDrawable(drawable);
    sourceBitmap = drawableToBitmap(drawable);
    CircleView.this.setup();
  }

  @Override public void setImageBitmap(Bitmap bitmap) {
    super.setImageBitmap(bitmap);
    sourceBitmap = bitmap;
    CircleView.this.setup();
  }

  @Override public void setImageResource(@DrawableRes int resId) {
    super.setImageResource(resId);
    sourceBitmap = drawableToBitmap(getDrawable());
    CircleView.this.setup();
  }

  @Override public void setImageURI(Uri uri) {
    super.setImageURI(uri);
    sourceBitmap = drawableToBitmap(getDrawable());
    CircleView.this.setup();
  }
}
