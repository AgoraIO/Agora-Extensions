//
//  ProfileViewController.m
//  LiveStreaming
//
//  Created by LSQ on 2020/6/9.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "ProfileViewController.h"

@interface ProfileViewController ()
@property (weak, nonatomic) IBOutlet UITextField *rtmpTextFiled;
@property (weak, nonatomic) IBOutlet UITextField *heightTextFiled;
@property (weak, nonatomic) IBOutlet UITextField *widthTextFiled;
@property (weak, nonatomic) IBOutlet UITextField *fpsTextField;
@property (weak, nonatomic) IBOutlet UITextField *rateTextFiled;

@property (weak, nonatomic) IBOutlet UIButton *pushAVBtn;
@property (weak, nonatomic) IBOutlet UIButton *pushVideoBtn;
@property (weak, nonatomic) IBOutlet UIButton *pushAudioBtn;

@property (weak, nonatomic) IBOutlet UIButton *offLevelBtn;
@property (weak, nonatomic) IBOutlet UIButton *debugLevelBtn;
@property (weak, nonatomic) IBOutlet UIButton *infoLevelBtn;
@property (weak, nonatomic) IBOutlet UIButton *warnLevelBtn;
@property (weak, nonatomic) IBOutlet UIButton *errorLevelBtn;

@property (weak, nonatomic) IBOutlet UIButton *samplerate44100Btn;
@property (weak, nonatomic) IBOutlet UIButton *samplerate22050Btn;
@property (weak, nonatomic) IBOutlet UIButton *sampelrate11025Btn;

@property (weak, nonatomic) IBOutlet UIButton *menoBtn;
@property (weak, nonatomic) IBOutlet UIButton *stereoBtn;

@property (weak, nonatomic) IBOutlet UIButton *portraitBtn;
@property (weak, nonatomic) IBOutlet UIButton *landscapeBtn;

@property (nonatomic, strong) AgoraStreamingContext *streamingContext;
@property (nonatomic, strong) NSArray *itemsArray;

@end

@implementation ProfileViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;

    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    self.streamingContext = self.streamingModel.streamingContext;

    self.itemsArray = @[@[@""], // Push the stream address
                        @[@""], // resolution
                        @[@""], // fps
                        @[@""], // bitrate
                        @[@""], // audio sampling rate
                        @[@""], // channels
                        @[@""], // push type
                        @[@""], // log
                        @[@""], // Log filtering level
    ];
    UIView *tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.frame.size.width, 80)];
    self.tableView.tableHeaderView = tableHeaderView;
    UIButton *backBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    backBtn.frame = CGRectMake(-10, 0, 100, 80);
    [backBtn setTitle:@"保存" forState:UIControlStateNormal];
    [backBtn setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    backBtn.titleLabel.font = [UIFont boldSystemFontOfSize:18];
    [tableHeaderView addSubview:backBtn];
    [backBtn addTarget:self action:@selector(backBtnDidClicked:) forControlEvents:UIControlEventTouchUpInside];

    self.rtmpTextFiled.text = self.streamingModel.rtmpUrl;
    self.heightTextFiled.text = [self stringFormat:self.streamingContext.videoStreamConfiguration.height];
    self.widthTextFiled.text = [self stringFormat:self.streamingContext.videoStreamConfiguration.width];
    self.fpsTextField.text = [self stringFormat:self.streamingContext.videoStreamConfiguration.framerate];
    self.rateTextFiled.text = [self stringFormat:self.streamingContext.videoStreamConfiguration.bitrate];
    switch (self.streamingContext.audioStreamConfiguration.sampleRateHz) {
        case SampleRate11025:
            self.sampelrate11025Btn.selected = YES;
            break;
        case SampleRate22050:
            self.samplerate22050Btn.selected = YES;
            break;
        case SampleRate44100:
            self.samplerate44100Btn.selected = YES;
            break;
        default:
            break;
    }
    switch (self.streamingContext.audioStreamConfiguration.soundType) {
        case AudioSoundTypeMono:
            self.menoBtn.selected = YES;
            break;
        case AudioSoundTypeStereo:
            self.stereoBtn.selected = YES;
            break;
        default:
            break;
    }
    if (self.streamingContext.enableAudioStreaming && self.streamingContext.enableVideoStreaming) {
        self.pushAVBtn.selected = YES;
    } else if (self.streamingContext.enableVideoStreaming) {
        self.pushVideoBtn.selected = YES;
    } else if (self.streamingContext.enableAudioStreaming) {
        self.pushAudioBtn.selected = YES;
    }
    switch (self.streamingModel.logFilterType) {
        case LogFilterOff:
            self.offLevelBtn.selected = YES;
            break;
        case LogFilterDebug:
            self.debugLevelBtn.selected = YES;
            break;
        case LogFilterInfo:
            self.infoLevelBtn.selected = YES;
            break;
        case LogFilterWarn:
            self.warnLevelBtn.selected = YES;
            break;
        case LogFilterError:
            self.errorLevelBtn.selected = YES;
            break;
        default:
            break;
    }
    switch (self.streamingContext.videoStreamConfiguration.orientationMode) {
        case OrientationModeFixedPortrait:
            self.portraitBtn.selected = YES;
            break;
        case OrientationModeFixedLandscape:
            self.landscapeBtn.selected = YES;
            break;
        default:
            break;
    }
}

- (NSString *)stringFormat:(int)value {
    return [NSString stringWithFormat:@"%d", value];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.view endEditing:YES];
    return YES;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

