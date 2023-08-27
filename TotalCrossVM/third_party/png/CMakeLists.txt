cmake_minimum_required(VERSION 3.5)

include(FetchContent)

set(SKIP_INSTALL_ALL TRUE) # Skip default libPNG installation
FetchContent_Declare(
    libpng
    GIT_REPOSITORY  https://github.com/glennrp/libpng.git
    GIT_TAG         v1.6.40
    GIT_PROGRESS    TRUE
    USES_TERMINAL_DOWNLOAD TRUE)

FetchContent_GetProperties(libpng)
if(NOT libpng_POPULATED)
    message("Setting up PNG...")

    FetchContent_Populate(libpng)
    set(PNG_BUILD_ZLIB ON CACHE BOOL "" FORCE) # Use our own version of zlib
    set(PNG_SHARED OFF CACHE BOOL "" FORCE)
    set(PNG_FRAMEWORK OFF CACHE BOOL "" FORCE)
    set(PNG_EXECUTABLES OFF CACHE BOOL "" FORCE)
    set(PNG_TESTS OFF CACHE BOOL "" FORCE)
    set(PNG_DEBUG_POSTFIX "" CACHE STRING "" FORCE) # XCode gets confused if used

    add_subdirectory(${libpng_SOURCE_DIR} ${libpng_BINARY_DIR})

    # Need this with SKIP_INSTALL_ALL enabled to find the headers
    target_include_directories(png_static PUBLIC 
        ${libpng_SOURCE_DIR}
        ${libpng_BINARY_DIR}
    )

    set_target_properties(
        png_static
        PROPERTIES
        PUBLIC_HEADER "${libpng_SOURCE_DIR} ${libpng_BINARY_DIR}"
        XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET 13.0
    )
endif()

# Our implementation using libpng
target_sources(tcvm PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/PngLoader.c")
