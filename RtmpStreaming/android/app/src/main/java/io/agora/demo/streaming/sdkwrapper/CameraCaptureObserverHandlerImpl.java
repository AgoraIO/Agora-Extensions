package io.agora.demo.streaming.sdkwrapper;

import android.graphics.Rect;
import android.util.Log;

import io.agora.streaming.CameraCaptureObserverHandler;

public class CameraCaptureObserverHandlerImpl extends CameraCaptureObserverHandler {
    private static final String TAG = CameraCaptureObserverHandlerImpl.class.getSimpleName();

    private int x;
    private int y;
    private int width;
    private int height;
    private Rect[] faceArray;

    @Override
    public void onCameraFocusAreaChanged(int imageWidth, int imageHeight, int x, int y) {
        this.x = x;
        this.y = y;
        this.width = imageWidth;
        this.height = imageHeight;

        Log.d(TAG, String.format("onCameraFocusAreaChanged %d,%d,%d,%d", x, y, imageWidth, imageHeight));
    }

    @Override
    public void onFacePositionChanged(int imageWidth, int imageHeight, Rect[] faceArray) {
        this.width = imageWidth;
        this.height = imageHeight;
        this.faceArray = faceArray;

        Log.d(TAG, String.format("onFacePositionChanged %d,%d", imageWidth, imageHeight));

        for (Rect rect: faceArray) {
            Log.d(TAG, String.format("onFacePositionChanged %d,%d,%d,%d", rect.left, rect.top, rect.right, rect.bottom));
        }
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public Rect[] getFaceArray(){
        return faceArray;
    }
}
