package com.example.cc.drawabletextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;


import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by YuanChao on 2018/4/12.
 * <p>
 * <h1>文字能显示drawable的TextView</h1>
 * 您需要设置属性textDrawable来指定文字的一个drawable<br/><br/>
 * <strong>支持的Drawable有：</strong>
 * <li>{@link ColorDrawable}</li>
 * <li>{@link GradientDrawable}</li>
 * <li>{@link BitmapDrawable}</li>
 * <li>{@link StateListDrawable}：即selector,selector的item的drawable以必须是以上三种支持的drawable</li></li>
 * </p>
 */

public class DrawableTextView extends android.support.v7.widget.AppCompatTextView {
    private static final String TAG = "GradientTextView";
    boolean inflated;
    /**
     * 它应该是一个selector
     */
    private Drawable textColorDrawable;
    private SparseArray<LinearGradient> mDrawableStateShader;
    private RectF mRect = new RectF();
    /**
     * 缓存shader，避免在列表中使用时创建大量对象。
     */
    private HashMap<String, Shader> mShaderHashMap = new HashMap<>();

    public DrawableTextView(Context context) {
        super(context);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);
            textColorDrawable = ta.getDrawable(R.styleable.DrawableTextView_textDrawable);
            ta.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() != 0 && !inflated) {
            resetColorState();
            inflated = true;
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        boolean isFocused = isFocused();
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (isFocused != focused)
            resetColorState();
    }

    /**
     * 设置文字Drawable
     *
     * @param textColorDrawable
     */
    public void setTextColorDrawable(Drawable textColorDrawable) {
        this.textColorDrawable = textColorDrawable;
        mShaderHashMap.clear();
        setTextDrawable(textColorDrawable);
    }

    private void setTextDrawable(Drawable drawable) {
        textColorDrawable = drawable;
        Shader shader = null;
        if (textColorDrawable instanceof StateListDrawable) {
            textColorDrawable.setState(getDrawableState());
            Drawable curDrawable = textColorDrawable.getCurrent();
            shader = mShaderHashMap.get(curDrawable.getClass().getName());
            if (shader != null) {
                setTextColor(Color.BLACK);
                getPaint().setShader(shader);
                return;
            }
            if (curDrawable instanceof ColorDrawable) {
                getPaint().setShader(null);
                setTextColor(((ColorDrawable) curDrawable).getColor());
            } else if (curDrawable instanceof GradientDrawable) {
                setTextColor(Color.BLACK);
                setGradientColor((GradientDrawable) curDrawable);
            } else if (curDrawable instanceof BitmapDrawable) {
                setTextColor(Color.BLACK);
                getPaint().setShader(shader = new BitmapShader(((BitmapDrawable) curDrawable).getBitmap(), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                mShaderHashMap.put(curDrawable.getClass().getName(), shader);
            }
        } else if (textColorDrawable instanceof GradientDrawable) {
            setTextColor(Color.BLACK);
            shader = mShaderHashMap.get(textColorDrawable.getClass().getName());
            if (shader != null) {
                getPaint().setShader(shader);
                return;
            }
            setGradientColor((GradientDrawable) textColorDrawable);
        }
    }

    private void resetColorState() {
        setTextDrawable(textColorDrawable);
    }

    private void setGradientColor(GradientDrawable drawable) {
        int[] colors = DrawableCompatHelper.getColors(drawable);
        Log.d(TAG, "colors:" + Arrays.toString(colors) + ",width:" + getMeasuredWidth());
        if (getMeasuredWidth() == 0) {
            int a = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            measure(a, a);
        }
        Shader shader = null;
        if (colors != null && colors.length > 0) {
            if (colors.length == 1) {
                colors = new int[]{colors[0], colors[0]};
            }
            getPaint().setShader(shader = new LinearGradient(0, 0, getMeasuredWidth(), 0,
                    colors, DrawableCompatHelper.getPositions(drawable,
                    colors.length == 3), Shader.TileMode.CLAMP));
            mShaderHashMap.put(drawable.getClass().getName(), shader);
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        boolean isPressed = isPressed();
        super.setPressed(pressed);
        if (isPressed != pressed)
            resetColorState();
    }

    @Override
    public void setSelected(boolean selected) {
        boolean isSelected = isSelected();
        super.setSelected(selected);
        if (isSelected != selected)
            resetColorState();
    }

}

