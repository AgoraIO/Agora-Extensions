package io.agora.demo.streaming.activities;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.agora.demo.streaming.R;
import io.agora.demo.streaming.presenter.LiveStreamingPresenter;
import io.agora.demo.streaming.sdkwrapper.CameraCaptureObserverHandlerImpl;
import io.agora.demo.streaming.ui.AgoraSurfaceView;

/**
 * added by nianji tang 2020-10-15
 */
class ScreenTouchImpl implements View.OnTouchListener {
    private static final double ZOOM_FACTOR = 0.2;
    private static final double FINGER_DISTANCE_INTERVAL = 10;
    private static final float CIRCLE_RADIUS = 100;
    private static final int DRAW_INERVAL_TIME_MS = 100;
    private static final int DRAW_INTERVAL_MS = 4000;
    private static final int POINT_MOVE_MIN_CNT = 5;
    private static final int AUTO_FOCUS_REFRESH_INTERVAL_TIME = 500;

    //even type
    private static final String POINT_DOWN = "point_down";
    private static final String POINT_MOVE = "point_move";
    private static final String POINT_UP = "point_up";
    private static final String POINTS_DOWN = "points_down";

    private List<String> eventList = new ArrayList<String>();
    private Object eventLockObject = new Object();
    private Runnable runnable = null;
    private int pointMoveCounts = 0;
    private String eventType = POINT_UP;

    private double zoomValue = 1;

    enum ZoomOperation {
        NONE,
        ENLARGE,
        NARROW,
    }

    private static final String TAG = "ScreenTouchImpl";

    private PointF touch = new PointF();
    private int pointDistance = 0;
    private int newPointDistance = 0;
    private LiveStreamingPresenter liveStreamingPresenter = null;

    private ScreenWindow screenWindow;
    private CameraCaptureObserverHandlerImpl cameraOberverHandle;

    //circle param
    private int cx;
    private int cy;
    private float radius;

    public ScreenTouchImpl(LiveStreamingPresenter presenter) {
        this.liveStreamingPresenter = presenter;
        cameraOberverHandle = new CameraCaptureObserverHandlerImpl();
        Log.d(TAG, "set surface_view and streaming io.agora.demo.streaming.presenter");
    }

    public void setScreenWindow(ScreenWindow screenWindow) {
        this.screenWindow = screenWindow;
    }


