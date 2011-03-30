#import <UIKit/UIKit.h>
#include <AudioToolbox/AudioToolbox.h>

#define kNumberAudioDataBuffers	3

@interface AudioQueueObject : NSObject {

	AudioQueueRef					queueObject;					// the audio queue object being used for playback
	AudioFileID						audioFileID;					// the identifier for the audio file to play
	CFURLRef						audioFileURL;
	Float64							hardwareSampleRate;
	AudioStreamBasicDescription		audioFormat;
	AudioQueueLevelMeterState		*audioLevels;
	SInt64							startingPacketNumber;			// the current packet number in the playback file
	id								notificationDelegate;
}

@property (readwrite)			AudioQueueRef				queueObject;
@property (readwrite)			AudioFileID					audioFileID;
@property (readwrite)			CFURLRef					audioFileURL;
@property (readwrite)			Float64						hardwareSampleRate;
@property (readwrite)			AudioStreamBasicDescription	audioFormat;
@property (readwrite)			AudioQueueLevelMeterState	*audioLevels;
@property (readwrite)			SInt64						startingPacketNumber;
@property (nonatomic, retain)	id							notificationDelegate;


- (void) incrementStartingPacketNumberBy:  (UInt32) inNumPackets;
- (void) setNotificationDelegate: (id) inDelegate;
- (void) enableLevelMetering;
- (void) getAudioLevels: (Float32 *) levels peakLevels: (Float32 *) peakLevels;
- (BOOL) isRunning;

@end