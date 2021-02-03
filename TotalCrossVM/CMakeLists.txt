# Copyright (C) 2020 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

cmake_minimum_required(VERSION 3.5)

# set the project name
project(TCVM VERSION 7.0.2)

if(NOT DEFINED BUILD_SHARED_LIBS AND NOT DEFINED TCVM_SHARED)
  if(CMAKE_GENERATOR STREQUAL Xcode)
    set(TCVM_SHARED OFF) 
    message("tcvm: ${TCVM_SHARED}")  
  else() # others
    set(TCVM_SHARED ON) 
  endif()
endif()

# Find SDL2 and Skia
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/modules")

# Find SDL2
IF (NOT (DEFINED ANDROID_ABI OR MSVC OR CMAKE_GENERATOR STREQUAL Xcode))
  find_package(SDL2 REQUIRED)
  include_directories(${SDL2_INCLUDE_DIR})
ENDIF (NOT (DEFINED ANDROID_ABI OR MSVC OR CMAKE_GENERATOR STREQUAL Xcode))

# Find Skia
if(NOT MSVC)
  find_package(Skia REQUIRED)
  include_directories(${SKIA_INCLUDE_DIRS})
endif(NOT MSVC)

set(TC_SRCDIR "${CMAKE_CURRENT_SOURCE_DIR}/src")
set(LB_SRCDIR "${CMAKE_CURRENT_SOURCE_DIR}/src/litebase")
set(IOS_BUNDLE_SRCDIR "${CMAKE_CURRENT_SOURCE_DIR}/xcode")

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
    ${TC_SRCDIR}/nm/lang/Runtime.c
    ${TC_SRCDIR}/nm/lang/Object.c
    ${TC_SRCDIR}/nm/lang/String.c
    ${TC_SRCDIR}/nm/lang/StringBuffer.c
    ${TC_SRCDIR}/nm/lang/Thread.c
    ${TC_SRCDIR}/nm/lang/Throwable.c
    ${TC_SRCDIR}/nm/lang/Process.c
    ${TC_SRCDIR}/nm/nio/channels/FileChannelImpl.c

    ${TC_SRCDIR}/nm/net/ssl_SSL.c
    ${TC_SRCDIR}/nm/net/ServerSocket.c
    ${TC_SRCDIR}/nm/net/Socket.c
    ${TC_SRCDIR}/nm/net/ConnectionManager.c

    ${TC_SRCDIR}/nm/pim/POutlook.c

    ${TC_SRCDIR}/nm/notification/notification.c
    
    ${TC_SRCDIR}/nm/phone/Dial.c
    ${TC_SRCDIR}/nm/phone/SMS.c
    ${TC_SRCDIR}/nm/phone/CellInfo.c
    ${TC_SRCDIR}/nm/telephony/TelephonyManager.c
    ${TC_SRCDIR}/nm/telephony/SmsManager.c
    ${TC_SRCDIR}/nm/firebase/firebase.c

    ${TC_SRCDIR}/nm/sys/CharacterConverter.c
    ${TC_SRCDIR}/nm/sys/Registry.c
    ${TC_SRCDIR}/nm/sys/Vm.c
    ${TC_SRCDIR}/nm/sys/Time.c
    ${TC_SRCDIR}/nm/sys/Convert.c

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

    ${TC_SRCDIR}/palmdb/palmdb.c

    ${TC_SRCDIR}/sqlite/sqlite3.c
    ${TC_SRCDIR}/nm/db/NativeDB.c

    ${TC_SRCDIR}/scanner/ios/ios_barcode.c

    ${TC_SRCDIR}/nm/cielo/printer/PrinterManager.c

    ${TC_SRCDIR}/nm/qrcode/qrcode_generation.c

    ${TC_SRCDIR}/nm/io/device/gpiod/Gpiod.c

    )

if(UNIX AND NOT (DEFINED ANDROID_ABI OR APPLE))
  set(SOURCES
    ${SOURCES}
    ${TC_SRCDIR}/nm/io/device/gpiod/posix/gpiodLib.c
  )
endif(UNIX AND NOT (DEFINED ANDROID_ABI OR APPLE))

if(MSVC)
  set(TC_MSVC_SOURCES
    ${TC_SRCDIR}/util/winsockLib.c
    ${TC_SRCDIR}/nm/qrcode/win/qrcodegen.c
  )
endif(MSVC)

if(NOT MSVC)
  set(TC_NOT_MSVC_SOURCES
    ${TC_SRCDIR}/nm/lang/posix/cpproc.c
    ${TC_SRCDIR}/nm/qrcode/qrcode.c
  )
endif(NOT MSVC)

if(DEFINED SKIA_LIBRARIES)
  set(TC_SKIA_SOURCES
    ${TC_SRCDIR}/nm/ui/android/skia.cpp
  )
