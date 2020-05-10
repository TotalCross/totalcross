// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda. 
//
// TotalCross Software Development Kit
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef YoutubePlayerController_h
#define YoutubePlayerController_h
@interface YoutubeController: UIViewController<YTPlayerViewDelegate, UIWebViewDelegate>
    {
        YTPlayerView* playerView;
    }
    - (instancetype)init;
    
@end

#endif /* YoutubePlayerController_h */
