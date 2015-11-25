package com.flappy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * Created by FF on 2015/11/5.
 */
public class Bird {
    /**
     * 鸟在屏幕高度的1/2位置
     */
    private static final float RADIO_POS_HEIGHT = 1 / 2F;
    /**
     * 鸟的宽度 30dp
     */
    private static final int BIRD_SIZE = 30;
    /**
     * 鸟的横坐标
     */
    private int x;
    /**
     * 鸟的纵坐标
     */
    private int y;
    /**
     * 鸟的宽度
     */
    private int mWidth;
    /**
     * 鸟的高度
     */
    private int mHeight;
    /**
     * 鸟的bitmap
     */
    private Bitmap bitmap;
    /**
     * 鸟绘制的范围
     */
    private RectF rect = new RectF();

    public Bird(Context context, int gameWidth, int gameHeight, Bitmap bitmap) {
        this.bitmap = bitmap;
        x = gameWidth / 2 - bitmap.getWidth() / 2;
        y = (int) (gameHeight * RADIO_POS_HEIGHT);
        //计算鸟的宽度和高度
        mWidth = Utils.dpToPx(context, BIRD_SIZE);
        mHeight = (int) (mWidth * 1.0f / bitmap.getWidth() * bitmap.getHeight());
    }

    public void draw(Canvas canvas) {
        rect.set(x, y, x + mWidth, y + mHeight);
        canvas.drawBitmap(bitmap, null, rect, null);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
