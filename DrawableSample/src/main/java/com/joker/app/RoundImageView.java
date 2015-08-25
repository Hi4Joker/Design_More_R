package com.joker.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

/**
 * Created by Joker on 2015/8/6.
 */
public class RoundImageView extends ImageView {
  private static final String TAG = RoundImageView.class.getSimpleName();
  /**
   * 图片的类型，圆形or圆角
   */
  private int type;
  private static final int TYPE_CIRCLE = 0;
  private static final int TYPE_ROUND = 1;

  /**
   * 圆角大小的默认值
   */
  private static final int BORDER_RADIUS_DEFAULT = 10;
  /**
   * 圆角的大小
   */
  private int mBorderRadius;

  /**
   * 绘图的Paint
   */
  private Paint mBitmapPaint;
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
  private RectF mRoundRect;
  /**
   * 画布与Bitmap
   */
  private Canvas mCanvas;
  private Bitmap canvasBitmap;
  private Bitmap source;

  public RoundImageView(Context context) {
    super(context);
  }

  public RoundImageView(Context context, AttributeSet attrs) {
    super(context, attrs);

    RoundImageView.this.init(context, attrs);
  }

  public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    RoundImageView.this.init(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    RoundImageView.this.init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);

    if (typedArray == null) return;

    try {
      mBorderRadius = typedArray.getDimensionPixelSize(R.styleable.RoundImageView_borderRadius,
          (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BORDER_RADIUS_DEFAULT,
              getResources().getDisplayMetrics()));
      type = typedArray.getInt(R.styleable.RoundImageView_type, TYPE_CIRCLE);
    } catch (Exception ignored) {
    } finally {
      typedArray.recycle();
    }
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (type == TYPE_CIRCLE) {

      if (source != null) {

        mWidth = source.getWidth() < mWidth ? source.getWidth() : mWidth;
        mRadius = mWidth / 2;
        setMeasuredDimension(mWidth, mWidth);
      } else {

        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        mRadius = mWidth / 2;
        setMeasuredDimension(mWidth, mWidth);
      }
    }
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    RoundImageView.this.initPaint();

    /*圆角*/
    if (type == TYPE_ROUND) mRoundRect = new RectF(0, 0, getWidth(), getHeight());
  }

  /**
   * 初始化BitmapShader
   */
  private void initPaint() {

    Drawable drawable = getDrawable();
    if (drawable == null) {
      return;
    }

    source = RoundImageView.this.drawableToBitmap(drawable);

    /*截取居中bitmap*/
    if (Math.max(source.getWidth(), source.getHeight()) > Math.max(getWidth(), getHeight())) {
      int size = Math.min(source.getWidth(), source.getHeight());
      int left = (source.getWidth() - size) / 2;
      int top = (source.getHeight() - size) / 2;
      source = Bitmap.createBitmap(source, left, top, size, size);
    }

    canvasBitmap =
        Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
    mCanvas = new Canvas(canvasBitmap);

    /*根据source创建shader，并在指定区域绘制source*/
    mBitmapShader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

    /**
     * type == TYPE_ROUND
     * 根据source和view自身大小进行缩放
     */
    float scale = RoundImageView.this.calculateScale(source);
    mMatrix.setScale(scale, scale);
    mBitmapShader.setLocalMatrix(mMatrix);

    //设置Paint与Shader
    mBitmapPaint = new Paint();
    mBitmapPaint.setAntiAlias(true);
    mBitmapPaint.setShader(mBitmapShader);
  }

  private float calculateScale(Bitmap source) {

    float scale = 1.0f;
    if (type == TYPE_CIRCLE) {
      // 拿到bitmap宽或高的小值
      int size = Math.min(source.getWidth(), source.getHeight());
      scale = mWidth * 1.0f / size;
    } else if (type == TYPE_ROUND) {
      // 如果图片的宽或者高与view的宽高不匹配，计算出需要缩放的比例；缩放后的图片的宽高，一定要大于我们view的宽高；所以我们这里取大值；
      scale = Math.max(getMeasuredWidth() * 1.0f / source.getWidth(),
          getMeasuredHeight() * 1.0f / source.getHeight());
    }

    Log.e(TAG, "scale:" + scale);

    return scale;
  }

  /**
   * drawable转bitmap
   */
  private Bitmap drawableToBitmap(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {

      return ((BitmapDrawable) drawable).getBitmap();
    }

    int width = drawable.getIntrinsicWidth();
    int height = drawable.getIntrinsicHeight();
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, width, height);
    drawable.draw(canvas);
    return bitmap;
  }

  @Override protected void onDraw(Canvas canvas) {

    if (getDrawable() == null) {
      return;
    }

    if (type == TYPE_ROUND) {
      mCanvas.drawRoundRect(mRoundRect, mBorderRadius, mBorderRadius, mBitmapPaint);
    } else if (type == TYPE_CIRCLE) {
      mCanvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
    }

    canvas.drawBitmap(canvasBitmap, 0, 0, null);
  }

  public void setmBorderRadius(int mBorderRadius) {
    int pxVal = dp2px(mBorderRadius);
    if (this.mBorderRadius != pxVal) {
      this.mBorderRadius = pxVal;
      invalidate();
    }
  }

  public void setType(int type) {
    if (this.type != type) {
      this.type = type;
      if (this.type != TYPE_ROUND && this.type != TYPE_CIRCLE) {
        this.type = TYPE_CIRCLE;
      }
      requestLayout();
    }
  }

  public int dp2px(int dpVal) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
        getResources().getDisplayMetrics());
  }

  private static final String STATE_INSTANCE = "state_instance";
  private static final String STATE_TYPE = "state_type";
  private static final String STATE_BORDER_RADIUS = "state_border_radius";

  @Override protected Parcelable onSaveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
    bundle.putInt(STATE_TYPE, type);
    bundle.putInt(STATE_BORDER_RADIUS, mBorderRadius);
    return bundle;
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    if (state instanceof Bundle) {
      Bundle bundle = (Bundle) state;
      super.onRestoreInstanceState(((Bundle) state).getParcelable(STATE_INSTANCE));
      this.type = bundle.getInt(STATE_TYPE);
      this.mBorderRadius = bundle.getInt(STATE_BORDER_RADIUS);
    } else {
      super.onRestoreInstanceState(state);
    }
  }
}
