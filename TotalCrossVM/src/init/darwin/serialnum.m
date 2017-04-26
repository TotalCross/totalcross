/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#import <Foundation/Foundation.h>
#import <UIKit/UIDevice.h>
#import "UIDevice+IdentifierAddition.h"
#import <Security/Security.h>
// DeviceUID.h
#import <Foundation/Foundation.h>
@interface DeviceUID : NSObject
+ (NSString *)uid;
@end

@interface DeviceUID ()

@property(nonatomic, strong, readonly) NSString *uidKey;
@property(nonatomic, strong, readonly) NSString *uid;

@end

@implementation DeviceUID

@synthesize uid = _uid;

#pragma mark - Public methods

+ (NSString *)uid {
    return [[[DeviceUID alloc] initWithKey:@"deviceUID"] uid];
}

#pragma mark - Instance methods

- (id)initWithKey:(NSString *)key {
    self = [super init];
    if (self) {
        _uidKey = key;
        _uid = nil;
    }
    return self;
}

/*! Returns the Device UID.
    The UID is obtained in a chain of fallbacks:
      - Keychain
      - NSUserDefaults
      - Apple IFV (Identifier for Vendor)
      - Generate a random UUID if everything else is unavailable
    At last, the UID is persisted if needed to.
 */
- (NSString *)uid {
    if (!_uid) _uid = [[self class] valueForKeychainKey:_uidKey service:_uidKey];
    if (!_uid) _uid = [[self class] valueForUserDefaultsKey:_uidKey];
    if (!_uid) _uid = [[self class] appleIFV];
    if (!_uid) _uid = [[self class] randomUUID];
    [self save];
    return _uid;
}

/*! Persist UID to NSUserDefaults and Keychain, if not yet saved
 */
- (void)save {
    if (![DeviceUID valueForUserDefaultsKey:_uidKey]) {
        [DeviceUID setValue:_uid forUserDefaultsKey:_uidKey];
    }
    if (![DeviceUID valueForKeychainKey:_uidKey service:_uidKey]) {
        [DeviceUID setValue:_uid forKeychainKey:_uidKey inService:_uidKey];
    }
}

#pragma mark - Keychain methods

/*! Create as generic NSDictionary to be used to query and update Keychain items.
 *  param1
 *  param2
 */
+ (NSMutableDictionary *)keychainItemForKey:(NSString *)key service:(NSString *)service {
    NSMutableDictionary *keychainItem = [[NSMutableDictionary alloc] init];
    keychainItem[(__bridge id)kSecClass] = (__bridge id)kSecClassGenericPassword;
    keychainItem[(__bridge id)kSecAttrAccessible] = (__bridge id)kSecAttrAccessibleAlways;
    keychainItem[(__bridge id)kSecAttrAccount] = key;
    keychainItem[(__bridge id)kSecAttrService] = service;
    return keychainItem;
}

/*! Sets
 *  param1
 *  param2
 */
+ (OSStatus)setValue:(NSString *)value forKeychainKey:(NSString *)key inService:(NSString *)service {
    NSMutableDictionary *keychainItem = [[self class] keychainItemForKey:key service:service];
    keychainItem[(__bridge id)kSecValueData] = [value dataUsingEncoding:NSUTF8StringEncoding];
    return SecItemAdd((__bridge CFDictionaryRef)keychainItem, NULL);
}

+ (NSString *)valueForKeychainKey:(NSString *)key service:(NSString *)service {
    OSStatus status;
    NSMutableDictionary *keychainItem = [[self class] keychainItemForKey:key service:service];
    keychainItem[(__bridge id)kSecReturnData] = (__bridge id)kCFBooleanTrue;
    keychainItem[(__bridge id)kSecReturnAttributes] = (__bridge id)kCFBooleanTrue;
    CFDictionaryRef result = nil;
    status = SecItemCopyMatching((__bridge CFDictionaryRef)keychainItem, (CFTypeRef *)&result);
    if (status != noErr) {
        return nil;
    }
    NSDictionary *resultDict = (__bridge_transfer NSDictionary *)result;
    NSData *data = resultDict[(__bridge id)kSecValueData];
    if (!data) {
        return nil;
    }
    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

#pragma mark - NSUserDefaults methods

+ (BOOL)setValue:(NSString *)value forUserDefaultsKey:(NSString *)key {
    [[NSUserDefaults standardUserDefaults] setObject:value forKey:key];
    return [[NSUserDefaults standardUserDefaults] synchronize];
}

+ (NSString *)valueForUserDefaultsKey:(NSString *)key {
    return [[NSUserDefaults standardUserDefaults] objectForKey:key];
}

#pragma mark - UID Generation methods

+ (NSString *)appleIFV {
    if(NSClassFromString(@"UIDevice") && [UIDevice instancesRespondToSelector:@selector(identifierForVendor)]) {
        // only available in iOS >= 6.0
        return [[UIDevice currentDevice].identifierForVendor UUIDString];
    }
    return nil;
}

+ (NSString *)randomUUID {
    if(NSClassFromString(@"NSUUID")) {
        return [[NSUUID UUID] UUIDString];
    }
    CFUUIDRef uuidRef = CFUUIDCreate(kCFAllocatorDefault);
    CFStringRef cfuuid = CFUUIDCreateString(kCFAllocatorDefault, uuidRef);
    CFRelease(uuidRef);
    NSString *uuid = [((__bridge NSString *) cfuuid) copy];
    CFRelease(cfuuid);
    return uuid;
}

@end

void getSerialNum(char *id, int maxlen)
{
    float ver = [[[UIDevice currentDevice] systemVersion] floatValue];
    NSString* nsserial = ver >= 6 ? [DeviceUID uid] : // new for iOS 6 or above
                                    [[UIDevice currentDevice] uniqueGlobalDeviceIdentifier];
    if (nsserial != nil)
    {
       const char* serial = [nsserial cStringUsingEncoding:NSASCIIStringEncoding];
       strncpy(id, serial, maxlen);
    }
}

int getRomVersion()
{
   float ver = [[[UIDevice currentDevice] systemVersion] floatValue];
   return (int) (ver * 100);
}
