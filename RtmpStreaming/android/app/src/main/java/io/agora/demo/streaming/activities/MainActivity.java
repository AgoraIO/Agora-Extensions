package io.agora.demo.streaming.activities;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.agora.demo.streaming.R;
import io.agora.demo.streaming.presenter.LiveStreamingPresenter;
import io.agora.demo.streaming.services.ScreenCaptureService;
import io.agora.demo.streaming.ui.OperationMenu;
import io.agora.demo.streaming.utils.PrefManager;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MIN_INPUT_METHOD_HEIGHT = 200;
    private static final int ANIM_DURATION = 200;

    private MediaProjectionManager mpm;
    private Intent mediaProjectionIntent;
    public static int PROJECTION_REQ_CODE = 1;

    // Permission request code of any integer value
    private static final int PERMISSION_REQ_CODE = 1 << 4;
    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Rect mVisibleRect = new Rect();
    private int mLastVisibleHeight = 0;
    private RelativeLayout mBodyLayout;
    private int mBodyDefaultMarginTop;
    private EditText mTopicEdit;
    private TextView mStartBtn;
    private ImageView mLogo;

    private Intent mForegroundService;

    private Animator.AnimatorListener mLogoAnimListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            // Do nothing
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mLogo.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            mLogo.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
            // Do nothing
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            mStartBtn.setEnabled(!TextUtils.isEmpty(editable));
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener mLayoutObserverListener =
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    checkInputMethodWindowState();
                }
            };

    private void checkInputMethodWindowState() {
        getWindow().getDecorView().getRootView().getWindowVisibleDisplayFrame(mVisibleRect);
        int visibleHeight = mVisibleRect.bottom - mVisibleRect.top;
        if (visibleHeight == mLastVisibleHeight) return;

        boolean inputShown = mDisplayMetrics.heightPixels - visibleHeight > MIN_INPUT_METHOD_HEIGHT;
        mLastVisibleHeight = visibleHeight;

        // Log.i(TAG, "onGlobalLayout:" + inputShown +
        //        "|" + getWindow().getDecorView().getRootView().getViewTreeObserver());

        // There is no official way to determine whether the
        // input method dialog has already shown.
        // This is a workaround, and if the visible content
        // height is significantly less than the screen height,
        // we should know that the input method dialog takes
        // up some screen space.
        if (inputShown) {
            if (mLogo.getVisibility() == View.VISIBLE) {
                mBodyLayout.animate().translationYBy(-mLogo.getMeasuredHeight())
                        .setDuration(ANIM_DURATION).setListener(null).start();
                mLogo.setVisibility(View.INVISIBLE);
            }
        } else if (mLogo.getVisibility() != View.VISIBLE) {
            mBodyLayout.animate().translationYBy(mLogo.getMeasuredHeight())
                    .setDuration(ANIM_DURATION).setListener(mLogoAnimListener).start();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OperationMenu.PROJECTION_REQ_CODE && resultCode == RESULT_OK) {
            Log.i(TAG, "start screen capture!");

            if (!ScreenCaptureService.serviceIsLive) {
                mForegroundService = new Intent(this, ScreenCaptureService.class);
                mForegroundService.putExtra("screen_intent", data);
                mForegroundService.putExtra("key_room_name", mTopicEdit.getText().toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(mForegroundService);
                } else {
                    startService(mForegroundService);
                }
                moveTaskToBack(true);
            } else {
                Toast.makeText(this, getString(R.string.main_capturing_screen_warning), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(ScreenCaptureService.serviceIsLive){
            moveTaskToBack(true);
            Toast toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
            toast.setText(getString(R.string.main_capturing_screen_warning));
            toast.show();
            return;
        }
        if(!intent.hasExtra("from")){
            return;
        }
        String from = intent.getStringExtra("from");

        if(!intent.hasExtra("why")){
            return;
        }
        String why = intent.getStringExtra("why");
        if(from.equals("ScreenCaptureService") && why.equals("StopSelf")){
            mpm = null;
            mediaProjectionIntent = null;
        }
    }

    private void initUI() {
        mBodyLayout = findViewById(R.id.middle_layout);
        mLogo = findViewById(R.id.main_logo);

        mTopicEdit = findViewById(R.id.topic_edit);
        mTopicEdit.addTextChangedListener(mTextWatcher);

        mStartBtn = findViewById(R.id.start_broadcast_button);
        if (TextUtils.isEmpty(mTopicEdit.getText())) mStartBtn.setEnabled(false);


        MainActivity.this.runOnUiThread(() -> {
            String sdkVersion = LiveStreamingPresenter.getSdkVersion();
            TextView VerText = findViewById(R.id.sdk_version_text);
            if (VerText != null) {
                VerText.setText("Version " + sdkVersion);
            }
        });
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        adjustViewPositions();
    }

    private void adjustViewPositions() {
        // Setting btn move downward away the status bar
        ImageView settingBtn = findViewById(R.id.setting_button);
        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) settingBtn.getLayoutParams();
        param.topMargin += mStatusBarHeight;
        settingBtn.setLayoutParams(param);

        // Logo is 0.48 times the screen width
        // ImageView logo = findViewById(R.id.main_logo);
        param = (RelativeLayout.LayoutParams) mLogo.getLayoutParams();
        int size = (int) (mDisplayMetrics.widthPixels * 0.48);
        param.width = size;
        param.height = size;
        mLogo.setLayoutParams(param);

        // Bottom margin of the main body should be two times it's top margin.
        param = (RelativeLayout.LayoutParams) mBodyLayout.getLayoutParams();
        param.topMargin = (mDisplayMetrics.heightPixels -
                mBodyLayout.getMeasuredHeight() - mStatusBarHeight) / 3;
        mBodyLayout.setLayoutParams(param);
        mBodyDefaultMarginTop = param.topMargin;

        // The width of the start button is roughly 0.72
        // times the width of the screen
        mStartBtn = findViewById(R.id.start_broadcast_button);
        param = (RelativeLayout.LayoutParams) mStartBtn.getLayoutParams();
        param.width = (int) (mDisplayMetrics.widthPixels * 0.72);
        mStartBtn.setLayoutParams(param);
    }

    public void onSettingClicked(View view) {
        // required for some of the settings to take effect
        LiveStreamingPresenter.destroyEngineAndKit();
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void onStartBroadcastClicked(View view) {
        Log.d(TAG, "onStartBroadcastClicked befor");
        checkPermission();
        Log.d(TAG, "onStartBroadcastClicked after");
    }

    private void checkPermission() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }

        if (granted) {
            resetLayoutAndForward();
        } else {
            requestPermissions();
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                granted = (result == PackageManager.PERMISSION_GRANTED);
                if (!granted) break;
            }

            if (granted) {
                resetLayoutAndForward();
            } else {
                toastNeedPermissions();
            }
        }
    }

    private void resetLayoutAndForward() {
        closeImeDialogIfNeeded();
        Log.d(TAG, "gotoLiveActivity before");

        RadioGroup rb = findViewById(R.id.capture_mode);
        int r_id = rb.getCheckedRadioButtonId();
        if (r_id == R.id.capture_mode_camera) {
            gotoLiveActivity();
        } else {
            if (mpm == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                }
            }
            if (mediaProjectionIntent == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mediaProjectionIntent = mpm.createScreenCaptureIntent();
                }
            }
            startActivityForResult(mediaProjectionIntent, PROJECTION_REQ_CODE);
        }

        Log.d(TAG, "gotoLiveActivity after");
    }

    private void closeImeDialogIfNeeded() {
        InputMethodManager manager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mTopicEdit.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void gotoLiveActivity() {
        int screenOrientation =
                PrefManager.SCREEN_ORIENTATIONS[PrefManager.getScreenOrientationIndex(this)];
        Class<?> cls = (screenOrientation == Configuration.ORIENTATION_PORTRAIT) ? LiveActivity.class
                : (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) ? LandscapeLiveActivity.class
                : DynamicLiveActivity.class;
        Intent intent = new Intent(getApplicationContext(), cls);
        intent.putExtra(LiveActivity.KEY_ROOM_NAME, mTopicEdit.getText().toString());
        startActivity(intent);
    }

    private void toastNeedPermissions() {
        Toast.makeText(this, R.string.need_necessary_permissions, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetUI();
        registerLayoutObserverForSoftKeyboard();
    }

    private void resetUI() {
        resetLogo();
        closeImeDialogIfNeeded();
    }

    private void resetLogo() {
        mLogo.setVisibility(View.VISIBLE);
        mBodyLayout.setY(mBodyDefaultMarginTop);
    }

    private void registerLayoutObserverForSoftKeyboard() {
        View view = getWindow().getDecorView().getRootView();
        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(mLayoutObserverListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeLayoutObserverForSoftKeyboard();
    }

    private void removeLayoutObserverForSoftKeyboard() {
        View view = getWindow().getDecorView().getRootView();
        view.getViewTreeObserver().removeOnGlobalLayoutListener(mLayoutObserverListener);
    }
}
