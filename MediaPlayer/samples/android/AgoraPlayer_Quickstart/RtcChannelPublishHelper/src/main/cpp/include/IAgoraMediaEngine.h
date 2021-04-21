#ifndef AGORA_MEDIA_ENGINE_H
#define AGORA_MEDIA_ENGINE_H
#if defined _WIN32 || defined __CYGWIN__
typedef __int64 int64_t;
typedef unsigned __int64 uint64_t;
typedef __int32 int32_t;
typedef unsigned __int32 uint32_t;
#else
#include <stdint.h>
#endif

namespace agora
{
namespace media
{

enum MEDIA_SOURCE_TYPE {
    AUDIO_PLAYOUT_SOURCE = 0,
    AUDIO_RECORDING_SOURCE = 1,
};

class IAudioFrameObserver
{
public:
  enum AUDIO_FRAME_TYPE {
    FRAME_TYPE_PCM16 = 0,  //PCM 16bit little endian
  };
  struct AudioFrame {
    AUDIO_FRAME_TYPE type;
    int samples;  //number of samples in this frame
    int bytesPerSample;  //number of bytes per sample: 2 for PCM16
    int channels;  //number of channels (data are interleaved if stereo)
    int samplesPerSec;  //sampling rate
    void* buffer;  //data buffer
    int64_t renderTimeMs;
    int avsync_type;
  };
public:
  virtual bool onRecordAudioFrame(AudioFrame& audioFrame) = 0;
  virtual bool onPlaybackAudioFrame(AudioFrame& audioFrame) = 0;
  virtual bool onMixedAudioFrame(AudioFrame& audioFrame) = 0;
  virtual bool onPlaybackAudioFrameBeforeMixing(unsigned int uid, AudioFrame& audioFrame) = 0;
  virtual bool isMultipleChannelFrameWanted() { return false; }
  virtual bool onPlaybackAudioFrameBeforeMixingEx(const char *channelId,
      unsigned int uid, AudioFrame& audioFrame) { return true; }
};

class IVideoFrameObserver
{
public:
  enum VIDEO_FRAME_TYPE {
    FRAME_TYPE_YUV420 = 0,  //YUV 420 format
    FRAME_TYPE_RGBA = 2,    // RGBA format
  };
  enum VIDEO_OBSERVER_POSITION {
    POSITION_POST_CAPTURER = 1 << 0,
    POSITION_PRE_RENDERER = 1 << 1,
    POSITION_PRE_ENCODER = 1 << 2,
  };
  struct VideoFrame {
    VIDEO_FRAME_TYPE type;
    int width;  //width of video frame
    int height;  //height of video frame
    int yStride;  //stride of Y data buffer
    int uStride;  //stride of U data buffer
    int vStride;  //stride of V data buffer
    void* yBuffer;  //Y data buffer
    void* uBuffer;  //U data buffer
    void* vBuffer;  //V data buffer
    int rotation; // rotation of this frame (0, 90, 180, 270)
    int64_t renderTimeMs;
    int avsync_type;
  };
public:
  virtual bool onCaptureVideoFrame(VideoFrame& videoFrame) = 0;
  virtual bool onPreEncodeVideoFrame(VideoFrame& videoFrame) { return true; }
  virtual bool onRenderVideoFrame(unsigned int uid, VideoFrame& videoFrame) = 0;
  virtual bool getRotationApplied() { return false; }
  virtual uint32_t getObservedFramePosition() { return POSITION_POST_CAPTURER | POSITION_PRE_RENDERER; }
  virtual bool isMultipleChannelFrameWanted() { return false; }
  virtual bool onRenderVideoFrameEx(const char *channelId, unsigned int uid, VideoFrame& videoFrame) { return true; }
  virtual VIDEO_FRAME_TYPE getVideoFormatPreference() { return FRAME_TYPE_YUV420; }
};

class IVideoFrame
{
public:
  enum PLANE_TYPE {
    Y_PLANE = 0,
    U_PLANE = 1,
    V_PLANE = 2,
    NUM_OF_PLANES = 3
  };
  enum VIDEO_TYPE {
    VIDEO_TYPE_UNKNOWN = 0,
    VIDEO_TYPE_I420 = 1,
    VIDEO_TYPE_IYUV = 2,
    VIDEO_TYPE_RGB24 = 3,
    VIDEO_TYPE_ABGR = 4,
    VIDEO_TYPE_ARGB = 5,
    VIDEO_TYPE_ARGB4444 = 6,
    VIDEO_TYPE_RGB565 = 7,
    VIDEO_TYPE_ARGB1555 = 8,
    VIDEO_TYPE_YUY2 = 9,
    VIDEO_TYPE_YV12 = 10,
    VIDEO_TYPE_UYVY = 11,
    VIDEO_TYPE_MJPG = 12,
    VIDEO_TYPE_NV21 = 13,
    VIDEO_TYPE_NV12 = 14,
    VIDEO_TYPE_BGRA = 15,
    VIDEO_TYPE_RGBA = 16,
  };
  virtual void release() = 0;
  virtual const unsigned char* buffer(PLANE_TYPE type) const = 0;

