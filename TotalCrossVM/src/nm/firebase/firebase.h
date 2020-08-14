#import <Foundation/Foundation.h>

@interface FirebaseTCCallback : NSObject

+ (void) privateOnMessageReceived: (NSString*)messageId messageType: (NSString*)messageType keys:
			(NSArray*)keys values: (NSArray*)values collapsedKey: (NSString*)collapsedKey ttl: (int)ttl;

+ (void) privateOnTokenRefresh;

@end
