// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef INSTANCEFIELDS_H
#define INSTANCEFIELDS_H

/*
   This header contains the definitions to access some instance fields of the classes.
   Note that static fields must be retrieved using the getStaticFieldXXX functions
   available in tcvm.h.

   You should declare these fields only to access the ones declared in the current class.

   Do NOT place casts to structures in this header file.

   Here's an example of how to use it:

     int32 size = Font_size(myFontObj);
     size += 2;
     Font_style(myFontObj) = size;

   You can use these macros (defined in class.h) to access the fields of various types:

      FIELD_I32(o,idx)     FIELD_OBJ(o,c,idx)    FIELD_I64(o,c,idx)    FIELD_DBL(o,c,idx)

   . o is the Object's instance
   . c is the object's class that can be retrieved with OBJ_CLASS(o)
   . idx is the index in the declaration order of the source code

   Note that FIELD_I32 does not require the class argument.

   In a class, at first are fields from the super classes, then the fields from the current class.
*/

// java.lang.String
#define String_chars(o)             ((o)->asObj)  // String has a single field, "char[] chars", and since it is widely used, we'll do an optimization here, not using FIELD_OBJ
#define String_charsLen(o)          (ARRAYOBJ_LEN(String_chars(o)))
#define String_charsStart(o)        ((JCharP)(ARRAYOBJ_START(String_chars(o))))

// java.lang.StringBuffer
#define StringBuffer_chars(o)       FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define StringBuffer_charsLen(o)    (ARRAYOBJ_LEN(StringBuffer_chars(o)))
#define StringBuffer_charsStart(o)  ((JCharP)(ARRAYOBJ_START(StringBuffer_chars(o))))
#define StringBuffer_count(o)       FIELD_I32(o, 0)

// java.lang.Class
#define Class_nativeStruct(o)       FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Class_targetName(o)         FIELD_OBJ(o, OBJ_CLASS(o), 1)

// java.lang.reflect.Field
#define Field_index(o)              FIELD_I32(o, 0)
#define Field_mod(o)                FIELD_I32(o, 1)
#define Field_primitiveType(o)      FIELD_I32(o, 2)
#define Field_nativeStruct(o)       FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Field_name(o)               FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define Field_declaringClass(o)     FIELD_OBJ(o, OBJ_CLASS(o), 2)
#define Field_type(o)               FIELD_OBJ(o, OBJ_CLASS(o), 3)

// java.lang.reflect.Method
#define Method_mod(o)                FIELD_I32(o, 0)
#define Method_nativeStruct(o)       FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Method_name(o)               FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define Method_declaringClass(o)     FIELD_OBJ(o, OBJ_CLASS(o), 2)
#define Method_parameterTypes(o)     FIELD_OBJ(o, OBJ_CLASS(o), 3) // array
#define Method_exceptionTypes(o)     FIELD_OBJ(o, OBJ_CLASS(o), 4) // array
#define Method_type(o)               FIELD_OBJ(o, OBJ_CLASS(o), 5) // not for Constructor
#define Method_returnType(o)         FIELD_OBJ(o, OBJ_CLASS(o), 6) // not for Constructor

// java.lang Wrappers
#define Boolean_v(o)        FIELD_I32(o, 0)
#define Byte_v(o)           FIELD_I32(o, 0)
#define Character_v(o)      FIELD_I32(o, 0)
#define Short_v(o)          FIELD_I32(o, 0)
#define Integer_v(o)        FIELD_I32(o, 0)
#define Float_v(o)          FIELD_DBL(o, OBJ_CLASS(o), 0)
#define Double_v(o)         FIELD_DBL(o, OBJ_CLASS(o), 0)
#define Long_v(o)           FIELD_I64(o, OBJ_CLASS(o), 0)

