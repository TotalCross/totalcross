# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

# zlib
if (USE_MADLER_ZLIB)
    add_subdirectory(zlib)
    get_target_property(ZLIB_INCLUDE_DIRS zlibstatic PUBLIC_HEADER)
    get_target_property(ZLIB_BINARY_DIR zlibstatic BINARY_DIR)
    get_target_property(ZLIB_ROOT zlibstatic SOURCE_DIR) # Root for the zlib library, used by libpng
else()
    if(NOT DEFINED ZLIB_DIR)
        message(FATAL_ERROR "ZLIB_DIR must point to the zlib-ng prebuilt directory")
    endif()

    set(TCVM_ZLIB_INCLUDE_DIR "${ZLIB_DIR}/include")
    set(TCVM_ZLIB_LIBRARY_CANDIDATES
        "${ZLIB_DIR}/lib/libz.a"
        "${ZLIB_DIR}/lib/zlib.lib"
        "${ZLIB_DIR}/lib/zlibstatic.lib"
    )

    unset(TCVM_ZLIB_LIBRARY)
    foreach(TCVM_ZLIB_LIBRARY_CANDIDATE ${TCVM_ZLIB_LIBRARY_CANDIDATES})
        if(EXISTS "${TCVM_ZLIB_LIBRARY_CANDIDATE}")
            set(TCVM_ZLIB_LIBRARY "${TCVM_ZLIB_LIBRARY_CANDIDATE}")
            break()
        endif()
    endforeach()

    if(NOT TCVM_ZLIB_LIBRARY)
        message(FATAL_ERROR "Unable to find the zlib-ng static library under ${ZLIB_DIR}/lib")
    endif()

    set(ZLIB_INCLUDE_DIR "${TCVM_ZLIB_INCLUDE_DIR}" CACHE PATH "zlib include directory" FORCE)
    set(ZLIB_LIBRARY "${TCVM_ZLIB_LIBRARY}" CACHE FILEPATH "zlib static library" FORCE)

    add_library(zlibstatic STATIC IMPORTED GLOBAL)
    set_target_properties(zlibstatic PROPERTIES
        IMPORTED_LOCATION "${TCVM_ZLIB_LIBRARY}"
        INTERFACE_INCLUDE_DIRECTORIES "${TCVM_ZLIB_INCLUDE_DIR}"
    )

    set(ZLIB_INCLUDE_DIRS "${TCVM_ZLIB_INCLUDE_DIR}")
    get_filename_component(ZLIB_BINARY_DIR "${TCVM_ZLIB_LIBRARY}" DIRECTORY)
    set(ZLIB_ROOT "${ZLIB_DIR}") # Root for the zlib library, used by libpng
endif()

# png
if(TARGET_WINCE)
    add_subdirectory(png)
    get_target_property(PNG_INCLUDE_DIRS png_static PUBLIC_HEADER)
    get_target_property(PNG_BINARY_DIR png_static BINARY_DIR)
else()
    find_package(PNG REQUIRED)

    if(NOT TARGET PNG::PNG)
        message(FATAL_ERROR "Unable to find the libpng imported target")
    endif()

    add_library(png_static STATIC IMPORTED GLOBAL)
    set_target_properties(png_static PROPERTIES
        IMPORTED_LOCATION "${PNG_LIBRARY}"
        INTERFACE_INCLUDE_DIRECTORIES "${PNG_INCLUDE_DIR}"
    )

    set(PNG_INCLUDE_DIRS "${PNG_INCLUDE_DIR}")
    get_filename_component(PNG_BINARY_DIR "${PNG_LIBRARY}" DIRECTORY)
endif()

if(CMAKE_GENERATOR STREQUAL Xcode)
    target_include_directories(tcvm PUBLIC
        ${ZLIB_INCLUDE_DIRS}
        ${PNG_INCLUDE_DIRS}
        ${JPEG_INCLUDE_DIRS}
        ${MBEDTLS_INCLUDE_DIRS}
        ${SQLITE3_INCLUDE_DIRS}
    )
endif()

# TotalCross uses its own wrapper around libpng.
target_sources(tcvm PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/png/PngLoader.c")

# TotalCross uses its own stream/object wrapper around libjpeg.
target_sources(tcvm PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/jpeg/JpegLoader.c")
target_include_directories(tcvm PRIVATE ${JPEG_INCLUDE_DIRS})
