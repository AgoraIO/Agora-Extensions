#include "mainwindow.h"
#include <IAgoraMediaEngine.h>
#include "ui_mainwindow.h"
//#include <IAgoraMediaPlayer.h>
//#include <IAgoraParameter.h>
#include <RtcChannelHelperPlugin/AgoraRtcChannelPublishHelper.h>
#include <IAgoraMediaPlayer.h>
#include <iostream>
#include <QMessageBox>
#include <QTextCodec>
#include  <QFileDialog>
using namespace agora::rtc;
class AgoraMediaPlayerEvent : public AgoraRtcChannelPublishHelperObserver
{
public:
	 agora::rtc::IMediaPlayer *media_player_;
	 Ui::MainWindow  *ui_;
	/**
	 * @brief Triggered when the player state changes
	 *
	 * @param state New player state
	 * @param ec Player error message
	 */
	virtual void onPlayerStateChanged(agora::media::MEDIA_PLAYER_STATE state,
		agora::media::MEDIA_PLAYER_ERROR ec)
	{
		switch (state)
		{
		case  agora::media::PLAYER_STATE_OPEN_COMPLETED:
			std::cout << "PLAYER_STATE_OPEN_COMPLETED" << std::endl;
			int64_t duration;
			 media_player_->getDuration(duration);
			 ui_->seekSlider->setMaximum(duration);
			break;
		case  agora::media::PLAYER_STATE_OPENING:
			std::cout << "PLAYER_STATE_OPENING" << std::endl;
			break;
		case  agora::media::PLAYER_STATE_IDLE:
			std::cout << "PLAYER_STATE_IDLE" << std::endl;
			break;
		case  agora::media::PLAYER_STATE_PLAYING:
			std::cout << "PLAYER_STATE_PLAYING" << std::endl;
			break;
		case agora::media::PLAYER_STATE_PLAYBACK_COMPLETED:
			std::cout << "PLAYER_STATE_PLAYBACK_COMPLETED" << std::endl;
			break;
		case agora::media::PLAYER_STATE_PAUSED:
			std::cout << "PLAYER_STATE_PAUSED" << std::endl;
			break;
		case agora::media::PLAYER_STATE_STOPPED:
			std::cout << "PLAYER_STATE_STOPPED" << std::endl;
			break;
		case agora::media::PLAYER_STATE_FAILED:
			std::cout << "PLAYER_STATE_FAILED" << std::endl;
			break;
		default:
			std::cout << "PLAYER_STATE_UNKNOWN" << std::endl;
			break;
		}
		
		switch (ec)
		{
		case agora::media::PLAYER_ERROR_URL_NOT_FOUND:
			std::cout << "PLAYER_ERROR_URL_NOT_FOUND" << std::endl;
			break;
		case agora::media::PLAYER_ERROR_NONE:
			std::cout << "PLAYER_ERROR_NONE" << std::endl;
			break;
		case agora::media::PLAYER_ERROR_CODEC_NOT_SUPPORTED:
			std::cout << "PLAYER_ERROR_CODEC_NOT_SUPPORTED" << std::endl;
			break;
		case agora::media::PLAYER_ERROR_INVALID_ARGUMENTS:
			std::cout << "PLAYER_ERROR_INVALID_ARGUMENTS" << std::endl;
			break;
		case agora::media::PLAY_ERROR_SRC_BUFFER_UNDERFLOW:
			std::cout << "PLAY_ERROR_SRC_BUFFER_UNDERFLOW" << std::endl;
			break;
		case agora::media::PLAYER_ERROR_INTERNAL:
			std::cout << "PLAYER_ERROR_INTERNAL" << std::endl;
			break;
		case agora::media::PLAYER_ERROR_INVALID_STATE:
			std::cout << "PLAYER_ERROR_INVALID_STATE" << std::endl;
			break;
		case agora::media::PLAYER_ERROR_NO_RESOURCE:
				std::cout << "PLAYER_ERROR_NO_RESOURCE" << std::endl;
				break;
		case agora::media::PLAYER_ERROR_OBJ_NOT_INITIALIZED:
			std::cout << "PLAYER_ERROR_OBJ_NOT_INITIALIZED" << std::endl;
			break;
		case agora::media::PLAYER_ERROR_INVALID_CONNECTION_STATE:
			std::cout << "PLAYER_ERROR_INVALID_CONNECTION_STATE" << std::endl;
			break;
		case agora::media::PLAYER_ERROR_UNKNOWN_STREAM_TYPE:
			std::cout << "PLAYER_ERROR_UNKNOWN_STREAM_TYPE" << std::endl;
			break;
		case agora::media::PLAYER_ERROR_VIDEO_RENDER_FAILED:
			std::cout << "PLAYER_ERROR_VIDEO_RENDER_FAILED" << std::endl;
			break;
		}
		
	}