//totalcross.sys.Time
#define Time_year(o)                FIELD_I32(o, 0)
#define Time_month(o)               FIELD_I32(o, 1)
#define Time_day(o)                 FIELD_I32(o, 2)
#define Time_hour(o)                FIELD_I32(o, 3)
#define Time_minute(o)              FIELD_I32(o, 4)
#define Time_second(o)              FIELD_I32(o, 5)
#define Time_millis(o)              FIELD_I32(o, 6)

// totalcross.ui.font.Font
#define Font_name(o)                FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Font_hvUserFont(o)          FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define Font_fm(o)                  FIELD_OBJ(o, OBJ_CLASS(o), 2)
#define Font_style(o)               FIELD_I32(o, 0)
#define Font_size(o)                FIELD_I32(o, 1)
#define Font_skiaIndex(o)           FIELD_I32(o, 2)

// totalcross.ui.font.FontMetrics
#define FontMetrics_font(o)         FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define FontMetrics_ascent(o)       FIELD_I32(o, 0)
#define FontMetrics_descent(o)      FIELD_I32(o, 1)

// totalcross.ui.gfx.Rect
#define Rect_x(o)                   FIELD_I32(o, 0)
#define Rect_y(o)                   FIELD_I32(o, 1)
#define Rect_width(o)               FIELD_I32(o, 2)
#define Rect_height(o)              FIELD_I32(o, 3)

// totalcross.ui.gfx.Coord
#define Coord_x(o)                  FIELD_I32(o, 0)
#define Coord_y(o)                  FIELD_I32(o, 1)

// totalcross.ui.gfx.Color
#define Color_rgb(o)                FIELD_I32(o, 0)

// totalcross.ui.gfx.Graphics
#define Graphics_foreColor(o)       FIELD_I32(o, 0)
#define Graphics_backColor(o)       FIELD_I32(o, 1)
#define Graphics_useAA(o)           FIELD_I32(o, 2)
#define Graphics_width(o)           FIELD_I32(o, 3)
#define Graphics_height(o)          FIELD_I32(o, 4)
#define Graphics_transX(o)          FIELD_I32(o, 5)
#define Graphics_transY(o)          FIELD_I32(o, 6)
#define Graphics_clipX1(o)          FIELD_I32(o, 7)
#define Graphics_clipY1(o)          FIELD_I32(o, 8)
#define Graphics_clipX2(o)          FIELD_I32(o, 9)
#define Graphics_clipY2(o)          FIELD_I32(o, 10)
#define Graphics_minX(o)            FIELD_I32(o, 11)
#define Graphics_minY(o)            FIELD_I32(o, 12)
#define Graphics_maxX(o)            FIELD_I32(o, 13)
#define Graphics_maxY(o)            FIELD_I32(o, 14)
#define Graphics_lastRX(o)          FIELD_I32(o, 15)
#define Graphics_lastRY(o)          FIELD_I32(o, 16)
#define Graphics_lastXC(o)          FIELD_I32(o, 17)
#define Graphics_lastYC(o)          FIELD_I32(o, 18)
#define Graphics_lastSize(o)        FIELD_I32(o, 19)
#define Graphics_pitch(o)           FIELD_I32(o, 20)
#define Graphics_alpha(o)           FIELD_I32(o, 21)
#define Graphics_isVerticalText(o)  FIELD_I32(o, 22)
#define Graphics_lastClipFactor(o)  FIELD_I32(o, 23)

#define Graphics_lastPPD(o)         FIELD_DBL(o, OBJ_CLASS(o), 0)

#define Graphics_surface(o)         FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Graphics_font(o)            FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define Graphics_xPoints(o)         FIELD_OBJ(o, OBJ_CLASS(o), 2)
#define Graphics_yPoints(o)         FIELD_OBJ(o, OBJ_CLASS(o), 3)
#define Graphics_ints(o)            FIELD_OBJ(o, OBJ_CLASS(o), 4)

