# Copyright (C) 2020 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

function(tcvm_auto_fetch_skia)
  set(TCVM_DEFAULT_SKIA_DIR "${CMAKE_SOURCE_DIR}/skia/local")

  if(DEFINED SKIA_LIBRARIES)
    if(EXISTS "${SKIA_LIBRARIES}")
      return()
    endif()
    unset(SKIA_LIBRARIES CACHE)
  endif()

  if(DEFINED SKIA_DIR AND NOT SKIA_DIR STREQUAL "${TCVM_DEFAULT_SKIA_DIR}")
    return()
  endif()

  find_program(TCVM_BASH_EXECUTABLE bash)
  if(NOT TCVM_BASH_EXECUTABLE)
    message(FATAL_ERROR "Unable to auto-fetch Skia because 'bash' was not found")
  endif()

  set(TCVM_FETCH_SCRIPT "${CMAKE_SOURCE_DIR}/skia/fetch-skia.sh")
  if(NOT EXISTS "${TCVM_FETCH_SCRIPT}")
    message(FATAL_ERROR "Unable to auto-fetch Skia because '${TCVM_FETCH_SCRIPT}' does not exist")
  endif()

  set(TCVM_SKIA_DIR "${TCVM_DEFAULT_SKIA_DIR}")
  set(TCVM_FETCH_NEEDED OFF)
  set(TCVM_FETCH_PLATFORM "")
  set(TCVM_FETCH_ARCH "")
  set(TCVM_FETCH_INSTALL_DEV OFF)

  if(DEFINED ANDROID_ABI)
    set(TCVM_FETCH_PLATFORM "android")
    set(TCVM_FETCH_ARCH "${ANDROID_ABI}")
    set(TCVM_SKIA_ARTIFACT "${CMAKE_SOURCE_DIR}/android/tcvm/src/main/jniLibs/${ANDROID_ABI}/libskia.so")
  elseif(CMAKE_GENERATOR STREQUAL Xcode)
    set(TCVM_FETCH_PLATFORM "ios")
    set(TCVM_FETCH_ARCH "universal")
    set(TCVM_FETCH_INSTALL_DEV ON)
    set(TCVM_SKIA_ARTIFACT "${TCVM_SKIA_DIR}/out/Release/ios/libskia.a")
  elseif(APPLE)
    set(TCVM_FETCH_PLATFORM "macos")
    set(TCVM_FETCH_ARCH "${CMAKE_OSX_ARCHITECTURES}")
    if(TCVM_FETCH_ARCH)
      list(LENGTH TCVM_FETCH_ARCH TCVM_FETCH_ARCH_LEN)
      if(TCVM_FETCH_ARCH_LEN GREATER 1)
        message(FATAL_ERROR "Skia auto-fetch expects a single macOS architecture, got: ${TCVM_FETCH_ARCH}")
      endif()
      list(GET TCVM_FETCH_ARCH 0 TCVM_FETCH_ARCH)
    else()
      set(TCVM_FETCH_ARCH "${CMAKE_SYSTEM_PROCESSOR}")
    endif()

    if(TCVM_FETCH_ARCH STREQUAL "amd64")
      set(TCVM_FETCH_ARCH "x86_64")
    elseif(TCVM_FETCH_ARCH STREQUAL "aarch64")
      set(TCVM_FETCH_ARCH "arm64")
    endif()

    set(TCVM_FETCH_INSTALL_DEV ON)
    set(TCVM_SKIA_ARTIFACT "${TCVM_SKIA_DIR}/out/Release/macos/${TCVM_FETCH_ARCH}/libskia.a")
  elseif(UNIX AND (CMAKE_SYSTEM_NAME STREQUAL "Linux"))
    set(TCVM_FETCH_PLATFORM "linux")
    set(TCVM_FETCH_ARCH "${CMAKE_SYSTEM_PROCESSOR}")
    if(TCVM_FETCH_ARCH STREQUAL "amd64")
      set(TCVM_FETCH_ARCH "x86_64")
    elseif(TCVM_FETCH_ARCH STREQUAL "arm64")
      set(TCVM_FETCH_ARCH "aarch64")
    elseif(TCVM_FETCH_ARCH STREQUAL "armv7")
      set(TCVM_FETCH_ARCH "armv7l")
    endif()

    set(TCVM_FETCH_INSTALL_DEV ON)
    set(TCVM_SKIA_ARTIFACT "${TCVM_SKIA_DIR}/out/Release/linux/${TCVM_FETCH_ARCH}/libskia.a")
  else()
    return()
  endif()

  if(TCVM_FETCH_INSTALL_DEV)
    if(NOT EXISTS "${TCVM_SKIA_DIR}/include/core/SkCanvas.h" OR NOT EXISTS "${TCVM_SKIA_DIR}/src/gpu/gl/GrGLDefines.h")
      set(TCVM_FETCH_NEEDED ON)
    endif()
  endif()

  if(NOT EXISTS "${TCVM_SKIA_ARTIFACT}")
    set(TCVM_FETCH_NEEDED ON)
  endif()

  if(NOT TCVM_FETCH_NEEDED)
    return()
  endif()

  set(TCVM_FETCH_ARGS
    "${TCVM_FETCH_SCRIPT}"
    --platform "${TCVM_FETCH_PLATFORM}"
    --arch "${TCVM_FETCH_ARCH}"
  )

  if(TCVM_FETCH_INSTALL_DEV)
    list(APPEND TCVM_FETCH_ARGS --install-dev)
  endif()

  message(STATUS "Skia artifacts not found locally. Fetching ${TCVM_FETCH_PLATFORM}/${TCVM_FETCH_ARCH}...")
  execute_process(
    COMMAND "${TCVM_BASH_EXECUTABLE}" ${TCVM_FETCH_ARGS}
    WORKING_DIRECTORY "${TCVM_REPO_ROOT}"
    RESULT_VARIABLE TCVM_FETCH_RESULT
    OUTPUT_VARIABLE TCVM_FETCH_STDOUT
    ERROR_VARIABLE TCVM_FETCH_STDERR
  )

  if(NOT TCVM_FETCH_RESULT EQUAL 0)
    message(FATAL_ERROR
      "Failed to auto-fetch Skia.\n"
      "stdout:\n${TCVM_FETCH_STDOUT}\n"
      "stderr:\n${TCVM_FETCH_STDERR}"
    )
  endif()

  message(STATUS "${TCVM_FETCH_STDOUT}")
endfunction()
