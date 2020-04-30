// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#include "mainview.h"
#include "tcvm.h"
#include "YoutubePlayerController.h"
#include "YoutubePlayer.h"
#import "YTPlayerView.h"
@implementation YoutubeController
    static int STATE_UNSTARTED = 0;
    static int STATE_ENDED = 1;
    static int STATE_PLAYING = 2;
    static int STATE_PAUSED = 3;
    static int STATE_BUFFERING = 4;
    static int STATE_CUED = 5;
    static int STATE_UNKNOWN = 6;
    
    static int ERROR_VIDEO_NOT_FOUND = 7;
    static int ERROR_UNKNOWN = 8;
    TCObject* callback = null;
    int autoPlayFlag = 1;
    CGRect playerFrame;
    UIView *spinnerView;
    
- (instancetype)initWithCallBack: (TCObject*) callback {
    callback = callback;
    return [self init];
}
    
- (instancetype)init {
    return [super init];
};
    static bool isHidden = false;
- (void) presentYoutubeVideo: (char*)videoId autoPlay:(int)autoPlay end:(int)end start:(int)start{
    autoPlayFlag = autoPlay;
    NSNumber *autoPlayNumber = [[NSNumber numberWithInteger:autoPlay] retain];
//    NSNumber *colorNumber = [[NSNumber numberWithInteger:color] retain];
//    NSNumber *optionNumber = [[NSNumber numberWithInteger:option] retain];
    NSNumber *endNumber = [[NSNumber numberWithInteger:end] retain];
//    NSNumber *loopNumber = [[NSNumber numberWithInteger:loop] retain];
//    NSNumber *relNumber = [[NSNumber numberWithInteger:rel] retain];
    NSNumber *startNumber = [[NSNumber numberWithInteger:start] retain];
//    NSNumber *policyNumber = [[NSNumber numberWithInteger:policy] retain];
    NSString *videoIdString = [[NSString stringWithUTF8String:videoId] retain];
//    NSString *hlObjCString = [NSString stringWithUTF8String:hl];
//    YTPlaybackQuality qualityObj = [self getQualityByName:[NSString stringWithUTF8String:quality]];
    
    dispatch_async(dispatch_get_main_queue(), ^
                       {
                           if ([DEVICE_CTX->_mainview.view.subviews containsObject:playerView]){
                               [playerView removeFromSuperview];
                           }
                           
                           [self addLoadingView];
                           
                           playerFrame = CGRectMake(0, 0, 0, 0);
                           playerView = [[YTPlayerView alloc]initWithFrame:playerFrame];
                           playerView.delegate = self;
                           [playerView webView].hidden = NO;
                           [playerView.webView setAllowsInlineMediaPlayback: NO];
                           [DEVICE_CTX->_mainview.view addSubview:playerView];
//                           [playerView setPlaybackQuality: qualityObj];
                           
                           NSMutableDictionary *playVars = @{
//                                                             @"showinfo":showInfoNumber,
                                                      @"modestbranding":@1,
                                                      @"autoplay": @1/*autoPlayNumber*/,
                                                      @"playsinline":@0,
//                                                      @"color":colorNumber,
//                                                      @"option":optionNumber,
                                                      @"end":endNumber,
//                                                      @"loop":loopNumber,
//                                                      @"policy": policyNumber,
//                                                      @"rel": relNumber,
//                                                             @"hl": @"fr",
                                                             @"start": startNumber}.mutableCopy;
                           
//                           if(hlObjCString != null && ![hlObjCString isEqualToString:@""]){
//                               [playVars addEntriesFromDictionary:@{@"hl": [NSString stringWithUTF8String:hl]}];
//                           }
                           
                            [playerView loadWithVideoId: videoIdString playerVars:
                                (NSDictionary*)playVars];
                           
                       });
    }
    