// totalcross.ui.image.Image
#define Image_width(o)              FIELD_I32(o, 1)
#define Image_height(o)             FIELD_I32(o, 2)
#define Image_frameCount(o)         FIELD_I32(o, 3)
#define Image_currentFrame(o)       FIELD_I32(o, 4)
#define Image_widthOfAllFrames(o)   FIELD_I32(o, 5)
#define Image_transparentColor(o)   FIELD_I32(o, 6)
#define Image_useAlpha(o)           FIELD_I32(o, 7)
#define Image_alphaMask(o)          FIELD_I32(o, 8)
#define Image_lastAccess(o)         FIELD_I32(o, 9)
#define Image_textureId(o)         FIELD_I32(o, 10)

#define Image_pixels(o)             FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Image_pixelsOfAllFrames(o)  FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define Image_comment(o)            FIELD_OBJ(o, OBJ_CLASS(o), 2)
#define Image_gfx(o)                FIELD_OBJ(o, OBJ_CLASS(o), 3)
#define Image_changed(o)            *((int32*)ARRAYOBJ_START(FIELD_OBJ(o, OBJ_CLASS(o), 4)))
#define Image_instanceCount(o)      *((int32*)ARRAYOBJ_START(FIELD_OBJ(o, OBJ_CLASS(o), 5)))

#define Image_hwScaleW(o)         FIELD_DBL(o, OBJ_CLASS(o), 0)
#define Image_hwScaleH(o)         FIELD_DBL(o, OBJ_CLASS(o), 1)

#define ImageOrControl_surfaceType(o) FIELD_I32(o, 0)

// totalcross.ui.Control
#define Control_x(o)                FIELD_I32(o, 1)
#define Control_y(o)                FIELD_I32(o, 2)
#define Control_width(o)            FIELD_I32(o, 3)
#define Control_height(o)           FIELD_I32(o, 4)

// generic surface - can only be used if "o" is a Control or Image
#define Surface_isImage(o)          (o && ImageOrControl_surfaceType(o) == 1)
#define Graphics_isImageSurface(g)  (Surface_isImage(Graphics_surface(g)))
#ifdef __gl2_h_
#define Graphics_useOpenGL(g)             (!Graphics_isImageSurface(g))
#else
#define Graphics_useOpenGL(g)             (false)
#endif

// totalcross.io.ByteArrayStream
#define ByteArrayStream_pos(o)       FIELD_I32(o, 0)
#define ByteArrayStream_buffer(o)    FIELD_OBJ(o, OBJ_CLASS(o), 0)

// totalcross.io.File
#define File_path(o)                *getInstanceFieldObject(o, "path", "totalcross.io.File")
#define File_fileRef(o)             *getInstanceFieldObject(o, "fileRef", "totalcross.io.File")
#define File_mode(o)                *getInstanceFieldInt(o, "mode", "totalcross.io.File")
#define File_slot(o)                *getInstanceFieldInt(o, "slot", "totalcross.io.File")
#define File_dontFinalize(o)        *getInstanceFieldInt(o, "dontFinalize", "totalcross.io.File")
#define File_pos(o)                 *getInstanceFieldInt(o, "pos", "totalcross.io.RandomAccessStream")

// totalcross.io.PDBFile
#define PDBFile_dbId(o)             FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define PDBFile_openRef(o)          FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define PDBFile_name(o)             FIELD_OBJ(o, OBJ_CLASS(o), 2)
#define PDBFile_hvRecordPos(o)      FIELD_I32(o, 0)
#define PDBFile_hvRecordOffset(o)   FIELD_I32(o, 1)
#define PDBFile_hvRecordLength(o)   FIELD_I32(o, 2)
#define PDBFile_hvRecordChanged(o)  FIELD_I32(o, 3)
#define PDBFile_dontFinalize(o)     FIELD_I32(o, 4)
#define PDBFile_mode(o)             FIELD_I32(o, 5)
#define PDBFile_hvRecordHandle(o)   FIELD_I64(o, OBJ_CLASS(o), 0)

