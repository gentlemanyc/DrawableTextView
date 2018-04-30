package com.example.cc.drawabletextview;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import java.lang.reflect.Field;

/**
 * Created by YuanChao on 2018/4/21.
 */

public class DrawableCompatHelper {
    public static int[] getColors(GradientDrawable drawable) {
        if (drawable == null)
            return new int[]{};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getColors();
        } else {
            try {
                Field field = GradientDrawable.class.getDeclaredField("mGradientState");
                field.setAccessible(true);
                Field colorsField = field.get(drawable).getClass().getDeclaredField("mGradientColors");
                colorsField.setAccessible(true);
                return (int[]) colorsField.get(field.get(drawable));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return new int[]{};
    }

    public static ColorStateList getColorStateList(GradientDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return drawable.getColor();
        } else {
            try {
                Field field = GradientDrawable.class.getDeclaredField("mGradientState");
                field.setAccessible(true);
                Field colorsField = field.get(drawable).getClass().getDeclaredField("mColorStateList");
                colorsField.setAccessible(true);
                return (ColorStateList) colorsField.get(field.get(drawable));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static float[] getPositions(GradientDrawable drawable, boolean hasCenterColor) {
        if (hasCenterColor) {
            float[] mPositions = new float[3];
            mPositions[0] = 0.0f;
            float mCenterX = 0.5f;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mCenterX = drawable.getGradientCenterX();
            } else {
                try {
                    Field field = GradientDrawable.class.getDeclaredField("mGradientState");
                    field.setAccessible(true);
                    Field colorsField = field.get(drawable).getClass().getDeclaredField("mCenterX");
                    colorsField.setAccessible(true);
                    mCenterX = (float) colorsField.get(field.get(drawable));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            // Since 0.5f is default value, try to take the one that isn't 0.5f
            mPositions[1] = mCenterX;
            mPositions[2] = 1f;
            return mPositions;
        }
        return null;
    }

}
