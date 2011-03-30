/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#import <GraphicsServices/GraphicsServices.h>
#import <Foundation/Foundation.h>
#import <CoreFoundation/CoreFoundation.h>
#import <CoreSurface/CoreSurface.h>
#import <UIKit/UIKit.h>
#import <UIKit/UITextView.h>

#ifdef darwin9
#import <QuartzCore/CALayer.h>
#define LKLayer CALayer
#else
#import <LayerKit/LKLayer.h>
#endif

@interface ChildView : UIView
{
   int orientation;
   CoreSurfaceBufferRef screenSurface;
   CGContextRef bitmapContext;
   CGImageRef cgImage;
   LKLayer *screenLayer;
   int width, height, pitch;
}
- (id)initWithFrame:(CGRect)rect orientation:(int)orient;
- (void)dealloc;
- (void)updateScreen:(void*)screen;
- (void)drawRect:(CGRect)frame;
- (CoreSurfaceBufferRef)getSurface;
- (unsigned short*)getPixels;

- (bool)isPortrait;
- (bool)isLandscape;

- (void)fixCoord: (CGPoint*)p;

#ifdef darwin9
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event;
- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event;
#else
- (void)gestureChanged:(GSEvent*)event;
- (void)gestureEnded:(GSEvent*)event;
- (void)gestureStarted:(GSEvent*)event;
- (void)keyDown:(GSEvent*)event;
- (void)keyUp:(GSEvent*)event;
- (void)mouseDown:(GSEvent*)event;
- (void)mouseDragged:(GSEvent*)event;
- (void)mouseEntered:(GSEvent*)event;
- (void)mouseExited:(GSEvent*)event;
- (void)mouseMoved:(GSEvent*)event;
- (void)mouseUp:(GSEvent*)event;
#endif

- (void)screenChange:(int)w height:(int)h;
- (void)addEvent:(NSDictionary*)event;

@end
