//
//  main.m
//  TotalCross
//
//  Created by Guilherme Hazan on 3/7/12.
//  Copyright (c) 2012 SuperWaba Ltda. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "AppDelegate.h"

void notifyStopVM(); // mainview.m

int main(int argc, char *argv[])
{
    @autoreleasepool 
    {
        int ret = UIApplicationMain(argc, argv, nil, NSStringFromClass([AppDelegate class]));
        notifyStopVM();
        return ret;
    }
}