// totalcross.io.device.PortConnector
#define PortConnector_portConnector(o)             FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define PortConnector_receiveBuffer(o)             FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define PortConnector_portNumber(o)                FIELD_I32(o, 0)
#define PortConnector_readTimeout(o)               FIELD_I32(o, 1)
#define PortConnector_writeTimeout(o)              FIELD_I32(o, 2)
#define PortConnector_stopWriteCheckOnTimeout(o)   FIELD_I32(o, 3)
#define PortConnector_dontFinalize(o)              FIELD_I32(o, 4)

// totalcross.io.sync.Conduit
#define Conduit_syncTarget(o)                   getInstanceFieldInt(o, "syncTarget", "totalcross.io.sync.Conduit")
#define Conduit_conduitHandle(o)                getInstanceFieldObject(o, "conduitHandle", "totalcross.io.sync.Conduit")
#define Conduit_conduitName(o)                  *getInstanceFieldObject(o, "conduitName", "totalcross.io.sync.Conduit")
#define Conduit_targetApplicationId(o)          *getInstanceFieldObject(o, "targetApplicationId", "totalcross.io.sync.Conduit")
#define Conduit_targetAppPath(o)               	getInstanceFieldObject(o, "targetAppPath", "totalcross.io.sync.Conduit")

// totalcross.io.sync.RemotePDBFile
#define RemotePDBFile_name(o)             	FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define RemotePDBFile_mode(o)                FIELD_I32(o, 0)
#define RemotePDBFile_open(o)                FIELD_I32(o, 1)
#define RemotePDBFile_lastSearchedRec(o)     FIELD_I32(o, 4)
#define RemotePDBFile_dontFinalize(o)        FIELD_I32(o, 5)
#define RemotePDBFile_pdbHandle(o)           FIELD_OBJ(o, OBJ_CLASS(o), 1)

// totalcross.io.sync.RemotePDBRecord
#define RemotePDBRecord_remotePDBFile(o)  getInstanceFieldObject(o, "rc", "totalcross.io.sync.RemotePDBRecord")
#define RemotePDBRecord_size(o)           FIELD_I32(o, 0)

// totalcross.io.device.bluetooth.UUID
#define UUID_bytes(o)                        FIELD_OBJ(o, OBJ_CLASS(o), 0)

// totalcross.io.device.bluetooth.DiscoveryAgent
#define DiscoveryAgent_deviceInquiryListener(o)       FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define DiscoveryAgent_inquiryNativeFields(o)         FIELD_OBJ(o, OBJ_CLASS(o), 1)

// totalcross.io.device.bluetooth.ServiceRecord
#define ServiceRecord_protocol(o)            FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define ServiceRecord_address(o)             FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define ServiceRecord_channel(o)             FIELD_I32(o, 0)

// totalcross.io.device.bluetooth.RemoteDevice
#define RemoteDevice_friendlyName(o)    FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define RemoteDevice_address(o)         FIELD_OBJ(o, OBJ_CLASS(o), 1)

// totalcross.io.device.bluetooth.SerialPortServer
#define SerialPortServer_nativeHandle(o)    FIELD_OBJ(o, OBJ_CLASS(o), 0)

// totalcross.io.device.bluetooth.SerialPortClient
#define SerialPortClient_nativeHandle(o)    FIELD_OBJ(o, OBJ_CLASS(o), 0)

