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



#include "tcvm.h"

#include "../../../minizip/zip.h"
#include "../../../minizip/unzip.h"
#include "../../../minizip/ioapi.h"

static bool initializeZipNative(Context currentContext, ZipNativeP zipNativeP, Object streamObj)
{
   zipNativeP->streamRead  = getMethod(OBJ_CLASS(streamObj), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT);
   zipNativeP->streamWrite = getMethod(OBJ_CLASS(streamObj), true, "writeBytes", 3, BYTE_ARRAY, J_INT, J_INT);
   zipNativeP->streamTell  = getMethod(OBJ_CLASS(streamObj), true, "getPos", 0);
   zipNativeP->streamSeek  = getMethod(OBJ_CLASS(streamObj), true, "setPos", 2, J_INT, J_INT);
   zipNativeP->streamClose = getMethod(OBJ_CLASS(streamObj), true, "close", 0);

   if (zipNativeP->streamRead == null || zipNativeP->streamWrite == null || zipNativeP->streamTell == null || zipNativeP->streamSeek == null || zipNativeP->streamClose == null)
      return false;

   zipNativeP->context = currentContext;
   zipNativeP->isFirstFile = true;
   return true;
}

static voidpf zalloc(voidpf opaque, uInt items, uInt size)
{
   UNUSED(opaque)
	return xmalloc(items*size);
}

static void zfree(voidpf opaque, voidpf address)
{
   UNUSED(opaque)
	xfree(address);
}

