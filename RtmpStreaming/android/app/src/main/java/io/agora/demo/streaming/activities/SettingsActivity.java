package io.agora.demo.streaming.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import java.io.File;
import java.util.Arrays;

import io.agora.demo.streaming.R;
import io.agora.demo.streaming.ui.ResolutionAdapter;
import io.agora.demo.streaming.utils.PrefManager;

public class SettingsActivity extends BaseActivity {
    private static final int DEFAULT_SPAN = 3;
    private static final int REQUEST_CODE_SCAN = 1;

    private EditText mUrlEditText;
    private TextView mFrameRateText;
    private TextView mVideoBitrateText;
    private TextView mVideoOrientationModeText;
    private TextView mScreenOrientationText;
    private TextView mAudioSampleRateText;
    private TextView mAudioTypeText;
    private TextView mAudioBitrateText;
    private EditText mLogPathEditText;
    private TextView mLogFilterText;
    private TextView mStreamTypeText;
    private TextView mVideoStatCheck;
    private TextView mMirrorLocalText;
    private TextView mMirrorRemoteText;

    private int mItemPadding;
    private ResolutionAdapter mResolutionAdapter;
    private RecyclerView.ItemDecoration mItemDecoration =
            new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                           @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.top = mItemPadding;
                    outRect.bottom = mItemPadding;
                    outRect.left = mItemPadding;
                    outRect.right = mItemPadding;

