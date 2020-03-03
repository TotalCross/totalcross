//
//  tcvm.h
//  tcvm
//
//  Created by Guilherme Hazan on 3/13/12.
//  Copyright (c) 2012 SuperWaba Ltda. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface tcvm : NSObject

+ (NSInteger) startVM:(Context*)context appName:(char *)appName;

+ (NSInteger) startProgram:(Context) context;

+ (void) privateOnMessageReceived: (NSString *)messageId messageType:(NSString *)messageType keys:(NSArray *) keys values:(NSArray *)values collapsedKey:(NSString *) collapsedKey ttl:(int)ttl;

+ (void) privateOnTokenRefresh;

@end
