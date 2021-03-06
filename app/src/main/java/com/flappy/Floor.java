package com.flappy;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

/**
 * Created by FF on 2015/11/5.
 */
public class Floor {
    /**
     * 地板位置游戏面板高度的4/5到底部
     */
    private static final float FLOOR_Y_POS_RADIO = 4 / 5F;
    /**
     * x坐标
     */
    private int x;
    /**
     * y坐标
     */
    private int y;
    /**
     * 填充物
     */
    private BitmapShader mFloorShader;

    private int mGameWidth;

    private int mGameHeight;

    public Floor(int gameWidth, int gameHeight, Bitmap bitmap) {
        mGameWidth = gameWidth;
        mGameHeight = gameHeight;
        y = (int) (gameHeight * FLOOR_Y_POS_RADIO);
        mFloorShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
    }

    public void draw(Canvas canvas, Paint paint) {
        if (-x > mGameWidth) {
            x = x % mGameWidth;
        }
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        //移动到指定的位置
        canvas.translate(x, y);
        paint.setShader(mFloorShader);
        canvas.drawRect(x, 0, -x + mGameWidth, mGameHeight - y, paint);
        canvas.restore();
        paint.setShader(null);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY(){
        return y;
    }
}