//////////////////////////////////////////////////////////////////////////
//TC_API void tuzZF_createZipFile_s(NMParams p) // totalcross/util/zip/ZipFile native private totalcross.util.zip.ZipFile createZipFile(String name) throws totalcross.io.IOException;
TC_API void tuzZF_createZipFile_f(NMParams p) // totalcross/util/zip/ZipFile native private totalcross.util.zip.ZipFile createZipFile(totalcross.io.File file) throws totalcross.io.IOException;
{
   Object zipFile = p->obj[0];
   Object streamObj = p->obj[1];
   Object zipNativeObj;
   ZipNativeP zipNativeP;
   unz_global_info unzGlobalInfo;
   Err err;

   if ((zipNativeObj = createByteArray(p->currentContext, sizeof(ZipNative))) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else
   {
      zipNativeP = (ZipNativeP) ARRAYOBJ_START(zipNativeObj);
      initializeZipNative(p->currentContext, zipNativeP, streamObj);

      fill_fopen_filefunc(&zipNativeP->zlib_def);
      zipNativeP->zlib_def.opaque = p->currentContext;

      zipNativeP->zipFile = unzOpen2((char*) streamObj, &zipNativeP->zlib_def);

      if (zipNativeP->zipFile != null)
      {
         err = unzGetGlobalInfo(zipNativeP->zipFile, &unzGlobalInfo);
         if (err == UNZ_OK)
         {
            ZipFile_nativeFile(zipFile) = zipNativeObj;
            ZipFile_size(zipFile) = unzGlobalInfo.number_entry;
            setObjectLock(zipNativeObj, UNLOCKED);

            p->retO = zipFile;
         }
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZF_close(NMParams p) // totalcross/util/zip/ZipFile native public void close() throws totalcross.io.IOException;
{
   Object zipFile = p->obj[0];
   Object nativeFile = ZipFile_nativeFile(zipFile);
   
   if (nativeFile != null)
   {
      unzFile* unzipFile = (unzFile*) ARRAYOBJ_START(nativeFile);
      unzCloseCurrentFile(*unzipFile);
      ZipFile_nativeFile(zipFile) = null;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZF_entries(NMParams p) // totalcross/util/zip/ZipFile native public String[] entries();
{
   Object zipFile = p->obj[0];
   Object nativeFile = ZipFile_nativeFile(zipFile);
   unzFile* unzipFile = (unzFile*) ARRAYOBJ_START(nativeFile);
   int32 size = ZipFile_size(zipFile);
   CharPs* list = null;
   int32 count = 0;
   volatile Object arrayObj = null;
   ObjectArray start, end;
   volatile Heap h;
   Err err;
   int32 i;

   h = heapCreate();
   IF_HEAP_ERROR(h)
   {
      heapDestroy(h);
      throwException(p->currentContext, OutOfMemoryError, null);
      return;
   }

   for(i = 0 ; i < size ; i++)
   {
      CharP filename = (CharP) heapAlloc(h, sizeof(char)*(MAX_PATHNAME));
      unz_file_info file_info;

      err = unzGetCurrentFileInfo(*unzipFile, &file_info, filename, MAX_PATHNAME, null, 0, null, 0);
      if (err != UNZ_OK)
         break;

      list = CharPsAdd(list, filename, h); // add entry to list
      count++;

      if (i + 1 < size)
      {
         err = unzGoToNextFile(*unzipFile);
         if (err != UNZ_OK)
            break;
      }
   }

   if (list != null)
   {
      if ((p->retO = arrayObj = createArrayObject(p->currentContext, "[totalcross.util.zip.ZipEntry", count)) != null)
      {
         start = (ObjectArray) ARRAYOBJ_START(arrayObj);
         end = start + ARRAYOBJ_LEN(arrayObj);
         for (; start < end; start++, list = list->next) // stop also if OutOfMemoryError
         {
            Object name = createStringObjectFromCharP(p->currentContext, list->value, -1);
            *start = createObject(p->currentContext, "totalcross.util.zip.ZipEntry");

            ZipEntry_name(*start) = name;
            setObjectLock(name, UNLOCKED);
            if (*start)
               setObjectLock(*start, UNLOCKED);
            else
               break;
         }
         setObjectLock(p->retO, UNLOCKED);
      }
   }
   heapDestroy(h);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZF_getEntry_s(NMParams p) // totalcross/util/zip/ZipFile native public String getEntry(String name);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZF_getEntryStream_s(NMParams p) // totalcross/util/zip/ZipFile native public totalcross.io.Stream getEntryStream(String name) throws IOException;
{
   Object zipFile = p->obj[0];
   Object nativeFile = ZipFile_nativeFile(zipFile);
   unzFile* unzipFile = (unzFile*) ARRAYOBJ_START(nativeFile);
   Object name = p->obj[1];
   CharP szName = String2CharP(name);

   if (unzLocateFile(*unzipFile, szName, 0) == UNZ_OK)
   {
	   z_stream c_stream; // compression stream
	   c_stream.zalloc = zalloc;
	   c_stream.zfree = zfree;
	   c_stream.opaque = (voidpf) 0;

      if (unzOpenCurrentFile(*unzipFile, &c_stream) == UNZ_OK)
      {
         Object zipStream = createObject(p->currentContext, "totalcross.util.zip.ZipStream");
         //ZipStream_zipFile(zipStream) = zipFile;
         CompressedStream_mode(zipStream) = 2;
         p->retO = zipStream;
         setObjectLock(zipStream, UNLOCKED);
      }
   }

   xfree(szName);
}



//////////////////////////////////////////////////////////////////////////
TC_API void tuzZS_createInflate_s(NMParams p) // totalcross/util/zip/ZipStream native protected Object createInflate(totalcross.io.Stream stream);
{
   Object zipStream = p->obj[0];
   Object streamObj = p->obj[1];
   Object zipNativeObj;
   ZipNativeP zipNativeP;

   if (streamObj == null)
      throwNullArgumentException(p->currentContext, "stream");
   else if ((zipNativeObj = createByteArray(p->currentContext, sizeof(ZipNative))) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else
   {
      zipNativeP = (ZipNativeP) ARRAYOBJ_START(zipNativeObj);
      if (!initializeZipNative(p->currentContext, zipNativeP, streamObj))
         throwException(p->currentContext, IOException, "Failed to initialize zip context");
      else
      {
         fill_fopen_filefunc(&zipNativeP->zlib_def);
         zipNativeP->zlib_def.opaque = zipNativeP;

         if ((zipNativeP->zipFile = unzOpen2((char*) streamObj, &zipNativeP->zlib_def)) == null)
            throwException(p->currentContext, IOException, "Failed to start the zip file");
         *ZipStream_nativeZip(zipStream) = zipNativeObj;
         p->retO = streamObj;
      }
      setObjectLock(zipNativeObj, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZS_createDeflate_si(NMParams p) // totalcross/util/zip/ZipStream native protected Object createDeflate(totalcross.io.Stream stream, int compressionType);
{
   Object zipStream = p->obj[0];
   Object streamObj = p->obj[1];
   Object zipNativeObj;
   ZipNativeP zipNativeP;

   if (streamObj == null)
      throwNullArgumentException(p->currentContext, "stream");
   else if ((zipNativeObj = createByteArray(p->currentContext, sizeof(ZipNative))) == null)
      throwException(p->currentContext, OutOfMemoryError, null);
   else
   {
      zipNativeP = (ZipNativeP) ARRAYOBJ_START(zipNativeObj);
      if (!initializeZipNative(p->currentContext, zipNativeP, streamObj))
         throwException(p->currentContext, IOException, "Failed to initialize zip context");
      else
      {
         fill_fopen_filefunc(&zipNativeP->zlib_def);
         zipNativeP->zlib_def.opaque = zipNativeP;

         if ((zipNativeP->zipFile = zipOpen2((char*) streamObj, APPEND_STATUS_CREATE, null, &zipNativeP->zlib_def)) == null)
            throwException(p->currentContext, IOException, "Failed to start the zip file");
         *ZipStream_nativeZip(zipStream) = zipNativeObj;
         p->retO = streamObj;
      }
      setObjectLock(zipNativeObj, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZS_available(NMParams p) // totalcross/util/zip/ZipStream native public int available() throws IOException;
{
   Object zipStream = p->obj[0];
   Object zipFile = *ZipStream_nativeZip(zipStream);
   Object nativeFile = ZipFile_nativeFile(zipFile);
   unzFile* unzipFile = (unzFile*) ARRAYOBJ_START(nativeFile);
   unz_file_info file_info;
   Err err;

   if ((err = unzGetCurrentFileInfo(*unzipFile, &file_info, null, 0, null, 0, null, 0)) != UNZ_OK)
      throwException(p->currentContext, IOException, null);
   else
      p->retI = file_info.compressed_size - unztell(*unzipFile);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZS_getNextEntry(NMParams p) // totalcross/util/zip/ZipStream native public totalcross.util.zip.ZipEntry getNextEntry() throws IOException;
{
   Object zipStream = p->obj[0];
   Object zipNativeObj = *ZipStream_nativeZip(zipStream);
   ZipNativeP zipNativeP = (ZipNativeP) ARRAYOBJ_START(zipNativeObj);
   int32 mode = CompressedStream_mode(zipStream);
   Object zipEntryObj = null;
   Object zipEntryNameObj = null;
   Object zipEntryCommentObj = null;
   Object zipEntryExtraObj = null;
   char zipEntryName[MAX_PATHNAME];
   CharP zipEntryCommentP = null;
   unz_file_info file_info;
   Err err;

   z_stream c_stream; // compression stream
   c_stream.zalloc = zalloc;
   c_stream.zfree = zfree;
   c_stream.opaque = (voidpf) 0;

   if (mode != 2) // INFLATE
      throwException(p->currentContext, IOException, "This operation can only be performed in INFLATE mode.");
   else 
   {
      if (zipNativeP->isFirstFile)
         err = unzGoToFirstFile(zipNativeP->zipFile);
      else
         err = unzGoToNextFile(zipNativeP->zipFile);

      if (err == UNZ_END_OF_LIST_OF_FILE)
         p->retO = null;
      else if (err != UNZ_OK)
         throwException(p->currentContext, IOException, "Failed to retrieve the next entry");
      else if ((err = unzGetCurrentFileInfo(zipNativeP->zipFile, &file_info, zipEntryName, MAX_PATHNAME, null, 0, null, 0)) != UNZ_OK)
         throwException(p->currentContext, IOException, "Failed to retrieve details for the next entry");
      else if ((zipEntryObj = createObject(p->currentContext, "totalcross.util.zip.ZipEntry")) == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else if ((zipEntryNameObj = createStringObjectFromCharP(p->currentContext, zipEntryName, file_info.size_filename)) == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else if (file_info.size_file_comment > 0 && (zipEntryCommentP = xmalloc(file_info.size_file_comment)) == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else if (file_info.size_file_extra > 0 && (zipEntryExtraObj = createByteArray(p->currentContext, file_info.size_file_extra)) == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else if ((err = unzGetCurrentFileInfo(zipNativeP->zipFile, &file_info, zipEntryName, MAX_PATHNAME, 
         zipEntryCommentP, file_info.size_file_comment, 
         file_info.size_file_comment > 0 ? ARRAYOBJ_START(zipEntryCommentObj) : null, file_info.size_file_extra)) != UNZ_OK) // guich@tc126_31: check for comment length
         throwException(p->currentContext, IOException, "Failed to retrieve details for the next entry");
      else if ((zipEntryCommentObj = createStringObjectFromCharP(p->currentContext, zipEntryCommentP, file_info.size_file_comment)) == null)
         throwException(p->currentContext, OutOfMemoryError, null);
      else if (unzOpenCurrentFile(zipNativeP->zipFile, &c_stream) != UNZ_OK)
         throwException(p->currentContext, IOException, "Failed to open the next entry");
      else
      {
         zipNativeP->isFirstFile = false;
         ZipEntry_name(zipEntryObj) = zipEntryNameObj;
         ZipEntry_time(zipEntryObj) = file_info.dosDate;
         ZipEntry_method(zipEntryObj) = file_info.compression_method;
         ZipEntry_comment(zipEntryObj) = zipEntryCommentObj;
         ZipEntry_extra(zipEntryObj) = zipEntryExtraObj;
         ZipEntry_crc(zipEntryObj) = file_info.crc;
         ZipEntry_size(zipEntryObj) = file_info.uncompressed_size;
         ZipEntry_csize(zipEntryObj) = file_info.compressed_size;
         p->retO = zipEntryObj;
      }
      setObjectLock(zipEntryCommentObj, UNLOCKED);
      setObjectLock(zipEntryNameObj, UNLOCKED);
      setObjectLock(zipEntryExtraObj, UNLOCKED);
      setObjectLock(zipEntryObj, UNLOCKED);
      if (zipEntryCommentP != null)
         xfree(zipEntryCommentP);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZS_putNextEntry_z(NMParams p) // totalcross/util/zip/ZipStream native public void putNextEntry(totalcross.util.zip.ZipEntry zipEntry) throws IOException;
{
   Object zipStream = p->obj[0];
   Object zipNativeObj = *ZipStream_nativeZip(zipStream);
   Object lastEntryObj = *ZipStream_lastEntry(zipStream);
   ZipNativeP zipNativeP = (ZipNativeP) ARRAYOBJ_START(zipNativeObj);
   Object zipEntryObj = p->obj[1];
   Object zipEntryNameObj;
   Object zipEntryCommentObj;
   Object zipEntryExtraObj;
   char zipEntryName[MAX_PATHNAME];
   CharP zipEntryNameP = null;
   CharP zipEntryCommentP = null;
   VoidP zipEntryExtraP = null;
   zip_fileinfo fileInfo;
   int32 method;
   Err err;

   // first check if entry is not null
   if (zipEntryObj == null)
   {
      throwNullArgumentException(p->currentContext, "zipEntry");
      return;
   }

   // check the method to be used...
   if ((method = ZipEntry_method(zipEntryObj)) == -1)
      method = ZipStream_method(zipStream);

   // ... and if the method is STORED, test if all the required fields are set
   if (method != Z_DEFLATED)
   {
      if (ZipEntry_csize(zipEntryObj) >= 0)
      {
         if (ZipEntry_size(zipEntryObj) < 0)
            ZipEntry_size(zipEntryObj) = ZipEntry_csize(zipEntryObj);
         else if (ZipEntry_size(zipEntryObj) != ZipEntry_csize(zipEntryObj))
         {
            throwException(p->currentContext, ZipException, "Method STORED, but compressed size != size");
            return;
         }
      }
      else
         ZipEntry_csize(zipEntryObj) = ZipEntry_size(zipEntryObj);

      if (ZipEntry_size(zipEntryObj) < 0)
      {
         throwException(p->currentContext, ZipException, "Method STORED, but size not set");
         return;
      }
      if (ZipEntry_crc(zipEntryObj) < 0)
      {
         throwException(p->currentContext, ZipException, "Method STORED, but crc not set");
         return;
      }
   }

   // close the previous entry if method was STORED
   if (lastEntryObj != null && zipNativeP->method != Z_DEFLATED)
   {
      int size = 0;
      int crc = 0;

      if (ZipEntry_size(lastEntryObj) > 0)
         size = ZipEntry_size(lastEntryObj);
      if (ZipEntry_crc(lastEntryObj) > 0)
         crc = ZipEntry_crc(lastEntryObj);

      err = zipCloseFileInZipRaw(zipNativeP->zipFile, size, crc);
      *ZipStream_lastEntry(zipStream) = null;

      if (err == ZIP_PARAMERROR)
      {
         throwException(p->currentContext, IOException, "No open entry to be closed");
         return;
      }
      if (err != ZIP_OK)
      {
         throwException(p->currentContext, IOException, null);
         return;
      }
   }

   // passed through all checks, time to get the work done
   if ((zipEntryNameObj = ZipEntry_name(zipEntryObj)) != null)
      zipEntryNameP = String2CharPBuf(zipEntryNameObj, zipEntryName);
   if ((zipEntryCommentObj = ZipEntry_comment(zipEntryObj)) != null)
      zipEntryCommentP = String2CharP(zipEntryCommentObj);
   if ((zipEntryExtraObj = ZipEntry_extra(zipEntryObj)) != null)
      zipEntryExtraP = ARRAYOBJ_START(zipEntryExtraObj);

   xmemzero(&fileInfo, sizeof(zip_fileinfo));
   fileInfo.dosDate = ZipEntry_time(zipEntryObj);

   if ((err = zipOpenNewFileInZip(zipNativeP->zipFile, zipEntryNameP, &fileInfo, 
                  zipEntryExtraP, (zipEntryExtraObj != null ? ARRAYOBJ_LEN(zipEntryExtraObj) : 0),
                  null, 0, 
                  zipEntryCommentP, 
                  method, Z_DEFAULT_COMPRESSION)) != ZIP_OK)
      throwException(p->currentContext, IOException, null);
   else
   {
      zipNativeP->method = method;
      *ZipStream_lastEntry(zipStream) = zipEntryObj;
   }
   xfree(zipEntryCommentP);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZS_closeEntry(NMParams p) // totalcross/util/zip/ZipStream native public void closeEntry() throws IOException;
{
   Object zipStream = p->obj[0];
   Object zipNativeObj = *ZipStream_nativeZip(zipStream);
   Object lastEntryObj = *ZipStream_lastEntry(zipStream);
   ZipNativeP zipNativeP = (ZipNativeP) ARRAYOBJ_START(zipNativeObj);
   int32 mode = CompressedStream_mode(zipStream);
   Err err;

   if (mode == 2) // INFLATE
      err = unzCloseCurrentFile(zipNativeP->zipFile);
   else if (mode == 1) // DEFLATE
   {
      int size = 0;
      int crc = 0;
      if (lastEntryObj != null && zipNativeP->method != Z_DEFLATED)
      {
         if (ZipEntry_size(lastEntryObj) > 0)
            size = ZipEntry_size(lastEntryObj);
         if (ZipEntry_crc(lastEntryObj) > 0)
            crc = ZipEntry_crc(lastEntryObj);
      }
      err = zipCloseFileInZipRaw(zipNativeP->zipFile, size, crc);
      *ZipStream_lastEntry(zipStream) = null;
   }
   else
   {
      throwException(p->currentContext, IOException, "Invalid object");
      return;
   }
   
   if (err == ZIP_PARAMERROR)
      throwException(p->currentContext, IOException, "No open entry to be closed");
   else if (err != ZIP_OK)
      throwException(p->currentContext, IOException, null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZS_readBytes_Bii(NMParams p) // totalcross/util/zip/ZipStream native public int readBytes(byte []buf, int start, int count) throws IOException;
{
   Object zipStream = p->obj[0];
   Object zipNativeObj = *ZipStream_nativeZip(zipStream);
   ZipNativeP zipNativeP = (ZipNativeP) ARRAYOBJ_START(zipNativeObj);
   int32 mode = CompressedStream_mode(zipStream);
   Object buf = p->obj[1];
   int32 start = p->i32[0];
   int32 count = p->i32[1];
   uint8* bufP = ARRAYOBJ_START(buf);

   if (mode != 2) // INFLATE
      throwException(p->currentContext, IOException, "This operation can only be performed in INFLATE mode.");
   else
      p->retI = unzReadCurrentFile(zipNativeP->zipFile, bufP + start, count);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZS_writeBytes_Bii(NMParams p) // totalcross/util/zip/ZipStream native public int writeBytes(byte []buf, int start, int count) throws IOException;
{
   Object zipStream = p->obj[0];
   Object zipNativeObj = *ZipStream_nativeZip(zipStream);
   ZipNativeP zipNativeP = (ZipNativeP) ARRAYOBJ_START(zipNativeObj);
   int32 mode = CompressedStream_mode(zipStream);
   Object buf = p->obj[1];
   int32 start = p->i32[0];
   int32 count = p->i32[1];
   uint8* bufP = ARRAYOBJ_START(buf);

   if (mode != 1) // DEFLATE
      throwException(p->currentContext, IOException, "This operation can only be performed in DEFLATE mode.");
   else
      p->retI = zipWriteInFileInZip(zipNativeP->zipFile, bufP + start, count);  
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZS_close(NMParams p) // totalcross/util/zip/ZipStream native public void close() throws IOException;
{
   Object zipStream = p->obj[0];
   Object zipNativeObj = *ZipStream_nativeZip(zipStream);
   Object lastEntryObj = *ZipStream_lastEntry(zipStream);
   ZipNativeP zipNativeP = (ZipNativeP) ARRAYOBJ_START(zipNativeObj);
   int32 mode = CompressedStream_mode(zipStream);
   Err err;

   executeMethod(p->currentContext, zipNativeP->streamTell, CompressedStream_streamRef(zipStream));
   if (p->currentContext->thrownException != null)
   {
      CompressedStream_mode(zipStream) = 0;
      return;
   }
   
   if (mode == 2) // INFLATE
   {
      if ((err = unzClose(zipNativeP->zipFile)) != UNZ_OK)
         throwException(p->currentContext, IOException, "An error ocurred while closing the ZipStream");
      *ZipStream_nativeZip(zipStream) = null;
      CompressedStream_mode(zipStream) = 0;
   }
   else if (mode == 1) // DEFLATE
   {
      // close the previous entry if method was STORED
      if (lastEntryObj != null && zipNativeP->method != Z_DEFLATED)
      {
         int size = 0;
         int crc = 0;

         if (ZipEntry_size(lastEntryObj) > 0)
            size = ZipEntry_size(lastEntryObj);
         if (ZipEntry_crc(lastEntryObj) > 0)
            crc = ZipEntry_crc(lastEntryObj);

         err = zipCloseFileInZipRaw(zipNativeP->zipFile, size, crc);
         *ZipStream_lastEntry(zipStream) = null;

         if (err == ZIP_PARAMERROR)
         {
            throwException(p->currentContext, IOException, "No open entry to be closed");
            return;
         }
         if (err != ZIP_OK)
         {
            throwException(p->currentContext, IOException, null);
            return;
         }
      }

      if ((err = zipClose(zipNativeP->zipFile, null)) != UNZ_OK)
         throwException(p->currentContext, IOException, "An error ocurred while closing the ZipStream");
      *ZipStream_nativeZip(zipStream) = null;
      CompressedStream_mode(zipStream) = 0;
   }
}

#ifdef ENABLE_TEST_SUITE
//#include "ZipFile_test.h"
#endif
