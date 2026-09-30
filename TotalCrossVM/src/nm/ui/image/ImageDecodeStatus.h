// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef IMAGE_DECODE_STATUS_H
#define IMAGE_DECODE_STATUS_H

typedef enum
{
   IMAGE_DECODE_SUCCESS,
   IMAGE_DECODE_CORRUPT,
   IMAGE_DECODE_RESOURCE_FAILURE
} ImageDecodeStatus;

int imageDecodeConsumeAllocationFailureForTest(void);
int imageDecodeConsumeFinalBufferFailureForTest(void);

#endif
