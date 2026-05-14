# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

function(tcvm_auto_fetch_sqlite3)
  set(TCVM_DEFAULT_SQLITE3_DIR "${CMAKE_SOURCE_DIR}/sqlite3/local")

  if(DEFINED SQLITE3_DIR AND NOT SQLITE3_DIR STREQUAL "${TCVM_DEFAULT_SQLITE3_DIR}")
    return()
  endif()

  if(NOT DEFINED SQLITE3_DIR)
    set(SQLITE3_DIR "${TCVM_DEFAULT_SQLITE3_DIR}" CACHE PATH "SQLite3 prebuilt directory")
  endif()

  if(WIN32)
    set(TCVM_SQLITE3_LIBRARY "${SQLITE3_DIR}/lib/sqlite3.lib")
  else()
    set(TCVM_SQLITE3_LIBRARY "${SQLITE3_DIR}/lib/libsqlite3.a")
  endif()

  if(EXISTS "${SQLITE3_DIR}/include/sqlite3.h" AND EXISTS "${TCVM_SQLITE3_LIBRARY}")
    return()
  endif()

  find_program(TCVM_BASH_EXECUTABLE bash)
  if(NOT TCVM_BASH_EXECUTABLE)
    message(FATAL_ERROR "Unable to auto-fetch SQLite3 because 'bash' was not found")
  endif()

  set(TCVM_SQLITE3_FETCH_SCRIPT "${CMAKE_SOURCE_DIR}/sqlite3/fetch-sqlite3.sh")
  if(NOT EXISTS "${TCVM_SQLITE3_FETCH_SCRIPT}")
    message(FATAL_ERROR "Unable to auto-fetch SQLite3 because '${TCVM_SQLITE3_FETCH_SCRIPT}' does not exist")
  endif()

  if(NOT DEFINED SQLITE3_VARIANT)
    set(SQLITE3_VARIANT "plain")
  endif()

  if(NOT DEFINED SQLITE3_RELEASE_TAG)
    set(SQLITE3_RELEASE_TAG "sqlite-3.32.3")
  endif()

  if(NOT DEFINED SQLITE3_GITHUB_REPO)
    set(SQLITE3_GITHUB_REPO "TotalCross/totalcross-sqlite3-build")
  endif()

  if(DEFINED ANDROID_ABI)
    set(TCVM_SQLITE3_PLATFORM "android")
    set(TCVM_SQLITE3_ARCH "${ANDROID_ABI}")
  elseif(CMAKE_GENERATOR STREQUAL Xcode)
    set(TCVM_SQLITE3_PLATFORM "ios")
    set(TCVM_SQLITE3_ARCH "arm64")
  elseif(WIN32)
    set(TCVM_SQLITE3_PLATFORM "windows")
    if(CMAKE_GENERATOR_PLATFORM MATCHES "x64")
      set(TCVM_SQLITE3_ARCH "x86_64")
    else()
      set(TCVM_SQLITE3_ARCH "x86")
    endif()
  elseif(APPLE)
    set(TCVM_SQLITE3_PLATFORM "macos")
    set(TCVM_SQLITE3_ARCH "${CMAKE_OSX_ARCHITECTURES}")
    if(TCVM_SQLITE3_ARCH)
      list(LENGTH TCVM_SQLITE3_ARCH TCVM_SQLITE3_ARCH_LEN)
      if(TCVM_SQLITE3_ARCH_LEN GREATER 1)
        message(FATAL_ERROR "SQLite3 auto-fetch expects a single macOS architecture, got: ${TCVM_SQLITE3_ARCH}")
      endif()
      list(GET TCVM_SQLITE3_ARCH 0 TCVM_SQLITE3_ARCH)
    else()
      set(TCVM_SQLITE3_ARCH "${CMAKE_SYSTEM_PROCESSOR}")
    endif()

    if(TCVM_SQLITE3_ARCH STREQUAL "amd64")
      set(TCVM_SQLITE3_ARCH "x86_64")
    elseif(TCVM_SQLITE3_ARCH STREQUAL "aarch64")
      set(TCVM_SQLITE3_ARCH "arm64")
    endif()
  elseif(UNIX AND CMAKE_SYSTEM_NAME STREQUAL "Linux")
    set(TCVM_SQLITE3_PLATFORM "linux")
    set(TCVM_SQLITE3_ARCH "${CMAKE_SYSTEM_PROCESSOR}")
    if(TCVM_SQLITE3_ARCH STREQUAL "amd64")
      set(TCVM_SQLITE3_ARCH "x86_64")
    elseif(TCVM_SQLITE3_ARCH STREQUAL "arm64")
      set(TCVM_SQLITE3_ARCH "aarch64")
    elseif(TCVM_SQLITE3_ARCH STREQUAL "arm" OR TCVM_SQLITE3_ARCH STREQUAL "armv7")
      set(TCVM_SQLITE3_ARCH "armv7l")
    endif()
  else()
    message(FATAL_ERROR "Unable to auto-fetch SQLite3 for ${CMAKE_SYSTEM_NAME}/${CMAKE_SYSTEM_PROCESSOR}")
  endif()

  message(STATUS "SQLite3 artifacts not found locally. Fetching ${SQLITE3_VARIANT}/${TCVM_SQLITE3_PLATFORM}/${TCVM_SQLITE3_ARCH}...")

  execute_process(
    COMMAND
      "${TCVM_BASH_EXECUTABLE}" "${TCVM_SQLITE3_FETCH_SCRIPT}"
      --platform "${TCVM_SQLITE3_PLATFORM}"
      --arch "${TCVM_SQLITE3_ARCH}"
      --variant "${SQLITE3_VARIANT}"
      --release-tag "${SQLITE3_RELEASE_TAG}"
      --github-repo "${SQLITE3_GITHUB_REPO}"
      --dest "${SQLITE3_DIR}"
    WORKING_DIRECTORY "${TCVM_REPO_ROOT}"
    RESULT_VARIABLE TCVM_SQLITE3_FETCH_RESULT
    OUTPUT_VARIABLE TCVM_SQLITE3_FETCH_STDOUT
    ERROR_VARIABLE TCVM_SQLITE3_FETCH_STDERR
  )

  if(NOT TCVM_SQLITE3_FETCH_RESULT EQUAL 0)
    message(FATAL_ERROR
      "Failed to auto-fetch SQLite3.\n"
      "stdout:\n${TCVM_SQLITE3_FETCH_STDOUT}\n"
      "stderr:\n${TCVM_SQLITE3_FETCH_STDERR}"
    )
  endif()

  message(STATUS "${TCVM_SQLITE3_FETCH_STDOUT}")
endfunction()