  // Copy frame: If required size is bigger than allocated one, new buffers of
  // adequate size will be allocated.
  // Return value: 0 on success ,-1 on error.
  virtual int copyFrame(IVideoFrame** dest_frame) const = 0;

  // Convert frame
  // Input:
  //   - src_frame        : Reference to a source frame.
  //   - dst_video_type   : Type of output video.
  //   - dst_sample_size  : Required only for the parsing of MJPG.
  //   - dst_frame        : Pointer to a destination frame.
  // Return value: 0 if OK, < 0 otherwise.
  // It is assumed that source and destination have equal height.
  virtual int convertFrame(VIDEO_TYPE dst_video_type, int dst_sample_size, unsigned char* dst_frame) const = 0;

  // Get allocated size per plane.
  virtual int allocated_size(PLANE_TYPE type) const = 0;

  // Get allocated stride per plane.
  virtual int stride(PLANE_TYPE type) const = 0;

  // Get frame width.
  virtual int width() const = 0;

  // Get frame height.
  virtual int height() const = 0;

  // Get frame timestamp (90kHz).
  virtual unsigned int timestamp() const = 0;

  // Get render time in milliseconds.
  virtual int64_t render_time_ms() const = 0;

  // Return true if underlying plane buffers are of zero size, false if not.
  virtual bool IsZeroSize() const = 0;
};

class IExternalVideoRenderCallback
{
public:
  virtual void onViewSizeChanged(int width, int height) = 0;
  virtual void onViewDestroyed() = 0;
};

struct ExternalVideoRenerContext
{
  IExternalVideoRenderCallback* renderCallback;
  void* view;
  int renderMode;
  int zOrder;
  float left;
  float top;
  float right;
  float bottom;
};

class IExternalVideoRender
{
public:
  virtual void release() = 0;
  virtual int initialize() = 0;
  virtual int deliverFrame(const IVideoFrame& videoFrame, int rotation, bool mirrored) = 0;
};

class IExternalVideoRenderFactory
{
public:
  virtual IExternalVideoRender* createRenderInstance(const ExternalVideoRenerContext& context) = 0;
};

struct ExternalVideoFrame
{
  enum VIDEO_BUFFER_TYPE
  {
    VIDEO_BUFFER_RAW_DATA = 1,
  };

  enum VIDEO_PIXEL_FORMAT
  {
    VIDEO_PIXEL_UNKNOWN = 0,
    VIDEO_PIXEL_I420 = 1,
    VIDEO_PIXEL_BGRA = 2,
    VIDEO_PIXEL_NV12 = 8,
  };

