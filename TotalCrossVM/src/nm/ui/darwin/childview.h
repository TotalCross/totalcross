// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#import <Foundation/Foundation.h>
#import <CoreFoundation/CoreFoundation.h>
#import <UIKit/UIKit.h>
#import <QuartzCore/CALayer.h>
#import <QuartzCore/QuartzCore.h>
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>

@interface ChildView : UIView
{
   bool isMultitouching;
   int shiftY;
   UIViewController* controller;
   EAGLContext *glcontext;
	GLuint defaultFramebuffer, colorRenderbuffer;
   bool firstCall;
   UIDeviceOrientation lastKnownOrientation;
@public
   int taskbarHeight;
}
- (CGRect)getBounds;
- (CGSize)getResolution;
- (UIDeviceOrientation)getOrientation;
- (id)init:(UIViewController*) ctrl;
- (void)setScreenValues:(void*)screen;
- (void)doRotate;
- (void)updateScreen;
- (void)createGLcontext;

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event;

@end
