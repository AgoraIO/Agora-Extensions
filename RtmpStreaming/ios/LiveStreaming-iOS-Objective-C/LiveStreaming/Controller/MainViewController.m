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

@interface MainViewController () <UITextFieldDelegate>
@property (weak, nonatomic) IBOutlet UITextField *roomNameTextField;
@property (weak, nonatomic) IBOutlet UIView *popoverSourceView;

@property (assign, nonatomic) BOOL isLastmileProbeTesting;
@property (nonatomic, strong) StreamingModel *streamingModel;

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
