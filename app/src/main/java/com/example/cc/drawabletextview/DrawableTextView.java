package com.example.cc.drawabletextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.LinearGradient;
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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


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
    private HashMap<String, ViewState> mShaderHashMap = new HashMap<>();

    public DrawableTextView(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        post(refreshRunnable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() != 0 && !inflated && textColorDrawable != null) {
            resetColorState();
            inflated = true;
        }
    }

    /**
     * 设置文字Drawable
     *
     * @param textColorDrawable
     */
    public void setTextDrawable(Drawable textColorDrawable) {
        this.textColorDrawable = textColorDrawable;
        mShaderHashMap.clear();
        setTextDrawableInner(textColorDrawable);
    }

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (textColorDrawable != null) {
                if (textColorDrawable instanceof GradientDrawable || textColorDrawable instanceof StateListDrawable)
                    resetColorState();
            }
        }
    };

    private void setTextDrawableInner(Drawable drawable) {
        textColorDrawable = drawable;
        Shader shader = null;
        if (textColorDrawable instanceof StateListDrawable) {
            textColorDrawable.setState(getDrawableState());
            Drawable curDrawable = textColorDrawable.getCurrent();
            shader = getCachedShader();
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
                getPaint().setShader(shader = setBitmapDrawable((BitmapDrawable) curDrawable));
                ViewState viewState = new ViewState(shader, getMeasuredWidth());
                mShaderHashMap.put(curDrawable.getClass().getName(), viewState);
            }
        } else if (textColorDrawable instanceof GradientDrawable) {
            setTextColor(Color.BLACK);
            shader = getCachedShader();
            if (shader != null) {
                getPaint().setShader(shader);
                return;
            }
            setGradientColor((GradientDrawable) textColorDrawable);
        } else if (textColorDrawable instanceof BitmapDrawable) {
            shader = setBitmapDrawable((BitmapDrawable) textColorDrawable);
            mShaderHashMap.put(textColorDrawable.getClass().getName(), new ViewState(shader, getMeasuredWidth()));
        }
    }

    private Shader getCachedShader() {
        ViewState viewState = mShaderHashMap.get(textColorDrawable.getClass().getName());
        if (textColorDrawable != null && viewState != null) {
            return viewState.getShader(getMeasuredWidth());
        }
        return null;
    }

    private void resetColorState() {
        setTextDrawableInner(textColorDrawable);
    }

    /**
     * 设置BitmapShader
     *
     * @param bitmapDrawable
     * @return
     */
    private Shader setBitmapDrawable(BitmapDrawable bitmapDrawable) {
        Shader shader = null;
        setTextColor(Color.BLACK);
        getPaint().setShader(shader = new BitmapShader((bitmapDrawable).getBitmap(),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        return shader;
    }

    /**
     * 设置 LinearGradient Shader
     *
     * @param drawable
     */
    private void setGradientColor(GradientDrawable drawable) {
        int[] colors = DrawableCompatHelper.getColors(drawable);
        Log.d(TAG, "colors:" + Arrays.toString(colors) + ",width:" + getMeasuredWidth());
        if (getMeasuredWidth() == 0) {
            int a = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            measure(a, a);
        }
        Shader shader = null;
        if (getMeasuredWidth() != 0 && colors != null && colors.length > 0) {
            if (colors.length == 1) {
                colors = new int[]{colors[0], colors[0]};
            }
            getPaint().setShader(shader = new LinearGradient(0, 0, getMeasuredWidth(), 0,
                    colors, DrawableCompatHelper.getPositions(drawable,
                    colors.length == 3), Shader.TileMode.CLAMP));
            mShaderHashMap.put(drawable.getClass().getName(), new ViewState(shader, getMeasuredWidth()));
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

    private final class ViewState {
        Shader shader;
        int initWidth;

        public Shader getShader(int width) {
            if (width != initWidth)
                return null;
            return shader;
        }

        public ViewState(Shader shader, int layoutChanted) {
            this.shader = shader;
            this.initWidth = layoutChanted;
        }
    }

}