// totalcross.io.device.gps.GPS
#define GPS_location(o)                   *getInstanceFieldObject(o, "location", "totalcross.io.device.gps.GPS")
#define GPS_latitude(o)                   ((double*) ARRAYOBJ_START(GPS_location(o)))[0]
#define GPS_longitude(o)                  ((double*) ARRAYOBJ_START(GPS_location(o)))[1]
#define GPS_direction(o)                  *getInstanceFieldDouble(o, "direction", "totalcross.io.device.gps.GPS")
#define GPS_velocity(o)                   *getInstanceFieldDouble(o, "velocity", "totalcross.io.device.gps.GPS")
#define GPS_satellites(o)                 *getInstanceFieldInt(o, "satellites", "totalcross.io.device.gps.GPS")
#define GPS_lastFix(o)                    *getInstanceFieldObject(o, "lastFix", "totalcross.io.device.gps.GPS")
#define GPS_pdop(o)                       *getInstanceFieldDouble(o, "pdop", "totalcross.io.device.gps.GPS")
#define GPS_lowSignalReason(o)            *getInstanceFieldObject(o, "lowSignalReason", "totalcross.io.device.gps.GPS")
#define GPS_messageReceived(o)            *getInstanceFieldObject(o, "messageReceived", "totalcross.io.device.gps.GPS") 
#define GPS_precision(o)                  *getInstanceFieldInt(o, "precision", "totalcross.io.device.gps.GPS")

// totalcross.net.Socket
#define Socket_socketRef(o)               FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Socket_readTimeout(o)             FIELD_I32(o, 0)
#define Socket_writeTimeout(o)            FIELD_I32(o, 1)
#define Socket_dontFinalize(o)            FIELD_I32(o, 2)

// totalcross.net.ServerSocket
#define ServerSocket_serverRef(o)         FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define ServerSocket_addr(o)              FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define ServerSocket_port(o)              FIELD_I32(o, 0)
#define ServerSocket_timeout(o)           FIELD_I32(o, 1)
#define ServerSocket_dontFinalize(o)      FIELD_I32(o, 2)

// totalcross.net.ssl.SSL
#define SSL_sslRef(o)                     FIELD_I64(o, OBJ_CLASS(o), 0)
#define SSL_sslDontFinalize(o)            FIELD_I32(o, 0)
#define SSL_nativeHeap(o)                 getInstanceFieldObject(o, "nativeHeap", "totalcross.net.ssl.SSL")

// totalcross.net.ssl.SSLCTX
#define SSLCTX_ctxRef(o)                  FIELD_I64(o, OBJ_CLASS(o), 0)
#define SSLCTX_dontFinalize(o)            FIELD_I32(o, 0)
#define SSLCTX_nativeHeap(o)              getInstanceFieldObject(o, "nativeHeap", "totalcross.net.ssl.SSLCTX")

// totalcross.net.ssl.SSLReadHolder
#define SSLReadHolder_buf(o)              FIELD_OBJ(o, OBJ_CLASS(o), 0)

// totalcross.ui.media.MediaClip
#define MediaClip_mediaClipRef(o)         FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define MediaClip_mediaClipStream(o)      FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define MediaClip_path(o)                 FIELD_OBJ(o, OBJ_CLASS(o), 2)
#define MediaClip_mediaHeader(o)          FIELD_OBJ(o, OBJ_CLASS(o), 3)
#define MediaClip_mediaSize(o)            FIELD_I32(o, 0)
#define MediaClip_state(o)                FIELD_I32(o, 1)
#define MediaClip_loaded(o)               FIELD_I32(o, 2)
#define MediaClip_size(o)                 FIELD_I32(o, 3)
#define MediaClip_dontFinalize(o)         FIELD_I32(o, 4)
#define MediaClip_stopped(o)              FIELD_I32(o, 5)
#define MediaClip_finished(o)             FIELD_I32(o, 6)
#define MediaClip_isRecording(o)          FIELD_I32(o, 7)
#define MediaClip_dataPos(o)              FIELD_I32(o, 8)
#define MediaClip_internalState(o)        FIELD_I32(o, 9)

// java.lang.Throwable
#define Throwable_msg(o)                  getInstanceFieldObject(o, "msg", "java.lang.Throwable") // this may be an object that extends throwable
#define Throwable_trace(o)                getInstanceFieldObject(o, "trace", "java.lang.Throwable")

