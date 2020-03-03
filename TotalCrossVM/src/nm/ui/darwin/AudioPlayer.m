#include <AudioToolbox/AudioToolbox.h>
#import "AudioQueueObject.h"
#import "AudioPlayer.h"


// See "AudioQueueOutputCallback"
static void playbackCallback (
	void					*inUserData,
	AudioQueueRef			inAudioQueue,
	AudioQueueBufferRef		bufferReference
) {
	// This callback, being outside the implementation block, needs a reference to the AudioPlayer object
	AudioPlayer *player = (__bridge AudioPlayer *) inUserData;
	if ([player donePlayingFile]) return;

	UInt32 numBytes;
	UInt32 numPackets = [player numPacketsToRead];

	// This callback is called when the playback audio queue object has an audio queue buffer
	// available for filling with more data from the file being played
	AudioFileReadPackets (
		[player audioFileID],
		NO,
		&numBytes,
		bufferReference->mPacketDescriptions,
		[player startingPacketNumber],
		&numPackets,
		bufferReference->mAudioData
	);

	if (numPackets > 0) {

		bufferReference->mAudioDataByteSize			= numBytes;
		bufferReference->mPacketDescriptionCount	= numPackets;

		AudioQueueEnqueueBuffer (
			inAudioQueue,
			bufferReference,
			0,
			NULL
		);

		[player incrementStartingPacketNumberBy: (UInt32) numPackets];

	} else {

		[player setDonePlayingFile: YES];		// 'donePlayingFile' used by this callback and by setupAudioQueueBuffers

		// if playback is stopping because file is finished, call AudioQueueStop here;
		// if user tapped Stop, then the AudioViewController calls AudioQueueStop
		if (player.audioPlayerShouldStopImmediately == NO) {
			[player stop];
		}

	}
}

// property callback function, invoked when a property changes.
static void propertyListenerCallback (
	void					*inUserData,
	AudioQueueRef			queueObject,
	AudioQueuePropertyID	propertyID
) {
	// This callback, being outside the implementation block, needs a reference to the AudioPlayer object
	AudioPlayer *player = (__bridge AudioPlayer *) inUserData;

	if (player.audioPlayerShouldStopImmediately == YES)
    {
		// If the user tapped Stop, update the UI now. After the AudioPlayer object
		//	and the underlying audio queue object have competely stopped, the
		//	AudioViewController will release them.
#if defined (THEOS)
		[player.notificationDelegate updateUserInterfaceOnAudioQueueStateChange: player];
#endif
	}
    else {
		// if the file reached the end, update the UI after the run loop has finished.
		//	This delay is required to ensure that the AudioPlayer class, and the
		//	underlying audio queue object, are not destroyed while they are still
		//	doing work.
   // guich: all this is probably no longer used
		//[player.notificationDelegate	performSelector: @selector (updateUserInterfaceOnAudioQueueStateChange:)
		//								withObject: player
		//								afterDelay: 0];
	}
}


@implementation AudioPlayer

@synthesize bufferByteSize;
@synthesize gain;
@synthesize numPacketsToRead;
@synthesize donePlayingFile;
@synthesize audioPlayerShouldStopImmediately;


- (id) initWithURL: (CFURLRef) soundFile {

	self = [super init];

	if (self != nil) {

		[self setAudioFileURL: soundFile];
		[self openPlaybackFile: [self audioFileURL]];
		[self setupPlaybackAudioQueueObject];
		[self setDonePlayingFile: NO];
		[self setAudioPlayerShouldStopImmediately: NO];
	}

	return self;
}

// magic cookies are not used by linear PCM audio. this method is included here
//	so this app still works if you change the recording format to one that uses
//	magic cookies.
- (void) copyMagicCookieToQueue: (AudioQueueRef) queue fromFile: (AudioFileID) file {

	UInt32 propertySize = sizeof (UInt32);

	OSStatus result = AudioFileGetPropertyInfo (
							file,
							kAudioFilePropertyMagicCookieData,
							&propertySize,
							NULL
						);

	if (!result && propertySize) {

		char *cookie = (char *) malloc (propertySize);

		AudioFileGetProperty (
			file,
			kAudioFilePropertyMagicCookieData,
			&propertySize,
			cookie
		);

		AudioQueueSetProperty (
			queue,
			kAudioQueueProperty_MagicCookie,
			cookie,
			propertySize
		);

		free (cookie);
	}
}

- (void) openPlaybackFile: (CFURLRef) soundFile {

	AudioFileOpenURL (

		(CFURLRef) self.audioFileURL,
		0x01, //fsRdPerm,						// read only
		kAudioFileCAFType,
		&audioFileID
	);

	UInt32 sizeOfPlaybackFormatASBDStruct = sizeof ([self audioFormat]);

	// get the AudioStreamBasicDescription format for the playback file
	AudioFileGetProperty (

		[self audioFileID],
		kAudioFilePropertyDataFormat,
		&sizeOfPlaybackFormatASBDStruct,
		&audioFormat
	);
}

