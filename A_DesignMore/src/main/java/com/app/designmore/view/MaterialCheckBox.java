package com.app.designmore.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.app.designmore.Constants;
import com.app.designmore.R;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by Hanks on 2015/6/30.
 *
 * fixed by Joker
 */
public class MaterialCheckBox extends View {

  private static final String TAG = MaterialCheckBox.class.getSimpleName();
  private Paint paintBlue;
  private Paint paintWithe;
  private Paint paintCenter;

  private int borderColor = Color.GRAY;     //边框颜色
  private int backgroundColor = Color.RED; //填充颜色
  private int doneShapeColor = Color.WHITE; //对号颜色

  private int baseWidth;                    //checkbox 边框宽度
  private int borderWidth;
  private int width, height;                //控件宽高

  private float[] points = new float[8];    //对号的4个点的坐标

  private int DURATION = Constants.REVEAL_DURATION / 2;               //动画时长
  private boolean checked;                  //选择状态
  private float correctProgress;            //划对号的进度

  private boolean drawReacting;
  private boolean isAnim;
  private OnCheckedChangeListener listener;

  private float padding;                    //内切圆的边据边框的距离

  public MaterialCheckBox(Context context) {
    super(context, null);
    MaterialCheckBox.this.initTypeArray(context, null);
  }

  public MaterialCheckBox(Context context, AttributeSet attrs) {
    super(context, attrs, 0);
    MaterialCheckBox.this.initTypeArray(context, attrs);
  }

