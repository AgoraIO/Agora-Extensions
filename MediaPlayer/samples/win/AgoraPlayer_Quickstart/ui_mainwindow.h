/********************************************************************************
** Form generated from reading UI file 'mainwindow.ui'
**
** Created by: Qt User Interface Compiler version 5.9.0
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_MAINWINDOW_H
#define UI_MAINWINDOW_H

#include <QtCore/QVariant>
#include <QtWidgets/QAction>
#include <QtWidgets/QApplication>
#include <QtWidgets/QButtonGroup>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QMainWindow>
#include <QtWidgets/QMenu>
#include <QtWidgets/QMenuBar>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QSlider>
#include <QtWidgets/QStatusBar>
#include <QtWidgets/QToolBar>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_MainWindow
{
public:
    QWidget *centralWidget;
    QWidget *containerView;
    QPushButton *openBtn;
    QPushButton *playBtn;
    QPushButton *stopBtn;
    QSlider *volume_slider_;
    QPushButton *attachBtn;
    QPushButton *detachBtn;
    QPushButton *publishAudioBtn;
    QPushButton *unPublishAudio;
    QSlider *seekSlider;
    QPushButton *publishVideoBtn;
    QPushButton *unpublishVideoBtn;
    QLineEdit *inputUrl;
    QLabel *label;
    QSlider *remote_volume;
    QLabel *label_2;
    QPushButton *pauseBtn;
    QMenuBar *menuBar;
    QMenu *menuAgoraMediaPlayerDemo;
    QToolBar *mainToolBar;
    QStatusBar *statusBar;

    void setupUi(QMainWindow *MainWindow)
    {
        if (MainWindow->objectName().isEmpty())
            MainWindow->setObjectName(QStringLiteral("MainWindow"));
        MainWindow->resize(1411, 843);
        centralWidget = new QWidget(MainWindow);
        centralWidget->setObjectName(QStringLiteral("centralWidget"));
        containerView = new QWidget(centralWidget);
        containerView->setObjectName(QStringLiteral("containerView"));
        containerView->setGeometry(QRect(20, -1, 921, 461));
        openBtn = new QPushButton(centralWidget);
        openBtn->setObjectName(QStringLiteral("openBtn"));
        openBtn->setGeometry(QRect(30, 560, 150, 46));
        playBtn = new QPushButton(centralWidget);
        playBtn->setObjectName(QStringLiteral("playBtn"));
        playBtn->setGeometry(QRect(240, 560, 150, 46));
        stopBtn = new QPushButton(centralWidget);
        stopBtn->setObjectName(QStringLiteral("stopBtn"));
        stopBtn->setGeometry(QRect(440, 560, 150, 46));
        volume_slider_ = new QSlider(centralWidget);
        volume_slider_->setObjectName(QStringLiteral("volume_slider_"));
        volume_slider_->setGeometry(QRect(950, 60, 241, 22));
        volume_slider_->setMaximum(100);
        volume_slider_->setValue(100);
        volume_slider_->setOrientation(Qt::Horizontal);
        attachBtn = new QPushButton(centralWidget);
        attachBtn->setObjectName(QStringLiteral("attachBtn"));
        attachBtn->setGeometry(QRect(30, 500, 150, 46));
        detachBtn = new QPushButton(centralWidget);
        detachBtn->setObjectName(QStringLiteral("detachBtn"));
        detachBtn->setGeometry(QRect(240, 500, 150, 46));
        publishAudioBtn = new QPushButton(centralWidget);
        publishAudioBtn->setObjectName(QStringLiteral("publishAudioBtn"));
        publishAudioBtn->setGeometry(QRect(440, 500, 150, 46));
        unPublishAudio = new QPushButton(centralWidget);
        unPublishAudio->setObjectName(QStringLiteral("unPublishAudio"));
        unPublishAudio->setGeometry(QRect(620, 500, 211, 46));
        seekSlider = new QSlider(centralWidget);
        seekSlider->setObjectName(QStringLiteral("seekSlider"));
        seekSlider->setGeometry(QRect(29, 720, 821, 22));
        seekSlider->setOrientation(Qt::Horizontal);
        publishVideoBtn = new QPushButton(centralWidget);
        publishVideoBtn->setObjectName(QStringLiteral("publishVideoBtn"));
        publishVideoBtn->setGeometry(QRect(30, 630, 150, 46));
        unpublishVideoBtn = new QPushButton(centralWidget);
        unpublishVideoBtn->setObjectName(QStringLiteral("unpublishVideoBtn"));
        unpublishVideoBtn->setGeometry(QRect(240, 630, 201, 46));
        inputUrl = new QLineEdit(centralWidget);
        inputUrl->setObjectName(QStringLiteral("inputUrl"));
        inputUrl->setGeometry(QRect(460, 610, 911, 91));
        label = new QLabel(centralWidget);
        label->setObjectName(QStringLiteral("label"));
        label->setGeometry(QRect(960, 20, 231, 24));
        remote_volume = new QSlider(centralWidget);
        remote_volume->setObjectName(QStringLiteral("remote_volume"));
        remote_volume->setGeometry(QRect(950, 150, 241, 22));
        remote_volume->setMaximum(100);
        remote_volume->setValue(100);
        remote_volume->setOrientation(Qt::Horizontal);
        label_2 = new QLabel(centralWidget);
        label_2->setObjectName(QStringLiteral("label_2"));
        label_2->setGeometry(QRect(960, 110, 231, 24));
        pauseBtn = new QPushButton(centralWidget);
        pauseBtn->setObjectName(QStringLiteral("pauseBtn"));
        pauseBtn->setGeometry(QRect(640, 560, 150, 46));
        MainWindow->setCentralWidget(centralWidget);
        menuBar = new QMenuBar(MainWindow);
        menuBar->setObjectName(QStringLiteral("menuBar"));
        menuBar->setGeometry(QRect(0, 0, 1411, 37));
        menuAgoraMediaPlayerDemo = new QMenu(menuBar);
        menuAgoraMediaPlayerDemo->setObjectName(QStringLiteral("menuAgoraMediaPlayerDemo"));
        MainWindow->setMenuBar(menuBar);
        mainToolBar = new QToolBar(MainWindow);
        mainToolBar->setObjectName(QStringLiteral("mainToolBar"));
        MainWindow->addToolBar(Qt::TopToolBarArea, mainToolBar);
        statusBar = new QStatusBar(MainWindow);
        statusBar->setObjectName(QStringLiteral("statusBar"));
        MainWindow->setStatusBar(statusBar);

        menuBar->addAction(menuAgoraMediaPlayerDemo->menuAction());

        retranslateUi(MainWindow);

        QMetaObject::connectSlotsByName(MainWindow);
    } // setupUi

    void retranslateUi(QMainWindow *MainWindow)
    {
        MainWindow->setWindowTitle(QApplication::translate("MainWindow", "MainWindow", Q_NULLPTR));
        openBtn->setText(QApplication::translate("MainWindow", "open", Q_NULLPTR));
        playBtn->setText(QApplication::translate("MainWindow", "play", Q_NULLPTR));
        stopBtn->setText(QApplication::translate("MainWindow", "stop", Q_NULLPTR));
        attachBtn->setText(QApplication::translate("MainWindow", "attachPlayer", Q_NULLPTR));
        detachBtn->setText(QApplication::translate("MainWindow", "detachPlayer", Q_NULLPTR));
        publishAudioBtn->setText(QApplication::translate("MainWindow", "publishAudio", Q_NULLPTR));
        unPublishAudio->setText(QApplication::translate("MainWindow", "unpublishAudio", Q_NULLPTR));
        publishVideoBtn->setText(QApplication::translate("MainWindow", "publishVideo", Q_NULLPTR));
        unpublishVideoBtn->setText(QApplication::translate("MainWindow", "unpublishVideo", Q_NULLPTR));
        inputUrl->setText(QApplication::translate("MainWindow", "http://compress.mv.letusmix.com/1c4dbe546537a9459d7b2c208d513303.mp4", Q_NULLPTR));
        inputUrl->setPlaceholderText(QApplication::translate("MainWindow", "Input your playing url", Q_NULLPTR));
        label->setText(QApplication::translate("MainWindow", "LocalVolume", Q_NULLPTR));
        label_2->setText(QApplication::translate("MainWindow", "RemoteVolume", Q_NULLPTR));
        pauseBtn->setText(QApplication::translate("MainWindow", "pause", Q_NULLPTR));
        menuAgoraMediaPlayerDemo->setTitle(QApplication::translate("MainWindow", "AgoraMediaPlayerDemo", Q_NULLPTR));
    } // retranslateUi

};

namespace Ui {
    class MainWindow: public Ui_MainWindow {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_MAINWINDOW_H
