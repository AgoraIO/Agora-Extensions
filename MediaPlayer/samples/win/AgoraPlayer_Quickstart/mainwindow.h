#ifndef MAINWINDOW_H
#define MAINWINDOW_H
//#include <IAgoraMediaPlayer.h>
#include <IAgoraMediaPlayer.h>
#include <IAgoraRtcEngine.h>
#include <QMainWindow>

namespace Ui {
class MainWindow;
}
class AgoraRtcEngineEvent;
class AgoraMediaPlayerEvent;
class AgoraRtcChannelPublishHelper;
class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

private:
    Ui::MainWindow *ui;
	agora::rtc::IRtcEngine *rtc_engine_;
	agora::rtc::IMediaPlayer *media_player_;
	AgoraRtcEngineEvent * rtc_event_;
	std::shared_ptr<AgoraMediaPlayerEvent> player_event_;
	AgoraRtcChannelPublishHelper * agora_rtc_channel_publish_helper_;
	void init_agora_sdk();
	void init_media_player_kit();
};

#endif // MAINWINDOW_H
