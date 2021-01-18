package io.agora.demo.streaming.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import io.agora.demo.streaming.utils.ScreenUtil;

public class AgoraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static String TAG = AgoraSurfaceView.class.getSimpleName();

    private SurfaceHolder mHolder;

    private Canvas mCanvas;  //声明一张画布
    private Paint mPaint;    //声明一张画笔
    private Rect rectanglePosition = new Rect(0, 0, 0, 0);
    private int cx;
    private int cy;
    private float radius;
    private ScreenUtil screenUtil = new ScreenUtil();

    public AgoraSurfaceView(Context context) {
        super(context);
    }

    public AgoraSurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);
        mHolder = getHolder();
        mPaint = new Paint();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    /**
     * 设置画笔颜色
     * @param color
     */
    public void setColor(int color){
        mPaint.setColor(color);
    }

    /**
     * 设置圆圈圆心坐标和半径
     * @param cx
     * @param cy
     * @param radius
     */
    public int setCircleParam(int cx, int cy, float radius){
        if(cx - radius < 0 || cy - radius < 0 || cy == 0 || cx == 0){
            Log.d(TAG, "setCircleParam: invalid param");
            return -1;
        }

        this.cx = cx;
        this.cy = cy;
        this.radius = radius;

        return 0;
    }

    public int setRectangleParam(int cx, int cy, int width, int height){
        if(cx <= 0 || cy <= 0 || width < 0 || height < 0){
            Log.d(TAG, "setRectangleParam: invalid param: cx = " + String.valueOf(cx) +
                    ", cy = " + String.valueOf(cy) + ", width = " + String.valueOf(width) +
                    ", height = " + String.valueOf(height));
            return -1;
        }
        this.cx = cx + height / 2;
        this.cy = cy + width / 2;

        rectanglePosition.right = cx + height;
        rectanglePosition.left = cx;
        rectanglePosition.top = cy;
        rectanglePosition.bottom = cy + width;

        return 0;
    }

    public void drawCircle(){
        mCanvas = mHolder.lockCanvas(null);   //获取画布对象
        if(mCanvas == null){
            mHolder.unlockCanvasAndPost(mCanvas);
            Log.d(TAG, "Canvas is null");
            return;
        }
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mCanvas.drawCircle(cx, cy, radius, mPaint);
        mHolder.unlockCanvasAndPost(mCanvas);  //把画布显示在屏幕上
        Log.d(TAG, "drawCircle");
    }

    public void drawRectangle(){
        mCanvas = mHolder.lockCanvas(null);   //获取画布对象
        if(mCanvas == null){
            mHolder.unlockCanvasAndPost(mCanvas);
            Log.d(TAG, "Canvas is null");
            return;
        }
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mCanvas.drawRect(rectanglePosition, mPaint);
        mHolder.unlockCanvasAndPost(mCanvas);  //把画布显示在屏幕上
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //to do nonthing
    }

    public SurfaceHolder getSurfaceHolder(){
        return mHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //do nonthing
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}