-(YTPlaybackQuality)getQualityByName: (NSString*) qualityStr{
    if([qualityStr isEqualToString:@"small"]) return kYTPlaybackQualitySmall;
    if([qualityStr isEqualToString:@"medium"]) return kYTPlaybackQualityMedium;
    if([qualityStr isEqualToString:@"large"]) return kYTPlaybackQualityLarge;
    if([qualityStr isEqualToString:@"hd720"]) return kYTPlaybackQualityHD720;
    if([qualityStr isEqualToString:@"hd1080"]) return kYTPlaybackQualityHD1080;
    if([qualityStr isEqualToString:@"highres"]) return kYTPlaybackQualityHighRes;
    if([qualityStr isEqualToString:@"auto"]) return kYTPlaybackQualityAuto;
    if([qualityStr isEqualToString:@"default"]) return kYTPlaybackQualityDefault;
    if([qualityStr isEqualToString:@"unknown"]) return kYTPlaybackQualityUnknown;
    return null;
}
-(void)playerViewDidBecomeReady:(YTPlayerView *)playerView {
    [playerView playVideo];
}
- (void)playerView:(nonnull YTPlayerView *)playerView receivedError:(YTPlayerError)error {
    if(spinnerView != null) [self removeLoadingView];
    if(callback == null) return;
    Method  onStateChange = getMethod(OBJ_CLASS(callback), false, "onStateChange", 1,
                                      J_INT);
    if(error == kYTPlayerErrorVideoNotFound) {
        executeMethod(lifeContext, onStateChange, callback, ERROR_VIDEO_NOT_FOUND);
    } else {
        executeMethod(lifeContext, onStateChange, callback, ERROR_UNKNOWN);
    }
}
-(void)playerView:(YTPlayerView *)playerView didChangeToState:(YTPlayerState)state {
    if (state == kYTPlayerStatePlaying && spinnerView != null)  {
        [self removeLoadingView];
        if(autoPlayFlag == 0) [playerView pauseVideo];
    }
    if(callback == null) return;
    Method  onStateChange = getMethod(OBJ_CLASS(callback), false, "onStateChange", 1,
                                 J_INT);
    if (state == kYTPlayerStateBuffering) {
        executeMethod(lifeContext, onStateChange, callback, STATE_BUFFERING);
    }
    if (state == kYTPlayerStatePlaying) {
        [self removeLoadingView];
        executeMethod(lifeContext, onStateChange, callback, STATE_PLAYING);
    }
    if (state == kYTPlayerStateEnded) {
        executeMethod(lifeContext, onStateChange, callback, STATE_ENDED);
    }
    if (state == kYTPlayerStatePaused) {
        executeMethod(lifeContext, onStateChange, callback, STATE_PAUSED);
    }
    if (state == kYTPlayerStateQueued) {
        executeMethod(lifeContext, onStateChange, callback, STATE_CUED);
    }
    if (state == kYTPlayerStateUnknown) {
        executeMethod(lifeContext, onStateChange, callback, STATE_UNKNOWN);
    }
    if (state == kYTPlayerStateUnstarted) {
        executeMethod(lifeContext, onStateChange, callback, STATE_UNSTARTED);
    }
}
-(void) addLoadingView {
    spinnerView = [[UIView alloc] init];
    spinnerView.frame = DEVICE_CTX->_mainview.view.bounds;
    spinnerView.backgroundColor = [UIColor colorWithRed: 0.5 green:0.5 blue: 0.5 alpha: 0.5];
    UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc] init];
    [spinner startAnimating];
    spinner.center = spinnerView.center;
    [spinnerView addSubview:spinner];
    [DEVICE_CTX->_mainview.view addSubview:spinnerView];
    [spinnerView superview].userInteractionEnabled = false;
}
    
-(void) removeLoadingView {
    if(spinnerView != null) [spinnerView removeFromSuperview];
    DEVICE_CTX->_mainview.view.userInteractionEnabled = true;
    spinnerView = nil;
}
@end


void playYoutube(NMParams p) {
    char url[128];
    if(p->obj[1] != null) String2CharPBuf(p->obj[1], url);
    int autoPlay = p->i32[0];
//    char quality[128];
//    String2CharPBuf(p->obj[3], quality);
//    int policy = p->i32[1];
//    int color = p->i32[2];
//    int option = p->i32[3];
    int start = p->i32[2];
    int end = p->i32[1];
//    char hl[128];
//    if(p->obj[4] != null) String2CharPBuf(p->obj[4], hl);
//    int loop = p->i32[5];
//    int rel = p->i32[6];
//    bool showInfo = p->i32[7];
    

    [[[YoutubeController alloc] initWithCallBack:&p->obj[2]] presentYoutubeVideo: url autoPlay:autoPlay end:end start:start];
}