  public MaterialCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    MaterialCheckBox.this.initTypeArray(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MaterialCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  private void initTypeArray(Context context, AttributeSet attrs) {

    TypedArray typedArray =
        context.obtainStyledAttributes(attrs, R.styleable.MaterialCheckBox, 0, 0);

    if (typedArray == null) return;

    try {
      borderColor = typedArray.getColor(R.styleable.MaterialCheckBox_border_color,
          getResources().getColor(android.R.color.darker_gray));

      backgroundColor = typedArray.getColor(R.styleable.MaterialCheckBox_background_color,
          getResources().getColor(android.R.color.holo_red_dark));

      doneShapeColor =
          typedArray.getColor(R.styleable.MaterialCheckBox_doneShape_color, Color.WHITE);

      borderWidth = baseWidth =
          typedArray.getDimensionPixelOffset(R.styleable.MaterialCheckBox_border_width, 2);
    } catch (Exception ignored) {
    } finally {
      typedArray.recycle();
    }

    paintBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
    paintBlue.setColor(borderColor);
    paintBlue.setStrokeWidth(borderWidth);

    paintWithe = new Paint(Paint.ANTI_ALIAS_FLAG);
    paintWithe.setColor(doneShapeColor);
    paintWithe.setStrokeWidth(borderWidth);

    paintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
    paintCenter.setColor(getResources().getColor(android.R.color.white));
    paintCenter.setStrokeWidth(borderWidth);

    setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        setChecked(!isChecked());
      }
    });
    drawReacting = true;
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    height = width = Math.max(w, h);

    points[0] = 101 / 378f * width;
    points[1] = 0.5f * width;

    points[2] = 163 / 378f * width;
    points[3] = 251 / 378f * width;

    points[4] = 149 / 378f * width;
    points[5] = 250 / 378f * width;

    points[6] = 278 / 378f * width;
    points[7] = 122 / 378f * width;

    padding = 57 / 378f * width;
  }

  /**
   * draw checkbox
   */
  @Override protected void onDraw(Canvas canvas) {
    RectF rect = new RectF(padding, padding, width - padding, height - padding);
    canvas.drawRoundRect(rect, baseWidth, baseWidth, paintBlue);
    if (drawReacting) {
      canvas.drawRect(padding + borderWidth, padding + borderWidth, width - padding - borderWidth,
          height - padding - borderWidth, paintCenter);
    } else {
      //画对号
      if (correctProgress > 0) {
        if (correctProgress < 1 / 3f) {
          float x = points[0] + (points[2] - points[0]) * correctProgress;
          float y = points[1] + (points[3] - points[1]) * correctProgress;
          canvas.drawLine(points[0], points[1], x, y, paintWithe);
        } else {
          float x = points[4] + (points[6] - points[4]) * correctProgress;
          float y = points[5] + (points[7] - points[5]) * correctProgress;
          canvas.drawLine(points[0], points[1], points[2], points[3], paintWithe);
          canvas.drawLine(points[4], points[5], x, y, paintWithe);
        }
      }
    }
  }

  public void setBackgroundColor(int backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public void setDoneShapeColor(int doneShapeColor) {
    this.doneShapeColor = doneShapeColor;
    paintWithe.setColor(doneShapeColor);
  }

  public void setBorderColor(int borderColor) {
    this.borderColor = borderColor;
  }

  public void setBorderWidth(int baseWidth) {
    this.baseWidth = baseWidth;
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
    if (checked) {
      showRect();
    } else {
      hideCorrect();
    }
  }

  private void hideRect() {
    if (isAnim) {
      return;
    }
    isAnim = true;
    drawReacting = true;
    ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        float p = (float) animation.getAnimatedValue();
        float c = 1f - p;
        borderWidth = (int) (baseWidth + c * (width - baseWidth));
        paintBlue.setColor(evaluate(c, borderColor, backgroundColor));
        invalidate();
        if (p >= 1) {
          isAnim = false;
          if (listener != null) {
            checked = false;
            listener.onCheckedChanged(MaterialCheckBox.this, checked);
          }
        }
      }
    });
    va.start();
  }

  private void showRect() {
    if (isAnim) {
      return;
    }
    isAnim = true;
    drawReacting = true;
    ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        float p = (float) animation.getAnimatedValue();
        borderWidth = (int) (10 + p * (width - 10));
        paintBlue.setColor(evaluate(p, borderColor, backgroundColor));
        invalidate();
        if (p >= 1) {
          isAnim = false;
          drawReacting = false;
          showCorrect();
        }
      }
    });
    va.start();
  }

  private void showCorrect() {
    if (isAnim) {
      return;
    }
    isAnim = true;
    correctProgress = 0;
    drawReacting = false;
    ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        correctProgress = (float) animation.getAnimatedValue();
        invalidate();
        if (correctProgress >= 1) {
          isAnim = false;
          if (listener != null) {
            checked = true;
            listener.onCheckedChanged(MaterialCheckBox.this, checked);
          }
        }
      }
    });
    va.start();
  }

  private void hideCorrect() {
    if (isAnim) {
      return;
    }
    isAnim = true;
    correctProgress = 1;
    drawReacting = false;
    ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        float p = (float) animation.getAnimatedValue();
        correctProgress = 1f - p;
        invalidate();
        if (p >= 1) {
          isAnim = false;
          hideRect();
        }
      }
    });
    va.start();
  }

  public void setOnCheckedChangedListener(OnCheckedChangeListener listener) {
    this.listener = listener;
  }

  public interface OnCheckedChangeListener {
    void onCheckedChanged(MaterialCheckBox materialCheckBox, boolean isChecked);
  }

  private int evaluate(float fraction, int startValue, int endValue) {
    int startInt = startValue;
    int startA = (startInt >> 24) & 0xff;
    int startR = (startInt >> 16) & 0xff;
    int startG = (startInt >> 8) & 0xff;
    int startB = startInt & 0xff;

    int endInt = endValue;
    int endA = (endInt >> 24) & 0xff;
    int endR = (endInt >> 16) & 0xff;
    int endG = (endInt >> 8) & 0xff;
    int endB = endInt & 0xff;
    return ((startA + (int) (fraction * (endA - startA))) << 24) | ((startR + (int) (fraction * (
        endR
            - startR))) << 16) | ((startG + (int) (fraction * (endG - startG))) << 8) | ((startB
        + (int) (fraction * (endB - startB))));
  }
}