// totalcross.xml.XmlTokenizer
#define XmlTokenizer_endTagToSkipTo(o)    FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define XmlTokenizer_bag(o)               FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define XmlTokenizer_ofsStart(o)          FIELD_I32(o, 0)
#define XmlTokenizer_ofsCur(o)            FIELD_I32(o, 1)
#define XmlTokenizer_ofsEnd(o)            FIELD_I32(o, 2)
#define XmlTokenizer_readPos(o)           FIELD_I32(o, 3)
#define XmlTokenizer_state(o)             FIELD_I32(o, 4)
#define XmlTokenizer_substate(o)          FIELD_I32(o, 5)
#define XmlTokenizer_ixEndTagToSkipTo(o)  FIELD_I32(o, 6)
#define XmlTokenizer_quote(o)             FIELD_I32(o, 7)
#define XmlTokenizer_strictlyXml(o)       FIELD_I32(o, 8)
#define XmlTokenizer_resolveCharRef(o)    FIELD_I32(o, 9)

// java.lang.Thread
#define Thread_taskID(o)                  FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Thread_runnable(o)                FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define Thread_priority(o)                FIELD_I32(o, 0)
#define Thread_alive(o)                   FIELD_I32(o, 1)

// totalcross.crypto.digest.Digest
#define Digest_digestRef(o)               getInstanceFieldObject(o, "digestRef", "totalcross.crypto.digest.Digest")

// totalcross.crypto.cipher.Cipher
#define Cipher_cipherRef(o)               getInstanceFieldObject(o, "cipherRef", "totalcross.crypto.cipher.Cipher")
#define Cipher_keyRef(o)                  getInstanceFieldObject(o, "keyRef", "totalcross.crypto.cipher.Cipher")
#define Cipher_operation(o)               getInstanceFieldInt(o, "operation", "totalcross.crypto.cipher.Cipher")
#define Cipher_key(o)                     getInstanceFieldObject(o, "key", "totalcross.crypto.cipher.Cipher")
#define Cipher_chaining(o)                getInstanceFieldInt(o, "chaining", "totalcross.crypto.cipher.Cipher")
#define Cipher_iv(o)                      getInstanceFieldObject(o, "iv", "totalcross.crypto.cipher.Cipher")
#define Cipher_padding(o)                 getInstanceFieldInt(o, "padding", "totalcross.crypto.cipher.Cipher")
#define Cipher_nativeHeap(o)              getInstanceFieldObject(o, "nativeHeap", "totalcross.crypto.cipher.Cipher")

// totalcross.crypto.cipher.AESKey
#define AESKey_data(o)                    FIELD_OBJ(o, OBJ_CLASS(o), 0)

// totalcross.crypto.cipher.RSAPublicKey
#define RSAPublicKey_e(o)                 FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define RSAPublicKey_n(o)                 FIELD_OBJ(o, OBJ_CLASS(o), 1)

// totalcross.crypto.cipher.RSAPrivateKey
#define RSAPrivateKey_e(o)                FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define RSAPrivateKey_d(o)                FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define RSAPrivateKey_n(o)                FIELD_OBJ(o, OBJ_CLASS(o), 2)

// totalcross.crypto.signature.Signature
#define Signature_signatureRef(o)         getInstanceFieldObject(o, "signatureRef", "totalcross.crypto.signature.Signature")
#define Signature_keyRef(o)               getInstanceFieldObject(o, "keyRef", "totalcross.crypto.signature.Signature")
#define Signature_operation(o)            getInstanceFieldInt(o, "operation", "totalcross.crypto.signature.Signature")
#define Signature_key(o)                  getInstanceFieldObject(o, "key", "totalcross.crypto.signature.Signature")

// totalcross.crypto.signature.PKCS1Signature
#define PKCS1Signature_digest(o)          getInstanceFieldObject(o, "digest", "totalcross.crypto.signature.PKCS1Signature")

