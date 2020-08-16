package io.agora.rtcwithfu.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.faceunity.FURenderer;
import com.faceunity.fulivedemo.ui.adapter.EffectRecyclerAdapter;

import io.agora.rtcwithfu.R;

public class FuBeautyContainer extends RelativeLayout implements
    EffectRecyclerAdapter.OnDescriptionChangeListener,
    FURenderer.OnTrackingStatusChangedListener {

    private static final String TAG = FuBeautyContainer.class.getSimpleName();
    private EffectPanel mEffectPanel;
    private TextView mDescriptionText;
    private TextView mTrackingText;
    private RecyclerView mTypeListView;
    private LinearLayout mEffectLayout;

    private Runnable mEffectDescriptionHide;

    public FuBeautyContainer(Context context) {
        super(context);
        View viewRoot = LayoutInflater.from(context).inflate(R.layout.layout_beauty_container,
            this, true);
        mDescriptionText = viewRoot.findViewById(R.id.effect_desc_text);
        mTrackingText = viewRoot.findViewById(R.id.iv_face_detect);
        mTypeListView = viewRoot.findViewById(R.id.effect_type_list);
        mEffectLayout = viewRoot.findViewById(R.id.effect_panel_container);

        mEffectDescriptionHide = new Runnable() {
            @Override
            public void run() {
                mDescriptionText.setVisibility(View.INVISIBLE);
                mDescriptionText.setText("");
            }
        };
    }

    public void bind(FURenderer fuRenderer) {
        mEffectPanel = new EffectPanel(mTypeListView, mEffectLayout, fuRenderer, this);
    }

    @Override
    public void onDescriptionChangeListener(int description) {
        if (description == 0) return;
        mDescriptionText.removeCallbacks(mEffectDescriptionHide);
        mDescriptionText.setText(description);
        mDescriptionText.setVisibility(View.VISIBLE);
        mDescriptionText.postDelayed(mEffectDescriptionHide, 1500);
    }

    @Override
    public void onTrackingStatusChanged(final int status) {
        post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "FURenderer.onTrackingStatusChanged, status: " + status);
                mTrackingText.setVisibility(status > 0 ? View.GONE : View.VISIBLE);
            }
        });
    }
}