	/**
	 * @brief Triggered when the player progress changes, once every 1 second
	 *
	 * @param position Current playback progress, in seconds
	 */
	virtual void onPositionChanged(const int64_t position)
	{
		std::cout << "cur_pos ==" << position << std::endl;
		ui_->seekSlider->setValue(position);
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
class AgoraRtcEngineEvent : public agora::rtc::IRtcEngineEventHandler
{
public:
	virtual ~AgoraRtcEngineEvent() {}

	/**
	 * Occurs when the local user successfully joins a specified channel.
	 *
	 * @param channel The channel name.
	 * @param userId The user ID.
	 * - If you specified a `uid` in the `joinChannel` method, the SDK returns the specified ID;
	 * - If not, this callback returns an ID that is automatically assigned by the Agora server.
	 * @param elapsed The time elapsed (ms) from the local user calling \ref IRtcEngine::joinChannel
	 * "joinChannel()" until this event occurs.
	 */
	virtual void onJoinChannelSuccess(const char* channel, uid_t userId, int elapsed) {
		(void)channel;
		(void)userId;
		(void)elapsed;
	}

	/**
	 * Occurs when the local user rejoins the channel after being disconnected due to
	 * network problems.
	 *
	 * When the app loses connection with the server because of network
	 * problems, the SDK automatically tries to reconnect to the server, and triggers this
	 * callback method upon reconnection.
	 *
	 * @param channel The channel name.
	 * @param userId The user ID.
	 * @param elapsed The time elapsed (ms) from the local user calling
	 * \ref IRtcEngine::joinChannel "joinChannel()" until this event occurs.
	 */
	virtual void onRejoinChannelSuccess(const char* channel, uid_t userId, int elapsed) {
		(void)channel;
		(void)userId;
		(void)elapsed;
	}
};

MainWindow::MainWindow(QWidget *parent) :
	QMainWindow(parent),
	rtc_event_(new AgoraRtcEngineEvent()),
	agora_rtc_channel_publish_helper_(new AgoraRtcChannelPublishHelper),
	ui(new Ui::MainWindow)
{
	ui->setupUi(this);
	//init media_player
	init_media_player_kit();
	//init agora sdk
	init_agora_sdk();
	player_event_.reset(new AgoraMediaPlayerEvent),
	player_event_->media_player_ = media_player_;
	player_event_->ui_ = ui;
	media_player_->registerPlayerObserver(player_event_.get());
	//bind ui connect methods
	connect(ui->openBtn,&QPushButton::clicked,this,[=]
	{
		if(ui->inputUrl->text().isEmpty())
		{
			//µ¯³ö¿ò
			QMessageBox::information(NULL, "", "please input url",
				QMessageBox::Yes | QMessageBox::No, QMessageBox::Yes);
			return;
		}
		media_player_->open(ui->inputUrl->text().toUtf8().data(),0);
	});
	connect(ui->playBtn,&QPushButton::clicked,this,[=]
	{
		media_player_->play();
	});
	connect(ui->stopBtn,&QPushButton::clicked,this,[=]
	{
		media_player_->stop();
	});
	connect(ui->volume_slider_,&QSlider::valueChanged,this,[=]
	{
		agora_rtc_channel_publish_helper_->adjustPlayoutSignalVolume(ui->volume_slider_->value());
		media_player_->adjustPlayoutVolume(ui->volume_slider_->value());

	});
	connect(ui->attachBtn, &QPushButton::clicked, this, [=]
	{
		media_player_->mute(true);
		media_player_->unregisterPlayerObserver(player_event_.get());
		player_event_.reset(new AgoraMediaPlayerEvent),
		player_event_->media_player_ = media_player_;
		player_event_->ui_ = ui;
		agora_rtc_channel_publish_helper_->attachPlayerToRtc(rtc_engine_, media_player_);
		agora_rtc_channel_publish_helper_->publishVideo();
		agora_rtc_channel_publish_helper_->registerAgoraRtcChannelPublishHelperObserver(player_event_.get());
		int ret = rtc_engine_->joinChannel(nullptr, "123456", nullptr, 0);
		std::cout << "joining channel " << ret << std::endl;
		
	});
	connect(ui->detachBtn,&QPushButton::clicked,this,[=]
	{
		agora_rtc_channel_publish_helper_->detachPlayerFromRtc();
		rtc_engine_->leaveChannel();
		rtc_engine_->stopPreview();
		media_player_->mute(false);
		player_event_.reset(new AgoraMediaPlayerEvent);
		player_event_->media_player_ = media_player_;
		player_event_->ui_ = ui;
		media_player_->registerPlayerObserver(player_event_.get());

	});
	connect(ui->publishAudioBtn,&QPushButton::clicked,this,[=]
	{
		agora_rtc_channel_publish_helper_->publishAudio();
	});
	connect(ui->unPublishAudio, &QPushButton::clicked, this, [=]
	{
		agora_rtc_channel_publish_helper_->unpublishAudio();

	});
	connect(ui->publishVideoBtn,&QPushButton::clicked,this,[=]
	{
		agora_rtc_channel_publish_helper_->publishVideo();
	});
	connect(ui->unpublishVideoBtn, &QPushButton::clicked, this, [=]
	{
		agora_rtc_channel_publish_helper_->unpublishVideo();
	});
	connect(ui->seekSlider,&QSlider::sliderReleased,this,[=]
	{
		media_player_->seek(ui->seekSlider->value());
	});
	connect(ui->remote_volume,&QSlider::valueChanged,this,[=]
	{
		agora_rtc_channel_publish_helper_->adjustPublishSignalVolume(ui->remote_volume->value());
	});
	connect(ui->pauseBtn,&QPushButton::clicked,this,[=]
	{
		media_player_->pause();
	});
}
static 	AgoraRtcEngineEvent rtc_engine_event_;
void MainWindow::init_agora_sdk()
{
	
	agora::rtc::RtcEngineContext ctx;
	ctx.eventHandler = &rtc_engine_event_;
	ctx.appId = "";//Specify your APP ID here
	if (*ctx.appId == '\0')
	{
		QMessageBox::critical(nullptr, tr("Agora QT Demo"),
			tr("You must specify APP ID before using the demo"));
	}
	rtc_engine_ = createAgoraRtcEngine();
	rtc_engine_->initialize(ctx);
	VideoEncoderConfiguration config;
	config.dimensions = VideoDimensions(640, 360);
	config.bitrate = 1200;
	config.frameRate = FRAME_RATE_FPS_15;
	rtc_engine_->setVideoEncoderConfiguration(config);
	rtc_engine_->setChannelProfile(CHANNEL_PROFILE_LIVE_BROADCASTING);
	rtc_engine_->setClientRole(CLIENT_ROLE_BROADCASTER);
	rtc_engine_->enableVideo();
	rtc_engine_->enableAudio();
    ui->containerView->setUpdatesEnabled(false);
	VideoCanvas canvas;
	canvas.uid = 0;
	canvas.view = (view_t)ui->containerView->winId();
	canvas.renderMode = RENDER_MODE_FIT;
	rtc_engine_->setupLocalVideo(canvas);
	
}

void MainWindow::init_media_player_kit()
{
	media_player_ = createAgoraMediaPlayer();
	agora::rtc::MediaPlayerContext context;
	media_player_->initialize(context);
	media_player_->setView((agora::media::base::view_t)ui->containerView->winId());

}

MainWindow::~MainWindow()
{
	delete ui;
}