- (void)backBtnDidClicked:(UIButton *)sender {
    self.streamingContext.videoStreamConfiguration.framerate = self.fpsTextField.text.integerValue;
    self.streamingContext.videoStreamConfiguration.bitrate = self.rateTextFiled.text.integerValue;
    self.streamingContext.videoStreamConfiguration.width = self.widthTextFiled.text.integerValue;
    self.streamingContext.videoStreamConfiguration.height = self.heightTextFiled.text.integerValue;
    self.streamingModel.streamingContext = self.streamingContext;
    self.streamingModel.rtmpUrl = self.rtmpTextFiled.text;

    NSString *modelPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *contextPath = [modelPath stringByAppendingPathComponent:@"streamingContext.archive"];
    [NSKeyedArchiver archiveRootObject:self.streamingModel.streamingContext toFile:contextPath];

    NSString *rtmpPath = [modelPath stringByAppendingPathComponent:@"rtmpPath.archive"];
    [NSKeyedArchiver archiveRootObject:self.streamingModel.rtmpUrl toFile:rtmpPath];

    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)sampleRateBtnDidClicked:(UIButton *)sender {
    switch (sender.tag) {
        case 0: {
            self.samplerate44100Btn.selected = YES;
            self.samplerate22050Btn.selected = NO;
            self.sampelrate11025Btn.selected = NO;
            self.streamingContext.audioStreamConfiguration.sampleRateHz = SampleRate44100;
        }
            break;
        case 1: {
            self.samplerate44100Btn.selected = NO;
            self.samplerate22050Btn.selected = YES;
            self.sampelrate11025Btn.selected = NO;
            self.streamingContext.audioStreamConfiguration.sampleRateHz = SampleRate22050;
        }
            break;
        case 2: {
            self.samplerate44100Btn.selected = NO;
            self.samplerate22050Btn.selected = NO;
            self.sampelrate11025Btn.selected = YES;
            self.streamingContext.audioStreamConfiguration.sampleRateHz = SampleRate11025;
        }
            break;
        default:
            break;
    }
}

- (IBAction)soundTypeBtnDidClicked:(UIButton *)sender {
    switch (sender.tag) {
        case 1: {
            self.menoBtn.selected = YES;
            self.stereoBtn.selected = NO;
            self.streamingContext.audioStreamConfiguration.soundType = AudioSoundTypeMono;
        }
            break;
        case 2: {
            self.menoBtn.selected = NO;
            self.stereoBtn.selected = YES;
            self.streamingContext.audioStreamConfiguration.soundType = AudioSoundTypeStereo;
        }
            break;
        default:
            break;
    }
}

- (IBAction)orientationModeBtnClicked:(UIButton *)sender {
    switch (sender.tag) {
        case 0: {
            self.streamingContext.videoStreamConfiguration.orientationMode = OrientationModeFixedPortrait;
            self.portraitBtn.selected = YES;
            self.landscapeBtn.selected = NO;
        }
            break;
        case 1: {
            self.streamingContext.videoStreamConfiguration.orientationMode = OrientationModeFixedLandscape;
            self.portraitBtn.selected = NO;
            self.landscapeBtn.selected = YES;
        }
            break;
        default:
            break;
    }
}

- (IBAction)pushAVType:(UIButton *)sender {
    switch (sender.tag) {
        case 0: {
            self.pushAVBtn.selected = YES;
            self.pushAudioBtn.selected = NO;
            self.pushVideoBtn.selected = NO;
        }
            break;
        case 1: {
            self.pushAVBtn.selected = NO;
            self.pushAudioBtn.selected = NO;
            self.pushVideoBtn.selected = YES;
        }
            break;
        case 2: {
            self.pushAVBtn.selected = NO;
            self.pushAudioBtn.selected = YES;
            self.pushVideoBtn.selected = NO;
        }
            break;
        default:
            break;
    }
}

- (IBAction)logLevelDidClicked:(UIButton *)sender {
    switch (sender.tag) {
        case 0: {
            self.offLevelBtn.selected = YES;
            self.debugLevelBtn.selected = NO;
            self.infoLevelBtn.selected = NO;
            self.warnLevelBtn.selected = NO;
            self.errorLevelBtn.selected = NO;
            self.streamingModel.logFilterType = LogFilterOff;
        }
            break;
        case 1: {
            self.offLevelBtn.selected = NO;
            self.debugLevelBtn.selected = YES;
            self.infoLevelBtn.selected = NO;
            self.warnLevelBtn.selected = NO;
            self.errorLevelBtn.selected = NO;
            self.streamingModel.logFilterType = LogFilterDebug;
        }
            break;
        case 2: {
            self.offLevelBtn.selected = NO;
            self.debugLevelBtn.selected = NO;
            self.infoLevelBtn.selected = YES;
            self.warnLevelBtn.selected = NO;
            self.errorLevelBtn.selected = NO;
            self.streamingModel.logFilterType = LogFilterInfo;
        }
            break;
        case 3: {
            self.offLevelBtn.selected = NO;
            self.debugLevelBtn.selected = NO;
            self.infoLevelBtn.selected = NO;
            self.warnLevelBtn.selected = YES;
            self.errorLevelBtn.selected = NO;
            self.streamingModel.logFilterType = LogFilterWarn;
        }
            break;
        case 4: {
            self.offLevelBtn.selected = NO;
            self.debugLevelBtn.selected = NO;
            self.infoLevelBtn.selected = NO;
            self.warnLevelBtn.selected = NO;
            self.errorLevelBtn.selected = YES;
            self.streamingModel.logFilterType = LogFilterError;
        }
            break;
        default:
            break;
    }
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.itemsArray.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.itemsArray[section] count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 8) { //
        return 120;
    } else {
        return 50;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSLog(@"section:%ld, row:%ld", indexPath.section, indexPath.row);
}

/*
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:<#@"reuseIdentifier"#> forIndexPath:indexPath];

    // Configure the cell...

    return cell;
}
*/

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
