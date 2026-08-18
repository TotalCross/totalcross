# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

include_guard(GLOBAL)

# ---------------------------------------------------------------------------
# Detect target platform
# ---------------------------------------------------------------------------

set(TC_TARGET_ANDROID OFF)
set(TC_TARGET_IOS OFF)
set(TC_TARGET_MACOS OFF)
set(TC_TARGET_WINDOWS OFF)
set(TC_TARGET_WINCE OFF)
set(TC_TARGET_LINUX OFF)

# Build environment; not an operating system.
set(TC_GENERATOR_XCODE OFF)

if(CMAKE_GENERATOR STREQUAL "Xcode")
  set(TC_GENERATOR_XCODE ON)
endif()

#
# Compatibility fallback for the legacy WinCE generator.
#
set(TC_LEGACY_WINCE_GENERATOR OFF)

if(MSVC AND
   CMAKE_GENERATOR STREQUAL
     "Visual Studio 9 2008 Pocket PC 2003 (ARMV4)")
  set(TC_LEGACY_WINCE_GENERATOR ON)
endif()

#
# CMAKE_SYSTEM_NAME describes the target, not the host.
#
if(CMAKE_SYSTEM_NAME STREQUAL "WindowsCE" OR
   TC_LEGACY_WINCE_GENERATOR)

  set(TC_TARGET_WINCE ON)
  set(TC_TARGET_OS_NAME "wince")

elseif(CMAKE_SYSTEM_NAME STREQUAL "Android"
       OR ANDROID
       OR DEFINED ANDROID_ABI)
  set(TC_TARGET_ANDROID ON)
  set(TC_TARGET_OS_NAME "android")

elseif(APPLE)

  #
  # CMAKE_OSX_SYSROOT also applies to iOS SDKs despite its name.
  # It may be either an SDK name or a complete SDK path.
  #
  string(TOLOWER "${CMAKE_OSX_SYSROOT}" TC_APPLE_SYSROOT)

  if(CMAKE_SYSTEM_NAME STREQUAL "iOS" OR
     TC_APPLE_SYSROOT MATCHES "iphoneos" OR
     TC_APPLE_SYSROOT MATCHES "iphonesimulator")

    set(TC_TARGET_IOS ON)
    set(TC_TARGET_OS_NAME "ios")
  else()
    set(TC_TARGET_MACOS ON)
    set(TC_TARGET_OS_NAME "macos")
  endif()

elseif(CMAKE_SYSTEM_NAME STREQUAL "Windows" OR WIN32)
  set(TC_TARGET_WINDOWS ON)
  set(TC_TARGET_OS_NAME "windows")

elseif(CMAKE_SYSTEM_NAME STREQUAL "Linux")
  set(TC_TARGET_LINUX ON)
  set(TC_TARGET_OS_NAME "linux")

else()
  message(
    FATAL_ERROR
    "Unable to detect target platform: "
    "CMAKE_SYSTEM_NAME='${CMAKE_SYSTEM_NAME}', "
    "CMAKE_GENERATOR='${CMAKE_GENERATOR}'"
  )
endif()

# ---------------------------------------------------------------------------
# Derived properties
# ---------------------------------------------------------------------------

set(TC_TARGET_APPLE OFF)
set(TC_TARGET_WINDOWS_FAMILY OFF)
set(TC_TARGET_MOBILE OFF)
set(TC_TARGET_DESKTOP OFF)

if(TC_TARGET_IOS OR TC_TARGET_MACOS)
  set(TC_TARGET_APPLE ON)
endif()

if(TC_TARGET_WINDOWS OR TC_TARGET_WINCE)
  set(TC_TARGET_WINDOWS_FAMILY ON)
endif()

if(TC_TARGET_ANDROID OR TC_TARGET_IOS OR TC_TARGET_WINCE)
  set(TC_TARGET_MOBILE ON)
endif()

if(TC_TARGET_WINDOWS OR TC_TARGET_MACOS OR TC_TARGET_LINUX)
  set(TC_TARGET_DESKTOP ON)
endif()

# ---------------------------------------------------------------------------
# Variables consumed by tc_platform_config.h.in
# ---------------------------------------------------------------------------

set(TC_CONFIG_OS_ANDROID ${TC_TARGET_ANDROID})
set(TC_CONFIG_OS_IOS ${TC_TARGET_IOS})
set(TC_CONFIG_OS_MACOS ${TC_TARGET_MACOS})
set(TC_CONFIG_OS_WINDOWS ${TC_TARGET_WINDOWS})
set(TC_CONFIG_OS_WINCE ${TC_TARGET_WINCE})
set(TC_CONFIG_OS_LINUX ${TC_TARGET_LINUX})

set(TC_CONFIG_OS_APPLE ${TC_TARGET_APPLE})
set(TC_CONFIG_OS_WINDOWS_FAMILY ${TC_TARGET_WINDOWS_FAMILY})
set(TC_CONFIG_OS_MOBILE ${TC_TARGET_MOBILE})
set(TC_CONFIG_OS_DESKTOP ${TC_TARGET_DESKTOP})

# ---------------------------------------------------------------------------
# Generate the compiler-visible configuration
# ---------------------------------------------------------------------------

set(
  TC_PLATFORM_GENERATED_INCLUDE_DIR
  "${CMAKE_CURRENT_BINARY_DIR}/generated/tcvm"
)

file(
  MAKE_DIRECTORY
  "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
)

function(tc_generate_platform_config)
  configure_file(
    "${CMAKE_CURRENT_LIST_DIR}/src/tcvm/tc_platform_config.h.in"
    "${TC_PLATFORM_GENERATED_INCLUDE_DIR}/tc_platform_config.h"
    @ONLY
  )
endfunction()

message(STATUS "TotalCross target OS: ${TC_TARGET_OS_NAME}")
message(STATUS "TotalCross CMake system: ${CMAKE_SYSTEM_NAME}")
message(STATUS "TotalCross generator: ${CMAKE_GENERATOR}")
message(STATUS "TotalCross processor: ${CMAKE_SYSTEM_PROCESSOR}")