  VIDEO_BUFFER_TYPE type;
  VIDEO_PIXEL_FORMAT format;
  void* buffer;
  int stride;
  int height;
  int cropLeft;
  int cropTop;
  int cropRight;
  int cropBottom;
  int rotation;
  long long timestamp;
};

enum CODEC_VIDEO_FRAME_TYPE {
	CODEC_VIDEO_FRAME_TYPE_BLANK_FRAME = 0,
	CODEC_VIDEO_FRAME_TYPE_KEY_FRAME = 3,
	CODEC_VIDEO_FRAME_TYPE_DELTA_FRAME = 4,
	CODEC_VIDEO_FRAME_TYPE_B_FRAME = 5,
	CODEC_VIDEO_FRAME_TYPE_UNKNOW
};

enum VIDEO_ORIENTATION {
	VIDEO_ORIENTATION_0 = 0,
	VIDEO_ORIENTATION_90 = 90,
	VIDEO_ORIENTATION_180 = 180,
	VIDEO_ORIENTATION_270 = 270
};

/** Video codec types */
enum VIDEO_CODEC_TYPE {
	/** Standard VP8 */
	VIDEO_CODEC_VP8 = 1,
	/** Standard H264 */
	VIDEO_CODEC_H264 = 2,
	/** Enhanced VP8 */
	VIDEO_CODEC_EVP = 3,
	/** Enhanced H264 */
	VIDEO_CODEC_E264 = 4,
};

/** * The struct of EncodedVideoFrameInfo. */
struct EncodedVideoFrameInfo {
	EncodedVideoFrameInfo() : codecType(VIDEO_CODEC_H264),
		width(0),
		height(0),
		frameType(CODEC_VIDEO_FRAME_TYPE_BLANK_FRAME),
		rotation(VIDEO_ORIENTATION_0),
		renderTimeMs(0) {}
	/**
	 * The video codec: #VIDEO_CODEC_TYPE.
	 */
	VIDEO_CODEC_TYPE codecType;
	/**   * The width (px) of the video.   */
	int width;
	/**   * The height (px) of the video.   */
	int height;
	/**   * The frame type of the encoded video frame: #VIDEO_FRAME_TYPE.   */
	CODEC_VIDEO_FRAME_TYPE frameType;
	/**   * The rotation information of the encoded video frame: #VIDEO_ORIENTATION.   */
	VIDEO_ORIENTATION rotation;
	/**   * The timestamp for rendering the video.   */
	int64_t renderTimeMs;
};
	
class IVideoEncodedImageReceiver {
public:
	/**
	  * Occurs each time the SDK receives an encoded video image.
	  * @param imageBuffer The pointer to the video image buffer.
	  * @param length The data length of the video image.
	  * @param videoEncodedFrameInfo The information of the encoded video frame: EncodedVideoFrameInfo.
	  *
	  */
	virtual bool OnEncodedVideoImageReceived(const uint8_t* imageBuffer, unsigned int length, const EncodedVideoFrameInfo& videoEncodedFrameInfo) = 0;

	virtual ~IVideoEncodedImageReceiver() {}
};

class IMediaEngine {
public:
  virtual void release() = 0;
  virtual int registerAudioFrameObserver(IAudioFrameObserver* observer) = 0;
  virtual int registerVideoFrameObserver(IVideoFrameObserver* observer) = 0;
  virtual int registerVideoRenderFactory(IExternalVideoRenderFactory* factory) = 0;
  virtual int pushAudioFrame(MEDIA_SOURCE_TYPE type, IAudioFrameObserver::AudioFrame *frame, bool wrap) = 0;
  virtual int pushAudioFrame(IAudioFrameObserver::AudioFrame *frame) = 0;
  virtual int pullAudioFrame(IAudioFrameObserver::AudioFrame *frame) = 0;

  virtual int setExternalVideoSource(bool enable, bool useTexture) = 0;
  virtual int pushVideoFrame(ExternalVideoFrame *frame) = 0;
  virtual int registerVideoEncodedImageReceiver(IVideoEncodedImageReceiver*receiver) = 0;
};

} //media

} //agora

#endif //AGORA_MEDIA_ENGINE_H