                    int pos = parent.getChildAdapterPosition(view);
                    if (pos < DEFAULT_SPAN) {
                        outRect.top = 0;
                    }
                    if (pos % DEFAULT_SPAN == 0) outRect.left = 0;
                    else if (pos % DEFAULT_SPAN == (DEFAULT_SPAN - 1)) outRect.right = 0;
                }
            };

    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mPref = PrefManager.getPreferences(getApplicationContext());
        initUI();
    }

    private void initUI() {
        mItemPadding = getResources().getDimensionPixelSize(R.dimen.setting_resolution_item_padding);

        mUrlEditText = findViewById(R.id.rtmp_url_edittext);
        mUrlEditText.setText(PrefManager.getRtmpUrl(this));
        ImageView codeScanBtn = findViewById(R.id.qrcode_scan_imageview);
        codeScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.gotoQRCodeActivity();
            }
        });

        RecyclerView resolutionList = findViewById(R.id.resolution_list);
        resolutionList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, DEFAULT_SPAN);
        resolutionList.setLayoutManager(layoutManager);

        mResolutionAdapter = new ResolutionAdapter(this, PrefManager.getVideoDimensionsIndex(this));
        resolutionList.setAdapter(mResolutionAdapter);
        resolutionList.addItemDecoration(mItemDecoration);

        mFrameRateText = findViewById(R.id.setting_framerate_value);
        mFrameRateText.setText(String.valueOf(
            PrefManager.VIDEO_FRAMERATES[PrefManager.getVideoFramerateIndex(this)].getValue()));

        mVideoBitrateText = findViewById(R.id.setting_video_bitrate_value);
        mVideoBitrateText.setText(String.valueOf(
            PrefManager.VIDEO_BITRATES[PrefManager.getVideoBitrateIndex(this)]));

        mVideoOrientationModeText = findViewById(R.id.setting_video_orientation_mode_value);
        mVideoOrientationModeText.setText(
            PrefManager.VIDEO_ORIENTATION_MODE_STRINGS[PrefManager.getVideoOrientationModeIndex(this)]);

        mScreenOrientationText = findViewById(R.id.setting_screen_orientation_value);
        mScreenOrientationText.setText(
            PrefManager.SCREEN_ORIENTATION_STRINGS[PrefManager.getScreenOrientationIndex(this)]);

        mAudioSampleRateText = findViewById(R.id.setting_audio_sample_rate_value);
        mAudioSampleRateText.setText(
            PrefManager.AUDIO_SAMPLE_RATE_STRINGS[PrefManager.getAudioSampleRateIndex(this)]);

        mAudioTypeText = findViewById(R.id.setting_audio_type_value);
        mAudioTypeText.setText(PrefManager.AUDIO_TYPE_STRINGS[PrefManager.getAudioTypeIndex(this)]);

        mAudioBitrateText = findViewById(R.id.setting_audio_bitrate_value);
        mAudioBitrateText.setText(String.valueOf(
            PrefManager.AUDIO_BITRATES[PrefManager.getAudioBitrateIndex(this)]));

        mLogPathEditText = findViewById(R.id.log_path_edittext);
        mLogPathEditText.setText(PrefManager.getLogPath(this));

        mLogFilterText = findViewById(R.id.setting_log_filter_value);
        mLogFilterText.setText(PrefManager.LOG_FILTER_STRINGS[
            PrefManager.getLogFilterIndex(this)]);

        mStreamTypeText = findViewById(R.id.setting_stream_type_value);
        mStreamTypeText.setText(PrefManager.STREAM_TYPES_STRINGS[
            PrefManager.getStreamTypeIndex(this)]);

        mVideoStatCheck = findViewById(R.id.setting_stats_checkbox);
        mVideoStatCheck.setActivated(PrefManager.isStatsEnabled(this));

        mMirrorLocalText = findViewById(R.id.setting_mirror_local_value);
        mMirrorLocalText.setText(PrefManager.VIDEO_MIRROR_MODE_STRINGS[
            PrefManager.VIDEO_MIRROR_MODES[PrefManager.getMirrorLocalIndex(this)]]);

        mMirrorRemoteText = findViewById(R.id.setting_mirror_remote_value);
        mMirrorRemoteText.setText(PrefManager.VIDEO_MIRROR_MODE_STRINGS[
            PrefManager.VIDEO_MIRROR_MODES[PrefManager.getMirrorRemoteIndex(this)]]);
    }

    private void gotoQRCodeActivity() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    private void onShareLogClicked(View view) {
        String logPath = PrefManager.getLogPath(this);
        File file = new File(logPath);
        if (file.exists()) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            share.setType("text/*");
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "upload log file"));
        } else {
            Toast.makeText(this, "log file doesn't exist!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                mUrlEditText.setText(content);
            }
        }
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        // Adjust for status bar height
        RelativeLayout titleLayout = findViewById(R.id.role_title_layout);
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) titleLayout.getLayoutParams();
        params.height += mStatusBarHeight;
        titleLayout.setLayoutParams(params);
    }

    @Override
    public void onBackPressed() {
        onBackArrowPressed(null);
    }

    public void onBackArrowPressed(View view) {
        saveRtmpUrl();
        saveResolution();
        saveLogPath();
        saveShowStats();
        finish();
    }

    private void saveRtmpUrl() {
        Editable urlEditable = mUrlEditText.getText();
        if (urlEditable != null) {
            mPref.edit().putString(PrefManager.PREF_RTMP_URL, urlEditable.toString()).apply();
        }
    }

    private void saveResolution() {
        int index = mResolutionAdapter.getSelected();
        mPref.edit().putInt(PrefManager.PREF_VIDEO_DIMENSIONS_INDEX, index).apply();
    }

    private void saveLogPath() {
        Editable logPathEditable = mLogPathEditText.getText();
        if (logPathEditable != null) {
            mPref.edit().putString(PrefManager.PREF_LOG_PATH, logPathEditable.toString()).apply();
        }
    }

    private void saveShowStats() {
        mPref.edit().putBoolean(PrefManager.PREF_ENABLE_STATS,
                mVideoStatCheck.isActivated()).apply();
    }

    public void onStatsChecked(View view) {
        view.setActivated(!view.isActivated());
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setting_framerate_view:
                final String[] framerateList = new String[PrefManager.VIDEO_FRAMERATES.length];
                for (int i = 0; i < PrefManager.VIDEO_FRAMERATES.length; i++) {
                    framerateList[i] = String.valueOf(PrefManager.VIDEO_FRAMERATES[i].getValue());
                }
                showChoiceDialog(framerateList, PrefManager.PREF_VIDEO_FRAMERATE_INDEX, mFrameRateText);
                break;
            case R.id.setting_video_bitrate_view:
                final String[] videoBitrateList = new String[PrefManager.VIDEO_BITRATES.length];
                for (int i = 0; i < PrefManager.VIDEO_BITRATES.length; i++) {
                    videoBitrateList[i] = String.valueOf(PrefManager.VIDEO_BITRATES[i]);
                }
                showChoiceDialog(videoBitrateList, PrefManager.PREF_VIDEO_BITRATE_INDEX, mVideoBitrateText);
                break;
            case R.id.setting_video_orientation_mode_view:
                showChoiceDialog(PrefManager.VIDEO_ORIENTATION_MODE_STRINGS,
                    PrefManager.PREF_VIDEO_ORIENTATION_MODE_INDEX, mVideoOrientationModeText);
                break;
            case R.id.setting_screen_orientation_view:
                showChoiceDialog(PrefManager.SCREEN_ORIENTATION_STRINGS,
                    PrefManager.PREF_SCREEN_ORIENTATION_INDEX, mScreenOrientationText);
                break;
            case R.id.setting_audio_sample_rate_view:
                showChoiceDialog(PrefManager.AUDIO_SAMPLE_RATE_STRINGS,
                    PrefManager.PREF_AUDIO_SAMPLE_RATE_INDEX, mAudioSampleRateText);
                break;
            case R.id.setting_audio_type_view:
                showChoiceDialog(PrefManager.AUDIO_TYPE_STRINGS,
                    PrefManager.PREF_AUDIO_TYPE_INDEX, mAudioTypeText);
                break;
            case R.id.setting_audio_bitrate_view:
                final String[] audioBitrateList = new String[PrefManager.AUDIO_BITRATES.length];
                for (int i = 0; i < PrefManager.AUDIO_BITRATES.length; i++) {
                    audioBitrateList[i] = String.valueOf(PrefManager.AUDIO_BITRATES[i]);
                }
                showChoiceDialog(audioBitrateList, PrefManager.PREF_AUDIO_BITRATE_INDEX, mAudioBitrateText);
                break;
            case R.id.setting_log_filter_view:
                showChoiceDialog(PrefManager.LOG_FILTER_STRINGS,
                    PrefManager.PREF_LOG_FILTER_INDEX, mLogFilterText);
                break;
            case R.id.setting_log_share_view:
                onShareLogClicked(view);
                break;
            case R.id.setting_stream_type_view:
                showChoiceDialog(PrefManager.STREAM_TYPES_STRINGS,
                    PrefManager.PREF_STREAM_TYPE_INDEX, mStreamTypeText);
                break;
            case R.id.setting_mirror_local_view:
                showChoiceDialog(PrefManager.VIDEO_MIRROR_MODE_STRINGS,
                    PrefManager.PREF_MIRROR_LOCAL, mMirrorLocalText);
                break;
            case R.id.setting_mirror_remote_view:
                showChoiceDialog(PrefManager.VIDEO_MIRROR_MODE_STRINGS,
                    PrefManager.PREF_MIRROR_REMOTE, mMirrorRemoteText);
                break;
        }
    }

    private void showChoiceDialog(final String[] list, final String prefKey, final TextView view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int checkedItem = Arrays.asList(list).indexOf(view.getText().toString());
        builder.setSingleChoiceItems(list, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                mPref.edit().putInt(prefKey, index).apply();
                view.setText(list[index]);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
