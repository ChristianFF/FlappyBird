package com.flappy;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by FF on 2015/11/5.
 */
public class Utils {
    public static int dpToPx(Context context, float dp) {
        int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
        return px;
    }
}