endif(DEFINED SKIA_LIBRARIES)

if(NOT MSVC)
  set(LB_SOURCES
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
endif(NOT MSVC)

if(CMAKE_GENERATOR STREQUAL Xcode)
  set(TCVM_iOS_FILES
    ${IOS_BUNDLE_SRCDIR}/tcvm/libtcvm.h
    ${IOS_BUNDLE_SRCDIR}/tcvm/libtcvm.m
    ${IOS_BUNDLE_SRCDIR}/tcvm/tcvm-Prefix.pch
    
    #event\\darwin
    ${TC_SRCDIR}/event/darwin/event.m

    #init\\darwin
    ${TC_SRCDIR}/init/darwin/demo_c.m
    ${TC_SRCDIR}/init/darwin/serialnum.m
    ${TC_SRCDIR}/init/darwin/startup_c.m
    ${TC_SRCDIR}/init/darwin/serialnum.m

    #util\\darwin
    ${TC_SRCDIR}/util/darwin/debug_c.m
    
    #nm\\ui\\darwin
    ${TC_SRCDIR}/nm/ui/darwin/mainview.m
    ${TC_SRCDIR}/nm/ui/darwin/media_Camera_c.m
    ${TC_SRCDIR}/nm/ui/darwin/Window_c.m
    ${TC_SRCDIR}/nm/ui/darwin/YoutubePlayerController.m
    ${TC_SRCDIR}/nm/ui/darwin/childview.m
    ${TC_SRCDIR}/nm/ui/darwin/sipargs.m

    #nm\\ui\\darwin
    ${TC_SRCDIR}/nm/ui/darwin/image_Image_c.m
    ${TC_SRCDIR}/nm/ui/darwin/media_Sound_c.m
  
    #nm\\notification\\darwin
    ${TC_SRCDIR}/nm/notification/darwin/notification_c.m

    #nm\\sys\\darwin
    ${TC_SRCDIR}/nm/sys/darwin/Vm_c.m

    #nm\\firebase\\darwin
    ${TC_SRCDIR}/nm/firebase/darwin/firebase_c.m

    #nm\\util\\darwin
    ${TC_SRCDIR}/nm/util/darwin/Locale.m

    #nm\\phone\\posix
    ${TC_SRCDIR}/nm/phone/posix/Dial_c.m
    
    
  )
endif()

if(DEFINED SDL2_LIBRARY)
  set(TC_SDL_SOURCES
    ${TC_SRCDIR}/init/tcsdl.cpp
  )
endif(DEFINED SDL2_LIBRARY)

if(DEFINED ANDROID_ABI)
  set(TC_ANDROID_SOURCES
    ${TC_SRCDIR}/nm/money/money.c
  )
endif(DEFINED ANDROID_ABI)

if(MSVC AND ${CMAKE_GENERATOR} STREQUAL "Visual Studio 9 2008 Pocket PC 2003 (ARMV4)")
  set(TC_WCE_SOURCES
    ${TC_SRCDIR}/util/win/aygshellLib.c
  )
endif()

include_directories(${TC_SRCDIR})
include_directories(${TC_SRCDIR}/nm/ui/darwin)
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

if(MSVC)
  include_directories(${TC_SRCDIR}/nm/qrcode/win)
  include_directories(${TC_SRCDIR}/core/wince)
else()
  include_directories(${TC_SRCDIR}/nm/qrcode)
  include_directories(${TC_SRCDIR}/nm/lang/posix)
endif(MSVC)

if(DEFINED SKIA_LIBRARIES)
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
endif(DEFINED SKIA_LIBRARIES)

# add_definitions
if(DEFINED ANDROID_ABI)
  add_definitions(-DANDROID)
  if ("${ANDROID_ABI}" MATCHES "arm64-v8a")
	  add_definitions(-DFORCE_LIBC_ALLOC)
  endif()
elseif(MSVC AND ${CMAKE_GENERATOR} STREQUAL "Visual Studio 9 2008 Pocket PC 2003 (ARMV4)")
  add_definitions(-DUNICODE -D_WIN32_WCE=$(CEVER) -D_MSC_VER=1200 -D_REENTRANT -DLITTLE_ENDIAN)
elseif(MSVC)
  add_definitions(-DFORCE_LIBC_ALLOC -DWIN32 -D_REENTRANT -DHAVE_MREMAP=0 -DLITTLE_ENDIAN)
else()
  if(CMAKE_GENERATOR STREQUAL Xcode)
    add_definitions(-Ddarwin)
    set(CMAKE_STATIC_LINKER_FLAGS ${CMAKE_STATIC_LINKER_FLAGS} "-lskia")
    set(CMAKE_XCODE_ATTRIBUTE_LIBRARY_SEARCH_PATHS ${SKIA_LIBRARY_DIRS})
    set(CMAKE_XCODE_ATTRIBUTE_LINK_BINARIES_WITH_LIBRARIES ${SKIA_LIBRARIES})
    set(CMAKE_OSX_SYSROOT iphoneos)
    set(CMAKE_OSX_ARCHITECTURES "$(ARCHS_STANDARD)")
  else()
    add_definitions(-DHEADLESS)
  endif()
  add_definitions(-Dlinux -DFORCE_LIBC_ALLOC -D_REENTRANT -DPOSIX -DHAVE_MREMAP=0 -fPIC -Wunused-function -Wno-import -fno-strict-aliasing)
endif(DEFINED ANDROID_ABI)
add_definitions(-DTOTALCROSS -DTC_EXPORTS)

# Support defining PNG_ARM_NEON_OPT externally
if(DEFINED PNG_ARM_NEON_OPT)
  add_definitions(-DPNG_ARM_NEON_OPT=${PNG_ARM_NEON_OPT})
endif(DEFINED PNG_ARM_NEON_OPT)

if(APPLE AND CMAKE_BUILD_TYPE MATCHES "Debug")
  add_definitions(-DCURRENT_DEBUG_PATH="${CMAKE_CURRENT_BINARY_DIR}")
endif(APPLE AND CMAKE_BUILD_TYPE MATCHES "Debug")

link_directories(${PROJECT_SOURCE_DIR})

# add the library
set(LIBRARY_ARGS ${SOURCES} ${TC_WCE_SOURCES} ${TC_MSVC_SOURCES} ${TC_NOT_MSVC_SOURCES} ${LB_SOURCES} ${TC_SKIA_SOURCES} ${TC_SDL_SOURCES} ${TC_ANDROID_SOURCES} ${TCVM_iOS_FILES})

if(TCVM_SHARED)
  add_library(tcvm SHARED ${LIBRARY_ARGS})
else()
  add_library(tcvm STATIC ${LIBRARY_ARGS})
endif()

# Set C/CXX flags
target_compile_options(tcvm
  PRIVATE
    $<$<CXX_COMPILER_ID:Gnu>:
      # g++ warning flags
      -Wall
    >
    $<$<CXX_COMPILER_ID:Clang>:
      # clang warning flags
    >
    $<$<CXX_COMPILER_ID:MSVC>:
      # MSVC warning flags
      /W3
    >
    $<$<PLATFORM_ID:APPLE>:
      -mmacosx-version-min=10.11
    >
    $<$<STREQUAL:"${CMAKE_GENERATOR}","Xcode">:
      -Wno-everything
    >
)
# We need to use C++ 11
if(${CMAKE_VERSION} VERSION_LESS "3.8" OR MSVC) 
  set_target_properties(tcvm PROPERTIES
    CXX_STANDARD 11
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
  )
else()
  target_compile_features(tcvm PUBLIC cxx_std_17)
endif()
# XCode Properties
if(CMAKE_GENERATOR STREQUAL Xcode)
  
  # adds $(inherited) to HEADER_SEARCH_PATHS in order to get pod libraries headers
  get_target_property(INCLUDED_HEADERS tcvm INCLUDE_DIRECTORIES)
  set(INCLUDED_HEADERS "$(inherited)" ${INCLUDED_HEADERS})
  string(REPLACE ";" " " INCLUDED_HEADERS_STR "${INCLUDED_HEADERS}")
  
  set_target_properties(
    tcvm
    PROPERTIES
    XCODE_ATTRIBUTE_CODE_SIGN_IDENTITY ""
    XCODE_ATTRIBUTE_PRODUCT_BUNDLE_IDENTIFIER com.totalcross.vm
    XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET 10.0
    XCODE_ATTRIBUTE_CLANG_ENABLE_MODULES YES
    XCODE_ATTRIBUTE_GCC_PREFIX_HEADER "${IOS_BUNDLE_SRCDIR}/tcvm/tcvm-Prefix.pch"
    XCODE_ATTRIBUTE_GCC_PRECOMPILE_PREFIX_HEADER "YES"
    XCODE_ATTRIBUTE_HEADER_SEARCH_PATHS "${INCLUDED_HEADERS_STR}"
  )
endif()

if(DEFINED SDL2_LIBRARY)
  target_link_libraries(tcvm ${SDL2_LIBRARY})
endif(DEFINED SDL2_LIBRARY)

if(DEFINED ANDROID_ABI)
  add_library(libskia SHARED IMPORTED)
  set_target_properties(# Specifies the target library.
                        libskia

                        # Specifies the parameter you want to define.
                        PROPERTIES IMPORTED_LOCATION

                        # Provides the path to the library you want to import.
                        ${SKIA_LIBRARIES}
                       )

  # Searches for a specified prebuilt library and stores the path as a
  # variable. Because CMake includes system libraries in the search path by
  # default, you only need to specify the name of the public NDK library
  # you want to add. CMake verifies that the library exists before
  # completing its build.
  find_library( # Sets the name of the path variable.
    log-lib

    # Specifies the name of the NDK library that
    # you want CMake to locate.
    log 
  )
  find_library(# Sets the name of the path variable.
            jnigraphics-lib

            # Specifies the name of the NDK library that
            # you want CMake to locate.
            jnigraphics 
  )  
  # Specifies libraries CMake should link to your target library. You
  # can link multiple libraries, such as libraries you define in this
  # build script, prebuilt third-party libraries, or system libraries.
  target_link_libraries(# Specifies the target library.
                        tcvm
                        libskia
                        GLESv2
                        EGL
                        android
                        # Links the target library to the log library
                        # included in the NDK.
                        ${log-lib} 
                        ${jnigraphics-lib} )
elseif(MSVC)
  if(${CMAKE_GENERATOR} STREQUAL "Visual Studio 9 2008 Pocket PC 2003 (ARMV4)")
    target_link_libraries(tcvm secchk.lib coredll.lib corelibc.lib ole32.lib oleaut32.lib uuid.lib commctrl.lib)
  else()
    target_link_libraries(tcvm winmm.lib ws2_32.lib imm32.lib Rasapi32.lib)
  endif()
else()
  target_link_libraries(tcvm ${SKIA_LIBRARIES} pthread stdc++)
  if(NOT APPLE AND UNIX)
    target_link_libraries(tcvm fontconfig)
    if (${CMAKE_SYSTEM_PROCESSOR} MATCHES "arm")
      target_link_libraries(tcvm -Wl,--wrap=log2f,--wrap=powf,--wrap=expf,--wrap=exp2f) # Avoid glibc dependencies
    endif(${CMAKE_SYSTEM_PROCESSOR} MATCHES "arm")
  endif(NOT APPLE AND UNIX)
endif(DEFINED ANDROID_ABI)

set(TC_SRCDIR "${CMAKE_CURRENT_SOURCE_DIR}/src")

include_directories(${TC_SRCDIR}/util)

if(MSVC)
  set(CMAKE_EXE_LINKER_FLAGS
    "${CMAKE_EXE_LINKER_FLAGS} /NODEFAULTLIB  /SUBSYSTEM:WINDOWS /LARGEADDRESSAWARE /DYNAMICBASE:NO"
  )
  set(LAUNCHER_PATH
    "win"
  )
  set(LAUNCHER_RESOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/tc.rc
  )
else()
  set(LAUNCHER_PATH
    "linux"
  ) 
endif(MSVC)

# add the executable
if(NOT CMAKE_GENERATOR STREQUAL Xcode)
add_executable(Launcher 
  ${TC_SRCDIR}/launchers/${LAUNCHER_PATH}/Launcher.c
  ${LAUNCHER_RESOURCES}
)

if(MSVC AND ${CMAKE_GENERATOR} STREQUAL "Visual Studio 9 2008 Pocket PC 2003 (ARMV4)")
  target_compile_options(Launcher PRIVATE /GS-)
  target_link_options(Launcher PRIVATE /STACK:0x200000 /ENTRY:)
endif()

if(MSVC)
  target_link_libraries(Launcher MSVCRT.lib)
else()
  target_link_libraries(Launcher dl)
endif(MSVC)
endif()

if(MSVC)
  INCLUDE_EXTERNAL_MSPROJECT(Litebase ${CMAKE_CURRENT_SOURCE_DIR}/../LitebaseSDK/builders/vc2008/Litebase.vcproj)
  if(${CMAKE_GENERATOR} STREQUAL "Visual Studio 9 2008 Pocket PC 2003 (ARMV4)")
    INCLUDE_EXTERNAL_MSPROJECT(Datalogic ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Datalogic.vcproj)
    INCLUDE_EXTERNAL_MSPROJECT(Dolphin ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Dolphin.vcproj)
    INCLUDE_EXTERNAL_MSPROJECT(Intermec ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Intermec.vcproj)
    INCLUDE_EXTERNAL_MSPROJECT(Motorola ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Motorola.vcproj)
    INCLUDE_EXTERNAL_MSPROJECT(OpticonH16 ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/OpticonH16.vcproj)
    add_dependencies(OpticonH16 tcvm)
    add_dependencies(Dolphin tcvm)
    add_dependencies(Intermec tcvm)
    INCLUDE_EXTERNAL_MSPROJECT(Pidion ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Pidion.vcproj)
  endif()
endif()