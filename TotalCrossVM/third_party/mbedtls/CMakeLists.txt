cmake_minimum_required(VERSION 3.5)

include(FetchContent)

FetchContent_Declare(
    mbedtls
    GIT_REPOSITORY  https://github.com/Mbed-TLS/mbedtls.git
    GIT_TAG         v3.2.1
    GIT_SHALLOW     TRUE
    GIT_PROGRESS    TRUE
    USES_TERMINAL_DOWNLOAD TRUE)

# FetchContent_MakeAvailable(mbedtls)

set(CMAKE_C_FLAGS_OLD "${CMAKE_C_FLAGS}")

# Add other flags here.

if(DEFINED ANDROID_ABI)
    if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
        # Required to build mbedTLS for ARM 32
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DMULADDC_CANNOT_USE_R7")
    endif()
endif()

FetchContent_GetProperties(mbedtls)
if(NOT mbedtls_POPULATED)
    message("Setting up ARM mbedTLS...")
    FetchContent_Populate(mbedtls)
    set(ENABLE_PROGRAMS OFF CACHE BOOL "" FORCE)
    set(ENABLE_TESTING OFF CACHE BOOL "" FORCE)
    set(INSTALL_MBEDTLS_HEADERS OFF CACHE BOOL "" FORCE)

    add_subdirectory(${mbedtls_SOURCE_DIR} ${mbedtls_BINARY_DIR})
    target_include_directories(mbedtls PUBLIC 
        $<BUILD_INTERFACE:${mbedtls_SOURCE_DIR}/include>
        $<INSTALL_INTERFACE:${mbedtls_SOURCE_DIR}>
    )
    set_target_properties(
        mbedtls mbedx509 mbedcrypto
        PROPERTIES
        PUBLIC_HEADER "${mbedtls_SOURCE_DIR}/include/**"
        XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET 13.0
    )
endif()
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS_OLD}")
