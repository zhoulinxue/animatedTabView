package org.zhx.commom.widgets;

import android.view.View;

public class WidgetUtil {
    public static int measureWidth(int measureSpec, int width) {
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        //设置一个默认值，就是这个View的默认宽度为500，这个看我们自定义View的要求
        int result = specSize;
        if (specMode == View.MeasureSpec.AT_MOST) {//相当于我们设置为wrap_content
            result = width;
        } else if (specMode == View.MeasureSpec.EXACTLY) {//相当于我们设置为match_parent或者为一个具体的值
            result = specSize;
        }
        return result;
    }

    public static int measureHeight(int measureSpec, int height) {
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        int result = specSize;
        if (specMode == View.MeasureSpec.AT_MOST) {
            result = height;
        } else if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }
}