    /**
     * added by nianji tang 2020-10-15
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean isAbortTouchPress = false;

        Log.d(TAG, "touch screen and event: " + event.getAction());
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                if (liveStreamingPresenter == null || liveStreamingPresenter.getMaxZoom() == 0) {
                    Log.d(TAG, "max zoom: " + String.valueOf(liveStreamingPresenter.getMaxZoom()));
                    break;
                }
                if (eventType.equals(new String(POINT_DOWN)) || eventType.equals(new String(POINT_UP))) {
                    break;
                }
                if (++pointMoveCounts < POINT_MOVE_MIN_CNT) {
                    break;
                }
                pointMoveCounts = 0;
                synchronized (eventLockObject) {
                    eventList.clear();
                }
                eventType = POINT_MOVE;
                ZoomOperation zoomOperation = setZoomType(event);
                setCameraZoom(zoomOperation);
                Log.d(TAG, "screen move");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                synchronized (eventLockObject) {
                    eventList.clear();
                }
                eventType = POINTS_DOWN;
                break;
            case MotionEvent.ACTION_DOWN:
                // Does camera support focus?
                if (liveStreamingPresenter == null || !liveStreamingPresenter.isFocusSupported()) {
                    isAbortTouchPress = true;
                    Log.d(TAG, "the function isFocusSupported result is: " + String.valueOf(liveStreamingPresenter.isFocusSupported()));
                    if (liveStreamingPresenter.isFocusSupported()) {
                        Log.d(TAG, "focus support");
                    } else {
                        Log.d(TAG, "focus not support");
                    }
                    break;
                }
                Log.d(TAG, "touch screen");
                touch = getTouchPoint(event);
                if (eventType.equals(new String(POINT_UP))) {
                    synchronized (eventLockObject) {
                        eventList.add(new String(POINT_DOWN));
                    }
                }
                eventType = POINT_DOWN;
                Log.d(TAG, "get x: " + touch.x + ", get y: " + touch.y);
                Log.d(TAG, "screen press down");
                isAbortTouchPress = true;
                // enable focus
                liveStreamingPresenter.setFocus(touch.x / screenWindow.getScreenWidth(), touch.y / screenWindow.getScreenHeight());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                eventType = POINT_UP;
                isAbortTouchPress = false;
                pointMoveCounts = 0;
                resetPointDistance();
                synchronized (eventLockObject) {
                    eventList.add(new String(POINT_UP));
                }
                Log.d(TAG, "screen press release");
                break;
        }
        return isAbortTouchPress;
    }

    private int getDistance(MotionEvent event) {
        float distanceX = event.getX(0) - event.getX(1);
        float distanceY = event.getY(0) - event.getY(1);

        return (int) Math.sqrt((double) (Math.pow(distanceX, 2.0) + Math.pow(distanceY, 2.0)));
    }

    private PointF getMidPosition(MotionEvent event) {
        PointF midPosition = new PointF();

        midPosition.x = (event.getX(0) + event.getX(1)) / 2;
        midPosition.y = (event.getY(0) + event.getY(1)) / 2;

        return midPosition;
    }

    private PointF getTouchPoint(MotionEvent event) {
        PointF touch = new PointF();

        touch.x = event.getX();
        touch.y = event.getY();

        touch = changeToValidPoint(touch);

        return touch;
    }

    private PointF changeToValidPoint(PointF touch) {

        if (touch.x + CIRCLE_RADIUS >= screenWindow.getScreenWidth()) {
            touch.x = screenWindow.getScreenWidth() - CIRCLE_RADIUS;
        } else if (touch.x <= CIRCLE_RADIUS) {
            touch.x = CIRCLE_RADIUS;
        }

        if (touch.y + CIRCLE_RADIUS >= screenWindow.getScreenHeight()) {
            touch.y = screenWindow.getScreenHeight() - CIRCLE_RADIUS;
        } else if (touch.y <= CIRCLE_RADIUS) {
            touch.y = CIRCLE_RADIUS;
        }

        return touch;
    }

    private void setCameraZoom(final ZoomOperation zoomOperation) {
        float maxZoomValue = liveStreamingPresenter.getMaxZoom();
        Log.d(TAG, "maxZoomValue = " + String.valueOf(maxZoomValue));

        if (zoomOperation == ZoomOperation.NARROW) {
            if (zoomValue - ZOOM_FACTOR > 1) {
                zoomValue -= ZOOM_FACTOR;
            } else {
                zoomValue = 1;
            }
            liveStreamingPresenter.setZoom((float) zoomValue);
            Log.d(TAG, "camera zoom narrow: " + String.valueOf(zoomValue));
        } else if (zoomOperation == ZoomOperation.ENLARGE) {
            if (zoomValue + ZOOM_FACTOR <= maxZoomValue) {
                zoomValue += ZOOM_FACTOR;
            } else {
                zoomValue = maxZoomValue;
            }
            liveStreamingPresenter.setZoom((float) zoomValue);
            Log.d(TAG, "camera zoom enlarge： " + String.valueOf(zoomValue));
        }
    }

    private void resetPointDistance() {
        pointDistance = 0;
        newPointDistance = 0;
    }

    private ZoomOperation setZoomType(MotionEvent event) {
        if (!eventType.equals(POINT_MOVE)) {
            return null;
        }
        if (pointDistance <= 0) {
            pointDistance = getDistance(event);   // get the distance between two fingers
        }
        newPointDistance = getDistance(event);
        if (newPointDistance > pointDistance) {
            if (newPointDistance - pointDistance >= FINGER_DISTANCE_INTERVAL) {
                pointDistance = newPointDistance;
                return ZoomOperation.ENLARGE;
            }
        } else {
            if (pointDistance - newPointDistance >= FINGER_DISTANCE_INTERVAL) {
                pointDistance = newPointDistance;
                return ZoomOperation.NARROW;
            }
        }
        return ZoomOperation.NONE;
    }

    private HandlerThread invokeThread = null;
    private Handler invokeHandler;
    private boolean invokeRun = true;
    private long startTime = System.currentTimeMillis();
    private long endTime = startTime;

    private AgoraSurfaceView getSurfaceView() {
        LiveActivity activity = screenWindow.getActivity();
        AgoraSurfaceView mSurfaceView = (AgoraSurfaceView) activity.findViewById(R.id.live_image_view);

        return mSurfaceView;
    }

    public void clearDraw() {
        LiveActivity activity = screenWindow.getActivity();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AgoraSurfaceView surfceView = getSurfaceView();
                surfceView.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void destroyDraw() throws InterruptedException {
        clearDraw();
        invokeRun = false;
        if (runnable != null && invokeHandler != null) {
            invokeHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if (invokeThread != null) {
            invokeThread.quitSafely();
            invokeThread.join();
        }
        invokeHandler = null;
        invokeThread = null;
    }

    private void setCircleParam(int cx, int cy, float radius) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
    }

    private void displayCircle(int cx, int cy, float radius) {
        LiveActivity activity = screenWindow.getActivity();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AgoraSurfaceView mSurfaceView = (AgoraSurfaceView) activity.findViewById(R.id.live_image_view);
                mSurfaceView.setVisibility(View.VISIBLE);
                mSurfaceView.setColor(Color.YELLOW);
                if (mSurfaceView.setCircleParam(cx, cy, radius) >= 0) {
                    mSurfaceView.drawCircle();
                }
            }
        });
    }

    private void displayRectangle(int cx, int cy, int width, int height) {
        LiveActivity activity = screenWindow.getActivity();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AgoraSurfaceView mSurfaceView = (AgoraSurfaceView) activity.findViewById(R.id.live_image_view);
                mSurfaceView.setVisibility(View.VISIBLE);
                mSurfaceView.setColor(Color.YELLOW);
                if (mSurfaceView.setRectangleParam(cx, cy, width, height) >= 0) {
                    mSurfaceView.drawRectangle();
                }
            }
        });
    }

    public void startDrawThread() {
        if (invokeThread == null) {
            invokeThread = new HandlerThread("clear Thread");
            invokeThread.start();
            invokeHandler = new Handler(invokeThread.getLooper());

            runnable = new Runnable() {
                @Override
                public void run() {
                    long eventStartTime = System.currentTimeMillis();
                    long eventEndTime = eventStartTime;
                    boolean findPointUp = false;

                    long autoFocusRefreshStartTime = System.currentTimeMillis();
                    long autoFocusRefreshEdTime = autoFocusRefreshStartTime;
                    int autoImageWidth = 0;
                    int autoImageHeight = 0;
                    int autoX = 0;
                    int autoY = 0;

                    while (invokeRun) {
                        endTime = System.currentTimeMillis();
                        if (endTime > startTime + DRAW_INTERVAL_MS) {
                            startTime = endTime;
                            if (!screenWindow.getAutoFocusState()) {
                                clearDraw();
                                screenWindow.setAutoFocusState(true);
                            }
                        }

                        synchronized (eventLockObject) {
                            do {
                                if (eventList.size() <= 0) {
                                    eventStartTime = System.currentTimeMillis();
                                    eventEndTime = eventStartTime;
                                    break;
                                }
                                Iterator<String> iterator = eventList.iterator();
                                while (iterator.hasNext()) {
                                    String s = iterator.next();
                                    if (s.equals(new String(POINT_UP))) {
                                        iterator.remove();
                                        findPointUp = true;
                                    }
                                }
                                if (findPointUp) {
                                    findPointUp = false;
                                    break;
                                }
                                eventEndTime = System.currentTimeMillis();
                                if (eventEndTime - eventStartTime < DRAW_INERVAL_TIME_MS) {
                                    break;
                                }
                                eventStartTime = eventEndTime;
                                clearDraw();
                                setCircleParam((int) touch.x, (int) touch.y, CIRCLE_RADIUS);
                                draw();
                                eventList.clear();
                                screenWindow.setAutoFocusState(false);
                            } while (false);
                        }

                        if (screenWindow.getAutoFocusState()) {
                            autoFocusRefreshEdTime = System.currentTimeMillis();
                            if (autoFocusRefreshEdTime - autoFocusRefreshStartTime >= AUTO_FOCUS_REFRESH_INTERVAL_TIME) {
                                autoFocusRefreshStartTime = autoFocusRefreshEdTime;
                                // show face rect
//                                Log.d(TAG, "display face rectangle");
//                                autoX = cameraOberverHandle.getX();
//                                autoY = cameraOberverHandle.getY();
//                                autoImageHeight = cameraOberverHandle.getHeight();
//                                autoImageWidth = cameraOberverHandle.getWidth();
                                clearDraw();
                                //displayRectangle(autoX, autoY, autoImageWidth, autoImageHeight);
                            }
                        } else {
                            autoFocusRefreshStartTime = System.currentTimeMillis();
                            autoFocusRefreshEdTime = autoFocusRefreshStartTime;
                        }

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            invokeHandler.post(runnable);
        }
    }

    private void draw() {
        LiveActivity activity = screenWindow.getActivity();

        startTime = endTime;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayCircle(cx, cy, radius);
            }
        });
    }
}
