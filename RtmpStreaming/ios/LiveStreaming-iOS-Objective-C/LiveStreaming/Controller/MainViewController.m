//
//  MainViewController.m
//  OpenLive
//
//  Created by GongYuhua on 2016/9/12.
//  Copyright © 2016年 Agora. All rights reserved.
//

#import "MainViewController.h"
#import "LiveViewController.h"
#import "KeyCenter.h"
#import "ProfileViewController.h"
#import "ToastTool.h"
#import <AgoraRtcKit/AgoraRtcEngineKit.h>
#import <ReplayKit/ReplayKit.h>

API_AVAILABLE(ios(12.0))
@interface MainViewController () <UITextFieldDelegate>
@property (weak, nonatomic) IBOutlet UITextField *roomNameTextField;
@property (weak, nonatomic) IBOutlet UIView *popoverSourceView;

@property (assign, nonatomic) BOOL isLastmileProbeTesting;
@property (nonatomic, strong) StreamingModel *streamingModel;

@property (nonatomic, strong) RPSystemBroadcastPickerView *broadPickerView;
@property (nonatomic, strong) AVAudioPlayer *audioPlayer;
@property (nonatomic, strong) UIButton *testBtn;

@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.streamingModel = [[StreamingModel alloc] init];

    NSString *modelPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *contextPath = [modelPath stringByAppendingPathComponent:@"streamingContext.archive"];
    AgoraStreamingContext *context = [NSKeyedUnarchiver unarchiveObjectWithFile:contextPath];
    if (context) {
        self.streamingModel.streamingContext = context;
    }

    NSString *rtmpPath = [modelPath stringByAppendingPathComponent:@"rtmpPath.archive"];
    self.streamingModel.rtmpUrl = [NSKeyedUnarchiver unarchiveObjectWithFile:rtmpPath];

#if 0
		// 屏幕采集
		if (@available(iOS 12.0, *)) {
			self.broadPickerView = [[RPSystemBroadcastPickerView alloc] initWithFrame:CGRectMake(self.view.frame.size.width/2 - 100/2, 400, 100, 100)];
			self.broadPickerView.preferredExtension = @"io.agora.LiveStreaming.ScreenShare";
			[self.view addSubview:self.broadPickerView];
		}
#endif

//	self.testBtn = [UIButton buttonWithType:UIButtonTypeSystem];
//	self.testBtn.frame = CGRectMake(100, 400, 100, 50);
//	[self.testBtn setTitle:@"copy" forState:UIControlStateNormal];
//	[self.testBtn addTarget:self action:@selector(copyBtnDidClicked:) forControlEvents:UIControlEventTouchUpInside];
//	[self.view addSubview:self.testBtn];
}

- (void)copyBtnDidClicked:(UIButton *)sender {
	NSURL *logUrl = [[NSFileManager defaultManager] containerURLForSecurityApplicationGroupIdentifier:@"group.io.agora.LiveStreaming"];
	NSString *logPath = [logUrl URLByAppendingPathComponent:@"Library/Caches/SDKLogs"].path;
	if ([[NSFileManager defaultManager] fileExistsAtPath:logPath]) {
		NSString *distDirectory = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject;
		NSString *distPath = [distDirectory stringByAppendingPathComponent:@"/SDKLogs"];
		[[NSFileManager defaultManager] removeItemAtPath:distPath error:nil];
		[[NSFileManager defaultManager] moveItemAtPath:logPath toPath:distPath error:nil];
	} else {
		NSLog(@"log does not exist:%@", logPath);
	}
}

- (void)viewDidAppear:(BOOL)animated {
//	[[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord withOptions:AVAudioSessionCategoryOptionMixWithOthers error:nil];
//	NSURL *mp3Url = [[NSBundle mainBundle] URLForResource:@"doudizhu" withExtension:@"mp3"];
//	self.audioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:mp3Url error:nil];
//	self.audioPlayer.numberOfLoops = -1;
//	[self.audioPlayer play];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    NSString *segueId = segue.identifier;
    if ([segueId isEqualToString:@"mainToLive"]) {
        LiveViewController *liveVC = segue.destinationViewController;
        liveVC.channelName = self.roomNameTextField.text;
        liveVC.streamingModel = self.streamingModel;
    } else if ([segueId isEqualToString:@"ProfileSetting"]) {
        ProfileViewController *profileVC = segue.destinationViewController;
        profileVC.streamingModel = self.streamingModel;
    }
}

- (IBAction)doStartLivePressed:(UIButton *)sender {
    [self.view becomeFirstResponder];
    if (!self.roomNameTextField.text.length) {
        UIAlertController *sheet = [UIAlertController alertControllerWithTitle:@"Please enter your room name." message:nil preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *cancel = [UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleDefault handler:nil];
        [sheet addAction:cancel];
        [self presentViewController:sheet animated:YES completion:nil];
    }
    if (!self.streamingModel.rtmpUrl.length) {
        UIAlertController *sheet = [UIAlertController alertControllerWithTitle:@"Please profile your rtmp url." message:nil preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *cancel = [UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleDefault handler:nil];
        [sheet addAction:cancel];
        [self presentViewController:sheet animated:YES completion:nil];
    }
    [self joinWithIdentifier:@"mainToLive" role:AgoraClientRoleBroadcaster];
}

- (void)joinWithIdentifier:(NSString *)identifier role:(AgoraClientRole)role {
    [self performSegueWithIdentifier:identifier sender:@(role)];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.view endEditing:YES];
    return YES;
}
@end
