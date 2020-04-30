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

#import "imageImage.h"
#import <ImageIO/ImageIO.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import <UIKit/UIKit.h>
#include "xtypes.h"

@implementation imageImage

//Unused, kept for future reference. Maybe write the image to the photo album?
/*
-(void)saveImage:(NSString*) imagePath {
    UIImage *image=[UIImage imageWithContentsOfFile:imagePath];
    UIImageWriteToSavedPhotosAlbum(image, self, @selector(savedPhotoImage:didFinishSavingWithError:contextInfo:), nil);
}
*/

@end

void CGImageWriteToFile(CGImageRef image, NSString *path) {  
    CFURLRef url = (__bridge CFURLRef) [NSURL fileURLWithPath:path];
    CGImageDestinationRef destination = CGImageDestinationCreateWithURL(url, kUTTypeJPEG, 1, nil);
    CGImageDestinationAddImage(destination, image, nil);

    if (!CGImageDestinationFinalize(destination)) {
        NSLog(@"Failed to write image to %@", path);
    }
}

void resizeImageAtPath (char* src, char* dst, int maxPixelSize) {
	NSString *srcString = [[NSString alloc] initWithCString:src encoding:NSWindowsCP1252StringEncoding];
	NSString *dstString = [[NSString alloc] initWithCString:dst encoding:NSWindowsCP1252StringEncoding];

    // Create the image source
    CGImageSourceRef imageSrc = CGImageSourceCreateWithURL((__bridge CFURLRef) [NSURL fileURLWithPath:srcString], nil);
    
    // Create thumbnail options
    CFDictionaryRef options;
    if (maxPixelSize > 0) {
	    options = (__bridge CFDictionaryRef) @{
	            (id) kCGImageSourceCreateThumbnailWithTransform : @YES,
	            (id) kCGImageSourceCreateThumbnailFromImageAlways : @YES,
	            (id) kCGImageSourceThumbnailMaxPixelSize : @(maxPixelSize)
		};
    } else {
	    options = (__bridge CFDictionaryRef) @{
	            (id) kCGImageSourceCreateThumbnailWithTransform : @YES,
	            (id) kCGImageSourceCreateThumbnailFromImageAlways : @YES
	    };
    }
    
    // Generate the thumbnail
    CGImageRef thumbnail = CGImageSourceCreateThumbnailAtIndex(imageSrc, 0, options); 
    CFRelease(imageSrc);
    
    // Write the thumbnail at path
    CGImageWriteToFile(thumbnail, dstString);
}
