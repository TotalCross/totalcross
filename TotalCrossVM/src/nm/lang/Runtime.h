
#ifndef Runtime_h
#define Runtime_h
#include "tcvm.h"

#define FILE_STREAM_INPUT 0
#define FILE_STREAM_OUTPUT 1

TCObject createFileStream(Context context, const int streamType, int fd);

#endif