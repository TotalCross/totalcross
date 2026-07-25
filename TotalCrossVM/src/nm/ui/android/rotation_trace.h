// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TOTALCROSS_ANDROID_ROTATION_TRACE_H
#define TOTALCROSS_ANDROID_ROTATION_TRACE_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

void rotationTraceBeginGeneration(int generation);
void rotationTraceSelectGeneration(int generation);
void rotationTraceEnqueueScreenChanged(int generation, int width, int height);
int rotationTraceSelectScreenChanged(int width, int height);
void rotationTraceStage(const char *stage, int width, int height);
void rotationTraceOnSwap(int width, int height);
void rotationTraceEmitSummary();

JNIEXPORT void JNICALL Java_totalcross_Launcher4A_nativeRotationTraceGeneration
  (JNIEnv *, jobject, jint);
JNIEXPORT void JNICALL Java_totalcross_Launcher4A_nativeRotationTraceScreenChanged
  (JNIEnv *, jobject, jint, jint, jint);

#ifdef __cplusplus
}
#endif

#endif
