package io.agora.capture.framework.modules.consumers;

import io.agora.capture.framework.modules.channels.VideoChannel;
import io.agora.capture.video.camera.VideoCaptureFrame;

public interface IVideoConsumer {
    int TYPE_ON_SCREEN = 0;
    int TYPE_OFF_SCREEN = 1;

    void onConsumeFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context);
    void connectChannel(int channelId);
    void disconnectChannel(int channelId);

    void setMirrorMode(int mode);

    /**
     * Give a chance for subclasses to return a drawing target
     * object. This object can only be either a Surface or a
     * SurfaceTexture.
     * @return
     */
    Object onGetDrawingTarget();

    int onMeasuredWidth();
    int onMeasuredHeight();

    /**
     * Called when the consumer is intended to stay
     * in a channel but its rendering pauses maybe
     * because this consumer is not current.
     */
    void recycle();

    /**
     * The tag is used to identify consumers that
     * are considered to be the same, although
     * they are different object instances.
     * @return tag of this consumer
     */
    String getTag();
}
