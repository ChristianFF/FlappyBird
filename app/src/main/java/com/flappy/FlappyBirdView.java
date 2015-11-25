package com.flappy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by FF on 2015/11/5.
 */
public class FlappyBirdView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private int mWidth;
    private int mHeight;
    private RectF mGamePanelRect = new RectF();

    //背景图片
    private Bitmap mBackGround = null;

    private SurfaceHolder mHolder = null;
    private Canvas mCanvas = null;
    private Paint mPaint = null;
    private Thread drawThread = null;
    private boolean isRunning = false;
    /**
     * 小鸟
     */
    private Bird mBird = null;
    private Bitmap mBirdBitmap = null;
    /**
     * 地板
     */
    private Floor mFloor = null;
    private Bitmap mFloorBitmap = null;
    /**
     * 移动速度
     */
    private int mSpeed;
    /**
     * 管道
     */
    private Bitmap mPipeTopBitmap;
    private Bitmap mPipeBottomBitmap;
    private RectF mPipeRect;
    private int mPipeWidth;
    /**
     * 管道的宽度 60dp
     */
    private static final int PIPE_WIDTH = 60;

    private List<Pipe> mPipes = new ArrayList<Pipe>();
    /**
     * 分数
     */
    private final int[] mNums = new int[]{R.drawable.n0, R.drawable.n1,
            R.drawable.n2, R.drawable.n3, R.drawable.n4, R.drawable.n5,
            R.drawable.n6, R.drawable.n7, R.drawable.n8, R.drawable.n9};
    private Bitmap[] mNumBitmap;
    private int mGrade = 0;
    /**
     * 单个数字的高度的1/15
     */
    private static final float RADIO_SINGLE_NUM_HEIGHT = 1 / 15f;
    /**
     * 单个数字的宽度
     */
    private int mSingleGradeWidth;
    /**
     * 单个数字的高度
     */
    private int mSingleGradeHeight;
    /**
     * 单个数字的范围
     */
    private RectF mSingleNumRectF;
    /**
     * 记录游戏的状态
     */
    private GameStatus mStatus = GameStatus.WAITTING;
    /**
     * 触摸上升的距离，因为是上升，所以为负值
     */
    private static final int TOUCH_UP_SIZE = -17;
    /**
     * 将上升的距离转化为px；这里多存储一个变量，变量在run中计算
     */
    private final int mBirdUpDis = Utils.dpToPx(getContext(), TOUCH_UP_SIZE);

    private int mTmpBirdDis;
    /**
     * 鸟自动下落的距离
     */
    private final int mAutoDownSpeed = Utils.dpToPx(getContext(), 2);
    /**
     * 两个管道间距离
     */
    private final int PIPE_DIS_BETWEEN_TWO = Utils.dpToPx(getContext(), 200);
    /**
     * 记录移动的距离，达到 PIPE_DIS_BETWEEN_TWO 则生成一个管道
     */
    private int mTmpMoveDistance;
    /**
     * 记录需要移除的管道
     */
    private List<Pipe> mNeedRemovePipe = new ArrayList<Pipe>();

    private int mRemovedPipe = 0;

    public FlappyBirdView(Context context) {
        this(context, null);
    }

    public FlappyBirdView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlappyBirdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        //设置背景透明
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        //设置获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        //设置屏幕常亮
        setKeepScreenOn(true);
        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        //获取背景图片
        mBackGround = BitmapFactory.decodeResource(getResources(), R.drawable.bg1);
        //获取小鸟图片
        mBirdBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b1);
        //获取地板图片
        mFloorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floor_bg2);
        // 初始化速度
        mSpeed = Utils.dpToPx(getContext(), 4);
        //获取管道图片
        mPipeTopBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.g2);
        mPipeBottomBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.g1);
        //设置管道宽度
        mPipeWidth = Utils.dpToPx(getContext(), PIPE_WIDTH);
        //获取分数图片
        mNumBitmap = new Bitmap[mNums.length];
        for (int i = 0; i < mNumBitmap.length; i++) {
            mNumBitmap[i] = BitmapFactory.decodeResource(getResources(), mNums[i]);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mGamePanelRect.set(0, 0, mWidth, mHeight);
        mBird = new Bird(getContext(), mWidth, mHeight, mBirdBitmap);
        mFloor = new Floor(mWidth, mHeight, mFloorBitmap);
        // 初始化管道范围
        mPipeRect = new RectF(0, 0, mPipeWidth, mHeight);
        Pipe pipe = new Pipe(getContext(), mWidth, mHeight, mPipeTopBitmap, mPipeBottomBitmap);
        mPipes.add(pipe);
        // 初始化分数
        mSingleGradeHeight = (int) (h * RADIO_SINGLE_NUM_HEIGHT);
        mSingleGradeWidth = (int) (mSingleGradeHeight * 1.0f
                / mNumBitmap[0].getHeight() * mNumBitmap[0].getWidth());
        mSingleNumRectF = new RectF(0, 0, mSingleGradeWidth, mSingleGradeHeight);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        isRunning = true;
        drawThread = new Thread(this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                long start = System.currentTimeMillis();
                solveLogic();
                drawScreen();
                long end = System.currentTimeMillis();
                if (end - start < 50) {
                    sleep(50 - (end - start));
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void solveLogic() {
        switch (mStatus) {
            case RUNNING:
                mGrade = 0;
                // 更新我们地板绘制的x坐标，地板移动
                mFloor.setX(mFloor.getX() - mSpeed);
                // 管道移动
                for (Pipe pipe : mPipes) {
                    if (pipe.getX() < -mPipeWidth) {
                        mNeedRemovePipe.add(pipe);
                        mRemovedPipe++;
                        continue;
                    }
                    pipe.setX(pipe.getX() - mSpeed);
                }
                //移除管道
                mPipes.removeAll(mNeedRemovePipe);

                // 管道
                mTmpMoveDistance += mSpeed;
                // 生成一个管道
                if (mTmpMoveDistance >= PIPE_DIS_BETWEEN_TWO) {
                    Pipe pipe = new Pipe(getContext(), getWidth(), getHeight(),
                            mPipeTopBitmap, mPipeBottomBitmap);
                    mPipes.add(pipe);
                    mTmpMoveDistance = 0;
                }
                //默认下落，点击时瞬间上升
                mTmpBirdDis += mAutoDownSpeed;
                mBird.setY(mBird.getY() + mTmpBirdDis);
                // 计算分数
                mGrade += mRemovedPipe;
                for (Pipe pipe : mPipes) {
                    if (pipe.getX() + mPipeWidth < mBird.getX()) {
                        mGrade++;
                    }
                }

                checkGameOver();
                break;

            case OVER: // 鸟落下
                // 如果鸟还在空中，先让它掉下来
                if (mBird.getY() < mFloor.getY() - mBird.getWidth()) {
                    mTmpBirdDis += mAutoDownSpeed;
                    mBird.setY(mBird.getY() + mTmpBirdDis);
                } else {
                    mStatus = GameStatus.WAITTING;
                    initPos();
                }
                break;
            default:
                break;
        }
    }

    private void checkGameOver() {
        // 如果触碰地板
        if (mBird.getY() > mFloor.getY() - mBird.getHeight()) {
            mStatus = GameStatus.OVER;
        }
        // 如果撞到管道
        for (Pipe wall : mPipes) {
            //已经穿过的
            if (wall.getX() + mPipeWidth < mBird.getX()) {
                continue;
            }
            if (wall.touchBird(mBird)) {
                mStatus = GameStatus.OVER;
                break;
            }
        }
    }

    /**
     * 重置鸟的位置等数据
     */
    private void initPos() {
        mPipes.clear();
        mNeedRemovePipe.clear();
        //重置鸟的位置
        mBird.setY(mHeight / 2);
        //重置下落速度
        mTmpBirdDis = 0;
        mTmpMoveDistance = 0;
        mRemovedPipe = 0;
        mGrade = 0;
    }

    private void drawScreen() {
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {
                //绘制背景
                mCanvas.drawBitmap(mBackGround, null, mGamePanelRect, null);
                //绘制小鸟
                mBird.draw(mCanvas);
                //绘制地板
                mFloor.draw(mCanvas, mPaint);
                //绘制管道
                for (Pipe pipe : mPipes) {
                    pipe.draw(mCanvas, mPipeRect);
                }
                //绘制分数
                String grade = mGrade + "";
                mCanvas.save(Canvas.MATRIX_SAVE_FLAG);
                mCanvas.translate(mWidth / 2 - grade.length() * mSingleGradeWidth / 2,
                        1f / 8 * mHeight);
                // draw single num one by one
                for (int i = 0; i < grade.length(); i++) {
                    String numStr = grade.substring(i, i + 1);
                    int num = Integer.valueOf(numStr);
                    mCanvas.drawBitmap(mNumBitmap[num], null, mSingleNumRectF, null);
                    mCanvas.translate(mSingleGradeWidth, 0);
                }
                mCanvas.restore();
            }
        } catch (Exception ex) {
            //Nothing
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            switch (mStatus) {
                case WAITTING:
                    mStatus = GameStatus.RUNNING;
                    mTmpBirdDis = mBirdUpDis;
                    break;
                case RUNNING:
                    mTmpBirdDis = mBirdUpDis;
                    break;
            }
        }
        return true;
    }
}