- (void) setupPlaybackAudioQueueObject {

	// create the playback audio queue object
	AudioQueueNewOutput (
		&audioFormat,
		playbackCallback,
		(__bridge void*) self,
		CFRunLoopGetCurrent (),
		kCFRunLoopCommonModes,
		0,								// run loop flags
		&queueObject
	);

	// set the volume of the playback audio queue
	[self setGain: 1.0];

	AudioQueueSetParameter (
		queueObject,
		kAudioQueueParam_Volume,
		gain
	);

	[self enableLevelMetering];

	// add the property listener callback to the playback audio queue
	AudioQueueAddPropertyListener (
		[self queueObject],
		kAudioQueueProperty_IsRunning,
		propertyListenerCallback,
		(__bridge void*) self
	);

	// copy the audio file's magic cookie to the audio queue object to give it
	// as much info as possible about the audio data to play
	[self copyMagicCookieToQueue: queueObject fromFile: audioFileID];
}

- (void) setupAudioQueueBuffers {

	// calcluate the size to use for each audio queue buffer, and calculate the
	// number of packets to read into each buffer
	[self calculateSizesFor: (Float64) kSecondsPerBuffer];

	// prime the queue with some data before starting
	// allocate and enqueue buffers
	int bufferIndex;
    bool isFormatVBR = (audioFormat.mBytesPerPacket == 0 || audioFormat.mFramesPerPacket == 0);

	for (bufferIndex = 0; bufferIndex < kNumberAudioDataBuffers; ++bufferIndex) {

		// if you intend to support *only* constant bit-rate formats, you can instead
		//	use AudioQueueAllocateBuffers. In this case, the playback callback function
		//	needs to change; you need to change the arguments to the
		//	AudioQueueEnqueueBuffer function. See its reference documentation.
		//	The AudioQueueAllocateBufferWithPacketDescriptions function is available
		//	only in iPhone OS and not in Mac OS X.
		AudioQueueAllocateBufferWithPacketDescriptions (
			[self queueObject],
			[self bufferByteSize],
			(isFormatVBR ? self.numPacketsToRead : 0),
			&buffers[bufferIndex]
		);

		playbackCallback (
			(__bridge void*) self,
			[self queueObject],
			buffers[bufferIndex]
		);

		if ([self donePlayingFile]) break;
	}
}


- (void) play {

	[self setupAudioQueueBuffers];

	AudioQueueStart (
		self.queueObject,
		NULL			// start time. NULL means ASAP.
	);
}

- (void) stop {

	AudioQueueStop (
		self.queueObject,
		self.audioPlayerShouldStopImmediately
	);

	AudioFileClose (self.audioFileID);
}


- (void) pause {

	AudioQueuePause (
		self.queueObject
	);
}


- (void) resume {

	AudioQueueStart (
		self.queueObject,
		NULL			// start time. NULL means ASAP
	);
}


- (void) calculateSizesFor: (Float64) seconds {

	UInt32 maxPacketSize;
	UInt32 propertySize = sizeof (maxPacketSize);

	AudioFileGetProperty (
		audioFileID,
		kAudioFilePropertyPacketSizeUpperBound,
		&propertySize,
		&maxPacketSize
	);

	static const int maxBufferSize = 0x10000;	// limit maximum size to 64K
	static const int minBufferSize = 0x4000;	// limit minimum size to 16K

	if (audioFormat.mFramesPerPacket) {
		Float64 numPacketsForTime = audioFormat.mSampleRate / audioFormat.mFramesPerPacket * seconds;
		[self setBufferByteSize: numPacketsForTime * maxPacketSize];
	} else {
		// if frames per packet is zero, then the codec doesn't know the relationship between
		// packets and time -- so we return a default buffer size
		[self setBufferByteSize: maxBufferSize > maxPacketSize ? maxBufferSize : maxPacketSize];
	}

		// we're going to limit our size to our default
	if (bufferByteSize > maxBufferSize && bufferByteSize > maxPacketSize) {
		[self setBufferByteSize: maxBufferSize];
	} else {
		// also make sure we're not too small - we don't want to go the disk for too small chunks
		if (bufferByteSize < minBufferSize) {
			[self setBufferByteSize: minBufferSize];
		}
	}

	[self setNumPacketsToRead: self.bufferByteSize / maxPacketSize];
}


- (void) dealloc {
	AudioQueueDispose (queueObject,YES);
   [super dealloc];
}

@end
