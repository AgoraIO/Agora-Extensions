package io.agora.demo.streaming.ui;

import android.view.MenuItem;
import android.widget.PopupMenu;

import java.util.HashMap;
import java.util.Map;

import io.agora.demo.streaming.R;
import io.agora.demo.streaming.StreamingMode;
import io.agora.demo.streaming.activities.LiveActivity;

public class OperationMenu implements PopupMenu.OnMenuItemClickListener , PopupMenu.OnDismissListener{
    private static final String TAG = OperationMenu.class.getSimpleName();

    private LiveActivity activity;
    private Map<String, Boolean> clickMap = new HashMap<>();

    public static int PROJECTION_REQ_CODE = 1;

    private static final String SCREEN_SHARE = "SCREEN_SAHARE";

    public OperationMenu(LiveActivity activity){
        this.activity = activity;
        clickMap.put(SCREEN_SHARE, false);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.screen_ration:
                if(activity.getCurrentStreamingMode() != StreamingMode.NONE){
//                    item.setEnabled(false);
                    break;
                }
                activity.onSwitchResolution(activity.findViewById(R.id.live_room_top_layout), item);
                break;
            case R.id.screen_snapshot:
                activity.onSnapshot(activity.findViewById(R.id.live_room_top_layout), item);
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    public void onDismiss(PopupMenu menu) {

    }

}
