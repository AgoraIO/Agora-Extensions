#pragma once
#include <atomic>
#include <IAgoraRtcEngine.h>
#include <IAgoraMediaPlayer.h>
#include <AgoraBase.h>
#include <iostream>
class MediaPlayerFrameObserver;
class CExtendAudioFrameObserver;
class AgoraRtcChannelPublishHelperObserver : public agora::rtc::IMediaPlayerObserver
{
	;
	/**
	 * @brief Triggered when the player state changes
	 *
	 * @param state New player state
	 * @param ec Player error message
	 */
	virtual void onPlayerStateChanged(agora::media::MEDIA_PLAYER_STATE state,
		agora::media::MEDIA_PLAYER_ERROR ec)
	{

	}

	/**
	 * @brief Triggered when the player progress changes, once every 1 second
	 *
	 * @param position Current playback progress, in seconds
	 */
	virtual void onPositionChanged(const int64_t position)
	{

	}
	/**
	 * @brief Triggered when the player have some event
	 *
	 * @param event
	 */
	virtual void onPlayerEvent(agora::media::MEDIA_PLAYER_EVENT event)
	{

	};

	/**
	 * @brief Triggered when metadata is obtained
	 *
	 * @param type Metadata type
	 * @param data data
	 * @param length  data length
	 */
	virtual void onMetadata(agora::media::MEDIA_PLAYER_METADATA_TYPE type, const uint8_t* data,
		uint32_t length)
	{

	}
};
class AgoraRtcChannelPublishHelper
{
public:
	
	std::unique_ptr<CExtendAudioFrameObserver> extend_audio_frame_observer_;
	static AgoraRtcChannelPublishHelper *AgoraRtcChannelPublishHelper::Get()
	{
		static  AgoraRtcChannelPublishHelper  helper;
		return &helper;
	}
	//����RtcEngine��MediaPlayer������
	void attachPlayerToRtc(agora::rtc::IRtcEngine *engine, agora::rtc::IMediaPlayer *media_player);
	//ע���¼��ص�
	void registerAgoraRtcChannelPublishHelperObserver(AgoraRtcChannelPublishHelperObserver *observer);
	// ����/ֹͣ������Ƶ����Ƶ��
	void publishAudio();
	void unpublishAudio();
	// ����/ֹͣ������Ƶ����Ƶ��
	void publishVideo();
	void unpublishVideo();
	// �������͵�Ƶ������Ƶ��������
	void adjustPublishSignalVolume(int volume);
	// ���ڱ��ز�����Ƶ����
	void adjustPlayoutSignalVolume(int volume);
	// �Ͽ� MediaPlayer �� RTC SDK �Ĺ���
	void detachPlayerFromRtc();
	AgoraRtcChannelPublishHelper();
	~AgoraRtcChannelPublishHelper();
private:
	std::unique_ptr<MediaPlayerFrameObserver> media_player_frame_observer_;
	agora::rtc::IRtcEngine *rtc_engine_;
	agora::rtc::IMediaPlayer *media_player_;
	agora::rtc::IMediaPlayerObserver *media_player_observer_;
};

