cmake_minimum_required(VERSION 3.5)

include(FetchContent)

FetchContent_Declare(
    zlib
    GIT_REPOSITORY  https://github.com/zlib-ng/zlib-ng.git
    GIT_TAG         2.1.3
    GIT_PROGRESS    TRUE
    USES_TERMINAL_DOWNLOAD TRUE)

FetchContent_GetProperties(zlib)
if(NOT zlib_POPULATED)
    message("Setting up zlib...")

    FetchContent_Populate(zlib)
    set(ZLIB_COMPAT ON CACHE BOOL "" FORCE)

    # Optimization options
    set(WITH_OPTIM ON CACHE BOOL "" FORCE)
    set(WITH_REDUCED_MEM OFF CACHE BOOL "" FORCE) # may be useful for embedded
    set(WITH_NEW_STRATEGIES ON CACHE BOOL "" FORCE)
    if(NOT DEFINED ANDROID_ABI) # the clang compiler does not support '-march=native'
        set(WITH_NATIVE_INSTRUCTIONS ON CACHE BOOL "" FORCE)
    elseif(${ANDROID_ABI} STREQUAL "armeabi-v7a")
        # compiler that comes with NDK 21 breaks on slide_hash_neon.c
        set(WITH_NEON OFF CACHE BOOL "" FORCE)
    endif()
    set(WITH_UNALIGNED ON CACHE BOOL "" FORCE)

    # Disable tests, benchmarks and extra checks and warnings
    set(ZLIB_ENABLE_TESTS OFF CACHE BOOL "" FORCE)
    set(ZLIBNG_ENABLE_TESTS OFF CACHE BOOL "" FORCE)
    set(WITH_GTEST OFF CACHE BOOL "" FORCE)
    set(WITH_FUZZERS OFF CACHE BOOL "" FORCE)
    set(WITH_BENCHMARKS OFF CACHE BOOL "" FORCE)
    set(WITH_BENCHMARK_APPS OFF CACHE BOOL "" FORCE)
    set(WITH_MAINTAINER_WARNINGS OFF CACHE BOOL "" FORCE)
    set(WITH_CODE_COVERAGE OFF CACHE BOOL "" FORCE)
    set(WITH_INFLATE_STRICT OFF CACHE BOOL "" FORCE)
    set(WITH_INFLATE_ALLOW_INVALID_DIST OFF CACHE BOOL "" FORCE)
    
    add_subdirectory(${zlib_SOURCE_DIR} ${zlib_BINARY_DIR})

    set_target_properties(
        zlibstatic
        PROPERTIES
        PUBLIC_HEADER "${zlib_BINARY_DIR}"
        XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET 13.0
    )
endif()
