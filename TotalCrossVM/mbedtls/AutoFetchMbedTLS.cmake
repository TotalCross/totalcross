# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

function(tcvm_auto_fetch_mbedtls)
  set(TCVM_DEFAULT_MBEDTLS_DIR "${CMAKE_SOURCE_DIR}/mbedtls/local")

  if(DEFINED MBEDTLS_DIR AND NOT MBEDTLS_DIR STREQUAL "${TCVM_DEFAULT_MBEDTLS_DIR}")
    return()
  endif()

  if(NOT DEFINED MBEDTLS_DIR)
    set(MBEDTLS_DIR "${TCVM_DEFAULT_MBEDTLS_DIR}" CACHE PATH "mbedTLS prebuilt directory")
  endif()

  if(WIN32)
    set(TCVM_MBEDTLS_LIBRARY "${MBEDTLS_DIR}/lib/mbedtls.lib")
    set(TCVM_MBEDX509_LIBRARY "${MBEDTLS_DIR}/lib/mbedx509.lib")
    set(TCVM_MBEDCRYPTO_LIBRARY "${MBEDTLS_DIR}/lib/mbedcrypto.lib")
  else()
    set(TCVM_MBEDTLS_LIBRARY "${MBEDTLS_DIR}/lib/libmbedtls.a")
    set(TCVM_MBEDX509_LIBRARY "${MBEDTLS_DIR}/lib/libmbedx509.a")
    set(TCVM_MBEDCRYPTO_LIBRARY "${MBEDTLS_DIR}/lib/libmbedcrypto.a")
  endif()

  if(EXISTS "${MBEDTLS_DIR}/include/mbedtls/ssl.h"
      AND EXISTS "${TCVM_MBEDTLS_LIBRARY}"
      AND EXISTS "${TCVM_MBEDX509_LIBRARY}"
      AND EXISTS "${TCVM_MBEDCRYPTO_LIBRARY}")
    return()
  endif()

  find_program(TCVM_BASH_EXECUTABLE bash)
  if(NOT TCVM_BASH_EXECUTABLE)
    message(FATAL_ERROR "Unable to auto-fetch mbedTLS because 'bash' was not found")
  endif()

  set(TCVM_MBEDTLS_FETCH_SCRIPT "${CMAKE_SOURCE_DIR}/mbedtls/fetch-mbedtls.sh")
  if(NOT EXISTS "${TCVM_MBEDTLS_FETCH_SCRIPT}")
    message(FATAL_ERROR "Unable to auto-fetch mbedTLS because '${TCVM_MBEDTLS_FETCH_SCRIPT}' does not exist")
  endif()

  if(NOT DEFINED MBEDTLS_RELEASE_TAG AND DEFINED ENV{MBEDTLS_RELEASE_TAG})
    set(MBEDTLS_RELEASE_TAG "$ENV{MBEDTLS_RELEASE_TAG}")
  elseif(NOT DEFINED MBEDTLS_RELEASE_TAG)
    set(MBEDTLS_RELEASE_TAG "mbedtls-3.5.2")
  endif()

  if(NOT DEFINED MBEDTLS_GITHUB_REPO AND DEFINED ENV{MBEDTLS_GITHUB_REPO})
    set(MBEDTLS_GITHUB_REPO "$ENV{MBEDTLS_GITHUB_REPO}")
  elseif(NOT DEFINED MBEDTLS_GITHUB_REPO)
    set(MBEDTLS_GITHUB_REPO "TotalCross/totalcross-mbedtls-build")
  endif()

  if(NOT DEFINED MBEDTLS_GITHUB_TOKEN_ENV AND DEFINED ENV{MBEDTLS_GITHUB_TOKEN_ENV})
    set(MBEDTLS_GITHUB_TOKEN_ENV "$ENV{MBEDTLS_GITHUB_TOKEN_ENV}")
  elseif(NOT DEFINED MBEDTLS_GITHUB_TOKEN_ENV)
    set(MBEDTLS_GITHUB_TOKEN_ENV "MBEDTLS_GITHUB_TOKEN")
  endif()

  if(DEFINED ANDROID_ABI)
    set(TCVM_MBEDTLS_PLATFORM "android")
    set(TCVM_MBEDTLS_ARCH "${ANDROID_ABI}")
  elseif(CMAKE_GENERATOR STREQUAL Xcode)
    set(TCVM_MBEDTLS_PLATFORM "ios")
    set(TCVM_MBEDTLS_ARCH "arm64")
  elseif(WIN32)
    set(TCVM_MBEDTLS_PLATFORM "windows")
    if(CMAKE_GENERATOR_PLATFORM MATCHES "x64")
      set(TCVM_MBEDTLS_ARCH "x86_64")
    else()
      set(TCVM_MBEDTLS_ARCH "x86")
    endif()
  elseif(APPLE)
    set(TCVM_MBEDTLS_PLATFORM "macos")
    set(TCVM_MBEDTLS_ARCH "${CMAKE_OSX_ARCHITECTURES}")
    if(TCVM_MBEDTLS_ARCH)
      list(LENGTH TCVM_MBEDTLS_ARCH TCVM_MBEDTLS_ARCH_LEN)
      if(TCVM_MBEDTLS_ARCH_LEN GREATER 1)
        message(FATAL_ERROR "mbedTLS auto-fetch expects a single macOS architecture, got: ${TCVM_MBEDTLS_ARCH}")
      endif()
      list(GET TCVM_MBEDTLS_ARCH 0 TCVM_MBEDTLS_ARCH)
    else()
      set(TCVM_MBEDTLS_ARCH "${CMAKE_SYSTEM_PROCESSOR}")
    endif()

    if(TCVM_MBEDTLS_ARCH STREQUAL "amd64")
      set(TCVM_MBEDTLS_ARCH "x86_64")
    elseif(TCVM_MBEDTLS_ARCH STREQUAL "aarch64")
      set(TCVM_MBEDTLS_ARCH "arm64")
    endif()
  elseif(UNIX AND CMAKE_SYSTEM_NAME STREQUAL "Linux")
    set(TCVM_MBEDTLS_PLATFORM "linux")
    set(TCVM_MBEDTLS_ARCH "${CMAKE_SYSTEM_PROCESSOR}")
    if(TCVM_MBEDTLS_ARCH STREQUAL "amd64")
      set(TCVM_MBEDTLS_ARCH "x86_64")
    elseif(TCVM_MBEDTLS_ARCH STREQUAL "arm64")
      set(TCVM_MBEDTLS_ARCH "aarch64")
    elseif(TCVM_MBEDTLS_ARCH STREQUAL "arm" OR TCVM_MBEDTLS_ARCH STREQUAL "armv7")
      set(TCVM_MBEDTLS_ARCH "armv7l")
    endif()
  else()
    message(FATAL_ERROR "Unable to auto-fetch mbedTLS for ${CMAKE_SYSTEM_NAME}/${CMAKE_SYSTEM_PROCESSOR}")
  endif()

  message(STATUS "mbedTLS artifacts not found locally. Fetching ${TCVM_MBEDTLS_PLATFORM}/${TCVM_MBEDTLS_ARCH}...")

  execute_process(
    COMMAND
      "${TCVM_BASH_EXECUTABLE}" "${TCVM_MBEDTLS_FETCH_SCRIPT}"
      --platform "${TCVM_MBEDTLS_PLATFORM}"
      --arch "${TCVM_MBEDTLS_ARCH}"
      --release-tag "${MBEDTLS_RELEASE_TAG}"
      --github-repo "${MBEDTLS_GITHUB_REPO}"
      --github-token-env "${MBEDTLS_GITHUB_TOKEN_ENV}"
      --dest "${MBEDTLS_DIR}"
    WORKING_DIRECTORY "${TCVM_REPO_ROOT}"
    RESULT_VARIABLE TCVM_MBEDTLS_FETCH_RESULT
    OUTPUT_VARIABLE TCVM_MBEDTLS_FETCH_STDOUT
    ERROR_VARIABLE TCVM_MBEDTLS_FETCH_STDERR
  )

  if(NOT TCVM_MBEDTLS_FETCH_RESULT EQUAL 0)
    message(FATAL_ERROR
      "Failed to auto-fetch mbedTLS.\n"
      "stdout:\n${TCVM_MBEDTLS_FETCH_STDOUT}\n"
      "stderr:\n${TCVM_MBEDTLS_FETCH_STDERR}"
    )
  endif()

  message(STATUS "${TCVM_MBEDTLS_FETCH_STDOUT}")
endfunction()