// totalcross.ui.media.Camera
#define Camera_initialDir(o)       FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define Camera_defaultFileName(o)  FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define Camera_title(o)            FIELD_OBJ(o, OBJ_CLASS(o), 2)
#define Camera_stillQuality(o)     FIELD_I32(o, 0)
#define Camera_videoType(o)        FIELD_I32(o, 1)
#define Camera_resolutionWidth(o)  FIELD_I32(o, 2)
#define Camera_resolutionHeight(o) FIELD_I32(o, 3)
#define Camera_videoTimeLimit(o)   FIELD_I32(o, 4)
#define Camera_captureMode(o)      FIELD_I32(o, 5)
#define Camera_allowRotation(o)    FIELD_I32(o, 6)
#define Camera_cameraType(o)       FIELD_I32(o, 7)

// totalcross.util.concurrent.Lock
#define Lock_mutex(o)        FIELD_OBJ(o, OBJ_CLASS(o), 0)

// totalcross.util.zip.CompressedStream
#define CompressedStream_streamRef(o)              *getInstanceFieldObject(o, "compressedStream", "totalcross.util.zip.CompressedStream")
#define CompressedStream_streamBuffer(o)           *getInstanceFieldObject(o, "streamBuffer", "totalcross.util.zip.CompressedStream")
#define CompressedStream_mode(o)                   *getInstanceFieldInt(o, "mode", "totalcross.util.zip.CompressedStream")

// totalcross.util.zip.ZipFile
#define ZipFile_nativeFile(o)          FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define ZipFile_size(o)                FIELD_I32(o, 0)

// totalcross.util.zip.ZipEntry
#define ZipEntry_name(o)               FIELD_OBJ(o, OBJ_CLASS(o), 0)
#define ZipEntry_time(o)               FIELD_I32(o, 0)
#define ZipEntry_crc(o)                FIELD_I64(o, OBJ_CLASS(o), 0)
#define ZipEntry_size(o)               FIELD_I64(o, OBJ_CLASS(o), 1)
#define ZipEntry_csize(o)              FIELD_I64(o, OBJ_CLASS(o), 2)
#define ZipEntry_method(o)             FIELD_I32(o, 1)
#define ZipEntry_extra(o)              FIELD_OBJ(o, OBJ_CLASS(o), 1)
#define ZipEntry_comment(o)            FIELD_OBJ(o, OBJ_CLASS(o), 2)

// totalcross.io.ZipStream
#define ZipStream_nativeZip(o)         getInstanceFieldObject(o, "nativeZip", "totalcross.util.zip.ZipStream")
#define ZipStream_lastEntry(o)         getInstanceFieldObject(o, "lastEntry", "totalcross.util.zip.ZipStream")
#define ZipStream_method(o)            *getInstanceFieldInt(o, "defaultMethod", "totalcross.util.zip.ZipStream")

// totalcross.telephony.SmsManager
#define SmsManager_smsReceiver(o)                     *getInstanceFieldObject(o, "receiver", "totalcross.telephony.SmsManager")
#define SmsMessage_displayOriginatingAddress(o)       *getInstanceFieldObject(o, "displayOriginatingAddress", "totalcross.telephony.SmsMessage")
#define SmsMessage_displayMessageBody(o)              *getInstanceFieldObject(o, "displayMessageBody", "totalcross.telephony.SmsMessage")
#define SmsMessage_userData(o)                        *getInstanceFieldObject(o, "userData", "totalcross.telephony.SmsMessage")

//totalcross.notification.Notification
#define Notification_title(o)                     	*getInstanceFieldObject(o, "title", "totalcross.notification.Notification")
#define Notification_text(o)       					*getInstanceFieldObject(o, "text", "totalcross.notification.Notification")

#define GpiodChip_handle(o)             *getInstanceFieldObject(o, "handle", "totalcross.io.device.gpiod.GpiodChip")
#define GpiodLine_handle(o)             *getInstanceFieldObject(o, "handle", "totalcross.io.device.gpiod.GpiodLine")

#endif
