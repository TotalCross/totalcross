//
//  YoutubePlayerController.h
//  tcvm
//
//  Created by Italo Yeltsin Medeiros Bruno on 22/05/19.
//  Copyright © 2019 SuperWaba Ltda. All rights reserved.
//

#ifndef YoutubePlayerController_h
#define YoutubePlayerController_h
@interface YoutubeController: UIViewController<YTPlayerViewDelegate, UIWebViewDelegate>
    {
        YTPlayerView* playerView;
    }
    - (instancetype)init;
    
@end

#endif /* YoutubePlayerController_h */
