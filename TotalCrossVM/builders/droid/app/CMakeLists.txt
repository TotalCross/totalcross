



cmake_minimum_required(VERSION 3.4.1)

SET(CMAKE_C_FLAGS  "${CMAKE_C_FLAGS} -Wno-everything -std=c11")
SET(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -Wno-everything")

set(TC_SRCDIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../src")
set(LB_SRCDIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../src/litebase")
set(SKIA_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../deps/skia")


set(SOURCES
    ${TC_SRCDIR}/axtls/aes.c
    ${TC_SRCDIR}/axtls/asn1.c
    ${TC_SRCDIR}/axtls/bigint.c
    ${TC_SRCDIR}/axtls/crypto_misc.c
    ${TC_SRCDIR}/axtls/hmac.c
    ${TC_SRCDIR}/axtls/os_port.c
    ${TC_SRCDIR}/axtls/loader.c
    ${TC_SRCDIR}/axtls/md5.c
    ${TC_SRCDIR}/axtls/md2.c
    ${TC_SRCDIR}/axtls/openssl.c
    ${TC_SRCDIR}/axtls/p12.c
    ${TC_SRCDIR}/axtls/rsa.c
    ${TC_SRCDIR}/axtls/rc4.c
    ${TC_SRCDIR}/axtls/sha1.c
    ${TC_SRCDIR}/axtls/sha256.c
    ${TC_SRCDIR}/axtls/sha384.c
    ${TC_SRCDIR}/axtls/sha512.c
    ${TC_SRCDIR}/axtls/x509.c
    ${TC_SRCDIR}/axtls/tls1.c
    ${TC_SRCDIR}/axtls/tls1_svr.c
    ${TC_SRCDIR}/axtls/tls1_clnt.c
    
    ${TC_SRCDIR}/openbsd/libutil/pkcs5_pbkdf2.c

    ${TC_SRCDIR}/tcvm/tcthread.c
    ${TC_SRCDIR}/tcvm/tcclass.c
    ${TC_SRCDIR}/tcvm/objectmemorymanager.c
    ${TC_SRCDIR}/tcvm/tcmethod.c
    ${TC_SRCDIR}/tcvm/tcfield.c
    ${TC_SRCDIR}/tcvm/context.c
    ${TC_SRCDIR}/tcvm/tcexception.c
    ${TC_SRCDIR}/tcvm/tcvm.c

    ${TC_SRCDIR}/init/demo.c
    ${TC_SRCDIR}/init/globals.c
    ${TC_SRCDIR}/init/nativeProcAddressesTC.c
    ${TC_SRCDIR}/init/startup.c
    ${TC_SRCDIR}/init/settings.c

    ${TC_SRCDIR}/util/jchar.c
    ${TC_SRCDIR}/util/datastructures.c
    ${TC_SRCDIR}/util/debug.c
    ${TC_SRCDIR}/util/tcz.c
    ${TC_SRCDIR}/util/utils.c
    ${TC_SRCDIR}/util/dlmalloc.c
    ${TC_SRCDIR}/util/mem.c
    ${TC_SRCDIR}/util/errormsg.c
    ${TC_SRCDIR}/util/nativelib.c
    ${TC_SRCDIR}/util/guid.c
    ${TC_SRCDIR}/util/xtypes.c

    ${TC_SRCDIR}/minizip/ioapi.c
    ${TC_SRCDIR}/minizip/unzip.c
    ${TC_SRCDIR}/minizip/zip.c

    ${TC_SRCDIR}/zlib/adler32.c
    ${TC_SRCDIR}/zlib/compress.c
    ${TC_SRCDIR}/zlib/crc32.c
    ${TC_SRCDIR}/zlib/deflate.c
    ${TC_SRCDIR}/zlib/infback.c
    ${TC_SRCDIR}/zlib/inffast.c
    ${TC_SRCDIR}/zlib/inflate.c
    ${TC_SRCDIR}/zlib/inftrees.c
    ${TC_SRCDIR}/zlib/trees.c
    ${TC_SRCDIR}/zlib/uncompr.c
    ${TC_SRCDIR}/zlib/zutil.c

    ${TC_SRCDIR}/ras/ras_Utils.c

    ${TC_SRCDIR}/nm/map/GoogleMaps.c

    ${TC_SRCDIR}/nm/crypto/AESCipher.c
    ${TC_SRCDIR}/nm/crypto/MD5Digest.c
    ${TC_SRCDIR}/nm/crypto/PKCS1Signature.c
    ${TC_SRCDIR}/nm/crypto/RSACipher.c
    ${TC_SRCDIR}/nm/crypto/SHA1Digest.c
    ${TC_SRCDIR}/nm/crypto/SHA256Digest.c

    ${TC_SRCDIR}/nm/io/PDBFile.c
    ${TC_SRCDIR}/nm/io/File.c
    ${TC_SRCDIR}/nm/io/device_PortConnector.c
    ${TC_SRCDIR}/nm/io/device/RadioDevice.c
    ${TC_SRCDIR}/nm/io/device/scanner/zxing.c
    ${TC_SRCDIR}/nm/io/device/gps/GPS.c

    ${TC_SRCDIR}/nm/io/device/bluetooth/DiscoveryAgent.c
    ${TC_SRCDIR}/nm/io/device/bluetooth/SerialPortClient.c
    ${TC_SRCDIR}/nm/io/device/bluetooth/SerialPortServer.c

    ${TC_SRCDIR}/nm/lang/Reflection.c
    ${TC_SRCDIR}/nm/lang/Class.c
    ${TC_SRCDIR}/nm/lang/cpproc.c
    ${TC_SRCDIR}/nm/lang/Runtime.c
    ${TC_SRCDIR}/nm/lang/Object.c
    ${TC_SRCDIR}/nm/lang/String.c
    ${TC_SRCDIR}/nm/lang/StringBuffer.c
    ${TC_SRCDIR}/nm/lang/Thread.c
    ${TC_SRCDIR}/nm/lang/Throwable.c

    ${TC_SRCDIR}/nm/net/ssl_SSL.c
    ${TC_SRCDIR}/nm/net/ServerSocket.c
    ${TC_SRCDIR}/nm/net/Socket.c
    ${TC_SRCDIR}/nm/net/ConnectionManager.c

    ${TC_SRCDIR}/nm/pim/POutlook.c

    ${TC_SRCDIR}/nm/money/money.c
    
    ${TC_SRCDIR}/nm/notification/notification.c
    
    ${TC_SRCDIR}/nm/phone/Dial.c
    ${TC_SRCDIR}/nm/phone/SMS.c
    ${TC_SRCDIR}/nm/phone/CellInfo.c
    ${TC_SRCDIR}/nm/telephony/SmsManager.c
    ${TC_SRCDIR}/nm/firebase/firebase.c

    ${TC_SRCDIR}/nm/sys/CharacterConverter.c
    ${TC_SRCDIR}/nm/sys/Registry.c
    ${TC_SRCDIR}/nm/sys/Vm.c
    ${TC_SRCDIR}/nm/sys/Time.c
    ${TC_SRCDIR}/nm/sys/Convert.c

    ${TC_SRCDIR}/nm/ui/android/skia.cpp
    ${TC_SRCDIR}/nm/ui/gfx_Graphics.c
    ${TC_SRCDIR}/nm/ui/event_Event.c
    ${TC_SRCDIR}/nm/ui/Control.c
    ${TC_SRCDIR}/nm/ui/font_Font.c
    ${TC_SRCDIR}/nm/ui/font_FontMetrics.c
    ${TC_SRCDIR}/nm/ui/image_Image.c
    ${TC_SRCDIR}/nm/ui/MainWindow.c
    ${TC_SRCDIR}/nm/ui/media_Sound.c
    ${TC_SRCDIR}/nm/ui/media_MediaClip.c
    ${TC_SRCDIR}/nm/ui/media_Camera.c
    ${TC_SRCDIR}/nm/ui/YoutubePlayer.c
    ${TC_SRCDIR}/nm/ui/Window.c

    ${TC_SRCDIR}/nm/util/concurrent_Lock.c
    ${TC_SRCDIR}/nm/util/zip_ZLib.c
    ${TC_SRCDIR}/nm/util/BigInteger.c
    ${TC_SRCDIR}/nm/util/Locale.c

    ${TC_SRCDIR}/nm/util/zip/CompressedStream.c
    ${TC_SRCDIR}/nm/util/zip/ZipFile.c
    ${TC_SRCDIR}/nm/util/zip/ZipEntry.c

    ${TC_SRCDIR}/png/pngrutil.c
    ${TC_SRCDIR}/png/pngerror.c
    ${TC_SRCDIR}/png/pngget.c
    ${TC_SRCDIR}/png/PngLoader.c
    ${TC_SRCDIR}/png/pngmem.c
    ${TC_SRCDIR}/png/pngpread.c
    ${TC_SRCDIR}/png/pngread.c
    ${TC_SRCDIR}/png/pngrio.c
    ${TC_SRCDIR}/png/pngrtran.c
    ${TC_SRCDIR}/png/png.c
    ${TC_SRCDIR}/png/pngset.c
    ${TC_SRCDIR}/png/pngtrans.c

    ${TC_SRCDIR}/jpeg/jcapimin.c
    ${TC_SRCDIR}/jpeg/jcapistd.c
    ${TC_SRCDIR}/jpeg/jccoefct.c
    ${TC_SRCDIR}/jpeg/jccolor.c
    ${TC_SRCDIR}/jpeg/jcdctmgr.c
    ${TC_SRCDIR}/jpeg/jchuff.c
    ${TC_SRCDIR}/jpeg/jcinit.c
    ${TC_SRCDIR}/jpeg/jcmainct.c
    ${TC_SRCDIR}/jpeg/jcmarker.c
    ${TC_SRCDIR}/jpeg/jcmaster.c
    ${TC_SRCDIR}/jpeg/jcomapi.c
    ${TC_SRCDIR}/jpeg/jcparam.c
    ${TC_SRCDIR}/jpeg/jcphuff.c
    ${TC_SRCDIR}/jpeg/jcprepct.c
    ${TC_SRCDIR}/jpeg/jcsample.c
    ${TC_SRCDIR}/jpeg/jdapimin.c
    ${TC_SRCDIR}/jpeg/jdapistd.c
    ${TC_SRCDIR}/jpeg/jdatadst.c
    ${TC_SRCDIR}/jpeg/jdatasrc.c
    ${TC_SRCDIR}/jpeg/jdcoefct.c
    ${TC_SRCDIR}/jpeg/jdcolor.c
    ${TC_SRCDIR}/jpeg/jddctmgr.c
    ${TC_SRCDIR}/jpeg/jdhuff.c
    ${TC_SRCDIR}/jpeg/jdinput.c
    ${TC_SRCDIR}/jpeg/jdmainct.c
    ${TC_SRCDIR}/jpeg/jdmarker.c
    ${TC_SRCDIR}/jpeg/jdmaster.c
    ${TC_SRCDIR}/jpeg/jdphuff.c
    ${TC_SRCDIR}/jpeg/jdpostct.c
    ${TC_SRCDIR}/jpeg/jdsample.c
    ${TC_SRCDIR}/jpeg/jerror.c
    ${TC_SRCDIR}/jpeg/jfdctfst.c
    ${TC_SRCDIR}/jpeg/jmemmgr.c
    ${TC_SRCDIR}/jpeg/jmemnobs.c
    ${TC_SRCDIR}/jpeg/JpegLoader.c
    ${TC_SRCDIR}/jpeg/jquant1.c
    ${TC_SRCDIR}/jpeg/jquant2.c
    ${TC_SRCDIR}/jpeg/jutils.c
    ${TC_SRCDIR}/jpeg/jcarith.c
    ${TC_SRCDIR}/jpeg/jdarith.c
    ${TC_SRCDIR}/jpeg/jdmerge.c
    ${TC_SRCDIR}/jpeg/jidctflt.c
    ${TC_SRCDIR}/jpeg/jidctfst.c
    ${TC_SRCDIR}/jpeg/jidctint.c
    ${TC_SRCDIR}/jpeg/jfdctflt.c
    ${TC_SRCDIR}/jpeg/jfdctint.c
    ${TC_SRCDIR}/jpeg/jaricom.c
    ${TC_SRCDIR}/jpeg/rdbmp.c

    ${TC_SRCDIR}/event/Event.c
    ${TC_SRCDIR}/event/specialkeys.c

    ${TC_SRCDIR}/nm/xml/xml_XmlTokenizer.c

    ${TC_SRCDIR}/palmdb/palmdb.c

    ${TC_SRCDIR}/sqlite/sqlite3.c
    ${TC_SRCDIR}/nm/db/NativeDB.c

    ${TC_SRCDIR}/scanner/android/Android_barcode.c
    
    ${TC_SRCDIR}/nm/cielo/printer/PrinterManager.c

    ${TC_SRCDIR}/nm/qrcode/qrcode_generation.c
    ${TC_SRCDIR}/nm/qrcode/qrcode.c

    ${TC_SRCDIR}/nm/io/device/gpiod/Gpiod.c

    ${LB_SRCDIR}/lbFile.c
    ${LB_SRCDIR}/PlainDB.c
    ${LB_SRCDIR}/TCVMLib.c
    ${LB_SRCDIR}/Litebase.c
    ${LB_SRCDIR}/ResultSet.c
    ${LB_SRCDIR}/NativeMethods.c
    ${LB_SRCDIR}/Table.c
    ${LB_SRCDIR}/LitebaseGlobals.c
    ${LB_SRCDIR}/nativeProcAddressesLB.c
    ${LB_SRCDIR}/Key.c
    ${LB_SRCDIR}/Node.c
    ${LB_SRCDIR}/Index.c
    ${LB_SRCDIR}/SQLValue.c
    ${LB_SRCDIR}/MarkBits.c
    ${LB_SRCDIR}/MemoryFile.c
    ${LB_SRCDIR}/NormalFile.c
    ${LB_SRCDIR}/PreparedStatement.c
    ${LB_SRCDIR}/UtilsLB.c

    ${LB_SRCDIR}/parser/LitebaseLex.c
    ${LB_SRCDIR}/parser/LitebaseMessage.c
    ${LB_SRCDIR}/parser/LitebaseParser.c
    ${LB_SRCDIR}/parser/SQLBooleanClause.c
    ${LB_SRCDIR}/parser/SQLBooleanClauseTree.c
    ${LB_SRCDIR}/parser/SQLColumnListClause.c
    ${LB_SRCDIR}/parser/SQLDeleteStatement.c
    ${LB_SRCDIR}/parser/SQLInsertStatement.c
    ${LB_SRCDIR}/parser/SQLSelectStatement.c
    ${LB_SRCDIR}/parser/SQLUpdateStatement.c
    )


include_directories(${TC_SRCDIR})
include_directories(${TC_SRCDIR}/axtls)
include_directories(${TC_SRCDIR}/dx)
include_directories(${TC_SRCDIR}/event)
include_directories(${TC_SRCDIR}/event/android)
include_directories(${TC_SRCDIR}/init)
include_directories(${TC_SRCDIR}/init/android)
include_directories(${TC_SRCDIR}/jpeg)
include_directories(${TC_SRCDIR}/litebase)
include_directories(${TC_SRCDIR}/litebase/parser)
include_directories(${TC_SRCDIR}/minizip)
include_directories(${TC_SRCDIR}/nm)
include_directories(${TC_SRCDIR}/nm/crypto)
include_directories(${TC_SRCDIR}/nm/db)
include_directories(${TC_SRCDIR}/nm/firebase)
include_directories(${TC_SRCDIR}/nm/io)
include_directories(${TC_SRCDIR}/nm/lang)
include_directories(${TC_SRCDIR}/nm/map)
include_directories(${TC_SRCDIR}/nm/money)
include_directories(${TC_SRCDIR}/nm/net)
include_directories(${TC_SRCDIR}/nm/phone)
include_directories(${TC_SRCDIR}/nm/pim)
include_directories(${TC_SRCDIR}/nm/sys)
include_directories(${TC_SRCDIR}/nm/telephony)
include_directories(${TC_SRCDIR}/nm/ui)
include_directories(${TC_SRCDIR}/nm/ui/android)
include_directories(${TC_SRCDIR}/nm/xml)
include_directories(${TC_SRCDIR}/palmdb)
include_directories(${TC_SRCDIR}/png)
include_directories(${TC_SRCDIR}/ras)
include_directories(${TC_SRCDIR}/scanner)
include_directories(${TC_SRCDIR}/scanner/android)
include_directories(${TC_SRCDIR}/sqlite)
include_directories(${TC_SRCDIR}/sync)
include_directories(${TC_SRCDIR}/tcvm)
include_directories(${TC_SRCDIR}/util)
include_directories(${TC_SRCDIR}/util/android)
include_directories(${TC_SRCDIR}/zlib)
include_directories(${TC_SRCDIR}/nm/qrcode)

include_directories(${SKIA_DIR}/android)
include_directories(${SKIA_DIR}/atlastext)
include_directories(${SKIA_DIR}/c)
include_directories(${SKIA_DIR}/codec)
include_directories(${SKIA_DIR}/config)
include_directories(${SKIA_DIR}/core)
include_directories(${SKIA_DIR}/effects)
include_directories(${SKIA_DIR}/encode)
include_directories(${SKIA_DIR}/gpu)
include_directories(${SKIA_DIR}/mac)
include_directories(${SKIA_DIR}/pathops)
include_directories(${SKIA_DIR}/ports)
include_directories(${SKIA_DIR}/private)
include_directories(${SKIA_DIR}/svg)
include_directories(${SKIA_DIR}/utils)


add_definitions(-DTOTALCROSS -DTC_EXPORTS -DANDROID)
if ("${ANDROID_ABI}" MATCHES "arm64-v8a")
	add_definitions(-DFORCE_LIBC_ALLOC)
endif()

add_library(tcvm SHARED ${SOURCES})
add_library(libskia SHARED IMPORTED)
set_target_properties( # Specifies the target library.
                       libskia

                       # Specifies the parameter you want to define.
                       PROPERTIES IMPORTED_LOCATION

                       # Provides the path to the library you want to import.
                       ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libskia.so )


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )
find_library( # Sets the name of the path variable.
              jnigraphics-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              jnigraphics )              

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.
                       tcvm
                       libskia
                       GLESv2
                       EGL
                       android
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} 
                       ${jnigraphics-lib} )
