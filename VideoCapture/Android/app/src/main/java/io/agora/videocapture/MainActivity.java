package io.agora.videocapture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import io.agora.capture.video.camera.CameraVideoManager;
import io.agora.capture.video.camera.Constant;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST = 1;

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private CameraVideoManager mCameraVideoManager;
    private FrameLayout mVideoLayout;
    private SurfaceView mVideoSurface;
    private boolean mPermissionGranted;
    private boolean mFinished;
    private boolean mIsMirrored = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoLayout = findViewById(R.id.video_layout);
        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (permissionGranted(Manifest.permission.CAMERA)) {
            onPermissionGranted();
            mPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST);
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        if (requestCode == REQUEST) {
             for (String permission : permissions) {
                 if (!permissionGranted(permission)) {
                     granted = false;
                 }
             }
        }

        if (granted) {
            onPermissionGranted();
            mPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST);
        }
    }

    private void onPermissionGranted() {
        // Preprocessor for Face Unity can be defined here
        // Now we ignore preprocessor
        // If there is a third-party preprocessor available,
        // say, FaceUnity, the camera manager is better to
        // be initialized asynchronously because FaceUnity
        // needs to loads resource files from local storage.
        // The loading may block the video rendering for a
        // little while.
        mCameraVideoManager = new CameraVideoManager(this, null);

        // Set camera capture configuration
        mCameraVideoManager.setPictureSize(640, 480);
        mCameraVideoManager.setFrameRate(24);
        mCameraVideoManager.setFacing(Constant.CAMERA_FACING_FRONT);
        mCameraVideoManager.setLocalPreviewMirror(true);

        // The preview surface is actually considered as
        // an on-screen consumer under the hood.
        mVideoSurface = new SurfaceView(this);
        mCameraVideoManager.setLocalPreview(mVideoSurface);
        mVideoLayout.addView(mVideoSurface);

        // Can attach other consumers here,
        // For example, rtc consumer or rtmp module

        mCameraVideoManager.startCapture();
    }

    public void onCameraChange(View view) {
        if (mCameraVideoManager != null) {
            mCameraVideoManager.switchCamera();
        }
    }

    public void onMirrorModeChanged(View view) {
        if (mCameraVideoManager != null) {
            mIsMirrored = !mIsMirrored;
            mCameraVideoManager.setLocalPreviewMirror(mIsMirrored);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPermissionGranted && mCameraVideoManager != null) mCameraVideoManager.startCapture();
    }

    @Override
    public void finish() {
        super.finish();
        mFinished = true;
        if (mCameraVideoManager != null) mCameraVideoManager.stopCapture();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mFinished && mCameraVideoManager != null) mCameraVideoManager.stopCapture();
    }
}
